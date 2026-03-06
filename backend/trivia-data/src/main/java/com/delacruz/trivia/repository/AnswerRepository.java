package com.ectrvia.trivia.repository;

import com.ectrvia.trivia.entity.AnswerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerData, Long> {
    List<AnswerData> findByQuestionIdOrderByAnswerIndexAsc(Long questionId);
    void deleteByQuestionId(Long questionId);
}
