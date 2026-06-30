-- Roles
INSERT INTO rol (id, nombre) VALUES (1, 'ROLE_ADMIN');
INSERT INTO rol (id, nombre) VALUES (2, 'ROLE_DESCARGADOR');

-- Usuarios
-- password "admin123" hasheado con BCrypt
INSERT INTO usuario (id, username, password)
  VALUES (1, 'admin', '$2a$10$a9Hiq.wVXUeEENPBRfPTHOFH6WMmjLSvmF4tYZ6uqCjxyB71ujofy');

-- password "descarga123" hasheado con BCrypt
INSERT INTO usuario (id, username, password)
  VALUES (2, 'transportista', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHy');

-- Asignacion de roles
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (1, 1); -- ROLE_ADMIN
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (2, 2); -- ROLE_DESCARGADOR