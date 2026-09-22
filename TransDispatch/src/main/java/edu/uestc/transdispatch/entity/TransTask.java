package edu.uestc.transdispatch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trans_task")
public class TransTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "task_code")
    private String taskCode;


    @ManyToOne
    @JoinColumn(name = "demand_id")
    private Demand demand;

    @ManyToOne
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "status")
    private String status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "finish_time")
    private LocalDateTime finishTime;

    @Column(name = "actual_distance")
    private Double actualDistance;

    @Column(name = "actual_cost")
    private Double actualCost;
}