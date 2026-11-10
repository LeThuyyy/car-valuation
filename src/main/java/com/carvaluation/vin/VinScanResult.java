package com.carvaluation.vin;

import java.util.ArrayList;
import java.util.List;

public class VinScanResult {
    private boolean success;
    private String vin;
    private boolean valid;
    private String make;
    private String model;
    private Integer year;
    private String trim;
    private String bodyType;
    private String transmission;
    private String fuelType;
    private String engine;
    private String origin;
    private String country;
    private String source; // "NHTSA", "VIETNAM_MARKET", "LOCAL_RULE", "MANUAL"
    private boolean needsConfirmation = true;
    private List<String> missingFields = new ArrayList<>();
    private String message;

    public VinScanResult() {}

    public static VinScanResult failure(String vin, String message) {
        VinScanResult r = new VinScanResult();
        r.setSuccess(false);
        r.setVin(vin);
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

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getTrim() {
        return trim;
    }

    public void setTrim(String trim) {
        this.trim = trim;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isNeedsConfirmation() {
        return needsConfirmation;
    }

    public void setNeedsConfirmation(boolean needsConfirmation) {
        this.needsConfirmation = needsConfirmation;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
