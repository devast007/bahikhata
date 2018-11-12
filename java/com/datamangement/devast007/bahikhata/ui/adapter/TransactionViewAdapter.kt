package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.TransactionViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.TransactionDetails

class TransactionViewAdapter(
    transactionViewActivity: TransactionViewActivity,
    transactionsList: ArrayList<TransactionDetails>
) : ExpandableListAdapter {
    val mTransactionViewActivity = transactionViewActivity
    val mTransactionsList = transactionsList
    var mInflater: LayoutInflater =
        mTransactionViewActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val mIsAdmin: Boolean = LedgerUtils.signInProfile!!.isAdmin

    override fun getChildrenCount(p0: Int): Int {
        return 1;
    }

    override fun getGroup(groupPos: Int): Any {
        return mTransactionsList.get(groupPos)
    }

    override fun onGroupCollapsed(p0: Int) {
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun registerDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getChild(group: Int, child: Int): Any {
        return mTransactionsList.get(group)
    }

    override fun onGroupExpanded(p0: Int) {
    }

    override fun getCombinedChildId(p0: Long, groudID: Long): Long {
        return groudID
    }

    override fun getGroupId(groudID: Int): Long {
        return groudID.toLong()
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return false
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getChildView(pos: Int, p1: Int, p2: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var view: View? = convertView
        var childHolder: ChildHolder? = null
        if (view == null) {
            view = mInflater.inflate(R.layout.row_transaction_child_view, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }


        val transactionDetails: TransactionDetails = mTransactionsList.get(pos)
        childHolder!!.tvSenderID.text = transactionDetails.senderId
        childHolder!!.tvRecevierID.text = transactionDetails.receiverId
        childHolder!!.tvTransactionDate.text = LedgerUtils.getConvertDate(transactionDetails.transactionDate)
        childHolder!!.tvAmount.text = LedgerUtils.getRupeesFormatted(transactionDetails.amount)
        childHolder!!.tvProjectID.text = transactionDetails.projectId
        childHolder!!.tvTransactionType.text = "" + transactionDetails.transactionType
        var strID = R.string.no
        if (transactionDetails.verified) {
            strID = R.string.yes
            childHolder.tvVerified.visibility = View.VISIBLE
            childHolder.btnLayout.visibility = View.GONE
            childHolder.tvVerified.setTextColor(Color.parseColor("#FF19732E"))
        } else {
            if (mIsAdmin) {
                childHolder.tvVerified.visibility = View.GONE
                childHolder.btnLayout.visibility = View.VISIBLE
            } else {
                childHolder.tvVerified.setTextColor(Color.parseColor("#FFDA3A2C"))
                childHolder.tvVerified.visibility = View.VISIBLE
                childHolder.btnLayout.visibility = View.GONE
            }
        }
        childHolder!!.tvVerified.setText(strID)
        childHolder!!.tvRemarks.text = transactionDetails.remarks
        childHolder!!.tvLoggedInId.text = transactionDetails.loggedInID
        childHolder!!.tvTimestamp.text = transactionDetails.timeStamp.toString()
        childHolder!!.tvTransactionID.text = transactionDetails.transactionID

        childHolder!!.tvDebitAccount.text = transactionDetails.debitedTo
        childHolder!!.tvCreditAccount.text = transactionDetails.creditedTo
        childHolder!!.tvPaymentMode.text = transactionDetails.paymentMode

        childHolder.btnVerify.setTag(R.string.tag_transaction_id, transactionDetails.transactionID)
        childHolder.btnDel.setTag(R.string.tag_transaction_id, transactionDetails.transactionID)
        childHolder.tvSenderID.setTag(R.string.tag_user_id, transactionDetails.senderId)
        childHolder.tvRecevierID.setTag(R.string.tag_user_id, transactionDetails.receiverId)
        childHolder.tvLoggedInId.setTag(R.string.tag_user_id, transactionDetails.loggedInID)
        childHolder.tvProjectID.setTag(R.string.tag_project_id, transactionDetails.projectId)
        childHolder.tvDebitAccount.setTag(R.string.tag_account_id, transactionDetails.debitedTo)
        childHolder.tvCreditAccount.setTag(R.string.tag_account_id, transactionDetails.creditedTo)

        childHolder.btnVerify.setOnClickListener(mTransactionViewActivity)
        childHolder.btnDel.setOnClickListener(mTransactionViewActivity)
        childHolder.tvSenderID.setOnClickListener(mTransactionViewActivity)
        childHolder.tvRecevierID.setOnClickListener(mTransactionViewActivity)
        childHolder.tvLoggedInId.setOnClickListener(mTransactionViewActivity)
        childHolder.tvProjectID.setOnClickListener(mTransactionViewActivity)
        childHolder.tvDebitAccount.setOnClickListener(mTransactionViewActivity)
        childHolder.tvCreditAccount.setOnClickListener(mTransactionViewActivity)

        return view
    }

    override fun areAllItemsEnabled(): Boolean {
        return true
    }

    override fun getChildId(p0: Int, childId: Int): Long {
        return childId.toLong()
    }

    override fun getCombinedGroupId(groupID: Long): Long {
        return groupID
    }

    override fun getGroupView(pos: Int, p1: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var view: View? = convertView
        var groupHolder: GroupHolder? = null
        if (view == null) {
            view = mInflater.inflate(R.layout.row_transaction_group_view, parent, false);
            groupHolder = GroupHolder(view)
            view.tag = groupHolder
        } else {
            groupHolder = view.tag as GroupHolder?
        }

        val transactionDetails: TransactionDetails = mTransactionsList.get(pos)
        groupHolder!!.tvSenderID.text = transactionDetails.senderId
        groupHolder!!.tvRecevierID.text = transactionDetails.receiverId
        groupHolder!!.tvTransactionDate.text = LedgerUtils.getConvertDate(transactionDetails.transactionDate)
        groupHolder!!.tvAmount.text = LedgerUtils.getRupeesFormatted(transactionDetails.amount)


        return view
    }

    override fun unregisterDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getGroupCount(): Int {
        return mTransactionsList.size
    }

    class GroupHolder(view: View) {
        var tvSenderID = view.findViewById<TextView>(R.id.tv_sender_id)
        val tvRecevierID = view.findViewById<TextView>(R.id.tv_receiver_id)
        val tvTransactionDate = view.findViewById<TextView>(R.id.tv_transaction_date)
        var tvAmount = view.findViewById<TextView>(R.id.tv_transaction_amount)


    }

    class ChildHolder(view: View) {
        var tvSenderID = view.findViewById<TextView>(R.id.tv_sender_id)
        val tvRecevierID = view.findViewById<TextView>(R.id.tv_receiver_id)
        val tvTransactionDate = view.findViewById<TextView>(R.id.tv_transaction_date)
        var tvAmount = view.findViewById<TextView>(R.id.tv_transaction_amount)
        var tvProjectID = view.findViewById<TextView>(R.id.tv_project_id)
        val tvTransactionType = view.findViewById<TextView>(R.id.tv_transaction_type)

        val tvDebitAccount = view.findViewById<TextView>(R.id.tv_transaction_debited_to)
        val tvCreditAccount = view.findViewById<TextView>(R.id.tv_transaction_credited_to)
        val tvPaymentMode = view.findViewById<TextView>(R.id.tv_payment_mode)

        val tvVerified = view.findViewById<TextView>(R.id.tv_verified)
        var tvTimestamp = view.findViewById<TextView>(R.id.tv_time_stamp)
        var tvLoggedInId = view.findViewById<TextView>(R.id.tv_log_in_id)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_remarks)
        val tvTransactionID = view.findViewById<TextView>(R.id.tv_transaction_id)

        var btnLayout = view.findViewById<LinearLayout>(R.id.ll_btn_layout)
        var btnDel = view.findViewById<Button>(R.id.btn_del)
        var btnVerify = view.findViewById<Button>(R.id.btn_verify)
    }

}