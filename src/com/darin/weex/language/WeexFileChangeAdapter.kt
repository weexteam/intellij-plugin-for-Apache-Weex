package com.darin.weex.language

import com.darin.weex.utils.TransformTasks
import com.darin.weex.utils.WeexUtils
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileAdapter
import com.intellij.openapi.vfs.VirtualFileEvent

/**
 * Created by darin on 03/11/2016.
 */
class WeexFileChangeAdapter : VirtualFileAdapter() {
    override fun beforeContentsChange(event: VirtualFileEvent) {
        super.beforeContentsChange(event)
    }


    override fun contentsChanged(event: VirtualFileEvent) {
        super.contentsChanged(event)

        val file = event.file


        if (!WeexFileType.isWeexLanguage(file)) {
            return
        }
        //transform
        WeexUtils.println("Will transform : " + event.file.path)
        TransformTasks.instance.addTransformTask(event.file)
    }
}
