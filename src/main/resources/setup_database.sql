-- SCRIPT DE CONFIGURACIÓN INICIAL PARA MYSQL
-- Base de datos: tacos_el_micro

USE tacos_el_micro;

-- 1. Insertar Roles básicos
INSERT INTO roles (nombre_rol) VALUES ('ADMIN') ON DUPLICATE KEY UPDATE nombre_rol='ADMIN';
INSERT INTO roles (nombre_rol) VALUES ('CLIENTE') ON DUPLICATE KEY UPDATE nombre_rol='CLIENTE';
INSERT INTO roles (nombre_rol) VALUES ('COCINERA') ON DUPLICATE KEY UPDATE nombre_rol='COCINERA';
INSERT INTO roles (nombre_rol) VALUES ('REPARTIDOR') ON DUPLICATE KEY UPDATE nombre_rol='REPARTIDOR';

-- 2. Insertar Usuario de prueba (Password: 123456)
-- Nota: El sistema usa texto plano para pruebas locales según AuthController.java
INSERT INTO usuarios (username, email, password, activo, id_rol, nombre, telefono, fecha_registro) 
VALUES ('admin', 'admin@tacos.com', '123456', true, 1, 'Admin', '0000000000', NOW())
ON DUPLICATE KEY UPDATE username='admin';

INSERT INTO usuarios (username, email, password, activo, id_rol, nombre, telefono, fecha_registro) 
VALUES ('usuario', 'user@test.com', '123456', true, 2, 'Cliente', '0000000000', NOW())
ON DUPLICATE KEY UPDATE username='usuario';

INSERT INTO usuarios (username, email, password, activo, id_rol, nombre, telefono, fecha_registro) 
VALUES ('alexander', 'repartidor@tacos.com', '123456', true, 4, 'Alexander', '0000000000', NOW())
ON DUPLICATE KEY UPDATE username='alexander';

-- 3. Insertar Categorías (Opcional, si tienes una tabla de categorías)
-- Si no tienes tabla de categorías, el ProductoController usa id_categoria como Integer.
-- Basado en HomeScreen.tsx: 1=Tacos, 2=Bebidas, 3=Aguas, etc.

-- 4. Insertar Productos de prueba
INSERT INTO Productos (nombre_producto, descripcion, precio_base, url_imagen, activo, disponible, id_categoria)
VALUES ('Taco al Pastor', 'Delicioso taco de cerdo marinado con piña', 15.00, 'default.png', true, true, 1);

INSERT INTO Productos (nombre_producto, descripcion, precio_base, url_imagen, activo, disponible, id_categoria)
VALUES ('Coca Cola', 'Refresco de 600ml', 20.00, 'default.png', true, true, 2);

INSERT INTO Productos (nombre_producto, descripcion, precio_base, url_imagen, activo, disponible, id_categoria)
VALUES ('Agua de Horchata', 'Agua fresca tradicional', 25.00, 'default.png', true, true, 3);
