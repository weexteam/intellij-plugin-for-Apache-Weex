package com.darin.weex.datas

import com.darin.weex.utils.WeexCmd.inputStreamToString
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*
import java.util.regex.Pattern

/**
 * Created by darin on 5/17/16.
 */
class WeexElementsParser(weexSelectText: WeexSelectText) {
    private val code: String
    private val pattern = "(?<=</).*?(?<=>)"
    private val file: VirtualFile?
    val custormElements = HashMap<String, String>()

    init {
        file = weexSelectText.virturlFile
        code = weexSelectText.text

        val currentPathFiles = currentPathFiles

        val m = Pattern.compile(pattern, Pattern.CANON_EQ).matcher(code)
        val elements = ArrayList<String>()
        val currentPath = File(file!!.parent.path)

        while (m.find())
            elements.add(m.group().split(">".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])


        val errorString = StringBuilder()

        for (ele in elements) {
            val eleFileName = ele + ".we"
            if (currentPathFiles.contains(eleFileName))
                custormElements.put(ele, currentPath.toString() + File.separator + eleFileName)
            else if (!WeexPrimitiveElements.isPrivitiveEle(ele))
                errorString.append("The custom element $ele is not found in $currentPath").append("\r\n")
        }

        val error = errorString.toString()
        if (!StringUtil.isEmpty(error))
            custormElements.put(ERROR_KEY, error)
        print("")
    }


    /**
     * @param transformToLowerCase get the real code in lower case
     * *
     * @return the real code
     */
    fun getRealCode(transformToLowerCase: Boolean): String {

        val realCode = StringBuilder()

        val entryIterator = custormElements.entries.iterator()

        var ele: Map.Entry<String, String>
        while (entryIterator.hasNext()) {
            ele = entryIterator.next()
            val realEle = getRealEle(ele.key, ele.value)
            if (StringUtil.isEmpty(realEle))
                continue
            realCode.append(realEle)
        }

        realCode.append(code)

        var realCodeString = realCode.toString()

        if (transformToLowerCase)
            for (element in custormElements.keys) {
                //upper case is unavailable for playground
                realCodeString = realCodeString.replace(element, element.toLowerCase())
            }

        return realCodeString
    }


    private val currentPathFiles: ArrayList<String>
        get() {
            val currentDirFiles = ArrayList<String>()
            if (file == null)
                return currentDirFiles

            val currentPath = File(file.parent.path)
            val files = currentPath.list()

            if (files != null)
                Collections.addAll(currentDirFiles, *files)

            return currentDirFiles
        }

    private fun getRealEle(name: String, path: String): String? {
        var realeEle: String? = null
        val weexScriptFile = File(path)
        if (weexScriptFile.isDirectory)
            return null
        var temp: String? = null
        try {
            temp = inputStreamToString(FileInputStream(weexScriptFile), true)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (!StringUtil.isEmpty(temp)) {
            realeEle = WeexTemplateCustomEle.templateString.replace("elename", name).replace("code", temp!!)
        }
        return realeEle
    }

    companion object {

        var ERROR_KEY = "weex_Plugin_Error_this_should_not_be_overrided"
    }


}
