package com.example.kotlindemoplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.android.utils.PathUtils
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File

/**
 * @author eddie
 * @date 2019/9/22
 */
class SingleTransform(private val project: Project) : Transform() {

    private val waitableExecutor: WaitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()

    override fun getName(): String {
        return "KotlinDemoPlugin"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }


    override fun transform(transformInvocation: TransformInvocation?) {

        val startTime = System.currentTimeMillis()
        val outputProvider = transformInvocation!!.outputProvider

        if (!transformInvocation.isIncremental) {
            outputProvider.deleteAll()
        }

        transformInvocation.inputs.forEach { transformInput ->
            transformInput.jarInputs.forEach {
                val dest = getJarOutputFile(it, outputProvider)
                val inputJar = it.file
                val status = it.status
                project.logger.error("dest path is " + dest.toPath() + ", jarInputName is " + it.name + ", status is " + status)
                processJar(inputJar, dest, status, transformInvocation.isIncremental)
            }


            transformInput.directoryInputs.forEach {
                val dest = getDirectoryOutputFile(it, outputProvider)
                processDirectory(it, dest, transformInvocation.isIncremental)
            }
        }
        waitableExecutor.waitForTasksWithQuickFail<Any>(true)
        project.logger.error("current cost time: " + (System.currentTimeMillis() - startTime) + "ms")
    }

    private fun processDirectory(directoryInput: DirectoryInput, outputFile: File, isIncremental: Boolean) {
        if (!isIncremental) {
            PathUtils.deleteRecursivelyIfExists(outputFile.toPath())
            transformDir(directoryInput.file, outputFile)
            return
        }

        FileUtils.forceMkdir(outputFile)
        val srcDirPath = directoryInput.file.absolutePath
        val destDirPath = outputFile.absolutePath
        val fileStatusMap = directoryInput.changedFiles
        fileStatusMap.forEach {
            val inputFile = it.key
            val status = it.value
            val destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
            val destFile = File(destFilePath)
            when (status) {
                Status.ADDED, Status.CHANGED -> {
                    FileUtils.touch(destFile)
                    transformSingleClass(inputFile, destFile, srcDirPath)
                }
                Status.REMOVED -> {
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                }
                else -> {
                }
            }

        }
    }


    private fun transformDir(inputDir: File, outputDir: File) {
        val inputDirPath = inputDir.absolutePath
        val outputDirPath = outputDir.absolutePath
        if (inputDir.isDirectory) {
            val inputDirTree = inputDir.walk()
            project.logger.error("inputDirTree is $inputDirTree, inputDir is $inputDirPath")
            inputDirTree
                    .filter { it.isFile }
                    .forEach {
                        val filePath = it.absolutePath
                        val outputFile = File(filePath.replace(inputDirPath, outputDirPath))
                        transformSingleClass(it, outputFile, filePath)
                    }

        }
    }


    private fun getJarOutputFile(jarInput: JarInput, outputProvider: TransformOutputProvider): File {
        var destName = jarInput.name
        val hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length - 4)
        }

        return outputProvider.getContentLocation(destName + "_" + hexName,
                jarInput.contentTypes,
                jarInput.scopes,
                Format.JAR
        )
    }


    private fun getDirectoryOutputFile(directoryInput: DirectoryInput, outputProvider: TransformOutputProvider): File {
        return outputProvider.getContentLocation(
                directoryInput.name,
                directoryInput.contentTypes,
                directoryInput.scopes,
                Format.DIRECTORY)
    }

    private fun processJar(inputFile: File, outputFile: File,
                           status: Status, isIncremental: Boolean) {

        if (!ScanUtil.shouldProcessPreDexJar(inputFile.absolutePath)) {
            FileUtils.copyFile(inputFile, outputFile)
            return
        }

        if (!isIncremental) {
            transformJar(inputFile, outputFile)
            return
        }

        when (status) {
            Status.ADDED, Status.CHANGED -> transformJar(inputFile, outputFile)
            Status.REMOVED -> {
                if (outputFile.exists()) {
                    FileUtils.forceDelete(outputFile)
                }
            }
            else -> {
            }
        }
    }

    private fun transformJar(inputFile: File, outputFile: File) {
        waitableExecutor.execute {
            ScanUtil.scanJar(inputFile)
            //copy to dest
            FileUtils.copyFile(inputFile, outputFile)
        }
    }

    private fun transformSingleClass(inputFile: File, outputFile: File, inputBaseDir: String) {
        waitableExecutor.execute {
            ScanUtil.transformSingleClassToFile(inputFile, outputFile, inputBaseDir)
        }
    }
}