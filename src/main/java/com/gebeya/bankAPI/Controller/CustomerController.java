package com.gebeya.bankAPI.Controller;

import com.gebeya.bankAPI.Model.Entities.Customer;
import com.gebeya.bankAPI.Model.Entities.Transaction;
import com.gebeya.bankAPI.Repository.CustomerRepository;
import com.gebeya.bankAPI.Repository.TransactionRepository;
import com.gebeya.bankAPI.Service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2")
public class CustomerController {
    CustomerService customerService;



    @Autowired
    public CustomerController(CustomerService customerService){
        this.customerService=customerService;
    }


//    @PostMapping("/customer")
//    public ResponseEntity<Customer> postCustomer(@RequestBody Customer customer)
//    {
//        Customer createdUser = customerService.addCustomer(customer);
//        return ResponseEntity.ok(createdUser);
//    }
//    @GetMapping("/")
//    public ResponseEntity<?> test()
//    {
//        Transaction x = transactionRepository.findByOTPAndMobileNo(807110,"0978904679").get();
//        return ResponseEntity.ok(x);
//    }

    @GetMapping("/customer")
    public ResponseEntity<Iterable<Customer>> listAllCustomer (){
        return ResponseEntity.ok(customerService.getAllCustomer());
    }
}
