package com.gebeya.bankAPI.Controller;

import com.gebeya.bankAPI.Model.Entities.Customer;
import com.gebeya.bankAPI.Model.Entities.Transaction;
import com.gebeya.bankAPI.Repository.CustomerRepository;
import com.gebeya.bankAPI.Repository.TransactionRepository;
import com.gebeya.bankAPI.Service.CustomerService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "return body"),
            @ApiResponse(responseCode = "404", description = "when customers could not be found"),
            @ApiResponse(responseCode = "500", description = "when internal error occurred"),

    })
    @GetMapping("/customer")
    public ResponseEntity<Iterable<Customer>> listAllCustomer (){
        return ResponseEntity.ok(customerService.getAllCustomer());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "return body"),
            @ApiResponse(responseCode = "404", description = "when customerID could not be found"),
            @ApiResponse(responseCode = "500", description = "when internal error occurred"),

    })

    @GetMapping("/customer/{id}")
    public ResponseEntity<Optional<Customer>> findCustomerById (@PathVariable("id") long id){
        return ResponseEntity.ok(customerService.findCustomerById(id));
    }

    //
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "when customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "when customerID could not be found"),
            @ApiResponse(responseCode = "500", description = "when internal error occurred"),

    })
    @DeleteMapping("/customer/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable("id") long customerId)
    {
        return ResponseEntity.ok(customerService.deleteCustomer(customerId));
    }


}
