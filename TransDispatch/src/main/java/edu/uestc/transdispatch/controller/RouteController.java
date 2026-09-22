package edu.uestc.transdispatch.controller;

import edu.uestc.transdispatch.entity.Route;
import edu.uestc.transdispatch.service.RouteService;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 路线接口
 */
@RestController
@RequestMapping("/routes")
public class RouteController {


    private final RouteService routeService;


    public RouteController(
            RouteService routeService
    ){

        this.routeService = routeService;

    }



    /**
     * 查询所有路线
     *
     * GET /routes
     */
    @GetMapping
    public List<Route> getAllRoutes(){

        return routeService.getAllRoutes();

    }



    /**
     * 根据id查询路线
     *
     * GET /routes/{id}
     */
    @GetMapping("/{id}")
    public Route getRouteById(
            @PathVariable Integer id
    ){

        return routeService
                .getRouteById(id)
                .orElse(null);

    }



    /**
     * 创建路线
     *
     * POST /routes
     */
    @PostMapping
    public Route createRoute(
            @RequestBody Route route
    ){

        return routeService.saveRoute(route);

    }



    /**
     * 删除路线
     *
     * DELETE /routes/{id}
     */
    @DeleteMapping("/{id}")
    public void deleteRoute(
            @PathVariable Integer id
    ){

        routeService.deleteRoute(id);

    }


}