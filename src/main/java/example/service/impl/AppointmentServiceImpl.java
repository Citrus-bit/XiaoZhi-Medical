package example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import example.entity.Appointment;
import example.mapper.AppointmentMapper;
import example.service.AppointmentService;
import org.springframework.stereotype.Service;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {
}
