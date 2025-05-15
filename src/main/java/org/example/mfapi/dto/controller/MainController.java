package org.example.mfapi.dto.controller;

import org.example.mfapi.dto.SetupDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MainController {

    @PostMapping(path = "/setup")
    public ResponseEntity<SetupDTO> setup(@RequestBody SetupDTO dto) {
        return  ResponseEntity.ok(dto);
    }


}
