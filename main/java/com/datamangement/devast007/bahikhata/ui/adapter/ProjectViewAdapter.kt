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


class ProjectViewAdapter(
    projectsViewActivity: ProjectsViewActivity,
    projectsList: ArrayList<ProjectDetails>
) :
    ExpandableListAdapter {

    private val mProjectsViewActivity = projectsViewActivity
    private val mProjectsList = projectsList
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
            view = mInflater.inflate(R.layout.row_project_child_view, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }

        val projectDetails: ProjectDetails = mProjectsList[pos]
        childHolder!!.tvId.text = projectDetails.projectID
        childHolder!!.tvName.text = projectDetails.name
        childHolder!!.tvNickname.text = projectDetails.nickname
        childHolder!!.tvAmount.text = LedgerUtils.getRupeesFormatted(projectDetails.amount)
        childHolder!!.tvAddress.text = projectDetails.address
        childHolder!!.tvDivision.text = projectDetails.division
        childHolder!!.tvStartDate.text = projectDetails.startDate
        childHolder!!.tvEndDate.text = projectDetails.endDate
        childHolder!!.tvRemarks.text = projectDetails.remarks

        childHolder!!.tvMbNo.text = projectDetails.mbNo
        childHolder!!.tvHead.text = projectDetails.head
        childHolder!!.tvMainAmount.text = LedgerUtils.getRupeesFormatted(projectDetails.mainAmount)
        childHolder!!.tvM1stYear.text =
            LedgerUtils.getRupeesFormatted(projectDetails.maintenace1stYearAmount)
        childHolder!!.tvM2ndYear.text =
            LedgerUtils.getRupeesFormatted(projectDetails.maintenace2ndYearAmount)
        childHolder!!.tvM3rdYear.text =
            LedgerUtils.getRupeesFormatted(projectDetails.maintenace3rdYearAmount)
        childHolder!!.tvM4thYear.text =
            LedgerUtils.getRupeesFormatted(projectDetails.maintenace4thYearAmount)
        childHolder!!.tvM5thYear.text =
            LedgerUtils.getRupeesFormatted(projectDetails.maintenace5thYearAmount)
        childHolder!!.tvTimestamp.text = projectDetails.timeStamp.toString()

        childHolder.tvEditProject.visibility = View.VISIBLE
        childHolder.tvEditProject.setTag(R.string.tag_project_id, projectDetails.projectID)
        childHolder.tvEditProject.setOnClickListener(mProjectsViewActivity)

        childHolder.tvUpdateProject.setTag(R.string.tag_project_id, projectDetails.projectID)
        childHolder.tvUpdateProject.setOnClickListener(mProjectsViewActivity)

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
        var tvId: TextView = view.findViewById<TextView>(R.id.tv_project_id)
        val tvName: TextView = view.findViewById<TextView>(R.id.tv_project_name)
        val tvNickname: TextView = view.findViewById<TextView>(R.id.tv_project_nickname)
        val tvAmount: TextView = view.findViewById<TextView>(R.id.tv_amount)
        var tvAddress: TextView = view.findViewById<TextView>(R.id.tv_project_address)
        var tvDivision: TextView = view.findViewById<TextView>(R.id.tv_project_div)
        val tvStartDate: TextView = view.findViewById<TextView>(R.id.tv_project_start_date)
        val tvEndDate: TextView = view.findViewById<TextView>(R.id.tv_project_end_date)
        val tvRemarks: TextView = view.findViewById<TextView>(R.id.tv_project_remarks)

        val tvMbNo: TextView = view.findViewById<TextView>(R.id.tv_project_mb_no)
        val tvHead: TextView = view.findViewById<TextView>(R.id.tv_project_head)
        val tvMainAmount: TextView = view.findViewById<TextView>(R.id.tv_project_main_amount)
        val tvM1stYear: TextView =
            view.findViewById<TextView>(R.id.tv_project_maintenance_amount_1st_year)
        val tvM2ndYear: TextView =
            view.findViewById<TextView>(R.id.tv_project_maintenance_amount_2nd_year)
        val tvM3rdYear: TextView =
            view.findViewById<TextView>(R.id.tv_project_maintenance_amount_3rd_year)
        val tvM4thYear: TextView =
            view.findViewById<TextView>(R.id.tv_project_maintenance_amount_4th_year)
        val tvM5thYear: TextView =
            view.findViewById<TextView>(R.id.tv_project_maintenance_amount_5th_year)

        val tvEditProject: TextView = view.findViewById<TextView>(R.id.tv_edit_project)
        val tvUpdateProject: TextView = view.findViewById<TextView>(R.id.tv_update_project_info)

        val tvTimestamp: TextView = view.findViewById<TextView>(R.id.tv_time_stamp)
    }

}