package com.example.demoplugin.internal

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.android.utils.PathUtils
import com.example.demoplugin.SingleExtension
import com.example.demoplugin.utils.Logger
import com.example.demoplugin.utils.ScanUtil
import com.google.common.base.Preconditions
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.util.concurrent.Callable

/**
 *
 * @author eddie
 * @date 2019/9/18
 */
class SingleClickTransform extends Transform {

    Project project
    private WaitableExecutor waitableExecutor
    SingleExtension singleExt

    SingleClickTransform(Project project) {
        this.project = project
        singleExt = project.getExtensions().create("singleExt", SingleExtension)
        waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
    }


    @Override
    String getName() {
        return "DemoPlugin"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }


    @Override
    void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        long startTime = System.currentTimeMillis()
        SingleExtension singleExt = project.getExtensions().getByType(SingleExtension)
        Logger.make(project, singleExt.debug)

        //outputProvider
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        Preconditions.checkNotNull(outputProvider)

        if (!transformInvocation.isIncremental()) {
            outputProvider.deleteAll()
        }

        Logger.e("isIncremental is " + transformInvocation.isIncremental())
        //scan jar and directory，this is tao road.
        transformInvocation.inputs.each { TransformInput input ->

            //scan jar file to find classes
            input.jarInputs.each { JarInput jarInput ->
                File dest = getJarOutputFile(jarInput, outputProvider)
                File inputJar = jarInput.getFile()
                Status status = jarInput.getStatus()
                Logger.e("destName is " + jarInput.getName() + " ,status is " + status)
                processJar(inputJar, dest, status, transformInvocation.isIncremental())
            }

            // scan class files
            input.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = getDirectoryOutputFile(directoryInput, outputProvider)
                processDirectory(directoryInput, dest, transformInvocation.isIncremental())
            }
        }
        waitableExecutor.waitForTasksWithQuickFail(true)
        Logger.e("current cost time: " + (System.currentTimeMillis() - startTime) + "ms")
    }

    void processDirectory(DirectoryInput directoryInput, File outputFile, boolean isIncremental) {
        if (!isIncremental) {
            PathUtils.deleteRecursivelyIfExists(outputFile.toPath())
            transformDir(directoryInput.getFile(), outputFile)
            return
        }

        FileUtils.forceMkdir(outputFile)
        String srcDirPath = directoryInput.getFile().getAbsolutePath()
        String destDirPath = outputFile.getAbsolutePath()

        Map<File, Status> fileStatusMap = directoryInput.getChangedFiles()
        fileStatusMap.each { Map.Entry<File, Status> entry ->
            File inputFile = entry.getKey()
            Status status = entry.getValue()
            String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath)
            File destFile = new File(destFilePath)
            Logger.e("inputFilePath is " + inputFile.getAbsolutePath() + ", " +
                    "status is " + status + ", destFilePath is " + destDirPath)
            switch (status) {
                case Status.NOTCHANGED:
                    break
                case Status.REMOVED:
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                    break
                case Status.ADDED:
                case Status.CHANGED:
                    FileUtils.touch(destFile)
                    transformSingleClass(inputFile, destFile, srcDirPath)
                    break
            }
        }
    }

    static File getJarOutputFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        String destName = jarInput.name
        // rename jar files
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        return outputProvider.getContentLocation(
                destName + "_" + hexName,
                jarInput.contentTypes,
                jarInput.scopes,
                Format.JAR)
    }


    static File getDirectoryOutputFile(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        return outputProvider.getContentLocation(
                directoryInput.name,
                directoryInput.contentTypes,
                directoryInput.scopes,
                Format.DIRECTORY)
    }

    void processJar(File inputFile, File outputFile, Status status, boolean isIncremental) {
        if (!ScanUtil.shouldProcessPreDexJar(inputFile.getAbsolutePath())) {
            FileUtils.copyFile(inputFile, outputFile)
            return
        }

        if (!isIncremental) {
            transformJar(inputFile, outputFile)
            return
        }

        switch (status) {
            case Status.NOTCHANGED:
                break
            case Status.ADDED:
            case Status.CHANGED:
                transformJar(inputFile, outputFile)
                break
            case Status.REMOVED:
                if (outputFile.exists()) {
                    FileUtils.forceDelete(outputFile)
                }
                break
        }
    }


    void transformDir(final File inputDir, final File outputDir) {
        final String inputDirPath = inputDir.getAbsolutePath()
        final String outputDirPath = outputDir.getAbsolutePath()
        if (inputDir.isDirectory()) {
            inputDir.eachFileRecurse { File file ->
                String filePath = file.getAbsolutePath()
                File outputFile = new File(filePath.replace(inputDirPath, outputDirPath))
                transformSingleClass(file, outputFile, filePath)
            }
        }
    }

    void transformJar(File src, File dest) {
        waitableExecutor.execute(new Callable<Object>() {
            @Override
            Object call() throws Exception {
                ScanUtil.scanJar(src)
                // copy to dest
                FileUtils.copyFile(src, dest)
                return null
            }
        })
    }

    void transformSingleClass(File inputFile, File outputFile, String inputBaseDir) {
        waitableExecutor.execute(new Callable<Object>() {
            @Override
            Object call() throws Exception {
                ScanUtil.transformSingleClassToFile(inputFile, outputFile, inputBaseDir)
                return null
            }
        })
    }
}