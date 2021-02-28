package com.seungmoo.studyolleh.zone;

import com.seungmoo.studyolleh.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone,Long> {
    Zone findByCityAndProvince(String cityName, String provinceName);
}
