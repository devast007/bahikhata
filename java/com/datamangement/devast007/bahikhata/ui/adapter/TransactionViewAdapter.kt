package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.TransactionViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.TransactionDetails

class TransactionViewAdapter(
    transactionViewActivity: TransactionViewActivity,
    transactionsList: ArrayList<TransactionDetails>,
    longClickedForAddition: Boolean
) : BaseExpandableListAdapter() {
    private val SUBCATEGORY: Int = 1
    private val mTransactionViewActivity: TransactionViewActivity = transactionViewActivity
    val mTransactionsList = transactionsList
    private val mLongClickedForAddition = longClickedForAddition
    var mInflater: LayoutInflater =
        mTransactionViewActivity.mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

    override fun getChildView(
        groupPos: Int,
        childPos: Int,
        p2: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        var view: View? = convertView
        var childHolder: ChildHolder? = null
        if (view == null) {
            view = mInflater.inflate(R.layout.row_transaction_child_view, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }


        val transactionDetails: TransactionDetails = mTransactionsList.get(groupPos)
        childHolder!!.tvSenderID.text = getSpannableString(
            transactionDetails.senderId,
            mTransactionViewActivity.mUsersMap[getUserId(transactionDetails.senderId)], 0
        )
        childHolder!!.tvRecevierID.text = getSpannableString(
            transactionDetails.receiverId,
            mTransactionViewActivity.mUsersMap[getUserId(transactionDetails.receiverId)], 0
        )
        childHolder!!.tvTransactionDate.text =
            LedgerUtils.getConvertDate(transactionDetails.transactionDate)
        childHolder!!.tvAmount.text = LedgerUtils.getRupeesFormatted(transactionDetails.amount)
        childHolder!!.tvRoundOff.text = "" + transactionDetails.roundOff
        childHolder!!.tvProjectID.text =
            mTransactionViewActivity.mProjectsMap[transactionDetails.projectId]
        childHolder!!.tvSubCategory.text = transactionDetails.subCategory


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

        if (mIsAdmin) {
            childHolder.editTransaction.visibility = View.VISIBLE
            /*childHolder.editTransaction.setOnClickListener(mTransactionViewActivity)
            childHolder.editTransaction.setTag(
                R.string.tag_transaction_id,
                transactionDetails.transactionID
            )*/
        } else {
            //toDO
            childHolder.editTransaction.visibility = View.VISIBLE
        }
        childHolder.editTransaction.setOnClickListener(mTransactionViewActivity)
        childHolder.editTransaction.setTag(
            R.string.tag_transaction_id,
            transactionDetails.transactionID
        )

        childHolder.editTransaction.setTag(
            R.string.tag_group_pos, groupPos
        )

        childHolder!!.tvVerified.setText(strID)
        childHolder!!.tvRemarks.text = transactionDetails.remarks
        childHolder!!.tvLoggedInId.text = getSpannableString(
            transactionDetails.loggedInID,
            mTransactionViewActivity.mUsersMap.get(transactionDetails.loggedInID), 0
        )
        childHolder!!.tvTimestamp.text = transactionDetails.timeStamp.toString()
        childHolder!!.tvTransactionID.text = transactionDetails.transactionID

        childHolder!!.tvDebitAccount.text = getSpannableString(
            transactionDetails.debitedTo,
            mTransactionViewActivity.mBankAccountsMap.get(transactionDetails.debitedTo), 0
        )
        childHolder!!.tvCreditAccount.text = getSpannableString(
            transactionDetails.creditedTo,
            mTransactionViewActivity.mBankAccountsMap.get(transactionDetails.creditedTo), 0
        )
        childHolder!!.tvPaymentMode.text = transactionDetails.paymentMode

        if (transactionDetails.relatedTransactionsIds == null || transactionDetails.relatedTransactionsIds!!.size <= 1) {
            childHolder!!.btnRelatedTransactions.isVisible = false
        } else {
            childHolder!!.btnRelatedTransactions.isVisible = true
            childHolder!!.btnRelatedTransactions.setTag(
                R.string.tag_transaction_id,
                transactionDetails.transactionID
            )
            childHolder!!.btnRelatedTransactions.setOnClickListener(mTransactionViewActivity)
        }

        childHolder!!.switchTransactionTracking.isChecked = transactionDetails.isTrackingOn

        childHolder.btnVerify.setTag(
            R.string.tag_transaction_id,
            transactionDetails.transactionID
        )
        childHolder.btnVerify.setTag(
            R.string.tag_group_pos, groupPos
        )
        childHolder.btnDel.setTag(R.string.tag_transaction_id, transactionDetails.transactionID)
        childHolder.tvSenderID.setTag(R.string.tag_user_id, getUserId(transactionDetails.senderId))
        childHolder.tvRecevierID.setTag(
            R.string.tag_user_id,
            getUserId(transactionDetails.receiverId)
        )
        childHolder.tvLoggedInId.setTag(R.string.tag_user_id, transactionDetails.loggedInID)
        childHolder.tvProjectID.setTag(R.string.tag_project_id, transactionDetails.projectId)
        childHolder.tvDebitAccount.setTag(R.string.tag_account_id, transactionDetails.debitedTo)
        childHolder.tvCreditAccount.setTag(R.string.tag_account_id, transactionDetails.creditedTo)
        childHolder.switchTransactionTracking.setTag(
            R.string.tag_transaction_id,
            transactionDetails.transactionID
        )

        childHolder.btnVerify.setOnClickListener(mTransactionViewActivity)
        childHolder.btnDel.setOnClickListener(mTransactionViewActivity)
        childHolder.tvSenderID.setOnClickListener(mTransactionViewActivity)
        childHolder.tvRecevierID.setOnClickListener(mTransactionViewActivity)
        childHolder.tvLoggedInId.setOnClickListener(mTransactionViewActivity)
        childHolder.tvProjectID.setOnClickListener(mTransactionViewActivity)
        childHolder.tvDebitAccount.setOnClickListener(mTransactionViewActivity)
        childHolder.tvCreditAccount.setOnClickListener(mTransactionViewActivity)
        childHolder.switchTransactionTracking.setOnClickListener(mTransactionViewActivity)

        if (transactionDetails.verified) {
            view!!.setBackgroundColor(Color.parseColor("#FFF3F3F3"))
        } else {
            view!!.setBackgroundColor(Color.parseColor("#FBC7D9"))
        }

        return view
    }

    private fun getUserId(userAccount: String): String? {
        var userId: String = ""
        userId = userAccount.substring(2)
        return userId

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
            view = mInflater.inflate(R.layout.row_transaction_group_view, parent, false);
            groupHolder = GroupHolder(view)
            view.tag = groupHolder
        } else {
            groupHolder = view.tag as GroupHolder?
        }


        val transactionDetails: TransactionDetails = mTransactionsList[pos]

        groupHolder!!.tvRecevierID.text = getSpannableString(
            transactionDetails.receiverId.substring(0, 1),
            mTransactionViewActivity.mUsersMap.get(getUserId(transactionDetails.receiverId)), 0
        )
        groupHolder!!.tvTransactionDate.text =
            LedgerUtils.getConvertDate(transactionDetails.transactionDate)

        val sender = getSpannableString(
            transactionDetails.senderId.substring(0, 1),
            mTransactionViewActivity.mUsersMap[getUserId(transactionDetails.senderId)], 0
        )
        val receiver = getSpannableString(
            transactionDetails.receiverId.substring(0, 1),
            mTransactionViewActivity.mUsersMap[getUserId(transactionDetails.receiverId)], 0
        )

        var project: Spannable? = SpannableString("")
        if (transactionDetails.subCategory.isNotEmpty()) {
            project = getSpannableString(
                transactionDetails.subCategory,
                mTransactionViewActivity.mProjectsMap[transactionDetails.projectId], SUBCATEGORY
            )
        } else {
            if (transactionDetails.projectId.isNotEmpty())
                project =
                    SpannableString(mTransactionViewActivity.mProjectsMap[transactionDetails.projectId])
        }


        when (mTransactionViewActivity.mSwitchProjectOrSender) {
            1 -> {

                groupHolder!!.tvSenderID.text = sender
                groupHolder!!.tvSenderID.setTextColor(Color.RED)
                //groupHolder!!.tvSenderID.setBackgroundColor(Color.RED)

                groupHolder!!.tvRecevierID.text = receiver
                groupHolder!!.tvRecevierID.setTextColor(
                    mTransactionViewActivity.mContext!!.resources.getColor(
                        R.color.dark_green
                    )
                )

            }
            0 -> {
                groupHolder!!.tvSenderID.text = project
                groupHolder!!.tvSenderID.setTextColor(Color.BLACK)
                //groupHolder!!.tvSenderID.setBackgroundColor(Color.RED)

                groupHolder!!.tvRecevierID.text = receiver
                groupHolder!!.tvRecevierID.setTextColor(
                    mTransactionViewActivity.mContext!!.resources.getColor(
                        R.color.dark_green
                    )
                )

            }
            else -> {
                groupHolder!!.tvSenderID.text = sender
                groupHolder!!.tvSenderID.setTextColor(Color.RED)

                groupHolder!!.tvRecevierID.text = project


            }
        }


        groupHolder.tvRemarks.text = transactionDetails.remarks

        if (mTransactionViewActivity.mShowCheckBox) {
            groupHolder!!.cbAddition.visibility = View.VISIBLE
            groupHolder!!.rlTxnAmount.visibility = View.GONE
            groupHolder!!.cbAddition.setTag(R.string.tag_amount, transactionDetails.amount)
            groupHolder!!.cbAddition.setTag(
                R.string.tag_transaction_id,
                transactionDetails.transactionID
            )
            groupHolder!!.cbAddition.text = "[" + (pos + 1) + "] " +
                    LedgerUtils.getRupeesFormatted(transactionDetails.amount)
            groupHolder!!.cbAddition.isChecked = transactionDetails.isChecked
        } else {
            groupHolder!!.cbAddition.visibility = View.GONE
            groupHolder!!.rlTxnAmount.visibility = View.VISIBLE
            groupHolder!!.tvAmount.text =
                LedgerUtils.getRupeesFormatted(transactionDetails.amount.toString())
            groupHolder!!.tvAmount.setOnLongClickListener(mTransactionViewActivity)

            groupHolder!!.tvAmount.setTag(
                R.string.tag_transaction_id,
                transactionDetails.transactionID
            )
            if (!TextUtils.isEmpty(transactionDetails.imageLink)) {
                groupHolder!!.tvAmount.setTextColor(
                    mTransactionViewActivity.resources
                        .getColorStateList(R.color.selector_text_color_state, null)
                )
            } else {
                groupHolder!!.tvAmount.setTextColor(
                    mTransactionViewActivity.resources
                        .getColorStateList(R.color.selector_textview_pressed, null)
                )
            }
        }


        if (transactionDetails.isTrackingOn) {
            groupHolder.ivTracking.visibility = View.VISIBLE
        } else {
            groupHolder.ivTracking.visibility = View.GONE
        }

        if (TextUtils.isEmpty(transactionDetails.creditedTo)) {
            groupHolder.ivHasBank.visibility = View.GONE
        } else {
            groupHolder.ivHasBank.visibility = View.VISIBLE
        }

        if (transactionDetails.relatedTransactionsIds == null || transactionDetails.relatedTransactionsIds!!.size <= 1) {
            groupHolder!!.ivHasRelation.visibility = View.GONE
        } else {
            groupHolder!!.ivHasRelation.visibility = View.VISIBLE
        }

        if (TextUtils.isEmpty(transactionDetails.imageLink)) {
            groupHolder!!.ivHasImage.visibility = View.GONE
        } else {
            groupHolder!!.ivHasImage.visibility = View.VISIBLE
        }

        if (transactionDetails.verified) {
            view!!.setBackgroundColor(Color.parseColor("#FFF3F3F3"))
        } else {
            view!!.setBackgroundColor(Color.parseColor("#FBC7D9"))
        }

        if (transactionDetails.isTransactionSelected) {
            view!!.setBackgroundColor(Color.parseColor("#CDDC39"))
        }

        groupHolder!!.cbAddition.setOnClickListener(mTransactionViewActivity)
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
        var cbAddition = view.findViewById<CheckBox>(R.id.cb_for_addition)
        var tvRemarks = view.findViewById<TextView>(R.id.tv_remarks)
        var ivTracking = view.findViewById<ImageView>(R.id.iv_transaction_tracking)
        var ivHasBank = view.findViewById<ImageView>(R.id.iv_txn_bank)
        var ivHasRelation = view.findViewById<ImageView>(R.id.iv_txn_relation)
        var ivHasImage = view.findViewById<ImageView>(R.id.iv_txn_image)
        var rlTxnAmount = view.findViewById<RelativeLayout>(R.id.rl_txn_amount)
    }

    class ChildHolder(view: View) {
        var tvSenderID = view.findViewById<TextView>(R.id.tv_sender_id)
        val tvRecevierID = view.findViewById<TextView>(R.id.tv_receiver_id)
        val tvTransactionDate = view.findViewById<TextView>(R.id.tv_transaction_date)
        var tvAmount = view.findViewById<TextView>(R.id.tv_transaction_amount)
        var tvRoundOff = view.findViewById<TextView>(R.id.tv_transaction_round_off)
        var tvProjectID = view.findViewById<TextView>(R.id.tv_project_id)
        var tvSubCategory = view.findViewById<TextView>(R.id.tv_subcategory)
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
        var editTransaction = view.findViewById<TextView>(R.id.tv_edit_transaction)
        var switchTransactionTracking = view.findViewById<Switch>(R.id.switch_transaction_tracking)
        var btnRelatedTransactions = view.findViewById<TextView>(R.id.btn_related_transactions)

    }

    private fun getSpannableString(key: String?, value: String?, type: Int): SpannableString {
        if (key == null || value == null) return SpannableString(key)

        var bold = StyleSpan(android.graphics.Typeface.BOLD)
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

}