// MatchController.java
@RestController
@RequestMapping("/api/trans")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/match")
    public MatchResponse calculateMatch(@RequestParam Integer demandId,
                                        @RequestParam Integer vehicleId) {
        return matchService.calculateMatch(demandId, vehicleId);
    }
}
