package com.datamangement.devast007.bahikhata.excel

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.MaterialDetails
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

class StatementExcelSheet(
    context: Context?,
    name: String?,
    materialsList: ArrayList<MaterialDetails>,
    projectsMap: HashMap<String, String>,
    usersMap: HashMap<String, String>,
    bankAccounts: HashMap<String, String>,
    materialOrService: HashMap<String, String>,
    serviceType: HashMap<String, HashMap<String, String>>,
    units: HashMap<String, String>,
    medium: HashMap<String, String>
) {

    private var mBalance: Double = 0.0
    private val mMaterialsList = materialsList
    private val mProjectsMap = projectsMap
    private val mUsersMap = usersMap
    private val mMaterialOrService = materialOrService
    private val mServiceType = serviceType
    private val mMedium = medium
    private val mUnits = units
    private val mBankAccounts = bankAccounts

    companion object {
        private const val TAG = "MaterialsExcelSheet"
        private const val C_SL_NO: Int = 0
        private const val C_DATE: Int = 1
        private const val C_PROJECT_NAME: Int = 2
        private const val C_SUBCATEGORY: Int = 3
        private const val C_MATERIAL_OR_SERVICE: Int = 4
        private const val C_TYPE_OF_SERVICE: Int = 5
        private const val C_MEDIUM_OF_SERVICE: Int = 6
        private const val C_QUANTITY: Int = 7
        private const val C_UNIT: Int = 8
        private const val C_RATE: Int = 9
        private const val C_AMOUNT: Int = 10
        private const val C_PAYMENT: Int = 11
        private const val C_BALANCE: Int = 12
        private const val C_PAID_BY: Int = 13
        private const val C_PAID_TO: Int = 14
        private const val C_BANK_ACCOUNT: Int = 15
        private const val C_VEHICLE_NO: Int = 16
        private const val C_CHALLAN_NO: Int = 17
        private const val C_REMARKS: Int = 18
        private const val C_ROUND_OFF: Int = 19

    }


    val mContext = context
    val mName = name

    private var mWorkbook: WritableWorkbook? = null

    private var mSheet: WritableSheet? = null

    private var mFile: File? = null

    fun writeToSheet() {
        val sd = File(Environment.getExternalStorageDirectory(), "Download")
        val time = DateFormat.format("yyyy_MM_dd_HH_mm_ss", Calendar.getInstance().time).toString()
        var name = mName!!.split(" ")[0].trim()
        log(" mName  =  $name")
        val csvFile = "statement_$time.xls"

        val bahiKhata = File(sd.absolutePath)
        log(" csvFile=  $csvFile")
        //create directory if not exist
        log(" !bahiKhata.isDirectory() =  " + bahiKhata.isDirectory)
        if (!bahiKhata.isDirectory) {
            log(" bahiKhata.mkdirs() =  " + bahiKhata.mkdirs())
        }

        //file path
        mFile = File(bahiKhata, csvFile)
        //try {
        mFile!!.createNewFile()
        val wbSettings = WorkbookSettings()
        wbSettings.locale = Locale("en", "EN")
        mWorkbook = Workbook.createWorkbook(mFile, wbSettings)
        //Excel sheet name. 0 represents first sheet
        mSheet = mWorkbook!!.createSheet(mName, 0)
        addDataToSheet()

        log(" writeToSheet =  " + mFile!!.absolutePath)
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
        mSheet!!.mergeCells(0, 0, 19, 0) // (column, row , column, row)
        Log.d("TAG", " add heading finshed.............")

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
            var materialDetails = mMaterialsList[i]
            val row = i + 2; // actual data start position

            mSheet!!.addCell(Number(C_SL_NO, row, (row - 1).toDouble(), cellFormat)) // serial no
            mSheet!!.addCell(
                Label(
                    C_DATE,
                    row,
                    LedgerUtils.getConvertDate(materialDetails.date),
                    cellFormat
                )
            )

            mSheet!!.addCell(
                Label(
                    C_PROJECT_NAME,
                    row,
                    mProjectsMap[materialDetails.projectId],
                    cellFormat
                )
            )

            mSheet!!.addCell(
                Label(
                    C_SUBCATEGORY,
                    row,
                    materialDetails.subCategory,
                    cellFormat
                )
            )

            mSheet!!.addCell(
                Label(
                    C_MATERIAL_OR_SERVICE,
                    row,
                    mMaterialOrService[materialDetails.materialOrService],
                    cellFormat
                )
            )

            if (!TextUtils.isEmpty(materialDetails.serviceType))
                mSheet!!.addCell(
                    Label(
                        C_TYPE_OF_SERVICE,
                        row,
                        mServiceType[materialDetails.materialOrService]!![materialDetails.serviceType]!!,
                        cellFormat
                    )
                )

            mSheet!!.addCell(
                Label(
                    C_MEDIUM_OF_SERVICE,
                    row,
                    mMedium[materialDetails.medium],
                    cellFormat
                )
            )
            if (!TextUtils.isEmpty(materialDetails.quantity)) {
                mSheet!!.addCell(
                    Number(
                        C_QUANTITY,
                        row,
                        materialDetails.quantity.toDouble(),
                        cellFormat
                    )
                )
            }
            mSheet!!.addCell(Label(C_UNIT, row, mUnits[materialDetails.unit], cellFormat))
            mSheet!!.addCell(Label(C_RATE, row, materialDetails.rate, cellFormat))

            var amount = 0.0
            if (!TextUtils.isEmpty(materialDetails.amount)) {
                amount = materialDetails.amount.toDouble()
            }

            var payment = materialDetails.payment.toDouble()

            var roundOff = materialDetails.roundOff

            if (amount > 0) {
                mSheet!!.addCell(Number(C_AMOUNT, row, amount, cellFormat))
            }
            if (payment > 0)
                mSheet!!.addCell(Number(C_PAYMENT, row, payment, cellFormat))

            //balance //  amount- payment
            mBalance = mBalance + amount - payment - roundOff
            mSheet!!.addCell(Number(C_BALANCE, row, mBalance, cellFormat))

            if (!TextUtils.isEmpty(materialDetails.paidBy)) {
                mSheet!!.addCell(
                    Label(
                        C_PAID_BY,
                        row,
                        mUsersMap[materialDetails.paidBy.substring(2)],
                        cellFormat
                    )
                )
            }
            if (!TextUtils.isEmpty(materialDetails.paidTo)) {

                mSheet!!.addCell(
                    Label(
                        C_PAID_TO,
                        row,
                        mUsersMap[materialDetails.paidTo.substring(2)],
                        cellFormat
                    )
                )
            }

            if (!TextUtils.isEmpty(materialDetails.bankAccount)) {

                mSheet!!.addCell(
                    Label(
                        C_BANK_ACCOUNT,
                        row,
                        mBankAccounts[materialDetails.bankAccount],
                        cellFormat
                    )
                )
            }


            mSheet!!.addCell(Label(C_VEHICLE_NO, row, materialDetails.vehicleNo, cellFormat))
            mSheet!!.addCell(Label(C_CHALLAN_NO, row, materialDetails.challanNo, cellFormat))

            mSheet!!.addCell(Label(C_REMARKS, row, materialDetails.remarks, cellFormat))
            if (roundOff > 0)
                mSheet!!.addCell(
                    Number(
                        C_ROUND_OFF,
                        row,
                        materialDetails.roundOff.toDouble(),
                        cellFormat
                    )
                )

            Log.d("TAG", " add row finshed.............")
        }
    }


    private fun addColumns() {
        val cellFont = WritableFont(WritableFont.COURIER, 12)
        cellFont.setBoldStyle(WritableFont.BOLD)

        val cellFormat = WritableCellFormat(cellFont)
        cellFormat.setBackground(Colour.GREY_25_PERCENT)

        addColumn(C_SL_NO, 1, mContext!!.getString(R.string.serial_no), cellFormat, mSheet!!)
        addColumn(C_DATE, 1, LedgerDefine.DATE, cellFormat, mSheet!!)
        addColumn(C_PROJECT_NAME, 1, LedgerDefine.PROJECT, cellFormat, mSheet!!)
        addColumn(C_SUBCATEGORY, 1, LedgerDefine.SUBCATEGORY, cellFormat, mSheet!!)
        addColumn(
            C_MATERIAL_OR_SERVICE,
            1,
            LedgerDefine.MATERIAL_OR_SERVICE_NAME,
            cellFormat,
            mSheet!!
        )
        addColumn(C_TYPE_OF_SERVICE, 1, LedgerDefine.SERVICE_TYPE, cellFormat, mSheet!!)
        addColumn(C_MEDIUM_OF_SERVICE, 1, LedgerDefine.SERVICE_MEDIUM, cellFormat, mSheet!!)
        addColumn(C_QUANTITY, 1, LedgerDefine.QUANTITY, cellFormat, mSheet!!)
        addColumn(C_UNIT, 1, LedgerDefine.UNIT, cellFormat, mSheet!!)
        addColumn(C_RATE, 1, LedgerDefine.RATE, cellFormat, mSheet!!)
        addColumn(C_AMOUNT, 1, LedgerDefine.AMOUNT, cellFormat, mSheet!!)
        addColumn(C_PAYMENT, 1, LedgerDefine.PAYMENT, cellFormat, mSheet!!)
        addColumn(C_BALANCE, 1, LedgerDefine.BALANCE, cellFormat, mSheet!!)
        addColumn(C_PAID_BY, 1, LedgerDefine.PAID_BY, cellFormat, mSheet!!)
        addColumn(C_PAID_TO, 1, LedgerDefine.PAID_TO, cellFormat, mSheet!!)
        addColumn(C_BANK_ACCOUNT, 1, LedgerDefine.BANK_ACCOUNT, cellFormat, mSheet!!)
        addColumn(C_VEHICLE_NO, 1, LedgerDefine.VEHICLE_NO, cellFormat, mSheet!!)
        addColumn(C_CHALLAN_NO, 1, LedgerDefine.CHALLAN_NO, cellFormat, mSheet!!)
        addColumn(C_REMARKS, 1, LedgerDefine.REMARK, cellFormat, mSheet!!)
        addColumn(C_ROUND_OFF, 1, LedgerDefine.ROUND_OFF, cellFormat, mSheet!!)

        Log.d("TAG", " add coloumn finshed.............")
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

    private fun log(str: String) {
        Log.d(TAG, "" + str)
    }


}