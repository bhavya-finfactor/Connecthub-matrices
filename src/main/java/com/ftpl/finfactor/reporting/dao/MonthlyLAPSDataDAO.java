package com.ftpl.finfactor.reporting.dao;

import com.ftpl.finfactor.reporting.model.LAPSDataCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
public class MonthlyLAPSDataDAO {

    private static final Logger logger = LoggerFactory.getLogger(MonthlyLAPSDataDAO.class);

    @Autowired
    private LAPSFinsenseDAOManager fsdaoManager;

    @Autowired
    private LAPSPfmDAOManager pfmdaoManager;

    public List<LAPSDataCount> fetchFinsenseStatusCount(LocalDate startDate, LocalDate endDate) {
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        Connection con = null;
        List<LAPSDataCount> resultList = new ArrayList<>();

        try {
            stmt = fsdaoManager.getStatement("select request_status, count(*) from fiu_data_request_can where request_timestamp >= ? and request_timestamp <= ? group by request_status;");
            con = stmt.getConnection();

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                LAPSDataCount lapsDataCount = new LAPSDataCount(
                        resultSet.getString(1),
                        resultSet.getString(2)
                );
                resultList.add(lapsDataCount);
            }
        } catch (SQLException e) {
            logger.error("Exception occurred while fetching Finsense request status counts.", e);
            throw new RuntimeException(e);
        } finally {
            fsdaoManager.close(resultSet, stmt, con);
        }
        return resultList;
    }

    public List<LAPSDataCount> fetchPfmStatusCount(LocalDate startDate, LocalDate endDate) {
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        Connection con = null;
        List<LAPSDataCount> resultList = new ArrayList<>();

        try {
            stmt = pfmdaoManager.getStatement("select session_status, count(*) from fiu_pfm_customer_consent_session where session_fidata_range_to >= ? and session_fidata_range_to <= ? group by session_status;");
            con = stmt.getConnection();

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                LAPSDataCount lapsDataCount = new LAPSDataCount(
                        resultSet.getString(1),
                        resultSet.getString(2)
                );

                resultList.add(lapsDataCount);
            }
        } catch (SQLException e) {
            logger.error("Exception occurred while fetching Finsense request status counts.", e);
            throw new RuntimeException(e);
        } finally {
            pfmdaoManager.close(resultSet, stmt, con);
        }
        return resultList;
    }
}
