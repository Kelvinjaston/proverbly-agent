package com.example.proverb.repo;

import com.example.proverb.model.Proverb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProverbRepository extends JpaRepository<Proverb,Long> {

    List<Proverb>findByLanguageIgnoreCase(String language);
}
