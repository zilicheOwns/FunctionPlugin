package com.example.demoplugin.utils

import com.example.demoplugin.internal.ScanSetting
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * @author eddie
 * @since 2019/9/18
 */
class ScanUtil {

    private static final String FILE_SEP = File.separator
    /**
     * @param jarFile All jar files that are compiled into apk
     */
    static void scanJar(File inputJar) {
        if (inputJar) {
            def inputJarFile = new JarFile(inputJar)

            def optJar = new File(inputJar.getParent(), inputJar.name + ".opt")
            if (optJar.exists()) {
                optJar.delete()
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))
            Enumeration enumeration = inputJarFile.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = inputJarFile.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if (entryName.startsWith(ScanSetting.FUN_PACKAGE_NAME)) {
                    def bytes = scanClass(inputStream)
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }

            jarOutputStream.close()
            inputJarFile.close()

            if (inputJar.exists()) {
                inputJar.delete()
            }
            optJar.renameTo(inputJar)
        }
    }

    static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }


    static void transformSingleClassToFile(File inputFile, File outputFile, String inputBaseDir) {
        if (!inputBaseDir.endsWith(FILE_SEP)) {
            inputBaseDir = inputBaseDir + FILE_SEP
        }
        if (isFullQualifiedClass(inputFile.getAbsolutePath()
                .replace(inputBaseDir, "").replace(FILE_SEP, "."))) {
            FileUtils.touch(outputFile)
            byte[] bytes = scanClass(inputFile)
            FileOutputStream fos = new FileOutputStream(outputFile)
            fos.write(bytes)
            fos.close()
        } else {
            if (inputFile.isFile()) {
                FileUtils.touch(outputFile)
                FileUtils.copyFile(inputFile, outputFile)
            }
        }
    }

    static boolean isFullQualifiedClass(String fullQualifiedClassName) {
        return fullQualifiedClassName.endsWith(".class") &&
                !fullQualifiedClassName.contains("R\$") &&
                !fullQualifiedClassName.contains("R.class") &&
                !fullQualifiedClassName.contains("BuildConfig.class")
    }

    /**
     * scan class file
     * @param class file
     */
    static byte[] scanClass(File file) {
        return scanClass(new FileInputStream(file))
    }

    static byte[] scanClass(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, 0)
        ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    static class ScanClassVisitor extends ClassVisitor {

        boolean isHintClass

        ScanClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            isHintClass = FilterUtil.isMatchingClass(interfaces)
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions)
            //命中相关类（实现OnClickListener的类）并且命中相关方法（onclick方法）
            if (isHintClass && FilterUtil.isMatchingMethod(name, desc)) {
                return mv == null ? null : new ScanMethodVisitor(Opcodes.ASM5, mv)
            } else {
                return mv
            }
        }
    }

    static class ScanMethodVisitor extends MethodVisitor {

        boolean needSingleClick

        ScanMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv)
        }

        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationVisitor av = super.visitAnnotation(desc, visible)
            needSingleClick = needSingleClick(desc)
            return av
        }

        static boolean needSingleClick(String desc) {
            return "Lziliche/top/function/recyclerview/SingleClick;" == desc
        }

        @Override
        void visitCode() {
            super.visitCode()
            if (needSingleClick) {
                Label l0 = new Label()
                mv.visitLabel(l0)
                mv.visitVarInsn(Opcodes.ALOAD, 1)
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ziliche/top/function/recyclerview/ClickUtils", "isFastDoubleClick", "(Landroid/view/View;)Z", false)
                mv.visitInsn(Opcodes.POP)
                Label l1 = new Label()
                mv.visitLabel(l1)
                mv.visitInsn(Opcodes.RETURN)
                Label l2 = new Label()
                mv.visitLabel(l2)
            }
        }
    }

}