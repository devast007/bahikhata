package com.datamangement.devast007.bahikhata.utils

import android.content.Context
import com.datamangement.devast007.bahikhata.ui.GoogleSigninActivity

class LedgerSharePrefManger(context: Context?) {
    var mContext = context
    val sharePrefName = "ledger_shared_pref"
    val COMPANY_NAME = "COMPANY_NAME"
    fun setCompanyName(name: String) {
        val shf = mContext!!.getSharedPreferences(sharePrefName, Context.MODE_PRIVATE)
        var editor = shf.edit()
        editor.putString(COMPANY_NAME, name)
        editor.commit()
    }
    fun getCompanyName():String{
        var companyName = ""
        val shf = mContext!!.getSharedPreferences(sharePrefName, Context.MODE_PRIVATE)
        companyName = shf.getString(COMPANY_NAME,"")
        return companyName
    }

}