// RevenueController.java
@RestController
@RequestMapping("/api/trans")
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping("/revenue")
    public RevenueResponse calculateRevenue(@RequestParam Integer demandId,
                                            @RequestParam Integer vehicleId,
                                            @RequestParam Integer routeId) {
        return revenueService.calculateRevenue(demandId, vehicleId, routeId);
    }
}
