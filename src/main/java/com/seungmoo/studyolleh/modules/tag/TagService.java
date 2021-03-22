package com.seungmoo.studyolleh.modules.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Tag findOrCreateNew(String tagTitle) {
        Optional<Tag> tag = tagRepository.findByTitle(tagTitle);
        return tag.orElseGet(() -> tagRepository.save(Tag.builder().title(tagTitle).build()));
    }

}
