package edu.uestc.transdispatch.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "route")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "route_name")
    private String routeName;

    @ManyToOne
    @JoinColumn(name = "start_poi_id")
    private Poi startPoi;

    @ManyToOne
    @JoinColumn(name = "end_poi_id")
    private Poi endPoi;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "estimated_time")
    private Double estimatedTime;

    @Column(name = "trans_cost")
    private Double transCost;

    @Column(name = "status")
    private String status;

    @OneToMany(
            mappedBy = "currentRoute"
    )
    private List<Vehicle> vehicles;

    @OneToMany(
            mappedBy = "route"
    )
    private List<TransTask> tasks;
}