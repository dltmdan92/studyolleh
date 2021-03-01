package com.seungmoo.studyolleh.study;

import com.seungmoo.studyolleh.domain.Account;
import com.seungmoo.studyolleh.domain.Study;
import com.seungmoo.studyolleh.domain.Tag;
import com.seungmoo.studyolleh.domain.Zone;
import com.seungmoo.studyolleh.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    public Study getStudy(String path) {
        Study byPath = studyRepository.findByPath(path);
        checkIfExistingStudy(path, byPath);
        return byPath;
    }

    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = studyRepository.findAccountWithTagsByPath(path);
        if (!study.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(study, account);
        return study;
    }

    private void checkIfManager(Study study, Account account) {
        if (!study.getManagers().contains(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public void addTag(Study study, Tag tag) {
        // 얘는 현재 persistent 상태 이다. 왜냐면 (OSIV 때매 -> 뷰렌더링 까지 영속성 컨텍스트가 살아있음)
        study.getTags().add(tag);
    }

    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findAccountWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(study, account);
        return study;
    }

    public void addZone(Study study, Zone zone) {
        // 얘는 현재 persistent 상태 이다. 왜냐면 (OSIV 때매 -> 뷰렌더링 까지 영속성 컨텍스트가 살아있음)
        study.getZones().add(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }
}
