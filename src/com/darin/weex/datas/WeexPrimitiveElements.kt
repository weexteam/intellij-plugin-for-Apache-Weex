package com.darin.weex.datas

import java.util.ArrayList
import java.util.Arrays

import com.darin.weex.utils.WeexCmd.inputStreamToString

/**
 * Created by darin on 5/17/16.
 */
object WeexPrimitiveElements {
    private val primitiveEles = ArrayList<String>()

    fun initPrivitiveEles() {
        val eles = inputStreamToString(WeexPrimitiveElements::class.java.getResourceAsStream("/data/primitive_elements"), false).trim().replace("\r\n", "").replace("\n", "").replace("\r", "").split(",")
        primitiveEles.clear()

        primitiveEles.addAll(eles)
    }


    fun isPrivitiveEle(ele: String): Boolean {
        if (primitiveEles.size == 0) {
            initPrivitiveEles()

        }
        return primitiveEles.contains(ele)
    }
}
