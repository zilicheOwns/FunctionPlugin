package com.example.demoplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import com.example.demoplugin.internal.SingleClickTransform
import com.example.demoplugin.utils.Logger
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class DemoPlugin implements Plugin<Project> {

    String APT_OPTION_NAME = "moduleName"

    @Override
    void apply(Project project) {
        //先判断project是否有这些插件
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (!isApp && !project.plugins.hasPlugin(LibraryPlugin) && !project.plugins.hasPlugin(TestPlugin)) {
            throw new GradleException("android plugin must be required.")
        }

        if (isApp) {
            Logger.make(project)
            Logger.e("current project name is " + project.getName())
            def android = project.extensions.getByType(AppExtension)
            //设置javaCompileOptions.annotationProcessorOptions
            android.defaultConfig.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
            android.productFlavors.all {
                it.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
            }
            android.registerTransform(new SingleClickTransform(project))

            android.getTransforms().each {
            }

//            android.applicationVariants.all { variant ->
//                def variantName = variant.name.capitalize()
//                project.logger.error("variant name is " + variantName)
//            }
        }
    }
}