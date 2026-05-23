package com.ems.service;

import com.ems.entity.Department;
import com.ems.entity.Employee;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
    }

    @Transactional
    public Department createDepartment(Department department, String adminUsername) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new RuntimeException("Department already exists with name: " + department.getName());
        }
        Department saved = departmentRepository.save(department);
        auditLogService.log("CREATE_DEPARTMENT", adminUsername, "Created department: " + saved.getName());
        return saved;
    }

    @Transactional
    public Department updateDepartment(Long id, Department departmentDetails, String adminUsername) {
        Department department = getDepartmentById(id);
        department.setName(departmentDetails.getName());
        department.setDescription(departmentDetails.getDescription());
        if (departmentDetails.getManager() != null) {
            department.setManager(departmentDetails.getManager());
        }
        Department updated = departmentRepository.save(department);
        auditLogService.log("UPDATE_DEPARTMENT", adminUsername, "Updated department: " + updated.getName());
        return updated;
    }

    @Transactional
    public void deleteDepartment(Long id, String adminUsername) {
        Department department = getDepartmentById(id);
        
        // Remove department reference from all employees in this department
        List<Employee> employees = employeeRepository.findByDepartmentIdAndStatusNot(id, "DELETED");
        for (Employee e : employees) {
            e.setDepartment(null);
            employeeRepository.save(e);
        }
        
        departmentRepository.delete(department);
        auditLogService.log("DELETE_DEPARTMENT", adminUsername, "Deleted department: " + department.getName());
    }

    @Transactional
    public void assignEmployeesToDepartment(Long departmentId, List<Long> employeeIds, String adminUsername) {
        Department department = getDepartmentById(departmentId);
        for (Long empId : employeeIds) {
            Employee employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + empId));
            employee.setDepartment(department);
            employeeRepository.save(employee);
        }
        auditLogService.log("ASSIGN_EMPLOYEES_DEPT", adminUsername, 
                "Assigned " + employeeIds.size() + " employees to department: " + department.getName());
    }
}
