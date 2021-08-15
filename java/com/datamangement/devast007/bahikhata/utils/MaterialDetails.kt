package com.datamangement.devast007.bahikhata.utils

import java.util.*

class MaterialDetails {

    var verified: Boolean = true
    var isMaterialSelected: Boolean = false
    var imageLink: String = ""
    var loggedInID: String = ""
    var materialID: String = ""
    var projectId: String = ""
    var supplierId: String = ""
    var reporterId: String = ""
    var amount: String = ""
    var rate: String = ""
    var quantity: String = ""
    var unit: String = ""
    var timeStamp: String = ""
    var materialOrService: String = ""
    var serviceType: String = ""
    var medium: String = ""
    var vehicleNo: String = ""
    var challanNo: String = ""
    var date: String = ""
    var subCategory: String = ""
    var remarks: String = ""

    // these field are exclusively for account statement
    var payment: Long = 0
    var bankAccount = ""
    var paidBy = ""
    var roundOff: Long = 0
    var paidTo = ""

    // these field for list view checkbox
    var isMaterialChecked = false

}