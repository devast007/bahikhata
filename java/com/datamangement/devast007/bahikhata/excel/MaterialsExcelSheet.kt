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
import java.io.File
import java.net.URL
import java.util.*

class MaterialsExcelSheet(
    context: Context?,
    name: String?,
    materialsList: ArrayList<MaterialDetails>,
    projectsMap: HashMap<String, String>,
    usersMap: HashMap<String, String>,
    materialOrService: HashMap<String, String>,
    serviceType: HashMap<String, HashMap<String, String>>,
    units: HashMap<String, String>,
    medium: HashMap<String, String>
) {

    private val mMaterialsList = materialsList
    private val mProjectsMap = projectsMap
    private val mUsersMap = usersMap
    private val mMaterialOrService = materialOrService
    private val mServiceType = serviceType
    private val mMedium = medium
    private val mUnits = units

    companion object {
        private const val TAG = "MaterialsExcelSheet"
        private const val C_SL_NO: Int = 0
        private const val C_DATE: Int = 1
        private const val C_SUPPLIER_NAME: Int = 2
        private const val C_PROJECT_NAME: Int = 3
        private const val C_SUBCATEGORY: Int = 4
        private const val C_MATERIAL_OR_SERVICE: Int = 5
        private const val C_TYPE_OF_SERVICE: Int = 6
        private const val C_MEDIUM_OF_SERVICE: Int = 7
        private const val C_QUANTITY: Int = 8
        private const val C_UNIT: Int = 9
        private const val C_RATE: Int = 10
        private const val C_AMOUNT: Int = 11
        private const val C_VEHICLE_NO: Int = 12
        private const val C_CHALLAN_NO: Int = 13
        private const val C_REPORTER: Int = 14
        private const val C_REMARKS: Int = 15
        private const val C_MATERIAL_ID: Int = 16
        private const val C_IMAGE_LINK: Int = 17

    }


    val mContext = context
    val mName = name

    private var mWorkbook: WritableWorkbook? = null

    private var mSheet: WritableSheet? = null

    private var mFile: File? = null

    fun writeToSheet() {
        val sd = File(Environment.getExternalStorageDirectory(), "Download")
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
        //try {
        mFile!!.createNewFile()
        val wbSettings = WorkbookSettings()
        wbSettings.locale = Locale("en", "EN")
        mWorkbook = Workbook.createWorkbook(mFile, wbSettings)
        //Excel sheet name. 0 represents first sheet
        mSheet = mWorkbook!!.createSheet(mName, 0)
        // column and row
        addDataToSheet()
        //loadUserData()
        //    } catch (e: Exception) {
        //      log(" Exception =  " + e.message)
        //}

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
        mSheet!!.mergeCells(0, 0, 16, 0) // (column, row , column, row)
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
                    C_SUPPLIER_NAME,
                    row,
                    mUsersMap[materialDetails.supplierId.substring(2)],
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
            mSheet!!.addCell(
                Number(
                    C_QUANTITY,
                    row,
                    materialDetails.quantity.toDouble(),
                    cellFormat
                )
            )
            mSheet!!.addCell(Label(C_UNIT, row, mUnits[materialDetails.unit], cellFormat))
            mSheet!!.addCell(Number(C_RATE, row, materialDetails.rate.toDouble(), cellFormat))
            mSheet!!.addCell(Number(C_AMOUNT, row, materialDetails.amount.toDouble(), cellFormat))

            mSheet!!.addCell(Label(C_VEHICLE_NO, row, materialDetails.vehicleNo, cellFormat))
            mSheet!!.addCell(Label(C_CHALLAN_NO, row, materialDetails.challanNo, cellFormat))
            mSheet!!.addCell(
                Label(
                    C_REPORTER,
                    row,
                    mUsersMap[materialDetails.reporterId.substring(2)],
                    cellFormat
                )
            )
            mSheet!!.addCell(Label(C_REMARKS, row, materialDetails.remarks, cellFormat))
            mSheet!!.addCell(Label(C_MATERIAL_ID, row, materialDetails.materialID, cellFormat))

            if (TextUtils.isEmpty(materialDetails.imageLink)) {
                mSheet!!.addCell(Label(C_IMAGE_LINK, row, "", cellFormat))
            } else {
                val url =URL(materialDetails.imageLink)
                var wrhl = WritableHyperlink(C_IMAGE_LINK, row, url)
                wrhl.setDescription("PHOTO")
                mSheet!!.addHyperlink(wrhl)
            }

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
        addColumn(C_SUPPLIER_NAME, 1, LedgerDefine.SUPPLIER_NAME, cellFormat, mSheet!!)
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
        addColumn(C_VEHICLE_NO, 1, LedgerDefine.VEHICLE_NO, cellFormat, mSheet!!)
        addColumn(C_CHALLAN_NO, 1, LedgerDefine.CHALLAN_NO, cellFormat, mSheet!!)
        addColumn(C_REPORTER, 1, LedgerDefine.REPORTER, cellFormat, mSheet!!)
        addColumn(C_REMARKS, 1, LedgerDefine.REMARK, cellFormat, mSheet!!)
        addColumn(C_MATERIAL_ID, 1, LedgerDefine.MATERIAL_ID, cellFormat, mSheet!!)
        addColumn(C_IMAGE_LINK, 1, LedgerDefine.IMAGE_LINK, cellFormat, mSheet!!)
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