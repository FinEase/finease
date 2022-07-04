package com.example.finease;

import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ExpenseActivity extends AppCompatActivity {

    private TextView totalBudgetAmountTextView;
    private RecyclerView recyclerView;

    private FloatingActionButton fab;

    private DatabaseReference expenseRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    private String post_key = "";
    private String item = "";
    private String mode = "";
    private int amount = 0;

    private DatePickerDialog datePickerDialog;
    private Button dateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

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
//                    String sTotal = String.valueOf("Month Expense: $" + totalAmount);
//                    totalBudgetAmountTextView.setText(sTotal);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                additem();
            }
        });

    }

    private void additem() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = myView.findViewById(R.id.itemspinner);
        final Spinner pmodeSpinner = myView.findViewById(R.id.pmodespinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final EditText note = myView.findViewById(R.id.note);
        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);
        final Button date = myView.findViewById(R.id.datePickerButton);

        final CheckBox checkBox = myView.findViewById(R.id.CheckBox);
        final Spinner recurspinner = myView.findViewById(R.id.recurspinner);
        final Spinner currencyspinner = myView.findViewById(R.id.currencySpinner);

        initDatePicker();
        dateButton = myView.findViewById(R.id.datePickerButton);
        dateButton.setText(getTodaysDate());

        ArrayAdapter adapter_items = ArrayAdapter.createFromResource(this,
                R.array.items, R.layout.spinner_item);
        ArrayAdapter adapter_recur = ArrayAdapter.createFromResource(this,
                R.array.recurring_frequency, R.layout.spinner_item);

        adapter_items.setDropDownViewResource(R.layout.spinner_dropdown_item);
        adapter_recur.setDropDownViewResource(R.layout.spinner_dropdown_item);
        itemSpinner.setAdapter(adapter_items);
        recurspinner.setAdapter(adapter_recur);


        ArrayAdapter adapter_pmode = ArrayAdapter.createFromResource(this,
                R.array.mode, R.layout.spinner_item);

        adapter_pmode.setDropDownViewResource(R.layout.spinner_dropdown_item);
        pmodeSpinner.setAdapter(adapter_pmode);

        pmodeSpinner.setVisibility(View.VISIBLE);
        note.setVisibility(View.VISIBLE);
        date.setVisibility(View.VISIBLE);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked()) {
                    recurspinner.setVisibility(View.VISIBLE);
                }
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String expenseAmount = amount.getText().toString();
                String expenseItem = itemSpinner.getSelectedItem().toString();
                String expenseMode = pmodeSpinner.getSelectedItem().toString();
                String notes = note.getText().toString();
                String dt = dateButton.getText().toString();
                int curr_int = currencyspinner.getSelectedItemPosition();
                int frequency = recurspinner.getSelectedItemPosition();

                String curr = "€";

                if (curr_int == 0) {
                    curr = "€";
                }
                if (curr_int == 1) {
                    curr = "£";
                }
                if (curr_int == 2) {
                    curr = "$";
                }
                if (curr_int == 3) {
                    curr = "₹";
                }

                if (TextUtils.isEmpty(expenseAmount)){
                    amount.setError("Amount is required!");
                    return;
                }

                if (expenseMode.equals("Mode of Payment")){
                    Toast.makeText(ExpenseActivity.this, "Select a valid payment mode", Toast.LENGTH_SHORT).show();
                }

                if (expenseItem.equals("Select item")){
                    Toast.makeText(ExpenseActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                }

                else {
                    loader.setMessage("adding the expense item");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String date_old = dt;
                    String date = null;
                    Date dt_1 = null;
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    SimpleDateFormat dt_format = new SimpleDateFormat("dd-MM-yyyy");
                    try {
                        dt_1 = dt_format.parse(date_old);
                        date = dt_format.format(dt_1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dt_1);
                    int month = cal.get(Calendar.MONTH) + 1;
                    String itemNday = expenseItem + date;
//                    String itemNweek = expenseItem+weeks.getWeeks();
                    String itemNweek = null;
                    int week = 0;
                    String itemNmonth = expenseItem + month;

                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(cal.getTime());
                    Calendar lastDayOfCurrentYear = Calendar.getInstance();
                    lastDayOfCurrentYear.set(Calendar.DATE, 31);
                    lastDayOfCurrentYear.set(Calendar.MONTH, 11);

                    Calendar lastDayOfCurrentMonth = Calendar.getInstance();
                    lastDayOfCurrentMonth.add(Calendar.MONTH, 1);
                    lastDayOfCurrentMonth.set(Calendar.DATE, 1);
                    lastDayOfCurrentMonth.add(Calendar.DATE, -1);
                    ArrayList<Date> dates = new ArrayList<Date>();
                    if (checkBox.isChecked()) {
                        if (frequency == 1) {
                            while(!cal1.after(lastDayOfCurrentMonth))
                            {
                                dates.add(cal1.getTime());
                                cal1.add(Calendar.WEEK_OF_MONTH, 1);
                            }
                        }
                        else if (frequency == 2) {
                            while(!cal1.after(lastDayOfCurrentYear))
                            {
                                dates.add(cal1.getTime());
                                cal1.add(Calendar.MONTH, 1);
                            }
                        }
                    };
                    if (checkBox.isChecked()) {
                        for (int i = 0; i < dates.size(); i++) {
                            String id = expenseRef.push().getKey();
                            month = dates.get(i).getMonth() + 1;
                            Data data = new Data(expenseItem, expenseMode, dateFormat.format(dates.get(i)), id, itemNday, itemNweek, itemNmonth, Integer.parseInt(expenseAmount), week, month, curr, notes);
                            expenseRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ExpenseActivity.this, "Expense item added successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ExpenseActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }

                                    loader.dismiss();
                                }
                            });
                        }
                    }
                    else {
                        String id = expenseRef.push().getKey();
                        Data data = new Data(expenseItem, expenseMode, date, id, itemNday, itemNweek, itemNmonth, Integer.parseInt(expenseAmount), week, month, curr, notes);
                        expenseRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ExpenseActivity.this, "Expense item added successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ExpenseActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }

                                loader.dismiss();
                            }
                        });
                    }
                    dialog.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        dialog.show();
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

                String curr = model.getCurr();
                holder.setItemAmount(curr + model.getAmount());
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
                        updateData();
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
    private void updateData() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.update_layout, null);

        myDialog.setView(mView);
        final AlertDialog dialog = myDialog.create();

        final TextView mItem = mView.findViewById(R.id.itemName);
        final EditText mAmount = mView.findViewById(R.id.amount);
        final EditText mNotes = mView.findViewById(R.id.note);
        final Spinner mCurr = mView.findViewById(R.id.currencySpinner);

        mNotes.setVisibility(View.VISIBLE);

        mItem.setText(item);

        mAmount.setText(String.valueOf(amount));
        mAmount.setSelection(String.valueOf(amount).length());

        Button delBut = mView.findViewById(R.id.btnDelete);
        Button btnUpdate = mView.findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                amount = Integer.parseInt(mAmount.getText().toString());
                String notes = mNotes.getText().toString();
                int curr_int = mCurr.getSelectedItemPosition();


//                MutableDateTime epoch = new MutableDateTime();
//                epoch.setDate(0);
//                DateTime now = new DateTime();
//                Weeks weeks = Weeks.weeksBetween(epoch, now);
//                Months months = Months.monthsBetween(epoch, now);

                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date date = new Date();
                String dt_1 = dateFormat.format(date);

                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int month = cal.get(Calendar.MONTH) + 1;

//                String itemNday = expenseItem+date;
//                String itemNweek = expenseItem+weeks.getWeeks();
                String itemNweek = null;
                int week = 0;
//                String itemNmonth = expenseItem+month;

                String itemNday = item + date;
//                String itemNweek = item + week;
                String itemNmonth = item + month;

                String curr = "€";

                if (curr_int == 0) {
                    curr = "€";
                }
                if (curr_int == 1) {
                    curr = "£";
                }
                if (curr_int == 2) {
                    curr = "$";
                }
                if (curr_int == 3) {
                    curr = "₹";
                }


                Data data = new Data(item, mode, dt_1, post_key, itemNday, itemNweek, itemNmonth, amount, week, month, curr, notes);
                expenseRef.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ExpenseActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ExpenseActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();

            }
        });

        delBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expenseRef.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ExpenseActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ExpenseActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private String getTodaysDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return day + "-" + month + "-" + year;
    }

    private void initDatePicker()
    {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                month = month + 1;
                String date = day + "-" + month + "-" + year;
                dateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

    }

    public void openDatePicker(View view)
    {
        datePickerDialog.show();
    }
}