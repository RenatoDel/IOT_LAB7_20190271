package com.example.iot_lab07_20190271.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_lab07_20190271.R;
import com.example.iot_lab07_20190271.adapters.MovementAdapter;
import com.example.iot_lab07_20190271.dialogs.AddLimaPassMovementDialog;
import com.example.iot_lab07_20190271.models.Movement;
import com.example.iot_lab07_20190271.services.MovementService;
import com.example.iot_lab07_20190271.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class LimaPassFragment extends Fragment implements MovementAdapter.OnMovementClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private LinearLayout layoutEmptyState;
    private MovementAdapter adapter;
    private List<Movement> movements;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lima_pass, container, false);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadMovements();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_movements);
        fabAdd = view.findViewById(R.id.fab_add_movement);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
    }

    private void setupRecyclerView() {
        movements = new ArrayList<>();
        adapter = new MovementAdapter(movements);
        adapter.setOnMovementClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddMovementDialog());
    }

    private void loadMovements() {
        MovementService.getMovementsByCardType(Constants.CARD_TYPE_LIMAPASS, new MovementService.MovementCallback() {
            @Override
            public void onSuccess(List<Movement> movementList) {
                movements.clear();
                movements.addAll(movementList);
                adapter.updateMovements(movements);

                // Mostrar/ocultar empty state
                if (movements.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error cargando movimientos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAddMovementDialog() {
        AddLimaPassMovementDialog dialog = AddLimaPassMovementDialog.newInstance();
        dialog.setOnMovementSavedListener(this::loadMovements);
        dialog.show(getParentFragmentManager(), "AddLimaPassMovementDialog");
    }

    private void showEditMovementDialog(Movement movement) {
        AddLimaPassMovementDialog dialog = AddLimaPassMovementDialog.newInstance(movement);
        dialog.setOnMovementSavedListener(this::loadMovements);
        dialog.show(getParentFragmentManager(), "EditLimaPassMovementDialog");
    }

    private void showDeleteConfirmation(Movement movement) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Viaje")
                .setMessage("¿Estás seguro de que quieres eliminar este viaje de Lima Pass?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteMovement(movement))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteMovement(Movement movement) {
        MovementService.deleteMovement(movement.getMovementId(), new MovementService.OperationCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                loadMovements();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error eliminando movimiento: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Implementación de MovementAdapter.OnMovementClickListener
    @Override
    public void onMovementClick(Movement movement) {
        Toast.makeText(getContext(), "Viaje de " + movement.getEntryStation() + " a " + movement.getExitStation(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Movement movement) {
        showEditMovementDialog(movement);
    }

    @Override
    public void onDeleteClick(Movement movement) {
        showDeleteConfirmation(movement);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMovements();
    }
}