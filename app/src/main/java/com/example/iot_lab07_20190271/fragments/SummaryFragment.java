package com.example.iot_lab07_20190271.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.iot_lab07_20190271.R;
import com.example.iot_lab07_20190271.services.SummaryService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SummaryFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    private TextView tvTotalTrips, tvCurrentMonth;
    private Button btnDateFilter, btnClearFilter;

    private Date filterStartDate, filterEndDate;
    private boolean isFiltered = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        initViews(view);
        setupCharts();
        setupListeners();
        loadData();

        return view;
    }

    private void initViews(View view) {
        barChart = view.findViewById(R.id.bar_chart);
        pieChart = view.findViewById(R.id.pie_chart);
        tvTotalTrips = view.findViewById(R.id.tv_total_trips);
        tvCurrentMonth = view.findViewById(R.id.tv_current_month);
        btnDateFilter = view.findViewById(R.id.btn_date_filter);
        btnClearFilter = view.findViewById(R.id.btn_clear_filter);
    }

    private void setupCharts() {
        setupBarChart();
        setupPieChart();
    }

    private void setupBarChart() {
        // Configuración general
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setMaxVisibleValueCount(60);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        // Configurar ejes
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(6);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Leyenda
        Legend legend = barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(9f);
        legend.setTextSize(11f);
        legend.setXEntrySpace(4f);
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(38f);
        pieChart.setTransparentCircleRadius(50f);

        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Distribución\nde Uso");
        pieChart.setCenterTextSize(12f);

        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // Leyenda
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
    }

    private void setupListeners() {
        btnDateFilter.setOnClickListener(v -> showDateRangePicker());
        btnClearFilter.setOnClickListener(v -> clearFilter());
    }

    private void loadData() {
        if (isFiltered && filterStartDate != null && filterEndDate != null) {
            SummaryService.getSummaryDataByDateRange(filterStartDate, filterEndDate, summaryCallback);
        } else {
            SummaryService.getSummaryData(summaryCallback);
        }
    }

    private SummaryService.SummaryCallback summaryCallback = new SummaryService.SummaryCallback() {
        @Override
        public void onSuccess(Map<String, SummaryService.MonthlyData> monthlyData, SummaryService.UsageData usageData) {
            updateUI(monthlyData, usageData);
        }

        @Override
        public void onError(Exception e) {
            Toast.makeText(getContext(), "Error cargando datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void updateUI(Map<String, SummaryService.MonthlyData> monthlyData, SummaryService.UsageData usageData) {
        // Actualizar estadísticas
        tvTotalTrips.setText(String.valueOf(usageData.totalTrips));

        // Calcular viajes del mes actual
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        String currentMonth = monthFormat.format(cal.getTime());
        SummaryService.MonthlyData currentMonthData = monthlyData.get(currentMonth);
        int currentMonthTrips = 0;
        if (currentMonthData != null) {
            currentMonthTrips = currentMonthData.linea1Count + currentMonthData.limaPassCount;
        }
        tvCurrentMonth.setText(String.valueOf(currentMonthTrips));

        // Actualizar gráficos
        updateBarChart(monthlyData);
        updatePieChart(usageData);
    }

    private void updateBarChart(Map<String, SummaryService.MonthlyData> monthlyData) {
        ArrayList<BarEntry> linea1Entries = new ArrayList<>();
        ArrayList<BarEntry> limaPassEntries = new ArrayList<>();
        ArrayList<String> months = new ArrayList<>();

        int index = 0;
        for (SummaryService.MonthlyData data : monthlyData.values()) {
            linea1Entries.add(new BarEntry(index, data.linea1Count));
            limaPassEntries.add(new BarEntry(index, data.limaPassCount));
            months.add(data.month);
            index++;
        }

        // Crear datasets
        BarDataSet linea1DataSet = new BarDataSet(linea1Entries, "Línea 1");
        linea1DataSet.setColor(Color.rgb(33, 150, 243)); // Azul
        linea1DataSet.setValueTextColor(Color.BLACK);
        linea1DataSet.setValueTextSize(10f);

        BarDataSet limaPassDataSet = new BarDataSet(limaPassEntries, "Lima Pass");
        limaPassDataSet.setColor(Color.rgb(76, 175, 80)); // Verde
        limaPassDataSet.setValueTextColor(Color.BLACK);
        limaPassDataSet.setValueTextSize(10f);

        // Configurar BarData
        BarData barData = new BarData(linea1DataSet, limaPassDataSet);
        barData.setBarWidth(0.35f);

        // Aplicar datos al gráfico
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(months));
        barChart.getXAxis().setLabelCount(months.size());

        // Agrupar barras
        barChart.groupBars(0f, 0.3f, 0.05f);
        barChart.invalidate();
    }

    private void updatePieChart(SummaryService.UsageData usageData) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (usageData.totalTrips > 0) {
            if (usageData.linea1Percentage > 0) {
                entries.add(new PieEntry(usageData.linea1Percentage, "Línea 1"));
            }
            if (usageData.limaPassPercentage > 0) {
                entries.add(new PieEntry(usageData.limaPassPercentage, "Lima Pass"));
            }
        } else {
            // Si no hay datos, mostrar mensaje
            entries.add(new PieEntry(100, "Sin datos"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        if (usageData.totalTrips > 0) {
            dataSet.setColors(
                    Color.rgb(33, 150, 243),  // Azul para Línea 1
                    Color.rgb(76, 175, 80)    // Verde para Lima Pass
            );
        } else {
            dataSet.setColors(Color.GRAY);
        }

        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.BLACK);

        pieChart.setData(pieData);
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }

    private void showDateRangePicker() {
        Calendar cal = Calendar.getInstance();

        // Selector de fecha de inicio
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar startCal = Calendar.getInstance();
            startCal.set(year, month, dayOfMonth, 0, 0, 0);
            filterStartDate = startCal.getTime();

            // Selector de fecha de fin
            new DatePickerDialog(requireContext(), (view2, year2, month2, dayOfMonth2) -> {
                Calendar endCal = Calendar.getInstance();
                endCal.set(year2, month2, dayOfMonth2, 23, 59, 59);
                filterEndDate = endCal.getTime();

                // Aplicar filtro
                applyDateFilter();

            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    .show();

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void applyDateFilter() {
        isFiltered = true;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateRange = dateFormat.format(filterStartDate) + " - " + dateFormat.format(filterEndDate);
        btnDateFilter.setText(dateRange);

        loadData();
        Toast.makeText(getContext(), "Filtro aplicado", Toast.LENGTH_SHORT).show();
    }

    private void clearFilter() {
        isFiltered = false;
        filterStartDate = null;
        filterEndDate = null;
        btnDateFilter.setText("Seleccionar rango");

        loadData();
        Toast.makeText(getContext(), "Filtro eliminado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}