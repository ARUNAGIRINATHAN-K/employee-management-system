package com.ems.service;

import com.ems.entity.Holiday;
import com.ems.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Transactional
    public Holiday create(Holiday h) {
        return holidayRepository.save(h);
    }

    @Transactional
    public Holiday update(Long id, Holiday h) {
        Holiday existing = holidayRepository.findById(id).orElseThrow();
        existing.setDate(h.getDate());
        existing.setName(h.getName());
        return holidayRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        holidayRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Holiday> list() {
        return holidayRepository.findAll();
    }
}
