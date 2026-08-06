package com.cognizant.retailpos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cognizant.retailpos.entity.Inventory;
import com.cognizant.retailpos.entity.Product;
import com.cognizant.retailpos.entity.StockLog;
import com.cognizant.retailpos.entity.User;
import com.cognizant.retailpos.enums.StockUpdateReason;
import com.cognizant.retailpos.repository.InventoryRepository;
import com.cognizant.retailpos.repository.StockLogRepository;

@ExtendWith(MockitoExtension.class)
class InventoryTest {
    @Mock InventoryRepository inventoryRepo;
    @Mock StockLogRepository logRepo;
    @Mock UserService users;
    @Mock ProductService products;
    private InventoryService service;
    private Inventory stock;

    @BeforeEach
    void setup() {
        service = new InventoryService(inventoryRepo, logRepo, users, products);
        Product product = new Product();
        product.setId(2L);
        product.setName("Rice 1kg");
        stock = new Inventory();
        stock.setProduct(product);
        stock.setQuantity(20);
    }

    @Test
    void adjustsStockAndLogs() {
        User user = new User();
        user.setUsername("associate");
        when(inventoryRepo.findByProductId(2L)).thenReturn(Optional.of(stock));
        when(inventoryRepo.save(any())).thenAnswer(call -> call.getArgument(0));
        when(users.findByUsername("associate")).thenReturn(user);

        Inventory result = service.adjustStock(
                2L, 5, StockUpdateReason.NEW_STOCK_DELIVERY, "Supplier delivery", "associate");

        assertThat(result.getQuantity()).isEqualTo(25);
        ArgumentCaptor<StockLog> log = ArgumentCaptor.forClass(StockLog.class);
        verify(logRepo).save(log.capture());
        assertThat(log.getValue().getPreviousQuantity()).isEqualTo(20);
        assertThat(log.getValue().getNewQuantity()).isEqualTo(25);
    }
}
