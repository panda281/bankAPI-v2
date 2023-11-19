package com.gebeya.bankAPI.Repository;

import com.gebeya.bankAPI.Model.Entities.MobileBankingUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MobileBankingUserRepository extends CrudRepository<MobileBankingUser, Integer> {
}
