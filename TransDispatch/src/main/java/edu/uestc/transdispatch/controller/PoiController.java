package edu.uestc.transdispatch.controller;

import edu.uestc.transdispatch.entity.Poi;
import edu.uestc.transdispatch.service.PoiService;

import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * POI位置节点接口
 */
@RestController
@RequestMapping("/pois")
public class PoiController {
    private final PoiService poiService;


    public PoiController(
            PoiService poiService
    ){

        this.poiService = poiService;

    }

    /**
     * 查询所有POI
     *
     * GET /pois
     */
    @GetMapping
    public List<Poi> getAllPois(){

        return poiService.getAllPois();

    }

    /**
     * 根据id查询POI
     *
     * GET /pois/{id}
     */
    @GetMapping("/{id}")
    public Poi getPoiById(
            @PathVariable Integer id
    ){

        return poiService
                .getPoiById(id)
                .orElse(null);

    }


    /**
     * 创建POI
     *
     * POST /pois
     */
    @PostMapping
    public Poi createPoi(@RequestBody Poi poi) {
        return poiService.createPoi(poi);
    }


    /**
     * 删除POI
     *
     * DELETE /pois/{id}
     */
    @DeleteMapping("/{id}")
    public void deletePoi(@PathVariable Integer id) {
        poiService.deletePoi(id);
    }


    @PostMapping("/factories")
    public Poi createFactory(@RequestBody Poi factory) {
        return poiService.createFactory(factory);
    }

    @DeleteMapping("/factories/{id}")
    public void deleteFactory(@PathVariable Integer id) {
        poiService.deleteFactory(id);
    }
}
