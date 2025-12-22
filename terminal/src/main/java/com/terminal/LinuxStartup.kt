package com.terminal

import android.util.Log
import java.io.File
import java.io.IOException

object LinuxStartup {

    fun extractAssets(context: android.content.Context) {
        val internalStorageDir = context.filesDir
        val assetManager = context.assets
        val filesToExtract = listOf("alpine_", "proot_", "start.sh")

        for (fileName in filesToExtract) {
            try {
                val inputStream = assetManager.open(fileName)
                val outFile = File(internalStorageDir, fileName)

                inputStream.use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d("Extraction","Extracted: ${outFile.absolutePath}")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Extraction","Failed to extract $fileName: ${e.message}")
            }
        }
    }

    fun startLinuxEnvironment(context: android.content.Context): String {
        // Check if the assets are already exists in target directory. if not, only then extract them
        val internalStorageDir = context.filesDir.absolutePath
        //mkdir if not exists
        val dir = File(internalStorageDir)

        val alpineFile = File(internalStorageDir, "alpine_")
        val prootFile = File(internalStorageDir, "proot_")
        val startScriptFile = File(internalStorageDir, "start.sh")

        if (!alpineFile.exists() || !prootFile.exists() || !startScriptFile.exists()) {
            extractAssets(context)
        }

        startScriptFile.setExecutable(true)

        return startScriptFile.absolutePath
    }
}