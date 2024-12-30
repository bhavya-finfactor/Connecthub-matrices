package com.ftpl.finfactor.reporting.dao;

import com.ftpl.finfactor.reporting.dao.manager.WeeklyDFSDataDAOManager;
import com.ftpl.finfactor.reporting.model.DFSDataCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeeklyDFSDataDAO {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyDFSDataDAO.class);

    @Autowired
    private WeeklyDFSDataDAOManager dfsDataDAOManager;
    
    public List<DFSDataCount> fetchFICounts(LocalDate startDate, LocalDate endDate){
            ResultSet resultSet = null;
            PreparedStatement stmt = null;
            Connection con = null;
            List<DFSDataCount> resultList = new ArrayList<>();

            try {
                stmt = dfsDataDAOManager.getStatement(
                        "SELECT  fetch_status ,COUNT(fetch_status)" +
                                "FROM fip_data_fetch_can " +
                                "WHERE fetch_timestamp >= ? AND fetch_timestamp <= ? " +
                                "GROUP BY fetch_status;"
                );
                con = stmt.getConnection();

                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));

                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    DFSDataCount fetchDataCount = new DFSDataCount(
                            resultSet.getString(1),
                            resultSet.getString(2)
                    );
                    resultList.add(fetchDataCount);
                }
            } catch (SQLException e) {
                logger.error("Exception occurred while fetching fetch_status data: {}", e.getMessage());
                throw new RuntimeException(e);
            } finally {
                dfsDataDAOManager.close(resultSet, stmt, con);
            }
            return resultList;
    }


    public List<DFSDataCount> fetchConsentCounts(LocalDate startDate,LocalDate endDate) {

            ResultSet resultSet = null;
            PreparedStatement stmt = null;
            Connection con = null;
            List<DFSDataCount> resultList = new ArrayList<>();

            try {
                stmt = dfsDataDAOManager.getStatement(
                        "SELECT  request_status,COUNT(request_status) " +
                                "FROM fip_data_request_can " +
                                "WHERE DATE(request_timestamp) >= ? AND DATE(request_timestamp) <= ? " +
                                "GROUP BY request_status;"
                );
                con = stmt.getConnection();

                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));

                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    DFSDataCount requestDataCount = new DFSDataCount(
                            resultSet.getString(1),
                            resultSet.getString(2)
                    );
                    resultList.add(requestDataCount);
                }
            } catch (SQLException e) {
                logger.error("Exception occurred while fetching request_status data: {}", e.getMessage());
                throw new RuntimeException(e);
            } finally {
                dfsDataDAOManager.close(resultSet, stmt, con);
            }
            return resultList;
    }

    public Map<String, Double> fetchAvgTime(LocalDate startDate, LocalDate endDate) {
             Map<String, Double> resultMap = new HashMap<>();

            ResultSet resultSet = null;
            PreparedStatement stmt = null;
            Connection con = null;

            try {
                stmt = dfsDataDAOManager.getStatement("SELECT fip_fi_notification_can.data_consumer_id, ROUND(AVG(UNIX_TIMESTAMP(fip_fi_notification_can.request_timestamp) - UNIX_TIMESTAMP(fip_data_request_can.request_timestamp)), 2) AS average_time_difference_seconds FROM fip_fi_notification_can INNER JOIN fip_data_request_can ON fip_fi_notification_can.session_id = fip_data_request_can.request_session_id WHERE fip_fi_notification_can.request_timestamp >= ? AND fip_fi_notification_can.request_timestamp < ? GROUP BY fip_fi_notification_can.data_consumer_id;");

                con = stmt.getConnection();

                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));

                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                            resultMap.put(resultSet.getString(1), resultSet.getDouble(2));
                }
            } catch (SQLException e) {
                logger.error("Exception occurred while fetching request_status data: {}", e.getMessage());
                throw new RuntimeException(e);
            } finally {
                dfsDataDAOManager.close(resultSet, stmt, con);
            }
            return resultMap;
    }

}
