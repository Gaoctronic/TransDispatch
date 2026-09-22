package edu.uestc.transdispatch.service;

import edu.uestc.transdispatch.entity.Route;
import edu.uestc.transdispatch.repository.RouteRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * 路线业务服务
 */
@Service
public class RouteService {


    private final RouteRepository routeRepository;


    /**
     * 构造器注入
     */
    public RouteService(RouteRepository routeRepository){

        this.routeRepository = routeRepository;

    }



    /**
     * 查询所有路线
     */
    public List<Route> getAllRoutes(){

        return routeRepository.findAll();

    }



    /**
     * 根据id查询路线
     */
    public Optional<Route> getRouteById(Integer id){

        return routeRepository.findById(id);

    }



    /**
     * 保存路线
     */
    public Route saveRoute(Route route){

        return routeRepository.save(route);

    }



    /**
     * 删除路线
     */
    public void deleteRoute(Integer id){

        routeRepository.deleteById(id);

    }


}