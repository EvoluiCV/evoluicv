package com.evoluicv.backend.cv;

import com.evoluicv.backend.cv.dto.AnalysisRequest;
import com.evoluicv.backend.cv.dto.AnalysisResponse;
import com.evoluicv.backend.ratelimit.ClientIp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class CvAnalysisController {

    private final CvAnalysisService cvAnalysisService;

    public CvAnalysisController(CvAnalysisService cvAnalysisService) {
        this.cvAnalysisService = cvAnalysisService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @PostMapping(value = "/cv/analyze", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AnalysisResponse analyzeJson(@Valid @RequestBody AnalysisRequest request, HttpServletRequest http) {
        return cvAnalysisService.analyzeFromText(
                request.cvText(),
                request.professionalGoal(),
                request.targetRole(),
                ClientIp.from(http)
        );
    }

    @PostMapping(value = "/cv/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyzeMultipart(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "cvText", required = false) String cvText,
            @RequestParam("professionalGoal") @NotBlank String professionalGoal,
            @RequestParam(value = "targetRole", required = false) String targetRole,
            HttpServletRequest http
    ) {
        if (file != null && !file.isEmpty()) {
            return cvAnalysisService.analyzeFromFile(file, professionalGoal, targetRole, ClientIp.from(http));
        }
        if (cvText == null || cvText.isBlank()) {
            throw new IllegalArgumentException("Envie um arquivo em 'file' ou o texto do CV em 'cvText'.");
        }
        return cvAnalysisService.analyzeFromText(cvText, professionalGoal, targetRole, ClientIp.from(http));
    }
}
