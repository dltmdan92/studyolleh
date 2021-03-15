package com.seungmoo.studyolleh.modules.study.event;

import com.seungmoo.studyolleh.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyCreatedEvent {

    private final Study study;

}
