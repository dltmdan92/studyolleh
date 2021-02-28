package com.seungmoo.studyolleh.settings.form;

import com.seungmoo.studyolleh.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
// 기본 생성자 없으면, Profile(Account account) -> 요 생성자로 먼저 객체 만들고, 각 setMethod로 주입할 것임
// Account가 없는 경우 NullPointerException 발생!! -> 기본 생성자 선언해주자.
// Controller에서 modelMapper로 객체 생성 시켜주도록 하자.
//@NoArgsConstructor
public class Profile {
    @Length(max = 35)
    private String bio;
    @Length(max = 50)
    private String url;
    @Length(max = 50)
    private String occupation;
    @Length(max = 50)
    private String location;

    private String profileImage;

    /*public Profile(Account account) {
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
        this.profileImage = account.getProfileImage();
    }*/
}
