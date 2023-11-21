package com.gebeya.bankAPI.Service;

import com.gebeya.bankAPI.Model.DTO.ResponseModel;
import com.gebeya.bankAPI.Model.Entities.Customer;

import java.util.Optional;

public interface CustomerService {
//    public Customer addCustomer(Customer customer);
    public Iterable<Customer> getAllCustomer();

    public ResponseModel deleteCustomer(long CustomerNo);

    public Optional<Customer> findCustomerById(long id);

}
