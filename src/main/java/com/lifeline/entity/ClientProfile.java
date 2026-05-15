package com.lifeline.entity;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.persistence.*;

/**
 * Stores vehicle and extra profile details for a client user.
 * One-to-one with USERS (linked by user_id).
 */
@Entity
@Table(name = "CLIENT_PROFILES")
public class ClientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_profile_seq")
    @SequenceGenerator(name = "client_profile_seq", sequenceName = "CLIENT_PROFILES_SEQ", allocationSize = 1)
    private Long id;

    /** FK → USERS(ID) */
    @OneToOne
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @Column(name = "PHONE", length = 30)
    private String phone;

    @Column(name = "INSURANCE_POLICY", length = 100)
    @JsonbProperty("insurance")
    private String insurancePolicy;

    @Column(name = "VEHICLE_MAKE", length = 80)
    private String vehicleMake;

    @Column(name = "VEHICLE_MODEL", length = 80)
    private String vehicleModel;

    @Column(name = "VEHICLE_YEAR", length = 10)
    private String vehicleYear;

    @Column(name = "LICENSE_PLATE", length = 20)
    private String licensePlate;

    // ── Getters & Setters ──────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @JsonbProperty("insurance")
    public String getInsurancePolicy() { return insurancePolicy; }
    public void setInsurancePolicy(String insurancePolicy) { this.insurancePolicy = insurancePolicy; }

    @JsonbProperty("vehicleMake")
    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }

    @JsonbProperty("vehicleModel")
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    @JsonbProperty("vehicleYear")
    public String getVehicleYear() { return vehicleYear; }
    public void setVehicleYear(String vehicleYear) { this.vehicleYear = vehicleYear; }

    @JsonbProperty("licensePlate")
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
}
