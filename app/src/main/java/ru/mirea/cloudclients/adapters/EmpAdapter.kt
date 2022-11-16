package ru.mirea.cloudclients.adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.cloudclients.R
import ru.mirea.cloudclients.model.Employee
import java.util.*
import kotlin.collections.ArrayList

class EmpAdapter(private val emp: List<Employee>) :
    RecyclerView.Adapter<EmpAdapter.EmpViewHolder>(), Filterable {

    private lateinit var context: Context
    private var empFilterList = emp

    class EmpViewHolder(empView: View) : RecyclerView.ViewHolder(empView) {
        val number: TextView = itemView.findViewById(R.id.personId)
        val name: TextView = itemView.findViewById(R.id.personName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false)
        return EmpViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EmpViewHolder, position: Int) {
        holder.number.text = empFilterList[position].id.toString()
        holder.name.text = empFilterList[position].name
        holder.itemView.setOnClickListener {
            val bld = AlertDialog.Builder(context)
            val view = LayoutInflater.from(context).inflate(R.layout.person_dialog, null)
            bld.setView(view)
            view.findViewById<TextView>(R.id.idView).text =
                HtmlCompat.fromHtml(context.getString(R.string.id, empFilterList[position].id), HtmlCompat.FROM_HTML_MODE_COMPACT)
            view.findViewById<TextView>(R.id.nameView).text =
                HtmlCompat.fromHtml(context.getString(R.string.person_name ,empFilterList[position].name), HtmlCompat.FROM_HTML_MODE_COMPACT)
            view.findViewById<TextView>(R.id.emailView).text =
                HtmlCompat.fromHtml(context.getString(R.string.person_email ,empFilterList[position].email), HtmlCompat.FROM_HTML_MODE_COMPACT)
            view.findViewById<TextView>(R.id.positionView).text =
                HtmlCompat.fromHtml(context.getString(R.string.person_position, empFilterList[position].position), HtmlCompat.FROM_HTML_MODE_COMPACT)
            val dlg = bld.create()
            dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dlg.show()
        }

    }

    override fun getItemCount(): Int {
        return empFilterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                empFilterList = if (charSearch.isEmpty()) {
                    emp
                } else {
                    val resultList = ArrayList<Employee>()
                    for (row in emp) {
                        if (row.name.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = empFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, res: FilterResults?) {
                empFilterList = res?.values as MutableList<Employee>
                notifyDataSetChanged()
            }

        }
    }
}