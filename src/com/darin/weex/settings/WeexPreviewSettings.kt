package com.darin.weex.settings

import com.darin.weex.WeexAppConfig
import com.darin.weex.utils.WeexShow
import com.darin.weex.utils.WeexUtils
import com.intellij.openapi.util.text.StringUtil

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

/**
 * Created by darin on 5/25/16.
 */
class WeexPreviewSettings internal constructor() {
    var component: JPanel? = null
    var mNodeInstallPath: JTextField? = null
    var mNodePathSelectButton: JButton? = null
    var mNodePathJLable: JLabel? = null
    var mNodeInstallPathString = WeexAppConfig.nodeInstallPath
    var mMainPanel: JPanel? = null


    init {
        mNodePathSelectButton!!.addActionListener {
            val choosePath = WeexUtils.chooseNpmPath(component!!)
            if (!StringUtil.isEmpty(choosePath)) {
                mNodeInstallPath!!.text = choosePath
                mNodeInstallPathString = choosePath!!
                mNodeInstallPath!!.requestFocus()
            }
        }
    }


    internal fun save() {

        var npmPath = mNodeInstallPath!!.text.trim { it <= ' ' }

        val isNpmPathValid = WeexAppConfig.isNodePathValid(npmPath)

        if (!isNpmPathValid) {
            val message = StringBuilder("node installed path is invalid \r\n")
            if (WeexAppConfig.isNodePathValid(WeexAppConfig.defaultNodeInstallPath)) {
                npmPath = WeexAppConfig.defaultNodeInstallPath
                mNodeInstallPath!!.text = npmPath
                message.append("we will set the default path " + WeexAppConfig.defaultNodeInstallPath)
            }

            WeexShow.showMessage(message.toString())
        }


        WeexAppConfig.nodeInstallPath = (npmPath)

    }

    fun reset() {

        mNodeInstallPathString = WeexAppConfig.nodeInstallPath
        /**
         * server editor

         * set server editor visible according to WeexAppConfig.REMOTE_SERVER_OPEN
         */
        /**
         * node installed path
         */
        mNodeInstallPath!!.text = mNodeInstallPathString
    }

    val userSetNpmPath: String
        get() = mNodeInstallPath!!.text.trim { it <= ' ' }
}
