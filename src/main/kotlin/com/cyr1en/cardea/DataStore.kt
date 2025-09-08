package com.cyr1en.cardea

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import javax.sql.DataSource

val dataStore = DataStore()

class DataStore(
    uname: String = "sa",
    pwd: String = "",
) {
    private val databasePath: Path = Paths.get("plugins/Cardea/data.db")
    private val jdbcUrl = "jdbc:h2:file:${databasePath.toAbsolutePath()};AUTO_SERVER=TRUE"

    private val dataSource: DataSource =
        run {
            Class.forName("org.h2.Driver")
            val cfg =
                HikariConfig().apply {
                    jdbcUrl = this@DataStore.jdbcUrl
                    username = uname
                    password = pwd

                    maximumPoolSize = 10
                    minimumIdle = 2
                    idleTimeout = 60_000
                    connectionTimeout = 10_000
                    maxLifetime = 30 * 60_000
                    poolName = "CardeaHikariPool"
                    isAutoCommit = true
                }
            HikariDataSource(cfg)
        }

    init {
        initializeSchema()
    }

    private fun initializeSchema() {
        dataSource.connection.use { conn ->
            conn.createStatement().use {
                it.execute(
                    """
                    CREATE TABLE IF NOT EXISTS cardea_password (
                        id INT PRIMARY KEY CHECK (id = 1),
                        password_value VARCHAR(255)
                    )
                    """.trimIndent(),
                )
                it.execute(
                    """
                    CREATE TABLE IF NOT EXISTS logged_players (
                        uuid VARCHAR(36) PRIMARY KEY,
                        username VARCHAR(16)
                    )
                    """.trimIndent(),
                )
            }

            conn
                .prepareStatement(
                    """
                    INSERT INTO cardea_password (id, password_value)
                    SELECT 1, NULL
                    WHERE NOT EXISTS (SELECT 1 FROM cardea_password WHERE id = 1)
                    """.trimIndent(),
                ).use { it.executeUpdate() }
        }
    }

    fun getPassword(): String? {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT password_value FROM cardea_password WHERE id = 1").use { ps ->
                ps.executeQuery().use { rs ->
                    return if (rs.next()) rs.getString("password_value") else null
                }
            }
        }
    }

    fun setPassword(newPassword: String?) {
        dataSource.connection.use { conn ->
            conn.prepareStatement("UPDATE cardea_password SET password_value = ? WHERE id = 1").use { ps ->
                if (newPassword == null) ps.setNull(1, java.sql.Types.VARCHAR) else ps.setString(1, newPassword)
                ps.executeUpdate()
            }
        }
    }

    fun hasPassword(): Boolean = getPassword()?.isNotEmpty() == true

    fun addLogged(
        uuid: UUID,
        username: String,
    ) {
        dataSource.connection.use { conn ->
            conn.prepareStatement("MERGE INTO logged_players (uuid, username) KEY(uuid) VALUES (?, ?)").use { ps ->
                ps.setString(1, uuid.toString())
                ps.setString(2, username)
                ps.executeUpdate()
            }
        }
    }

    fun hasLogged(uuid: UUID): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT 1 FROM logged_players WHERE uuid = ?").use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeQuery().use { rs -> return rs.next() }
            }
        }
    }

    fun removeLogged(uuid: UUID) {
        dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM logged_players WHERE uuid = ?").use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeUpdate()
            }
        }
    }

    fun removeLogged(username: String) {
        dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM logged_players WHERE username = ?").use { ps ->
                ps.setString(1, username)
                ps.executeUpdate()
            }
        }
    }

    fun hasLogged(username: String): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT 1 FROM logged_players WHERE username = ?").use { ps ->
                ps.setString(1, username)
                ps.executeQuery().use { rs -> return rs.next() }
            }
        }
    }

    fun getLoggedUsernames(): List<String> {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT username FROM logged_players").use { ps ->
                ps.executeQuery().use { rs ->
                    val result = mutableListOf<String>()
                    while (rs.next()) {
                        val name = rs.getString("username")
                        if (name != null) result += name
                    }
                    return result
                }
            }
        }
    }

    fun removeAllLogged() {
        dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM logged_players").use { it.executeUpdate() }
        }
    }

    fun close() {
        if (dataSource is HikariDataSource) {
            dataSource.close()
        }
    }
}
