package com.gebeya.bankAPI.Service;

import com.gebeya.bankAPI.Exception.ErrorMessage;
import com.gebeya.bankAPI.Model.DTO.*;
import com.gebeya.bankAPI.Model.Entities.Account;
import com.gebeya.bankAPI.Model.Entities.Customer;
import com.gebeya.bankAPI.Model.Entities.History;
import com.gebeya.bankAPI.Model.Entities.Transaction;
import com.gebeya.bankAPI.Model.Enums.*;
import com.gebeya.bankAPI.Repository.AccountRepository;
import com.gebeya.bankAPI.Repository.CustomerRepository;
import com.gebeya.bankAPI.Repository.HistoryRepository;
import com.gebeya.bankAPI.Repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService{

    AccountRepository accountRepository;
    CustomerRepository customerRepository;
    TransactionRepository transactionRepository;
    HistoryRepository historyRepository;
    WebClient webClientForOtp;

    WebClient webClientForTopUp;


    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, CustomerRepository customerRepository, TransactionRepository transactionRepository, HistoryRepository historyRepository, WebClient webClientForOtp,WebClient webClientForTopUp)
    {
        this.accountRepository=accountRepository;
        this.customerRepository=customerRepository;
        this.transactionRepository=transactionRepository;
        this.historyRepository=historyRepository;
        this.webClientForOtp =webClientForOtp;
        this.webClientForTopUp = webClientForTopUp;
    }

    @Override
    public ResponseModel addAccount(Account account){
        Customer customer = new Customer(account.getCustomer());
        Customer createdCustomer = customerRepository.save(customer);
        Account newAccount = new Account(account);
        newAccount.setAccountStatus(AccountStatus.Active);
        newAccount.setCustomer(createdCustomer);
        accountRepository.save(newAccount);
        return new ResponseModel(true, "Account created successfully");
    }

    @Override
    public ResponseModel updateAccountCustomer(long accountId, Account account){
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isEmpty())
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Account not found");

            Account existingAccount = optionalAccount.get();

            existingAccount.setBalance(account.getBalance());
            existingAccount.setAccountStatus(account.getAccountStatus());
            existingAccount.setCustomer(account.getCustomer());



            accountRepository.save(existingAccount);

            return new ResponseModel(true, "Account and associated customer updated successfully");

    }

    @Override
    public OperationResult<String> checkBalance(long accountNo){
        if(!accountRepository.existsById(accountNo))
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"invalid AccountNo");

        double amount = accountRepository.findById(accountNo).get().getBalance();
        CustomerProfileByAccountDTO  customerProfileByAccountDTO = customerProfileExtractor(accountNo);
        historyRepository.save(new History(TransactionCode.BalanceInquiry,accountRepository.findById(accountNo).get(),amount,ResponseCode.successful,customerProfileByAccountDTO.getMobileNo()));
        return new OperationResult<>(true,"the current amount is "+amount,String.valueOf(amount));
    }

    @Override
    public ResponseModel transfer(TransferDTO transferDTO)
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

        transactionRepository.save(new Transaction(TransactionCode.Transfer,senderAccount, SIDE.Debit,transferDTO.getAmount(), ResponseCode.successful));
        transactionRepository.save(new Transaction(TransactionCode.Transfer,receiverAccount,SIDE.Credit,transferDTO.getAmount(),ResponseCode.successful));

        CustomerProfileByAccountDTO  senderMobileNo = customerProfileExtractor(senderAccount.getAccountNo());
        CustomerProfileByAccountDTO  receiverMobileNo = customerProfileExtractor(receiverAccount.getAccountNo());

        History senderHistory = historySaver(TransactionCode.Transfer,senderAccount,SIDE.Debit, senderAccount.getBalance(), ResponseCode.successful,senderMobileNo.getMobileNo());
        historyRepository.save(senderHistory);

        History receiverHistory = historySaver(TransactionCode.Transfer,receiverAccount,SIDE.Credit, receiverAccount.getBalance(), ResponseCode.successful,receiverMobileNo.getMobileNo());
        historyRepository.save(receiverHistory);

        return new ResponseModel(true,"transfer completed successfully");
    }

    public CustomerProfileByAccountDTO customerProfileExtractor(long accountNo){
        return new CustomerProfileByAccountDTO(customerRepository.customerProfileExtractor(accountNo).get());
    }

    public History historySaver(TransactionCode transactionCode, Account account, SIDE side, double amount, ResponseCode responseCode, String mobileNo)
    {
        History history = new History();
        history.setTransactionCode(transactionCode);
        history.setAccount(account);
        history.setSide(side);
        history.setAmount(amount);
        history.setResponseCode(responseCode);
        history.setPhoneNo(mobileNo);
        return history;
    }


    public Transaction transactionSaverForDepositWithdrawal(TransactionCode transactionCode, Account account, SIDE side, double amount, ResponseCode responseCode, int otp)
    {
        return new Transaction(transactionCode,account,side,amount,responseCode,otp);
    }

    private int otpGenerator()
    {
        Random random = new Random();
        return random.nextInt(900000) + 100000;
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

    @Override
    public ResponseModel deposit(TransactionRequestDTOtemp request)
    {
        if(request.getmAccountNo()==0)
        {

            if(!isTheAccountExists(request.getdAccountNo()))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer Account can not be found");

            CustomerProfileByAccountDTO customerProfileByAccountDTO = customerProfileExtractor(request.getdAccountNo());
            if(customerProfileByAccountDTO.getCustomerProfile()!= CustomerProfile.Default)
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"bad request");

            //then it is default customer deposit
            Account account = accountRepository.findById(request.getdAccountNo()).get();
            Customer customerData = customerRepository.findById(customerProfileByAccountDTO.getCif()).get();

            if(customerData.getMobileNo()==null || Objects.equals(customerData.getMobileNo(), ""))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer MobileNo could not be found");

            int generatedOtp = otpGenerator();
            transactionRepository.save(transactionSaverForDepositWithdrawal(TransactionCode.Deposit,account,SIDE.Credit, request.getAmount(), ResponseCode.pending,generatedOtp));
            otpHandler(generatedOtp,customerData.getMobileNo());
            historyRepository.save(new History(TransactionCode.Deposit,account,SIDE.Credit,request.getAmount(),ResponseCode.pending,customerProfileByAccountDTO.getMobileNo()));
            return new ResponseModel(true,"operation completed successfully");
            //TO-DO historyRepository.save()
            //TO-DO return ResponseModel
        }
        else
        {


            //common
            if(isOtpExpired(request.getOtp()))
                throw new ErrorMessage(HttpStatus.BAD_REQUEST, "OTP expired");
            //common
            if(!isTheAccountExists(request.getmAccountNo()))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer Account can not be found");
            //common
            CustomerProfileByAccountDTO McustomerProfileByAccountDTO = customerProfileExtractor(request.getmAccountNo());
            if(McustomerProfileByAccountDTO.getCustomerProfile()!=CustomerProfile.Merchant)
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"bad request");
            //common
            Account MerchantAccount = accountRepository.findById(request.getmAccountNo()).get();

            if(request.getMobileNo()!=null)
            {
                Optional<Customer> existingAccount = customerRepository.findByMobileNo(request.getMobileNo());
                //not common
                if(existingAccount.isEmpty())
                    throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer PhoneNumber could not be found");

                Optional<Transaction> DCustomer = transactionRepository.findByOTPAndMobileNo(request.getOtp(),request.getMobileNo());
                if(DCustomer.isEmpty())
                    throw new ErrorMessage(HttpStatus.NOT_FOUND,"Invalid OTP or MobileNo");
                Transaction DTransaction = DCustomer.get();
                Account DefaultAccount = accountRepository.findById(DTransaction.getAccount().getAccountNo()).get();

                if(DTransaction.getAmount()>MerchantAccount.getBalance())
                    throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficient balance");

                DefaultAccount.setBalance(DefaultAccount.getBalance()+DTransaction.getAmount());
                accountRepository.save(DefaultAccount);

                DTransaction.setResponseCode(ResponseCode.successful);
                transactionRepository.save(DTransaction);
                //update the DCustomer History
                //TO-DO
                MerchantAccount.setBalance(MerchantAccount.getBalance()-DTransaction.getAmount());
                accountRepository.save(MerchantAccount);
                transactionRepository.save(new Transaction(TransactionCode.Withdrawal,MerchantAccount,SIDE.Debit, DTransaction.getAmount(), ResponseCode.successful));
                //update the MCustomer History
                //TO-DO
            }
            else if(request.getdAccountNo()!=0)
            {
                if(!isTheAccountExists(request.getdAccountNo()))
                    throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer Account can not be found");

                Optional<Transaction> DCustomer = transactionRepository.findByOTPAndAccount_AccountNo(request.getOtp(),request.getdAccountNo());
                if(DCustomer.isEmpty())
                    throw new ErrorMessage(HttpStatus.NOT_FOUND,"Invalid OTP or AccountNo");

                Transaction DTransaction = DCustomer.get();
                Account DefaultAccount = accountRepository.findById(request.getdAccountNo()).get();

                if(DTransaction.getAmount()>MerchantAccount.getBalance())
                    throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficient balance");

                DefaultAccount.setBalance(DefaultAccount.getBalance()+DTransaction.getAmount());
                accountRepository.save(DefaultAccount);

                DTransaction.setResponseCode(ResponseCode.successful);
                transactionRepository.save(DTransaction);

                //update the DCustomer History
                //TO-DO

                MerchantAccount.setBalance(MerchantAccount.getBalance()-DTransaction.getAmount());
                accountRepository.save(MerchantAccount);

                transactionRepository.save(new Transaction(TransactionCode.Withdrawal,MerchantAccount,SIDE.Debit,DTransaction.getAmount(),ResponseCode.successful));
                //update the MCustomer History
                //TO-DO
            }

        }

        return new ResponseModel(true,"operation completed successfully");
    }

    public  ResponseModel withdrawal (TransactionRequestDTOtemp request)
    {
        if(request.getmAccountNo()==0)
        {

            if(!isTheAccountExists(request.getdAccountNo()))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer Account can not be found");

            CustomerProfileByAccountDTO customerProfileByAccountDTO = customerProfileExtractor(request.getdAccountNo());
            if(customerProfileByAccountDTO.getCustomerProfile()!= CustomerProfile.Default)
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"bad request");

            //then it is default customer deposit
            Account account = accountRepository.findById(request.getdAccountNo()).get();
            Customer customerData = customerRepository.findById(customerProfileByAccountDTO.getCif()).get();

            if(account.getBalance()<=request.getAmount())
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficient balance");

            if(customerData.getMobileNo()==null || Objects.equals(customerData.getMobileNo(), ""))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer MobileNo could not be found");

            int generatedOtp = otpGenerator();
            transactionRepository.save(transactionSaverForDepositWithdrawal(TransactionCode.Withdrawal,account,SIDE.Debit, request.getAmount(), ResponseCode.pending,generatedOtp));
            otpHandler(generatedOtp,customerData.getMobileNo());

            //TO-DO historyRepository.save()
            //TO-DO return ResponseModel
        }
        else{

            //common
            if(isOtpExpired(request.getOtp()))
                throw new ErrorMessage(HttpStatus.BAD_REQUEST, "OTP expired");
            //common
            if(!isTheAccountExists(request.getmAccountNo()))
                throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer Account can not be found");
            //common
            CustomerProfileByAccountDTO McustomerProfileByAccountDTO = customerProfileExtractor(request.getmAccountNo());
            if(McustomerProfileByAccountDTO.getCustomerProfile()!=CustomerProfile.Merchant)
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"bad request");
            //common
            Account MerchantAccount = accountRepository.findById(request.getmAccountNo()).get();

            if(request.getMobileNo()!=null)
            {
                Optional<Customer> existingAccount = customerRepository.findByMobileNo(request.getMobileNo());
                //not common
                if(existingAccount.isEmpty())
                    throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer PhoneNumber could not be found");

                Optional<Transaction> DCustomer = transactionRepository.findByOTPAndMobileNo(request.getOtp(),request.getMobileNo());
                if(DCustomer.isEmpty())
                    throw new ErrorMessage(HttpStatus.NOT_FOUND,"Invalid OTP or MobileNo");
                Transaction DTransaction = DCustomer.get();
                Account DefaultAccount = accountRepository.findById(DTransaction.getAccount().getAccountNo()).get();

                if(DTransaction.getAmount()>DefaultAccount.getBalance())
                    throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficient balance");

                DefaultAccount.setBalance(DefaultAccount.getBalance()-DTransaction.getAmount());
                accountRepository.save(DefaultAccount);

                DTransaction.setResponseCode(ResponseCode.successful);
                transactionRepository.save(DTransaction);
                //update the DCustomer History
                //TO-DO
                MerchantAccount.setBalance(MerchantAccount.getBalance()+DTransaction.getAmount());
                accountRepository.save(MerchantAccount);
                transactionRepository.save(new Transaction(TransactionCode.Deposit,MerchantAccount,SIDE.Credit, DTransaction.getAmount(), ResponseCode.successful));
                //update the MCustomer History
                //TO-DO
            }
            else if(request.getdAccountNo()!=0)
            {
                if(!isTheAccountExists(request.getdAccountNo()))
                    throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer Account can not be found");

                Optional<Transaction> DCustomer = transactionRepository.findByOTPAndAccount_AccountNo(request.getOtp(),request.getdAccountNo());
                if(DCustomer.isEmpty())
                    throw new ErrorMessage(HttpStatus.NOT_FOUND,"Invalid OTP or AccountNo");

                Transaction DTransaction = DCustomer.get();
                Account DefaultAccount = accountRepository.findById(request.getdAccountNo()).get();

                if(DTransaction.getAmount()>DefaultAccount.getBalance())
                    throw new ErrorMessage(HttpStatus.BAD_REQUEST,"Insufficient balance");

                DefaultAccount.setBalance(DefaultAccount.getBalance()-DTransaction.getAmount());
                accountRepository.save(DefaultAccount);

                DTransaction.setResponseCode(ResponseCode.successful);
                transactionRepository.save(DTransaction);

                //update the DCustomer History
                //TO-DO

                MerchantAccount.setBalance(MerchantAccount.getBalance()+DTransaction.getAmount());
                accountRepository.save(MerchantAccount);

                transactionRepository.save(new Transaction(TransactionCode.Deposit,MerchantAccount,SIDE.Credit,DTransaction.getAmount(),ResponseCode.successful));
                //update the MCustomer History
                //TO-DO
            }
            else {
                throw new ErrorMessage(HttpStatus.BAD_REQUEST,"unknown error");
            }
        }
        return new ResponseModel(true,"operation completed successfully");
    }

    private void otpHandler(int otp, String mobileNo)
    {
        OtpRequestDTO requestBody = otpRequestDTOSetter(otp, mobileNo);
        Mono<String> responseBodyMono = otpHandler(requestBody);
        responseBodyMono
                .map(responseBody -> "Processed: " + responseBody)
                .doOnSuccess(result -> log.info("Account content: {}", result))
                .onErrorMap(throwable -> new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during OTP sending"))
                .subscribe();
    }
    private boolean isTheAccountExists(long accountNo)
    {
        return accountRepository.existsById(accountNo);
    }
    private Mono<String> otpHandler(OtpRequestDTO requestDTO)
    {
        return webClientForOtp.post()
                .uri("/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .retrieve()
                .bodyToMono(String.class);
    }

    private OtpRequestDTO otpRequestDTOSetter(int otp, String phoneNo)
    {
       return new OtpRequestDTO("abinet","z]lY3Zl)St98T9(x.d",phoneNo,String.valueOf(otp),"otp");
    }

@Override
    public TopUpResponseDTO topUp(topUpRequestDTO topup)
    {
        if(!isTheAccountExists(topup.getAccountNo()))
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"user account number could not be found");
        Account account = accountRepository.findById(topup.getAccountNo()).get();
        if(account.getBalance()<topup.getAmount())
            throw new ErrorMessage(HttpStatus.BAD_REQUEST, "Insufficient balance");
        TopUpResponseDTO returnedResponse = topUpfetch(topup.getAmount());
        account.setBalance(account.getBalance()-topup.getAmount());
        accountRepository.save(account);
        return returnedResponse;


    }
    // update the account table when the user topUp;

    public TopUpResponseDTO topUpfetch(int paramValue)
    {
        TopUpResponseDTO jsonResponse = webClientForTopUp.get()
                .uri("/topup.php/"+paramValue)
                .retrieve()
                .onStatus(status ->status.is4xxClientError(), response->{
                    throw new ErrorMessage(HttpStatus.BAD_REQUEST,"bad request occurred");
                })
                .onStatus(status -> status.is5xxServerError(), response ->{
                    throw new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR,"unknown server error occurred. please try again later");
                })
                .bodyToMono(TopUpResponseDTO.class)
                .block();

        return jsonResponse;
    }


}
