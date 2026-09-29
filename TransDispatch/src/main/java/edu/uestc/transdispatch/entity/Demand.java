package edu.uestc.transdispatch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "demand")
public class Demand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "demand_name")
    private String demandName;

    @Column(name = "status")
    private String status;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "start_poi_id")
    private Poi startPoi;

    @ManyToOne
    @JoinColumn(name = "end_poi_id")
    private Poi endPoi;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @ManyToOne
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @OneToMany(
            mappedBy = "demand"
    )
    private List<TransTask> tasks;


}