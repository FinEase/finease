package com.example.finease;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataItems {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableDetailList = new HashMap<String, List<String>>();

        // As we are populating List of fruits, vegetables and nuts, using them here
        // We can modify them as per our choice.
        // And also choice of fruits/vegetables/nuts can be changed
        List<String> q1 = new ArrayList<String>();
        q1.add("In the Add Spending tab, tap the \"+\" button to open up the page to record your income. In open tab consist of \n" +
                "Select Mode of Payment\n" +
                "Select Item\n" +
                "Select Date\n" +
                "Select Amount\n" +
                "Add Note\n" +
                "\n" +
                "By giving all these values you can successfully record an expense.");

        List<String> q2 = new ArrayList<String>();
        q2.add("Click on the ViewExpense tab, you will be able to see all your history. " +
                "Here you also have an option to filter out the values based on Mode of Payment, Month and Category.");

        List<String> q3 = new ArrayList<String>();
        q3.add("Yes, it is possible to add expenses in different currencies. " +
                "For this, in the AddExpense tab while recording an Expense choose the currencies accordingly.");

        List<String> q4 = new ArrayList<String>();
        q4.add("Yes, the FinEase application provides a graphical representation of your expenses. " +
                "For this click on the Analytics tab and here you can see a pie chart analysis of the monthly spending.");

        List<String> q5 = new ArrayList<String>();
        q5.add("Yes, a user can add an expense as a recurring record here the record will be added for the following month. " +
                "For this in the Add Spending tab click on \"+\" and the recurring transaction checkbox.");

        // Fruits are grouped under Fruits Items. Similarly the rest two are under
        // Vegetable Items and Nuts Items respecitively.
        // i.e. expandableDetailList object is used to map the group header strings to
        // their respective children using an ArrayList of Strings.
        expandableDetailList.put("How to add an expense?", q1);
        expandableDetailList.put("How to review your expense?", q2);
        expandableDetailList.put("Can I add expenses in different currencies?", q3);
        expandableDetailList.put("Is it possible to have a graphical analysis of your expenses?", q4);
        expandableDetailList.put("Is it possible to add a recurring expense record?", q5);
        return expandableDetailList;
    }
}
