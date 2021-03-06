package com.datamangement.devast007.bahikhata.utils

class SignInProfile() {
    var isAdmin: Boolean = false
        get() = field
        set(value) {
            field = value
        }
    var isSupervisor: Boolean = false
        get() = field
        set(value) {
            field = value
        }
    var isHasLimitedAccess: Boolean = false
        get() = field
        set(value) {
            field = value
        }
    var isNormal: Boolean = false
        get() = field
        set(value) {
            field = value
        }
    var name: String = ""
        get() = field
        set(value) {
            field = value
        }
    var userID: String = ""
        get() = field
        set(value) {
            field = value
        }

    var normalAmount: Long = 0
        get() = field
        set(value) {
            field = value
        }

    var supervisorAmount: Long = 0
        get() = field
        set(value) {
            field = value
        }
    var email: String = ""
        get() = field
        set(value) {
            field = value
        }
    var phone: String = ""
        get() = field
        set(value) {
            field = value
        }
    var address: String = ""
        get() = field
        set(value) {
            field = value
        }
    var accesibleProjects: ArrayList<String>? = null
        get() = field
        set(value) {
            field = value
        }

    var userAccounts: ArrayList<String>? = null
        get() = field
        set(value) {
            field = value
        }
}