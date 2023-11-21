package com.gebeya.bankAPI.Service;

import com.gebeya.bankAPI.Model.DTO.*;
import com.gebeya.bankAPI.Model.Entities.Account;


public interface AccountService {
    public ResponseModel addAccount(Account Account);

    public ResponseModel checkBalance(long accountNo);

    public ResponseModel transfer(TransferDTO transferDTO);

    public ResponseModel deposit(TransactionRequestDTOtemp transactionRequestDTO);

    public CustomerProfileByAccountDTO customerProfileExtractor(long accountNo);

    public  ResponseModel withdrawal (TransactionRequestDTOtemp transactionRequestDTOtemp);

    public ResponseModel updateAccountCustomer(long accountId, Account account);

    public TopUpResponseDTO topUp(topUpRequestDTO topup);

    public ResponseModel deleteAccountCustomer(long AccountNo);



}
