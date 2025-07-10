package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     *
     * @param username
     * @return
     */
    Employee getByUsername(String username);

    /**
     * 插入员工
     * @param employee
     */
    @AutoFill(OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return {@link Page }<{@link Employee }>
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 更新员工操作
     * @param employee
     */
    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 通过id查询员工
     * @param id
     * @return {@link Employee }
     */
    Employee getById(Long id);

}
