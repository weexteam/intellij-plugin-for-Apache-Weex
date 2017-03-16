package com.darin.weex.language

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

/**
 * Created by darin on 5/18/16.
 */
class WeexFileTYpeFactory : FileTypeFactory() {

    override fun createFileTypes(consumer: FileTypeConsumer) {
        consumer.consume(WeexFileType.INSTANCE, WeexFileType.DEFAULT_EXTENSION)
    }
}
