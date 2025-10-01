package com.ipl.product.service;
import com.ipl.product.api.dto.ProductDtos;
import com.ipl.product.domain.Product;
import com.ipl.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo){
        this.repo=repo;
    }

    public List<Product> all() {
        return repo.findAll();
    }

    public Product get(Long id){
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Product "+id+" not found"));
    }

    public Product create(ProductDtos.Create d){
        return repo.save(new Product(d.name,d.price,d.stock));
    }

    public Product update(Long id, ProductDtos.Update d){
        Product p = get(id);
        p.setName(d.name);
        p.setPrice(d.price);
        p.setStock(d.stock);
        return repo.save(p);
    }

    public void delete(Long id) {
        repo.delete(get(id));
    }

    @Transactional
    public void reserve(Long id, int qty){
        Product p = get(id);
        if(p.getStock() < qty) throw new IllegalStateException("INSUFFICIENT_STOCK");
        p.setStock(p.getStock() - qty);
    }
}
