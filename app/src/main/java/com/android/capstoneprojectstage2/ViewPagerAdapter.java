package com.android.capstoneprojectstage2;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by trikh on 12-04-2017.
 */

class ViewPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    ViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return new MainFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.string_today);

            case 1:
                return mContext.getString(R.string.string_week);

            case 2:
                return mContext.getString(R.string.string_month);

            default:
                return null;
        }
    }
}
