package com.example.BillBreak_downApp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Fragment {

    private BillBreakdownApp app;
    private EditText etName, etAmount;
    private TextView tvResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        app = new BillBreakdownApp(requireContext());

        etName = view.findViewById(R.id.et_name);
        etAmount = view.findViewById(R.id.et_amount);
        tvResult = view.findViewById(R.id.tv_result);

        Button btnAddUser = view.findViewById(R.id.btn_add_user);
        Button btnClearData = view.findViewById(R.id.btn_clear_data);
        Button btnEqualBreakdown = view.findViewById(R.id.btnEqualBreakdown);
        Button btnCustomBreakdown = view.findViewById(R.id.btnCustomBreakdown);

        btnAddUser.setOnClickListener(v -> {
            addUser();
        });

        btnClearData.setOnClickListener(v -> {
            clearUserData();
        });

        btnEqualBreakdown.setOnClickListener(v -> {
            showEqualBreakdown();
        });

        btnCustomBreakdown.setOnClickListener(v -> {
            showCustomBreakdownMenu(v);
        });

        return view;
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void clearInputs() {
        etName.setText("");
        etAmount.setText("");
    }

    private void addUser() {
        String name = etName.getText().toString().trim();
        String amountText = etAmount.getText().toString().trim();
        double amount = 0.0;

        if (name.isEmpty()) {
            showToast("Please enter a name!");
            return;
        }

        if (app.isDuplicateName(name)) {
            showToast("The name already exists!");
            return;
        }

        if (!amountText.isEmpty()) {
            if (!amountText.matches("^\\d+(\\.\\d{1,2})?$")) {
                etAmount.setError("Invalid format!");
                return;
            }

            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount!");
                return;
            }
        }

        app.addUser(name, amount);
        showToast("User added successfully!");
        clearInputs();
    }

    private void clearUserData() {
        app.clearUserData();
        showToast("User data is cleared successfully!");
        clearInputs();
        tvResult.setText("");
    }

    private void showEqualBreakdown() {
        Map<String, Double> equalBreakdown = app.getEqualBreakdown();

        if (equalBreakdown.isEmpty()) {
            showToast("No users added!");
            return;
        }

        displayBreakdown(equalBreakdown);
    }

    private void showCustomBreakdownMenu(View anchorView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Custom Breakdown Option");

        String[] options = {"By Percentage", "By Ratio", "By Amount"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showPercentageInputDialog();
                        break;
                    case 1:
                        showRatioInputDialog();
                        break;
                    case 2:
                        showAmountInputDialog();
                        break;
                }
            }
        });

        builder.show();
    }

    private void showPercentageInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Percentage Breakdown");

        final LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        for (String name : app.getUsersData().keySet()) {
            TextView tv = new TextView(requireContext());
            tv.setText(name);
            layout.addView(tv);

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(input);
        }

        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Double> percentages = new ArrayList<>();
                double totalPercentage = 0.0;
                int childCount = layout.getChildCount();

                for (int i = 0; i < childCount; i += 2) {
                    EditText input = (EditText) layout.getChildAt(i + 1);
                    String percentageText = input.getText().toString().trim();
                    if (!percentageText.isEmpty()) {
                        try {
                            double percentage = Double.parseDouble(percentageText);
                            percentages.add(percentage);
                            totalPercentage += percentage;
                        } catch (NumberFormatException e) {
                            showToast("Invalid percentage value! Please enter valid numbers.");
                            return;
                        }
                    }
                }

                if (Math.abs(totalPercentage - 100.0) < 0.01) {
                    Map<String, Double> percentagesMap = new HashMap<>();

                    for (int i = 0; i < childCount; i += 2) {
                        TextView tv = (TextView) layout.getChildAt(i);
                        EditText input = (EditText) layout.getChildAt(i + 1);
                        String name = tv.getText().toString();
                        String percentageText = input.getText().toString().trim();

                        if (!percentageText.isEmpty()) {
                            try {
                                double percentage = Double.parseDouble(percentageText);
                                percentagesMap.put(name, percentage);
                            } catch (NumberFormatException e) {
                                showToast("Invalid percentage value! Please enter valid numbers.");
                                return;
                            }
                        } else {
                            showToast("Please enter percentage for all users.");
                            return;
                        }
                    }

                    app.setCustomPercentageBreakdown(percentagesMap);
                    Map<String, Double> customBreakdown = app.getCustomBreakdown();
                    displayBreakdown(customBreakdown);
                } else {
                    showToast("Total percentage should be 100.");
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showRatioInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Ratio Breakdown");

        final LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        for (String name : app.getUsersData().keySet()) {
            TextView tv = new TextView(requireContext());
            tv.setText(name);
            layout.addView(tv);

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setTag(name);
            layout.addView(input);
        }

        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map<String, Integer> ratios = new HashMap<>();
                int totalRatio = 0;
                for (int i = 0; i < layout.getChildCount(); i += 2) {
                    EditText input = (EditText) layout.getChildAt(i + 1);
                    String ratioText = input.getText().toString().trim();
                    if (!ratioText.isEmpty()) {
                        try {
                            int ratio = Integer.parseInt(ratioText);
                            String userName = (String) input.getTag();
                            ratios.put(userName, ratio);
                            totalRatio += ratio;
                        } catch (NumberFormatException e) {
                            showToast("Invalid ratio value! Please enter valid whole numbers.");
                            return;
                        }
                    } else {
                        showToast("Please enter ratio for all users.");
                        return;
                    }
                }

                if (totalRatio == 0) {
                    showToast("Total ratio cannot be 0.");
                    return;
                }

                app.setCustomRatioBreakdown(ratios);
                Map<String, Double> customBreakdown = app.getCustomBreakdown();
                displayBreakdown(customBreakdown);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showAmountInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Amount Breakdown");

        final LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        for (String name : app.getUsersData().keySet()) {
            TextView tv = new TextView(requireContext());
            tv.setText(name);
            layout.addView(tv);

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(input);
        }

        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Double> amounts = new ArrayList<>();
                double totalAmount = 0.0;

                for (int i = 0; i < layout.getChildCount(); i += 2) {
                    EditText input = (EditText) layout.getChildAt(i + 1);
                    String amountText = input.getText().toString().trim();
                    if (!amountText.isEmpty()) {
                        try {
                            double amount = Double.parseDouble(amountText);
                            amounts.add(amount);
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            showToast("Invalid amount value! Please enter valid numbers.");
                            return;
                        }
                    } else {
                        showToast("Please enter amount for all users.");
                        return;
                    }
                }

                if (Math.abs(totalAmount - app.getTotalBill()) < 0.01) {
                    app.setCustomAmountBreakdown(amounts);
                    Map<String, Double> customBreakdown = app.getCustomBreakdown();
                    displayBreakdown(customBreakdown);
                } else {
                    showToast("The sum of individual amounts should be equal to the total bill amount entered.");
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void displayBreakdown(Map<String, Double> breakdown) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        StringBuilder stringBuilder = new StringBuilder();

        if (breakdown.isEmpty()) {
            showToast("No data available!");
            tvResult.setText("");
            return;
        }

        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            String name = entry.getKey();
            double amount = entry.getValue();
            String formattedAmount = decimalFormat.format(amount);
            stringBuilder.append(name).append(": RM").append(formattedAmount).append("\n");
        }

        tvResult.setText(stringBuilder.toString());

        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            String name = entry.getKey();
            double amount = entry.getValue();
            app.saveResult(name, amount);
        }
    }
}
