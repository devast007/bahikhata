package com.datamangement.devast007.bahikhata.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.TransactionViewActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DialogFragmentTransactionFilter : DialogFragment() {


    private var listView: ListView? = null
    private val TAG = "DialogFragmentTransactionFilter"
    private var transactionViewActivity: TransactionViewActivity? = null
    private lateinit var mBottomSheetDialog: Dialog
    private var mType: Int = -1
    private var adapter: FilterAdapter? = null

    private val FILTER_SENDER = 0
    private val FILTER_RECIEVER = 1
    private val FILTER_WORK = 2
    private val FILTER_SUBCATEGORY = 3
    private var mFilter = FILTER_RECIEVER
    private var currentFilterArrayList: ArrayList<MutableMap.MutableEntry<String, String>> =
        ArrayList()

    private var rbReciever: RadioButton? = null
    private var rbSender: RadioButton? = null
    private var rbWork: RadioButton? = null
    private var rbSubcategory: RadioButton? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        transactionViewActivity = activity as TransactionViewActivity
        mBottomSheetDialog = Dialog(
            requireActivity(),
            R.style.MaterialDialogSheet
        )
        mBottomSheetDialog.setContentView(R.layout.dialog_transaction_filter)
        mBottomSheetDialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mBottomSheetDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.verticalMargin = 5.0f
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        mBottomSheetDialog.window!!.attributes = lp

        var titleSearchView = mBottomSheetDialog.findViewById<SearchView>(R.id.sv_dialog_filter)
        listView = mBottomSheetDialog.findViewById<ListView>(R.id.listview)

        rbReciever = mBottomSheetDialog.findViewById<RadioButton>(R.id.rb_receiver)
        rbSender = mBottomSheetDialog.findViewById<RadioButton>(R.id.rb_sender)
        rbWork = mBottomSheetDialog.findViewById<RadioButton>(R.id.rb_work)
        rbSubcategory = mBottomSheetDialog.findViewById<RadioButton>(R.id.rb_subcategory)

        mBottomSheetDialog.findViewById<FloatingActionButton>(R.id.btn_filter_ok)
            .setOnClickListener(View.OnClickListener {
                transactionViewActivity!!.setFilter()

                mBottomSheetDialog.cancel()
            })

        mBottomSheetDialog.findViewById<RadioGroup>(R.id.rg_filter).setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                titleSearchView.setQuery("", true)
                currentFilterArrayList.clear()
                when (checkedId) {
                    R.id.rb_receiver -> {
                        mFilter = FILTER_RECIEVER
                        for (item in transactionViewActivity!!.mReceiverUsersMapForFilter) {
                            currentFilterArrayList.add(item)
                        }
                    }

                    R.id.rb_sender -> {
                        mFilter = FILTER_SENDER
                        for (item in transactionViewActivity!!.mSenderUsersMapForFilter) {
                            currentFilterArrayList.add(item)
                        }
                    }

                    R.id.rb_work -> {
                        mFilter = FILTER_WORK
                        for (item in transactionViewActivity!!.mProjectsMapForFilter) {
                            currentFilterArrayList.add(item)
                        }
                    }
                    R.id.rb_subcategory -> {
                        mFilter = FILTER_SUBCATEGORY
                        for (item in transactionViewActivity!!.mSubcategoryForFilter) {
                            currentFilterArrayList.add(item)
                        }
                    }
                }
                if (adapter != null)
                    adapter!!.notifyDataSetChanged()
            })

        titleSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {

                currentFilterArrayList!!.clear()
                var tempMap: HashMap<String, String>? = null
                when (mFilter) {
                    FILTER_RECIEVER -> tempMap =
                        transactionViewActivity!!.mReceiverUsersMapForFilter
                    FILTER_SENDER -> tempMap = transactionViewActivity!!.mSenderUsersMapForFilter
                    FILTER_WORK -> tempMap = transactionViewActivity!!.mProjectsMapForFilter
                    FILTER_SUBCATEGORY -> tempMap = transactionViewActivity!!.mSubcategoryForFilter
                }
                if (tempMap != null) {
                    for (item in tempMap) {

                        if (newText.isEmpty() || item!!.value.contains(newText, true)) {
                            currentFilterArrayList.add(item)
                        }
                    }

                    listView!!.adapter = adapter


                }
                return true
            }
        })
        rbReciever!!.isChecked = true
        mFilter = FILTER_RECIEVER

        for (item in transactionViewActivity!!.mReceiverUsersMapForFilter) {
            currentFilterArrayList.add(item)
        }
        adapter = FilterAdapter()
        listView!!.adapter = adapter

        return mBottomSheetDialog
    }


    inner class FilterAdapter() : BaseAdapter() {
        private var mInflater: LayoutInflater =
            transactionViewActivity!!.mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view: View? = convertView
            if (view == null) {
                view = mInflater.inflate(R.layout.row_filter_view, parent, false);
            }
            var cbFilter = view!!.findViewById<CheckBox>(R.id.cb_filter)
            cbFilter.text = currentFilterArrayList[position].value
            var tempFilter: ArrayList<String>? = null
            when (mFilter) {
                FILTER_RECIEVER ->
                    tempFilter = transactionViewActivity!!.mFilterListReceiver
                FILTER_SENDER ->
                    tempFilter = transactionViewActivity!!.mFilterListSender
                FILTER_WORK ->
                    tempFilter = transactionViewActivity!!.mFilterListWork
                FILTER_SUBCATEGORY ->
                    tempFilter = transactionViewActivity!!.mFilterListSubcategory
            }
            val key = currentFilterArrayList[position].key
            Log.d("test", " tempFilter!!.contains(key)  " + tempFilter!!.size)
            cbFilter.isChecked = tempFilter!!.contains(key)

            cbFilter.setOnClickListener {
                val isChecked = (it as CheckBox).isChecked
                when (mFilter) {
                    FILTER_RECIEVER -> {
                        if (isChecked) {
                            transactionViewActivity!!.mFilterListReceiver.add(key)
                        } else {
                            transactionViewActivity!!.mFilterListReceiver.remove(key)
                        }
                    }
                    FILTER_SENDER -> {
                        if (isChecked) {
                            transactionViewActivity!!.mFilterListSender.add(key)
                        } else {
                            transactionViewActivity!!.mFilterListSender.remove(key)
                        }
                    }
                    FILTER_WORK -> {
                        if (isChecked) {
                            transactionViewActivity!!.mFilterListWork.add(key)
                        } else {
                            transactionViewActivity!!.mFilterListWork.remove(key)
                        }
                    }
                    FILTER_SUBCATEGORY -> {
                        if (isChecked) {
                            transactionViewActivity!!.mFilterListSubcategory.add(key)
                        } else {
                            transactionViewActivity!!.mFilterListSubcategory.remove(key)
                        }
                    }
                }

            }
            return view
        }

        override fun getItem(position: Int): Any {
            return currentFilterArrayList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return currentFilterArrayList.size
        }

    }

}


