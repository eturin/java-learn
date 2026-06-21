import javax.management.Notification;

interface SMSService {

    void sendSMS(User user);
}

interface EmailService {

    void sendEmail(User user);
}


interface NotificationStrategy{
    void notifyCustomer(User usr);
}
class Notifier{
    private NotificationStrategy strategy;
    Notifier(NotificationStrategy strategy) {
        this.strategy = strategy;
    }

    public NotificationStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(NotificationStrategy strategy) {
        this.strategy = strategy;
    }

    public void run(User usr) {
        strategy.notifyCustomer(usr);
    }
}

class Application {

    private EmailService emailService;
    private SMSService smsService;

    public Application(EmailService emailService, SMSService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public void run(User user) {
        var notifier = new Notifier(emailService::sendEmail);
        notifier.run(user);
        notifier.setStrategy(smsService::sendSMS);
        notifier.run(user);
    }
}

class User {
    private final String email;
    private final String phoneNumber;

    User(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}

public class Main {
    static void main() {

    }
}
