package com.ftpl.finfactor.reporting.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LAPSDataCount {
    private String Status;
    private String count;

    @Override
    public String toString() {
        return "LAPSDataCount{" +
                "requestStatus='" + Status + '\'' +
                ", count='" + count + '\'' +
                '}';
    }
}
