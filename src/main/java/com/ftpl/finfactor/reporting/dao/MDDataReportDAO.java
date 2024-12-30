package com.ftpl.finfactor.reporting.dao;

import com.ftpl.finfactor.reporting.dao.manager.MonthlyMDDataDAOManager;
import com.ftpl.finfactor.reporting.model.MDDataCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class MDDataReportDAO {

    private static final Logger logger = LoggerFactory.getLogger(MDDataReportDAO.class);

    @Autowired
    private MonthlyMDDataDAOManager mdDataDaoManager;

    public List<MDDataCount> fetchMDData(LocalDate startDate, LocalDate endDate){
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        Connection con = null;
        List<MDDataCount> resultList = new ArrayList<>();

        try {
            stmt = mdDataDaoManager.getStatement("select request_status, count(request_status)  from fip_data_request_can where DATE(request_timestamp) >= ? and DATE(request_timestamp) <= ? group by request_status;");
            con = stmt.getConnection();

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                MDDataCount mdDataCount = new MDDataCount(
                        resultSet.getString(1),
                        resultSet.getString(2)
                );
                resultList.add(mdDataCount);
            }
        } catch (SQLException e) {
            logger.error("Exception occurred while fetching MD Data report. {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            mdDataDaoManager.close(resultSet, stmt, con);
        }
        return resultList;
    }
}
