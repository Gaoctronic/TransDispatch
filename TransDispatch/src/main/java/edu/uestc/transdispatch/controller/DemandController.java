package edu.uestc.transdispatch.controller;

import edu.uestc.transdispatch.entity.Demand;
import edu.uestc.transdispatch.entity.TransTask;
import edu.uestc.transdispatch.service.DemandService;
import edu.uestc.transdispatch.service.DispatchService;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 运输需求接口
 */
@RestController
@RequestMapping("/demands")
public class DemandController {


    private final DemandService demandService;


    private final DispatchService dispatchService;



    public DemandController(
            DemandService demandService,
            DispatchService dispatchService
    ){

        this.demandService = demandService;

        this.dispatchService = dispatchService;

    }



    /**
     * 查询所有需求
     *
     * GET /demands
     */
    @GetMapping
    public List<Demand> getAllDemands(){

        return demandService
                .getAllDemands();

    }



    /**
     * 查询单个需求
     *
     * GET /demands/{id}
     */
    @GetMapping("/{id}")
    public Demand getDemandById(
            @PathVariable Integer id
    ){

        return demandService
                .getDemandById(id)
                .orElse(null);

    }



    /**
     * 创建运输需求
     *
     * POST /demands
     */
    @PostMapping
    public Demand createDemand(
            @RequestBody Demand demand
    ){

        return demandService
                .saveDemand(demand);

    }



    /**
     * 调度需求
     *
     * POST /demands/{id}/dispatch
     */
    @PostMapping("/{id}/dispatch")
    public TransTask dispatchDemand(
            @PathVariable Integer id
    ){

        Demand demand =
                demandService
                        .getDemandById(id)
                        .orElseThrow();


        return dispatchService
                .dispatchDemand(demand);

    }


}