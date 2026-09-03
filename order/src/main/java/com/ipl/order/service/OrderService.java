package com.ipl.order.service;
import com.ipl.order.api.dto.OrderDtos;
import com.ipl.order.api.dto.OrderView;
import com.ipl.order.domain.ClientOrder;
import com.ipl.order.domain.OrderItem;
import com.ipl.order.external.ClientClient;
import com.ipl.order.external.ProductClient;
import com.ipl.order.external.ProductClient.ProductDto;
import com.ipl.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository repo;
    private final ClientClient clientClient;
    private final ProductClient productClient;

    public OrderService(OrderRepository r, ClientClient cc, ProductClient pc){
        this.repo = r;
        this.clientClient = cc;
        this.productClient=pc;
    }

    public OrderView get(Long id){
        ClientOrder o = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Order "+id+" not found"));

        return toView(o);
    }

    public List<OrderView> all() {
        return repo.findAll().stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    public List<OrderView> byClient(Long clientId) {
        return repo.findByClientId(clientId).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderView create(OrderDtos.Create req){
        // check client existence -> exception if not
        ClientClient.ClientDto client = clientClient.getClient(req.clientId);

        // check all products existence and stock sufficient
        Map<Long, ProductDto> products = new LinkedHashMap<>();
        for (OrderDtos.Item it : req.items){
            ProductClient.ProductDto p = productClient.getProduct(it.productId);
            if (p.stock < it.quantity) {
                throw new RuntimeException("INSUFFICIENT_STOCK");
            }
            products.put(it.productId, p);
        }

        ClientOrder order = new ClientOrder(client.id);

        for (OrderDtos.Item it : req.items){
            ProductClient.ProductDto p = productClient.getProduct(it.productId);
            productClient.reserve(p.id, it.quantity);
            order.addItem(new OrderItem(p.id, p.name, it.quantity, p.price));
        }

        return toView(repo.save(order));
    }

    private OrderView toView(ClientOrder o) {
        List<OrderView.Line> lines = o.getItems().stream()
                .map(li -> new OrderView.Line(
                        li.getProductId(),
                        li.getProductName(),
                        li.getQuantity(),
                        li.getUnitPrice(),
                        li.getLineTotal()))
                .collect(Collectors.toList());   // <- replace .toList()
        return new OrderView(o.getId(), o.getClientId(), o.getCreatedAt(), o.getTotal(), lines);
    }
}
