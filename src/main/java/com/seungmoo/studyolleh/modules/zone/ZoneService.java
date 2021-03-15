package com.seungmoo.studyolleh.modules.zone;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    // ZoneService Bean이 만들어진(초기화) 이후에 실행됨.
    @PostConstruct
    public void initZoneData() throws IOException {
        if (zoneRepository.count() == 0) {
            Resource resource = new ClassPathResource("zones_korea.csv");
            InputStream zonesInputStream = resource.getInputStream();

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zonesInputStream))) {
                List<Zone> zoneList = bufferedReader.lines().map(line -> {
                    String[] split = line.split(",");
                    return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
                }).collect(Collectors.toList());

                // Files는 그냥 java main으로 실행했을 때, 파일 경로로 읽어올 수 있는 방식이다. 여기선 안됨
                /*
                List<Zone> zoneList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {
                    String[] split = line.split(",");
                    return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
                })
                .collect(Collectors.toList());*/
                zoneRepository.saveAll(zoneList);
            }
        }
    }

}
