package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.BankAccountViewActivity
import com.datamangement.devast007.bahikhata.utils.BankAccountDetail
import com.datamangement.devast007.bahikhata.utils.LedgerUtils


class BankAccountViewAdapter(
    bankAccountViewActivity: BankAccountViewActivity,
    accountList: ArrayList<BankAccountDetail>
) :
    BaseExpandableListAdapter() {

    private val mbankAccountViewActivity = bankAccountViewActivity
    private val mAccountsList = accountList
    private var mInflater: LayoutInflater =
        mbankAccountViewActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getChildrenCount(p0: Int): Int {
        return 1;
    }

    override fun getGroup(groupPos: Int): Any {
        return mAccountsList.get(groupPos)
    }

    override fun onGroupCollapsed(p0: Int) {
    }

    override fun isEmpty(): Boolean {
        return false
    }


    override fun getChild(group: Int, child: Int): Any {
        return mAccountsList.get(group)
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
            view = mInflater.inflate(R.layout.row_bank_account_child_view, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }

        val accountDetail: BankAccountDetail = mAccountsList[pos]
        childHolder!!.tvID.text = accountDetail.id
        childHolder!!.tvAccountNo.text = accountDetail.accountNo
        childHolder!!.tvPayee.text = accountDetail.payee
        childHolder!!.amount.text = LedgerUtils.getRupeesFormatted(accountDetail.amount)
        childHolder!!.remark.text = accountDetail.remarks
        childHolder!!.tvIFSCCode.text = accountDetail.ifscCode
        childHolder!!.timestamp.text = accountDetail.timestamp
        childHolder!!.tvBranch.text = accountDetail.branch
        childHolder!!.edit.setTag(R.string.tag_account_id, accountDetail.id)

        childHolder!!.edit.setOnClickListener(mbankAccountViewActivity)

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
            view = mInflater.inflate(R.layout.row_bank_account_group_view, parent, false);
            groupHolder = GroupHolder(view)
            view.tag = groupHolder
        } else {
            groupHolder = view.tag as GroupHolder?
        }

        val accountDetails: BankAccountDetail = mAccountsList[pos]
        groupHolder!!.tvPayee.text = accountDetails.payee
        groupHolder!!.tvAccountNo.text = accountDetails.accountNo
        groupHolder!!.tvAccountAmount.text = LedgerUtils.getRupeesFormatted(accountDetails.amount)
        groupHolder!!.tvIFSCCode.text = accountDetails.ifscCode

        groupHolder!!.tvAccountAmount.setTag(R.string.tag_account_id, accountDetails.id)
        groupHolder!!.tvAccountAmount.setOnClickListener(mbankAccountViewActivity)
        return view
    }


    override fun getGroupCount(): Int {
        return mAccountsList.size
    }

    class GroupHolder(view: View) {
        var tvPayee: TextView = view.findViewById<TextView>(R.id.tv_bank_account_payee)
        var tvAccountAmount: TextView = view.findViewById<TextView>(R.id.tv_bank_account_amount)
        val tvAccountNo: TextView = view.findViewById<TextView>(R.id.tv_bank_account_number)
        var tvIFSCCode: TextView = view.findViewById<TextView>(R.id.tv_bank_account_ifsc_code)

    }

    class ChildHolder(view: View) {
        var tvPayee: TextView = view.findViewById<TextView>(R.id.tv_bank_account_payee)
        var tvBranch: TextView = view.findViewById<TextView>(R.id.tv_bank_account_branch)
        val tvAccountNo: TextView = view.findViewById<TextView>(R.id.tv_bank_account_number)
        var tvIFSCCode: TextView = view.findViewById<TextView>(R.id.tv_bank_account_ifsc_code)
        var tvID: TextView = view.findViewById<TextView>(R.id.tv_bank_account_id)
        var remark: TextView = view.findViewById<TextView>(R.id.tv_bank_account_remarks)
        var timestamp: TextView = view.findViewById<TextView>(R.id.tv_bank_account_timestamp)
        var amount: TextView = view.findViewById<TextView>(R.id.tv_bank_account_amount)
        var edit: TextView = view.findViewById<TextView>(R.id.tv_edit_bank_account_info)
    }

}