package com.datamangement.devast007.bahikhata.excel

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.QuerySnapshot
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.Colour
import jxl.write.*
import jxl.write.Number
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class GstExcelSheet(context: Context?, name: String?, gstLists: ArrayList<GstDetails>) {


    companion object {
        private const val TAG = "MaterialsExcelSheet"
        private const val C_SL_NO: Int = 0

        private const val C_DATE: Int = 1
        private const val C_MATERIAL: Int = 2

        private const val C_GST_TAX: Int = 3

        private const val C_GST_TAX_PERCENTAGE: Int = 4

        private const val C_BILL_AMOUNT: Int = 5

        private const val C_SUPPLIER_NAME: Int = 6

        private const val C_RECEIVER_NAME: Int = 7

        private const val C_PROJECT_ID: Int = 8

        private const val C_REMARKS: Int = 9

        private const val C_GST_ID: Int = 10

        private const val C_TIMESTAMP: Int = 11

        private const val C_SUPPLIER_ID: Int = 12

        private const val C_RECEIVER_ID: Int = 13

        private const val C_LOGGED_ID: Int = 14
    }

    private val mGstList = gstLists
    val mContext = context
    val mName = name

    private var mUsersMap: HashMap<String, String> = HashMap()

    private var mWorkbook: WritableWorkbook? = null

    private var mSheet: WritableSheet? = null

    private var mFile: File? = null

    fun writeToSheet() {
        val sd = File(Environment.getExternalStorageDirectory(), "BahiKhata")
        val time = DateFormat.format("yyyy_MM_dd_HH_mm_ss", Calendar.getInstance().time).toString()
        log(" time =  $time")
        val csvFile = mName + "_GST_" + time + ".xls"

        val bahiKhata = File(sd.absolutePath)
        log(" csvFile=  $csvFile")
        //create directory if not exist
        log(" !bahiKhata.isDirectory() =  " + bahiKhata.isDirectory)
        if (!bahiKhata.isDirectory) {
            log(" bahiKhata.mkdirs() =  " + bahiKhata.mkdirs())
        }

        //file path
        mFile = File(bahiKhata, csvFile)
        try {
            mFile!!.createNewFile()
            val wbSettings = WorkbookSettings()
            wbSettings.locale = Locale("en", "EN")
            mWorkbook = Workbook.createWorkbook(mFile, wbSettings)
            //Excel sheet name. 0 represents first sheet
            mSheet = mWorkbook!!.createSheet(mName, 0)
            // column and row

            loadUserData()
        } catch (e: Exception) {
            log(" Exception =  " + e.message)
        }

        log(" writeToSheet =  " + mFile!!.absolutePath)
        // return mFile!!.absolutePath
    }

    private fun loadUserData() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                        mUsersMap[document.get(LedgerDefine.USER_ID).toString()] =
                            document.get(LedgerDefine.NAME).toString()

                    }
                    addDataToSheet()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
            })
    }


    private fun addDataToSheet() {
        addHeading()
        addColumns()
        addRows()
        mWorkbook!!.write()
        mWorkbook!!.close()
        LocalBroadcastManager.getInstance(mContext!!)
            .sendBroadcast(
                Intent(LedgerDefine.LOCAL_BROADCAST_INTENT).putExtra(
                    LedgerDefine.INTENT_EXTRA_FILE_PATH,
                    mFile!!.absolutePath
                )
            )
        Log.d(TAG, "broadcast sent")
    }

    private fun addHeading() {
        val cellFont = WritableFont(WritableFont.TIMES, 12)
        cellFont.setBoldStyle(WritableFont.BOLD)
        cellFont.colour = Colour.BLUE
        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.GOLD)
        cellFormat.alignment = Alignment.CENTRE
        mSheet!!.addCell(
            Label(
                0,
                0,
                mName + " ( " + DateFormat.format("dd/MM/yyyy", Calendar.getInstance().time).toString() + " ) ",
                cellFormat
            )
        )
        mSheet!!.mergeCells(0, 0, 14, 0) // (column, row , column, row)

    }

    private fun addRows() {
        val cellFont = WritableFont(WritableFont.COURIER, 10)
        cellFont.setBoldStyle(WritableFont.BOLD)
        cellFont.colour = Colour.DARK_GREEN
        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.WHITE)
        cellFormat.setBorder(Border.ALL, BorderLineStyle.HAIR)

        val size = mGstList.size

        for (i in 0 until size) {
            var gstDetails = mGstList.get(i)
            val row = i + 2; // actual data start position

            mSheet!!.addCell(Number(C_SL_NO, row, (row - 1).toDouble(), cellFormat)) // serial no
            mSheet!!.addCell(Label(C_SUPPLIER_ID, row, gstDetails.supplierID, cellFormat))
            mSheet!!.addCell(Label(Companion.C_SUPPLIER_NAME, row, mUsersMap[getUserId(gstDetails.supplierID)], cellFormat))

            mSheet!!.addCell(Label(C_RECEIVER_ID, row, gstDetails.receiverId, cellFormat))
            mSheet!!.addCell(Label(C_RECEIVER_NAME, row, mUsersMap[getUserId(gstDetails.receiverId)], cellFormat))

            mSheet!!.addCell(Number(C_GST_TAX_PERCENTAGE, row, gstDetails.gstTaxPercent.toDouble(), cellFormat))
            mSheet!!.addCell(Number(C_GST_TAX, row, gstDetails.gstTax.toDouble(), cellFormat))
            mSheet!!.addCell(Number(C_BILL_AMOUNT, row, gstDetails.billAmount.toDouble(), cellFormat))
            mSheet!!.addCell(Label(C_PROJECT_ID, row, gstDetails.projectId, cellFormat))
            mSheet!!.addCell(
                Label(
                    C_DATE,
                    row,
                    LedgerUtils.getConvertDate(gstDetails.date),
                    cellFormat
                )
            )
            mSheet!!.addCell(Label(C_GST_ID, row, gstDetails.gstId, cellFormat))
            mSheet!!.addCell(Label(C_MATERIAL, row, gstDetails.material, cellFormat))
            mSheet!!.addCell(Label(C_TIMESTAMP, row, gstDetails.timeStamp.toString(), cellFormat))
            mSheet!!.addCell(Label(C_LOGGED_ID, row, gstDetails.loggedInID, cellFormat))
            mSheet!!.addCell(Label(C_REMARKS, row, gstDetails.remarks, cellFormat))
        }

    }
    private fun getUserId(userAccount: String): String? {
        var userId: String = ""
        userId = userAccount.substring(2)
        return userId

    }

    private fun addColumns() {
        val cellFont = WritableFont(WritableFont.COURIER, 12)
        cellFont.setBoldStyle(WritableFont.BOLD)

        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.GREY_25_PERCENT)

        addColumn(C_SL_NO, 1, mContext!!.getString(R.string.serial_no), cellFormat, mSheet!!)
        addColumn(C_DATE, 1, LedgerDefine.DATE, cellFormat, mSheet!!)
        addColumn(C_MATERIAL, 1, LedgerDefine.MATERIAL, cellFormat, mSheet!!)
        addColumn(C_GST_TAX, 1, LedgerDefine.GST_TAX_AMOUNT, cellFormat, mSheet!!)
        addColumn(C_GST_TAX_PERCENTAGE, 1, LedgerDefine.GST_TAX_PERCENTAGE, cellFormat, mSheet!!)
        addColumn(C_BILL_AMOUNT, 1, LedgerDefine.GST_BILL_AMOUNT, cellFormat, mSheet!!)
        addColumn(C_SUPPLIER_NAME, 1, LedgerDefine.SENDER_NAME, cellFormat, mSheet!!)
        addColumn(C_RECEIVER_NAME, 1, LedgerDefine.RECEIVER_NAME, cellFormat, mSheet!!)
        addColumn(C_GST_ID, 1, LedgerDefine.MATERIAL_ID, cellFormat, mSheet!!)
        addColumn(C_REMARKS, 1, LedgerDefine.REMARK, cellFormat, mSheet!!)
        addColumn(C_TIMESTAMP, 1, LedgerDefine.TIME_STAMP, cellFormat, mSheet!!)
        addColumn(C_SUPPLIER_ID, 1, LedgerDefine.SENDER_ID, cellFormat, mSheet!!)
        addColumn(C_RECEIVER_ID, 1, LedgerDefine.RECEIVER_ID, cellFormat, mSheet!!)
        addColumn(C_LOGGED_ID, 1, LedgerDefine.LOGGED_IN_ID, cellFormat, mSheet!!)
        addColumn(C_PROJECT_ID, 1, LedgerDefine.PROJECT_ID, cellFormat, mSheet!!)
    }

    private fun addColumn(c: Int, r: Int, colName: String?, cellFormat: WritableCellFormat, sheet: WritableSheet) {
        sheet.addCell(Label(c, r, colName, cellFormat))
        var cell = sheet.getColumnView(0)
        cell.isAutosize = true
        cell = sheet.getRowView(r)
        cell.isAutosize = true
        sheet.setColumnView(c, cell)
    }

    private fun log(str: String) {
        Log.d(TAG, "" + str)
    }


}