package com.mounir.learn.drbooking.web.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing form for the weekly working hours editor.
 */
public class WorkingHoursForm {

    private List<Row> rows = new ArrayList<>();

    public static WorkingHoursForm empty() {
        WorkingHoursForm form = new WorkingHoursForm();
        for (DayOfWeek day : DayOfWeek.values()) {
            Row row = new Row();
            row.setDayOfWeek(day);
            row.setEnabled(false);
            row.setStartTime(LocalTime.of(9, 0));
            row.setEndTime(LocalTime.of(17, 0));
            form.rows.add(row);
        }
        return form;
    }

    public List<Row> getRows() { return rows; }
    public void setRows(List<Row> rows) { this.rows = rows; }

    public static class Row {
        private DayOfWeek dayOfWeek;
        private boolean enabled;
        private LocalTime startTime;
        private LocalTime endTime;

        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    }
}
