package hotelbooking.demo.services;

import hotelbooking.demo.domains.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;


@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final UserService userService;
    public EmailService(JavaMailSender javaMailSender,
                        TemplateEngine templateEngine,
                        UserService userService){
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.userService = userService;
    }
    @Async
    public void sendVerifyEmail(String toEmail, String email, String token){
        try {
            String verifyUrl = "http://localhost:8080/api/v1/auth/verify?token=" + token;
            User user = userService.getUserByEmail(email);
            Context context = new Context();
            context.setVariable("name", user.getFullname());
            context.setVariable("link", verifyUrl);

            String htmlContent = templateEngine.process("verification-email", context);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("Kích hoạt tài khoản Hotel Booking");
            helper.setText(htmlContent, true); // true = Cho phép HTML

            javaMailSender.send(message);
            System.out.println("Email kích hoạt đã gửi đến: " + toEmail);
        }catch (MessagingException e) {
            System.out.println("Lỗi gửi mail: " + e.getMessage());
            // Tùy chọn: ném lỗi ra ngoài nếu muốn controller biết
        }
    }
}
