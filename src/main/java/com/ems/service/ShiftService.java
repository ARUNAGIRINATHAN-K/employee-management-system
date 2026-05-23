package com.ems.service;

import com.ems.entity.Shift;
import com.ems.repository.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    @Transactional(readOnly = true)
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    @Transactional
    public Shift createShift(Shift shift) {
        if (shiftRepository.findByName(shift.getName()).isPresent()) {
            throw new RuntimeException("Shift name already exists: " + shift.getName());
        }
        return shiftRepository.save(shift);
    }
}
