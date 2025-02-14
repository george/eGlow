package me.mrgraycat.eglow.database.credentials.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mrgraycat.eglow.database.credentials.DatabaseCredentials;

@Getter @AllArgsConstructor
public class MySqlDatabaseCredentials implements DatabaseCredentials {

    private final String ip;
    private final int port;

    private final String databaseName;

    private final String username;
    private final String password;

}
