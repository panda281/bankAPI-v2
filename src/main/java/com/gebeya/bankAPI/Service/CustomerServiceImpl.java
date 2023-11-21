package com.gebeya.bankAPI.Service;

import com.gebeya.bankAPI.Exception.ErrorMessage;
import com.gebeya.bankAPI.Model.DTO.ResponseModel;
import com.gebeya.bankAPI.Model.Entities.Customer;
import com.gebeya.bankAPI.Repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class CustomerServiceImpl implements CustomerService{


   CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository)
    {
        this.customerRepository=customerRepository;
    }

//    @Override
//    public Customer addCustomer(Customer customer)
//    {
//        return customerRepository.save(customer);
//    }

    @Override
    public Iterable<Customer> getAllCustomer(){
        return customerRepository.findAll();
    }

    @Override
    public ResponseModel deleteCustomer(long CustomerNo){
        if(!customerRepository.existsById(CustomerNo))
        {
            throw new ErrorMessage(HttpStatus.NOT_FOUND,"Customer Id could not be found");
        }
        customerRepository.deleteById(CustomerNo);

        return new ResponseModel(true, "Customer deleted successfully");
    }
}
