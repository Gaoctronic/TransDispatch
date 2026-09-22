package edu.uestc.transdispatch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private VehicleType vehicleType;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "current_poi_id")
    private Poi currentPoi;


    @ManyToOne
    @JoinColumn(name = "current_route_id")
    private Route currentRoute;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_fuel_1")
    private Double currentFuel1;

    @Column(name = "current_load_t")
    private Double currentLoadT;

    @Column(name = "current_action")
    private String currentAction;

    @OneToMany(
            mappedBy = "vehicle"
    )
    private List<TransTask> tasks;


}