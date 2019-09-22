package com.example.kotlindemoplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author eddie
 * @date 2019/9/21
 */
class KotlinDemoPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (isApp) {
            project.logger.error("kotlin: current project name is " + project.name)

            val android = project.extensions.getByType(AppExtension::class.java)
            android.defaultConfig.javaCompileOptions.annotationProcessorOptions.argument(aptOptionName, project.name)
            android.productFlavors.all {
                it.javaCompileOptions.annotationProcessorOptions.argument(aptOptionName, project.name)
            }
            android.registerTransform(SingleTransform(project))
        }
    }

    companion object {
        const val aptOptionName: String = "moduleName"
    }
}