package com.seungmoo.studyolleh.infra;

import org.testcontainers.containers.GenericContainer;

public abstract class AbstractContainerBaseTest {

    protected static final GenericContainer genericContainer;

    static {
        genericContainer = new GenericContainer("postgres")
                .withEnv("POSTGRES_DB", "springdata_test")
                // 지금 버전에서는 아래에 postgres_password가 설정되지 않으면 컨테이너가 띄워지지 않음.
                .withEnv("POSTGRES_PASSWORD", "1568919am!")
                // 컨테이너 포트를 선언한다. (testcontainer는 host post, 즉 mapping 해주는 포트는 선언할 수 없다.)
                // mapping port를 참조할 수는 있다.
                .withExposedPorts(5432);

        genericContainer.start();
    }

}
