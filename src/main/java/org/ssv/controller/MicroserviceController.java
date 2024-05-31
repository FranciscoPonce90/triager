package org.ssv.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ssv.model.Analysis;
import org.ssv.model.Microservice;
import org.ssv.model.Smell;
import org.ssv.service.TriageService;
import org.ssv.service.database.FacadeService;

import java.util.List;

@RestController
@Api(tags = "Microservices")
@RequestMapping("/microservices")
public class MicroserviceController {

    @Autowired
    private FacadeService facadeService;

    @ApiOperation(value = "Add a new microservice to an analysis", notes = "Provide an analysis ID and microservice details to add a new microservice")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully added microservice", response = Microservice.class),
            @ApiResponse(code = 404, message = "Analysis not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @PostMapping("/{analysisId}")
    public ResponseEntity<Microservice> addMicroservice(
            @ApiParam(value = "ID of the analysis", required = true) @PathVariable String analysisId,
            @ApiParam(value = "Details of the microservice to add", required = true) @RequestBody Microservice microservice) {

        Analysis analysis = facadeService.findAnalysisById(analysisId);
        microservice.setAnalysis(analysis);
        facadeService.saveMicroservice(microservice);
        return ResponseEntity.status(201).body(microservice);
    }

    @ApiOperation(value = "Assign a microservice to a smell", notes = "Provide analysis ID, microservice ID, and smell ID to assign a microservice to a smell")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully assigned microservice to smell"),
            @ApiResponse(code = 404, message = "Analysis, microservice or smell not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @PutMapping("/{analysisId}/{microserviceId}/{smellId}")
    public ResponseEntity<Void> assignMicroserviceToSmell(
            @ApiParam(value = "ID of the analysis", required = true) @PathVariable String analysisId,
            @ApiParam(value = "ID of the microservice", required = true) @PathVariable int microserviceId,
            @ApiParam(value = "ID of the smell", required = true) @PathVariable int smellId) {

        facadeService.findAnalysisById(analysisId);
        Smell smell = facadeService.findSmellById(analysisId, smellId);
        if (microserviceId == -1) {
            smell.setMicroservice(null);
            smell.setUrgencyCode(null);
            facadeService.saveSmell(smell);
            return ResponseEntity.ok().build();
        }

        Microservice microservice = facadeService.findMicroserviceById(analysisId, microserviceId);
        TriageService triageService = new TriageService();
        smell.setUrgencyCode(triageService.urgencyCodeCalculator(microservice, smell));
        smell.setMicroservice(microservice);
        facadeService.saveSmell(smell);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Assign a microservice to multiple smells", notes = "Provide analysis ID, microservice ID, and list of smell IDs to assign a microservice to multiple smells")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully assigned microservice to multiple smells"),
            @ApiResponse(code = 404, message = "Analysis or microservice not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @PutMapping("/{analysisId}/{microserviceId}/smells")
    public ResponseEntity<Void> assignMicroservicesToMultipleSmells(
            @ApiParam(value = "ID of the analysis", required = true) @PathVariable String analysisId,
            @ApiParam(value = "ID of the microservice", required = true) @PathVariable int microserviceId,
            @ApiParam(value = "List of smell IDs", required = true) @RequestBody List<Integer> smellsIds) {

        facadeService.findAnalysisById(analysisId);
        Microservice microservice = facadeService.findMicroserviceById(analysisId, microserviceId);
        TriageService triageService = new TriageService();
        for (int smellId : smellsIds) {
            Smell smell = facadeService.findSmellById(analysisId, smellId);
            if (smell != null) {
                smell.setUrgencyCode(triageService.urgencyCodeCalculator(microservice, smell));
                smell.setMicroservice(microservice);
                facadeService.saveSmell(smell);
            }
        }
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Update a microservice", notes = "Provide analysis ID and microservice details to update a microservice")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated microservice"),
            @ApiResponse(code = 404, message = "Analysis or microservice not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @PutMapping("/{analysisId}")
    public ResponseEntity<Void> updateMicroservice(
            @ApiParam(value = "ID of the analysis", required = true) @PathVariable String analysisId,
            @ApiParam(value = "Updated microservice details", required = true) @RequestBody Microservice microserviceTmp) {

        Analysis analysis = facadeService.findAnalysisById(analysisId);
        Microservice microservice = facadeService.findMicroserviceById(analysisId, microserviceTmp.getId());

        facadeService.updateMicroservice(microservice, microserviceTmp);
        TriageService triageService = new TriageService();
        for (Smell smell : analysis.getSmells()) {
            if (smell.getMicroservice() != null && smell.getMicroservice().getName().equals(microservice.getName())) {
                smell.setUrgencyCode(triageService.urgencyCodeCalculator(microservice, smell));
                facadeService.saveSmell(smell);
            }
        }
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Delete a microservice by ID", notes = "Provide analysis ID and microservice ID to delete a specific microservice")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Successfully deleted microservice"),
            @ApiResponse(code = 404, message = "Analysis or microservice not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @DeleteMapping("/{analysisId}/{microserviceId}")
    public ResponseEntity<Void> deleteMicroservice(
            @ApiParam(value = "ID of the analysis", required = true) @PathVariable String analysisId,
            @ApiParam(value = "ID of the microservice to delete", required = true) @PathVariable int microserviceId) {

        facadeService.findAnalysisById(analysisId);
        Microservice microservice = facadeService.findMicroserviceById(analysisId, microserviceId);
        facadeService.deleteMicroservice(microservice);
        return ResponseEntity.noContent().build();
    }
}
