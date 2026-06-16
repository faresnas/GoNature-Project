CREATE DATABASE  IF NOT EXISTS `gonature` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `gonature`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: gonature
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `active_visitors`
--

DROP TABLE IF EXISTS `active_visitors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `active_visitors` (
  `park_id` int NOT NULL,
  `current_count` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`park_id`),
  CONSTRAINT `active_visitors_ibfk_1` FOREIGN KEY (`park_id`) REFERENCES `parks` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `active_visitors`
--

LOCK TABLES `active_visitors` WRITE;
/*!40000 ALTER TABLE `active_visitors` DISABLE KEYS */;
INSERT INTO `active_visitors` VALUES (1,0),(2,0);
/*!40000 ALTER TABLE `active_visitors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employees`
--

DROP TABLE IF EXISTS `employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employees` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `employee_number` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `role` enum('PARK_WORKER','PARK_MANAGER','DEPARTMENT_MANAGER','SERVICE_REP') NOT NULL,
  `park_id` int DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `employee_number` (`employee_number`),
  UNIQUE KEY `username` (`username`),
  KEY `park_id` (`park_id`),
  CONSTRAINT `employees_ibfk_1` FOREIGN KEY (`park_id`) REFERENCES `parks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES (1,'Dana','Cohen','EMP001','dana@gonature.com','PARK_WORKER',1,'dana','1234'),(2,'Yossi','Levy','EMP002','yossi@gonature.com','PARK_MANAGER',1,'yossi','1234'),(3,'Rina','Mizrahi','EMP003','rina@gonature.com','DEPARTMENT_MANAGER',NULL,'rina','1234'),(4,'Tal','Ben-Ami','EMP004','tal@gonature.com','SERVICE_REP',NULL,'tal','1234'),(6,'David','Levi','E005','david@gonature.com','PARK_MANAGER',2,'david','1234');
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `guides`
--

DROP TABLE IF EXISTS `guides`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `guides` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `id_number` varchar(20) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_number` (`id_number`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guides`
--

LOCK TABLES `guides` WRITE;
/*!40000 ALTER TABLE `guides` DISABLE KEYS */;
INSERT INTO `guides` VALUES (2,'hadi sho','teuide@gmail.com','0549586600',NULL,'hadi','12341234');
/*!40000 ALTER TABLE `guides` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_number` int NOT NULL,
  `order_date` date DEFAULT NULL,
  `number_of_visitors` int DEFAULT NULL,
  `confirmation_code` int DEFAULT NULL,
  `subscriber_id` int DEFAULT NULL,
  `date_of_placing_order` date DEFAULT NULL,
  PRIMARY KEY (`order_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'2030-03-03',3,1111,201,'2026-05-01'),(2,'2022-09-09',12,2222,202,'2026-05-10');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `park_visits`
--

DROP TABLE IF EXISTS `park_visits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `park_visits` (
  `id` int NOT NULL AUTO_INCREMENT,
  `reservation_id` int DEFAULT NULL,
  `park_id` int NOT NULL,
  `entry_time` datetime NOT NULL,
  `exit_time` datetime DEFAULT NULL,
  `num_visitors` int NOT NULL DEFAULT '1',
  `visitor_type` enum('INDIVIDUAL','GROUP','WALK_IN','SUBSCRIBER') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `park_id` (`park_id`),
  KEY `reservation_id` (`reservation_id`),
  CONSTRAINT `park_visits_ibfk_1` FOREIGN KEY (`park_id`) REFERENCES `parks` (`id`),
  CONSTRAINT `park_visits_ibfk_2` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `park_visits`
--

LOCK TABLES `park_visits` WRITE;
/*!40000 ALTER TABLE `park_visits` DISABLE KEYS */;
/*!40000 ALTER TABLE `park_visits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parks`
--

DROP TABLE IF EXISTS `parks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parks` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `max_capacity` int NOT NULL,
  `prebooked_reserved` int NOT NULL DEFAULT '0',
  `avg_stay_hours` double NOT NULL DEFAULT '4',
  `full_price` double NOT NULL DEFAULT '0',
  `reserved_quota` int DEFAULT '0',
  `promotion_discount` double DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parks`
--

LOCK TABLES `parks` WRITE;
/*!40000 ALTER TABLE `parks` DISABLE KEYS */;
INSERT INTO `parks` VALUES (1,'Carmel National Park',120,20,4,80,0,0),(2,'Ein Gedi Nature Reserve',80,15,5,60,0,0);
/*!40000 ALTER TABLE `parks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pending_requests`
--

DROP TABLE IF EXISTS `pending_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pending_requests` (
  `id` int NOT NULL AUTO_INCREMENT,
  `park_id` int NOT NULL,
  `request_type` enum('MAX_CAPACITY','PREBOOKED_RESERVED','AVG_STAY_HOURS','PROMOTION') NOT NULL,
  `new_value` double NOT NULL,
  `requested_by` int NOT NULL,
  `status` enum('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `park_id` (`park_id`),
  KEY `requested_by` (`requested_by`),
  CONSTRAINT `pending_requests_ibfk_1` FOREIGN KEY (`park_id`) REFERENCES `parks` (`id`),
  CONSTRAINT `pending_requests_ibfk_2` FOREIGN KEY (`requested_by`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pending_requests`
--

LOCK TABLES `pending_requests` WRITE;
/*!40000 ALTER TABLE `pending_requests` DISABLE KEYS */;
INSERT INTO `pending_requests` VALUES (1,1,'MAX_CAPACITY',90,2,'APPROVED','2026-06-16 00:08:14'),(2,1,'MAX_CAPACITY',120,2,'APPROVED','2026-06-16 00:16:26'),(3,2,'AVG_STAY_HOURS',5,6,'APPROVED','2026-06-16 00:17:02');
/*!40000 ALTER TABLE `pending_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `traveler_id` int NOT NULL,
  `traveler_type` enum('VISITOR','SUBSCRIBER','GUIDE') NOT NULL DEFAULT 'VISITOR',
  `park_id` int NOT NULL,
  `visit_date` date NOT NULL,
  `entry_time` time NOT NULL,
  `num_visitors` int NOT NULL DEFAULT '1',
  `email` varchar(100) DEFAULT NULL,
  `type` enum('INDIVIDUAL','GROUP','WALK_IN','SUBSCRIBER') NOT NULL,
  `status` enum('PENDING','CONFIRMED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'PENDING',
  `confirmation_code` varchar(20) NOT NULL,
  `is_prepaid` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `confirmation_code` (`confirmation_code`),
  KEY `park_id` (`park_id`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`park_id`) REFERENCES `parks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES (20,1,'VISITOR',1,'2026-06-15','10:00:00',1,'sho@gmail.com','INDIVIDUAL','CONFIRMED','88102691',0);
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscribers`
--

DROP TABLE IF EXISTS `subscribers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscribers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `id_number` varchar(20) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `family_size` int DEFAULT '1',
  `credit_card` varchar(20) DEFAULT NULL,
  `subscriber_number` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_number` (`id_number`),
  UNIQUE KEY `subscriber_number` (`subscriber_number`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscribers`
--

LOCK TABLES `subscribers` WRITE;
/*!40000 ALTER TABLE `subscribers` DISABLE KEYS */;
INSERT INTO `subscribers` VALUES (3,'hadi','shofania','212152979','0549866600','hodhod@gmail.com',13,NULL,'27013166'),(4,'hadi','haha','2121529793','0549866600','hee@gmail.com',1,NULL,'90735172'),(5,'hadaaaa','hahaasssdd','254785965','0549866666','he232e@gmail.com',4,NULL,'50239734'),(6,'dsa','asd','212123434','0549866600','gma@gmail.com',1,NULL,'82111251');
/*!40000 ALTER TABLE `subscribers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visitors`
--

DROP TABLE IF EXISTS `visitors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitors` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_number` varchar(20) NOT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_number` (`id_number`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visitors`
--

LOCK TABLES `visitors` WRITE;
/*!40000 ALTER TABLE `visitors` DISABLE KEYS */;
INSERT INTO `visitors` VALUES (1,'111111111','Yarin','Shapiro','050-3333333','yarin@mail.com'),(2,'222222222','Bayan','Nasser','052-4444444','bayan@mail.com');
/*!40000 ALTER TABLE `visitors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waiting_list`
--

DROP TABLE IF EXISTS `waiting_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waiting_list` (
  `id` int NOT NULL AUTO_INCREMENT,
  `traveler_id` int NOT NULL,
  `traveler_type` enum('VISITOR','SUBSCRIBER','GUIDE') NOT NULL DEFAULT 'VISITOR',
  `park_id` int NOT NULL,
  `visit_date` date NOT NULL,
  `entry_time` time NOT NULL,
  `num_visitors` int NOT NULL DEFAULT '1',
  `email` varchar(100) DEFAULT NULL,
  `position` int NOT NULL,
  `notified_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `park_id` (`park_id`),
  CONSTRAINT `waiting_list_ibfk_1` FOREIGN KEY (`park_id`) REFERENCES `parks` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waiting_list`
--

LOCK TABLES `waiting_list` WRITE;
/*!40000 ALTER TABLE `waiting_list` DISABLE KEYS */;
/*!40000 ALTER TABLE `waiting_list` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-16  3:30:22
