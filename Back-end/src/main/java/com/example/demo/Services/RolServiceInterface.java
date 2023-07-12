package com.example.demo.Services;

import com.example.demo.Entitys.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RolServiceInterface {
    Rol saveRol (Rol newRol) throws Exception;

    Boolean checkID (String id) throws Exception;

    Page<Rol> rolPage (Pageable page) throws Exception;

    Rol findbyID (String id) throws Exception;
}
