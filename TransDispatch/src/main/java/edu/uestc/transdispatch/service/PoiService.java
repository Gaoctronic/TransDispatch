package edu.uestc.transdispatch.service;

import edu.uestc.transdispatch.entity.Poi;
import edu.uestc.transdispatch.repository.PoiRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * POI业务服务
 *
 * 管理地图中的位置节点
 */
@Service
public class PoiService {


    private final PoiRepository poiRepository;


    /**
     * 构造器注入
     */
    public PoiService(PoiRepository poiRepository){

        this.poiRepository = poiRepository;

    }



    /**
     * 查询所有POI
     */
    public List<Poi> getAllPois(){

        return poiRepository.findAll();

    }



    /**
     * 根据id查询POI
     */
    public Optional<Poi> getPoiById(Integer id){

        return poiRepository.findById(id);

    }



    /**
     * 保存POI
     */
    public Poi savePoi(Poi poi){

        return poiRepository.save(poi);

    }



    /**
     * 删除POI
     */
    public void deletePoi(Integer id){

        poiRepository.deleteById(id);

    }

}