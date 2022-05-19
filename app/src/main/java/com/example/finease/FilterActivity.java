package com.example.finease;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FilterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private RecyclerView recyclerView;

    private DatabaseReference expenseRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    private Button fil;

    private String post_key = "";
    private String item = "";
    private String mode = "";
    private int amount = 0;

    Spinner filterSpinner, valueSpinner;
    private EditText amountfield;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        filterSpinner = (Spinner) findViewById(R.id.filterspinner);
        valueSpinner = (Spinner) findViewById(R.id.filtervaluespinner);
        filterSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.filter, R.layout.spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        expenseRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        expenseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Data data = snap.getValue(Data.class);
                    totalAmount += data.getAmount();
//                    String sTotal = String.valueOf("Month budget: $" + totalAmount);
//                    totalBudgetAmountTextView.setText(sTotal);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fil = findViewById(R.id.filtersubmit);

        fil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterItem();
            }
        });

    }

//    @Override
    public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
                               long arg2) {
        parent.getItemAtPosition(pos);
        if (pos == 0) {
            valueSpinner.setVisibility(View.GONE);
        }
        if (pos == 1) {
            valueSpinner.setVisibility(View.VISIBLE);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(this, R.array.mode,
                            R.layout.spinner_dropdown_item);
            valueSpinner.setAdapter(adapter);
        } else if (pos == 2) {
            valueSpinner.setVisibility(View.VISIBLE);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(this, R.array.items,
                            R.layout.spinner_dropdown_item);
            valueSpinner.setAdapter(adapter);
        } else if (pos == 3) {
            valueSpinner.setVisibility(View.VISIBLE);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(this, R.array.month,
                            R.layout.spinner_dropdown_item);
            valueSpinner.setAdapter(adapter);
        } else if (pos == 4) {
            amountfield = findViewById(R.id.amount);
            amountfield.setVisibility(View.VISIBLE);
            valueSpinner.setVisibility(View.GONE);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter
                    .createFromResource(this, R.array.items,
                            R.layout.spinner_dropdown_item);
            valueSpinner.setAdapter(adapter);
        }
    }

//    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }


    @Override
    protected void onStart() {
        super.onStart();
        DateTime now = new DateTime();

        Query query = expenseRef.orderByChild("month").equalTo(now.getMonthOfYear());
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(query, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull final Data model) {

                holder.setItemAmount("Amount: €"+ model.getAmount());
                holder.setDate("On: "+model.getDate());
                holder.setItemName("Item: "+model.getItem());
                holder.setModeName("Payment Mode: "+model.getMode());
                holder.setNotes("Notes: "+model.getNotes());

                holder.notes.setVisibility(View.VISIBLE);

                switch (model.getItem()){
                    case "Transport":
                        holder.imageView.setImageResource(R.drawable.ic_transport);
                        break;
                    case "Food":
                        holder.imageView.setImageResource(R.drawable.ic_food);
                        break;
                    case "House":
                        holder.imageView.setImageResource(R.drawable.ic_house);
                        break;
                    case "Entertainment":
                        holder.imageView.setImageResource(R.drawable.ic_entertainment);
                        break;
                    case "Education":
                        holder.imageView.setImageResource(R.drawable.ic_education);
                        break;
                    case "Charity":
                        holder.imageView.setImageResource(R.drawable.ic_consultancy);
                        break;
                    case "Apparel":
                        holder.imageView.setImageResource(R.drawable.ic_shirt);
                        break;
                    case "Health":
                        holder.imageView.setImageResource(R.drawable.ic_health);
                        break;
                    case "Personal":
                        holder.imageView.setImageResource(R.drawable.ic_personalcare);
                        break;
                    case "Other":
                        holder.imageView.setImageResource(R.drawable.ic_other);
                        break;
                }

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(position).getKey();
                        item = model.getItem();
                        mode = model.getMode();
                        amount = model.getAmount();
                    }
                });


            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ImageView imageView;
        public TextView notes, date;
        public Button dates;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            notes = itemView.findViewById(R.id.note);
            date = itemView.findViewById(R.id.date);
        }

        public  void setItemName (String itemName){
            TextView item = mView.findViewById(R.id.item);
            item.setText(itemName);
        }

        public  void setModeName (String modeName){
            TextView mode = mView.findViewById(R.id.mode);
            mode.setVisibility(View.VISIBLE);
            mode.setText(modeName);
        }

        public void setItemAmount(String itemAmount){
            TextView amount = mView.findViewById(R.id. amount);
            amount.setText(itemAmount);
        }

        public void setDate(String itemDate){
            TextView date = mView.findViewById(R.id.date);
            date.setText(itemDate);
        }

        public void setNotes(String notes){
            TextView note = mView.findViewById(R.id.note);
            note.setText(notes);
        }
    }

    private void filterItem() {
        final Button filter = findViewById(R.id.filtersubmit);

        filter.setOnClickListener(view -> {
            int fil1, pos=0;
            String fil2;
            String fil11="";
            String fil22="";
            Query query;

            fil1 = filterSpinner.getSelectedItemPosition();
            if (fil1 == 1){
                fil11 = "mode";
            }
            else if (fil1 == 2){
                fil11 = "item";
            }
            else if (fil1 == 3){
                fil11 = "month";
            }
            else if (fil1 == 4){
                fil11 = "amount";
            }

            fil2 = valueSpinner.getSelectedItem().toString();
            fil22 = fil2;
            if (fil11 == "month"){
                pos = valueSpinner.getSelectedItemPosition();
            }
            else if (fil11 == "amount"){
                pos = Integer.valueOf(amountfield.getText().toString());
            }
            Log.d("fil1", fil11);
            Log.d("fil2", fil22);
            if (fil11 == "month" || fil11 == "amount") {
                query = expenseRef.orderByChild(fil11).equalTo(pos);
            }
            else {
                query = expenseRef.orderByChild(fil11).equalTo(fil22);
            }
            FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                    .setQuery(query, Data.class)
                    .build();

            FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull final Data model) {

                    holder.setItemAmount("Amount: €" + model.getAmount());
                    holder.setDate("On: " + model.getDate());
                    holder.setItemName("Item: " + model.getItem());
                    holder.setModeName("Payment Mode: " + model.getMode());
                    holder.setNotes("Notes: " + model.getNotes());

                    holder.notes.setVisibility(View.VISIBLE);

                    switch (model.getItem()) {
                        case "Transport":
                            holder.imageView.setImageResource(R.drawable.ic_transport);
                            break;
                        case "Food":
                            holder.imageView.setImageResource(R.drawable.ic_food);
                            break;
                        case "House":
                            holder.imageView.setImageResource(R.drawable.ic_house);
                            break;
                        case "Entertainment":
                            holder.imageView.setImageResource(R.drawable.ic_entertainment);
                            break;
                        case "Education":
                            holder.imageView.setImageResource(R.drawable.ic_education);
                            break;
                        case "Charity":
                            holder.imageView.setImageResource(R.drawable.ic_consultancy);
                            break;
                        case "Apparel":
                            holder.imageView.setImageResource(R.drawable.ic_shirt);
                            break;
                        case "Health":
                            holder.imageView.setImageResource(R.drawable.ic_health);
                            break;
                        case "Personal":
                            holder.imageView.setImageResource(R.drawable.ic_personalcare);
                            break;
                        case "Other":
                            holder.imageView.setImageResource(R.drawable.ic_other);
                            break;
                    }

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            post_key = getRef(position).getKey();
                            item = model.getItem();
                            mode = model.getMode();
                            amount = model.getAmount();
                        }
                    });


                }

                @NonNull
                @Override
                public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_layout, parent, false);
                    return new MyViewHolder(view);
                }
            };

            recyclerView.setAdapter(adapter);
            adapter.startListening();
            adapter.notifyDataSetChanged();
        });
    }
}