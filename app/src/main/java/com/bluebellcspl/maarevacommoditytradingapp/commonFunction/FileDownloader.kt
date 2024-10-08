package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.bluebellcspl.maarevacommoditytradingapp.fragment.ProfileFragment
import java.io.File

class FileDownloader private constructor(private val context: Context) {
    val TAG = "FileDownloader"
    fun downloadFile(fileUrl: String, fileName: String,description:String) {
        Log.d(TAG, "downloadFile: URL : $fileUrl")
        Log.d(TAG, "downloadFile: FILENAME : $fileName")
        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setTitle("Downloading $fileName")
        request.setDescription(description)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // Set the destination directory and file name
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        // Enqueue the download
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Register a BroadcastReceiver to receive download complete notification
        val onCompleteReceiver = MyDownloadReceiver(downloadId)
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onCompleteReceiver, filter, Context.RECEIVER_EXPORTED)
        }else
        {
            context.registerReceiver(onCompleteReceiver, filter)
        }

        Toast.makeText(context, "Downloading $fileName", Toast.LENGTH_SHORT).show()
    }

    fun downloadZipFile(fileUrl: String, fileName: String,description:String,fragment: ProfileFragment) {
        Log.d(TAG, "downloadFile: URL : $fileUrl")
        Log.d(TAG, "downloadFile: FILENAME : $fileName")
        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setTitle("Downloading $fileName")
        request.setDescription(description)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // Set the destination directory and file name
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        // Enqueue the download
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val onCompleteReceiver = MyZipDownloadReceiver(downloadId,fragment)
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onCompleteReceiver, filter, Context.RECEIVER_EXPORTED)
        }else
        {
            context.registerReceiver(onCompleteReceiver, filter)
        }

//        Toast.makeText(context, "Downloading $fileName", Toast.LENGTH_SHORT).show()

    }

    fun downloadImage(fileUrl: String, fileName: String) {
        Log.d(TAG, "downloadImage: URL : $fileUrl")
        Log.d(TAG, "downloadImage: IMAGE_NAME : $fileName")

        // Get the download directory
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        // Check if the directory exists, if not create it
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Set the destination directory and file name
        val destinationFile = File(directory, "Maareva/Images/$fileName")

        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setTitle("Downloading $fileName")
        request.setDescription("Downloading Image")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationUri(Uri.fromFile(destinationFile))

        // Enqueue the download
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Register a BroadcastReceiver to receive download complete notification
        val onCompleteReceiver = MyDownloadReceiver(downloadId)
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onCompleteReceiver, filter, Context.RECEIVER_EXPORTED)
        }else
        {
            context.registerReceiver(onCompleteReceiver, filter)
        }

        Toast.makeText(context, "Downloading $fileName", Toast.LENGTH_SHORT).show()
    }

    // BroadcastReceiver to handle download complete event
    inner class MyDownloadReceiver(private val downloadId: Long) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                Toast.makeText(context, "Download complete!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class MyZipDownloadReceiver(private val downloadId: Long,val fragment: ProfileFragment) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                Toast.makeText(context, "Backup Downloaded completed!", Toast.LENGTH_SHORT).show()
                fragment.logout()
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FileDownloader? = null

        fun getInstance(context: Context): FileDownloader {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FileDownloader(context).also { INSTANCE = it }
            }
        }
    }
}
