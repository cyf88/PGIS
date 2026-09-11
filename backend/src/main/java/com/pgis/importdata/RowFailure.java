package com.pgis.importdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RowFailure {
    private String file;
    private long line;
    private String reason;
}
