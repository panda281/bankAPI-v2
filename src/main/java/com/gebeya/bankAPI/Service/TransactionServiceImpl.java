package com.gebeya.bankAPI.Service;

import com.gebeya.bankAPI.Exception.ErrorMessage;
import com.gebeya.bankAPI.Model.DTO.*;
import com.gebeya.bankAPI.Model.Entities.Account;
import com.gebeya.bankAPI.Model.Entities.Customer;
import com.gebeya.bankAPI.Model.Entities.Transaction;
import com.gebeya.bankAPI.Model.Enums.AccountStatus;
import com.gebeya.bankAPI.Model.Enums.ResponseCode;
import com.gebeya.bankAPI.Model.Enums.SIDE;
import com.gebeya.bankAPI.Model.Enums.TransactionCode;
import com.gebeya.bankAPI.Repository.AccountRepository;
import com.gebeya.bankAPI.Repository.CustomerRepository;
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

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository, CustomerRepository customerRepository)
    {
        this.transactionRepository=transactionRepository;
        this.accountRepository= accountRepository;
        this.customerRepository = customerRepository;
    }


    public boolean isOtpExpired(int otp){
        Optional<Transaction> existingTransaction = transactionRepository.findByOTP(otp);
        if(existingTransaction.isEmpty())
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Invalid OTP");

        LocalDateTime otpCreatedTime = existingTransaction.get().getTransactionDate();
        LocalDateTime currentTimeStamp = LocalDateTime.now();
        Duration duration = Duration.between(otpCreatedTime,currentTimeStamp);
        long minuteDifference = duration.toMinutes();
        return minuteDifference >= 30;
    }



    public int otpGenerator()
    {
        Random random = new Random();
        return random.nextInt(900000) + 100000;
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


    public Mono<String> otpSender(OtpRequestDTO requestDTO)
    {
        WebClient webClient = WebClient.create("https://sms.yegara.com/api2");
        return webClient.post()
                .uri("/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .retrieve()
                .bodyToMono(String.class);
    }



    public Transaction depositForDefaultCustomer(TransactionRequestDTO transaction)
    {
        Transaction newTransaction = new Transaction();

        if(isTheDefaultCustomerAccountExists(transaction))
        {
//            if (transaction.getTransactionCode()!=300)
//                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Invalid Request");
            Optional<Account> existingAccount = accountRepository.findById(transaction.getAccountNo());
            Account account = existingAccount.get();
            Optional<Customer> existingCustomer = customerRepository.findById(account.getCustomer().getCif());
            if(existingCustomer.isEmpty())
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer can not be found");

            Customer customerData = existingCustomer.get();
            if(customerData.getMobileNo()==null || Objects.equals(customerData.getMobileNo(), ""))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer MobileNo could not be found");


            newTransaction.setAccount(account);
            newTransaction.setAmount(transaction.getAmount());
            newTransaction.setTransactionCode(TransactionCode.Deposit);
            newTransaction.setSide(SIDE.Credit);
            newTransaction.setResponseCode(ResponseCode.pending);

            int generatedOtp = otpGenerator();
            newTransaction.setOTP(generatedOtp);
            try{
                OtpRequestDTO requestBody = otpRequestDTOSetter(generatedOtp, customerData.getMobileNo());
                Mono<String> responseBodyMono = otpSender(requestBody);
//                responseBodyMono.map(responseBody -> "Processed: " + responseBody) .subscribe(result -> log.info("Account content: {}", result));

                responseBodyMono
                        .map(responseBody -> "Processed: " + responseBody)
                        .doOnSuccess(result -> log.info("Account content: {}", result))
                        .onErrorMap(throwable -> new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during OTP sending"))
                        .subscribe();
            }
            catch(Exception ex)
            {
                throw new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR,"internal error occurred. please try again later");
            }

            transactionRepository.save(newTransaction);


        }
        else{
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer Account can not be found");
        }
        return newTransaction;
    }
    @Override
    public Transaction withdrawForDefaultCustomer(TransactionRequestDTO transaction)
    {
        Transaction newTransaction = new Transaction();

        if(isTheDefaultCustomerAccountExists(transaction))
        {
//            if (transaction.getTransactionCode()!=400)
//                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Invalid Request");
            Optional<Account> existingAccount = accountRepository.findById(transaction.getAccountNo());
            Account account = existingAccount.get();
            if(account.getBalance()<=transaction.getAmount())
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficent balance");

            Optional<Customer> existingCustomer = customerRepository.findById(account.getCustomer().getCif());
            if(existingCustomer.isEmpty())
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer can not be found");

            Customer customerData = existingCustomer.get();
            if(customerData.getMobileNo()==null || Objects.equals(customerData.getMobileNo(), ""))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer MobileNo could not be found");


            newTransaction.setAccount(account);
            newTransaction.setAmount(transaction.getAmount());
            newTransaction.setTransactionCode(TransactionCode.Withdrawal);
            newTransaction.setSide(SIDE.Debit);
            newTransaction.setResponseCode(ResponseCode.pending);
            int generatedOtp = otpGenerator();
            newTransaction.setOTP(generatedOtp);
            try{
                OtpRequestDTO requestBody = otpRequestDTOSetter(generatedOtp, customerData.getMobileNo());
                Mono<String> responseBodyMono = otpSender(requestBody);
                responseBodyMono.map(responseBody -> "Processed: " + responseBody) .subscribe(result -> log.info("Account content: {}", result));
            }
            catch(Exception ex)
            {
                throw new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR,"internal error occured. please try again later");
            }

            transactionRepository.save(newTransaction);


        }
        else{
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer Account can not be found");
        }
        return newTransaction;
    }

    public OtpRequestDTO otpRequestDTOSetter(int otp, String phoneNo)
    {
        OtpRequestDTO requestDTO = new OtpRequestDTO();
        requestDTO.setMessage(String.valueOf(otp));
        requestDTO.setPassword("z]lY3Zl)St98T9(x.d");
        requestDTO.setUsername("abinet");
        requestDTO.setTo(phoneNo);
        requestDTO.setTemplate_id("otp");

        return requestDTO;
    }

    public boolean isTheDefaultCustomerAccountExists(TransactionRequestDTO transaction)
    {
        return accountRepository.existsById(transaction.getAccountNo());
    }

    public boolean isTheMerchantCustomerAccountExists(MerchantDTO transaction)
    {
        return accountRepository.existsById(transaction.getAccountNo());
    }
    @Override
    public Transaction transfer(TransferDTO transferDTO)
    {
        if(!(accountRepository.existsById(transferDTO.getSenderAccountNo()) && accountRepository.existsById(transferDTO.getReceiverAccountNo())))
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"account could not be found");

        Account senderAccount = accountRepository.findById(transferDTO.getSenderAccountNo()).get();
        Account receiverAccount = accountRepository.findById(transferDTO.getReceiverAccountNo()).get();

        if(senderAccount.getAccountStatus().equals(AccountStatus.Blocked) || receiverAccount.getAccountStatus().equals(AccountStatus.Blocked))
            throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Account is blocked");

        if(senderAccount.getBalance()<transferDTO.getAmount())
            throw new ErrorMessage(HttpStatus.BAD_REQUEST, "Insufficient Balance");


        senderAccount.setBalance(senderAccount.getBalance() - transferDTO.getAmount());
        receiverAccount.setBalance(receiverAccount.getBalance() + transferDTO.getAmount());

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        Transaction senderTransaction = new Transaction();
        senderTransaction.setAmount(transferDTO.getAmount());
        senderTransaction.setAccount(senderAccount);
        senderTransaction.setTransactionCode(TransactionCode.Transfer);
        senderTransaction.setSide(SIDE.Debit);
        senderTransaction.setResponseCode(ResponseCode.successful);
//        Transaction receiverTransaction = new Transaction();
//        receiverTransaction.setAmount(transferDTO.getAmount());
//        receiverTransaction.setAccount(receiverAccount);
//        receiverTransaction.setTransactionCode(TransactionCode.Transfer);
//        receiverTransaction.setSide(SIDE.Credit);
//        receiverTransaction.setResponseCode(ResponseCode.successful);
//        transactionRepository.save(senderTransaction);
        transactionRepository.save(new Transaction(TransactionCode.Transfer,senderAccount,SIDE.Debit,transferDTO.getAmount(),ResponseCode.successful));
        transactionRepository.save(new Transaction(TransactionCode.Transfer,receiverAccount,SIDE.Credit,transferDTO.getAmount(),ResponseCode.successful));

        return senderTransaction;
    }
    @Override
    public Transaction depositForMerchantCustomer(MerchantDTO transaction){
        Transaction DTransaction;
        Transaction MTransaction = new Transaction();

        //check transaction code

//        if (transaction.getTransactionCode()!=300)
//            throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Invalid Request");

        //check if the OTP expired
        if(isOtpExpired(transaction.getOtp()))
            throw new ErrorMessage(HttpStatus.BAD_REQUEST, "OTP expired");

        //check if the MAccount Exists
        if(!isTheMerchantCustomerAccountExists(transaction))
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Merchant account could not be found");

        Account MerchantAccount = accountRepository.findById(transaction.getAccountNo()).get();


        //check if DAccountNo available
        if(transaction.getDefaultCustomerAccountNo()!=0)
        {

            //check if the DAccount Exists
            if(accountRepository.existsById(transaction.getDefaultCustomerAccountNo()))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Account could not be found");

            Optional<Transaction> DCustomer = transactionRepository.findByOTPAndAccount_AccountNo(transaction.getOtp(),transaction.getDefaultCustomerAccountNo());

            //check if the DAccount transaction record exists
            if(DCustomer.isEmpty())
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Invalid OTP or AccountNo");
            DTransaction = DCustomer.get();


            Account DefaultAccount = accountRepository.findById(transaction.getDefaultCustomerAccountNo()).get();

            //check if the MAccount balance is sufficient
            if(DTransaction.getAmount()>MerchantAccount.getBalance())
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficient balance");

            //update the DCustomer balance
            DefaultAccount.setBalance(DefaultAccount.getBalance()+DTransaction.getAmount());
            accountRepository.save(DefaultAccount);
            //update the DCustomer transaction
            DTransaction.setResponseCode(ResponseCode.successful);
            transactionRepository.save(DTransaction);
            //update the DCustomer History
            //TO-DO
            //*********************************************
            //update the MCustomer balance
            MerchantAccount.setBalance(MerchantAccount.getBalance()-DTransaction.getAmount());
            accountRepository.save(MerchantAccount);
            //create the MCustomer transaction
            MTransaction.setTransactionCode(TransactionCode.Withdrawal);
            MTransaction.setSide(SIDE.Debit);
            MTransaction.setResponseCode(ResponseCode.successful);
            MTransaction.setAmount(DTransaction.getAmount());
            MTransaction.setAccount(DTransaction.getAccount());
            transactionRepository.save(MTransaction);
            //update the MCustomer History
            //TO-DO
            //***********************************************
        }
        else if (transaction.getMobileNo()!=null)
        {
            Optional<Customer> existingAccount = customerRepository.findByMobileNo(transaction.getMobileNo());

            //check if the phoneNumber exists on the Customer table
            if(existingAccount.isEmpty())
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer PhoneNumber could not be found");
            log.info("Account content: {}", transaction.getOtp()+" "+transaction.getMobileNo());
            Optional<Transaction> DCustomer = transactionRepository.findByOTPAndMobileNo(transaction.getOtp(),transaction.getMobileNo());
            //check if the DAccount transaction record exists
            if(DCustomer.isEmpty())
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Invalid OTP or AccountNo");
            DTransaction = DCustomer.get();
            Account DefaultAccount = accountRepository.findById(DTransaction.getAccount().getAccountNo()).get();

            //check if the MAccount balance is sufficient
            if(DTransaction.getAmount()>MerchantAccount.getBalance())
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficient balance");

            //update the DCustomer balance
            DefaultAccount.setBalance(DefaultAccount.getBalance()+DTransaction.getAmount());
            accountRepository.save(DefaultAccount);
            //update the DCustomer transaction
            DTransaction.setResponseCode(ResponseCode.successful);
            transactionRepository.save(DTransaction);
            //update the DCustomer History
            //TO-DO
            //*********************************************
            //update the MCustomer balance
            MerchantAccount.setBalance(MerchantAccount.getBalance()-DTransaction.getAmount());
            accountRepository.save(MerchantAccount);
            //create the MCustomer transaction
            MTransaction.setTransactionCode(TransactionCode.Withdrawal);
            MTransaction.setSide(SIDE.Debit);
            MTransaction.setResponseCode(ResponseCode.successful);
            MTransaction.setAmount(DTransaction.getAmount());
            MTransaction.setAccount(DTransaction.getAccount());
            transactionRepository.save(MTransaction);
            //update the MCustomer History
            //TO-DO
            //***********************************************
        }
        else {
            throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Bad Request");
        }


        return MTransaction;
    }
    @Override
    public Transaction withdrawalForMerchantCustomer(MerchantDTO transaction){
        Transaction DTransaction;
        Transaction MTransaction = new Transaction();

        //check transaction code

//        if (transaction.getTransactionCode()!=300)
//            throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Invalid Request");

        //check if the OTP expired
        if(isOtpExpired(transaction.getOtp()))
            throw new ErrorMessage(HttpStatus.BAD_REQUEST, "OTP expired");

        //check if the MAccount Exists
        if(!isTheMerchantCustomerAccountExists(transaction))
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Merchant account could not be found");

        Account MerchantAccount = accountRepository.findById(transaction.getAccountNo()).get();


        //check if DAccountNo available
        if(transaction.getDefaultCustomerAccountNo()!=0)
        {

            //check if the DAccount Exists
            if(accountRepository.existsById(transaction.getDefaultCustomerAccountNo()))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Account could not be found");

            Optional<Transaction> DCustomer = transactionRepository.findByOTPAndAccount_AccountNo(transaction.getOtp(),transaction.getDefaultCustomerAccountNo());

            //check if the DAccount transaction record exists
            if(DCustomer.isEmpty())
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Invalid OTP or AccountNo");
            DTransaction = DCustomer.get();


            Account DefaultAccount = accountRepository.findById(transaction.getDefaultCustomerAccountNo()).get();

            //check if the MAccount balance is sufficient
            if(DefaultAccount.getBalance()>DTransaction.getAmount())
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficient balance");

            //update the DCustomer balance
            DefaultAccount.setBalance(DefaultAccount.getBalance()- DTransaction.getAmount());
            accountRepository.save(DefaultAccount);
            //update the DCustomer transaction
            DTransaction.setResponseCode(ResponseCode.successful);
            transactionRepository.save(DTransaction);
            //update the DCustomer History
            //TO-DO
            //*********************************************
            //update the MCustomer balance
            MerchantAccount.setBalance(MerchantAccount.getBalance()+ DTransaction.getAmount());
            accountRepository.save(MerchantAccount);
            //create the MCustomer transaction
            MTransaction.setTransactionCode(TransactionCode.Deposit);
            MTransaction.setSide(SIDE.Credit);
            MTransaction.setResponseCode(ResponseCode.successful);
            MTransaction.setAmount(MTransaction.getAmount() + DTransaction.getAmount());
            MTransaction.setAccount(DTransaction.getAccount());
            transactionRepository.save(MTransaction);
            //update the MCustomer History
            //TO-DO
            //***********************************************
        }
        else if (transaction.getMobileNo()!=null)
        {
            Optional<Customer> existingAccount = customerRepository.findByMobileNo(transaction.getMobileNo());

            //check if the phoneNumber exists on the Customer table
            if(existingAccount.isEmpty())
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer PhoneNumber could not be found");

            Optional<Transaction> DCustomer = transactionRepository.findByOTPAndMobileNo(transaction.getOtp(),transaction.getMobileNo());
            //check if the DAccount transaction record exists
            if(DCustomer.isEmpty())
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Invalid OTP or AccountNo");
            DTransaction = DCustomer.get();
            Account DefaultAccount = accountRepository.findById(DTransaction.getAccount().getAccountNo()).get();

            //check if the MAccount balance is sufficient
            if(DefaultAccount.getBalance()>DTransaction.getAmount())
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficient balance");

            //update the DCustomer balance
            DefaultAccount.setBalance(DefaultAccount.getBalance()- DTransaction.getAmount());
            accountRepository.save(DefaultAccount);
            //update the DCustomer transaction
            DTransaction.setResponseCode(ResponseCode.successful);
            transactionRepository.save(DTransaction);
            //update the DCustomer History
            //TO-DO
            //*********************************************
            //update the MCustomer balance
            MerchantAccount.setBalance(MerchantAccount.getBalance()+ DTransaction.getAmount());
            accountRepository.save(MerchantAccount);
            //create the MCustomer transaction
            MTransaction.setTransactionCode(TransactionCode.Deposit);
            MTransaction.setSide(SIDE.Credit);
            MTransaction.setResponseCode(ResponseCode.successful);
            MTransaction.setAmount(MTransaction.getAmount() + DTransaction.getAmount());
            MTransaction.setAccount(DTransaction.getAccount());
            transactionRepository.save(MTransaction);
            //update the MCustomer History
            //TO-DO
            //***********************************************
        }
        else {
            throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Bad Request");
        }


        return MTransaction;
    }

    @Override
    public List<ShortStatementDTO> shortStatement(long accountNo)
    {
        if(!accountRepository.existsById(accountNo))
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Account could not be found");
        return transactionRepository.findFirst5ByAccount_AccountNoOrderByTransactionDate(accountNo);
    }



}
