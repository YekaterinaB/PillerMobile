package com.example.piller.models


class Supervisor(
    name: String,
    email: String
) {
    private var supervisorName=name
    private var supervisorEmail=email

    fun getSupervisorName():String{

        return supervisorName
    }

    fun getsupervisorEmail(): String {
        return supervisorEmail
    }

}