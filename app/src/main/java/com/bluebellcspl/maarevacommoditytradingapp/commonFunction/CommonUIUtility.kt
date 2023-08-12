package com.bluebellcspl.maarevacommoditytradingapp.commonFunction

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bluebellcspl.maarevacommoditytradingapp.R


class CommonUIUtility(var context: Context) {
    val dialog = Dialog(context)
    val StatusAdapterArray = arrayOf<String>("Ok", "Partials OK", "Not Ok")
    public final fun showAlertWithOkButton(whatMessage: String) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setMessage(whatMessage)
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("OK", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                dialog?.dismiss()
            }
        })
        alertDialog.show()
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    interface Callback {
        fun onSuccess(t: Int)
    }

//    fun showAlert(title : String,msg : String,callback : Callback){
//        AlertDialog.Builder(context).setTitle(title).setMessage(msg).setPositiveButton("OK", object : DialogInterface.OnClickListener{
//            override fun onClick(p0: DialogInterface?, p1: Int) {
//                callback.onSucess(0)
//            }
//        }).show()
//    }

    fun showAlert(
        title: String?,
        msg: String?,
        callback: Callback
    ) {
        AlertDialog.Builder(context).setTitle(title).setMessage(msg).setPositiveButton(
            "OK"
        ) { dialog, which -> callback.onSuccess(0) }.setNegativeButton(
            "CANCEL"
        ) { dialog, which -> callback.onSuccess(-1) }.show()
    }

    public fun showProgress() {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null, false)
        dialog.setContentView(view.rootView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val lp = WindowManager.LayoutParams()
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes = lp
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    public fun dismissProgress() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    public fun hideKeyboard(view: View) {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    public fun getStatusArrayAdapter(): ArrayAdapter<String> {
        var adapter = ArrayAdapter<String>(
            context,
            R.layout.drop_down_item_adapter,R.id.textView,
            StatusAdapterArray
        )
        return adapter
    }


    fun setCustomArrayAdapter(stringArray: Array<String>): ArrayAdapter<String> {
        val arrayAdapter = ArrayAdapter<String>(
            context, R.layout.drop_down_item_adapter,R.id.textView,
            stringArray
        )
        return arrayAdapter
    }

    public fun getCustomArrayAdapter(stringArray: ArrayList<String>): ArrayAdapter<String> {
        var adapter = ArrayAdapter<String>(
            context,
            R.layout.drop_down_item_adapter,R.id.textView,
            stringArray
        )
        return adapter
    }
}