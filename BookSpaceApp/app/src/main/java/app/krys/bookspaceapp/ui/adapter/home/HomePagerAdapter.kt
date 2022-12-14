package app.krys.bookspaceapp.ui.adapter.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.krys.bookspaceapp.ui.favourite.FavouritesFragment
import app.krys.bookspaceapp.ui.myspace.MySpaceFragment
import app.krys.bookspaceapp.ui.recent.RecentFragment

/**
 * [HomePagerAdapter] handles the UI related to the Home view
 * */

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val fragmentList: List<Fragment> = listOf(
        RecentFragment(),
        MySpaceFragment(),
        FavouritesFragment()
    )

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return if (position < fragmentList.size) {
            fragmentList[position]
        }else {
            fragmentList[0]
        }
    }
}