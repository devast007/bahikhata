package com.datamangement.devast007.bahikhata.ui.dialog

import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.pdf.MaterialPDF
import com.datamangement.devast007.bahikhata.ui.MaterialViewActivity
import com.datamangement.devast007.bahikhata.ui.TransactionViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine


class DialogPDFColumnSelectionMaterial : DialogFragment() {

    private val TAG = "DialogFragmentForAmount"
    var materialViewActivity: MaterialViewActivity? = null
    private lateinit var mBottomSheetDialog: Dialog
    val column: ArrayList<String> = ArrayList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        materialViewActivity = activity as MaterialViewActivity
        mBottomSheetDialog = Dialog(
            requireActivity(),
            R.style.MaterialDialogSheetAmount
        )
        mBottomSheetDialog.setContentView(R.layout.dialog_pdf_column_selection_material)
        mBottomSheetDialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mBottomSheetDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.verticalMargin = 5.0f
        lp.gravity = Gravity.TOP
        lp.windowAnimations = R.style.MaterialDialogSheetAnimation
        mBottomSheetDialog.window!!.attributes = lp


        val btnPdf: Button = mBottomSheetDialog.findViewById(R.id.btn_create_pdf)
        btnPdf.setOnClickListener(View.OnClickListener {
            if ((mBottomSheetDialog.findViewById(R.id.cb_sender_name_pdf) as CheckBox).isChecked) {
                column.add(LedgerDefine.SUPPLIER_NAME!!)
            }

            if ((mBottomSheetDialog.findViewById(R.id.cb_receiver_name_pdf) as CheckBox).isChecked) {
                column.add(LedgerDefine.REPORTER!!)
            }

            if ((mBottomSheetDialog.findViewById(R.id.cb_work_name_pdf) as CheckBox).isChecked) {
                column.add(LedgerDefine.PROJECT!!)
            }

            if ((mBottomSheetDialog.findViewById(R.id.cb_amount_pdf) as CheckBox).isChecked) {
                column.add(LedgerDefine.AMOUNT)
            }

            if ((mBottomSheetDialog.findViewById(R.id.cb_vehicle_pdf) as CheckBox).isChecked) {
                column.add(LedgerDefine.VEHICLE_NO)
            }

            if ((mBottomSheetDialog.findViewById(R.id.cb_challan_pdf) as CheckBox).isChecked) {
                column.add(LedgerDefine.CHALLAN_NO)
            }

            if ((mBottomSheetDialog.findViewById(R.id.cb_remarks) as CheckBox).isChecked) {
                column.add(LedgerDefine.REMARK)
            }

            PdfAsyncTask().execute()
            btnPdf.text = "Please Wait...."
            btnPdf.isEnabled = false

        })
        return mBottomSheetDialog

    }


    inner class PdfAsyncTask : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {
            MaterialPDF(materialViewActivity!!, column).createPdf()
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            mBottomSheetDialog.cancel()
        }

    }


}
