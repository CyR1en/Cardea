package com.cyr1en.cardea

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class DataStore(
    username: String = "sa",
    password: String = ""
) {
    private val databasePath: Path = Paths.get("plugins/Cardea/data.db")
    private val url = "jdbc:h2:file:${databasePath.toAbsolutePath()};AUTO_SERVER=TRUE"
    private val connection: Connection

    init {
        Class.forName("org.h2.Driver")
        connection = DriverManager.getConnection(url, username, password)
        initializeSchema()
    }

    private fun initializeSchema() {
        connection.createStatement().use {
            it.execute(
                """
                CREATE TABLE IF NOT EXISTS app_password (
                    id INT PRIMARY KEY CHECK (id = 1),
                    password_value VARCHAR(255)
                )
                """.trimIndent()
            )
            it.execute(
                """
                CREATE TABLE IF NOT EXISTS logged_players (
                    uuid VARCHAR(36) PRIMARY KEY
                )
                """.trimIndent()
            )
        }

        connection.prepareStatement(
            """
            INSERT INTO app_password (id, password_value)
            SELECT 1, NULL
            WHERE NOT EXISTS (SELECT 1 FROM app_password WHERE id = 1)
            """.trimIndent()
        ).use { it.executeUpdate() }
    }

    fun getPassword(): String? {
        connection.prepareStatement("SELECT password_value FROM app_password WHERE id = 1").use { ps ->
            ps.executeQuery().use { rs ->
                return if (rs.next()) rs.getString("password_value") else null
            }
        }
    }

    fun setPassword(newPassword: String?) {
        connection.prepareStatement("UPDATE app_password SET password_value = ? WHERE id = 1").use { ps ->
            if (newPassword == null) ps.setNull(1, java.sql.Types.VARCHAR) else ps.setString(1, newPassword)
            ps.executeUpdate()
        }
    }

    fun hasPassword(): Boolean = getPassword()?.isNotEmpty() == true

    fun addLogged(uuid: UUID) {
        connection.prepareStatement("MERGE INTO logged_players (uuid) KEY(uuid) VALUES (?)").use { ps ->
            ps.setString(1, uuid.toString())
            ps.executeUpdate()
        }
    }

    fun hasLogged(uuid: UUID): Boolean {
        connection.prepareStatement("SELECT 1 FROM logged_players WHERE uuid = ?").use { ps ->
            ps.setString(1, uuid.toString())
            ps.executeQuery().use { rs -> return rs.next() }
        }
    }

    fun removeLogged(uuid: UUID) {
        connection.prepareStatement("DELETE FROM logged_players WHERE uuid = ?").use { ps ->
            ps.setString(1, uuid.toString())
            ps.executeUpdate()
        }
    }

    fun removeAllLogged() {
        connection.prepareStatement("DELETE FROM logged_players").use { it.executeUpdate() }
    }

    fun close() {
        connection.close()
    }
}