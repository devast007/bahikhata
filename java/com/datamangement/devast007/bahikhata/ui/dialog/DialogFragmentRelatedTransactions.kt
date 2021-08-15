package com.datamangement.devast007.bahikhata.ui.dialog

import android.app.Dialog
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.TransactionViewActivity
import com.datamangement.devast007.bahikhata.ui.adapter.TransactionViewAdapter
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.TransactionDetails
import kotlinx.android.synthetic.main.activity_add_transaction.*


class DialogFragmentRelatedTransactions : DialogFragment(), ListAdapter {

    private val SUBCATEGORY: Int = 1
    private val TAG = "DialogSelectUserOrProject"
    private var transactionActivity: TransactionViewActivity? = null
    private lateinit var mBottomSheetDialog: Dialog
    private var mType: Int = -1
    private var simpleAdapter: SimpleAdapter? = null
    var mInflater: LayoutInflater? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        transactionActivity = activity as TransactionViewActivity
        mBottomSheetDialog = Dialog(
            requireActivity(),
            R.style.MaterialDialogSheet
        )
        mBottomSheetDialog.setContentView(R.layout.dialog_related_transactions)
        mBottomSheetDialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mBottomSheetDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.verticalMargin = 5.0f
        lp.gravity = Gravity.CENTER
        lp.windowAnimations = R.style.DialogAnimation
        mBottomSheetDialog.window!!.attributes = lp

        var listView = mBottomSheetDialog.findViewById<ListView>(R.id.listview_related_transactions)

        mInflater =
            transactionActivity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        listView.adapter =this!!

        return mBottomSheetDialog
    }

    override fun registerDataSetObserver(p0: DataSetObserver?) {

    }

    override fun unregisterDataSetObserver(p0: DataSetObserver?) {

    }

    override fun getCount(): Int {
        return transactionActivity!!.mRelatedTransactionsList.size
    }

    override fun getItem(p0: Int): Any {
        return transactionActivity!!.mRelatedTransactionsList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getView(p0: Int, convertView: View?, parent: ViewGroup?): View {
        var childHolder: ChildHolder? = null
        var view: View? = convertView
        if (view == null) {
            view = mInflater!!.inflate(R.layout.row_related_transactions, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }

        val transactionDetails: TransactionDetails =
            transactionActivity!!.mRelatedTransactionsList[p0]
        childHolder!!.tvSenderID.text = getSpannableString(
            transactionDetails.senderId,
            transactionActivity!!.mUsersMap[getUserId(transactionDetails.senderId)], 0
        )
        childHolder!!.tvRecevierID.text = getSpannableString(
            transactionDetails.receiverId,
            transactionActivity!!.mUsersMap[getUserId(transactionDetails.receiverId)], 0
        )
        childHolder!!.tvTransactionDate.text =
            LedgerUtils.getConvertDate(transactionDetails.transactionDate)
        childHolder!!.tvAmount.text = LedgerUtils.getRupeesFormatted(transactionDetails.amount)
        childHolder!!.tvRoundOff.text = "" + transactionDetails.roundOff
        childHolder!!.tvProjectID.text =
            transactionActivity!!.mProjectsMap[transactionDetails.projectId]
        childHolder!!.tvSubCategory.text = transactionDetails.subCategory



        childHolder!!.tvRemarks.text = transactionDetails.remarks
        childHolder!!.tvLoggedInId.text = getSpannableString(
            transactionDetails.loggedInID,
            transactionActivity!!.mUsersMap[transactionDetails.loggedInID], 0
        )
        childHolder!!.tvTimestamp.text = transactionDetails.timeStamp.toString()
        childHolder!!.tvTransactionID.text = transactionDetails.transactionID

        childHolder!!.tvDebitAccount.text = getSpannableString(
            transactionDetails.debitedTo,
            transactionActivity!!.mBankAccountsMap[transactionDetails.debitedTo], 0
        )
        childHolder!!.tvCreditAccount.text = getSpannableString(
            transactionDetails.creditedTo,
            transactionActivity!!.mBankAccountsMap[transactionDetails.creditedTo], 0
        )

        return view!!
    }

    class ChildHolder(view: View) {
        var tvSenderID = view.findViewById<TextView>(R.id.tv_sender_id)
        val tvRecevierID = view.findViewById<TextView>(R.id.tv_receiver_id)
        val tvTransactionDate = view.findViewById<TextView>(R.id.tv_transaction_date)
        var tvAmount = view.findViewById<TextView>(R.id.tv_transaction_amount)
        var tvRoundOff = view.findViewById<TextView>(R.id.tv_transaction_round_off)
        var tvProjectID = view.findViewById<TextView>(R.id.tv_project_id)
        var tvSubCategory = view.findViewById<TextView>(R.id.tv_subcategory)

        val tvDebitAccount = view.findViewById<TextView>(R.id.tv_transaction_debited_to)
        val tvCreditAccount = view.findViewById<TextView>(R.id.tv_transaction_credited_to)

        var tvTimestamp = view.findViewById<TextView>(R.id.tv_time_stamp)
        var tvLoggedInId = view.findViewById<TextView>(R.id.tv_log_in_id)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_remarks)
        val tvTransactionID = view.findViewById<TextView>(R.id.tv_transaction_id)
    }

    override fun getItemViewType(p0: Int): Int {
        return p0
    }

    override fun getViewTypeCount(): Int {
       return 1
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun areAllItemsEnabled(): Boolean {

        return false
    }

    override fun isEnabled(p0: Int): Boolean {
      return true
    }

    private fun getSpannableString(key: String?, value: String?, type: Int): SpannableString {
        if (key == null || value == null) return SpannableString(key)

        var bold = StyleSpan(Typeface.BOLD)
        val tempKey = "[$key]"
        val content = SpannableString("$tempKey $value")
        content.setSpan(bold, 0, tempKey.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (type == SUBCATEGORY) {
            content.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                0,
                tempKey.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return content
    }

    private fun getUserId(userAccount: String): String? {
        var userId: String = ""
        userId = userAccount.substring(2)
        return userId

    }

}
