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


class FirestoreDataBase {
    private val TAG: String = "FirestoreDataBase"
    private var mFunctions: FirebaseFunctions = FirebaseFunctions.getInstance()
        get() = field

    val db = FirebaseFirestore.getInstance()
        get() = field


    public fun updateSkipField(docssnap: Task<QuerySnapshot>, context: Context) {
        // Get a new write batch
        val batch = db.batch()
        val companyID = LedgerSharePrefManger(context).getCompanyID()
// Update the population of 'SF'
        for (doc in docssnap.result!!) {
            var upDoc = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS).document(doc.id)
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
        val companyID = LedgerSharePrefManger(context).getCompanyID()
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
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .whereEqualTo(LedgerDefine.PROJECT_ID, project.projectID)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                    if (task.isSuccessful) {
                        val batchtran = db.batch()
                        for (document in task.result!!) {
                            Log.d(TAG, document.id + " => " + document.data)
                            Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                            var tran = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
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

        val companyID = LedgerSharePrefManger(mContext).getCompanyID()
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


    fun setBankAccounts(mBankDetails: ArrayList<BankAccountDetail>, mContext: Context) {


        val companyID = "DBCONSTRUCTIONS" // LedgerSharePrefManger(mContext).getCompanyID()
        val batchUpdatePayee = db.batch()
        for (detail in mBankDetails) {
            val account = HashMap<String, Any>()
            account[LedgerDefine.BANK_ACCOUNT_ID] = detail.id
            account[LedgerDefine.BANK_ACCOUNT_NUMBER] = detail.accountNo
            account[LedgerDefine.PAYEE_NAME] = detail.payee
            account[LedgerDefine.IFSC_CODE] = detail.ifscCode
            account[LedgerDefine.BANK_ACCOUNT_BRANCH_NAME] = detail.branch
            account[LedgerDefine.REMARK] = detail.remarks
            account[LedgerDefine.TIME_STAMP] = detail.timestamp
            val refs = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
                .document(detail.id)
            batchUpdatePayee.set(refs, account)
        }

        batchUpdatePayee.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all batchUpdatePayee.commit()")
        }

    }


    fun setUsers(usersDetails: ArrayList<UserDetails>, mContext: Context) {
        val companyID = "DBCONSTRUCTIONS" // LedgerSharePrefManger(mContext).getCompanyID()
        val batchUpdatePayee = db.batch()
        for (detail in usersDetails) {
            Log.d("devde", "   for (detail in usersDetails) ")

            val user = HashMap<String, Any>()

            Log.d("devde", " user[LedgerDefine.USER_ID] = newUserID")
            user[LedgerDefine.USER_ID] = detail.userID.toLong()
            user[LedgerDefine.NAME] = detail.name
            user[LedgerDefine.ADDRESS] = detail.address
            user[LedgerDefine.EMAIL] = detail.email
            user[LedgerDefine.PHONE_NUMBER] = detail.phone
            user[LedgerDefine.REMARK] = detail.remarks
            user[LedgerDefine.ACCOUNTS] = detail.userAccounts as ArrayList<String>
            user[LedgerDefine.DESIGNATION] = detail.designation
            user[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()
            // user[LedgerDefine.ACCESSIBLE_PROJECTS] = detail.accesibleProjectsList!!

            val refs = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
                .document(detail.userID)
            batchUpdatePayee.set(refs, user)
        }

        batchUpdatePayee.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all setUsers.commit()")
        }

    }

    fun setProjetcs(projectDetails: ArrayList<ProjectDetails>, mContext: Context) {

        val companyID = "DBCONSTRUCTIONS" // LedgerSharePrefManger(mContext).getCompanyID()
        val batchUpdateProjects = db.batch()

        for (detail in projectDetails) {
            val project = java.util.HashMap<String, Any>()
            val projectID = detail.projectID
            project[LedgerDefine.PROJECT_ID] = projectID
            project[LedgerDefine.NAME] = detail.name
            project[LedgerDefine.ADDRESS] = detail.address
            project[LedgerDefine.DIVISION] = detail.division
            project[LedgerDefine.REMARK] = detail.remarks
            project[LedgerDefine.START_DATE] = detail.startDate
            project[LedgerDefine.END_DATE] = detail.endDate
            project[LedgerDefine.TIME_STAMP] = detail.timeStamp.toString()
            project[LedgerDefine.MB_NO] = detail.mbNo
            project[LedgerDefine.HEAD] = detail.head
            project[LedgerDefine.MAIN_AMOUNT] = detail.mainAmount
            project[LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT] = detail.maintenace1stYearAmount
            project[LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT] = detail.maintenace2ndYearAmount
            project[LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT] = detail.maintenace3rdYearAmount
            project[LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT] = detail.maintenace4thYearAmount
            project[LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT] = detail.maintenace5thYearAmount

            val refs = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
                .document(projectID)
            batchUpdateProjects.set(refs, project)
        }

        batchUpdateProjects.commit().addOnCompleteListener {
            // ...
            Log.d("devde", "all setUsers.commit()")
        }

    }

    fun setTransactions(mContext: Context?, mTransactionList: ArrayList<TransactionDetails>) {

        //
        var count = 1
        val size = mTransactionList.size
        var batchUpdatePayee = db.batch()
        for (details in mTransactionList) {
            val transactionMap = HashMap<String, Any>()
            transactionMap[LedgerDefine.VERIFIED] = details.verified
            transactionMap[LedgerDefine.LOGGED_IN_ID] = details.loggedInID
            // amount
            transactionMap[LedgerDefine.AMOUNT] = details.amount

            Log.d("devde", "all done  transactionMap[LedgerDefine.AMOUNT] = details.amount")
            // debit/credit account
            transactionMap[LedgerDefine.DEBIT_ACCOUNT_ID] = details.debitedTo


            transactionMap[LedgerDefine.CREDIT_ACCOUNT_ID] = details.creditedTo

            transactionMap[LedgerDefine.PAYMENT_MODE] = details.paymentMode

            //project


            transactionMap[LedgerDefine.PROJECT_ID] = details.projectId
            //sender
            transactionMap[LedgerDefine.SENDER_ID] =details.senderId

            //receiver
            transactionMap[LedgerDefine.RECEIVER_ID] = details.receiverId

            // transaction date
            transactionMap[LedgerDefine.TRANSACTION_DATE] = details.transactionDate

            // timestamp
            transactionMap[LedgerDefine.TIME_STAMP] = details.timeStamp

            Log.d("devde", "all done  transactionMap[LedgerDefine.TIME_STAMP] = details.timeStamp.toString())")
            transactionMap[LedgerDefine.TRANSACTION_TYPE] = details.transactionType
            transactionMap[LedgerDefine.REMARK] = details.remarks
            transactionMap[LedgerDefine.TRANSACTION_ID] = details.transactionID

            val companyID = "DBCONSTRUCTIONS"//LedgerSharePrefManger(mContext).getCompanyID()

            val refs = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .document(details.transactionID)
            batchUpdatePayee.set(refs, transactionMap)
            Log.d("devde", "all done1  batchUpdatePayee.set(refs, transactionMap)")
            //
            count += 1
            if (count % 500 == 0 || count >= size) {

                //Log.d("devde", "all done now ..count = "+count )
                batchUpdatePayee.commit().addOnCompleteListener {
                    // ...
                    Log.d("devde", "all done now ..........." + count + " ,it.exception = " + it.exception)
                }

                Log.d("devde", "Thread sleeping1 .... ")
                Thread.sleep(3000)
                batchUpdatePayee = db.batch()
                Log.d("devde", "Thread sleeping done ")

            }

        }


    }

    private fun getProjectID(projectId: String): String {
        var id: String = ""
        if (!TextUtils.isEmpty(projectId)) {
            var temp = projectId.substring(8).toInt()
            if(temp < 10 ){
                id = "00"+temp+"_PROJECT"
            }else{
                id = "0"+temp+"_PROJECT"
            }
        }

        return id
    }

    private fun getUserID(userid: String): String {

        var id: String? = null
        val temp: Int = userid.toInt()
        if (temp == 3010000) {
            id = "A_$temp"
        } else if (temp < 6010000) {
            id = "M_$temp"
        } else {
            id = "P_" + (temp - 1000000)
        }
        return id
    }

}
