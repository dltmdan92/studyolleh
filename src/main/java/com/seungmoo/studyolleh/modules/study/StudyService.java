package com.seungmoo.studyolleh.modules.study;

import com.seungmoo.studyolleh.modules.account.Account;
import com.seungmoo.studyolleh.modules.study.event.StudyCreatedEvent;
import com.seungmoo.studyolleh.modules.study.event.StudyUpdateEvent;
import com.seungmoo.studyolleh.modules.study.form.StudyDescriptionForm;
import com.seungmoo.studyolleh.modules.study.form.StudyForm;
import com.seungmoo.studyolleh.modules.tag.Tag;
import com.seungmoo.studyolleh.modules.tag.TagRepository;
import com.seungmoo.studyolleh.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;

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
        Study study = studyRepository.findStudyWithTagsByPath(path);
        if (!study.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디 소개를 수정했습니다."));
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
        Study study = studyRepository.findStudyWithZonesByPath(path);
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

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(study, account);
        return study;
    }

    public void publish(Study study) {
        study.setPublished(true);
        eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }

    public void close(Study study) {
        study.close();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디가 종료되었습니다."));
    }

    public void startRecruit(Study study) {
        study.startRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "팀원 모집을 시작합니다."));
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "팀원 모집을 중단 했습니다."));
    }

    // @RequestParam에 대해서는 @Valid 못쓰니까 여기서 valid 로직 해준다.
    public boolean isValidPath(String newPath) {
        if (!newPath.matches(StudyForm.VALID_PATH_PATTERN)) {
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }

    public void remove(Study study) {
        if (study.isRemovable()) {
            studyRepository.delete(study);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void addMember(Study study, Account account) {
        study.addMember(account);
    }

    public void removeMember(Study study, Account account) {
        study.removeMember(account);
    }

    public Study getStudyToEnroll(String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }

    public void generateTestStudies(Account account) {
        /**
         * 이렇게 One 트랜잭션 메소드 안에 for loop 이 있을 경우
         * createNewStudy의 save 메소드가 일괄로 돌고
         * 그 다음에 study_tags와 study_managers에 insert문이 같이 일괄로 돌아간다.
         */
        for (int i = 0; i < 30; i++) {
            String randomValue = RandomString.make(5);
            Study study = Study.builder()
                    .title("테스트 스터디 " + randomValue)
                    .path("test-" + randomValue)
                    .shortDescription("테스트용 스터디 입니다.")
                    .fullDescription("test")
                    .tags(new HashSet<>())
                    .managers(new HashSet<>())
                    .build();
            study.publish();
            Study newStudy = this.createNewStudy(study, account);
            Optional<Tag> jpa = tagRepository.findByTitle("JPA");
            jpa.ifPresent(tag -> newStudy.getTags().add(tag));
        }
    }
}
