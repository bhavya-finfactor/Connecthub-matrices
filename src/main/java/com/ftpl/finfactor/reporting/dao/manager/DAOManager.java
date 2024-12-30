package com.ftpl.finfactor.reporting.dao.manager;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;

@Component
public interface DAOManager {

    default PreparedStatement getStatement(String query, DataSource dataSource) throws SQLException {
        if (query == null) {
            throw new IllegalArgumentException("Query not found");
        }
        Connection connection = dataSource.getConnection();
        return connection.prepareStatement(query);
    }

    default void close(ResultSet rs, Statement stmt, Connection con) {
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
