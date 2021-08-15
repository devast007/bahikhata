package com.datamangement.devast007.bahikhata.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.AddTransactionActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import kotlinx.android.synthetic.main.activity_add_transaction.*
import kotlinx.android.synthetic.main.activity_add_transaction.et_remarks


class DialogTransactionRemarksSubcategroy : DialogFragment() {


    private var mType: Int = -1
    var items: ArrayList<String>? = null
    private val TAG = "DialogFragmentForAmount"
    private var addTransactionActivity: AddTransactionActivity? = null
    private lateinit var mBottomSheetDialog: Dialog


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        addTransactionActivity = activity as AddTransactionActivity
        mBottomSheetDialog = Dialog(
            activity!!,
            R.style.MaterialDialogSheetAmount
        )
        mBottomSheetDialog.setContentView(R.layout.dialog_amount_selection)
        mBottomSheetDialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mBottomSheetDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.verticalMargin = 5.0f
        lp.gravity = Gravity.LEFT
        lp.windowAnimations = R.style.MaterialDialogSheetAnimation
        mBottomSheetDialog.window!!.attributes = lp
        var listView = mBottomSheetDialog.findViewById<ListView>(android.R.id.list)
        mType = arguments!!.get(LedgerDefine.KEY_SELECTION_TYPE) as Int
        when (mType) {
            LedgerDefine.SELECTION_TYPE_SUBCATEGORY -> items =
                addTransactionActivity!!.msubCategoryArrayList
            LedgerDefine.SELECTION_TYPE_REMARKS -> items =
                addTransactionActivity!!.mRemarksArrayList
        }
        listView.adapter = AdapterAmount()
        return mBottomSheetDialog

    }

    inner class AdapterAmount : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var tv: TextView? = null
            if (convertView == null) {
                tv = TextView(addTransactionActivity)
                tv.height = 200
                tv.setTypeface(tv.typeface, Typeface.BOLD)
                tv.width = ViewGroup.LayoutParams.MATCH_PARENT
                tv.textSize = 16F
                tv.gravity = Gravity.LEFT
                tv.setTextColor(addTransactionActivity!!.resources.getColor(R.color.selector_text_color_state))
            } else {
                tv = convertView as TextView
            }
            Log.d(
                TAG,
                "Error getting documents:  items?.get(position) ?: \"\" " + items?.get(position)
                    ?: ""
            )
            tv.text = items?.get(position) ?: ""

            tv.setOnClickListener(View.OnClickListener {
                val text = items?.get(position) ?: ""
                when (mType) {
                    LedgerDefine.SELECTION_TYPE_SUBCATEGORY -> {
                        addTransactionActivity!!.et_auto_complete_subcategory.setText(text)
                        addTransactionActivity!!.et_auto_complete_subcategory.setSelection(text.length)
                    }
                    LedgerDefine.SELECTION_TYPE_REMARKS -> {
                        addTransactionActivity!!.et_remarks.setText(text)
                        addTransactionActivity!!.et_remarks.setSelection(text.length)
                    }
                }
                mBottomSheetDialog.cancel()
            })
            return tv
        }

        override fun getItem(position: Int): Any {
            return items?.get(position) ?: ""
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return items!!.size
        }

    }


}
