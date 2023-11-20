package com.gebeya.bankAPI.Controller;


import com.gebeya.bankAPI.Model.DTO.*;
import com.gebeya.bankAPI.Model.Entities.Account;
import com.gebeya.bankAPI.Repository.HistoryRepository;
import com.gebeya.bankAPI.Service.AccountService;
import com.gebeya.bankAPI.Service.MobileBankingUserService;
import com.gebeya.bankAPI.Service.TransactionService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@Slf4j
@RequestMapping("/api/v2")
public class AccountController {


    @Autowired
    HistoryRepository historyRepository;

    AccountService accountService;
    TransactionService transactionService;
    MobileBankingUserService mobileBankingUserService;

    ResourceBundleMessageSource messageSource;

    @Autowired
    public AccountController(AccountService accountService, TransactionService transactionService, MobileBankingUserService mobileBankingUserService, ResourceBundleMessageSource messageSource)
    {
        this.accountService=accountService;
        this.transactionService=transactionService;
        this.mobileBankingUserService = mobileBankingUserService;
        this.messageSource=messageSource;
    }

    //it works
    @PostMapping("/account")


    public ResponseEntity<?> postAccount (@RequestBody Account account){
//        log.info("Account content: {}", account);
//        Account createdAccount = ;
        return ResponseEntity.ok(accountService.addAccount(account));
    }


    @PutMapping("/account")
    public ResponseEntity<?>updateAccount(@RequestParam long accountNo ,@RequestBody Account account){
        return ResponseEntity.ok(accountService.updateAccountCustomer(accountNo,account));
    }

//        @PostMapping("/account")
//    public ResponseEntity<Account> postAccount (){
//            Account senderAccount = accountRepository.findById(10000004L).get();
//                senderAccount.setBalance(50);
//
//        return ResponseEntity.ok(accountRepository.save(senderAccount));
//    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "when operation completed successfully"),
            @ApiResponse(responseCode = "400", description = "when OTP expire"),
            @ApiResponse(responseCode = "400", description = "when bad request happens"),
            @ApiResponse(responseCode = "400", description = "when the balance is insufficient"),
            @ApiResponse(responseCode = "404", description = "when a customer account can not be found"),
            @ApiResponse(responseCode = "404", description = "when a customer mobile could not be found"),
            @ApiResponse(responseCode = "404", description = "when invalid otp or mobileNo inserted"),
            @ApiResponse(responseCode = "404", description = "when invalid otp or accountNo inserted"),
            @ApiResponse(responseCode = "500", description = "when internal error occurred"),

    })
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody TransactionRequestDTOtemp t){
        log.info("Account content: {}", t);
        return ResponseEntity.ok(accountService.deposit(t));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "when an operation complete successfully"),
            @ApiResponse(responseCode = "400", description = "when OTP expire"),
            @ApiResponse(responseCode = "400", description = "when bad request happens"),
            @ApiResponse(responseCode = "400", description = "when unknown error occured"),
            @ApiResponse(responseCode = "400", description = "when the balance is insufficient"),
            @ApiResponse(responseCode = "404", description = "when a customer account can not be found"),
            @ApiResponse(responseCode = "404", description = "when a customer mobile could not be found"),
            @ApiResponse(responseCode = "404", description = "when invalid otp or mobileNo inserted"),
            @ApiResponse(responseCode = "404", description = "when invalid otp or accountNo inserted"),
            @ApiResponse(responseCode = "500", description = "when internal error occurred"),

    })
    @PostMapping("/withdrawal")
    public ResponseEntity<?> withdrawal(@RequestBody TransactionRequestDTOtemp transaction){
        log.info("Account content: {}", transaction);
        return ResponseEntity.ok(accountService.withdrawal(transaction));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferDTO transferDTO){
        ResponseModel responseModel = accountService.transfer(transferDTO);
        return ResponseEntity.ok(responseModel);
    }




    @GetMapping("/ShortStatement")
    public ResponseEntity<?> statement(@RequestParam long id)
    {
        List<ShortStatementDTO> shortStatementDTO = transactionService.shortStatement(id);
        return ResponseEntity.ok(shortStatementDTO);
    }



    @GetMapping("/CheckBalance")
    public ResponseEntity<?> checkBalance(@RequestParam long accountNo){
        return ResponseEntity.ok(accountService.checkBalance(accountNo));
    }

    @PostMapping("/activeMobileBanking")
    public ResponseEntity<?> activateMobileBanking(@RequestBody MobileBankingUsersDTO dto)
    {
        return ResponseEntity.ok(mobileBankingUserService.activeMobileBanking(dto));
    }

    @GetMapping("/changelanguage")
    public String[] changelanguage(@RequestHeader("Accept-Language") String locale) {
        Locale locale1 = new Locale(locale);
        String[] messages = new String[8];
        for(int i = 0;i<messages.length;i++)
        {
            String messageKey = "message" + i;
            messages[i] =messageSource.getMessage(messageKey,null,locale1);
        }
        return messages;
    }

    @PostMapping("/topup")
    public ResponseEntity<?> airtime(@RequestBody topUpRequestDTO top){
        TopUpResponseDTO topup = accountService.topUp(top);
        return ResponseEntity.ok(topup);
    }

//    @GetMapping("/test/{id}")
//    public ResponseEntity<?> test (@PathVariable("id") int id)
//    {
//        return ResponseEntity.ok(historyRepository.findByRrn(id));
//    }

}
