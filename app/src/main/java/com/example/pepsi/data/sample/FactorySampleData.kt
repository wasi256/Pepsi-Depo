package com.example.pepsi.data.sample

import com.example.pepsi.data.model.ManagerProfile
import com.example.pepsi.data.model.Worker
import com.example.pepsi.data.model.WorkerRole

/**
 * Placeholder data still used by the Factory Profile screen (manager identity and
 * who to notify for a password change), since there is no real login/session yet.
 * Everything else the factory module shows now comes from the live API --
 * see FactoryRepository.
 */
object FactorySampleData {

    private val workers = listOf(
        Worker("Joseph Tumusiime", "+256 700 777 888", WorkerRole.SystemAdmin),
    )

    val manager = ManagerProfile(
        name = "Diana Nakato",
        telephone = "+256 700 888 999",
        email = "diana.nakato@pepsidepo.com",
        password = "Pepsi@2026",
        gender = "Female",
    )

    val systemAdministrator = workers.first { it.role == WorkerRole.SystemAdmin }
}
