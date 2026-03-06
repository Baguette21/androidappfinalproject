package com.ectrvia.trivia.repository;

import com.ectrvia.trivia.entity.QuestionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionData, Long> {
    List<QuestionData> findByRoomIdOrderByQuestionOrderAsc(Long roomId);
    List<QuestionData> findByCategoryIdOrderByQuestionOrderAsc(Long categoryId);
    int countByRoomId(Long roomId);
    int countByCategoryId(Long categoryId);
}
