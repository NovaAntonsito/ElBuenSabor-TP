package com.example.demo.Services;

import com.example.demo.Entitys.UnidadMedida;
import com.example.demo.Repository.UnidadMedidaRepository;
import com.fasterxml.jackson.databind.util.BeanUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UnidadMedidaService implements UnidadMedidaServiceInterface{
    private final UnidadMedidaRepository unidadMedidaRepository;
    @Override
    public void save(UnidadMedida unidadMedida) throws Exception {
        unidadMedidaRepository.save(unidadMedida);
    }


    @Override
    public UnidadMedida findbyID(Long id) throws Exception {
        if(!unidadMedidaRepository.findByID(id).equals(null)){
            return unidadMedidaRepository.findByID(id);
        }else{
            throw new RuntimeException("No existe ese objeto");
        }
    }
    @Override
    public UnidadMedida updateUnidadMedida(Long id, UnidadMedida medidaUpdate) throws Exception {
        UnidadMedida medidaFound = this.findbyID(id);
        if (medidaFound != null) {
            medidaFound.setNombre(medidaUpdate.getNombre());
            return unidadMedidaRepository.save(medidaFound);
        } else {
            throw new RuntimeException("No existe ese objeto");
        }
    }


    @Override
    public List<UnidadMedida> findAll() throws Exception {
        return unidadMedidaRepository.findAll();
    }
}