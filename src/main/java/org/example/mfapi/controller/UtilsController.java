package org.example.mfapi.controller;

import org.example.mfapi.dto.setupgroup.RdbAccess;
import org.example.mfapi.service.UtilsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;

import java.util.Set;

@RestController
public class UtilsController {

    private final UtilsService utilsService;

    public UtilsController(UtilsService utilsService) {
        this.utilsService = utilsService;
    }

    @PostMapping(path = "/api/utils/tryConnectRdb")
    public Boolean tryConnectRdb(@RequestBody RdbAccess access) {
        return utilsService.tryConnectRdb(access);
    }

    @PostMapping(path = "/api/utils/getMongoCollections")
    public Set<String> getMongoCollections(@RequestBody MongoConnectionCredentials credentials) {
        var mongoConnection = utilsService.createMongoConnection(credentials);
        return utilsService.getMongoCollections(mongoConnection);
    }

}
