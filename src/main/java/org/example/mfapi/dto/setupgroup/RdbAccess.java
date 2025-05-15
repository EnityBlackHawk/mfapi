package org.example.mfapi.dto.setupgroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RdbAccess {

    private String host;
    private String user;
    private String password;
    private String sgbd;

}
