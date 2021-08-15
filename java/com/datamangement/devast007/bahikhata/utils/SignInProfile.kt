package com.datamangement.devast007.bahikhata.utils

class SignInProfile() {
    var isHasEditPermission: Boolean = false

    var isAdmin: Boolean = false

    var isSupervisor: Boolean = false

    var isHasLimitedAccess: Boolean = false

    var isNormal: Boolean = false

    var name: String = ""

    var userID: String = ""

    var normalAmount: Long = 0

    var supervisorAmount: Long = 0

    var email: String = ""

    var phone: String = ""

    var address: String = ""

    var accesibleProjects: ArrayList<String>? = null

    var userAccounts: ArrayList<String>? = null

}