package com.darin.weex.language;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by darin on 5/18/16.
 */
public class WeexFileTYpeFactory extends FileTypeFactory {

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(WeexFileType.INSTANCE, WeexFileType.DEFAULT_EXTENSION);
    }
}
