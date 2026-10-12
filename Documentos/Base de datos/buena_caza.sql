CREATE DATABASE  IF NOT EXISTS `buena_caza`;
USE `buena_caza`;
--
-- Table structure for table `cabana`
--
CREATE TABLE `cabana` (
  `id_cabana` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) DEFAULT NULL,
  `capacidad` int DEFAULT NULL,
  `estado` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id_cabana`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `invitado`
--
CREATE TABLE `invitado` (
  `id_invitado` int NOT NULL AUTO_INCREMENT,
  `dni` int NOT NULL,
  `email` varchar(200) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(45) DEFAULT NULL,
  `direccion` varchar(200) NOT NULL,
  `es_admin` tinyint NOT NULL DEFAULT '0',
  `contrasena_hash` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_invitado`),
  UNIQUE KEY `uq_invitado_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--
--
-- Table structure for table `temporada_tipo`
--
CREATE TABLE `temporada_tipo` (
  `id_temporada_tipo` int NOT NULL AUTO_INCREMENT,
  `tipo` varchar(45) NOT NULL,
  PRIMARY KEY (`id_temporada_tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `temporada`
--
CREATE TABLE `temporada` (
  `id_temporada` int NOT NULL AUTO_INCREMENT,
  `id_temporada_tipo` int NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_final` date NOT NULL,
  `precio_por_noche` decimal(15,2) DEFAULT NULL,
  PRIMARY KEY (`id_temporada`),
  KEY `id_temporada_tipo_idx` (`id_temporada_tipo`),
  CONSTRAINT `fk_temporada_temporada_tipo` FOREIGN KEY (`id_temporada_tipo`) REFERENCES `temporada_tipo` (`id_temporada_tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `reserva`
--
CREATE TABLE `reserva` (
  `id_reserva` int NOT NULL AUTO_INCREMENT,
  `id_cabana` int NOT NULL,
  `id_invitado` int NOT NULL,
  `id_temporada` int NOT NULL,
  `fecha_entrada` date NOT NULL,
  `fecha_salida` date NOT NULL,
  `monto_total` decimal(15,2) DEFAULT NULL,
  `comentario` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_reserva`),
  KEY `id_cabana_idx` (`id_cabana`),
  KEY `id_invitado_idx` (`id_invitado`),
  KEY `id_temporada_idx` (`id_temporada`),
  CONSTRAINT `fk_reserva_cabana` FOREIGN KEY (`id_cabana`) REFERENCES `cabana` (`id_cabana`),
  CONSTRAINT `fk_reserva_invitado` FOREIGN KEY (`id_invitado`) REFERENCES `invitado` (`id_invitado`),
  CONSTRAINT `fk_reserva_temporada` FOREIGN KEY (`id_temporada`) REFERENCES `temporada` (`id_temporada`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--
-- Carga de datos de configuracion y testeo
--

INSERT INTO `cabana` VALUES (1,'Waingunga',4,'libre'),(2,'Seeonee',4,'libre');
INSERT INTO `invitado`
  (`id_invitado`, `dni`, `email`, `nombre`, `telefono`, `direccion`, `es_admin`)
VALUES
  (1,45454545,'matias@gmail.com','Matias Urbicain',NULL,'Alsina 291',1),
  (2,45253547,'tomas@gmail.com','Tomas Perez',NULL,'Sarmiento 501',0),
  (3,39224563,'walter@gmail.com','Walter Barrionuevo',NULL,'Salta 891',0);
INSERT INTO `temporada_tipo` VALUES (1,'Alta'),(2,'Media'),(3,'Baja');
INSERT INTO `temporada` VALUES (1,2,'2026-09-01','2026-12-01',80000.00),(2,1,'2026-12-01','2027-03-01',90000.00),(3,3,'2027-03-01','2027-06-01',60000.00),(4,2,'2027-06-01','2027-09-01',85000.00);
INSERT INTO `reserva` VALUES (1,1,2,1,'2026-10-01','2026-10-14',936000.00,NULL),(2,2,3,2,'2027-01-03','2027-01-10',630000.00,NULL),(3,1,2,3,'2027-04-10','2027-04-24',756000.00,NULL);


-- Vista para el reporte general de reservas
CREATE OR REPLACE VIEW `vw_reserva` AS
SELECT 
    `r`.`id_reserva`, `r`.`fecha_entrada`, `r`.`fecha_salida`, `r`.`monto_total`, `r`.`comentario`,
    `c`.`nombre` AS `cabana_nombre`, `i`.`nombre` AS `invitado_nombre`, `i`.`dni` AS `invitado_dni`, `tt`.`tipo` AS `temporada_tipo`
FROM `reserva` `r`
JOIN `cabana` `c` ON `r`.`id_cabana` = `c`.`id_cabana`
JOIN `invitado` `i` ON `r`.`id_invitado` = `i`.`id_invitado`
JOIN `temporada` `t` ON `r`.`id_temporada` = `t`.`id_temporada`
JOIN `temporada_tipo` `tt` ON `t`.`id_temporada_tipo` = `tt`.`id_temporada_tipo`;
