package com.ftpl.finfactor.reporting.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class DAOManager {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("finsenseDataSource")
    public DataSource finsenseDataSourceConnection;

    @Autowired
    @Qualifier("pfmDataSource")
    public DataSource pfmDataSource;

    public PreparedStatement getStatement(String query, DataSource dataSource) throws SQLException {
        if (query == null) {
            throw new IllegalArgumentException("Query not found");
        }
        Connection connection = dataSource.getConnection();
        return connection.prepareStatement(query);
    }

    public void close(ResultSet rs, Statement stmt, Connection con) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException ignored) { }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException ignored) { }
        try {
            if (con != null) con.close();
        } catch (SQLException ignored) { }
    }
}
