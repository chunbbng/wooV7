package team.woo.algorithm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.woo.domain.Schedule;
import team.woo.domain.ScheduleRepository;
import team.woo.member.Member;
import team.woo.member.MemberRepository;

import java.util.Optional;

@Service
public class TWservice {

    private final TaskTypeRepository taskTypeRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public TWservice(TaskTypeRepository taskTypeRepository, ScheduleRepository scheduleRepository, MemberRepository memberRepository) {
        this.taskTypeRepository = taskTypeRepository;
        this.scheduleRepository = scheduleRepository;
        this.memberRepository = memberRepository;
    }

}