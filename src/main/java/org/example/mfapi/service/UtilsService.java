package org.example.mfapi.service;

import org.example.mfapi.dto.setupgroup.RdbAccess;
import org.springframework.stereotype.Service;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.model.Credentials;
import org.utfpr.mf.mongoConnection.MongoConnection;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;

import java.sql.SQLException;
import java.util.Set;

@Service
public class UtilsService {

    public Boolean tryConnectRdb(RdbAccess rdbAccess) {
        Credentials credentials = new Credentials(
                rdbAccess.getHost(),
                rdbAccess.getUser(),
                rdbAccess.getPassword()
        );

        try {
            DbMetadata dbMetadata = new DbMetadata(credentials, null);
            var isConnected = dbMetadata.isConnected();

            if(isConnected) {
                dbMetadata.getConnection().close();
            }
            return isConnected;
        } catch (SQLException e) {
            return false;
        }

    }

    public MongoConnection createMongoConnection(MongoConnectionCredentials credentials) {
        return new MongoConnection(credentials);
    }

    public Set<String> getMongoCollections(MongoConnection mongoConnection) {
        try {
            return mongoConnection.getTemplate().getCollectionNames();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve collections: " + e.getMessage(), e);
        }
    }

}
