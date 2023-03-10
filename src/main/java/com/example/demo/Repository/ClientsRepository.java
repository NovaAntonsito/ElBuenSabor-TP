package com.example.demo.Repository;

import com.example.demo.Entitys.Client;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientsRepository extends BaseRepository<Client,Long> {
    @Query(value = "SELECT * FROM clientes c WHERE :name IS NULL OR c.nombre LIKE %:name%",
            nativeQuery = true)
    List<Client> searchClientes (@Param("name")String name);
}
