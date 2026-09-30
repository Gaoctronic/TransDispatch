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


    public Poi createPoi(Poi poi) {
        poi.setDeleted(false);
        return poiRepository.save(poi);
    }

    /**
     * 删除POI
     */
    public void deletePoi(Integer id) {
        Poi poi = poiRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow();

        poi.setDeleted(true);
        poiRepository.save(poi);
    }

    public Poi createFactory(Poi factory) {
        factory.setPoiType("FACTORY");
        return poiRepository.save(factory);
    }

    public void deleteFactory(Integer id) {
        Poi factory = poiRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow();

        if (!"FACTORY".equals(factory.getPoiType())) {
            throw new IllegalArgumentException("该 POI 不是工厂");
        }

        factory.setDeleted(true);
        poiRepository.save(factory);
    }
}