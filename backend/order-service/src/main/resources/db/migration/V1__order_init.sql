-- Ensure service-specific schema exists
CREATE SCHEMA IF NOT EXISTS orders;

-- V1__order_init.sql
CREATE TABLE IF NOT EXISTS orders.orders (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT NOT NULL,
  status      TEXT NOT NULL DEFAULT 'CREATED'
                CHECK (status IN ('CREATED','PAID','SHIPPED','CANCELLED')),
  total       NUMERIC(12,2) NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_orders_user
    FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS orders.order_items (
  id          BIGSERIAL PRIMARY KEY,
  order_id    BIGINT NOT NULL,
  product_id  BIGINT NOT NULL,
  unit_price  NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
  quantity    INT NOT NULL CHECK (quantity > 0),
  CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id) REFERENCES orders.orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_order_items_product
    FOREIGN KEY (product_id) REFERENCES products.products(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_orders_user      ON orders.orders(user_id);
CREATE INDEX IF NOT EXISTS idx_order_items_ord  ON orders.order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_prod ON orders.order_items(product_id);
