package com.conversor.monedas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesResponse {
    private String from;
    private String to;
    private List<TimeSeriesPointDto> points;
}
