package com.carvaluation.mileage;

public class OdoScanResult {
    private boolean success;
    private Integer mileage; // Số km
    private String unit = "km";
    private double confidence = 0.9;
    private boolean needsConfirmation = true;
    private String message;

    public OdoScanResult() {}

    public OdoScanResult(boolean success, Integer mileage, String unit, boolean needsConfirmation, String message) {
        this.success = success;
        this.mileage = mileage;
        this.unit = unit;
        this.needsConfirmation = needsConfirmation;
        this.message = message;
    }

    public static OdoScanResult failure(String message) {
        OdoScanResult r = new OdoScanResult();
        r.setSuccess(false);
        r.setMessage(message);
        r.setNeedsConfirmation(true);
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public boolean isNeedsConfirmation() {
        return needsConfirmation;
    }

    public void setNeedsConfirmation(boolean needsConfirmation) {
        this.needsConfirmation = needsConfirmation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
