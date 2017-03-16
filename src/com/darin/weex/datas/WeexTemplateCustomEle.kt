package com.darin.weex.datas

import com.darin.weex.utils.WeexCmd.inputStreamToString


/**
 * Created by darin on 5/17/16.
 */
object WeexTemplateCustomEle {
    lateinit var templateString: String

    fun initTemplateString() {
        templateString = inputStreamToString(WeexTemplateCustomEle::class.java.getResourceAsStream("/data/element"), true)
    }
}
