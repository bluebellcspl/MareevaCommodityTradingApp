package com.bluebellcspl.maarevacommoditytradingapp

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.CommonUIUtility
import com.bluebellcspl.maarevacommoditytradingapp.commonFunction.FileDownloader
import com.bluebellcspl.maarevacommoditytradingapp.databinding.ActivityChatImageBinding
import com.bluebellcspl.maarevacommoditytradingapp.model.ChatResponseModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ChatImageActivity : AppCompatActivity() {
    lateinit var binding:ActivityChatImageBinding
    private val commonUIUtility by lazy { CommonUIUtility(this) }
    private val fileDownloader by lazy { FileDownloader.getInstance(this@ChatImageActivity) }
    val TAG = "ChatImageActivity"
    lateinit var model:ChatResponseModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chat_image)
        setSupportActionBar(binding.toolbarChatImage.toolbar)
        supportActionBar!!.title = "Image View"
        model = intent.getParcelableExtra<ChatResponseModel>("ChatMessage")!!
        commonUIUtility.showProgress()
        Glide.with(this@ChatImageActivity)
            .load(model.FileMedia)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    commonUIUtility.dismissProgress()
                    commonUIUtility.showToast("Image Url Not Found!")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    commonUIUtility.dismissProgress()
                    return false
                }
            })
            .into(binding.myZoomageView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.image_download_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.btnDownloadImage->fileDownloader.downloadImage(model.FileMedia,"IMG_"+System.currentTimeMillis().toString()+".jpg")
        }
        return true
    }
}