package com.seungmoo.studyolleh.modules.study.event;

import com.seungmoo.studyolleh.modules.study.Study;
import lombok.Getter;

@Getter

public class StudyCreatedEvent {

    private Study study;

    public StudyCreatedEvent(Study study) {
        this.study = study;
    }

}
