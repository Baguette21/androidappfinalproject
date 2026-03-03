package com.ectrvia.trivia.model;

import lombok.Data;

@Data
public class Answer {
    private Long id;
    private Long questionId;
    private String answerText;
    private Integer answerIndex;
}
