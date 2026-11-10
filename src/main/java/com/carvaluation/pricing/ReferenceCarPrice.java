package com.carvaluation.pricing;

import jakarta.persistence.*;

@Entity
@Table(name = "reference_car_prices")
public class ReferenceCarPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String make;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(name = "model_year", nullable = false)
    private Integer year;

    @Column(length = 50)
    private String trim;

    @Column(name = "original_new_price_vnd")
    private Long originalNewPriceVnd; // Giá lăn bánh lúc mua mới

    @Column(name = "base_used_price_vnd", nullable = false)
    private Long baseUsedPriceVnd; // Giá xe cũ tiêu chuẩn trên thị trường hiện tại

    @Column(name = "depreciation_per_year_rate")
    private Double depreciationPerYearRate = 0.08; // Tỷ lệ khấu hao trung bình mỗi năm (8%)

    public ReferenceCarPrice() {}

    public ReferenceCarPrice(String make, String model, Integer year, String trim, Long originalNewPriceVnd, Long baseUsedPriceVnd) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.trim = trim;
        this.originalNewPriceVnd = originalNewPriceVnd;
        this.baseUsedPriceVnd = baseUsedPriceVnd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getOriginalNewPriceVnd() {
        return originalNewPriceVnd;
    }

    public void setOriginalNewPriceVnd(Long originalNewPriceVnd) {
        this.originalNewPriceVnd = originalNewPriceVnd;
    }

    public Long getBaseUsedPriceVnd() {
        return baseUsedPriceVnd;
    }

    public void setBaseUsedPriceVnd(Long baseUsedPriceVnd) {
        this.baseUsedPriceVnd = baseUsedPriceVnd;
    }

    public Double getDepreciationPerYearRate() {
        return depreciationPerYearRate;
    }

    public void setDepreciationPerYearRate(Double depreciationPerYearRate) {
        this.depreciationPerYearRate = depreciationPerYearRate;
    }
}
