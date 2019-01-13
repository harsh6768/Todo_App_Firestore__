package com.technohack.todo_app_firestore.Adapter;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FieldValue;
import com.technohack.todo_app_firestore.MainActivity;
import com.technohack.todo_app_firestore.R;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.MyViewHolder> {

    MainActivity mainActivity;

    List<TodoModel> todoModelList;

    public TodoAdapter(MainActivity mainActivity, List<TodoModel> todoModelList) {
        this.mainActivity = mainActivity;
        this.todoModelList = todoModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view=LayoutInflater.from(mainActivity.getBaseContext()).inflate(R.layout.list_item,parent,false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int position) {

        myViewHolder.title.setText(todoModelList.get(position).getTitle());
        myViewHolder.description.setText(todoModelList.get(position).getDescription());


        //when we click any item ,then the value of that todo will save into the editext so that we can update the value
        myViewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, int position, boolean onLongClick) {

             //setting the data into the edit text to update the data
                mainActivity.titleText.setText(todoModelList.get(position).getTitle());
                mainActivity.descriptionText.setText(todoModelList.get(position).getDescription());

                //setting the flag to true because we are going to update the data
                mainActivity.isUpdate=true;
                mainActivity.idUpdate=todoModelList.get(position).getId();

            }
        });

    }

    @Override
    public int getItemCount() {
        return todoModelList.size();
    }

    //
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener {

        ItemClickListener itemClickListener;

        TextView title,description;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);


            title=itemView.findViewById(R.id.item_titleId);
            description=itemView.findViewById(R.id.item_descId);


        }

        public void setItemClickListener(ItemClickListener itemClickListener){
            this.itemClickListener=itemClickListener;
        }


        @Override
        public void onClick(View view) {
             itemClickListener.onItemClick(view, getAdapterPosition() ,false);
        }

        //for creating the context menu to delete the todo item
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

            contextMenu.setHeaderTitle("Select The Action");

            contextMenu.add(0,0,getAdapterPosition(),"DELETE");

        }

    }
}
