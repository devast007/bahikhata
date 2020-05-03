package com.datamangement.devast007.bahikhata.pdf


import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.datamangement.devast007.bahikhata.ui.TransactionViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.PdfName
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class TransactionPDF(
    transactionViewActivity: TransactionViewActivity,
    columns: ArrayList<String>?
) {
    private val TOTAL = "TOTAL"
    private val SL = "SL"
    private val DATE = "DATE"
    private val AMOUNT = "AMOUNT"
    private val SENDER = "SENDER"
    private val RECEIVER = "RECEIVER"
    private val WORK = "WORK"
    private val ACCOUNT_NAME = "ACCOUNT NAME"
    private val REMARKS = "REMARKS"

    private val mTransactionViewActivity = transactionViewActivity
    private val mTransactionList = mTransactionViewActivity.mTransactionList
    private val mColumns = columns
    private val mHeading = mTransactionViewActivity.mPdfheading
    val sd = File(Environment.getExternalStorageDirectory(), "BahiKhata")


    inner class WatermarkPageEvent : PdfPageEventHelper() {
        var t: PdfTemplate? = null
        var total: Image? = null
        var FONT: Font = Font(Font.FontFamily.HELVETICA, 52f, Font.BOLD, GrayColor(0.85f))
        override fun onEndPage(writer: PdfWriter, document: Document?) {
            ColumnText.showTextAligned(
                writer.directContentUnder,
                Element.ALIGN_CENTER,
                Phrase(LedgerSharePrefManger(mTransactionViewActivity).getCompanyName(), FONT),
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
        val document = Document(PageSize.A4, -50f, -50f, 50f, 50f)
        val writer =
            PdfWriter.getInstance(document, FileOutputStream(sd.absolutePath + "$mHeading.pdf"))

        writer.pageEvent = WatermarkPageEvent()


        document.open()

        // set heading
        val headingParagraph: Paragraph = Paragraph(16f, mHeading)
        headingParagraph.alignment = Element.ALIGN_CENTER
        headingParagraph.spacingAfter = 30f
        document.add(headingParagraph)
        val maxColumn = 3 + mColumns!!.size
        var relativeWidths: FloatArray = FloatArray(maxColumn)
        var column = 0
        while (column < maxColumn) {
            var width = 0f
            when (column) {
                0 -> {
                    width = 1f
                }
                1, 2 -> {
                    width = 2f
                }
                else -> {
                    width = 3f
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
        table.addCell(AMOUNT) // amount

        var name = ""
        if (mColumns.contains(LedgerDefine.SENDER_NAME)) { // sender name
            table.addCell(SENDER)
        }

        if (mColumns.contains(LedgerDefine.RECEIVER_NAME)) { // receiver name
            table.addCell(RECEIVER)
        }


        if (mColumns.contains(LedgerDefine.NICKNAME)) { // project nick namee
            table.addCell(WORK)
        }

        if (mColumns.contains(LedgerDefine.PAYEE_NAME)) { // credit account
            table.addCell(ACCOUNT_NAME)
        }

        if (mColumns.contains(LedgerDefine.REMARK)) { // TRANSACTION REMARKS
            table.addCell(REMARKS)
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
        var sum: Long = 0


        for (details in mTransactionList) {
            if (details.isChecked) {
                table.addCell("$slNo")
                table.addCell(LedgerUtils.getConvertDate(details.transactionDate))
                var pdfCell = PdfPCell(Phrase("" + LedgerUtils.getRupeesFormatted(details.amount)))
                pdfCell.horizontalAlignment = PdfPCell.ALIGN_RIGHT
                table.addCell(pdfCell)
                if (mColumns.contains(LedgerDefine.SENDER_NAME)) {
                    table.addCell(
                        "" + mTransactionViewActivity.mUsersMap[details.senderId.substring(
                            2
                        )]
                    )
                }

                if (mColumns.contains(LedgerDefine.RECEIVER_NAME)) {
                    table.addCell(
                        "" + mTransactionViewActivity.mUsersMap[details.receiverId.substring(
                            2
                        )]
                    )

                }

                if (mColumns.contains(LedgerDefine.NICKNAME)) {
                    if (details.projectId.isEmpty()) {
                        table.addCell("")
                    } else {
                        table.addCell("" + mTransactionViewActivity.mProjectsMap[details.projectId])
                    }
                }

                if (mColumns.contains(LedgerDefine.PAYEE_NAME)) {
                    if (details.creditedTo.isEmpty()) {
                        table.addCell("")
                    } else {
                        table.addCell("" + mTransactionViewActivity.mBankAccountsMap[details.creditedTo])
                    }
                }

                if (mColumns.contains(LedgerDefine.REMARK)) {
                    if (details.remarks.isEmpty()) {
                        table.addCell("")
                    } else {
                        table.addCell("" + details.remarks)
                    }
                }

                sum += details.amount
                slNo++
            }
        }
        table.addCell("")
        table.addCell(TOTAL)
        val paragraphAmount = Paragraph("" + LedgerUtils.getRupeesFormatted(sum))
        paragraphAmount.alignment = Element.ALIGN_RIGHT
        table.addCell(paragraphAmount)
        for (extraColumn in mColumns) {
            table.addCell("")
        }
        // last line
        document.add(table);
        document.close();


        var newIntent = Intent(Intent.ACTION_VIEW)
        newIntent.setDataAndType(
            Uri.parse("file://${sd.absoluteFile}" + mHeading + ".pdf"),
            "application/pdf"
        )
        newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        mTransactionViewActivity!!.startActivity(newIntent)
    }
}

