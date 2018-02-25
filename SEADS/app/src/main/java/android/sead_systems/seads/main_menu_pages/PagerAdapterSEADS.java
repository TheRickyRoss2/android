package android.sead_systems.seads.main_menu_pages;

import android.content.Context;
import android.sead_systems.seads.R;
import android.sead_systems.seads.main_menu.EnumNavBarNames;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PagerAdapterSEADS extends FragmentPagerAdapter {
    private int PAGE_COUNT;
    private String tabTitles[];// = new String[] { "Devices", "Rooms", "Overview" };
    private Context mContext;

    public PagerAdapterSEADS(FragmentManager fm, Context context) {
        super(fm);
        this.PAGE_COUNT = EnumNavBarNames.values().length;
        this.tabTitles = new String[this.PAGE_COUNT];
        int index = 0;
        for(EnumNavBarNames x : EnumNavBarNames.values()) {
            this.tabTitles[index] = x.getTitle();
            index++;
        }
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {

        PageFragmentConfig pageFragmentConfig = new PageFragmentConfig(position);
        return PageFragmentFactory.newInstance(pageFragmentConfig);
    }


    public View getTabView(int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tab_main, null);
        TextView textView = (TextView) view.findViewById(R.id.tab_title);
        textView.setText(tabTitles[position]);
        ImageView img = (ImageView) view.findViewById(R.id.tab_image);
        img.setImageResource(R.drawable.rounded_button);
        return view;
    }

}
