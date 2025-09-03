-- Ensure service-specific schema exists
CREATE SCHEMA IF NOT EXISTS purchases;

-- V1__purchase_init.sql
CREATE TABLE IF NOT EXISTS purchases.purchase (
  id          BIGSERIAL PRIMARY KEY,
  user_email  VARCHAR(255) NOT NULL,
  status      TEXT NOT NULL DEFAULT 'CREATED'
                CHECK (status IN ('CREATED','PAID','SHIPPED','CANCELLED')),
  total       NUMERIC(12,2) NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS purchases.purchase_item (
  id          BIGSERIAL PRIMARY KEY,
  purchase_id BIGINT NOT NULL,
  product_id  BIGINT NOT NULL,
  name        VARCHAR(255) NOT NULL,
  price       NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  quantity    INT NOT NULL CHECK (quantity > 0),
  CONSTRAINT fk_purchase_item_purchase
    FOREIGN KEY (purchase_id) REFERENCES purchases.purchase(id) ON DELETE CASCADE,
  CONSTRAINT fk_purchase_item_product
    FOREIGN KEY (product_id) REFERENCES products.product(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_purchase_user      ON purchases.purchase(user_email);
CREATE INDEX IF NOT EXISTS idx_purchase_item_purchase  ON purchases.purchase_item(purchase_id);
CREATE INDEX IF NOT EXISTS idx_purchase_item_prod ON purchases.purchase_item(product_id);
