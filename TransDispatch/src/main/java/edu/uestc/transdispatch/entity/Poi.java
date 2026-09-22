package edu.uestc.transdispatch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "poi")
public class Poi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "poi_name")
    private String poiName;

    @Column(name = "poi_type")
    private String poiType;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @OneToMany(
            mappedBy = "currentPoi"
    )
    private List<Vehicle> vehicles;

    @OneToMany(
            mappedBy = "startPoi"
    )
    private List<Route> startRoutes;

    @OneToMany(
            mappedBy = "endPoi"
    )
    private List<Route> endRoutes;


}