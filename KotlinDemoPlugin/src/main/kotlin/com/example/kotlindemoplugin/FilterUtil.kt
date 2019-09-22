package com.example.kotlindemoplugin

/**
 * @author eddie
 */
object FilterUtil {

    /**
     * 是否实现android/view/View$OnClickListener接口
     * @param interfaces 类的实现接口
     * @return true 匹配
     */

    fun isMatchingClass(interfaces: Array<out String>?): Boolean {
        return isMatchingInterfaces(interfaces, "android/view/View\$OnClickListener")
    }


    /**`
     * 接口名是否匹配
     * @param interfaces    类的实现接口
     * @param interfaceName 需要匹配的接口名
     */
    private fun isMatchingInterfaces(interfaces: Array<out String>?, interfaceName: String): Boolean {
        var isMatch = false
        // 是否满足实现的接口
        interfaces?.forEach { anInterface ->
            if (anInterface == interfaceName) {
                isMatch = true
            }
        }
        return isMatch
    }


    /**
     * 是否匹配View的onClick(View v)方法
     * @param name 方法名
     * @param desc 参数的方法的描述符
     * @return true 匹配
     */
    fun isMatchingMethod(name: String?, desc: String?): Boolean {
        return name == "onClick" && desc == "(Landroid/view/View;)V"
    }
}
