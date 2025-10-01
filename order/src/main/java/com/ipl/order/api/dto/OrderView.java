package com.ipl.order.api.dto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class OrderView {
    public Long id;
    public Long clientId;
    public OffsetDateTime createdAt;
    public BigDecimal total;
    public List<Line> items;

    public static class Line {
        public Long productId;
        public String productName;
        public Integer quantity;
        public BigDecimal unitPrice;
        public BigDecimal lineTotal;

        public Line(){}

        public Line(Long pid,String pn,Integer q,BigDecimal up,BigDecimal lt){
            productId = pid;
            productName = pn;
            quantity = q;
            unitPrice = up;
            lineTotal=lt;
        }
    }

    public OrderView(){}

    public OrderView(Long id,Long c,OffsetDateTime t,BigDecimal tot,List<Line> lines){
        this.id = id;
        this.clientId = c;
        this.createdAt = t;
        this.total = tot;
        this.items=lines;
    }
}
