package com.ftpl.finfactor.reporting.dao;

import com.ftpl.finfactor.reporting.Model.LAPSDataCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

    public List<LAPSDataCount> fetchFinsenseStatusCount(LocalDate startDate, LocalDate endDate) {
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        Connection con = null;
        List<LAPSDataCount> resultList = new ArrayList<>();

        try {
            stmt = daoManager.getStatement("query.finsense.data.fetch",daoManager.finsenseDataSourceConnection);
            con = stmt.getConnection();

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                LAPSDataCount lapsDataCount = new LAPSDataCount();

                lapsDataCount.setStatus(resultSet.getString(1));
                lapsDataCount.setCount(resultSet.getString(2));

                resultList.add(lapsDataCount);
            }
        } catch (SQLException e) {
            logger.error("Exception occurred while fetching Finsense request status counts.", e);
            throw new RuntimeException(e);
        } finally {
            daoManager.close(resultSet, stmt, con);
        }
        return resultList;
    }

    public List<LAPSDataCount> fetchPfmStatusCount(LocalDate startDate, LocalDate endDate) {
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        Connection con = null;
        List<LAPSDataCount> resultList = new ArrayList<>();

        try {
            stmt = daoManager.getStatement("query.pfm.data.fetch", daoManager.pfmDataSource);
            con = stmt.getConnection();

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                LAPSDataCount lapsDataCount = new LAPSDataCount();

                lapsDataCount.setStatus(resultSet.getString(1));
                lapsDataCount.setCount(resultSet.getString(2));

                resultList.add(lapsDataCount);
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
