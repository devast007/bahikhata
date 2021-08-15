package com.datamangement.devast007.bahikhata.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.AddTransactionActivity
import kotlinx.android.synthetic.main.activity_add_transaction.*


class DialogFragmentForTransationAmount : DialogFragment() {



    val items: Array<String> = arrayOf(
        "1,000",
        "1,500",
        "2,000",
        "2,500",
        "3,000",
        "4,000",
        "5,000",

        "10,000",
        "15,000",
        "20,000",
        "25,000",
        "30,000",
        "40,000",
        "50,000"
        ,
        "1,00,000",
        "1,50,000",
        "2,00,000",
        "3,00,000",
        "4,00,000",
        "5,00,000",
        "6,00,000",
        "7,00,000",
        "8,00,000",
        "9,00,000",
        "10,00,000",

        "15,00,000",
        "20,00,000",
        "25,00,000",
        "30,00,000",
        "40,00,000",
        "50,00,000"
    )
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
        lp.width =300
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.verticalMargin = 5.0f
        lp.gravity = Gravity.RIGHT
        lp.windowAnimations = R.style.MaterialDialogSheetAnimationAmount
        mBottomSheetDialog.window!!.attributes = lp
        var listView = mBottomSheetDialog.findViewById<ListView>(android.R.id.list)
        listView.adapter = AdapterAmount()
        return mBottomSheetDialog

    }

    inner class AdapterAmount : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var tv: TextView? = null
            if (convertView == null) {
                tv = TextView(addTransactionActivity)
                tv.height = 90
                tv.setTypeface(tv.typeface, Typeface.BOLD)
                tv.width = ViewGroup.LayoutParams.WRAP_CONTENT
                tv.textSize = 16F
                tv.gravity = Gravity.RIGHT
            } else {
                tv = convertView as TextView
            }
            tv.text = items[position]
            if (position % 2 == 0) {
                tv.setTextColor(Color.BLACK)
            } else {
                tv.setTextColor(Color.BLUE)
            }
            tv.setOnClickListener(View.OnClickListener {
                val amount = items[position].replace(",", "")
                addTransactionActivity!!.et_amount.setText(amount)
                addTransactionActivity!!.et_amount.setSelection(amount.length)
                mBottomSheetDialog.cancel()
            })
            return tv
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return items.size
        }

    }


}
