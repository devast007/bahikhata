package com.datamangement.devast007.bahikhata.excel

import android.content.Context
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.utils.BankAccountDetail
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.Colour
import jxl.write.*
import java.io.File
import java.util.*

class BankAccountsExcelSheet(context: Context?, accountList: ArrayList<BankAccountDetail>) {
    private val mAccountList = accountList
    val mContext = context

    fun getFilePath(): String {
        val sd = File(Environment.getExternalStorageDirectory(), "BahiKhata")
        val time = DateFormat.format("yyyy_MM_dd_HH_mm_ss", Calendar.getInstance().time).toString()
        log(" time =  $time")
        val csvFile = "BANK_ACCOUNTS_$time.xls"

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
            val sheet = workbook.createSheet("BANK_ACCOUNTS", 0)
            // column and row
            addHeading(sheet)
            addColumns(sheet)
            addRows(sheet)

            workbook.write()
            workbook.close()
        } catch (e: Exception) {
            log(" Exception =  " + e.message)
        }

        log(" writeToSheet =  " + file.absolutePath)
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
                "BANK_ACCOUNTS" + " ( " + DateFormat.format("dd/MM/yyyy", Calendar.getInstance().time).toString() + " ) ",
                cellFormat
            )
        )
        sheet.mergeCells(0, 0, 8, 0) // (column, row , column, row)

    }

    private fun addRows(sheet: WritableSheet) {
        val cellFont = WritableFont(WritableFont.COURIER, 10)
        cellFont.setBoldStyle(WritableFont.BOLD)
        cellFont.colour = Colour.DARK_GREEN
        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.WHITE)
        cellFormat.setBorder(Border.ALL, BorderLineStyle.MEDIUM)

        val size = mAccountList.size

        for (i in 0 until size) {
            var accountDetail = mAccountList.get(i)
            val row = i + 2; // actual data start position

            sheet.addCell(Label(0, row, "" + (row - 1))) // serial no
            sheet.addCell(Label(1, row, accountDetail.id))
            sheet.addCell(Label(2, row, accountDetail.payee))
            sheet.addCell(Label(3, row, accountDetail.accountNo))
            sheet.addCell(Label(4, row, accountDetail.ifscCode))
            sheet.addCell(Label(5, row, accountDetail.branch))
            sheet.addCell(Label(6, row, accountDetail.remarks))
            sheet.addCell(Label(7, row, accountDetail.timestamp.toString()))
            sheet.addCell(Label(8, row, "" + accountDetail.amount))
        }

    }

    private fun addColumns(sheet: WritableSheet) {
        val cellFont = WritableFont(WritableFont.COURIER, 12)
        cellFont.setBoldStyle(WritableFont.BOLD)

        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.GREY_25_PERCENT)

        addcolumn(0, 1, mContext!!.getString(R.string.serial_no), cellFormat, sheet)
        addcolumn(1, 1, LedgerDefine.BANK_ACCOUNT_ID, cellFormat, sheet)
        addcolumn(2, 1, LedgerDefine.PAYEE_NAME, cellFormat, sheet)
        addcolumn(3, 1, LedgerDefine.BANK_ACCOUNT_NUMBER, cellFormat, sheet)
        addcolumn(4, 1, LedgerDefine.IFSC_CODE, cellFormat, sheet)
        addcolumn(5, 1, LedgerDefine.BANK_ACCOUNT_BRANCH_NAME, cellFormat, sheet)
        addcolumn(6, 1, LedgerDefine.REMARK, cellFormat, sheet)
        addcolumn(7, 1, LedgerDefine.TIME_STAMP, cellFormat, sheet)
        addcolumn(8, 1, LedgerDefine.AMOUNT, cellFormat, sheet)

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