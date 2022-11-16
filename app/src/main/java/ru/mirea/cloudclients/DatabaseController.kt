package ru.mirea.cloudclients

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

object DatabaseController {

    private const val jdbcUrl =
        "jdbc:postgresql://dpg-cdcpslmn6mpsbhf0ehdg-a.frankfurt-postgres.render.com:5432/practice_0odm"
    private val mutex = Mutex()
    private lateinit var connection: Connection
    private lateinit var sPassword: String
    lateinit var sLogin: String
    var id: Int = 0

    suspend fun login(login: String, password: String): Result<String> {
        var message: Result<String>
        withContext(Dispatchers.IO) {
            try {
                connection = DriverManager.getConnection(
                    jdbcUrl, login,
                    password
                )
                message = if (connection.isValid(0)) {
                    sPassword = password
                    sLogin = login
                    val query = connection.createStatement()
                    val dataSet = query.executeQuery("SELECT * FROM employees WHERE login = current_user")
                    dataSet.next()
                    id = dataSet.getInt("employee_id")
                    Result.Success("Вход выполнен!")
                }
                else
                    Result.Error(Exception("Ошибка соединения"))
            } catch (e: PSQLException) {
                message = Result.Error(e)
            }
        }
        return message
    }

    suspend fun getEmployees(): Result<ResultSet> {
        var data: Result<ResultSet>
        withContext(Dispatchers.IO) {
            mutex.withLock {
                data = try {
                    if (connection.isValid(0)) {
                        val query = connection.createStatement()
                        Result.Success(query.executeQuery("SELECT * FROM getEmployees()"))
                    } else
                        Result.Error(Exception("Ошибка запроса"))
                } catch (e: PSQLException) {
                    Result.Error(e)
                }
            }
        }
        return data
    }

    suspend fun getSelfInfo(): Result<String>{
        var data: Result<String>
        withContext(Dispatchers.IO) {
            mutex.withLock {
                data = try {
                    if (sLogin == "admin")
                        Result.Success("Admin")
                    else if (connection.isValid(0)) {
                        val query = connection.createStatement()
                        val dataSet =
                            query.executeQuery("SELECT * FROM employees WHERE login = current_user")
                        dataSet.next()
                        Result.Success("${dataSet.getInt("employee_id")} ${dataSet.getString("name")}")
                    } else
                        Result.Error(Exception("Ошибка запроса"))
                } catch (e: PSQLException) {
                    Result.Error(e)
                }
            }
        }
        return data
    }

    suspend fun checkConnection(): Result<Boolean> {
        var valid: Result<Boolean>
        withContext(Dispatchers.IO) {
            mutex.withLock {
                if (connection.isValid(0)) {
                    valid = Result.Success(true)
                    return@withContext
                }
                valid = try {
                    connection.close()
                    connection = DriverManager.getConnection(
                        jdbcUrl, sLogin,
                        sPassword
                    )
                    Result.Success(connection.isValid(0))
                } catch (e: PSQLException) {
                    Result.Error(e)
                }
            }
        }
        return valid;
    }

    suspend fun getEmployee(id: Int): Result<ResultSet> {
        var data: Result<ResultSet>
        withContext(Dispatchers.IO) {
            mutex.withLock {
                data = try {
                    if (connection.isValid(0)) {
                        val query = connection.prepareStatement(
                            "SELECT employees.employee_id," +
                                    " employees.name, employees.email, employee_position.name AS position" +
                                    " FROM employees LEFT JOIN employee_position ON " +
                                    "employees.position_id = employee_position.position_id WHERE employee_id = ?"
                        )
                        query.setInt(1, id)
                        Result.Success(query.executeQuery())
                    } else
                        Result.Error(Exception("Ошибка запроса"))
                } catch (e: PSQLException) {
                    Result.Error(e)
                }
            }
        }
        return data
    }

    suspend fun getTasks(): Result<ResultSet> {
        var data: Result<ResultSet>
        withContext(Dispatchers.IO) {
            mutex.withLock {
                data = try {
                    if (connection.isValid(0)) {
                        val query = connection.createStatement()
                        Result.Success(
                            query.executeQuery(
                                "SELECT task_id, priority, status, creation_dt," +
                                        " execution_time, completion_dt, description, contract_id, client_id," +
                                        " author, executor, task_classifier.name as type FROM tasks LEFT JOIN" +
                                        " task_classifier ON tasks.tt_id = task_classifier.tt_id ORDER BY status"
                            )
                        )
                    } else
                        Result.Error(Exception("Ошибка запроса"))
                } catch (e: PSQLException) {
                    Result.Error(e)
                }
            }
        }
        return data
    }

    suspend fun closeConnection(): Result<String> {
        var message: Result<String> = Result.Success("Closed")
        withContext(Dispatchers.IO) {
            mutex.withLock {
                try {
                    if (::connection.isInitialized)
                        connection.close()
                } catch (e: PSQLException) {
                    message = Result.Error(e)
                }
            }
        }
        return message
    }

    suspend fun isManager(): Result<Boolean> {
        var status: Result<Boolean> = Result.Success(true)
        withContext(Dispatchers.IO) {
            mutex.withLock {
                status = try {
                    if (connection.isValid(0)) {
                        val query = connection.createStatement()
                        val result =
                            query.executeQuery("SELECT pg_has_role(current_user, 'manager', 'MEMBER')")
                        result.next()
                        Result.Success(result.getString("pg_has_role") == "t")
                    } else
                        Result.Error(Exception("Ошибка запроса"))
                } catch (e: PSQLException) {
                    Result.Error(e)
                }
            }
        }
        return status
    }

    suspend fun isAdmin(): Result<Boolean> {
        var status: Result<Boolean> = Result.Success(true)
        withContext(Dispatchers.IO) {
            mutex.withLock {
                status = try {
                    if (connection.isValid(0)) {
                        val query = connection.createStatement()
                        val result =
                            query.executeQuery("SELECT pg_has_role(current_user, 'admin', 'MEMBER')")
                        result.next()
                        Result.Success(result.getString("pg_has_role") == "t")
                    } else
                        Result.Error(Exception("Ошибка запроса"))
                } catch (e: PSQLException) {
                    Result.Error(e)
                }
            }
        }
        return status
    }

    suspend fun isInit(): Boolean {
        var result: Boolean = false
        withContext(Dispatchers.IO) {
            mutex.withLock {
                result = connection.isValid(0)
            }
        }
        return result
    }

}