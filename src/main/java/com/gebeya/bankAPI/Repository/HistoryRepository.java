package com.gebeya.bankAPI.Repository;

import com.gebeya.bankAPI.Model.Entities.History;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends CrudRepository<History,Integer> {
}
