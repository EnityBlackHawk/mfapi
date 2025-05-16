package org.example.mfapi.controller;

import org.example.mfapi.dto.ModelDTO;
import org.example.mfapi.dto.SetupDTO;
import org.example.mfapi.service.MfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.utfpr.mf.migration.params.MetadataInfo;
import org.utfpr.mf.migration.params.Model;

@RestController
@RequestMapping("/api")
public class MainController {

    private final MfService mfService;

    public MainController(MfService mfService) {
        this.mfService = mfService;
    }

    @PostMapping(path = "/setup")
    public ResponseEntity<MetadataInfo> setup(@RequestBody SetupDTO dto) {
        return  ResponseEntity.ok(mfService.setup(dto));
    }

    @PostMapping(path = "/generateModel")
    public ResponseEntity<ModelDTO> generateModel(@RequestBody MetadataInfo metadataInfo) {
        return ResponseEntity.ok(mfService.generateModel(metadataInfo));
    }


}
