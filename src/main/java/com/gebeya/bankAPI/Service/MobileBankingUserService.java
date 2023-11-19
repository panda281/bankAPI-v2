package com.gebeya.bankAPI.Service;

import com.gebeya.bankAPI.Model.DTO.MobileBankingUsersDTO;
import com.gebeya.bankAPI.Model.DTO.ResponseModel;

public interface MobileBankingUserService {
    public ResponseModel activeMobileBanking(MobileBankingUsersDTO user);

//    public boolean login(MobileBankingUsersDTO user)
}
