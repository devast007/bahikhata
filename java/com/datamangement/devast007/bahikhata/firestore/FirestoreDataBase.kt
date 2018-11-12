package com.datamangement.devast007.bahikhata.firestore

import android.util.Log
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.TransactionDetails
import com.datamangement.devast007.bahikhata.utils.UserProfile
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import java.util.*
import com.google.firebase.firestore.FirebaseFirestoreSettings




class FirestoreDataBase {
    private val TAG: String = "FirestoreDataBase"
    private var mFunctions: FirebaseFunctions = FirebaseFunctions.getInstance()
        get() = field

    val db = FirebaseFirestore.getInstance()
        get() = field


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

}
