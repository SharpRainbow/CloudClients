package ru.mirea.cloudclients.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch
import ru.mirea.cloudclients.DatabaseController
import ru.mirea.cloudclients.Result
import ru.mirea.cloudclients.adapters.TaskAdapter
import ru.mirea.cloudclients.databinding.FragmentTasksBinding
import ru.mirea.cloudclients.model.Task
import java.sql.ResultSet

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TasksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TasksFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var tasks: ResultSet
    private lateinit var tskView: RecyclerView
    private lateinit var binding: FragmentTasksBinding
    private lateinit var refresher: SwipeRefreshLayout
    private lateinit var adapter: TaskAdapter
    private lateinit var filter: Spinner
    private var taskList: MutableList<Task> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        tskView = binding.tasks
        filter = binding.fltSpinner
        filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                adapter.filter.filter(parent?.getItemAtPosition(pos).toString())
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
        refresher = binding.tasksSwipeContainer
        tskView.layoutManager = LinearLayoutManager(context)
        adapter = TaskAdapter(taskList)
        tskView.adapter = adapter
        refresher.setOnRefreshListener(this)
        refresher.isRefreshing = true
        onRefresh()
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SecondFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TasksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            when(val result: Result<Boolean> = DatabaseController.checkConnection()) {
                is Result.Success<Boolean> -> {
                    if (!result.data){
                        Toast.makeText(context, "Ошибка сетевого запроса", Toast.LENGTH_SHORT).show()
                        refresher.isRefreshing = false
                        return@launch
                    }
                }
                is Result.Error -> {Toast.makeText(context, result.exception.message, Toast.LENGTH_SHORT).show()}
            }
            when(val result: Result<ResultSet> = DatabaseController.getTasks()) {
                is Result.Success<ResultSet> -> tasks = result.data
                is Result.Error -> {
                    Toast.makeText(context, result.exception.message, Toast.LENGTH_SHORT).show()}
            }
            if (::tasks.isInitialized){
                taskList.clear()
                while (tasks.next()) {
                    val tmp = Task(tasks.getInt("task_id"), tasks.getShort("priority"), null,
                        tasks.getTimestamp("creation_dt"), tasks.getTimestamp("execution_time"),
                        tasks.getTimestamp("completion_dt"), tasks.getString("description"),
                        tasks.getInt("contract_id"), tasks.getInt("client_id"), tasks.getInt("author"), tasks.getInt("executor"),
                        tasks.getString("type"))
                    val stat = tasks.getBoolean("status")
                    if (!tasks.wasNull())
                        tmp.status = stat
                    /*when(val result: Result<ResultSet> = DatabaseController.getEmployee(tasks.getInt("author"))) {
                        is Result.Success<ResultSet> -> {
                            val employee = result.data
                            if (employee.next()) {
                                tmp.author = Employee(
                                    employee.getInt("employee_id"),
                                    employee.getString("name"),
                                    employee.getString("email"),
                                    employee.getString("position")
                                )
                            }
                        }
                        is Result.Error -> {
                            Toast.makeText(context, result.exception.message, Toast.LENGTH_SHORT).show()}
                    }
                    when(val result: Result<ResultSet> = DatabaseController.getEmployee(tasks.getInt("executor"))) {
                        is Result.Success<ResultSet> -> {
                            val employee = result.data
                            if (employee.next()) {
                                tmp.executor = Employee(
                                    employee.getInt("employee_id"),
                                    employee.getString("name"),
                                    employee.getString("email"),
                                    employee.getString("position")
                                )
                            }
                        }
                        is Result.Error -> {
                            Toast.makeText(context, result.exception.message, Toast.LENGTH_SHORT).show()}
                    }*/
                    taskList.add(tmp)
                }
                adapter.notifyDataSetChanged()
                refresher.isRefreshing = false
            }
        }
    }
}