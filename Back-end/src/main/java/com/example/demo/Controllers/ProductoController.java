package com.example.demo.Controllers;

import com.example.demo.Controllers.DTOS.ProductoDTO;
import com.example.demo.Entitys.Categoria;
import com.example.demo.Entitys.Producto;
import com.example.demo.Services.CatergoriaService;
import com.example.demo.Services.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RequiredArgsConstructor
@RestController
@RequestMapping("v1/api/producto")
@Slf4j
@CrossOrigin(origins = "*")
public class ProductoController {
    private final ProductoService productoService;
    private final CatergoriaService catergoriaService;
    @GetMapping("")
    public ResponseEntity<Page<Producto>> getAllinAlta(@PageableDefault(value = 10, page = 0)Pageable page) throws Exception{
        Page<Producto> prodsInAlta = productoService.getAll(page);
        return ResponseEntity.status(HttpStatus.OK).body(prodsInAlta);
    }

    @PostMapping("")
    public ResponseEntity<Producto> createProducto(@RequestPart("producto") ProductoDTO productoDTO, @RequestPart("imagen") MultipartFile file) throws Exception{
        ProductoDTO newProdDTO = new ProductoDTO();
        Categoria cateFound = catergoriaService.findbyID(productoDTO.getProductoCategoria());
        System.out.println(cateFound.getNombre());
        Producto newProd = newProdDTO.toEntity(productoDTO,cateFound);
        Producto newProducto = productoService.crearProducto(newProd,file);
        return ResponseEntity.status(HttpStatus.OK).body(newProducto);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImg (@RequestParam("file") MultipartFile file) throws Exception{
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Work in progress");
    }



}
