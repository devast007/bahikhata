package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.AlertDialog
import android.app.SearchManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentForServiceType
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentForUnitAndMedium
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_material_and_service_view.*
import java.util.*
import kotlin.collections.HashMap


class MaterialAndServiceViewActivity : AppCompatActivity(), View.OnClickListener,
    AdapterView.OnItemClickListener {


    var mSelectedMaterialServiceId: String? = null
    val TAG = "MaterialServiceView"
    var mContext: Context = this
    var mListView: ListView? = null
    public val mArrayList: ArrayList<IdAndNameObject> = ArrayList<IdAndNameObject>()
    var mItemDataList: ArrayList<Map<String, String>> =
        ArrayList<Map<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_and_service_view)
        mContext = this
        mListView = findViewById(R.id.lv_material_and_service)
        mListView!!.onItemClickListener = this
        supportActionBar!!.setTitle(R.string.manage_material_and_services)
        findViewById<ImageView>(R.id.iv_save_service_material).setOnClickListener(this)
        getMaterialAndServiceList()


    }

    override fun onStart() {
        super.onStart()
    }

    var queryTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                mSimpleAdapter!!.filter.filter(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                return true
            }
        }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_material_and_services, menu)

        val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView? = searchItem?.actionView as SearchView

        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setOnQueryTextListener(queryTextListener)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item!!.itemId) {
            R.id.action_add_units -> manageUnitOrMedium(LedgerDefine.TYPE_UNIT)
            R.id.action_add_medium -> manageUnitOrMedium(LedgerDefine.TYPE_MEDIUM)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun manageUnitOrMedium(type: Int) {

        mArrayList.clear()
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var doc = LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_UNITS
        if (type == LedgerDefine.TYPE_MEDIUM) {
            doc = LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MEDIUM
        }
        db.collection(doc)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        var details = IdAndNameObject()
                        details.id = document.get(LedgerDefine.ID) as String
                        details.name = document.get(LedgerDefine.NAME) as String
                        mArrayList.add(details)
                    }

                    var fm = supportFragmentManager
                    var dFragment = DialogFragmentForUnitAndMedium()
                    var bundle = Bundle()
                    bundle.putInt(LedgerDefine.KEY_SELECTION_TYPE, type)
                    dFragment.arguments = bundle
                    // Show DialogFragment
                    dFragment.show(fm, " project select Dialog Fragment")


                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })


    }


    private var mSimpleAdapter: SimpleAdapter? = null


    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.iv_save_service_material -> saveNewServiceMaterial()
        }
    }

    private fun saveNewServiceMaterial() {
        val text = et_add_service_material.text
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(mContext, "name is empty", Toast.LENGTH_SHORT).show()
            return
        }
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var id = "01"
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS)
                .orderBy(LedgerDefine.MATERIAL_OR_SERVICE_ID, Query.Direction.DESCENDING)
                .limit(1)
        docRef.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> {
                if (it.size() <= 0) {
                    // mNewProjectID = "001"
                } else {
                    var tempUserId: String =
                        it.documents[0].get(LedgerDefine.MATERIAL_OR_SERVICE_ID).toString()
                    var pre = tempUserId.substring(0, 2).toInt() + 1
                    id = "" + pre
                    if (pre < 10) {
                        id = "0$pre"
                    }
                }
                id += "_MS"

                val db = FirestoreDataBase().db
                val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
                var masterRef =
                    db.collection(
                        LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS
                    )
                        .document(id!!)

                val materialAndServiceMap = HashMap<String, Any>()
                var name = "" + text
                materialAndServiceMap[LedgerDefine.MATERIAL_OR_SERVICE_NAME] = name
                materialAndServiceMap[LedgerDefine.MATERIAL_OR_SERVICE_ID] = id
                materialAndServiceMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()


                masterRef.set(materialAndServiceMap) // 3rd(1), set the actual data with SYSTEM_MILLI
                    .addOnSuccessListener(OnSuccessListener<Void> {
                        val listItemMap = HashMap<String, String>()
                        listItemMap[LedgerDefine.MATERIAL_OR_SERVICE_NAME] = name
                        listItemMap[LedgerDefine.MATERIAL_OR_SERVICE_ID] = id
                        mItemDataList.add(listItemMap)
                        mSimpleAdapter!!.notifyDataSetChanged()
                        et_add_service_material.setText("")
                    })
                    .addOnFailureListener(OnFailureListener { e ->

                    })


            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(
                    TAG,
                    "Error writing document",
                    e
                )
            })
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        var id: String = "" + p1!!.findViewById<TextView>(android.R.id.text1).text
        var name: String = "" + p1!!.findViewById<TextView>(android.R.id.text2).text
        Log.d(TAG, " onItemClick AddOrUpdateMaterialAndServiceName  $id")
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        showServiceTypesList(id, name)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        UpdateMaterialAndServiceName(id, name)
                    }
                }
            }

        val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
        builder.setMessage("Please Choose Any One?")
            .setPositiveButton("SERVICE TYPES", dialogClickListener)
            .setNegativeButton("EDIT NAME", dialogClickListener).show()

    }


    private fun UpdateMaterialAndServiceName(id: String, name: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Name")
        builder.setCancelable(true)
        var ad: AlertDialog? = null
        var btnText = "UPDATE"
        val input = EditText(mContext)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        input.filters = arrayOf<InputFilter>(AllCaps())
        input.setText(name)
        builder.setView(input)
        builder.setPositiveButton(
            btnText
        )
        { dialogInterface, i ->
            // onlcick to update or add new item
            if (TextUtils.isEmpty(input.text)) {
                Toast.makeText(mContext, "name is empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val db = FirestoreDataBase().db
            val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
            var masterRef =
                db.collection(
                    LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS
                )
                    .document(id!!)

            val materialAndServiceMap = HashMap<String, Any>()
            var name = "" + input.text
            materialAndServiceMap[LedgerDefine.MATERIAL_OR_SERVICE_NAME] = name
            materialAndServiceMap[LedgerDefine.MATERIAL_OR_SERVICE_ID] = id
            materialAndServiceMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

                    masterRef.update(materialAndServiceMap)
                        .addOnSuccessListener(OnSuccessListener {
                            for (item in mItemDataList) {
                                Log.d(
                                    TAG,
                                    " item id " + (item as HashMap)[LedgerDefine.MATERIAL_OR_SERVICE_ID]
                                )

                                if ((item as HashMap)[LedgerDefine.MATERIAL_OR_SERVICE_ID] == id) {
                                    item[LedgerDefine.MATERIAL_OR_SERVICE_NAME] = name
                                    Log.d(
                                        TAG,
                                        " item name " + (item as HashMap)[LedgerDefine.MATERIAL_OR_SERVICE_NAME]
                                    )
                                    break
                                }
                            }
                            mSimpleAdapter!!.notifyDataSetChanged()

                        }).addOnFailureListener(OnFailureListener { e ->

                        })


            dialogInterface.dismiss()
        }
        ad = builder.show()
    }

    private fun showServiceTypesList(id: String, name: String) {
        mSelectedMaterialServiceId = id
        mArrayList.clear()
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS + "/" + mSelectedMaterialServiceId + LedgerDefine.SLASH_SERVICE_TYPES)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        var details = IdAndNameObject()
                        details.id = document.get(LedgerDefine.ID) as String
                        details.name = document.get(LedgerDefine.NAME) as String
                        mArrayList.add(details)
                    }

                    var fm = supportFragmentManager
                    var dFragment = DialogFragmentForServiceType()
                    var bundle = Bundle()
                    // bundle.putInt(LedgerDefine.KEY_SELECTION_TYPE, type)
                    dFragment.arguments = bundle
                    // Show DialogFragment
                    dFragment.show(fm, " project select Dialog Fragment")


                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })


    }


    private fun getMaterialAndServiceList(): Boolean {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {

                    for (document in task.result!!) {
                        val listItemMap = HashMap<String, String>()
                        listItemMap[LedgerDefine.MATERIAL_OR_SERVICE_ID] =
                            document.get(LedgerDefine.MATERIAL_OR_SERVICE_ID) as String
                        listItemMap[LedgerDefine.MATERIAL_OR_SERVICE_NAME] =
                            document.get(LedgerDefine.MATERIAL_OR_SERVICE_NAME) as String
                        mItemDataList.add(listItemMap)
                    }
                    mSimpleAdapter = SimpleAdapter(
                        this,
                        mItemDataList,
                        android.R.layout.simple_list_item_2,
                        arrayOf(
                            LedgerDefine.MATERIAL_OR_SERVICE_ID,
                            LedgerDefine.MATERIAL_OR_SERVICE_NAME
                        ),
                        intArrayOf(android.R.id.text1, android.R.id.text2)
                    )
                    mListView!!.adapter = mSimpleAdapter
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
        return true;
    }


    private fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                return false
            }
        } else {
            return true
        }
    }


    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
    }


}
