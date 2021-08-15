package com.datamangement.devast007.bahikhata.utils

class UserDetails {
    var name: String = ""
        get() = field
        set(value) {
            field = value
        }

    var amount: Long = 0
        get() = field
        set(value) {
            field = value
        }

    var p_MaterialCost: Long = 0
        get() = field
        set(value) {
            field = value
        }
    var p_Payment: Long = 0
        get() = field
        set(value) {
            field = value
        }
    var p_gstBill: Long = 0
        get() = field
        set(value) {
            field = value
        }

    var m_Amount: Long = 0
        get() = field
        set(value) {
            field = value
        }

    var userID = ""
        get() = field
        set(value) {
            field = value
        }

    var address = ""
        get() = field
        set(value) {
            field = value
        }

    var email = ""
        get() = field
        set(value) {
            field = value
        }
    var phone = ""
        get() = field
        set(value) {
            field = value
        }

    var accesibleProjectsStr = ""
        get() = field
        set(value) {
            field = value
        }
    var accesibleProjectsList: ArrayList<String>? = null
        get() = field
        set(value) {
            field = value
        }

    var userAccounts: ArrayList<String>? = null
        get() = field
        set(value) {
            field = value
        }

    var userCreatedDate = ""
        get() = field
        set(value) {
            field = value
        }

    var designation : Long = -1
        get() = field
        set(value) {
            field = value
        }

    var remarks = ""
        get() = field
        set(value) {
            field = value
        }

    var isUserDisable = false

    var isHasEditPermission = false
}