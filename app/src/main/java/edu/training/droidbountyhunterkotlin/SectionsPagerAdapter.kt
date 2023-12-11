package edu.training.droidbountyhunterkotlin

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private var fragments: ArrayList<Fragment> = ArrayList()

    override fun getItem(position: Int): Fragment {
        if (fragments.size < 3){ // Si no contiene los 3 fragments los agregará
            if (position < 2){
                fragments.add(position, ListFragment())
                val arguments = Bundle()
                arguments.putInt(SECTION_NUMBER, position)
                fragments[position].arguments = arguments
            }else{
                fragments.add(position, AcercaDeFragment())
            }
        }
        return fragments[position]
    }


    override fun getPageTitle(position: Int) = when (position) {
        0 -> context.getString(R.string.titulo_fugitivos).toUpperCase()
        1 -> context.getString(R.string.titulo_capturados).toUpperCase()
        else -> context.getString(R.string.titulo_acerca_de).toUpperCase()
    }

    override fun getCount(): Int {
        // Show 3 total pages.
        return 3
    }
}