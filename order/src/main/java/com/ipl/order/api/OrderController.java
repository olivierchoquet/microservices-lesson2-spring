package com.ipl.order.api;
import com.ipl.order.api.dto.OrderDtos;
import com.ipl.order.api.dto.OrderView;
import com.ipl.order.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService s){
        this.service = s;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderView create(@RequestBody @Valid OrderDtos.Create req){
        return service.create(req);
    }

    @GetMapping("/{id}")
    public OrderView one(@PathVariable("id") Long id){
        return service.get(id);
    }

    @GetMapping
    public List<OrderView> list(@RequestParam(name = "clientId", required=false) Long clientId){
        return clientId == null ? service.all() : service.byClient(clientId);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String nf(Exception e){
        return e.getMessage();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String conflict(Exception e){
        return e.getMessage();
    }
}
