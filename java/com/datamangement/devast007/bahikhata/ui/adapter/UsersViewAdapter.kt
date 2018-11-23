package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.TextView
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.UsersViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.UserDetails


class UsersViewAdapter(usersViewActivity: UsersViewActivity, usersList: ArrayList<UserDetails>) :
    ExpandableListAdapter {

    val mUsersViewActivity = usersViewActivity
    val mUsersList = usersList
    var mInflater: LayoutInflater =
        mUsersViewActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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

    override fun getChildView(pos: Int, p1: Int, p2: Boolean, convertView: View?, parent: ViewGroup?): View? {
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
        childHolder!!.tvAmount.text = LedgerUtils.getRupeesFormatted(userDetails.amount)
        childHolder!!.tvAddress.text = userDetails.address
        childHolder!!.tvUserCreatedDate.text = userDetails.userCreatedDate
        childHolder!!.tvPhone.text = userDetails.phone
        childHolder!!.tvEmail.text = userDetails.email
        childHolder!!.tvAccessibleProjects.text = userDetails.accesibleProjectsStr
        childHolder!!.designation.text = "" + userDetails.designation
        childHolder!!.tvRemarks.text = userDetails.remarks

        childHolder!!.tvEdit.setTag(R.string.tag_user_id, userDetails.userID)
        childHolder!!.tvEdit.setOnClickListener(mUsersViewActivity)
        childHolder!!.tvEdit.visibility = View.VISIBLE

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
            view = mInflater.inflate(R.layout.row_users_group_view, parent, false);
            groupHolder = GroupHolder(view)
            view.tag = groupHolder
        } else {
            groupHolder = view.tag as GroupHolder?
        }

        val userDetails: UserDetails = mUsersList.get(pos)
        groupHolder!!.tvId.text = userDetails.userID
        groupHolder!!.tvName.text = userDetails.name
        groupHolder!!.tvAmount.text = LedgerUtils.getRupeesFormatted(userDetails.amount)
        groupHolder!!.tvAmount.setTag(R.string.tag_user_id, userDetails.userID)
        groupHolder!!.tvAmount.setTag(R.string.tag_user_designation, userDetails.designation)
        groupHolder!!.tvAmount.setOnClickListener(mUsersViewActivity)
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
        var tvAmount = view.findViewById<TextView>(R.id.tv_amount)

    }

    class ChildHolder(view: View) {
        var tvEdit = view.findViewById<TextView>(R.id.tv_edit_user_info)
        var tvId = view.findViewById<TextView>(R.id.tv_user_id)
        val tvName = view.findViewById<TextView>(R.id.tv_user_name)
        val tvAmount = view.findViewById<TextView>(R.id.tv_amount_1)
        var tvAddress = view.findViewById<TextView>(R.id.tv_user_address)
        var tvUserCreatedDate = view.findViewById<TextView>(R.id.tv_user_created_date)
        val tvPhone = view.findViewById<TextView>(R.id.tv_user_phone)
        val tvEmail = view.findViewById<TextView>(R.id.tv_user_email)
        val tvAccessibleProjects = view.findViewById<TextView>(R.id.tv_user_project_access)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_user_remarks)
        val designation = view.findViewById<TextView>(R.id.tv_user_designation)
    }

}