package com.example.mhike;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeFragment extends Fragment {

    private Button createHikePlanButton, clearHikePlanButton;
    private RecyclerView recyclerView;
    private DatabaseHelper databaseHelper;
    private HikePlanAdapter adapter;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        clearHikePlanButton = view.findViewById(R.id.btn_clearHikePlan);
        clearHikePlanButton.setOnClickListener(v -> {
            databaseHelper.clearAllHikePlans();
            Toast.makeText(requireContext(), "Clear All Hike Plan Successfully.", Toast.LENGTH_SHORT).show();
            setupRecyclerViewAndLoadData(view);
            hideSearch(view);
        });

        setupRecyclerViewAndLoadData(view);

        createHikePlanButton = view.findViewById(R.id.btn_createHikePlan);
        createHikePlanButton.setOnClickListener(v -> {
            DialogFragment createHikePlanDialog = new CreateHikePlanDialogFragment();
            createHikePlanDialog.show(getParentFragmentManager(), "create_hike_plan_dialog");
        });
        EditText editTextSearch = view.findViewById(R.id.editTextSearch);
        Button btnSearch = view.findViewById(R.id.btn_search);

        btnSearch.setOnClickListener(v -> {
            String searchName = editTextSearch.getText().toString();
            updateRecyclerViewData(searchName);
        });
        hideSearch(view);

        return view;
    }
    private void hideSearch(View view){
        boolean isTableEmpty = databaseHelper.isTableHKEmpty();
        EditText editTextSearch = view.findViewById(R.id.editTextSearch);
        Button btnSearch = view.findViewById(R.id.btn_search);
        if(isTableEmpty){
            btnSearch.setVisibility(View.GONE);
            editTextSearch.setVisibility(View.GONE);
        }
    }
    private void updateRecyclerViewData(String searchName) {
        Cursor cursor;
        if (!searchName.isEmpty()) {
            cursor = databaseHelper.getFilteredCursor(searchName);
        } else {
            cursor = databaseHelper.getCursor();
        }

        if (recyclerView.getAdapter() != null) {
            adapter.swapCursor(cursor);
        } else {
            setupRecyclerViewAndLoadData(requireView());
        }
        adapter = new HikePlanAdapter(cursor);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }


    private void setupRecyclerViewAndLoadData(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        databaseHelper = new DatabaseHelper(requireContext());

        boolean isTableEmpty = databaseHelper.isTableHKEmpty();
        TextView noHikePlansTextView = view.findViewById(R.id.textViewNoHikePlans);

        if (!isTableEmpty) {
            noHikePlansTextView.setVisibility(View.GONE);
            clearHikePlanButton.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            Cursor cursor = databaseHelper.getCursor();
            adapter = new HikePlanAdapter(cursor);
            recyclerView.setAdapter(adapter);
            updateRecyclerViewData("");
        } else {
            clearHikePlanButton.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            noHikePlansTextView.setVisibility(View.VISIBLE);
        }
    }


    public static class CreateHikePlanDialogFragment extends DialogFragment {

        public CreateHikePlanDialogFragment () {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.hike_plan_create_form, container, false);

            EditText editTextDate = view.findViewById(R.id.editTextDate);
            DatePickerDialog.OnDateSetListener dateSetListener = (view1, year, month, dayOfMonth) -> {
                String date = dayOfMonth + " / " + (month + 1) + " / " + year;
                editTextDate.setText(date);
            };

            editTextDate.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener, year, month, day);
                datePickerDialog.show();
            });

            Button backFormButton = view.findViewById(R.id.btn_backPopupCreateHikePlan);
            backFormButton.setOnClickListener(v -> dismiss());

            Button createFormHikePlanButton = view.findViewById(R.id.btn_formCreateHikePlan);

            createFormHikePlanButton.setOnClickListener(v -> {
                EditText editTextHikePlan = view.findViewById(R.id.editTextHikePlan);
                EditText editTextLocation = view.findViewById(R.id.editTextLocation);
                EditText editTextHikeLength = view.findViewById(R.id.editTextHikeLength);
                EditText editTextDescription = view.findViewById(R.id.editTextDescription);
                CheckBox checkBoxPackingAvailable = view.findViewById(R.id.checkBoxPackingAvailable);
                EditText editTextIntendTime = view.findViewById(R.id.editTextIntendTime);
                CheckBox checkBoxLicense = view.findViewById(R.id.checkBoxLicense);
                RadioGroup radioGroupDifficulty = view.findViewById(R.id.radioGroupDifficulty);
                if (isDataValid(view).equals("valid")) {
                    String hikePlan = editTextHikePlan.getText().toString();
                    String location = editTextLocation.getText().toString();
                    String date = editTextDate.getText().toString();
                    boolean packingAvailable = checkBoxPackingAvailable.isChecked();
                    String hikeLength = editTextHikeLength.getText().toString();
                    String difficulty = getSelectedGroup(view, radioGroupDifficulty);
                    String description = editTextDescription.getText().toString();
                    boolean license = checkBoxLicense.isChecked();
                    String intendTime = editTextIntendTime.getText().toString();

                    Bundle args = new Bundle();
                    args.putString("hikePlan", hikePlan);
                    args.putString("location", location);
                    args.putString("date", date);
                    args.putBoolean("packingAvailable", packingAvailable);
                    args.putString("hikeLength", hikeLength);
                    args.putString("difficulty", difficulty);
                    args.putString("description", description);
                    args.putString("intendTime", intendTime);
                    args.putBoolean("license", license);

                    DialogFragment createFormHikePlanDialogFragment = new CreateFormHikePlanDialogFragment();
                    createFormHikePlanDialogFragment.setArguments(args);
                    createFormHikePlanDialogFragment.show(getParentFragmentManager(), "createFormHikePlanDialogFragment_dialog");
                } else {
                    String validationMessage = isDataValid(view);
                    Toast.makeText(requireContext(), validationMessage, Toast.LENGTH_SHORT).show();
                }
            });

            return view;
        }

        private String isDataValid(View view) {
            EditText editTextHikePlan = view.findViewById(R.id.editTextHikePlan);
            EditText editTextLocation = view.findViewById(R.id.editTextLocation);
            EditText editTextHikeLength = view.findViewById(R.id.editTextHikeLength);
            EditText editTextDate = view.findViewById(R.id.editTextDate);
            EditText editTextIntendTime = view.findViewById(R.id.editTextIntendTime);
            RadioGroup radioGroupDifficulty = view.findViewById(R.id.radioGroupDifficulty);

            String hikePlan = editTextHikePlan.getText().toString();
            String hikeLength = editTextHikeLength.getText().toString();
            String DateOfTheHike = editTextDate.getText().toString();
            String difficulty = getSelectedGroup(view, radioGroupDifficulty);
            String intendTime = editTextIntendTime.getText().toString();

            if (hikePlan.isEmpty() || difficulty.isEmpty() || DateOfTheHike.isEmpty()) {
                return "Please fill in all the required fields.";
            }

            try {
                int hikeLengthValue = Integer.parseInt(hikeLength);
                if (hikeLengthValue < 1 || hikeLengthValue > 50) {
                    return "Hike Length should be a number between 1 and 50.";
                }
            } catch (NumberFormatException e) {
                return "Hike Length should be a valid number.";
            }

            try {
                int intendTimeValue = Integer.parseInt(intendTime);
                if (intendTimeValue < 1 || intendTimeValue > 50) {
                    return "Intend time should be a number between 1 and 50.";
                }
            } catch (NumberFormatException e) {
                return "Intend time should be a valid number.";
            }

            return "valid";
        }

    }
    private static String getSelectedGroup(View view, RadioGroup radioGroup) {
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = view.findViewById(selectedRadioButtonId);
            return selectedRadioButton.getText().toString();
        } else {
            return "";
        }
    }

    public static class CreateFormHikePlanDialogFragment extends DialogFragment {

        public CreateFormHikePlanDialogFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.confirm_hike_plan_creation, container, false);

            Bundle args = getArguments();
            if (args != null) {
                String hikePlan = args.getString("hikePlan");
                String location = args.getString("location");
                String date = args.getString("date");
                boolean packingAvailable = args.getBoolean("packingAvailable");
                String hikeLength = args.getString("hikeLength");
                String difficulty = args.getString("difficulty");
                String description = args.getString("description");
                String intendTime = args.getString("intendTime");
                boolean license = args.getBoolean("license");

                TextView confirmValueHikePlan = view.findViewById(R.id.confirmValueHikePlan);
                confirmValueHikePlan.setText(hikePlan);

                TextView confirmValueLocation = view.findViewById(R.id.confirmValueLocation);
                confirmValueLocation.setText(location);

                TextView confirmValueDate = view.findViewById(R.id.confirmValueDateOfTheHike);
                confirmValueDate.setText(date);

                TextView confirmValuePackingAvailable = view.findViewById(R.id.confirmValuePackingAvailable);
                confirmValuePackingAvailable.setText(packingAvailable ? "Yes" : "No");

                TextView confirmValueHikeLength = view.findViewById(R.id.confirmValueHikeLength);
                confirmValueHikeLength.setText(hikeLength);

                TextView confirmValueDifficulty = view.findViewById(R.id.confirmValueDifficulty);
                confirmValueDifficulty.setText(difficulty);

                TextView confirmValueDescription = view.findViewById(R.id.confirmValueDescription);
                confirmValueDescription.setText(description);

                TextView confirmValueIntendTime = view.findViewById(R.id.confirmValueIntendTime);
                confirmValueIntendTime.setText(intendTime);

                TextView confirmValueLicense = view.findViewById(R.id.confirmValueLicense);
                confirmValueLicense.setText(license ? "Yes" : "No");

                Button btnConfirmCreateHikePlan = view.findViewById(R.id.btn_confirmCreateHikePlan);
                btnConfirmCreateHikePlan.setOnClickListener(v -> {

                    DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
                    long newRowId = databaseHelper.insertData(hikePlan, location, date, String.valueOf(packingAvailable), hikeLength, difficulty, description, intendTime, String.valueOf(license));

                    if (newRowId != -1) {
                        Toast.makeText(requireContext(), "Hike Plan created and inserted into the database.", Toast.LENGTH_SHORT).show();

                        HomeFragment homeFragment = (HomeFragment) getParentFragmentManager().findFragmentByTag("home_fragment");
                        if (homeFragment != null) {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            assert mainActivity != null;
                            mainActivity.replaceFragment(new HomeFragment(), "home_fragment");
                        }

                        FragmentManager fragmentManager = getParentFragmentManager();
                        for (Fragment fragment : fragmentManager.getFragments()) {
                            if (fragment instanceof DialogFragment) {
                                DialogFragment dialogFragment = (DialogFragment) fragment;
                                dialogFragment.dismiss();
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to create Hike Plan.", Toast.LENGTH_SHORT).show();
                    }
                });
            }


            Button backConfirmButton = view.findViewById(R.id.btn_back_confirm_page);
            backConfirmButton.setOnClickListener(v -> dismiss());

            return view;
        }
    }
    public static class EditHikePlanDialogFragment extends DialogFragment {

        public EditHikePlanDialogFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.edit_hike_plan, container, false);

            Bundle args = getArguments();
            if (args != null) {
                long hikePlanId = args.getLong("hikePlanId", -1); // Default to -1 if not found

                String finalDifficult;
                EditText editTextHikePlan = view.findViewById(R.id.editTextHikePlan);
                EditText editTextLocation = view.findViewById(R.id.editTextLocation);
                EditText editTextDate = view.findViewById(R.id.editTextDate);
                CheckBox checkBoxPackingAvailable = view.findViewById(R.id.checkBoxPackingAvailable);
                EditText editTextHikeLength = view.findViewById(R.id.editTextHikeLength);
                RadioGroup radioGroupDifficulty = view.findViewById(R.id.radioGroupDifficulty);
                EditText editTextDescription = view.findViewById(R.id.editTextDescription);
                EditText editTextIntendTime = view.findViewById(R.id.editTextIntendTime);
                CheckBox checkBoxLicense = view.findViewById(R.id.checkBoxLicense);
                DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
                Cursor hikePlanCursor = databaseHelper.getHikePlanById(hikePlanId);

                if (hikePlanCursor != null && hikePlanCursor.moveToFirst()) {
                    @SuppressLint("Range") String hikePlanName = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(databaseHelper.COLUMN_HP_NAME));
                    @SuppressLint("Range") String location = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(databaseHelper.COLUMN_HP_LOCATION));
                    @SuppressLint("Range") String date = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(databaseHelper.COLUMN_HP_DOTH));
                    @SuppressLint("Range") boolean packingAvailable = Boolean.parseBoolean(hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_Packing_Available)));
                    @SuppressLint("Range") String hikeLength = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_Hike_Length));
                    @SuppressLint("Range") String difficulty = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_LOD));
                    @SuppressLint("Range") String description = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_Des));
                    @SuppressLint("Range") String intendTime = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_Intend_Time));
                    @SuppressLint("Range") boolean license = Boolean.parseBoolean(hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_License)));

                    editTextHikePlan.setText(hikePlanName);
                    editTextLocation.setText(location);
                    editTextDate.setText(date);
                    checkBoxPackingAvailable.setChecked(packingAvailable);
                    editTextHikeLength.setText(hikeLength);

                    switch (difficulty) {
                        case "Easy":
                            radioGroupDifficulty.check(R.id.radioButtonEasy);
                            break;
                        case "Normal":
                            radioGroupDifficulty.check(R.id.radioButtonNormal);
                            break;
                        case "Hard":
                            radioGroupDifficulty.check(R.id.radioButtonHard);
                            break;
                    }

                    editTextDescription.setText(description);
                    editTextIntendTime.setText(intendTime);
                    checkBoxLicense.setChecked(license);
                }

                if (hikePlanCursor != null) {
                    hikePlanCursor.close();
                }

                int selectedRadioButtonId = radioGroupDifficulty.getCheckedRadioButtonId();
                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = view.findViewById(selectedRadioButtonId);
                    finalDifficult = selectedRadioButton.getText().toString();
                } else {
                    finalDifficult = null;
                }

                Button saveButton = view.findViewById(R.id.btn_saveEdit);

                saveButton.setOnClickListener(v -> {
                    String hikePlanName = editTextHikePlan.getText().toString();
                    String location = editTextLocation.getText().toString();
                    String date = editTextDate.getText().toString();
                    boolean packingAvailable = checkBoxPackingAvailable.isChecked();
                    String hikeLength = editTextHikeLength.getText().toString();
                    String difficulty = finalDifficult;
                    String description = editTextDescription.getText().toString();
                    String intendTime = editTextIntendTime.getText().toString();
                    boolean license = checkBoxLicense.isChecked();

                    long updatedRows = databaseHelper.updateHikePlanInDatabase(hikePlanId, hikePlanName, location, date, packingAvailable, hikeLength, difficulty, description, intendTime, license);

                    if (updatedRows > 0) {
                        Toast.makeText(requireContext(), "Hike Plan updated in the database.", Toast.LENGTH_SHORT).show();
                        HomeFragment homeFragment = (HomeFragment) getParentFragmentManager().findFragmentByTag("home_fragment");
                        if (homeFragment != null) {
                            homeFragment.updateRecyclerViewData("");
                        }

                        FragmentManager fragmentManager = getParentFragmentManager();
                        for (Fragment fragment : fragmentManager.getFragments()) {
                            if (fragment instanceof DialogFragment) {
                                DialogFragment dialogFragment = (DialogFragment) fragment;
                                dialogFragment.dismiss();
                            }
                        }
                        dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update Hike Plan.", Toast.LENGTH_SHORT).show();
                    }
                });

                Button backButton = view.findViewById(R.id.btn_backPopupSaveHikePlan);
                backButton.setOnClickListener(v -> {
                    dismiss();
                });
            }

            return view;
        }
    }
    public static class ViewHikePlanDialogFragment extends DialogFragment {

        public ViewHikePlanDialogFragment() {
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.view_hike_plan, container, false);
            Bundle args = getArguments();
            if (args != null) {
                long hikePlanId = args.getLong("hikePlanId", -1);

                TextView textViewHikePlan = view.findViewById(R.id.confirmValueHikePlan);
                TextView textViewLocation = view.findViewById(R.id.confirmValueLocation);
                TextView textViewDate = view.findViewById(R.id.confirmValueDateOfTheHike);
                TextView textViewPackingAvailable = view.findViewById(R.id.confirmValuePackingAvailable);
                TextView textViewHikeLength = view.findViewById(R.id.confirmValueHikeLength);
                TextView textViewDifficulty = view.findViewById(R.id.confirmValueDifficulty);
                TextView textViewDescription = view.findViewById(R.id.confirmValueDescription);
                TextView textViewIntendTime = view.findViewById(R.id.confirmValueIntendTime);
                TextView textViewLicense = view.findViewById(R.id.confirmValueLicense);
                TextView textViewObservations = view.findViewById(R.id.confirmValueObservation);
                DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
                Cursor hikePlanCursor = databaseHelper.getHikePlanById(hikePlanId);

                Button backButton = view.findViewById(R.id.btn_back);
                Button createObservationButton = view.findViewById(R.id.btn_createObservation);
                Button editObservationButton = view.findViewById(R.id.btn_editObservation);

                if (hikePlanCursor != null && hikePlanCursor.moveToFirst()) {
                    @SuppressLint("Range") String hikePlanName = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(databaseHelper.COLUMN_HP_NAME));
                    @SuppressLint("Range") String location = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(databaseHelper.COLUMN_HP_LOCATION));
                    @SuppressLint("Range") String date = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(databaseHelper.COLUMN_HP_DOTH));
                    @SuppressLint("Range") String packingAvailable = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_Packing_Available));
                    @SuppressLint("Range") String hikeLength = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_Hike_Length));
                    @SuppressLint("Range") String difficulty = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_LOD));
                    @SuppressLint("Range") String description = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_Des));
                    @SuppressLint("Range") String intendTime = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_Intend_Time));
                    @SuppressLint("Range") String license = hikePlanCursor.getString(hikePlanCursor.getColumnIndex(DatabaseHelper.COLUMN_HP_License));

                    Cursor observationsCursor = databaseHelper.findObservationsByHikePlanId(hikePlanId);
                    if (databaseHelper.isTableOBEmpty()) {
                        createObservationButton.setVisibility(View.VISIBLE);
                        editObservationButton.setVisibility(View.GONE);
                        textViewObservations.setText("Observations unavailable.");
                    } else if (observationsCursor != null && observationsCursor.getCount() > 0) {
                        createObservationButton.setVisibility(View.GONE);
                        editObservationButton.setVisibility(View.VISIBLE);
                        textViewObservations.setText("Observations available.");
                        observationsCursor.close();
                    } else {
                        Log.d("create vi", "onCreateView: ");
                        createObservationButton.setVisibility(View.VISIBLE);
                        editObservationButton.setVisibility(View.GONE);
                        textViewObservations.setText("Observations unavailable.");
                    }

                    textViewHikePlan.setText(hikePlanName);
                    textViewLocation.setText(location);
                    textViewDate.setText(date);
                    textViewPackingAvailable.setText(packingAvailable.equals("true") ? "Yes" : "No");
                    textViewHikeLength.setText(hikeLength);
                    textViewDifficulty.setText(difficulty);
                    textViewDescription.setText(description);
                    textViewIntendTime.setText(intendTime);
                    textViewLicense.setText(license.equals("true") ? "Yes" : "No");
                }

                if (hikePlanCursor != null) {
                    hikePlanCursor.close();
                }

                backButton.setOnClickListener(v -> dismiss());

                createObservationButton.setOnClickListener(v -> {
                    DialogFragment createObservationsDialogFragment = new CreateObservationsDialogFragment(hikePlanId);
                    createObservationsDialogFragment.show(getParentFragmentManager(), "create_observations_dialog");
                });
            }

            return view;
        }
    }
    public static class CreateObservationsDialogFragment extends DialogFragment {
        long hikePlanId;
        public CreateObservationsDialogFragment(long hikePlanId) {
            this.hikePlanId = hikePlanId;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.observation_create_form, container, false);
            EditText editTextAnimalSign = view.findViewById(R.id.editTextAnimalSign);
            EditText editTextVegetations = view.findViewById(R.id.editTextVegetations);
            EditText editTextDate = view.findViewById(R.id.editTextDate);
            EditText editTextTime = view.findViewById(R.id.editTextTime);
            RadioGroup radioGroupTrails = view.findViewById(R.id.radioGroupTrails);
            RadioGroup radioGroupWeather = view.findViewById(R.id.radioGroupWeather);
            EditText editTextLakeLocation = view.findViewById(R.id.editTextLakeLocation);
            EditText editTextComment = view.findViewById(R.id.editTextComment);

            String animalSign = editTextAnimalSign.getText().toString();
            String vegetations = editTextVegetations.getText().toString();
            String lakeLocation = editTextLakeLocation.getText().toString();
            String comments = editTextComment.getText().toString();
            String timeOfObservation = "Date: " + editTextDate + " Time: " + editTextTime;
            String trailsConditions = getSelectedGroup(view, radioGroupTrails);
            String weatherConditions = getSelectedGroup(view, radioGroupWeather);
            DatePickerDialog.OnDateSetListener dateSetListener = (view1, year, month, dayOfMonth) -> {
                String date = dayOfMonth + " / " + (month + 1) + " / " + year;
                editTextDate.setText(date);
            };

            editTextDate.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener, year, month, day);
                datePickerDialog.show();
            });
            TimePickerDialog.OnTimeSetListener timeSetListener = (view12, hourOfDay, minute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editTextTime.setText(time);
            };

            editTextTime.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), timeSetListener, hourOfDay, minute, true);
                timePickerDialog.show();
            });


            Button backButton = view.findViewById(R.id.btn_back);
            backButton.setOnClickListener(v -> dismiss());

            Button submitButton = view.findViewById(R.id.btn_formCreateObservation);

            submitButton.setOnClickListener(v -> {
                if (!editTextDate.getText().toString().equals("") || !editTextTime.getText().toString().equals("")) {
                    DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
                    long newRowId = databaseHelper.insertDataObservation(hikePlanId, animalSign, vegetations, timeOfObservation, trailsConditions, weatherConditions, lakeLocation, comments);

                    if (newRowId != -1) {
                        Toast.makeText(requireContext(), "Observations created and inserted into the database.", Toast.LENGTH_SHORT).show();
                        Toast.makeText(requireContext(), animalSign, Toast.LENGTH_SHORT).show();
                        Log.d("Observation", "Inserted");

                        HomeFragment homeFragment = (HomeFragment) getParentFragmentManager().findFragmentByTag("home_fragment");
                        if (homeFragment != null) {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            assert mainActivity != null;
                            mainActivity.replaceFragment(new HomeFragment(), "home_fragment");
                        }

                        FragmentManager fragmentManager = getParentFragmentManager();
                        for (Fragment fragment : fragmentManager.getFragments()) {
                            if (fragment instanceof DialogFragment) {
                                DialogFragment dialogFragment = (DialogFragment) fragment;
                                dialogFragment.dismiss();
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to create Observations.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return view;
        }
    }

    public class HikePlanAdapter extends RecyclerView.Adapter<HikePlanAdapter.ViewHolder> {

        private Cursor cursor;

        public HikePlanAdapter(Cursor cursor) {
            this.cursor = cursor;
        }
        @SuppressLint("NotifyDataSetChanged")
        public void swapCursor(Cursor newCursor) {
            if (cursor != null) {
                cursor.close();
            }
            cursor = newCursor;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hike_plan_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor != null && cursor.moveToPosition(position)) {
                @SuppressLint("Range") String hikePlanName = cursor.getString(cursor.getColumnIndex("hp_name"));
                holder.nameTextView.setText(hikePlanName);

                holder.imageViewDetails.setOnClickListener(v -> {
                    int itemPosition = holder.getAdapterPosition();

                    if (itemPosition != RecyclerView.NO_POSITION) {
                        cursor.moveToPosition(itemPosition);
                        @SuppressLint("Range") long hikePlanId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_HP_ID));

                        if (hikePlanId != -1) {
                            Bundle args = new Bundle();
                            args.putLong("hikePlanId", hikePlanId);

                            DialogFragment viewHikePlanDialogFragment = new ViewHikePlanDialogFragment();
                            viewHikePlanDialogFragment.setArguments(args);
                            viewHikePlanDialogFragment.show(getParentFragmentManager(), "view_hike_plan_dialog");
                        } else {
                            Toast.makeText(requireContext(), "Hike Plan ID not found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                holder.imageViewEdit.setOnClickListener(v -> {
                    int itemPosition = holder.getAdapterPosition();

                    if (itemPosition != RecyclerView.NO_POSITION) {
                        cursor.moveToPosition(itemPosition);
                        @SuppressLint("Range") long hikePlanId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_HP_ID));

                        if (hikePlanId != -1) {
                            Bundle args = new Bundle();
                            args.putLong("hikePlanId", hikePlanId);

                            DialogFragment editHikePlanDialogFragment = new EditHikePlanDialogFragment();
                            editHikePlanDialogFragment.setArguments(args);
                            editHikePlanDialogFragment.show(getParentFragmentManager(), "edit_hike_plan_dialog");
                        } else {
                            Toast.makeText(requireContext(), "Hike Plan ID not found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                holder.imageViewDelete.setOnClickListener(v -> {
                    int itemPosition = holder.getAdapterPosition();

                    if (itemPosition != RecyclerView.NO_POSITION) {
                        cursor.moveToPosition(itemPosition);
                        @SuppressLint("Range") long hikePlanId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_HP_ID));

                        if (hikePlanId != -1) {
                            int deletedRows = databaseHelper.deleteHikePlan(hikePlanId);

                            if (deletedRows > 0) {
                                Toast.makeText(requireContext(), "Hike Plan deleted.", Toast.LENGTH_SHORT).show();
                                updateRecyclerViewData("");
                                hideSearch(getView());
                            } else {
                                Toast.makeText(requireContext(), "Failed to delete Hike Plan.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Hike Plan ID not found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return cursor != null ? cursor.getCount() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            ImageView imageViewDetails, imageViewEdit, imageViewDelete;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.textViewHikePlanName);
                imageViewDetails = itemView.findViewById(R.id.imageViewDetails);
                imageViewEdit = itemView.findViewById(R.id.imageViewEdit);
                imageViewDelete = itemView.findViewById(R.id.imageViewDelete);


            }
        }
    }
}
