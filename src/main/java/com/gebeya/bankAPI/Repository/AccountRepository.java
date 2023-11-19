package com.gebeya.bankAPI.Repository;

import com.gebeya.bankAPI.Model.Entities.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account,Long> {

}
