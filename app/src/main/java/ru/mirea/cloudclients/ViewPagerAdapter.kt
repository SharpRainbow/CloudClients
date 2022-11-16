package ru.mirea.cloudclients

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.mirea.cloudclients.fragments.EmployeesFragment
import ru.mirea.cloudclients.fragments.TasksFragment

class ViewPagerAdapter(activity: AppCompatActivity, private val names: List<String>)
    : FragmentStateAdapter(activity) {

    private val fragments = mutableListOf<Fragment>()

    init {
        fragments.add(TasksFragment())
        fragments.add(EmployeesFragment())
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getName(position: Int): String {
        return names[position]
    }
}