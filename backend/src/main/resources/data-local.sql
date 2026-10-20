-- Datos de ejemplo para el perfil local (H2). Los mismos que database/supabase.sql.
-- Contraseñas: admin → sakura123, mesero → mesero123, cliente → cliente123.
insert into usuario (nombre, email, password_hash, rol, activo, creado_en) values
  ('Administrador', 'admin@sakura.example', '{bcrypt}$2y$10$vslauSpwvCBYiG/HsFtTWOA...eIkcG1oucGlhxHNmbJtj8Di/9K.', 'ADMIN', true, current_timestamp),
  ('Mesero de turno', 'mesero@sakura.example', '{bcrypt}$2y$10$5fGoLYEnTs3Zn.usYK0AEuB1zfKimobDpDDnxXiTJqE3uJunB3CH2', 'MESERO', true, current_timestamp),
  ('Cliente de prueba', 'cliente@sakura.example', '{bcrypt}$2y$10$lHV9slVwq0.aGN4vvKwmUOrIaXa4Honc2jEImk/WP5kj75rVFBuk.', 'CLIENTE', true, current_timestamp);

insert into categoria (nombre, descripcion, orden) values
  ('Entrantes', 'Para abrir el apetito', 1),
  ('Nigiri', 'Arroz y pescado, pieza a pieza', 2),
  ('Sashimi', 'Pescado crudo cortado al momento', 3),
  ('Maki', 'Rollos de sushi', 4),
  ('Teppanyaki', 'A la plancha, frente a ti', 5),
  ('Postres', null, 6),
  ('Bebidas', null, 7);

insert into producto (categoria_id, nombre, descripcion, precio, disponible, destacado, orden) values
  (2, 'Nigiri de toro flameado', 'Ventresca de atún, soja dulce y sal de yuzu', 38000, true, true, 1),
  (3, 'Sashimi moriawase', 'Selección del día de cinco pescados', 68000, true, true, 2),
  (4, 'Uramaki Sakura', 'Langostino en tempura, aguacate y mayonesa de shiso', 42000, true, true, 3),
  (1, 'Gyozas de wagyu', 'Caldo dashi y aceite de cebollino', 36000, true, true, 4),
  (5, 'Solomillo al teppanyaki', 'Verduras a la plancha y salsa ponzu', 79000, true, true, 5),
  (6, 'Mochi de flor de cerezo', 'Con helado de té matcha', 22000, true, true, 6),
  (1, 'Edamame con sal de mar', null, 15000, true, false, 7),
  (1, 'Miso shiru', 'Sopa de miso con tofu y wakame', 14000, true, false, 8),
  (7, 'Té verde', 'Sencha caliente', 9000, true, false, 9),
  (7, 'Ramune', 'Refresco japonés', 12000, false, false, 10);

insert into mesa (numero, capacidad, ubicacion, estado) values
  (1, 2, 'Salón', 'DISPONIBLE'), (2, 2, 'Salón', 'DISPONIBLE'), (3, 4, 'Salón', 'DISPONIBLE'), (4, 4, 'Salón', 'DISPONIBLE'),
  (5, 6, 'Salón', 'DISPONIBLE'), (6, 2, 'Barra', 'DISPONIBLE'), (7, 2, 'Barra', 'DISPONIBLE'), (8, 4, 'Terraza', 'DISPONIBLE'),
  (9, 4, 'Terraza', 'DISPONIBLE'), (10, 8, 'Comedor privado', 'DISPONIBLE');

insert into turno (hora, capacidad_max, activo) values
  ('13:00', 20, true), ('13:30', 20, true), ('14:00', 20, true), ('14:30', 20, true), ('15:00', 20, true), ('15:30', 20, true),
  ('20:00', 20, true), ('20:30', 20, true), ('21:00', 20, true), ('21:30', 20, true), ('22:00', 20, true), ('22:30', 20, true);

insert into cierre (fecha, motivo) values ('2026-12-25', 'Navidad'), ('2027-01-01', 'Año Nuevo');

insert into experiencia (nombre, descripcion, precio, personas, etiqueta, orden) values
  ('Omakase Sakura', 'Menú degustación de 9 pases elegido por el chef.', 240000, 1, 'Más popular', 1),
  ('Hanami para dos', 'Entrantes, selección de sushi, principal y postre para compartir.', 390000, 2, null, 2),
  ('Barra teppanyaki', 'Cena frente a la plancha: cinco pases cocinados al momento.', 190000, 1, null, 3);

insert into faq (pregunta, respuesta, orden) values
  ('¿Cómo reservo?', 'Desde esta misma página: elige día, número de personas y hora, y recibirás un código de reserva al momento.', 1),
  ('¿Puedo cancelar mi reserva?', 'Sí. Con tu código de reserva y el email con el que reservaste puedes cancelarla desde la sección de reservas.', 2),
  ('¿Aceptáis grupos grandes?', 'Online se reserva para 1 a 8 personas. Para grupos más grandes o eventos privados, llámanos.', 3),
  ('¿Qué horario tenéis?', 'Comidas de 13:00 a 16:00 y cenas de 20:00 a 23:30. El último turno se reserva media hora antes del cierre de cocina.', 4),
  ('¿Tenéis opciones para alergias o intolerancias?', 'Indícalo en las notas de la reserva y el equipo de cocina adaptará los platos que sea posible.', 5);
