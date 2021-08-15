package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.UsersViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.UserDetails


class UsersViewAdapter(usersViewActivity: UsersViewActivity, usersList: ArrayList<UserDetails>) :
    ExpandableListAdapter {


    val TAG = "UsersViewAdapter"
    val mUsersViewActivity = usersViewActivity
    val mUsersList = usersList
    var mInflater: LayoutInflater =
        mUsersViewActivity.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val green = Color.parseColor("#B1D5A6")
    val white = Color.parseColor("#FFFFFF")
    override fun getChildrenCount(p0: Int): Int {
        return 1;
    }

    override fun getGroup(groupPos: Int): Any {
        return mUsersList[groupPos]
    }

    override fun onGroupCollapsed(p0: Int) {
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun registerDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getChild(group: Int, child: Int): Any {
        return mUsersList.get(group)
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

    override fun getChildView(
        pos: Int,
        p1: Int,
        p2: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        var view: View? = convertView
        var childHolder: ChildHolder? = null
        if (view == null) {
            view = mInflater.inflate(R.layout.row_users_child_view, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }

        val userDetails: UserDetails = mUsersList.get(pos)
        childHolder!!.tvId.text = userDetails.userID
        childHolder!!.tvName.text = userDetails.name
        childHolder!!.tvAddress.text = userDetails.address
        childHolder!!.tvUserCreatedDate.text = userDetails.userCreatedDate
        childHolder!!.tvPhone.text = userDetails.phone
        childHolder!!.tvEmail.text = userDetails.email
        childHolder!!.tvAccessibleProjects.text = userDetails.accesibleProjectsStr
        childHolder!!.designation.text = "" + userDetails.designation
        childHolder!!.tvRemarks.text = userDetails.remarks

        childHolder!!.tvUpdateUserInfo.setTag(R.string.tag_user_id, userDetails.userID)
        childHolder!!.tvUpdateUserInfo.setOnClickListener(mUsersViewActivity)

        childHolder!!.switchDisableUser.setTag(R.string.tag_user_id, userDetails.userID)
        childHolder!!.switchDisableUser.setOnClickListener(mUsersViewActivity)


        childHolder!!.tvEdit.setTag(R.string.tag_user_id, userDetails.userID)
        childHolder!!.tvEdit.setOnClickListener(mUsersViewActivity)
        childHolder!!.tvEdit.visibility = View.VISIBLE

        var personalID = LedgerDefine.PREFIX_PERSONAL + userDetails.userID


        if (userDetails.userAccounts!!.size > 1) {
            childHolder.tvMasterAmount.setTag(
                R.string.tag_user_id,
                LedgerDefine.PREFIX_MASTER + userDetails.userID
            )

            childHolder.tvMasterAmount.text =
                String.format(
                    mUsersViewActivity.mContext.getString(R.string.left_balance_master_formattor),
                    LedgerUtils.getRupeesFormatted(userDetails.m_Amount)
                )
            childHolder.tvMasterAmount.isClickable = true
            childHolder.tvMasterAmount.isEnabled = true
        } else {
            childHolder.tvMasterAmount.isClickable = false
            childHolder.tvMasterAmount.isEnabled = false
            childHolder.tvMasterAmount.setText(R.string.no_master_account)
        }



        if (userDetails.userID == LedgerDefine.ADMIN_ID) {
            personalID = LedgerDefine.PREFIX_ADMIN + userDetails.userID
            childHolder.tvOnlyMasterText.visibility = View.GONE
            childHolder.tvPersonalGst.visibility = View.GONE
            childHolder.tvMasterAmount.visibility = View.GONE
            childHolder.tvOnlyPersonalText.setText(R.string.this_is_company_account)
            childHolder.tvEdit.isEnabled = false

        } else {
            childHolder.tvOnlyMasterText.visibility = View.VISIBLE
            childHolder.tvPersonalGst.visibility = View.VISIBLE
            childHolder.tvMasterAmount.visibility = View.VISIBLE
            childHolder.tvOnlyPersonalText.setText(R.string.personal)
            childHolder.tvEdit.isEnabled = true
        }

        childHolder.tvPersonalPayment.setTag(R.string.tag_user_id, personalID)
        childHolder.tvPersonalPayment.setOnClickListener(mUsersViewActivity)
        //todo gst and material


        if (userDetails.p_MaterialCost != null && userDetails.p_Payment != null && userDetails.p_Payment.toInt() != 0 && userDetails.p_MaterialCost.toInt() != 0) {

            childHolder.tvPersonalGst.text = String.format(
                mUsersViewActivity.mContext.getString(R.string.left_balance_material_cost_minus_payment_formattor),
                LedgerUtils.getRupeesFormatted((userDetails.p_MaterialCost - userDetails.p_Payment))
            )
        } else {
            childHolder.tvPersonalGst.setText(R.string.no_data)
        }

        if (userDetails.p_gstBill != null && userDetails.p_Payment != null && userDetails.p_Payment.toInt() != 0 && userDetails.p_gstBill.toInt() != 0) {

            childHolder.tvPersonalPayment.text = String.format(
                mUsersViewActivity.mContext.getString(R.string.left_gst_payment_minus_gst_bill_formattor),
                LedgerUtils.getRupeesFormatted((userDetails.p_Payment - userDetails.p_gstBill))
            )
        } else {
            childHolder.tvPersonalPayment.setText(R.string.no_data)
        }


        if (userDetails.isUserDisable) {
            childHolder.switchDisableUser.isChecked = true
            view!!.setBackgroundColor(Color.GRAY)
        } else {
            childHolder.switchDisableUser.isChecked = false
            view!!.setBackgroundColor(Color.WHITE)
        }

        // admin check of login
        if(LedgerUtils.signInProfile!!.isAdmin){
            childHolder.switchEditPermission.visibility = View.VISIBLE
            childHolder.switchEditPermission.isChecked = userDetails.isHasEditPermission
            childHolder!!.switchEditPermission.setTag(R.string.tag_user_id, userDetails.userID)
            childHolder!!.switchEditPermission.setOnClickListener(mUsersViewActivity)
        }else{
            childHolder.switchEditPermission.visibility = View.GONE
        }
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


    override fun getGroupView(
        pos: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        var view: View? = convertView
        var groupHolder: GroupHolder? = null
        if (view == null) {
            view = mInflater.inflate(R.layout.row_users_group_view, parent, false);
            groupHolder = GroupHolder(view)
            view.tag = groupHolder

        } else {
            groupHolder = view.tag as GroupHolder?
        }
        val userDetails: UserDetails = mUsersList[pos]
        groupHolder!!.tvId.text = userDetails.userID
        groupHolder!!.tvName.text = userDetails.name

        var personalID = LedgerDefine.PREFIX_PERSONAL + userDetails.userID

        if (userDetails.designation == LedgerDefine.DESIGNATION_ADMIN) {

            personalID = LedgerDefine.PREFIX_ADMIN + userDetails.userID
            groupHolder.llMasterAmount.visibility = View.GONE
            groupHolder.tvGst.visibility = View.GONE
            groupHolder.tvOnlyMasterText.visibility = View.GONE
            groupHolder.tvMaterial.visibility = View.GONE
            groupHolder.tvOnlyPersonalText.setText(R.string.this_is_company_account)
            view!!.setBackgroundColor(Color.RED)
        } else {

            groupHolder.tvMaterial.visibility = View.VISIBLE
            groupHolder.llMasterAmount.visibility = View.VISIBLE
            groupHolder.tvGst.visibility = View.VISIBLE
            groupHolder.tvOnlyMasterText.visibility = View.VISIBLE
            groupHolder.tvOnlyPersonalText.setText(R.string.personal)
            view!!.setBackgroundColor(white)
            if (userDetails.userAccounts!!.size <= 1) {
                groupHolder.llMasterAmount.isClickable = false
                groupHolder.llMasterAmount.isEnabled = false
                groupHolder.llMasterAmount.visibility = View.GONE //
                groupHolder.tvOnlyMasterText.setText(R.string.no_master_account)
            } else {
                groupHolder.tvOnlyMasterText.setText(R.string.master)
                groupHolder.llMasterAmount.visibility = View.VISIBLE
                /*groupHolder.llMasterAmount.text = String.format(
                    mUsersViewActivity.mContext.getString(R.string.left_balance_master_formattor),
                    LedgerUtils.getRupeesFormatted(userDetails.m_Amount)
                )*/

                groupHolder.tvTransactionsSentMaster.setTag(
                    R.string.tag_user_id,
                    LedgerDefine.PREFIX_MASTER + userDetails.userID
                )
                groupHolder.tvTransactionsReceivedMaster.setTag(
                    R.string.tag_user_id,
                    LedgerDefine.PREFIX_MASTER + userDetails.userID
                )
                groupHolder.tvTransactionsSentMaster.setOnClickListener(mUsersViewActivity)
                groupHolder.tvTransactionsReceivedMaster.setOnClickListener(mUsersViewActivity)
            }

        }

        groupHolder.tvMaterial.setTag(R.string.tag_user_id, personalID)
        /*groupHolder.tvMaterial.text = String.format(
            mUsersViewActivity.mContext.getString(R.string.material_cost_formattor),
            LedgerUtils.getRupeesFormatted(userDetails.p_MaterialCost)
        )*/

        groupHolder.tvTransactionsSent.setTag(R.string.tag_user_id, personalID)
        groupHolder.tvTransactionsReceived.setTag(R.string.tag_user_id, personalID)
        /*groupHolder.tvTransactionsSent.text = String.format(
            mUsersViewActivity.mContext.getString(R.string.payment_formattor),
            LedgerUtils.getRupeesFormatted(userDetails.p_Payment)
        )*/

        groupHolder.tvGst.setTag(R.string.tag_user_id, personalID)
        /* groupHolder.tvGst.text = String.format(
             mUsersViewActivity.mContext.getString(R.string.gst_bill_formattor),
             LedgerUtils.getRupeesFormatted(userDetails.p_gstBill)
         )*/

        groupHolder.tvMaterial.setOnClickListener(mUsersViewActivity)

        groupHolder.tvTransactionsSent.setOnClickListener(mUsersViewActivity)
        groupHolder.tvTransactionsReceived.setOnClickListener(mUsersViewActivity)
        groupHolder.tvGst.setOnClickListener(mUsersViewActivity)
        groupHolder.llMasterAmount.setOnClickListener(mUsersViewActivity)
        if (userDetails.isUserDisable) {
            view!!.setBackgroundColor(Color.GRAY)
        } else if (isExpanded) {
            view!!.setBackgroundResource(R.drawable.selected_group)
        } else {
            view!!.setBackgroundColor(Color.parseColor("#00000000"))
        }

        return view
    }

    override fun unregisterDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getGroupCount(): Int {
        return mUsersList.size
    }

    class GroupHolder(view: View) {
        var tvId = view.findViewById<TextView>(R.id.tv_user_id)
        val tvName = view.findViewById<TextView>(R.id.tv_user_name)
        var tvMaterial = view.findViewById<TextView>(R.id.tv_material)
        var tvTransactionsSent = view.findViewById<TextView>(R.id.btn_transactions_sent)
        var tvTransactionsReceived = view.findViewById<TextView>(R.id.btn_transactions_received)
        var tvGst = view.findViewById<TextView>(R.id.tv_gst)
        var llMasterAmount = view.findViewById<LinearLayout>(R.id.ll_master_amount)
        var tvTransactionsSentMaster =
            view.findViewById<TextView>(R.id.btn_transactions_sent_master)
        var tvTransactionsReceivedMaster =
            view.findViewById<TextView>(R.id.btn_transactions_received_master)
        var tvOnlyMasterText = view.findViewById<TextView>(R.id.tv_only_text_master)
        var tvOnlyPersonalText = view.findViewById<TextView>(R.id.tv_only_text_personal)
    }

    class ChildHolder(view: View) {
        var tvEdit = view.findViewById<TextView>(R.id.tv_edit_user_info)
        var tvUpdateUserInfo = view.findViewById<TextView>(R.id.tv_update_user_info)
        var switchDisableUser = view.findViewById<Switch>(R.id.switch_disable_user)
        var tvId = view.findViewById<TextView>(R.id.tv_user_id)
        val tvName = view.findViewById<TextView>(R.id.tv_user_name)
        var tvAddress = view.findViewById<TextView>(R.id.tv_user_address)
        var tvUserCreatedDate = view.findViewById<TextView>(R.id.tv_user_created_date)
        val tvPhone = view.findViewById<TextView>(R.id.tv_user_phone)
        val tvEmail = view.findViewById<TextView>(R.id.tv_user_email)
        val tvAccessibleProjects = view.findViewById<TextView>(R.id.tv_user_project_access)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_user_remarks)
        val designation = view.findViewById<TextView>(R.id.tv_user_designation)

        val tvPersonalPayment = view.findViewById<TextView>(R.id.tv_personal_payment)
        val tvPersonalGst = view.findViewById<TextView>(R.id.tv_personal_gst)
        val tvMasterAmount = view.findViewById<TextView>(R.id.tv_master_amount)
        var tvOnlyMasterText = view.findViewById<TextView>(R.id.tv_only_text_master)
        var tvOnlyPersonalText = view.findViewById<TextView>(R.id.tv_only_text_personal)
        var switchEditPermission = view.findViewById<Switch>(R.id.switch_disable_edit_permission)


    }

}