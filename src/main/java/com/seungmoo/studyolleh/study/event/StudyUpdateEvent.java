package com.seungmoo.studyolleh.study.event;


import com.seungmoo.studyolleh.domain.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyUpdateEvent {

    private final Study study;

    private final String message;

}
