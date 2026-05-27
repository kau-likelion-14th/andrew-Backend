package likelion14th.lte.user.entity;

import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
// [Q1. @NoArgsConstructor는 매개변수가 없는 기본 생성자를 만듭니다.
// 그런데 왜 누구나 쓸 수 있게 PUBLIC으로 열어두지 않고, 굳이 PROTECTED로 막아두었을까요? (객체 생성의 안전성과 JPA 관점)]
// 답변: JPA는 데이터베이스에서 값을 조회할 때 실제 객체 대신 가짜 객체인 프록시(Proxy)를 먼저 생성하여 지연 로딩(Lazy Loading)을 처리합니다.
// 이 프록시 객체를 만들려면 기술적으로 '기본 생성자'가 반드시 필요합니다. 단, JPA 스펙상 기본 생성자의 접근 제어자가 protected까지 허용되므로,
// 안전성과 기술적 요구사항을 모두 만족하는 PROTECTED로 제한하는 것입니다.
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Q2. @Column(nullable = false) 어노테이션이 DB와 자바 코드 사이에서 하는 역할은 무엇인가요?]
    // 답변: 애플리케이션 레벨에서 데이터베이스에 쿼리를 보내기 전, 자바 객체의 해당 필드가 null이면 하이버네이트가 예외를 미리 발생시켜 잘못된 데이터가 DB로 넘어가는 것을 원천 차단합니다.
    @Column(nullable = false)
    private String username;

    @Column(length = 16, nullable = false, unique = true)
    private String userTag;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String s3Image;

    @Builder(access = AccessLevel.PUBLIC)
    private User(String username, String userTag, String introduction, String profileImage, String s3Image) {
        this.username = username;
        this.userTag = userTag;
        this.introduction = introduction;
    }

    // [Q3. @Setter를 위 @Getter 처럼 사용하면 모든 맴버들에 setIntruduction() 같은 setter 메서드가 생성됩니다. 하지만 왜 @Setter를 쓰지않고 updateIntroduction() 이라는 명확한 메서드를 만든 객체지향적인 이유는 무엇인가요?]
    // 답변: @Setter를 열어두면 외부의 엉뚱한 곳에서 객체의 상태를 아무렇게나 변경할 수 있어 추적이 힘들어집니다.
    // 객체 스스로가 자신의 상태 변경을 제어하도록 내부에 메서드를 작성하는 것이 객체지향의 캡슐화(Encapsulation) 원칙에 부합합니다.
    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
