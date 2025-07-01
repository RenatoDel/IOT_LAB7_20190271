package com.example.iot_lab07_20190271.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.iot_lab07_20190271.R;
import com.example.iot_lab07_20190271.models.Movement;
import com.example.iot_lab07_20190271.services.MovementService;
import com.example.iot_lab07_20190271.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddMovementDialog extends DialogFragment {

    private String cardType;
    private Movement editMovement;
    private OnMovementSavedListener listener;

    private EditText etCardId, etTravelTime;
    private Spinner spinnerEntryStation, spinnerExitStation;
    private TextView tvSelectedDate, tvSelectedTime;
    private Button btnSelectDate, btnSelectTime, btnSave, btnCancel;

    private Calendar selectedDateTime;

    // Interface para callback
    public interface OnMovementSavedListener {
        void onMovementSaved();
    }

    public static AddMovementDialog newInstance(String cardType) {
        AddMovementDialog dialog = new AddMovementDialog();
        Bundle args = new Bundle();
        args.putString("cardType", cardType);
        dialog.setArguments(args);
        return dialog;
    }

    public static AddMovementDialog newInstance(Movement movement) {
        AddMovementDialog dialog = new AddMovementDialog();
        Bundle args = new Bundle();
        args.putString("cardType", movement.getCardType());
        args.putParcelable("editMovement", movement);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cardType = getArguments().getString("cardType");
            editMovement = getArguments().getParcelable("editMovement");
        }
        selectedDateTime = Calendar.getInstance();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_limapass_movement, null);

        initViews(view);
        setupSpinners();
        setupListeners();

        // Si se edita, cargar datos
        if (editMovement != null) {
            loadEditData();
        }

        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        etCardId = view.findViewById(R.id.et_card_id);
        etTravelTime = view.findViewById(R.id.et_travel_time);
        spinnerEntryStation = view.findViewById(R.id.spinner_entry_station);
        spinnerExitStation = view.findViewById(R.id.spinner_exit_station);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvSelectedTime = view.findViewById(R.id.tv_selected_time);
        btnSelectDate = view.findViewById(R.id.btn_select_date);
        btnSelectTime = view.findViewById(R.id.btn_select_time);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Título según tarjeta
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        if (editMovement != null) {
            tvTitle.setText("Editar Movimiento");
        } else {
            String title = Constants.CARD_TYPE_LINEA1.equals(cardType) ?
                    "Nuevo Viaje - Línea 1" : "Nuevo Viaje - Lima Pass";
            tvTitle.setText(title);
        }

        // Mostrar fecha y hora actual
        updateDateTimeDisplay();
    }

    private void setupSpinners() {
        String[] stations;

        if (Constants.CARD_TYPE_LINEA1.equals(cardType)) {
            stations = Constants.LINEA1_STATIONS;
        } else {
            stations = Constants.LIMAPASS_STOPS;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                stations
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerEntryStation.setAdapter(adapter);
        spinnerExitStation.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveMovement());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void loadEditData() {
        etCardId.setText(editMovement.getCardId());
        etTravelTime.setText(String.valueOf(editMovement.getTravelTime()));

        selectedDateTime.setTime(editMovement.getDate());
        updateDateTimeDisplay();

        // Seleccionar estaciones
        setSpinnerSelection(spinnerEntryStation, editMovement.getEntryStation());
        setSpinnerSelection(spinnerExitStation, editMovement.getExitStation());
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        tvSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
        tvSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveMovement() {
        if (!validateInputs()) {
            return;
        }

        String cardId = etCardId.getText().toString().trim();
        String entryStation = spinnerEntryStation.getSelectedItem().toString();
        String exitStation = spinnerExitStation.getSelectedItem().toString();
        int travelTime = Integer.parseInt(etTravelTime.getText().toString().trim());
        Date selectedDate = selectedDateTime.getTime();

        if (editMovement != null) {
            // Actualizar movimiento
            editMovement.setCardId(cardId);
            editMovement.setDate(selectedDate);
            editMovement.setEntryStation(entryStation);
            editMovement.setExitStation(exitStation);
            editMovement.setTravelTime(travelTime);

            MovementService.updateMovement(
                    editMovement.getMovementId(),
                    editMovement,
                    new MovementService.OperationCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onMovementSaved();
                            }
                            dismiss();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else {
            // Crear nuevo movimiento
            Movement newMovement = new Movement(
                    null, null, cardType, cardId,
                    selectedDate, entryStation, exitStation, travelTime
            );

            MovementService.createMovement(newMovement, new MovementService.OperationCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onMovementSaved();
                    }
                    dismiss();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean validateInputs() {
        String cardId = etCardId.getText().toString().trim();
        String travelTimeStr = etTravelTime.getText().toString().trim();

        if (cardId.isEmpty()) {
            etCardId.setError("Ingresa el ID de la tarjeta");
            return false;
        }

        if (travelTimeStr.isEmpty()) {
            etTravelTime.setError("Ingresa el tiempo de viaje");
            return false;
        }

        try {
            int travelTime = Integer.parseInt(travelTimeStr);
            if (travelTime <= 0) {
                etTravelTime.setError("El tiempo debe ser mayor a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            etTravelTime.setError("Ingresa un número válido");
            return false;
        }

        String entryStation = spinnerEntryStation.getSelectedItem().toString();
        String exitStation = spinnerExitStation.getSelectedItem().toString();

        if (entryStation.equals(exitStation)) {
            Toast.makeText(getContext(), "La estación de entrada y salida no pueden ser iguales", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void setOnMovementSavedListener(OnMovementSavedListener listener) {
        this.listener = listener;
    }
}