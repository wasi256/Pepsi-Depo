package com.example.pepsi.common.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.pepsi.common.model.AuditLog
import com.example.pepsi.common.model.Depo
import com.example.pepsi.common.model.Gender
import com.example.pepsi.common.model.Role
import com.example.pepsi.common.model.User
import com.example.pepsi.common.util.generatePassword
import java.util.Date

/**
 * In-memory data store standing in for a real backend. Holds Compose-observable
 * state shared across the system_admin screens for this early build of the module.
 */
object AdminDataStore {

    val users = mutableStateListOf(
        User("USR-001", "Amara", "Okafor", "0803 000 1111", "amara.okafor@pepsidepo.com", generatePassword(), Gender.FEMALE, Role.SYSTEM_ADMIN),
        User("USR-002", "Chinedu", "Eze", "0803 000 2222", "chinedu.eze@pepsidepo.com", generatePassword(), Gender.MALE, Role.MANAGER),
        User("USR-003", "Bola", "Adeyemi", "0803 000 3333", "bola.adeyemi@pepsidepo.com", generatePassword(), Gender.MALE, Role.DEPO_ATTENDANT),
        User("USR-004", "Ifeoma", "Nwosu", "0803 000 4444", "ifeoma.nwosu@pepsidepo.com", generatePassword(), Gender.FEMALE, Role.DEPO_ATTENDANT),
        User("USR-005", "Tunde", "Bakare", "0803 000 5555", "tunde.bakare@pepsidepo.com", generatePassword(), Gender.MALE, Role.DEPO_ATTENDANT),
    )

    val depos = mutableStateListOf(
        Depo("DEP-001", "Ikeja Central Depo", "Ikeja, Lagos", "USR-003"),
        Depo("DEP-002", "Aba Regional Depo", "Aba, Abia", "USR-004"),
        Depo("DEP-003", "Kano North Depo", "Kano", null),
    )

    val auditLogs = mutableStateListOf(
        AuditLog("LOG-001", Date(), "Amara Okafor", "SYSTEM_START", "System initialized"),
        AuditLog("LOG-002", Date(), "Amara Okafor", "USER_REGISTERED", "Registered Bola Adeyemi as Depo Attendant"),
        AuditLog("LOG-003", Date(), "Amara Okafor", "DEPO_REGISTERED", "Registered Ikeja Central Depo"),
    )

    val systemHealthPercent = mutableStateOf(96)

    // Historical readings preceding the live count/percent, used to draw the
    // Overview trend charts. The live value is always appended as the latest point.
    private val usersTrendHistory = listOf(2, 2, 3, 3, 4, 4)
    private val deposTrendHistory = listOf(1, 1, 1, 2, 2, 3)
    private val healthTrendHistory = listOf(90, 92, 91, 94, 95, 95)

    private var userSeq = users.size
    private var depoSeq = depos.size
    private var logSeq = auditLogs.size

    fun usersTrend(): List<Float> = (usersTrendHistory + users.size).map { it.toFloat() }

    fun deposTrend(): List<Float> = (deposTrendHistory + depos.size).map { it.toFloat() }

    fun healthTrend(): List<Float> = (healthTrendHistory + systemHealthPercent.value).map { it.toFloat() }

    fun depoAttendants(): List<User> = users.filter { it.role == Role.DEPO_ATTENDANT }

    fun userById(id: String?): User? = users.firstOrNull { it.id == id }

    fun nextUserId(): String = "USR-%03d".format(userSeq + 1)

    fun nextDepoId(): String = "DEP-%03d".format(depoSeq + 1)

    fun registerUser(
        firstName: String,
        lastName: String,
        tel: String,
        email: String,
        password: String,
        gender: Gender,
        role: Role,
        actor: String = "System Admin",
    ): User {
        userSeq += 1
        val user = User(
            id = "USR-%03d".format(userSeq),
            firstName = firstName,
            lastName = lastName,
            tel = tel,
            companyEmail = email,
            password = password,
            gender = gender,
            role = role,
        )
        users.add(user)
        logAction(actor, "USER_REGISTERED", "Registered ${user.fullName} as ${role.label}")
        return user
    }

    fun registerDepo(
        name: String,
        location: String,
        depoAttendantId: String?,
        actor: String = "System Admin",
    ): Depo {
        depoSeq += 1
        val depo = Depo(
            id = "DEP-%03d".format(depoSeq),
            name = name,
            location = location,
            depoAttendantId = depoAttendantId,
        )
        depos.add(depo)
        logAction(actor, "DEPO_REGISTERED", "Registered $name at $location")
        return depo
    }

    fun changePassword(userId: String, currentPassword: String, newPassword: String): Boolean {
        val index = users.indexOfFirst { it.id == userId }
        if (index == -1) return false
        val user = users[index]
        if (user.password != currentPassword) return false
        users[index] = user.copy(password = newPassword)
        logAction(user.fullName, "PASSWORD_CHANGED", "Password changed for ${user.fullName}")
        return true
    }

    private fun logAction(actor: String, action: String, details: String) {
        logSeq += 1
        auditLogs.add(0, AuditLog("LOG-%03d".format(logSeq), Date(), actor, action, details))
    }
}
