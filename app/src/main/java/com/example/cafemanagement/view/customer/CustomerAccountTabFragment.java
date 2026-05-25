package com.example.cafemanagement.view.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.cafemanagement.R;

public class CustomerAccountTabFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout  tabLayout  = view.findViewById(R.id.tabAccount);
        ViewPager2 viewPager  = view.findViewById(R.id.pagerAccount);

        viewPager.setAdapter(new AccountPagerAdapter(requireActivity()));
        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) -> {
            tab.setText(pos == 0 ? "Điểm thưởng" : "Hồ sơ");
        }).attach();
    }

    static class AccountPagerAdapter extends FragmentStateAdapter {
        AccountPagerAdapter(FragmentActivity fa) { super(fa); }
        @Override public int getItemCount() { return 2; }
        @NonNull @Override
        public Fragment createFragment(int pos) {
            return pos == 0 ? new CustomerPointsFragment()
                    : new CustomerProfileFragment();
        }
    }
}