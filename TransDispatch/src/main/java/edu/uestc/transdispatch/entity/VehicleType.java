package edu.uestc.transdispatch.entity;

import jakarta.persistence.*;
import lombok.Data;


import java.util.List;

@Data
@Entity
@Table(name = "vehicle_type")
public class VehicleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "max_load_t")
    private Double maxLoadT;

    @Column(name = "speed_kmh")
    private Double speedKmh;

    @Column(name = "fuel_l_100km")
    private Double fuelL100km;

    @Column(name = "tank_capacity_l")
    private Double tankCapacityL;

    @OneToMany(
            mappedBy = "vehicleType",
            cascade = CascadeType.ALL
    )
    private List<Vehicle> vehicles;


}
