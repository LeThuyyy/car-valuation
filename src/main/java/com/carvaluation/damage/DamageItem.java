package com.carvaluation.damage;

public class DamageItem {
    private DamageType type;
    private String location;
    private Severity severity;
    private String description;
    private Long estimatedDeductionVnd; // Số tiền khấu trừ ước tính (VND)
    private boolean confirmedByExpert = false;
    private String expertNote;

    public DamageItem() {}

    public DamageItem(DamageType type, String location, Severity severity, String description, Long estimatedDeductionVnd) {
        this.type = type;
        this.location = location;
        this.severity = severity;
        this.description = description;
        this.estimatedDeductionVnd = estimatedDeductionVnd;
    }

    public DamageType getType() {
        return type;
    }

    public void setType(DamageType type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getEstimatedDeductionVnd() {
        return estimatedDeductionVnd;
    }

    public void setEstimatedDeductionVnd(Long estimatedDeductionVnd) {
        this.estimatedDeductionVnd = estimatedDeductionVnd;
    }

    public boolean isConfirmedByExpert() {
        return confirmedByExpert;
    }

    public void setConfirmedByExpert(boolean confirmedByExpert) {
        this.confirmedByExpert = confirmedByExpert;
    }

    public String getExpertNote() {
        return expertNote;
    }

    public void setExpertNote(String expertNote) {
        this.expertNote = expertNote;
    }
}
