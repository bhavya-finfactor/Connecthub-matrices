package com.ftpl.finfactor.reporting.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
public class MonthlyLAPSDataDAO {

    private static final Logger logger = LoggerFactory.getLogger(MonthlyLAPSDataDAO.class);

    @Autowired
    private DAOManager daoManager;

    public List<Map<String, Object>> fetchFinsenseRequestStatusCount(LocalDate startDate, LocalDate endDate) {
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        Connection con = null;
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            stmt = daoManager.getStatement("query.finsense.data.fetch");
            con = stmt.getConnection();

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                resultList.add(Map.of(
                        "request_status", resultSet.getString("request_status"),
                        "count", resultSet.getInt("count(*)")
                ));
            }
        } catch (SQLException e) {
            logger.error("Exception occurred while fetching Finsense request status counts.", e);
            throw new RuntimeException(e);
        } finally {
            daoManager.close(resultSet, stmt, con);
        }
        return resultList;
    }

    public List<Map<String, Object>> fetchPfmRequestStatusCount(LocalDate startDate, LocalDate endDate) {
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        Connection con = null;
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            stmt = daoManager.getStatement("query.pfm.data.fetch");
            con = stmt.getConnection();

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                resultList.add(Map.of(
                        "request_status", resultSet.getString("request_status"),
                        "count", resultSet.getInt("count(*)")
                ));
            }
        } catch (SQLException e) {
            logger.error("Exception occurred while fetching Finsense request status counts.", e);
            throw new RuntimeException(e);
        } finally {
            daoManager.close(resultSet, stmt, con);
        }
        return resultList;
    }
}
