package example.service;

import example.entity.Appointment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Test
    public void testCRUD() {
        // Insert
        Appointment appointment = new Appointment();
        appointment.setUsername("张三");
        appointment.setIdCard("110101199001011234");
        appointment.setDepartment("内科");
        appointment.setDate("2025-04-14");
        appointment.setTime("上午");
        boolean saved = appointmentService.save(appointment);
        System.out.println("Saved: " + saved);
        assert saved;
        assert appointment.getId() != null;

        // Select
        Appointment retrieved = appointmentService.getById(appointment.getId());
        System.out.println("Retrieved: " + retrieved.getUsername());
        assert "张三".equals(retrieved.getUsername());

        // Update
        retrieved.setDepartment("外科");
        boolean updated = appointmentService.updateById(retrieved);
        System.out.println("Updated: " + updated);
        assert updated;
        assert "外科".equals(appointmentService.getById(retrieved.getId()).getDepartment());

        // List
        List<Appointment> list = appointmentService.list();
        System.out.println("List size: " + list.size());
        assert !list.isEmpty();

        // Delete
        boolean deleted = appointmentService.removeById(retrieved.getId());
        System.out.println("Deleted: " + deleted);
        assert deleted;
        assert appointmentService.getById(retrieved.getId()) == null;
    }
}
