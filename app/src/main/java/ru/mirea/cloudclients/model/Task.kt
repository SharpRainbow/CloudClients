package ru.mirea.cloudclients.model

import java.sql.Timestamp

data class Task(val id: Int, var priority: Short, var status: Boolean?, val created: Timestamp,
                var deadline: Timestamp?, var completed: Timestamp?, var description: String?,
                val contract: Int?, val client: Int, var author: Int,
                var executor: Int?, val type: String)