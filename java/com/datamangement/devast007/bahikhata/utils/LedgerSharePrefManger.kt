package com.datamangement.devast007.bahikhata.utils

import android.content.Context

class LedgerSharePrefManger(context: Context?) {
    var mContext = context
    val sharePrefName = "ledger_shared_pref"
    val COMPANY_NAME = "COMPANY_NAME"
    val COMPANY_ID = "COMPANY_ID"
    fun setCompanyID(id: String) {
        val shf = mContext!!.getSharedPreferences(sharePrefName, Context.MODE_PRIVATE)
        var editor = shf.edit()
        editor.putString(COMPANY_ID, id)
        editor.commit()
    }
    fun getCompanyID():String{
        var companyID = ""
        val shf = mContext!!.getSharedPreferences(sharePrefName, Context.MODE_PRIVATE)
        companyID = shf.getString(COMPANY_ID,"")!!
        return companyID
    }

    fun setCompanyName(name: String) {
        val shf = mContext!!.getSharedPreferences(sharePrefName, Context.MODE_PRIVATE)
        var editor = shf.edit()
        editor.putString(COMPANY_NAME, name)
        editor.commit()
    }
    fun getCompanyName():String{
        var companyName = ""
        val shf = mContext!!.getSharedPreferences(sharePrefName, Context.MODE_PRIVATE)
        companyName = shf.getString(COMPANY_NAME,"")!!
        return companyName
    }

}