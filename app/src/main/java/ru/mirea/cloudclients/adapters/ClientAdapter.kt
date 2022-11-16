package ru.mirea.cloudclients.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.cloudclients.R
import ru.mirea.cloudclients.model.Client

class ClientAdapter(private val clients: List<Client>) :
    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    private lateinit var context: Context
    private var filteredClientList = clients

    class ClientViewHolder(clView: View) : RecyclerView.ViewHolder(clView) {
        val id: TextView = itemView.findViewById(R.id.personId)
        val name: TextView = itemView.findViewById(R.id.personName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false)
        return ClientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.id.text = filteredClientList[position].id.toString()
        holder.name.text = filteredClientList[position].name
    }

    override fun getItemCount(): Int {
        return filteredClientList.size
    }
}