package com.example.kotlindemoplugin

import jdk.internal.org.objectweb.asm.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * @author eddie
 * @date 2019/9/22
 */
object ScanUtil {

    fun scanJar(inputJar: File) {
        val inputJarFile = JarFile(inputJar)
        val optJar = File(inputJar.parent, inputJar.name + ".opt")
        if (optJar.exists()) {
            optJar.delete()
        }

        val jarOutputStream = JarOutputStream(FileOutputStream(optJar))
        val enumeration = inputJarFile.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement() as JarEntry
            val entryName = jarEntry.name
            val zipEntry = ZipEntry(entryName)
            val inputStream = inputJarFile.getInputStream(zipEntry)
            jarOutputStream.putNextEntry(zipEntry)
            if (entryName.startsWith(ScanSetting.FUN_PACKAGE_NAME)) {
                val byteArray = scanClass(inputStream)
                jarOutputStream.write(byteArray)
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

    fun shouldProcessPreDexJar(path: String): Boolean {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }


    fun transformSingleClassToFile(inputFile: File, outputFile: File, inputBaseDir: String) {
        var baseDir = inputBaseDir
        if (!inputBaseDir.endsWith(File.separator)) {
            baseDir += File.separator
        }
        if (isFullQualifiedClass(inputFile.absolutePath
                        .replace(baseDir, "").replace(File.separator, "."))) {
            FileUtils.touch(outputFile)
            val bytes = scanClass(inputFile)
            val fos = FileOutputStream(outputFile)
            fos.write(bytes)
            fos.close()
        } else {
            if (inputFile.isFile) {
                FileUtils.touch(outputFile)
                FileUtils.copyFile(inputFile, outputFile)
            }
        }
    }

    private fun isFullQualifiedClass(fullQualifiedClassName: String): Boolean {
        return fullQualifiedClassName.endsWith(".class") &&
                !fullQualifiedClassName.contains("R\$") &&
                !fullQualifiedClassName.contains("R.class") &&
                !fullQualifiedClassName.contains("BuildConfig.class")
    }


    private fun scanClass(inputStream: InputStream): ByteArray {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv = ScanClassVisitor(Opcodes.ASM5, cw)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    private fun scanClass(file: File): ByteArray {
        return scanClass(FileInputStream(file))
    }


    class ScanClassVisitor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {
        private var isHintClass: Boolean = false

        override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
            isHintClass = FilterUtil.isMatchingClass(interfaces)
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            val mv = cv.visitMethod(access, name, desc, signature, exceptions)
            if (isHintClass && FilterUtil.isMatchingMethod(name, desc)) {
                return ScanMethodVisitor(api, mv)
            }
            return mv
        }
    }

    class ScanMethodVisitor(api: Int, mv: MethodVisitor) : MethodVisitor(api, mv) {

        private var needSingleClick: Boolean = false

        override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
            val av = super.visitAnnotation(desc, visible)
            needSingleClick = needSingleClick(desc)
            return av
        }

        private fun needSingleClick(desc: String?): Boolean {
            return "Lziliche/top/function/recyclerview/SingleClick;" == desc
        }

        override fun visitCode() {
            super.visitCode()
            if (needSingleClick) {
                val l0 = Label()
                mv.visitLabel(l0)
                mv.visitVarInsn(Opcodes.ALOAD, 1)
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "ziliche/top/function/recyclerview/ClickUtils", "isFastDoubleClick", "(Landroid/view/View;)Z", false)
                mv.visitInsn(Opcodes.POP)
                val l1 = Label()
                mv.visitLabel(l1)
                mv.visitInsn(Opcodes.RETURN)
                val l2 = Label()
                mv.visitLabel(l2)

            }
        }
    }

}