package com.example.new_app.common.usable

import android.content.Context
import android.net.Uri

fun saveImageUriPermission(context: Context, uri: Uri) {
    val sharedPreferences = context.getSharedPreferences("uri_permissions", Context.MODE_PRIVATE)

    val editor = sharedPreferences.edit()
    //ACTION_OPEN_DOCUMENT  -  ACTION_CREATE_DOCUMENT
    //ACTION_OPEN_DOCUMENT_TREE



    editor.putString(uri.toString(), uri.toString())
    editor.apply()
}
