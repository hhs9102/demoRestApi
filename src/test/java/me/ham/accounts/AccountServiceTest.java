package me.ham.accounts;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest{

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void findByUsername(){

        String username = "hhs9102@naver.com";
        String password = "password";

        Account account = Account.builder()
            .email("hhs9102@naver.com")
            .password(passwordEncoder.encode("password"))
            .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
            .build();
        this.accountRepository.save(account);

        UserDetailsService userDetailsService = accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername("hhs9102@naver.com");

        System.out.println(password);
        System.out.println(userDetails.getPassword());

        assertThat(this.passwordEncoder.matches(password, userDetails.getPassword()));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void findByUsernameFail(){
        accountService.loadUserByUsername("laskdjf@naver.com");
    }

    @Test
    public void expectedException(){
        String userEmail = "alskjdf@naver.com";
        expectedException.expect(UsernameNotFoundException.class);

        accountService.loadUserByUsername(userEmail);
    }

}