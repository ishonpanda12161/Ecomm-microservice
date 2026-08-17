package com.ecommerce.app.payload;

import lombok.Data;

import java.util.List;

@Data
public class UserSearchResponseDTO {

    public List<UserResponseDTO> userResponseDTOS;
    private Integer pageNum;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;

}
