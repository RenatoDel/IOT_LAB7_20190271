package com.example.iot_lab07_20190271.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_lab07_20190271.R;
import com.example.iot_lab07_20190271.models.Movement;
import com.example.iot_lab07_20190271.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MovementAdapter extends RecyclerView.Adapter<MovementAdapter.MovementViewHolder> {

    private List<Movement> movements;
    private OnMovementClickListener listener;

    // Interface para clicks
    public interface OnMovementClickListener {
        void onMovementClick(Movement movement);
        void onEditClick(Movement movement);
        void onDeleteClick(Movement movement);
    }

    public MovementAdapter(List<Movement> movements) {
        this.movements = movements;
    }

    public void setOnMovementClickListener(OnMovementClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movement, parent, false);
        return new MovementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovementViewHolder holder, int position) {
        Movement movement = movements.get(position);
        holder.bind(movement);
    }

    @Override
    public int getItemCount() {
        return movements.size();
    }

    public void updateMovements(List<Movement> newMovements) {
        this.movements = newMovements;
        notifyDataSetChanged();
    }

    class MovementViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCardType, ivEdit, ivDelete;
        private TextView tvCardId, tvRoute, tvDate, tvTime, tvTravelTime;

        public MovementViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCardType = itemView.findViewById(R.id.iv_card_type);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            tvCardId = itemView.findViewById(R.id.tv_card_id);
            tvRoute = itemView.findViewById(R.id.tv_route);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTravelTime = itemView.findViewById(R.id.tv_travel_time);
        }

        public void bind(Movement movement) {
            // Ícono según tipo de tarjeta
            if (Constants.CARD_TYPE_LINEA1.equals(movement.getCardType())) {
                ivCardType.setImageResource(R.drawable.ic_train);
            } else {
                ivCardType.setImageResource(R.drawable.ic_bus);
            }

            // Información del movimiento con terminología correcta
            String cardTypeLabel = Constants.CARD_TYPE_LINEA1.equals(movement.getCardType()) ?
                    "Línea 1" : "Lima Pass";

            tvCardId.setText("Tarjeta " + cardTypeLabel + ": " + movement.getCardId());

            // Mostrar "Estación" para Línea 1 y "Paradero" para Lima Pass
            String routeText;
            if (Constants.CARD_TYPE_LINEA1.equals(movement.getCardType())) {
                routeText = movement.getEntryStation() + " → " + movement.getExitStation();
            } else {
                routeText = movement.getEntryStation() + " → " + movement.getExitStation();
            }
            tvRoute.setText(routeText);

            // Formatear fecha y hora
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            tvDate.setText(dateFormat.format(movement.getDate()));
            tvTime.setText(timeFormat.format(movement.getDate()));
            tvTravelTime.setText(movement.getTravelTime() + " min");

            // Click listeners (iguales que antes)
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMovementClick(movement);
                }
            });

            ivEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(movement);
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(movement);
                }
            });
        }
    }
}