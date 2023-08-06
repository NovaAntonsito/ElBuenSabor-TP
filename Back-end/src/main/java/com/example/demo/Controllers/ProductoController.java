package com.example.demo.Controllers;

import com.example.demo.Controllers.DTOS.ProductoDTO;
import com.example.demo.Controllers.DTOS.ProductosCarritoDTO;
import com.example.demo.Entitys.Categoria;
import com.example.demo.Entitys.Insumo;
import com.example.demo.Entitys.Producto;
import com.example.demo.Entitys.ProductoInsumos;
import com.example.demo.Services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@RestController
@RequestMapping("v1/api/producto")
@Slf4j
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    private final CatergoriaService catergoriaService;

    private final CloudinaryServices cloudServices;

    private final InsumoService insumoService;

    private final ProductoInsumoService productoInsumosService;

    private final ConfigLocalService configService;

    @GetMapping("")
    public ResponseEntity<?> getAllinAlta() throws Exception{
        try {
            List<Producto> prodsInAlta = productoService.getAllNoPage();
            List<ProductoDTO> prodsDto = new ArrayList<>();
            for (Producto p : prodsInAlta){
                ProductoDTO pDto = new ProductoDTO();
                prodsDto.add(pDto.toDTO(p));
            }
            return ResponseEntity.status(HttpStatus.OK).body(prodsDto);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getOneProducto(@PathVariable("id") Long id) throws Exception{
        try {
            Producto productoFound = productoService.findbyID(id);
            ProductoDTO pDto = new ProductoDTO();
            pDto = pDto.toDTO(productoFound);
            Double precioTotalProducto = 0D;
            precioTotalProducto +=   pDto.getPrecio();
            if (precioTotalProducto == 0D){
                return ResponseEntity.status(HttpStatus.OK).body(pDto);
            }
            Double valorAgregadoPorCocinar = configService.getPrecioPorTiempo((double) pDto.getTiempoCocina());
            precioTotalProducto += valorAgregadoPorCocinar;
            // Supongamos que tienes un valor productoDescuento (long) que contiene el descuento en un rango de 0 a 100.
            long productoDescuento = pDto.getDescuento();

            // Dividimos el valor de productoDescuento entre 100 para obtener la fracción.
            double productoDividido = (double) productoDescuento / 100.0;


            // Calculamos el descuento aplicando la fracción productoDividido al precioTotal.
            precioTotalProducto *= (1 - productoDividido);

            pDto.setPrecio(precioTotalProducto);
            return ResponseEntity.status(HttpStatus.OK).body(pDto);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }

    }

    @PostMapping("")
    public ResponseEntity<?> createProducto(@RequestPart("producto") ProductoDTO productoDTO, @RequestPart(value = "imagen", required = false) MultipartFile file) throws Exception {

        try {
            List<ProductoInsumos> insumoList = new ArrayList<>();
            Categoria cateFound = catergoriaService.findbyID(productoDTO.getProductoCategoria());
            String url = null;

            if (file != null) {
                BufferedImage imgActual = ImageIO.read(file.getInputStream());
                var result = cloudServices.UploadIMG(file);
                url = (String) result.get("url");
            }

            for(ProductoInsumos insumos: productoDTO.getInsumos()){
                productoInsumosService.save(insumos);
                insumoList.add(insumos);
            }

            Producto newProd = productoDTO.toEntity(productoDTO,cateFound,insumoList,url);
            newProd = productoService.crearProducto(newProd,file);
            return ResponseEntity.status(HttpStatus.OK).body(newProd);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }

    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProducto(@PathVariable("id") Long ID, @RequestPart("producto") ProductoDTO productoDTO, @RequestPart("file") MultipartFile file) throws Exception {
        try {
            ProductoDTO newProdDTO = new ProductoDTO();
            List<ProductoInsumos> insumoSet = new ArrayList<>();
            Categoria cateFound = catergoriaService.findbyID(productoDTO.getProductoCategoria());
            String url = null;

            if (file != null) {
                BufferedImage imgActual = ImageIO.read(file.getInputStream());
                var result = cloudServices.UploadIMG(file);
                url = (String) result.get("url");
            }

            for(ProductoInsumos insumos: productoDTO.getInsumos()){
                productoInsumosService.save(insumos);
                insumoSet.add(insumos);
            }
            Producto updatedProducto = productoService.updateProducto(ID, newProdDTO.toEntity(productoDTO, catergoriaService.findbyID(productoDTO.getProductoCategoria()),insumoSet,url));
            return ResponseEntity.status(HttpStatus.OK).body(updatedProducto);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProducto(@PathVariable("id")Long ID) throws Exception{
        try {
            productoService.deleteSoftProducto(ID);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "message", "El objeto se borro correctamente"
            ));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }

    }
    @GetMapping("/search")
    public ResponseEntity<?> searchProducto
            (@RequestParam(required = false, value = "id") Long id,
             @RequestParam(required = false, value = "nombre") String nombre,
             Pageable page) throws Exception{
        try {
            List<Producto> productoPage;
            productoPage = productoService.searchByNameAndCategoria(id, nombre);
            /*if (id != null){
                productoPage = new ArrayList<>();
            }else {
                productoPage = productoService.searchByNameAndCategoria(null, nombre);
            }
            Categoria categoriaFound = catergoriaService.findbyID(id);
            processedProducts.clear();

            if (categoriaFound != null){
                productoPage.addAll(fillProductoPageRecursive(productoPage,categoriaFound.getID(),nombre));
            }*/
            List<ProductoDTO> prodsDto = new ArrayList<>();
            for (Producto p : productoPage){
                ProductoDTO pDto = new ProductoDTO();
                pDto = pDto.toDTO(p);
                Double precioTotalProducto = 0D;
                precioTotalProducto +=   pDto.getPrecio();
                if (precioTotalProducto == 0D){
                    prodsDto.add(pDto.toDTO(p));
                }else {
                    Double valorAgregadoPorCocinar = configService.getPrecioPorTiempo((double) pDto.getTiempoCocina());
                    precioTotalProducto += valorAgregadoPorCocinar;
                    // Supongamos que tienes un valor productoDescuento (long) que contiene el descuento en un rango de 0 a 100.
                    long productoDescuento = pDto.getDescuento();

                    // Dividimos el valor de productoDescuento entre 100 para obtener la fracción.
                    double productoDividido = (double) productoDescuento / 100.0;


                    // Calculamos el descuento aplicando la fracción productoDividido al precioTotal.
                    precioTotalProducto *= (1 - productoDividido);

                    pDto.setPrecio(precioTotalProducto);
                    prodsDto.add(pDto);
                }

            }
            return ResponseEntity.status(HttpStatus.OK).body(prodsDto);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    List<Long> processedProducts = new ArrayList<>();
    public List<Producto> fillProductoPageRecursive(List<Producto> returnProducts,Long categoriaId, String nombre) throws Exception {
            Categoria categoriaFound = catergoriaService.findbyID(categoriaId);
            if (categoriaFound != null) {
                // Buscar productos por nombre y categoría
                List<Categoria> subCategorias =categoriaFound.getSubCategoria();
                if (subCategorias.size() > 0){

                    // Recorrer las subcategorías y llamar recursivamente
                    for (Categoria cat : subCategorias) {
                        System.out.println(cat.getNombre());
                        List<Producto> addedProducts = fillProductoPageRecursive(returnProducts,cat.getID(), nombre);
                        for (Producto p : addedProducts){
                            if (!processedProducts.contains(p.getID())) {
                                processedProducts.add(p.getID());
                                returnProducts.add(p);
                            }

                        }
                    }
                }else{
                    List<Producto> pLista = productoService.searchByNameAndCategoria(categoriaFound.getID(), nombre);
                    for (Producto pNuevo : pLista) {
                        if (!processedProducts.contains(pNuevo.getID())) {
                            processedProducts.add(pNuevo.getID());
                            returnProducts.add(pNuevo);
                        }
                    }

                }
            }
            return returnProducts;
    }

}
