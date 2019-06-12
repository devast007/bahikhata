package com.datamangement.devast007.bahikhata.firestore

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.FirebaseFunctions
import java.util.*
import kotlin.collections.HashMap


class FirestoreDataBase {
    private val TAG: String = "FirestoreDataBase"
    private var mFunctions: FirebaseFunctions = FirebaseFunctions.getInstance()
        get() = field

    val db = FirebaseFirestore.getInstance()
        get() = field


    public fun updateSkipField(docssnap: Task<QuerySnapshot>, context: Context) {
        // Get a new write batch
        val batch = db.batch()
        val companyID = LedgerSharePrefManger(context).getCompanyName()
// Update the population of 'SF'
        for (doc in docssnap.result!!) {
            var upDoc = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").document(doc.id)
            batch.update(upDoc, LedgerDefine.IS_SKIP, true)
        }

// Commit the batch
        batch.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all done now .......................................")
        }
    }

    public fun getTransactionData(userProfile: UserProfile, startPoint: Int): Task<TransactionDetails> {
        // Create the arguments to the callable function.
        val data: HashMap<String, Any> = HashMap()
        data.put(LedgerDefine.KEY_FETCHING_START_FROM, startPoint)
        data.put(LedgerDefine.KEY_USER_ID, userProfile.userId)
        data.put(LedgerDefine.KEY_COMPANY_ID, userProfile.companyId)

        return mFunctions
            .getHttpsCallable("getTransactions")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                task.result!!.data as TransactionDetails
            }
    }

    public fun callFireBaseFunction(): Task<Boolean> {
        // Create the arguments to the callable function.
        val data: HashMap<String, Any> = HashMap()
        data.put("type", 1);
        data.put("userId", 6010408);
        data.put("companyId", "company_001");
        //data.put("secondNumber", 42);

        return mFunctions
            .getHttpsCallable("callFireBaseFunction")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                Log.d("BOND", "" + task.result)
                Log.d("BOND", "" + task.result!!.data)
                task.result!!.data as Boolean
            }
    }


    fun setDocument() {
        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        db.firestoreSettings = settings
        var docRef = db.collection("companies/company_001/transactions").document()
        // [START set_document]
        val city = HashMap<String, Any>()
        city.put("senderID", "6010408")
        city.put("receiverID", "6010309")
        city.put("companyID", "company_001")
        city.put("amount", 500)





        docRef.set(city)
            .addOnSuccessListener(OnSuccessListener<Void> { Log.d(TAG, "DocumentSnapshot successfully written!") })
            .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
        // [END set_document]

    }

    fun createNewDocForProjects(context: Context, mProjectsList: ArrayList<ProjectDetails>) {
        val companyID = LedgerSharePrefManger(context).getCompanyName()
        val batchProjectDel = db.batch()
        val batchProjectadd = db.batch()
        var i = 1
        for (project in mProjectsList) {
            var name = project.name.replace("_", " ", true)
            var newID = name.toUpperCase().replace(" ", "", ignoreCase = true)
            if (i < 10) {
                newID = "00" + i + "_" + newID
            } else {
                newID = "0" + i + "_" + newID
            }
            i++
            val map = HashMap<String, Any>()
            map.put(LedgerDefine.PROJECT_ID, newID)
            map.put(LedgerDefine.NAME, name)
            map.put(LedgerDefine.ADDRESS, project.address)
            map.put(LedgerDefine.DIVISION, project.division)
            map.put(LedgerDefine.REMARK, project.remarks)
            map.put(LedgerDefine.START_DATE, project.startDate)
            map.put(LedgerDefine.END_DATE, project.endDate)
            map.put(LedgerDefine.AMOUNT, project.amount)
            val delete =
                db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
                    .document(project.projectID)
            //batchProjectDel.delete(delete)
            val add =
                db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS).document(newID)
            //batchProjectadd.set(add, map)
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions")
                .whereEqualTo(LedgerDefine.PROJECT_ID, project.projectID)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                    if (task.isSuccessful) {
                        val batchtran = db.batch()
                        for (document in task.result!!) {
                            Log.d(TAG, document.id + " => " + document.data)
                            Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                            var tran = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions")
                                .document(document.id)
                            // batchtran.update(tran, LedgerDefine.PROJECT_ID, newID)
                        }
                        if (task!!.result!!.size() > 0) {
                            batchtran.commit().addOnCompleteListener {
                                // ...
                                Log.d("devde", "all done now newID " + newID)
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.exception)
                    }
                })

        }


        batchProjectadd.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all batchProjectadd.commit()")
        }

// Commit the batch
        batchProjectDel.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all done batchProjectDel.commit()")
        }
    }

    fun createProjects(mTransactionList: ArrayList<TransactionDetails>, mContext: Context) {

        var pKeyMap: HashMap<String, Long> = HashMap()
        for (tDetails in mTransactionList) {
            if (!TextUtils.isEmpty(tDetails.projectId)) {
                if (pKeyMap[tDetails.projectId] == null) {
                    pKeyMap[tDetails.projectId] = -tDetails.amount
                } else {
                    pKeyMap[tDetails.projectId] = pKeyMap[tDetails.projectId]!! - tDetails.amount
                }
            }
        }

        var keys = pKeyMap.keys

        val companyID = LedgerSharePrefManger(mContext).getCompanyName()
        val batchProjectadd = db.batch()
        for (key in keys) {
            val project = HashMap<String, Any>()
            project.put(LedgerDefine.PROJECT_ID, key)
            project.put(LedgerDefine.NAME, key)
            project.put(LedgerDefine.ADDRESS, "")
            project.put(LedgerDefine.DIVISION, "")
            project.put(LedgerDefine.REMARK, "")
            project.put(LedgerDefine.START_DATE, "")
            project.put(LedgerDefine.END_DATE, "")
            project.put(LedgerDefine.AMOUNT, pKeyMap[key]!!)

            val add =
                db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS).document(key)
            // batchProjectadd.set(add, project)
        }

        batchProjectadd.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all batchProjectadd.commit()")
        }

    }


    fun updatePayeeNameUppercase(mBankDetails: ArrayList<BankAccountDetail>, mContext: Context) {


        val companyID = LedgerSharePrefManger(mContext).getCompanyName()
        val batchUpdatePayee = db.batch()
        for (detail in mBankDetails) {
            val bankAccount = HashMap<String, Any>()
            var payeeName = detail.payee.toString().toUpperCase().trim()
            Log.d("devde", "payeeName = " + payeeName)
            bankAccount.put(LedgerDefine.PAYEE_NAME, payeeName)
            val refs = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
                .document(detail.id)
            batchUpdatePayee.update(refs, bankAccount)
        }

        batchUpdatePayee.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all batchUpdatePayee.commit()")
        }

    }


    fun updateUserNameUppercase(usersDetails: ArrayList<UserDetails>, mContext: Context) {


        val companyID = LedgerSharePrefManger(mContext).getCompanyName()
        val batchUpdatePayee = db.batch()
        for (detail in usersDetails) {
            val user = HashMap<String, Any>()
            var name = detail.name.toString().toUpperCase().trim()
            Log.d("devde", "Name = $name")
            user[LedgerDefine.NAME] = name
            val refs = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
                .document(detail.userID)
            batchUpdatePayee.update(refs, user)
        }

        batchUpdatePayee.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all updateUserNameUppercase.commit()")
        }

    }

    fun setTimeStampForProjects(projectDetails: ArrayList<ProjectDetails>, mContext: Context) {


        val companyID = LedgerSharePrefManger(mContext).getCompanyName()
        val batchUpdateProjects = db.batch()
        for (detail in projectDetails) {
            val project = HashMap<String, Any>()
            project[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()
            val refs = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
                .document(detail.projectID)
            batchUpdateProjects.update(refs, project)
        }

        batchUpdateProjects.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all updateUserNameUppercase.commit()")
        }

    }

}
