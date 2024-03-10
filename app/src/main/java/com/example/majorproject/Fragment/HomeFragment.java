package com.example.majorproject.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.majorproject.AddCommunity;
import com.example.majorproject.CurrencyConverter;
import com.example.majorproject.News;
import com.example.majorproject.R;
import com.google.android.material.navigation.NavigationView;

public class HomeFragment extends Fragment {

    DrawerLayout drawer;
    ImageView btnMenu;
    NavigationView navigationView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        drawer = view.findViewById(R.id.homeDrawerLayout);
        btnMenu = view.findViewById(R.id.btnHomeMenu);
        navigationView = view.findViewById(R.id.navigationHome);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.open();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int ItemId = item.getItemId();

                if (ItemId==R.id.drawerOptHome){
                   drawer.close();
                }
                if(ItemId== R.id.drawerOptCommunity){
                    startActivity(new Intent(getActivity(), AddCommunity.class));
                }
                if(ItemId== R.id.drawerOptNews){
                    startActivity(new Intent(getActivity(), News.class));
                }
                if(ItemId== R.id.drawerOptConverter){
                    startActivity(new Intent(getActivity(), CurrencyConverter.class));
                }
                return true;
            }
        });

        //Handel backpress method on this and onBackPress drawer is open then first close the drawer and second backpress fragment close
        backPressed(requireActivity());

        return  view;
    }

    private void backPressed(Activity context){
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(drawer.isOpen()){
                    drawer.close();
                }else {
                    setEnabled(false);
                    context.onBackPressed();
                }
            }
        });
    }

}