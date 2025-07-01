package com.example.iot_lab07_20190271.services;

import android.util.Log;

import com.example.iot_lab07_20190271.models.Movement;
import com.example.iot_lab07_20190271.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SummaryService {
    private static final String TAG = "SummaryService";

    // Clase para datos del gráfico de barras
    public static class MonthlyData {
        public String month;
        public int linea1Count;
        public int limaPassCount;

        public MonthlyData(String month, int linea1Count, int limaPassCount) {
            this.month = month;
            this.linea1Count = linea1Count;
            this.limaPassCount = limaPassCount;
        }
    }

    // Clase para datos del gráfico de torta
    public static class UsageData {
        public int linea1Percentage;
        public int limaPassPercentage;
        public int totalTrips;

        public UsageData(int linea1Percentage, int limaPassPercentage, int totalTrips) {
            this.linea1Percentage = linea1Percentage;
            this.limaPassPercentage = limaPassPercentage;
            this.totalTrips = totalTrips;
        }
    }

    // Interface para callbacks
    public interface SummaryCallback {
        void onSuccess(Map<String, MonthlyData> monthlyData, UsageData usageData);
        void onError(Exception e);
    }

    // Obtener estadísticas de resumen
    public static void getSummaryData(SummaryCallback callback) {
        com.example.iot_lab07_20190271.services.MovementService.getAllMovements(new com.example.iot_lab07_20190271.services.MovementService.MovementCallback() {
            @Override
            public void onSuccess(List<Movement> movements) {
                try {
                    Map<String, MonthlyData> monthlyData = calculateMonthlyData(movements);
                    UsageData usageData = calculateUsageData(movements);

                    callback.onSuccess(monthlyData, usageData);
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando datos de resumen", e);
                    callback.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // Calcular datos mensuales para gráfico de barras
    private static Map<String, MonthlyData> calculateMonthlyData(List<Movement> movements) {
        Map<String, MonthlyData> monthlyMap = new HashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        // Inicializar últimos 6 meses
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -i);
            String monthKey = monthFormat.format(cal.getTime());
            monthlyMap.put(monthKey, new MonthlyData(monthKey, 0, 0));
        }

        // Contar movimientos por mes y tipo
        for (Movement movement : movements) {
            String monthKey = monthFormat.format(movement.getDate());
            MonthlyData data = monthlyMap.get(monthKey);

            if (data != null) {
                if (Constants.CARD_TYPE_LINEA1.equals(movement.getCardType())) {
                    data.linea1Count++;
                } else if (Constants.CARD_TYPE_LIMAPASS.equals(movement.getCardType())) {
                    data.limaPassCount++;
                }
            }
        }

        return monthlyMap;
    }

    // Calcular datos de uso para gráfico de torta
    private static UsageData calculateUsageData(List<Movement> movements) {
        int linea1Count = 0;
        int limaPassCount = 0;

        for (Movement movement : movements) {
            if (Constants.CARD_TYPE_LINEA1.equals(movement.getCardType())) {
                linea1Count++;
            } else if (Constants.CARD_TYPE_LIMAPASS.equals(movement.getCardType())) {
                limaPassCount++;
            }
        }

        int total = linea1Count + limaPassCount;
        int linea1Percentage = total > 0 ? (linea1Count * 100) / total : 0;
        int limaPassPercentage = total > 0 ? (limaPassCount * 100) / total : 0;

        return new UsageData(linea1Percentage, limaPassPercentage, total);
    }

    // Filtrar movimientos por rango de fechas
    public static void getSummaryDataByDateRange(Date startDate, Date endDate, SummaryCallback callback) {
        com.example.iot_lab07_20190271.services.MovementService.getAllMovements(new com.example.iot_lab07_20190271.services.MovementService.MovementCallback() {
            @Override
            public void onSuccess(List<Movement> allMovements) {
                try {
                    // Filtrar por rango de fechas
                    List<Movement> filteredMovements = allMovements.stream()
                            .filter(movement -> {
                                Date movementDate = movement.getDate();
                                return !movementDate.before(startDate) && !movementDate.after(endDate);
                            })
                            .collect(java.util.stream.Collectors.toList());

                    Map<String, MonthlyData> monthlyData = calculateMonthlyData(filteredMovements);
                    UsageData usageData = calculateUsageData(filteredMovements);

                    callback.onSuccess(monthlyData, usageData);
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando datos filtrados", e);
                    callback.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}