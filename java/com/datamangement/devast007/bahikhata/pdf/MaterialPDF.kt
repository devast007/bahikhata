package com.datamangement.devast007.bahikhata.pdf


import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import com.datamangement.devast007.bahikhata.ui.MaterialViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.PdfName
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MaterialPDF(
    materialViewActivity: MaterialViewActivity,
    columns: ArrayList<String>?
) {

    private var mTotalQuantity: Double = 0.0
    var materialsCheck: ArrayList<String> = ArrayList<String>()
    private val TOTAL = "TOTAL"
    private val SL = "SL"
    private val DATE = "DATE"
    private val SUPPLIER = "SUPPLIER"
    private val REPORTER = "REPORTER"
    private val WORK = "WORK"
    private val MATERIAL = "MATERIAL"
    private val QUANTITY = "QUANTITY"
    private val UNIT = "UNIT"
    private val RATE = "RATE"
    private val AMOUNT = "AMOUNT"
    private val IMAGE = "IMAGE"
    private val REMARKS = "REMARKS"
    private val VEHICLE = "VEHICLE"
    private val CHALLAN = "CHALLAN"


    private val mMaterialViewActivity = materialViewActivity
    private val mMaterialList = mMaterialViewActivity.mMaterialsList
    private val mColumns = columns
    private val mHeading = mMaterialViewActivity.mPdfHeading
    private var sd: File? = null


    inner class WatermarkPageEvent : PdfPageEventHelper() {
        var t: PdfTemplate? = null
        var total: Image? = null
        var FONT: Font = Font(Font.FontFamily.HELVETICA, 52f, Font.BOLD, GrayColor(0.85f))
        override fun onEndPage(writer: PdfWriter, document: Document?) {
            ColumnText.showTextAligned(
                writer.directContentUnder,
                Element.ALIGN_CENTER,
                Phrase(
                    LedgerSharePrefManger(mMaterialViewActivity.mContext).getCompanyName(),
                    FONT
                ),
                297.5f,
                421f,
                45f
            )

            val table = PdfPTable(3)
            try {
                table.setWidths(intArrayOf(24, 24, 24))
                table.totalWidth = 770f
                table.defaultCell.fixedHeight = 20f
                table.defaultCell.border = Rectangle.BOTTOM
                table.addCell(Phrase(mHeading))
                table.defaultCell.horizontalAlignment = Element.ALIGN_RIGHT
                table.addCell(
                    Phrase(
                        java.lang.String.format("Page %d of", writer.pageNumber)
                    )
                )
                val cell = PdfPCell(total)
                cell.border = Rectangle.BOTTOM
                table.addCell(cell)
                val canvas = writer.directContent
                canvas.beginMarkedContentSequence(PdfName.ARTIFACT)
                table.writeSelectedRows(0, -1, 36f, 30f, canvas)
                canvas.endMarkedContentSequence()
            } catch (de: DocumentException) {
                throw ExceptionConverter(de)
            }
        }

        override fun onOpenDocument(writer: PdfWriter, document: Document?) {
            t = writer.directContent.createTemplate(30f, 16f)
            try {
                total = Image.getInstance(t)
                total!!.role = PdfName.ARTIFACT
            } catch (de: DocumentException) {
                throw ExceptionConverter(de)
            } catch (ioe: IOException) {
                throw ExceptionConverter(ioe)
            }
        }

        override fun onCloseDocument(writer: PdfWriter, document: Document?) {
            ColumnText.showTextAligned(
                t, Element.ALIGN_LEFT,
                Phrase(writer.pageNumber.toString()), 2f, 4f, 0f
            )
        }

    }

    fun createPdf() {
        sd = File(Environment.getExternalStorageDirectory(), "Download")
        val document = Document(PageSize.A4.rotate(), -50f, -50f, 50f, 50f)
        val writer =
            PdfWriter.getInstance(
                document,
                FileOutputStream(sd!!.absolutePath + "/" + "$mHeading.pdf")
            )

        writer.pageEvent = WatermarkPageEvent()


        document.open()

        // set heading
        val headingParagraph: Paragraph = Paragraph(16f, mHeading)
        headingParagraph.alignment = Element.ALIGN_CENTER
        headingParagraph.spacingAfter = 30f
        document.add(headingParagraph)
        val FIXED_COLUMNS = 6
        val maxColumn = FIXED_COLUMNS + mColumns!!.size
        var relativeWidths: FloatArray = FloatArray(maxColumn)
        var column = 0
        while (column < maxColumn) {
            var width = 0f
            when (column) {
                0 -> {
                    width = 1f
                }
                else -> {
                    width = 2f
                }
            }
            relativeWidths[column] = width
            column++
        }
        val table = PdfPTable(relativeWidths)

        // set column name in table
        table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(SL) // serial no
        table.addCell(DATE) // date

        if (mColumns.contains(LedgerDefine.PROJECT)) { // project nick namee
            table.addCell(WORK)
        }

        if (mColumns.contains(LedgerDefine.SUPPLIER_NAME)) { // supplier name
            table.addCell(SUPPLIER)
        }

        table.addCell(MATERIAL) // material

        table.addCell(QUANTITY) // quantity
        table.addCell(UNIT) // quantity
        table.addCell(RATE) // rate

        if (mColumns.contains(LedgerDefine.AMOUNT)) { // amount
            table.addCell(AMOUNT)
        }

        if (mColumns.contains(LedgerDefine.VEHICLE_NO)) { // amount
            table.addCell(VEHICLE)
        }

        if (mColumns.contains(LedgerDefine.CHALLAN_NO)) { // amount
            table.addCell(CHALLAN)
        }

        if (mColumns.contains(LedgerDefine.REMARK)) { // REMARKS
            table.addCell(REMARKS)
        }


        if (mColumns.contains(LedgerDefine.REPORTER)) { // reporter name
            table.addCell(REPORTER)
        }



        table.headerRows = 1
        var headerFont = Font()
        headerFont.color = BaseColor.WHITE
        val cells = table.getRow(0).cells
        for (j in cells.indices) {
            cells[j].backgroundColor = BaseColor.GRAY
            cells[j].phrase.font = headerFont
        }
        // set row items in table
        var slNo = 1
        var mTotalAmount: Double = 0.0


        for (details in mMaterialList) {
            if (details.isMaterialChecked) {

                table.addCell("$slNo") // serial no
                table.addCell(LedgerUtils.getConvertDate(details.date)) // date

                if (mColumns.contains(LedgerDefine.PROJECT)) { // project nick namee
                    table.addCell("" + mMaterialViewActivity.mProjectsMap[details.projectId])
                }

                if (mColumns.contains(LedgerDefine.SUPPLIER_NAME)) { // supplier name
                    table.addCell(
                        "" + mMaterialViewActivity.mUsersMap[details.supplierId.substring(
                            2
                        )]
                    )
                }

                // no of material check
                if (!materialsCheck.contains(details.materialOrService))
                    materialsCheck.add(details.materialOrService)


                table.addCell(mMaterialViewActivity.mMaterialOrService[details.materialOrService]) // material


                mTotalQuantity += details.quantity.toDouble()

                if (!TextUtils.isEmpty(details.imageLink)) {

                    var FONT: Font =
                        Font(Font.FontFamily.TIMES_ROMAN, 15f, Font.NORMAL, BaseColor.BLUE)
                    val phrase = Phrase()

                    val anchor = Anchor(details.quantity, FONT)
                    anchor.reference = details.imageLink

                    phrase.add(anchor)

                    table.addCell(phrase)
                } else {
                    table.addCell(details.quantity) // quantity
                }

                table.addCell(mMaterialViewActivity.mUnits[details.unit]) // quantity
                table.addCell(details.rate) // rate

                if (mColumns.contains(LedgerDefine.AMOUNT)) { // amount
                    var pdfCell =
                        PdfPCell(Phrase("" + LedgerUtils.getRupeesFormatted(details.amount)))
                    pdfCell.horizontalAlignment = PdfPCell.ALIGN_RIGHT
                    table.addCell(pdfCell)
                }

                if (mColumns.contains(LedgerDefine.VEHICLE_NO)) { // amount
                    table.addCell("" + details.vehicleNo)
                }

                if (mColumns.contains(LedgerDefine.CHALLAN_NO)) { // amount
                    table.addCell("" + details.challanNo)
                }

                if (mColumns.contains(LedgerDefine.REMARK)) { // REMARKS
                    table.addCell("" + details.remarks)
                }


                if (mColumns.contains(LedgerDefine.REPORTER)) { // reporter name
                    table.addCell(
                        "" + mMaterialViewActivity.mUsersMap[details.reporterId.substring(
                            2
                        )]
                    )
                }

                mTotalAmount += details.amount.toDouble()
                slNo++
            }
        }


        table.addCell("") // serial no
        table.addCell("") // date

        if (mColumns.contains(LedgerDefine.PROJECT)) { // project nick namee
            table.addCell("")
        }

        if (mColumns.contains(LedgerDefine.SUPPLIER_NAME)) { // supplier name
            table.addCell("")
        }

        table.addCell("") // material
        if (materialsCheck.size == 1)
            table.addCell("" + mTotalQuantity) // quantity

        table.addCell("") // quantity
        table.addCell("") // rate

        if (mColumns.contains(LedgerDefine.AMOUNT)) { // amount
            val paragraphAmount =
                Paragraph("" + LedgerUtils.getRupeesFormatted(mTotalAmount.toLong()))
            paragraphAmount.alignment = Element.ALIGN_RIGHT
            table.addCell(paragraphAmount)
        }

        if (mColumns.contains(LedgerDefine.REMARK)) { // REMARKS
            table.addCell("")
        }


        if (mColumns.contains(LedgerDefine.REPORTER)) { // reporter name
            table.addCell("")
        }

        // last line
        document.add(table);
        document.close();


        var newIntent = Intent(Intent.ACTION_VIEW)

        newIntent.setDataAndType(
            Uri.parse("file://${sd!!.absoluteFile}/" + mHeading + ".pdf"),
            "application/pdf"
        )
        newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        mMaterialViewActivity!!.mContext!!.startActivity(newIntent)
    }
}

