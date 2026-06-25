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
INSERT INTO `active_visitors` VALUES (1,0),(2,0),(3,0),(4,0);
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES (1,'Dana','Cohen','EMP001','dana123@gmail.com','PARK_WORKER',1,'dana','1234'),(2,'Yossi','Levy','EMP002','yossi@gonature.com','PARK_MANAGER',1,'yossi','1234'),(3,'Rina','Mizrahi','EMP003','rina@gonature.com','DEPARTMENT_MANAGER',NULL,'rina','1234'),(4,'Tal','Ben Amia','EMP004','tal@gonature.com','SERVICE_REP',NULL,'tal','1234'),(6,'David','Levi','E005','david@gonature.com','PARK_MANAGER',2,'david','1234'),(7,'Noa','Shapira','2001','noa@gonature.com','PARK_WORKER',2,'noa','1234'),(8,'Eli','Katz','3001','eli@gonature.com','PARK_MANAGER',3,'eli','1234'),(9,'Maya','Peretz','3002','maya@gonature.com','PARK_WORKER',3,'maya','1234'),(10,'Omer','Dayan','4001','omer@gonature.com','PARK_MANAGER',4,'omer','1234'),(11,'Lihi','Golan','4002','lihi@gonature.com','PARK_WORKER',4,'lihi','1234');
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guides`
--

LOCK TABLES `guides` WRITE;
/*!40000 ALTER TABLE `guides` DISABLE KEYS */;
INSERT INTO `guides` VALUES (6,'tamer','tamer@gmail.com','0549866600','1234','tamer','1234');
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
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `park_visits`
--

LOCK TABLES `park_visits` WRITE;
/*!40000 ALTER TABLE `park_visits` DISABLE KEYS */;
INSERT INTO `park_visits` VALUES (36,129,1,'2026-05-01 08:00:00','2026-05-01 12:00:00',3,'INDIVIDUAL'),(37,130,1,'2026-05-02 09:00:00','2026-05-02 13:00:00',2,'INDIVIDUAL'),(38,131,1,'2026-05-03 10:00:00','2026-05-03 14:00:00',4,'INDIVIDUAL'),(39,132,1,'2026-05-05 08:00:00','2026-05-05 12:00:00',1,'INDIVIDUAL'),(40,133,1,'2026-05-07 11:00:00','2026-05-07 15:00:00',5,'INDIVIDUAL'),(41,134,1,'2026-05-08 09:00:00','2026-05-08 13:00:00',2,'INDIVIDUAL'),(42,135,1,'2026-05-10 10:00:00','2026-05-10 14:00:00',3,'INDIVIDUAL'),(43,136,1,'2026-05-12 08:00:00','2026-05-12 12:00:00',4,'INDIVIDUAL'),(44,137,1,'2026-05-14 09:00:00','2026-05-14 13:00:00',2,'INDIVIDUAL'),(45,138,1,'2026-05-15 10:00:00','2026-05-15 14:00:00',6,'INDIVIDUAL'),(46,139,1,'2026-05-03 08:00:00','2026-05-03 12:00:00',10,'GROUP'),(47,140,1,'2026-05-06 09:00:00','2026-05-06 13:00:00',8,'GROUP'),(48,141,1,'2026-05-09 10:00:00','2026-05-09 14:00:00',12,'GROUP'),(49,142,1,'2026-05-13 08:00:00','2026-05-13 12:00:00',15,'GROUP'),(50,143,1,'2026-05-16 09:00:00','2026-05-16 13:00:00',7,'GROUP'),(51,144,1,'2026-05-04 08:00:00','2026-05-04 12:00:00',3,'INDIVIDUAL'),(52,145,1,'2026-05-11 10:00:00','2026-05-11 14:00:00',4,'INDIVIDUAL'),(53,146,1,'2026-05-18 09:00:00','2026-05-18 13:00:00',2,'INDIVIDUAL'),(54,157,2,'2026-05-01 08:00:00','2026-05-01 12:00:00',4,'INDIVIDUAL'),(55,158,2,'2026-05-03 09:00:00','2026-05-03 13:00:00',6,'GROUP'),(56,159,2,'2026-05-05 10:00:00','2026-05-05 14:00:00',3,'INDIVIDUAL'),(57,160,2,'2026-05-08 08:00:00','2026-05-08 12:00:00',8,'GROUP'),(58,161,2,'2026-05-10 09:00:00','2026-05-10 13:00:00',2,'INDIVIDUAL'),(59,162,2,'2026-05-12 10:00:00','2026-05-12 14:00:00',5,'INDIVIDUAL'),(60,163,2,'2026-05-15 08:00:00','2026-05-15 12:00:00',4,'GROUP'),(61,169,3,'2026-05-02 08:00:00','2026-05-02 12:00:00',5,'INDIVIDUAL'),(62,170,3,'2026-05-04 09:00:00','2026-05-04 13:00:00',8,'GROUP'),(63,171,3,'2026-05-06 10:00:00','2026-05-06 14:00:00',3,'INDIVIDUAL'),(64,172,3,'2026-05-09 08:00:00','2026-05-09 12:00:00',6,'GROUP'),(65,173,3,'2026-05-11 09:00:00','2026-05-11 13:00:00',4,'INDIVIDUAL'),(66,174,3,'2026-05-13 10:00:00','2026-05-13 14:00:00',2,'INDIVIDUAL'),(67,179,4,'2026-05-01 08:00:00','2026-05-01 12:00:00',6,'GROUP'),(68,180,4,'2026-05-03 09:00:00','2026-05-03 13:00:00',4,'INDIVIDUAL'),(69,181,4,'2026-05-05 10:00:00','2026-05-05 14:00:00',8,'GROUP'),(70,182,4,'2026-05-07 08:00:00','2026-05-07 12:00:00',3,'INDIVIDUAL'),(71,183,4,'2026-05-09 09:00:00','2026-05-09 13:00:00',5,'GROUP'),(72,184,4,'2026-05-11 10:00:00','2026-05-11 14:00:00',2,'INDIVIDUAL'),(99,189,1,'2026-05-02 08:00:00','2026-05-02 12:00:00',2,'INDIVIDUAL'),(100,190,1,'2026-05-05 09:00:00','2026-05-05 13:00:00',3,'INDIVIDUAL'),(101,191,2,'2026-05-07 10:00:00','2026-05-07 14:00:00',4,'INDIVIDUAL'),(102,192,3,'2026-05-10 08:00:00','2026-05-10 12:00:00',2,'INDIVIDUAL'),(103,193,4,'2026-05-12 09:00:00','2026-05-12 13:00:00',3,'INDIVIDUAL'),(104,194,1,'2026-05-03 10:00:00','2026-05-03 14:00:00',4,'INDIVIDUAL'),(105,195,2,'2026-05-08 08:00:00','2026-05-08 12:00:00',2,'INDIVIDUAL'),(106,196,3,'2026-05-14 09:00:00','2026-05-14 13:00:00',3,'INDIVIDUAL');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parks`
--

LOCK TABLES `parks` WRITE;
/*!40000 ALTER TABLE `parks` DISABLE KEYS */;
INSERT INTO `parks` VALUES (1,'Carmel National Park',120,0,4,100,0,0),(2,'Ein Gedi Nature Reserve',80,15,5,100,0,0),(3,'Banias Nature Reserve',60,10,4,100,0,0),(4,'Hermon Stream Reserve',70,10,4,100,0,0);
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pending_requests`
--

LOCK TABLES `pending_requests` WRITE;
/*!40000 ALTER TABLE `pending_requests` DISABLE KEYS */;
INSERT INTO `pending_requests` VALUES (13,1,'MAX_CAPACITY',120,2,'APPROVED','2026-06-24 18:48:16');
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
  `status` enum('PENDING','CONFIRMED','CANCELLED','NO_SHOW','INSIDE','EXITED') DEFAULT NULL,
  `confirmation_code` varchar(20) NOT NULL,
  `is_prepaid` tinyint(1) NOT NULL DEFAULT '0',
  `reminder_sent` tinyint(1) DEFAULT '0',
  `reminder_sent_at` datetime DEFAULT NULL,
  `reminder_confirmed` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `confirmation_code` (`confirmation_code`),
  KEY `park_id` (`park_id`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`park_id`) REFERENCES `parks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=197 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES (129,6,'GUIDE',1,'2026-05-01','08:00:00',3,'t@t.com','INDIVIDUAL','EXITED','10000001',0,0,NULL,0),(130,6,'GUIDE',1,'2026-05-02','09:00:00',2,'t@t.com','INDIVIDUAL','EXITED','10000002',0,0,NULL,0),(131,6,'GUIDE',1,'2026-05-03','10:00:00',4,'t@t.com','INDIVIDUAL','EXITED','10000003',0,0,NULL,0),(132,6,'GUIDE',1,'2026-05-05','08:00:00',1,'t@t.com','INDIVIDUAL','EXITED','10000004',0,0,NULL,0),(133,6,'GUIDE',1,'2026-05-07','11:00:00',5,'t@t.com','INDIVIDUAL','EXITED','10000005',0,0,NULL,0),(134,6,'GUIDE',1,'2026-05-08','09:00:00',2,'t@t.com','INDIVIDUAL','EXITED','10000006',0,0,NULL,0),(135,6,'GUIDE',1,'2026-05-10','10:00:00',3,'t@t.com','INDIVIDUAL','EXITED','10000007',0,0,NULL,0),(136,6,'GUIDE',1,'2026-05-12','08:00:00',4,'t@t.com','INDIVIDUAL','EXITED','10000008',0,0,NULL,0),(137,6,'GUIDE',1,'2026-05-14','09:00:00',2,'t@t.com','INDIVIDUAL','EXITED','10000009',0,0,NULL,0),(138,6,'GUIDE',1,'2026-05-15','10:00:00',6,'t@t.com','INDIVIDUAL','EXITED','10000010',0,0,NULL,0),(139,6,'GUIDE',1,'2026-05-03','08:00:00',10,'t@t.com','GROUP','EXITED','10000011',0,0,NULL,0),(140,6,'GUIDE',1,'2026-05-06','09:00:00',8,'t@t.com','GROUP','EXITED','10000012',1,0,NULL,0),(141,6,'GUIDE',1,'2026-05-09','10:00:00',12,'t@t.com','GROUP','EXITED','10000013',0,0,NULL,0),(142,6,'GUIDE',1,'2026-05-13','08:00:00',15,'t@t.com','GROUP','EXITED','10000014',1,0,NULL,0),(143,6,'GUIDE',1,'2026-05-16','09:00:00',7,'t@t.com','GROUP','EXITED','10000015',0,0,NULL,0),(144,6,'GUIDE',1,'2026-05-04','08:00:00',3,'t@t.com','INDIVIDUAL','EXITED','10000016',0,0,NULL,0),(145,6,'GUIDE',1,'2026-05-11','10:00:00',4,'t@t.com','INDIVIDUAL','EXITED','10000017',0,0,NULL,0),(146,6,'GUIDE',1,'2026-05-18','09:00:00',2,'t@t.com','INDIVIDUAL','EXITED','10000018',0,0,NULL,0),(147,6,'GUIDE',1,'2026-05-02','10:00:00',2,'t@t.com','INDIVIDUAL','CANCELLED','10000019',0,0,NULL,0),(148,6,'GUIDE',1,'2026-05-05','11:00:00',3,'t@t.com','INDIVIDUAL','CANCELLED','10000020',0,0,NULL,0),(149,6,'GUIDE',1,'2026-05-08','08:00:00',4,'t@t.com','GROUP','CANCELLED','10000021',0,0,NULL,0),(150,6,'GUIDE',1,'2026-05-12','09:00:00',2,'t@t.com','INDIVIDUAL','CANCELLED','10000022',0,0,NULL,0),(151,6,'GUIDE',1,'2026-05-17','10:00:00',5,'t@t.com','GROUP','CANCELLED','10000023',0,0,NULL,0),(152,6,'GUIDE',1,'2026-05-20','08:00:00',3,'t@t.com','INDIVIDUAL','CANCELLED','10000024',0,0,NULL,0),(153,6,'GUIDE',1,'2026-05-04','09:00:00',2,'t@t.com','INDIVIDUAL','NO_SHOW','10000025',0,0,NULL,0),(154,6,'GUIDE',1,'2026-05-07','10:00:00',4,'t@t.com','INDIVIDUAL','NO_SHOW','10000026',0,0,NULL,0),(155,6,'GUIDE',1,'2026-05-10','08:00:00',3,'t@t.com','GROUP','NO_SHOW','10000027',0,0,NULL,0),(156,6,'GUIDE',1,'2026-05-15','11:00:00',2,'t@t.com','INDIVIDUAL','NO_SHOW','10000028',0,0,NULL,0),(157,6,'GUIDE',2,'2026-05-01','08:00:00',4,'t@t.com','INDIVIDUAL','EXITED','20000001',0,0,NULL,0),(158,6,'GUIDE',2,'2026-05-03','09:00:00',6,'t@t.com','GROUP','EXITED','20000002',0,0,NULL,0),(159,6,'GUIDE',2,'2026-05-05','10:00:00',3,'t@t.com','INDIVIDUAL','EXITED','20000003',0,0,NULL,0),(160,6,'GUIDE',2,'2026-05-08','08:00:00',8,'t@t.com','GROUP','EXITED','20000004',1,0,NULL,0),(161,6,'GUIDE',2,'2026-05-10','09:00:00',2,'t@t.com','INDIVIDUAL','EXITED','20000005',0,0,NULL,0),(162,6,'GUIDE',2,'2026-05-12','10:00:00',5,'t@t.com','INDIVIDUAL','EXITED','20000006',0,0,NULL,0),(163,6,'GUIDE',2,'2026-05-15','08:00:00',4,'t@t.com','GROUP','EXITED','20000007',0,0,NULL,0),(164,6,'GUIDE',2,'2026-05-02','09:00:00',3,'t@t.com','INDIVIDUAL','CANCELLED','20000008',0,0,NULL,0),(165,6,'GUIDE',2,'2026-05-06','10:00:00',2,'t@t.com','INDIVIDUAL','CANCELLED','20000009',0,0,NULL,0),(166,6,'GUIDE',2,'2026-05-09','08:00:00',4,'t@t.com','GROUP','CANCELLED','20000010',0,0,NULL,0),(167,6,'GUIDE',2,'2026-05-04','09:00:00',3,'t@t.com','INDIVIDUAL','NO_SHOW','20000011',0,0,NULL,0),(168,6,'GUIDE',2,'2026-05-11','10:00:00',2,'t@t.com','INDIVIDUAL','NO_SHOW','20000012',0,0,NULL,0),(169,6,'GUIDE',3,'2026-05-02','08:00:00',5,'t@t.com','INDIVIDUAL','EXITED','30000001',0,0,NULL,0),(170,6,'GUIDE',3,'2026-05-04','09:00:00',8,'t@t.com','GROUP','EXITED','30000002',1,0,NULL,0),(171,6,'GUIDE',3,'2026-05-06','10:00:00',3,'t@t.com','INDIVIDUAL','EXITED','30000003',0,0,NULL,0),(172,6,'GUIDE',3,'2026-05-09','08:00:00',6,'t@t.com','GROUP','EXITED','30000004',0,0,NULL,0),(173,6,'GUIDE',3,'2026-05-11','09:00:00',4,'t@t.com','INDIVIDUAL','EXITED','30000005',0,0,NULL,0),(174,6,'GUIDE',3,'2026-05-13','10:00:00',2,'t@t.com','INDIVIDUAL','EXITED','30000006',0,0,NULL,0),(175,6,'GUIDE',3,'2026-05-03','08:00:00',3,'t@t.com','INDIVIDUAL','CANCELLED','30000007',0,0,NULL,0),(176,6,'GUIDE',3,'2026-05-07','09:00:00',5,'t@t.com','GROUP','CANCELLED','30000008',0,0,NULL,0),(177,6,'GUIDE',3,'2026-05-05','10:00:00',2,'t@t.com','INDIVIDUAL','NO_SHOW','30000009',0,0,NULL,0),(178,6,'GUIDE',3,'2026-05-10','08:00:00',4,'t@t.com','INDIVIDUAL','NO_SHOW','30000010',0,0,NULL,0),(179,6,'GUIDE',4,'2026-05-01','08:00:00',6,'t@t.com','GROUP','EXITED','40000001',1,0,NULL,0),(180,6,'GUIDE',4,'2026-05-03','09:00:00',4,'t@t.com','INDIVIDUAL','EXITED','40000002',0,0,NULL,0),(181,6,'GUIDE',4,'2026-05-05','10:00:00',8,'t@t.com','GROUP','EXITED','40000003',0,0,NULL,0),(182,6,'GUIDE',4,'2026-05-07','08:00:00',3,'t@t.com','INDIVIDUAL','EXITED','40000004',0,0,NULL,0),(183,6,'GUIDE',4,'2026-05-09','09:00:00',5,'t@t.com','GROUP','EXITED','40000005',1,0,NULL,0),(184,6,'GUIDE',4,'2026-05-11','10:00:00',2,'t@t.com','INDIVIDUAL','EXITED','40000006',0,0,NULL,0),(185,6,'GUIDE',4,'2026-05-02','08:00:00',4,'t@t.com','INDIVIDUAL','CANCELLED','40000007',0,0,NULL,0),(186,6,'GUIDE',4,'2026-05-06','09:00:00',3,'t@t.com','GROUP','CANCELLED','40000008',0,0,NULL,0),(187,6,'GUIDE',4,'2026-05-04','10:00:00',2,'t@t.com','INDIVIDUAL','NO_SHOW','40000009',0,0,NULL,0),(188,6,'GUIDE',4,'2026-05-08','08:00:00',5,'t@t.com','INDIVIDUAL','NO_SHOW','40000010',0,0,NULL,0),(189,9,'SUBSCRIBER',1,'2026-05-02','08:00:00',2,'t@t.com','INDIVIDUAL','EXITED','50000001',0,0,NULL,0),(190,9,'SUBSCRIBER',1,'2026-05-05','09:00:00',3,'t@t.com','INDIVIDUAL','EXITED','50000002',0,0,NULL,0),(191,9,'SUBSCRIBER',2,'2026-05-07','10:00:00',4,'t@t.com','INDIVIDUAL','EXITED','50000003',0,0,NULL,0),(192,9,'SUBSCRIBER',3,'2026-05-10','08:00:00',2,'t@t.com','INDIVIDUAL','EXITED','50000004',0,0,NULL,0),(193,9,'SUBSCRIBER',4,'2026-05-12','09:00:00',3,'t@t.com','INDIVIDUAL','EXITED','50000005',0,0,NULL,0),(194,10,'SUBSCRIBER',1,'2026-05-03','10:00:00',4,'t@t.com','INDIVIDUAL','EXITED','50000006',0,0,NULL,0),(195,10,'SUBSCRIBER',2,'2026-05-08','08:00:00',2,'t@t.com','INDIVIDUAL','EXITED','50000007',0,0,NULL,0),(196,10,'SUBSCRIBER',3,'2026-05-14','09:00:00',3,'t@t.com','INDIVIDUAL','EXITED','50000008',0,0,NULL,0);
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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscribers`
--

LOCK TABLES `subscribers` WRITE;
/*!40000 ALTER TABLE `subscribers` DISABLE KEYS */;
INSERT INTO `subscribers` VALUES (13,'hadi','shofania','212152979','0549866600','hodhod@gmail.com',10,NULL,'24665023'),(14,'Neta','Bar','444444444','0504444444','neta@test.com',3,'1234567890123456','100001');
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visitors`
--

LOCK TABLES `visitors` WRITE;
/*!40000 ALTER TABLE `visitors` DISABLE KEYS */;
INSERT INTO `visitors` VALUES (11,'12345','alaa','alaa','0549866600','alaa@gmail.com'),(13,'123456','hadi','shofania','0549866600','hod@gmail.com'),(14,'1234','zed','zed','0549866600','hod@gmail.com'),(15,'111111111','Avi','Cohen','0501111111','avi@test.com');
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
  `status` enum('WAITING','NOTIFIED','CONFIRMED','EXPIRED','CANCELLED') DEFAULT 'WAITING',
  `offer_expires_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `park_id` (`park_id`),
  CONSTRAINT `waiting_list_ibfk_1` FOREIGN KEY (`park_id`) REFERENCES `parks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
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

-- Dump completed on 2026-06-25  3:19:43
