package likelion14th.lte.user.repository;

import likelion14th.lte.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// [추가문제] (필수 X) 이 코드는 인터페이스일 뿐이고 구현체(implements) 클래스가 없습니다.
// 그런데 어떻게 프로그램 실행 시 DB와 통신하는 객체로 동작할 수 있나요?
// 답변: 로그램이 실행될 때, 스프링 데이터 JPA가 인터페이스를 감지하고 컴파일 시점이 아닌 런타임에 내부적으로 Dynamic Proxy(또는 Byte Buddy)를 사용하여 인터페이스의 구현 객체를 동적으로 생성해 줍니다.
//이때 주입되는 실제 구현체는 스프링 내부의 SimpleJpaRepository 클래스 기반의 프록시 객체이며, 이를 통해 개발자가 인터페이스만 선언해도 SQL 생성 및 DB 통신이 완벽하게 처리됩니다.
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findById(Long id);
}
