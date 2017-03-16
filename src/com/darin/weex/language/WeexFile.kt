package com.darin.weex.language

import com.darin.weex.language.parser.WeexParserDefinition
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.ex.http.HttpFileSystem
import com.intellij.openapi.vfs.impl.http.HttpVirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceResolver
import com.intellij.psi.impl.source.xml.XmlFileImpl
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlElementType

/**
 * Created by darin on 8/19/16.
 */
class WeexFile @JvmOverloads constructor(provider: FileViewProvider, type: IFileElementType = WeexParserDefinition.elementType) : XmlFileImpl(provider, type), FileReferenceResolver {

    override fun toString(): String {

        return "WeexFile:" + name
    }

    override fun getDocument(): XmlDocument? {
        val treeElement = calcTreeElement()

        val node = treeElement.findChildByType(XmlElementType.HTML_DOCUMENT)
        return if (node != null) node.psi as XmlDocument else null
    }

    override fun resolveFileReference(reference: FileReference, name: String): PsiFileSystemItem? {
        val file = virtualFile as? HttpVirtualFile ?: return null

        var parent: VirtualFile? = file
        if (!parent!!.isDirectory) {
            parent = parent.parent
            if (parent == null) {
                parent = file
            }
        }

        var childFile = parent.findChild(name)
        val fileSystem = parent.fileSystem as HttpFileSystem
        if (childFile == null) {
            childFile = fileSystem.createChild(parent, name, !reference.isLast)
        }
        if (childFile!!.isDirectory) {
            // pre create children
            var childParent: VirtualFile = childFile
            val references = reference.fileReferenceSet.allReferences
            var i = reference.index + 1
            val n = references.size
            while (i < n) {
                val childReference = references[i]
                childParent = fileSystem.createChild(childParent, childReference.decode(childReference.text), i != n - 1)
                i++
            }
            return manager.findDirectory(childFile)
        } else {
            return manager.findFile(childFile)
        }
    }

    override fun getVariants(reference: FileReference): Collection<Any> {
        return emptyList()
    }
}

