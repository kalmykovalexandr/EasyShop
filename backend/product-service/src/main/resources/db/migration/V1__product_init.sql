-- Ensure service-specific schema exists
CREATE SCHEMA IF NOT EXISTS products;

CREATE TABLE IF NOT EXISTS products.product (
  id          BIGSERIAL PRIMARY KEY,
  name        VARCHAR(255) NOT NULL,
  description TEXT,
  price       NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  stock       INT NOT NULL CHECK (stock >= 0),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_product_name ON products.product(name);
