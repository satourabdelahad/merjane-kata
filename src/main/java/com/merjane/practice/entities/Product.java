package com.merjane.practice.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "available")
    private Integer available;

    // "NORMAL", "SEASONAL" or "EXPIRABLE"
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ProductType type;

    @Column(name = "name")
    private String name;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "season_start_date")
    private LocalDate seasonStartDate;

    @Column(name = "season_end_date")
    private LocalDate seasonEndDate;

    public Product() {}

    public Product(Long id, Integer leadTime, Integer available, ProductType type, String name,
                   LocalDate expiryDate, LocalDate seasonStartDate, LocalDate seasonEndDate) {
        this.id = id;
        this.leadTime = leadTime;
        this.available = available;
        this.type = type;
        this.name = name;
        this.expiryDate = expiryDate;
        this.seasonStartDate = seasonStartDate;
        this.seasonEndDate = seasonEndDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getLeadTime() { return leadTime; }
    public void setLeadTime(Integer leadTime) { this.leadTime = leadTime; }
    public Integer getAvailable() { return available; }
    public void setAvailable(Integer available) { this.available = available; }
    public ProductType getType() { return type; }
    public void setType(ProductType type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public LocalDate getSeasonStartDate() { return seasonStartDate; }
    public void setSeasonStartDate(LocalDate seasonStartDate) { this.seasonStartDate = seasonStartDate; }
    public LocalDate getSeasonEndDate() { return seasonEndDate; }
    public void setSeasonEndDate(LocalDate seasonEndDate) { this.seasonEndDate = seasonEndDate; }
}
