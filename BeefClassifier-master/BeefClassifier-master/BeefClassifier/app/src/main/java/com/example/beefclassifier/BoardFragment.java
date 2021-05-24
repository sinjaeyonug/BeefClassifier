package com.example.beefclassifier;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class BoardFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;
    private ImageButton WriteBtn;
    public BoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Board");
        Query query = ref.limitToFirst(100);
        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>() //어떤데이터를 어디서갖고올거며 어떠한 형태의 데이터클래스 결과를 반환할거냐 옵션을 정의한다.
                .setQuery(query, Post.class)
                .build();
        recyclerView=view.findViewById(R.id.RecylerView);
        adapter = new  FirebaseRecyclerAdapter<Post, PostViewHolder>(options){
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post model) {
                holder.titleView.setText(model.getTitle());
                holder.WriteDayView.setText(model.getDate());
                holder.WriterNameView.setText(model.getWriterName());
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent =new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra("postkey", postKey);
                        startActivity(intent);
                    }
                });

            }
            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item, parent, false);
                return new PostViewHolder(view);
            }

        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity()); //레이아웃매니저 생성
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager); ////만든 레이아웃매니저 객체를(설정을) 리사이클러 뷰에 설정해줌
       recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));


        WriteBtn = (ImageButton)view.findViewById(R.id.WriteButton);

        WriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView t=(TextView)getActivity().findViewById(R.id.UserName);
                Intent intent = new Intent(getActivity(),WriteActivity.class);
                intent.putExtra("Username",t.getText().toString());
                startActivity(intent);
            }
        });

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}