package com.technohack.todo_app_firestore;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.technohack.todo_app_firestore.Adapter.TodoAdapter;
import com.technohack.todo_app_firestore.Adapter.TodoModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;

    public MaterialEditText titleText,descriptionText;

    private RecyclerView recyclerView;

    private FloatingActionButton floatingActionButton;
    public boolean isUpdate=false;
    private List<TodoModel> todoModelList;
    TodoAdapter todoAdapter;

    public String idUpdate="";

    //AlertDialog dialog;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

          mFirestore=FirebaseFirestore.getInstance();

          titleText=findViewById(R.id.main_titleId);
          descriptionText=findViewById(R.id.main_descriptionId);

          todoModelList=new ArrayList<>();

        // dialog=new SpotsDialog(getBaseContext());

        floatingActionButton=findViewById(R.id.main_flotingBtnId);

        //to loading the value
        progressDialog= new ProgressDialog(MainActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        //for adding the todo into the firestore
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //isUpdate is initialized with false
                //when we are only saving the data
                if(!isUpdate){

                    saveDataIntoFirebase(titleText.getText().toString(),descriptionText.getText().toString());

                }
                //when we click in any item and updating the value
                //we use floating button for saving and updating the data
                else{
                    //if data is not updated
                    updateData(titleText.getText().toString(),descriptionText.getText().toString());
                    //reset the flag
                    isUpdate=!isUpdate;

                }
            }
        });


        recyclerView=findViewById(R.id.main_recyclerviewId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        //load data in recyclerView

        loadDataFromFirestore();

    }

    //for deleting the any item we are using context menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        //if any item is clicked then we need to get the index of that item
        if(item.getTitle().equals("DELETE"))
            deleteItem(item.getOrder());

        //item.getOrder();  will return the index value of the list item
        
        return super.onContextItemSelected(item);

    }

    private void deleteItem(int index) {

        mFirestore.collection("TodoList")
                .document(todoModelList.get(index).getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //it will store all the data except deleted data
                        loadDataFromFirestore();

                    }
                });

    }

    //for updating the data the
    private void updateData(String title, String description) {

        mFirestore.collection("TodoList")
                .document(idUpdate)
                .update("title",title,"description",description)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                         Toast.makeText(MainActivity.this,"Updated!!!",Toast.LENGTH_LONG).show();

                    }
                });


        //Realtime update data
        mFirestore.collection("TodoList")
                .document(idUpdate)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        loadDataFromFirestore();

                    }
                });


    }

    //for storing the into the firestore
    private void saveDataIntoFirebase(String title, String description) {


        progressDialog.show();

        String id=UUID.randomUUID().toString();

        Map<String,Object> todoMap=new HashMap<>();

        todoMap.put("id",id);
        todoMap.put("title",title);
        todoMap.put("description",description);


        //store the data into the firebase firestore
        mFirestore.collection("TodoList")
                .document(id)
                .set(todoMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        progressDialog.dismiss();
                        //Refresh data
                        //when data added into the firebase we need to update the recyclerview and display all the value
                        loadDataFromFirestore();
                    }

                });

    }

    private void loadDataFromFirestore() {

        //if data already present in list then clear the data
        if(todoModelList.size()>0)
                 todoModelList.clear();

        mFirestore.collection("TodoList")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //fetching the data from the firestore
                        for(DocumentSnapshot doc:task.getResult()){

                             TodoModel todoModel=new TodoModel(doc.getString("id"),
                                     doc.getString("title"),doc.getString("description"));

                             todoModelList.add(todoModel);

                        }

                        todoAdapter=new TodoAdapter(MainActivity.this,todoModelList);

                        recyclerView.setAdapter(todoAdapter);

                        progressDialog.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

                        progressDialog.dismiss();

                    }
                });

    }

}
