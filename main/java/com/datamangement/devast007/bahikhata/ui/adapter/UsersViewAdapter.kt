package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.TextView
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.UsersViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.UserDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot


class UsersViewAdapter(usersViewActivity: UsersViewActivity, usersList: ArrayList<UserDetails>) :
    ExpandableListAdapter {



    val TAG = "UsersViewAdapter"
    val mUsersViewActivity = usersViewActivity
    val mUsersList = usersList
    var mInflater: LayoutInflater =
        mUsersViewActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val green = Color.parseColor("#B1D5A6")
    override fun getChildrenCount(p0: Int): Int {
        return 1;
    }

    override fun getGroup(groupPos: Int): Any {
        return mUsersList.get(groupPos)
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

        childHolder!!.tvEdit.setTag(R.string.tag_user_id, userDetails.userID)
        childHolder!!.tvEdit.setOnClickListener(mUsersViewActivity)
        childHolder!!.tvEdit.visibility = View.VISIBLE

        var personalID = LedgerDefine.PREFIX_PERSONAL + userDetails.userID

        if (userDetails.userAccounts!!.size > 1) {
            childHolder.tvMasterAmount.setTag(
                R.string.tag_user_id,
                LedgerDefine.PREFIX_MASTER + userDetails.userID
            )
            childHolder.tvMasterAmount.setOnClickListener(mUsersViewActivity)

            childHolder.tvMasterAmount.text =
                String.format(
                    mUsersViewActivity.getString(R.string.left_balance_master_formattor),
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
            childHolder.tvPersonalMaterialCost.visibility = View.GONE
            childHolder.tvPersonalGst.visibility = View.GONE
            childHolder.tvMasterAmount.visibility = View.GONE
            childHolder.tvOnlyPersonalText.setText(R.string.this_is_company_account)
            childHolder.tvEdit.isEnabled = false
        } else {
            childHolder.tvOnlyMasterText.visibility = View.VISIBLE
            childHolder.tvPersonalGst.visibility = View.VISIBLE
            childHolder.tvMasterAmount.visibility = View.VISIBLE
            childHolder.tvPersonalMaterialCost.visibility = View.VISIBLE
            childHolder.tvOnlyPersonalText.setText(R.string.personal)
            childHolder.tvEdit.isEnabled = true
        }

        childHolder.tvPersonalPayment.setTag(R.string.tag_user_id, personalID)
        childHolder.tvPersonalPayment.setOnClickListener(mUsersViewActivity)
        //todo gst and material


        childHolder.tvPersonalGst.setTag(R.string.tag_user_id, personalID)
        childHolder.tvPersonalMaterialCost.setOnClickListener(mUsersViewActivity)
        childHolder.tvPersonalGst.setOnClickListener(mUsersViewActivity)
        childHolder.tvPersonalMaterialCost.setTag(
            R.string.tag_user_id,
            personalID
        )
        childHolder.tvPersonalMaterialCost.text =
            String.format(
                mUsersViewActivity.getString(R.string.material_cost_formattor),
                LedgerUtils.getRupeesFormatted(userDetails.p_MaterialCost)
            )

        childHolder.tvPersonalPayment.text = String.format(
            mUsersViewActivity.getString(R.string.payment_formattor),
            LedgerUtils.getRupeesFormatted(userDetails.p_Payment)
        )


        childHolder.tvPersonalGst.text = String.format(
            mUsersViewActivity.getString(R.string.gst_bill_formattor),
            LedgerUtils.getRupeesFormatted(userDetails.p_gstBill)
        )

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
        p1: Boolean,
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

        if (userDetails.designation == LedgerDefine.DESIGNATION_ADMIN) {
            groupHolder.tvMasterAmount.visibility = View.GONE
            groupHolder.tvPersonalGstBalance.visibility = View.GONE
            groupHolder.tvPersonalBalance.visibility = View.GONE
            groupHolder.tvOnlyMasterText.visibility = View.GONE
            groupHolder.tvOnlyPersonalText.setText(R.string.this_is_company_account)
            view!!.setBackgroundColor(Color.RED)
        } else {

            groupHolder.tvMasterAmount.visibility = View.VISIBLE
            groupHolder.tvPersonalGstBalance.visibility = View.VISIBLE
            groupHolder.tvPersonalBalance.visibility = View.VISIBLE
            groupHolder.tvOnlyMasterText.visibility = View.VISIBLE
            groupHolder.tvOnlyPersonalText.setText(R.string.personal)
            view!!.setBackgroundColor(green)
            if (userDetails.userAccounts!!.size <= 1) {
                groupHolder.tvMasterAmount.setText(R.string.no_master_account)
            } else {
                if (userDetails.m_Amount != null && userDetails.m_Amount.toInt() != 0) {
                    groupHolder.tvMasterAmount.text = String.format(
                        mUsersViewActivity.getString(R.string.left_balance_master_formattor),
                        LedgerUtils.getRupeesFormatted(userDetails.m_Amount)
                    )

                } else {
                    groupHolder.tvMasterAmount.setText(R.string.no_record_found)
                }
            }

            if (userDetails.p_MaterialCost != null && userDetails.p_Payment != null && userDetails.p_Payment.toInt() != 0 && userDetails.p_MaterialCost.toInt() != 0) {

                groupHolder.tvPersonalBalance.text = String.format(
                    mUsersViewActivity.getString(R.string.left_balance_material_cost_minus_payment_formattor),
                    LedgerUtils.getRupeesFormatted((userDetails.p_MaterialCost - userDetails.p_Payment))
                )
            } else {
                groupHolder.tvPersonalBalance.setText(R.string.no_data)
            }

            if (userDetails.p_gstBill != null && userDetails.p_Payment != null && userDetails.p_Payment.toInt() != 0 && userDetails.p_gstBill.toInt() != 0) {

                groupHolder.tvPersonalGstBalance.text = String.format(
                    mUsersViewActivity.getString(R.string.left_gst_payment_minus_gst_bill_formattor),
                    LedgerUtils.getRupeesFormatted((userDetails.p_Payment - userDetails.p_gstBill))
                )
            } else {
                groupHolder.tvPersonalGstBalance.setText(R.string.no_data)
            }
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
        var tvPersonalBalance = view.findViewById<TextView>(R.id.tv_personal_balance)
        var tvPersonalGstBalance = view.findViewById<TextView>(R.id.tv_personal_gst)
        var tvMasterAmount = view.findViewById<TextView>(R.id.tv_master_amount)
        var tvOnlyMasterText = view.findViewById<TextView>(R.id.tv_only_text_master)
        var tvOnlyPersonalText = view.findViewById<TextView>(R.id.tv_only_text_personal)
    }

    class ChildHolder(view: View) {
        var tvEdit = view.findViewById<TextView>(R.id.tv_edit_user_info)
        var tvUpdateUserInfo = view.findViewById<TextView>(R.id.tv_update_user_info)
        var tvId = view.findViewById<TextView>(R.id.tv_user_id)
        val tvName = view.findViewById<TextView>(R.id.tv_user_name)
        var tvAddress = view.findViewById<TextView>(R.id.tv_user_address)
        var tvUserCreatedDate = view.findViewById<TextView>(R.id.tv_user_created_date)
        val tvPhone = view.findViewById<TextView>(R.id.tv_user_phone)
        val tvEmail = view.findViewById<TextView>(R.id.tv_user_email)
        val tvAccessibleProjects = view.findViewById<TextView>(R.id.tv_user_project_access)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_user_remarks)
        val designation = view.findViewById<TextView>(R.id.tv_user_designation)

        val tvPersonalMaterialCost = view.findViewById<TextView>(R.id.tv_personal_material_cost)
        val tvPersonalPayment = view.findViewById<TextView>(R.id.tv_personal_payment)
        val tvPersonalGst = view.findViewById<TextView>(R.id.tv_personal_gst)
        val tvMasterAmount = view.findViewById<TextView>(R.id.tv_master_amount)
        var tvOnlyMasterText = view.findViewById<TextView>(R.id.tv_only_text_master)
        var tvOnlyPersonalText = view.findViewById<TextView>(R.id.tv_only_text_personal)


    }

}