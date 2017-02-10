package com.darin.weex.language;

import com.darin.weex.utils.TransformTasks;
import com.darin.weex.utils.WeexUtils;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by darin on 03/11/2016.
 */
public class WeexFileChangeAdapter extends VirtualFileAdapter {
    @Override
    public void beforeContentsChange(@NotNull VirtualFileEvent event) {
        super.beforeContentsChange(event);
    }


    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        super.contentsChanged(event);
        if (event == null)
            return;

        VirtualFile file = event.getFile();

        if (file == null)
            return;


        if (!WeexFileType.isWeexLanguage(file)) {
            return;
        }
        //transform
        WeexUtils.println("Will transform : " + event.getFile().getPath());
        TransformTasks.instance.addTransformTask(event.getFile());
    }
}
