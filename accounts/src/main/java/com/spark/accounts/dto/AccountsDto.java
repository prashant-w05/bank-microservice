package com.spark.accounts.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AccountsDto {

    @NotNull(message = "AccountNumber can not be a null or empty")
    @Min(value = 10, message = "AccountNumber must be 10 digits")
    //@Max(value = 10, message = "AccountNumber must be 10 digits")
    //@Pattern(regexp="(^$|[0-9]{10})",message = "AccountNumber must be 10 digits")
    private Long accountNumber;

    @NotEmpty(message = "AccountType can not be a null or empty")
    private String accountType;

    @NotEmpty(message = "BranchAddress can not be a null or empty")
    private String branchAddress;

}
