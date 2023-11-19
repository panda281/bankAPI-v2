package com.gebeya.bankAPI.Service;

import com.gebeya.bankAPI.Model.Entities.Customer;

public interface CustomerService {
//    public Customer addCustomer(Customer customer);
    public Iterable<Customer> getAllCustomer();

}
