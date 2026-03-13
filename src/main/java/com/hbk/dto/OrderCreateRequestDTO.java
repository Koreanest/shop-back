package com.hbk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequestDTO {

    @NotBlank
    @Size(max = 60)
    private String receiverName;

    @NotBlank
    @Size(max = 30)
    private String receiverPhone;

    @Size(max = 10)
    private String zip;

    @NotBlank
    @Size(max = 200)
    private String address1;

    @Size(max = 200)
    private String address2;

    @Size(max = 200)
    private String memo;
}