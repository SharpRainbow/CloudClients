package ru.mirea.cloudclients.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.mirea.cloudclients.DatabaseController
import ru.mirea.cloudclients.R
import ru.mirea.cloudclients.model.Task
import java.sql.ResultSet
import java.text.SimpleDateFormat
import ru.mirea.cloudclients.Result
import ru.mirea.cloudclients.model.Employee
import java.util.*
import kotlin.collections.ArrayList

class TaskAdapter(private val tasks: List<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>(), Filterable {

        private lateinit var context: Context
        private var taskListFilter = tasks

        class TaskViewHolder(taskView: View) : RecyclerView.ViewHolder(taskView) {
            val id: TextView = itemView.findViewById(R.id.taskID)
            val type: TextView = itemView.findViewById(R.id.taskType)
            val pr: ImageView = itemView.findViewById(R.id.prioprityView)
            val stat: TextView = itemView.findViewById(R.id.taskStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            context = parent.context
            val itemView = LayoutInflater.from(context).inflate(R.layout.recycler_item_task, parent, false)
            return TaskViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            val item = taskListFilter[position]
            holder.id.text = item.id.toString()
            holder.type.text = item.type
            when(item.priority.toInt()){
                1 -> holder.pr.setImageResource(R.drawable.priority_low)
                2 -> holder.pr.setImageResource(R.drawable.priority_mid)
                3 -> holder.pr.setImageResource(R.drawable.priority_high)
            }
            val status = when(item.status){
                null -> "Создано"
                false -> "Выполняется"
                true -> "Выполнено"
            }
            holder.stat.text = status
            holder.itemView.setOnClickListener {
                val bld = AlertDialog.Builder(context)
                val view = LayoutInflater.from(context).inflate(R.layout.task_dialog, null)
                bld.setView(view)
                view.findViewById<TextView>(R.id.taskIdView).text =
                    HtmlCompat.fromHtml(context.getString(R.string.id, item.id), HtmlCompat.FROM_HTML_MODE_COMPACT)
                view.findViewById<TextView>(R.id.taskPrView).text =
                    HtmlCompat.fromHtml(context.getString(R.string.priority, item.priority), HtmlCompat.FROM_HTML_MODE_COMPACT)
                view.findViewById<TextView>(R.id.taskStView).text =
                    HtmlCompat.fromHtml(context.getString(R.string.status, status), HtmlCompat.FROM_HTML_MODE_COMPACT)
                view.findViewById<TextView>(R.id.taskCrDt).text =
                    HtmlCompat.fromHtml(context.getString(R.string.cr_dt, SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(item.created)), HtmlCompat.FROM_HTML_MODE_COMPACT)
                view.findViewById<TextView>(R.id.taskDeadline).text =
                    HtmlCompat.fromHtml(context.getString(R.string.deadline, when(item.deadline){
                        null -> "Без срока выполнения"
                        else -> SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(item.deadline)
                    }), HtmlCompat.FROM_HTML_MODE_COMPACT)
                view.findViewById<TextView>(R.id.taskEndDt).text =
                    HtmlCompat.fromHtml(context.getString(R.string.end_dt, when(item.completed){
                        null -> "Задача выполняется"
                        else -> SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(item.completed)
                    }), HtmlCompat.FROM_HTML_MODE_COMPACT)
                view.findViewById<TextView>(R.id.taskDesc).text =
                    HtmlCompat.fromHtml(context.getString(R.string.desc, item.description), HtmlCompat.FROM_HTML_MODE_COMPACT)
                view.findViewById<TextView>(R.id.taskCont).text = when(item.contract){
                    0 -> "Контракт не заключен"
                    else -> item.contract.toString()
                }
                view.findViewById<TextView>(R.id.taskTpView).text =
                    HtmlCompat.fromHtml(context.getString(R.string.type, item.type), HtmlCompat.FROM_HTML_MODE_COMPACT)
                val authorTv = view.findViewById<TextView>(R.id.taskAuth)
                val execTv = view.findViewById<TextView>(R.id.taskEx)
                val clientTv = view.findViewById<TextView>(R.id.taskCl)
                clientTv.text = item.client.toString()
                val dlg = bld.create()
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                (context as LifecycleOwner).lifecycleScope.launch {
                    when(val result: Result<ResultSet> = DatabaseController.getEmployee(item.author)) {
                        is Result.Success<ResultSet> -> {
                            val employee = result.data
                            if (employee.next()) {
                                val author = Employee(
                                    employee.getInt("employee_id"),
                                    employee.getString("name"),
                                    employee.getString("email"),
                                    employee.getString("position")
                                )
                                authorTv.text =
                                    HtmlCompat.fromHtml(context.getString(R.string.person_link, author.name), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                authorTv.setOnClickListener {
                                    val builder = AlertDialog.Builder(context)
                                    val viewPerson = LayoutInflater.from(context).inflate(R.layout.person_dialog, null)
                                    builder.setView(viewPerson)
                                    viewPerson.findViewById<TextView>(R.id.idView).text =
                                        HtmlCompat.fromHtml(context.getString(R.string.id, author.id), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                    viewPerson.findViewById<TextView>(R.id.nameView).text =
                                        HtmlCompat.fromHtml(context.getString(R.string.person_name, author.name), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                    viewPerson.findViewById<TextView>(R.id.emailView).text =
                                        HtmlCompat.fromHtml(context.getString(R.string.person_email, author.email), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                    viewPerson.findViewById<TextView>(R.id.positionView).text =
                                        HtmlCompat.fromHtml(context.getString(R.string.person_position, author.position), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                    val dialog = builder.create()
                                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                    dialog.show()
                                }
                            }
                        }
                        is Result.Error -> {
                            Toast.makeText(context, result.exception.message, Toast.LENGTH_SHORT).show()}
                    }
                    item.executor?.let {
                        when(val result: Result<ResultSet> = DatabaseController.getEmployee(it)) {
                            is Result.Success<ResultSet> -> {
                                val employee = result.data
                                if (employee.next()) {
                                    val executor = Employee(
                                        employee.getInt("employee_id"),
                                        employee.getString("name"),
                                        employee.getString("email"),
                                        employee.getString("position")
                                    )
                                    execTv.text =
                                        HtmlCompat.fromHtml(context.getString(R.string.person_link, executor.name), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                    execTv.setOnClickListener {
                                        val builder = AlertDialog.Builder(context)
                                        val viewPerson = LayoutInflater.from(context).inflate(R.layout.person_dialog, null)
                                        builder.setView(viewPerson)
                                        viewPerson.findViewById<TextView>(R.id.idView).text =
                                            HtmlCompat.fromHtml(context.getString(R.string.id, executor.id), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                        viewPerson.findViewById<TextView>(R.id.nameView).text =
                                            HtmlCompat.fromHtml(context.getString(R.string.person_name, executor.name), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                        viewPerson.findViewById<TextView>(R.id.emailView).text =
                                            HtmlCompat.fromHtml(context.getString(R.string.person_email, executor.email), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                        viewPerson.findViewById<TextView>(R.id.positionView).text =
                                            HtmlCompat.fromHtml(context.getString(R.string.person_position, executor.position), HtmlCompat.FROM_HTML_MODE_COMPACT)
                                        val dialog = builder.create()
                                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                        dialog.show()
                                    }
                                }
                            }
                            is Result.Error -> {
                                Toast.makeText(context, result.exception.message, Toast.LENGTH_SHORT).show()}
                        }
                    }
                }
                dlg.show()
            }
        }

        override fun getItemCount(): Int {
            return taskListFilter.size
        }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                taskListFilter = when (charSearch) {
                    "Доступные" -> {
                        tasks
                    }
                    "Созданные мной" -> {
                        val resultList = ArrayList<Task>()
                        for (row in tasks) {
                            if (row.author == DatabaseController.id
                            ) {
                                resultList.add(row)
                            }
                        }
                        resultList
                    }
                    "Мои задачи" -> {
                        val resultList = ArrayList<ru.mirea.cloudclients.model.Task>()
                        for (row in tasks) {
                            if (row.executor == DatabaseController.id
                            ) {
                                resultList.add(row)
                            }
                        }
                        resultList
                    }
                    else -> {
                        tasks
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = taskListFilter
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, res: FilterResults?) {
                taskListFilter = res?.values as MutableList<Task>
                notifyDataSetChanged()
            }

        }
    }
}