package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.TextView
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.ProjectsViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.ProjectDetails


class ProjectViewAdapter(projectsViewActivity: ProjectsViewActivity, projectsList: ArrayList<ProjectDetails>) :
    ExpandableListAdapter {

    val mProjectsViewActivity = projectsViewActivity
    val mProjectsList = projectsList
    var mInflater: LayoutInflater =
        mProjectsViewActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getChildrenCount(p0: Int): Int {
        return 1;
    }

    override fun getGroup(groupPos: Int): Any {
        return mProjectsList.get(groupPos)
    }

    override fun onGroupCollapsed(p0: Int) {
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun registerDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getChild(group: Int, child: Int): Any {
        return mProjectsList.get(group)
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
            view = mInflater.inflate(R.layout.row_project_child_view, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }

        val projectDetails: ProjectDetails = mProjectsList.get(pos)
        childHolder!!.tvId.setText(projectDetails.projectID)
        childHolder!!.tvName.setText(projectDetails.name)
        childHolder!!.tvAmount.setText(LedgerUtils.getRupeesFormatted(projectDetails.amount))
        childHolder!!.tvAddress.setText(projectDetails.address)
        childHolder!!.tvDivision.setText(projectDetails.division)
        childHolder!!.tvStartDate.setText(projectDetails.startDate)
        childHolder!!.tvEndDate.setText(projectDetails.endDate)
        childHolder!!.tvRemarks.setText(projectDetails.remarks)


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
            view = mInflater.inflate(R.layout.row_project_group_view, parent, false);
            groupHolder = GroupHolder(view)
            view.tag = groupHolder
        } else {
            groupHolder = view.tag as GroupHolder?
        }

        val projectDetails: ProjectDetails = mProjectsList.get(pos)
        groupHolder!!.tvId.setText(projectDetails.projectID)
        groupHolder!!.tvName.setText(projectDetails.name)
        groupHolder!!.tvAmount.setText(LedgerUtils.getRupeesFormatted(projectDetails.amount))
        groupHolder!!.tvAmount.setTag(R.string.tag_project_id, projectDetails.projectID)
        groupHolder!!.tvAmount.setOnClickListener(mProjectsViewActivity)
        return view
    }

    override fun unregisterDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getGroupCount(): Int {
        return mProjectsList.size
    }

    class GroupHolder(view: View) {
        var tvId = view.findViewById<TextView>(R.id.tv_project_id)
        val tvName = view.findViewById<TextView>(R.id.tv_project_name)
        var tvAmount: TextView = view.findViewById<TextView>(R.id.tv_amount) as TextView

    }

    class ChildHolder(view: View) {
        var tvId = view.findViewById<TextView>(R.id.tv_project_id)
        val tvName = view.findViewById<TextView>(R.id.tv_project_name)
        val tvAmount = view.findViewById<TextView>(R.id.tv_amount)
        var tvAddress = view.findViewById<TextView>(R.id.tv_project_address)
        var tvDivision = view.findViewById<TextView>(R.id.tv_project_div)
        val tvStartDate = view.findViewById<TextView>(R.id.tv_project_start_date)
        val tvEndDate = view.findViewById<TextView>(R.id.tv_project_end_date)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_project_remarks)
    }

}