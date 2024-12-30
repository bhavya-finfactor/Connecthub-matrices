package com.ftpl.finfactor.reporting.dao.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class LAPSFinsenseDAOManager implements DAOManager {

    @Autowired
    @Qualifier("finsenseDataSource")
    private DataSource finsenseDataSourceConnection;


    public PreparedStatement getStatement(String query) throws SQLException {
        return DAOManager.super.getStatement(query, finsenseDataSourceConnection);
    }

    @Override
    public void close(ResultSet rs, Statement stmt, Connection con) {
    }
}
