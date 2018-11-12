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
        childHolder!!.tvId.setText(userDetails.userID)
        childHolder!!.tvName.setText(userDetails.name)
        childHolder!!.tvAmount.setText(LedgerUtils.getRupeesFormatted(userDetails.amount))
        childHolder!!.tvAddress.setText(userDetails.address)
        childHolder!!.tvUserCreatedDate.setText(userDetails.userCreatedDate)
        childHolder!!.tvPhone.setText(userDetails.phone)
        childHolder!!.tvEmail.setText(userDetails.email)
        childHolder!!.tvAccessibleProjects.setText(userDetails.accesibleProjectsStr)
        childHolder!!.designation.setText("" + userDetails.designation)
        childHolder!!.tvRemarks.setText(userDetails.remarks)


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
        groupHolder!!.tvId.setText(userDetails.userID)
        groupHolder!!.tvName.setText(userDetails.name)
        groupHolder!!.tvAmount.setText(LedgerUtils.getRupeesFormatted(userDetails.amount))
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
        var tvAmount: TextView = view.findViewById<TextView>(R.id.tv_user_benefits) as TextView

    }

    class ChildHolder(view: View) {
        var tvId = view.findViewById<TextView>(R.id.tv_user_id)
        val tvName = view.findViewById<TextView>(R.id.tv_user_name)
        val tvAmount = view.findViewById<TextView>(R.id.tv_user_benefits)
        var tvAddress = view.findViewById<TextView>(R.id.tv_user_address)
        var tvUserCreatedDate = view.findViewById<TextView>(R.id.tv_user_created_date)
        val tvPhone = view.findViewById<TextView>(R.id.tv_user_phone)
        val tvEmail = view.findViewById<TextView>(R.id.tv_user_email)
        val tvAccessibleProjects = view.findViewById<TextView>(R.id.tv_user_project_access)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_user_remarks)
        val designation = view.findViewById<TextView>(R.id.tv_user_designation)
    }

}