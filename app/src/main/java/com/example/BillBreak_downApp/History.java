package com.example.BillBreak_downApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class History extends Fragment {

    private SQLiteAdapter sqLiteAdapter;
    private ListView listView;
    private TextView tvNoData;
    private EditText etFilterDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history, container, false);

        sqLiteAdapter = new SQLiteAdapter(requireContext());
        listView = view.findViewById(R.id.list_view);
        tvNoData = view.findViewById(R.id.tv_na);
        Button btnRefresh = view.findViewById(R.id.btn_refresh);
        Button btnClear = view.findViewById(R.id.btn_clear);
        Button btnFilterDate = view.findViewById(R.id.btn_filter_date);
        etFilterDate = new EditText(requireContext());

        loadData();

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String name = item.get(SQLiteAdapter.KEY_CONTENT);
            String details = item.get(SQLiteAdapter.VALUE);
            showToast("Name: " + name + "\n" + details);
        });

        btnRefresh.setOnClickListener(v -> {
            loadData();
        });

        btnClear.setOnClickListener(v -> {
            clearDatabase();
        });

        btnFilterDate.setOnClickListener(v -> {
            showDatePickerDialog();
        });

        return view;
    }

    private void loadData() {
        sqLiteAdapter.openToRead();
        Cursor cursor = sqLiteAdapter.getAllResults();

        if (cursor.getCount() > 0) {
            showData(cursor);
        } else {
            showNoData();
        }

        sqLiteAdapter.close();
    }

    private void showData(Cursor cursor) {
        listView.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);

        List<Map<String, String>> data = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteAdapter.KEY_CONTENT));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteAdapter.DATE_TIME));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(SQLiteAdapter.VALUE));
                String formattedAmount = "RM " + new DecimalFormat("0.00").format(amount);
                String details = "Date: " + dateTime + "\nAmount: " + formattedAmount;

                Map<String, String> item = new HashMap<>();
                item.put(SQLiteAdapter.KEY_CONTENT, name);
                item.put(SQLiteAdapter.VALUE, details);
                data.add(item);
            } while (cursor.moveToNext());
        }

        String[] columns = {SQLiteAdapter.KEY_CONTENT, SQLiteAdapter.VALUE};
        int[] display = {R.id.tv_name, R.id.tv_amount};
        SimpleAdapter simpleAdapter = new SimpleAdapter(requireContext(), data, R.layout.list_item, columns, display);
        listView.setAdapter(simpleAdapter);
    }

    private void showNoData() {
        listView.setVisibility(View.GONE);
        tvNoData.setVisibility(View.VISIBLE);
    }

    private void clearDatabase() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear Database")
                .setMessage("Are you sure you want to clear the database?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    sqLiteAdapter.openToWrite();
                    sqLiteAdapter.clearData();
                    sqLiteAdapter.close();
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDatePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Date");

        final EditText etFilterDate = new EditText(requireContext());
        etFilterDate.setHint("dd/MM/yy  eg.(05/08/23)");
        builder.setView(etFilterDate);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedDate = etFilterDate.getText().toString();
                if (isValidDate(selectedDate)) {
                    filterByDate(selectedDate);
                } else {
                    Toast.makeText(requireContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void filterByDate(String selectedDate) {
        sqLiteAdapter.openToRead();
        Cursor cursor = sqLiteAdapter.getResultsByDate(selectedDate);

        if (cursor.getCount() > 0) {
            showData(cursor);
        } else {
            showNoData();
        }

        sqLiteAdapter.close();
    }


    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}