package org.example.mfapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrateDTO {

    GeneratedJavaCode generatedJavaCode;
    MongoConnectionCredentials credentials;

}
