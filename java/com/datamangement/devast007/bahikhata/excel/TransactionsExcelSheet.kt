package com.datamangement.devast007.bahikhata.excel

import android.content.Context
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.TransactionDetails
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.Colour
import jxl.write.*
import java.io.File
import java.util.*

class TransactionsExcelSheet(context: Context?, name: String?, transactionList: ArrayList<TransactionDetails>) {
    val mTransactionList = transactionList
    val mContext = context
    val mName = name

    fun getFilePath(): String {
        val sd = File(Environment.getExternalStorageDirectory(), "BahiKhata")
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
        val file = File(bahiKhata, csvFile)
        try {
            file.createNewFile()
            val wbSettings = WorkbookSettings()
            wbSettings.locale = Locale("en", "EN")
            val workbook: WritableWorkbook
            workbook = Workbook.createWorkbook(file, wbSettings)
            //Excel sheet name. 0 represents first sheet
            val sheet = workbook.createSheet(mName, 0)
            // column and row
            addHeading(sheet)
            addColumns(sheet)
            addRows(sheet)

            workbook.write()
            workbook.close()
        } catch (e: Exception) {
            log(" Exception =  " + e.message)
        }

        log(" getFilePath =  " + file.absolutePath)
        return file.absolutePath
    }

    private fun addHeading(sheet: WritableSheet) {
        val cellFont = WritableFont(WritableFont.TIMES, 12)
        cellFont.setBoldStyle(WritableFont.BOLD)
        cellFont.colour = Colour.BLUE
        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.GOLD)
        cellFormat.alignment = Alignment.CENTRE
        sheet.addCell(
            Label(
                0,
                0,
                mName + " ( " + DateFormat.format("dd/MM/yyyy", Calendar.getInstance().time).toString() + " ) ",
                cellFormat
            )
        )
        sheet.mergeCells(0, 0, 13, 0) // (column, row , column, row)

    }

    private fun addRows(sheet: WritableSheet) {
        val cellFont = WritableFont(WritableFont.COURIER, 10)
        cellFont.setBoldStyle(WritableFont.BOLD)
        cellFont.colour = Colour.DARK_GREEN
        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.WHITE)
        cellFormat.setBorder(Border.ALL, BorderLineStyle.MEDIUM)

        val size = mTransactionList.size

        for (i in 0 until size) {
            var transactionDetails = mTransactionList.get(i)
            val row = i + 2; // actual data start position

            sheet.addCell(Label(0, row, "" + (row - 1))) // serial no
            sheet.addCell(Label(1, row, transactionDetails.senderId))
            sheet.addCell(Label(2, row, transactionDetails.receiverId))
            sheet.addCell(Label(3, row, "" + transactionDetails.amount))
            sheet.addCell(Label(4, row, transactionDetails.projectId))
            sheet.addCell(Label(5, row, LedgerUtils.getConvertDate(transactionDetails.transactionDate)))
            sheet.addCell(Label(6, row, "" + transactionDetails.transactionType))
            sheet.addCell(Label(7, row, transactionDetails.timeStamp.toString()))
            sheet.addCell(Label(8, row, transactionDetails.debitedTo))
            sheet.addCell(Label(9, row, transactionDetails.creditedTo))
            sheet.addCell(Label(10, row, transactionDetails.paymentMode))
            sheet.addCell(Label(11, row, transactionDetails.loggedInID))
            sheet.addCell(Label(12, row, transactionDetails.transactionID))
            sheet.addCell(Label(13, row, transactionDetails.remarks))
        }

    }

    private fun addColumns(sheet: WritableSheet) {
        val cellFont = WritableFont(WritableFont.COURIER, 12)
        cellFont.setBoldStyle(WritableFont.BOLD)

        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.GREY_25_PERCENT)

        addcolumn(0, 1, mContext!!.getString(R.string.serial_no), cellFormat, sheet)
        addcolumn(1, 1, LedgerDefine.SENDER_ID, cellFormat, sheet)
        addcolumn(2, 1, LedgerDefine.RECEIVER_ID, cellFormat, sheet)
        addcolumn(3, 1, LedgerDefine.AMOUNT, cellFormat, sheet)
        addcolumn(4, 1, LedgerDefine.PROJECT_ID, cellFormat, sheet)
        addcolumn(5, 1, LedgerDefine.TRANSACTION_DATE, cellFormat, sheet)
        addcolumn(6, 1, LedgerDefine.TRANSACTION_TYPE, cellFormat, sheet)
        addcolumn(7, 1, LedgerDefine.TIME_STAMP, cellFormat, sheet)
        addcolumn(8, 1, LedgerDefine.DEBIT_ACCOUNT_ID, cellFormat, sheet)
        addcolumn(9, 1, LedgerDefine.CREDIT_ACCOUNT_ID, cellFormat, sheet)
        addcolumn(10, 1, LedgerDefine.PAYMENT_MODE, cellFormat, sheet)
        addcolumn(11, 1, LedgerDefine.LOGGED_IN_ID, cellFormat, sheet)
        addcolumn(12, 1, LedgerDefine.TRANSACTION_ID, cellFormat, sheet)
        addcolumn(13, 1, LedgerDefine.REMARK, cellFormat, sheet)

    }

    private fun addcolumn(c: Int, r: Int, colName: String?, cellFormat: WritableCellFormat, sheet: WritableSheet) {
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