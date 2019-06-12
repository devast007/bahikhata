package com.datamangement.devast007.bahikhata.excel

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.support.v4.content.LocalBroadcastManager
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.MaterialDetails
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

class MaterialsExcelSheet(context: Context?, name: String?, materialsList: ArrayList<MaterialDetails>) {


    companion object {
        private const val TAG = "MaterialsExcelSheet"
        private const val C_SL_NO: Int = 0

        private const val C_DATE: Int = 1
        private const val C_MATERIAL: Int = 2

        private const val C_RATE: Int = 3

        private const val C_QUANTITY: Int = 4

        private const val C_AMOUNT: Int = 5

        private const val C_SENDER_NAME: Int = 6

        private const val C_RECEIVER_NAME: Int = 7

        private const val C_PROJECT_ID: Int = 8

        private const val C_REMARKS: Int = 9

        private const val C_MATERIAL_ID: Int = 10

        private const val C_TIMESTAMP: Int = 11

        private const val C_SENDER_ID: Int = 12

        private const val C_RECEIVER_ID: Int = 13

        private const val C_LOGGED_ID: Int = 14
    }

    private val mMaterialsList = materialsList
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
        val csvFile = mName + "_Materials_" + time + ".xls"

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
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/users")
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

        val size = mMaterialsList.size

        for (i in 0 until size) {
            var materialDetails = mMaterialsList.get(i)
            val row = i + 2; // actual data start position

            mSheet!!.addCell(Number(C_SL_NO, row, (row - 1).toDouble(), cellFormat)) // serial no
            mSheet!!.addCell(Label(C_SENDER_ID, row, materialDetails.senderId, cellFormat))
            mSheet!!.addCell(Label(Companion.C_SENDER_NAME, row, mUsersMap[materialDetails.senderId], cellFormat))

            mSheet!!.addCell(Label(C_RECEIVER_ID, row, materialDetails.receiverId, cellFormat))
            mSheet!!.addCell(Label(C_RECEIVER_NAME, row, mUsersMap[materialDetails.receiverId], cellFormat))

            mSheet!!.addCell(Number(C_QUANTITY, row, materialDetails.quantity.toDouble(), cellFormat))
            mSheet!!.addCell(Number(C_RATE, row, materialDetails.rate.toDouble(), cellFormat))
            mSheet!!.addCell(Number(C_AMOUNT, row, materialDetails.amount.toDouble(), cellFormat))
            mSheet!!.addCell(Label(C_PROJECT_ID, row, materialDetails.projectId, cellFormat))
            mSheet!!.addCell(
                Label(
                    C_DATE,
                    row,
                    LedgerUtils.getConvertDate(materialDetails.date),
                    cellFormat
                )
            )
            mSheet!!.addCell(Label(C_MATERIAL_ID, row, materialDetails.materialID, cellFormat))
            mSheet!!.addCell(Label(C_MATERIAL, row, materialDetails.material, cellFormat))
            mSheet!!.addCell(Label(C_TIMESTAMP, row, materialDetails.timeStamp.toString(), cellFormat))
            mSheet!!.addCell(Label(C_LOGGED_ID, row, materialDetails.loggedInID, cellFormat))
            mSheet!!.addCell(Label(C_REMARKS, row, materialDetails.remarks, cellFormat))
        }

    }


    private fun addColumns() {
        val cellFont = WritableFont(WritableFont.COURIER, 12)
        cellFont.setBoldStyle(WritableFont.BOLD)

        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.GREY_25_PERCENT)

        addColumn(C_SL_NO, 1, mContext!!.getString(R.string.serial_no), cellFormat, mSheet!!)
        addColumn(C_DATE, 1, LedgerDefine.DATE, cellFormat, mSheet!!)
        addColumn(C_MATERIAL, 1, LedgerDefine.MATERIAL, cellFormat, mSheet!!)
        addColumn(C_RATE, 1, LedgerDefine.RATE, cellFormat, mSheet!!)
        addColumn(C_QUANTITY, 1, LedgerDefine.QUANTITY, cellFormat, mSheet!!)
        addColumn(C_AMOUNT, 1, LedgerDefine.AMOUNT, cellFormat, mSheet!!)
        addColumn(C_SENDER_NAME, 1, LedgerDefine.SENDER_NAME, cellFormat, mSheet!!)
        addColumn(C_RECEIVER_NAME, 1, LedgerDefine.RECEIVER_NAME, cellFormat, mSheet!!)
        addColumn(C_MATERIAL_ID, 1, LedgerDefine.MATERIAL_ID, cellFormat, mSheet!!)
        addColumn(C_REMARKS, 1, LedgerDefine.REMARK, cellFormat, mSheet!!)
        addColumn(C_TIMESTAMP, 1, LedgerDefine.TIME_STAMP, cellFormat, mSheet!!)
        addColumn(C_SENDER_ID, 1, LedgerDefine.SENDER_ID, cellFormat, mSheet!!)
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