package edu.uestc.transdispatch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "cargo")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cargo_name")
    private String cargoName;

    @Column(name = "cargo_type")
    private String cargoType;

    @Column(name = "weight_t")
    private Double weightT;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "status")
    private String status;

    @ManyToMany
    @JoinTable(
            name = "cargo_vehicle_type",
            joinColumns = @JoinColumn(name = "cargo_id"),
            inverseJoinColumns = @JoinColumn(name = "vehicle_type_id")
    )
    private List<VehicleType> suitableVehicleTypes;

    @OneToMany(
            mappedBy = "cargo"
    )
    private List<TransTask> tasks;


}