package com.example.iot_asma_support;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import eu.long1.spacetablayout.SpaceTabLayout;
import com.github.barteksc.pdfviewer.PDFView;


public class MainActivity extends AppCompatActivity {

    private PDFView pdfView;
    DatabaseReference myRef;
    DatabaseReference userRef;

    List<History> histories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfView.fromAsset("Sample_1.pdf")
                .enableSwipe(true)
                .load();


        FirebaseAuth auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        ExtendedFloatingActionButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                return;
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Account").child(auth.getCurrentUser().getUid());

        List<Fragment> fragmentList = new ArrayList<>();

        myRef.child("Support").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Support support = snapshot.getValue(Support.class);

                fragmentList.add(new HomeFragment(support.getUsername()));

                fragmentList.add(new HelpFragment());

                userRef = database.getReference("Account").child(support.getUid());

                userRef.child("History").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(int i = 0; i < snapshot.getChildrenCount(); i++){
                            History history = snapshot.child(String.valueOf(i)).getValue(History.class);
                            histories.add(history);
                        }

                        fragmentList.add(new HistoryFragment(getApplicationContext(), histories));
                        fragmentList.add(new HistoryFragment(getApplicationContext(), histories));

                        ViewPager viewPager = findViewById(R.id.viewPager);
                        SpaceTabLayout tabLayout = findViewById(R.id.spaceTabLayout);
                        tabLayout.initialize(viewPager, getSupportFragmentManager(), fragmentList, null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}