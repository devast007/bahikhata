package com.datamangement.devast007.bahikhata.utils

import java.util.*
import kotlin.collections.ArrayList

class TransactionDetails {

    var imageLink: String =""
    var loggedInID: String = ""
        get() = field
        set(value) {
            field = value
        }

    var transactionID: String = ""
        get() = field
        set(value) {
            field = value
        }

    var projectId: String = ""
        get() = field
        set(value) {
            field = value
        }

    var senderId: String = ""
        get() = field
        set(value) {
            field = value
        }

    var receiverId: String = ""
        get() = field
        set(value) {
            field = value
        }

    var amount: Long = 0
        get() = field
        set(value) {
            field = value
        }
    var roundOff: Long = 0
        get() = field
        set(value) {
            field = value
        }
    var debitedTo: String = ""
        get() = field
        set(value) {
            field = value
        }
    var creditedTo: String = ""
        get() = field
        set(value) {
            field = value
        }
    var timeStamp: String = ""
        get() = field
        set(value) {
            field = value
        }

    var paymentMode: String = ""
        get() = field
        set(value) {
            field = value
        }


    var transactionDate: String = ""
        get() = field
        set(value) {
            field = value
        }


    var transactionType: Long = -1
        get() = field
        set(value) {
            field = value
        }

    var verified: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    var subCategory: String = ""
        get() = field
        set(value) {
            field = value
        }

    var remarks: String = ""
        get() = field
        set(value) {
            field = value
        }

    var systemMilli: String = ""
        get() = field
        set(value) {
            field = value
        }


    var isTrackingOn: Boolean = false
        get() = field
        set(value) {
            field = value
        }


// this is not a firestore data , it is for addition
    var isChecked: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    var relatedTransactionsIds: ArrayList<String>? = null

    var isTransactionSelected = false

}