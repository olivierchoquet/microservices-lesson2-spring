package com.ipl.order.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
public class ClientOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private OffsetDateTime createdAt;

    private BigDecimal total;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    public ClientOrder() {}

    public ClientOrder(Long clientId){
        this.clientId=clientId;
        this.status=OrderStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.total = BigDecimal.ZERO;
    }

    public void addItem(OrderItem i){
        i.setOrder(this);
        items.add(i);
        total = total.add(i.getLineTotal());
    }

    public Long getId(){
        return id;
    }

    public Long getClientId(){
        return clientId;
    }

    public OffsetDateTime getCreatedAt(){
        return createdAt;
    }

    public BigDecimal getTotal(){
        return total;
    }

    public List<OrderItem> getItems(){
        return items;
    }
}
