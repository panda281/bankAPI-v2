package com.gebeya.bankAPI.Service;

import com.gebeya.bankAPI.Exception.ErrorMessage;
import com.gebeya.bankAPI.Model.DTO.*;
import com.gebeya.bankAPI.Model.Entities.Account;
import com.gebeya.bankAPI.Model.Entities.Customer;
import com.gebeya.bankAPI.Model.Entities.History;
import com.gebeya.bankAPI.Model.Entities.Transaction;
import com.gebeya.bankAPI.Model.Enums.AccountStatus;
import com.gebeya.bankAPI.Model.Enums.ResponseCode;
import com.gebeya.bankAPI.Model.Enums.SIDE;
import com.gebeya.bankAPI.Model.Enums.TransactionCode;
import com.gebeya.bankAPI.Repository.AccountRepository;
import com.gebeya.bankAPI.Repository.CustomerRepository;
import com.gebeya.bankAPI.Repository.HistoryRepository;
import com.gebeya.bankAPI.Repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static reactor.core.publisher.Signal.subscribe;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService{

    TransactionRepository transactionRepository;
    AccountRepository accountRepository;
    CustomerRepository customerRepository;
    AccountService accountService;
    HistoryRepository historyRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository, CustomerRepository customerRepository, AccountService accountService, HistoryRepository historyRepository)
    {
        this.transactionRepository=transactionRepository;
        this.accountRepository= accountRepository;
        this.customerRepository = customerRepository;
        this.accountService=accountService;
        this.historyRepository = historyRepository;
    }

    public Transaction addTransaction(Transaction transaction)
    {
        return transactionRepository.save(transaction);
    }

    @Override
    @Scheduled(fixedRate = 60000)

    public void autoCheckOTPExpiration()
    {
        LocalDateTime currentTime = LocalDateTime.now();
        log.info("Account content: {}", "scheduler is working...");
        List<Transaction> transactionList = transactionRepository.findAllByResponseCode(ResponseCode.pending);

        for(Transaction transaction: transactionList)
        {
            LocalDateTime otpCreationTime = transaction.getTransactionDate();
            Duration duration = Duration.between(otpCreationTime,currentTime);
            long minuteDifference = duration.toMinutes();
            if(minuteDifference>30){
                transaction.setResponseCode(ResponseCode.failed);
                transactionRepository.save(transaction);
            }

        }

    }









    @Override
    public List<ShortStatementDTO> shortStatement(long accountNo)
    {
        if(!accountRepository.existsById(accountNo))
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Account could not be found");
            String phoneNo = accountService.customerProfileExtractor(accountNo).getMobileNo();
        historyRepository.save(new History(phoneNo,ResponseCode.successful,TransactionCode.ShortStatement,accountRepository.findById(accountNo).get()));
        List<ShortStatementDTO> shortStatements = transactionRepository.findFirst5ByAccount_AccountNoOrderByTransactionDate(accountNo);
        if(shortStatements.isEmpty())
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Transaction is empty");
        return transactionRepository.findFirst5ByAccount_AccountNoOrderByTransactionDate(accountNo);
    }





}
