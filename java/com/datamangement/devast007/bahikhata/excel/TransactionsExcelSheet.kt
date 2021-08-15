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
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.TransactionDetails
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

class TransactionsExcelSheet(
    context: Context?,
    name: String?,
    transactionList: ArrayList<TransactionDetails>
) {
    private val mProjectsMap: HashMap<String, String> = HashMap()
    val mTransactionList = transactionList
    val mContext = context
    val mName = name

    private var mUsersMap: HashMap<String, String> = HashMap()
    private var mBankAccountsMap: HashMap<String, String> = HashMap()

    private var mWorkbook: WritableWorkbook? = null

    private var mSheet: WritableSheet? = null

    private var mFile: File? = null

    fun writeToSheet() {
        val sd = File(Environment.getExternalStorageDirectory(), "Download")
        val time = DateFormat.format("yyyy_MM_dd_HH_mm_ss", Calendar.getInstance().time).toString()
        log(" time =  $time")
        val csvFile = mName + "_TRANSACTION_" + time + ".xls"

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
            loadProjectsData()
        } catch (e: Exception) {
            log(" Exception =  " + e.message)
        }

        log(" writeToSheet =  " + mFile!!.absolutePath)
        // return mFile!!.absolutePath
    }

    private fun loadProjectsData() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        mProjectsMap[document.id.toString()] =
                            document.get(LedgerDefine.NICKNAME).toString()
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
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
                    loadBankAccounts()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
            })
    }

    private fun loadBankAccounts() {

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        mBankAccountsMap[document.get(LedgerDefine.BANK_ACCOUNT_ID).toString()] =
                            document.get(LedgerDefine.PAYEE_NAME).toString()
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
                mName + " ( " + DateFormat.format("dd/MM/yyyy", Calendar.getInstance().time)
                    .toString() + " ) ",
                cellFormat
            )
        )
        mSheet!!.mergeCells(0, 0, 13, 0) // (column, row , column, row)

    }

    private fun addRows() {
        val cellFont = WritableFont(WritableFont.COURIER, 10)
        cellFont.setBoldStyle(WritableFont.BOLD)
        cellFont.colour = Colour.DARK_GREEN
        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.WHITE)
        cellFormat.setBorder(Border.ALL, BorderLineStyle.HAIR)

        val size = mTransactionList.size

        for (i in 0 until size) {
            var transactionDetails = mTransactionList.get(i)
            val row = i + 2; // actual data start position

            mSheet!!.addCell(Number(C_SL_NO, row, (row - 1).toDouble(), cellFormat)) // serial no
            mSheet!!.addCell(Label(C_SENDER_ID, row, transactionDetails.senderId, cellFormat))
            mSheet!!.addCell(
                Label(
                    C_SENDER_NAME,
                    row,
                    mUsersMap[transactionDetails.senderId.substring(2)],
                    cellFormat
                )
            )

            mSheet!!.addCell(Label(C_RECEIVER_ID, row, transactionDetails.receiverId, cellFormat))
            mSheet!!.addCell(
                Label(
                    C_RECEIVER_NAME,
                    row,
                    mUsersMap[transactionDetails.receiverId.substring(2)],
                    cellFormat
                )
            )

            mSheet!!.addCell(
                Number(
                    C_AMOUNT,
                    row,
                    transactionDetails.amount.toDouble(),
                    cellFormat
                )
            )
            mSheet!!.addCell(
                Label(
                    C_PROJECT_NICKNAME,
                    row,
                    mProjectsMap[transactionDetails.projectId],
                    cellFormat
                )
            )

            mSheet!!.addCell(Label(C_PROJECT_ID, row, transactionDetails.projectId, cellFormat))
            mSheet!!.addCell(
                Label(
                    C_TRANSACTION_DATE,
                    row,
                    LedgerUtils.getConvertDate(transactionDetails.transactionDate),
                    cellFormat
                )
            )
            mSheet!!.addCell(
                Number(
                    C_TRANSACTION_TYPE,
                    row,
                    transactionDetails.transactionType.toDouble(),
                    cellFormat
                )
            )
            mSheet!!.addCell(
                Label(
                    C_TIMESTAMP,
                    row,
                    transactionDetails.timeStamp.toString(),
                    cellFormat
                )
            )

            mSheet!!.addCell(Label(C_DEBIT_ID, row, transactionDetails.debitedTo, cellFormat))
            mSheet!!.addCell(
                Label(
                    C_DEBIT_PAYEE,
                    row,
                    mBankAccountsMap[transactionDetails.debitedTo],
                    cellFormat
                )
            )

            mSheet!!.addCell(Label(C_CREDIT_ID, row, transactionDetails.creditedTo, cellFormat))
            mSheet!!.addCell(
                Label(
                    C_CREDIT_PAYEE,
                    row,
                    mBankAccountsMap[transactionDetails.creditedTo],
                    cellFormat
                )
            )

            mSheet!!.addCell(Label(C_PAYMENT_MODE, row, transactionDetails.paymentMode, cellFormat))
            mSheet!!.addCell(Label(C_LOGGEDIN_ID, row, transactionDetails.loggedInID, cellFormat))
            mSheet!!.addCell(
                Label(
                    C_TRANSACTION_ID,
                    row,
                    transactionDetails.transactionID,
                    cellFormat
                )
            )
            mSheet!!.addCell(Label(C_REMARKS, row, transactionDetails.remarks, cellFormat))
        }

    }

    private val C_SL_NO: Int = 0

    private val C_TRANSACTION_DATE: Int = 1

    private val C_SENDER_NAME: Int = 2

    private val C_RECEIVER_NAME: Int = 3

    private val C_AMOUNT: Int = 4
    private val C_PROJECT_NICKNAME: Int = 5


    private val C_REMARKS: Int = 6

    private val C_DEBIT_PAYEE: Int = 7

    private val C_CREDIT_PAYEE: Int = 8

    private val C_DEBIT_ID: Int = 9

    private val C_CREDIT_ID: Int = 10

    private val C_PROJECT_ID: Int = 11

    private val C_SENDER_ID: Int = 12

    private val C_RECEIVER_ID: Int = 13

    private val C_PAYMENT_MODE: Int = 14

    private val C_TRANSACTION_TYPE: Int = 15

    private val C_TIMESTAMP: Int = 16

    private val C_LOGGEDIN_ID: Int = 17

    private val C_TRANSACTION_ID: Int = 18

    private fun addColumns() {
        val cellFont = WritableFont(WritableFont.COURIER, 12)
        cellFont.setBoldStyle(WritableFont.BOLD)

        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.GREY_25_PERCENT)

        addColumn(C_SL_NO, 1, mContext!!.getString(R.string.serial_no), cellFormat, mSheet!!)
        addColumn(C_TRANSACTION_DATE, 1, LedgerDefine.TRANSACTION_DATE, cellFormat, mSheet!!)
        addColumn(C_SENDER_NAME, 1, LedgerDefine.SENDER_NAME, cellFormat, mSheet!!)
        addColumn(C_RECEIVER_NAME, 1, LedgerDefine.RECEIVER_NAME, cellFormat, mSheet!!)
        addColumn(C_AMOUNT, 1, LedgerDefine.AMOUNT, cellFormat, mSheet!!)
        addColumn(C_PROJECT_NICKNAME, 1, LedgerDefine.NICKNAME, cellFormat, mSheet!!)
        addColumn(C_REMARKS, 1, LedgerDefine.REMARK, cellFormat, mSheet!!)
        addColumn(C_DEBIT_PAYEE, 1, LedgerDefine.DEBIT_PAYEE, cellFormat, mSheet!!)
        addColumn(C_CREDIT_PAYEE, 1, LedgerDefine.CREDIT_PAYEE, cellFormat, mSheet!!)
        addColumn(C_DEBIT_ID, 1, LedgerDefine.DEBIT_ACCOUNT_ID, cellFormat, mSheet!!)
        addColumn(C_CREDIT_ID, 1, LedgerDefine.CREDIT_ACCOUNT_ID, cellFormat, mSheet!!)
        addColumn(C_PROJECT_ID, 1, LedgerDefine.PROJECT_ID, cellFormat, mSheet!!)
        addColumn(C_SENDER_ID, 1, LedgerDefine.SENDER_ID, cellFormat, mSheet!!)
        addColumn(C_RECEIVER_ID, 1, LedgerDefine.RECEIVER_ID, cellFormat, mSheet!!)
        addColumn(C_PAYMENT_MODE, 1, LedgerDefine.PAYMENT_MODE, cellFormat, mSheet!!)
        addColumn(C_TRANSACTION_TYPE, 1, LedgerDefine.TRANSACTION_TYPE, cellFormat, mSheet!!)
        addColumn(C_TIMESTAMP, 1, LedgerDefine.TIME_STAMP, cellFormat, mSheet!!)
        addColumn(C_LOGGEDIN_ID, 1, LedgerDefine.LOGGED_IN_ID, cellFormat, mSheet!!)
        addColumn(C_TRANSACTION_ID, 1, LedgerDefine.TRANSACTION_ID, cellFormat, mSheet!!)


    }

    private fun addColumn(
        c: Int,
        r: Int,
        colName: String?,
        cellFormat: WritableCellFormat,
        sheet: WritableSheet
    ) {
        sheet.addCell(Label(c, r, colName, cellFormat))
        var cell = sheet.getColumnView(0)
        cell.isAutosize = true
        cell = sheet.getRowView(r)
        cell.isAutosize = true
        sheet.setColumnView(c, cell)
    }

    internal var TAG = "WriteExcelSheetEMD"

    private fun log(str: String) {
        Log.d(TAG, "" + str)
    }
}