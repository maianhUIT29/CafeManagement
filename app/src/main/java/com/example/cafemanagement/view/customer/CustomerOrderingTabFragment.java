package com.example.cafemanagement.view.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cafemanagement.R;

public class CustomerOrderingTabFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ordering_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lần đầu load → hiện màn hình chọn bàn
        if (savedInstanceState == null) {
            showSetup();
        }
    }

    /** Hiện màn hình chọn bàn/mang về */
    public void showSetup() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.orderingContainer, new CustomerOrderSetupFragment())
                .commit();
    }

    /** Sau khi chọn xong bàn → hiện menu */
    public void showMenu() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.orderingContainer, new CustomerMenuFragment())
                .addToBackStack("menu")
                .commit();
    }

    /** Từ menu → giỏ hàng */
    public void showBasket() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.orderingContainer, new CustomerBasketFragment())
                .addToBackStack("basket")
                .commit();
    }
}