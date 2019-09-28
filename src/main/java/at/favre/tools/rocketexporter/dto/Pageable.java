package at.favre.tools.rocketexporter.dto;

import lombok.Data;

@Data
class Pageable {
    private String offset;
    private String count;
    private String total;
    private String success;
}
