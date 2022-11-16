package ru.mirea.cloudclients.fragments

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import ru.mirea.cloudclients.DatabaseController
import ru.mirea.cloudclients.Result
import ru.mirea.cloudclients.adapters.EmpAdapter
import ru.mirea.cloudclients.databinding.FragmentEmployeesBinding
import ru.mirea.cloudclients.model.Employee
import java.sql.ResultSet


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EmployeesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EmployeesFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var employees: ResultSet
    private lateinit var empView: RecyclerView
    private lateinit var binding: FragmentEmployeesBinding
    private lateinit var refresher: SwipeRefreshLayout
    private lateinit var adapter: EmpAdapter
    private var empList: MutableList<Employee> = mutableListOf<Employee>()
    private lateinit var addBtn: FloatingActionButton
    private lateinit var searchBar: SearchView

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
        binding = FragmentEmployeesBinding.inflate(inflater, container, false)
        empView = binding.empls
        searchBar = binding.searchBar
        searchBar.setOnClickListener {
            searchBar.isIconified = false
        }
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(new: String?): Boolean {
                adapter.filter.filter(new)
                return false
            }
        })
        refresher = binding.swipeContainer
        addBtn = binding.addEmp
        empView.layoutManager = LinearLayoutManager(context)
        adapter = EmpAdapter(empList)
        empView.adapter = adapter
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
         * @return A new instance of fragment FirstFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EmployeesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onRefresh() {
        lifecycleScope.launch{
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
            when(val result: Result<ResultSet> = DatabaseController.getEmployees()) {
                is Result.Success<ResultSet> -> employees = result.data
                is Result.Error -> {Toast.makeText(context, result.exception.message, Toast.LENGTH_SHORT).show()}
            }
            if (::employees.isInitialized){
                empList.clear()
                while (employees.next()) {
                    empList.add(
                        Employee(employees.getInt("id"),
                            employees.getString("name"),employees.getString("email"),employees.getString("pos"))
                    )
                }
                adapter.notifyDataSetChanged()
            }
            refresher.isRefreshing = false
            when(val result: Result<Boolean> = DatabaseController.isAdmin()) {
                is Result.Success<Boolean> -> {
                    if (result.data)
                        addBtn.visibility = View.VISIBLE
                }
                is Result.Error -> {Toast.makeText(context, result.exception.message, Toast.LENGTH_SHORT).show()}
            }
        }
    }
}