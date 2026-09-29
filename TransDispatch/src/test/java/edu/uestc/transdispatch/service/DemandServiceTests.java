package edu.uestc.transdispatch.service;

import edu.uestc.transdispatch.dto.DemandCreateDTO;
import edu.uestc.transdispatch.entity.Cargo;
import edu.uestc.transdispatch.entity.Demand;
import edu.uestc.transdispatch.entity.Poi;
import edu.uestc.transdispatch.repository.CargoRepository;
import edu.uestc.transdispatch.repository.DemandRepository;
import edu.uestc.transdispatch.repository.PoiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandServiceTests {

    @Mock
    private DemandRepository demandRepository;

    @Mock
    private PoiRepository poiRepository;

    @Mock
    private CargoRepository cargoRepository;

    private DemandService demandService;

    @BeforeEach
    void setUp() {
        demandService = new DemandService(demandRepository, poiRepository, cargoRepository);
    }

    @Test
    void createDemandBuildsAndSavesActiveDemand() {
        DemandCreateDTO request = createRequest();
        Poi startPoi = poi(1, "FACTORY");
        Poi endPoi = poi(2, "STORE");
        Cargo cargo = new Cargo();
        cargo.setId(1);

        when(poiRepository.findById(1)).thenReturn(Optional.of(startPoi));
        when(poiRepository.findById(2)).thenReturn(Optional.of(endPoi));
        when(cargoRepository.findById(1)).thenReturn(Optional.of(cargo));
        when(demandRepository.save(any(Demand.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Demand result = demandService.createDemand(request);

        assertEquals("测试运输需求", result.getDemandName());
        assertSame(startPoi, result.getStartPoi());
        assertSame(endPoi, result.getEndPoi());
        assertSame(cargo, result.getCargo());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(request.getStartTime(), result.getStartTime());
        assertEquals(request.getDeadline(), result.getDeadline());
        verify(demandRepository).save(result);
    }

    @Test
    void createDemandRejectsNonFactoryStartPoi() {
        DemandCreateDTO request = createRequest();
        when(poiRepository.findById(1)).thenReturn(Optional.of(poi(1, "SCHOOL")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> demandService.createDemand(request)
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(demandRepository, never()).save(any());
    }

    @Test
    void createDemandRejectsMissingStartPoi() {
        DemandCreateDTO request = createRequest();
        when(poiRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> demandService.createDemand(request)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(demandRepository, never()).save(any());
    }

    @Test
    void createDemandRejectsMissingCargo() {
        DemandCreateDTO request = createRequest();
        when(poiRepository.findById(1)).thenReturn(Optional.of(poi(1, "FACTORY")));
        when(poiRepository.findById(2)).thenReturn(Optional.of(poi(2, "STORE")));
        when(cargoRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> demandService.createDemand(request)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(demandRepository, never()).save(any());
    }

    @Test
    void invalidateDemandUpdatesStatusWithoutDeleting() {
        Demand demand = new Demand();
        demand.setId(10);
        demand.setStatus("ACTIVE");
        when(demandRepository.findById(10)).thenReturn(Optional.of(demand));
        when(demandRepository.save(demand)).thenReturn(demand);

        Demand result = demandService.invalidateDemand(10);

        assertEquals("INVALID", result.getStatus());
        verify(demandRepository).save(demand);
        verify(demandRepository, never()).deleteById(any());
    }

    @Test
    void invalidateDemandRejectsMissingDemand() {
        when(demandRepository.findById(10)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> demandService.invalidateDemand(10)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(demandRepository, never()).save(any());
    }

    private DemandCreateDTO createRequest() {
        DemandCreateDTO request = new DemandCreateDTO();
        request.setStartPoiId(1);
        request.setEndPoiId(2);
        request.setCargoId(1);
        request.setDemandName("测试运输需求");
        request.setStartTime(LocalDateTime.of(2026, 9, 25, 10, 0));
        request.setDeadline(LocalDateTime.of(2026, 9, 26, 10, 0));
        return request;
    }

    private Poi poi(Integer id, String poiType) {
        Poi poi = new Poi();
        poi.setId(id);
        poi.setPoiType(poiType);
        return poi;
    }
}
