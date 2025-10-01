package com.ipl.order.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    private ClientOrder order;

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public OrderItem() {}

    public OrderItem(Long productId,String productName,Integer qty,BigDecimal unitPrice){
        this.productId = productId;
        this.productName = productName;
        this.quantity = qty;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
    }

    public void setOrder(ClientOrder o){
        this.order=o;
    }

    public Long getProductId(){
        return productId;
    }

    public String getProductName(){
        return productName;
    }

    public Integer getQuantity(){
        return quantity;
    }

    public BigDecimal getUnitPrice(){
        return unitPrice;
    }

    public BigDecimal getLineTotal(){
        return lineTotal;
    }
}
