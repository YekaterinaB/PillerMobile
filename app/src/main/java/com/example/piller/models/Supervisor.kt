package com.example.piller.models


class Supervisor(
    name: String,
    email: String,
    confirmed: Boolean
) {
    private var supervisorName = name
    private var supervisorEmail = email
    private var isConfirmed = confirmed

    fun getSupervisorName(): String {

        return supervisorName
    }

    fun getIsConfirmed(): Boolean {

        return isConfirmed
    }

    fun setIsConfirmedToTrue() {
        isConfirmed = true
    }

    fun getsupervisorEmail(): String {
        return supervisorEmail
    }

}