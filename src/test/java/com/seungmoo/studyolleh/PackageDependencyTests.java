package com.seungmoo.studyolleh;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = App.class)
public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.seungmoo.studyolleh.modules..")
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage("com.seungmoo.studyolleh.modules..");

    /**
     * study 패키지에 있는 클래스는
     * study, event 패키지에서만 접근 가능해야 한다.
     *
     * 현재 account에서 study를 바라보고 있다.
     *
     */
    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage(STUDY, EVENT);

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY, ACCOUNT, EVENT);

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, ACCOUNT);

    /**
     * com.seungmoo.studyolleh.modules 패키지 내부의 모듈들을 모두 slice 하고
     * 순환참조 여부 테스트
     */
    @ArchTest
    ArchRule cycleCheck = slices().matching("com.seungmoo.studyolleh.modules.(*)..")
            .should().beFreeOfCycles();

}
