/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2017 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.nbt

import com.demonwav.mcdev.nbt.editor.CompressionSelection
import com.demonwav.mcdev.nbt.editor.NbtToolbar
import com.demonwav.mcdev.nbt.lang.NbttFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.zip.GZIPOutputStream

class NbtVirtualFile(private val backingFile: VirtualFile, private val project: Project) : VirtualFile() {

    var bytes: ByteArray
    val isCompressed: Boolean
    lateinit var toolbar: NbtToolbar

    init {
        val (rootCompound, isCompressed) = Nbt.buildTagTree(backingFile.inputStream)
        this.bytes = rootCompound.toString().toByteArray()
        this.isCompressed = isCompressed
    }

    override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) {
        backingFile.refresh(asynchronous, recursive, postRunnable)
    }

    override fun getLength() = bytes.size.toLong()
    override fun getFileSystem() = backingFile.fileSystem
    override fun getPath() = backingFile.path
    override fun isDirectory() = false
    override fun getTimeStamp() = backingFile.timeStamp
    override fun getModificationStamp() = 0L
    override fun getName() = backingFile.name + ".nbtt"
    override fun contentsToByteArray() = bytes
    override fun isValid() = backingFile.isValid
    override fun getInputStream() = ByteArrayInputStream(bytes)
    override fun getParent() = backingFile
    override fun getChildren() = emptyArray<VirtualFile>()
    override fun isWritable() = backingFile.isWritable
    override fun getOutputStream(requestor: Any, newModificationStamp: Long, newTimeStamp: Long) =
        VfsUtilCore.outputStreamAddingBOM(NbtOutputStream(requestor, this, project, toolbar.selection), this)
}

private class NbtOutputStream(
    private val requestor: Any,
    private val file: NbtVirtualFile,
    private val project: Project,
    private val compressionSelection: CompressionSelection
) : ByteArrayOutputStream() {
    override fun close() {
        file.bytes = toByteArray()

        val nbttFile = PsiManager.getInstance(project).findFile(file) as NbttFile
        val rootTag = nbttFile.getRootCompound().getRootCompoundTag()

        // just to be safe
        file.parent.bom = null
        val filteredStream = when (compressionSelection) {
            CompressionSelection.GZIP -> GZIPOutputStream(file.parent.getOutputStream(requestor))
            CompressionSelection.UNCOMPRESSED -> file.parent.getOutputStream(requestor)
        }

        DataOutputStream(filteredStream).use { stream ->
            rootTag.write(stream)
        }
    }
}
