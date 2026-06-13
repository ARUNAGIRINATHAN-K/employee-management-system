package com.ems.service;

import com.ems.entity.SystemConfig;
import com.ems.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository repository;

    @Transactional(readOnly = true)
    public List<SystemConfig> list() { return repository.findAll(); }

    @Transactional
    public SystemConfig create(SystemConfig cfg) { return repository.save(cfg); }

    @Transactional
    public SystemConfig update(Long id, SystemConfig cfg) {
        SystemConfig existing = repository.findById(id).orElseThrow();
        existing.setConfigKey(cfg.getConfigKey());
        existing.setConfigValue(cfg.getConfigValue());
        existing.setDescription(cfg.getDescription());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) { repository.deleteById(id); }

    @Transactional(readOnly = true)
    public SystemConfig findByKey(String key) { return repository.findByConfigKey(key).orElse(null); }
}
