CREATE DATABASE  IF NOT EXISTS `coode_lab` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `coode_lab`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: coode_lab
-- ------------------------------------------------------
-- Server version	8.0.46

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
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `admin_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin1@coode.com','$2a$10$tom3Lp8p3KPKcIjbD6S5E.XnuQEwFEeRdJkKrZ917eSZgJnr5N4vS'),(2,'admin2@coode.com','$2a$10$nP.TFZjQKlDRr5gt0xaLQeY/WZ1kq0Nf3YjKIZiJ/HFosrRueIIGe');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `cart_item_id` bigint NOT NULL AUTO_INCREMENT,
  `price` decimal(10,2) NOT NULL,
  `product_quantity` int NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `cart_id` bigint NOT NULL,
  `variant_id` bigint NOT NULL,
  PRIMARY KEY (`cart_item_id`),
  UNIQUE KEY `uk_cart_variant` (`cart_id`,`variant_id`),
  KEY `FK5yyw1o0dor9gmxfra1dqvn4qa` (`variant_id`),
  CONSTRAINT `FK5yyw1o0dor9gmxfra1dqvn4qa` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`),
  CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`cart_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (1,299.00,2,598.00,1,1),(2,899.00,1,899.00,1,6),(3,699.00,1,699.00,2,9),(4,999.00,2,1998.00,2,11);
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `cart_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `total_quantity` int NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`cart_id`),
  UNIQUE KEY `UK64t7ox312pqal3p7fg9o503c2` (`user_id`),
  CONSTRAINT `FKb5o626f86h46m4s7ms6ginnop` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (1,'2026-09-07 17:18:13.694480',2,'2026-09-07 17:18:13.694480',1),(2,'2026-09-07 17:18:13.699161',2,'2026-09-07 17:18:13.699161',2);
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` bigint NOT NULL AUTO_INCREMENT,
  `price` decimal(10,2) NOT NULL,
  `price_total` decimal(10,2) NOT NULL,
  `product_quantity` int NOT NULL,
  `status` varchar(50) NOT NULL,
  `order_id` bigint NOT NULL,
  `variant_id` bigint NOT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  KEY `FKemq71edpbn9wsxnxncfn1algp` (`variant_id`),
  KEY `FKh2b04eyamwe2jqedwv3lbrx7f` (`vendor_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `FKemq71edpbn9wsxnxncfn1algp` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`),
  CONSTRAINT `FKh2b04eyamwe2jqedwv3lbrx7f` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`vendor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,299.00,598.00,2,'RECEIVED',1,1,1),(2,899.00,899.00,1,'RECEIVED',1,6,1),(3,699.00,699.00,1,'SHIPPED',2,9,2),(4,999.00,1998.00,2,'SHIPPED',2,11,2);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `recipient_address` varchar(255) NOT NULL,
  `recipient_name` varchar(100) NOT NULL,
  `recipient_phone` varchar(100) NOT NULL,
  `sum_total` decimal(10,2) NOT NULL,
  `total_amount` int NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`order_id`),
  KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'2026-09-07 17:18:13.741923','台北市中山區一段1號','小明','0912345678',1497.00,3,'2026-09-07 17:18:13.741923',1),(2,'2026-09-07 17:18:13.746638','台中市西屯區二段2號','小美','0987654321',3279.00,3,'2026-09-07 17:18:13.746638',2);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `outfit_items`
--

DROP TABLE IF EXISTS `outfit_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `outfit_items` (
  `outfititems_id` bigint NOT NULL AUTO_INCREMENT,
  `slot_type` varchar(100) NOT NULL,
  `outfit_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `variant_id` bigint NOT NULL,
  PRIMARY KEY (`outfititems_id`),
  UNIQUE KEY `uk_outfit_slot` (`outfit_id`,`slot_type`),
  KEY `FKkvma90ovttjs13ql1qtwv7mn6` (`product_id`),
  KEY `FKpli0res7uapg2dhkbaosh8v4j` (`variant_id`),
  CONSTRAINT `FK87wgrfdjuqofpa5kdypxybvyh` FOREIGN KEY (`outfit_id`) REFERENCES `outfits` (`outfit_id`),
  CONSTRAINT `FKkvma90ovttjs13ql1qtwv7mn6` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `FKpli0res7uapg2dhkbaosh8v4j` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `outfit_items`
--

LOCK TABLES `outfit_items` WRITE;
/*!40000 ALTER TABLE `outfit_items` DISABLE KEYS */;
INSERT INTO `outfit_items` VALUES (1,'UPPER_BODY',1,1,1),(2,'FULL_BODY',2,4,12);
/*!40000 ALTER TABLE `outfit_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `outfits`
--

DROP TABLE IF EXISTS `outfits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `outfits` (
  `outfit_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`outfit_id`),
  KEY `FKf249bhuwj850p7mbg5el7a5f9` (`user_id`),
  CONSTRAINT `FKf249bhuwj850p7mbg5el7a5f9` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `outfits`
--

LOCK TABLES `outfits` WRITE;
/*!40000 ALTER TABLE `outfits` DISABLE KEYS */;
INSERT INTO `outfits` VALUES (1,'週末休閒穿搭',1),(2,'上班通勤穿搭',2);
/*!40000 ALTER TABLE `outfits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_variants`
--

DROP TABLE IF EXISTS `product_variants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variants` (
  `variant_id` bigint NOT NULL AUTO_INCREMENT,
  `color` varchar(50) NOT NULL,
  `images_jpg` varchar(255) DEFAULT NULL,
  `outfit_png` varchar(255) DEFAULT NULL,
  `size` varchar(50) NOT NULL,
  `status` varchar(20) NOT NULL,
  `stock` int DEFAULT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`variant_id`),
  UNIQUE KEY `uk_product_variant` (`product_id`,`color`,`size`),
  CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_variants`
--

LOCK TABLES `product_variants` WRITE;
/*!40000 ALTER TABLE `product_variants` DISABLE KEYS */;
INSERT INTO `product_variants` VALUES (1,'白','/images/products/tops/1/white/product.jpg','/images/products/tops/1/white/outfit.png','M','ACTIVE',48,1),(2,'白','/images/products/tops/1/white/product.jpg','/images/products/tops/1/white/outfit.png','L','ACTIVE',28,1),(3,'黑','/images/products/tops/1/black/product.jpg','/images/products/tops/1/black/outfit.png','M','ACTIVE',20,1),(4,'黑','/images/products/tops/1/black/product.jpg','/images/products/tops/1/black/outfit.png','L','ACTIVE',12,1),(5,'黑','/images/products/tops/1/black/product.jpg','/images/products/tops/1/black/outfit.png','XL','INACTIVE',0,1),(6,'藍','/images/products/outerwear/2/blue/product.jpg','/images/products/outerwear/2/blue/outfit.png','M','ACTIVE',16,2),(7,'藍','/images/products/outerwear/2/blue/product.jpg','/images/products/outerwear/2/blue/outfit.png','L','ACTIVE',8,2),(8,'藍','/images/products/outerwear/2/blue/product.jpg','/images/products/outerwear/2/blue/outfit.png','XL','ACTIVE',6,2),(9,'藍','/images/products/pants/3/blue/product.jpg','/images/products/pants/3/blue/outfit.png','30','ACTIVE',18,3),(10,'藍','/images/products/pants/3/blue/product.jpg','/images/products/pants/3/blue/outfit.png','32','ACTIVE',22,3),(11,'黑','/images/products/dresses/4/black/product.jpg','/images/products/dresses/4/black/outfit.png','S','ACTIVE',15,4),(12,'黑','/images/products/dresses/4/black/product.jpg','/images/products/dresses/4/black/outfit.png','M','ACTIVE',12,4),(13,'白','/images/products/dresses/4/white/product.jpg','/images/products/dresses/4/white/outfit.png','M','ACTIVE',9,4),(14,'淺藍','/images/products/tops/5/light-blue/product.jpg','/images/products/tops/5/light-blue/outfit.png','S','ACTIVE',2,5),(15,'淺藍','/images/products/tops/5/light-blue/product.jpg','/images/products/tops/5/light-blue/outfit.png','M','ACTIVE',4,5),(16,'淺藍','/images/products/tops/5/light-blue/product.jpg','/images/products/tops/5/light-blue/outfit.png','L','ACTIVE',10,5),(17,'藍','/images/products/tops/6/blue/product.jpg','/images/products/tops/6/blue/outfit.png','M','ACTIVE',0,6),(18,'藍','/images/products/tops/6/blue/product.jpg','/images/products/tops/6/blue/outfit.png','L','ACTIVE',20,6),(19,'黑','/images/products/tops/6/black/product.jpg','/images/products/tops/6/black/outfit.png','L','ACTIVE',8,6),(20,'黑','/images/products/headwear/7/black/product.jpg','/images/products/headwear/7/black/outfit.png','F','ACTIVE',25,7),(21,'黑','/images/products/headwear/7/black/product.jpg','/images/products/headwear/7/black/outfit.png','U','ACTIVE',18,7),(22,'深藍','/images/products/tops/8/navy/product.jpg','/images/products/tops/8/navy/outfit.png','S','ACTIVE',6,8),(23,'深藍','/images/products/tops/8/navy/product.jpg','/images/products/tops/8/navy/outfit.png','M','ACTIVE',12,8),(24,'深藍','/images/products/tops/8/navy/product.jpg','/images/products/tops/8/navy/outfit.png','L','ACTIVE',8,8),(25,'綠','/images/products/dresses/9/green/product.jpg','/images/products/dresses/9/green/outfit.png','S','ACTIVE',10,9),(26,'綠','/images/products/dresses/9/green/product.jpg','/images/products/dresses/9/green/outfit.png','M','ACTIVE',8,9),(27,'碎花','/images/products/dresses/10/floral/product.jpg','/images/products/dresses/10/floral/outfit.png','S','ACTIVE',10,10),(28,'碎花','/images/products/dresses/10/floral/product.jpg','/images/products/dresses/10/floral/outfit.png','M','ACTIVE',8,10),(29,'藍','/images/products/skirts/11/blue/product.jpg','/images/products/skirts/11/blue/outfit.png','S','ACTIVE',10,11),(30,'藍','/images/products/skirts/11/blue/product.jpg','/images/products/skirts/11/blue/outfit.png','M','ACTIVE',8,11),(31,'藍','/images/products/skirts/12/blue/product.jpg','/images/products/skirts/12/blue/outfit.png','S','ACTIVE',10,12),(32,'藍','/images/products/skirts/12/blue/product.jpg','/images/products/skirts/12/blue/outfit.png','M','ACTIVE',8,12),(33,'橄欖綠','/images/products/pants/13/olive/product.jpg','/images/products/pants/13/olive/outfit.png','S','ACTIVE',12,13),(34,'橄欖綠','/images/products/pants/13/olive/product.jpg','/images/products/pants/13/olive/outfit.png','M','ACTIVE',10,13),(35,'卡其','/images/products/pants/13/beige/product.jpg','/images/products/pants/13/beige/outfit.png','S','ACTIVE',12,13),(36,'卡其','/images/products/pants/13/beige/product.jpg','/images/products/pants/13/beige/outfit.png','M','ACTIVE',10,13);
/*!40000 ALTER TABLE `product_variants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `category_type` varchar(100) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `images_jpg` varchar(255) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `outfit_png` varchar(255) DEFAULT NULL,
  `pattern` varchar(100) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `status` varchar(20) NOT NULL,
  `style` varchar(100) DEFAULT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`product_id`),
  KEY `FKs6kdu75k7ub4s95ydsr52p59s` (`vendor_id`),
  CONSTRAINT `FKs6kdu75k7ub4s95ydsr52p59s` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`vendor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'TOP','100%純棉，透氣舒適','/images/products/tops/1/white/product.jpg','純棉T恤','/images/products/tops/1/white/outfit.png','MEN',299.00,'ACTIVE','休閒',1),(2,'OUTER','經典牛仔，百搭款式','/images/products/outerwear/2/blue/product.jpg','牛仔外套','/images/products/outerwear/2/blue/outfit.png','MEN',899.00,'ACTIVE','街頭',1),(3,'BOTTOM','經典丹寧布料，百搭耐穿','/images/products/pants/3/blue/product.jpg','單寧長褲','/images/products/pants/3/blue/outfit.png','WOMEN',699.00,'ACTIVE','機能',2),(4,'DRESS','剪裁俐落，一件即可完成穿搭','/images/products/dresses/4/black/product.jpg','連身洋裝','/images/products/dresses/4/black/outfit.png','WOMEN',999.00,'ACTIVE','韓系',2),(5,'TOP','柔軟針織，秋冬必備','/images/products/tops/5/light-blue/product.jpg','針織毛衣','/images/products/tops/5/light-blue/outfit.png','WOMEN',499.00,'ACTIVE','韓系',1),(6,'TOP','商務休閒皆宜','/images/products/tops/6/blue/product.jpg','格紋襯衫','/images/products/tops/6/blue/outfit.png','MEN',599.00,'ACTIVE','正式',1),(7,'HEADWEAR','百搭帽款，男女童皆適','/images/products/headwear/7/black/product.jpg','棒球帽','/images/products/headwear/7/black/outfit.png','KIDS',399.00,'ACTIVE','街頭',1),(8,'TOP','深藍色針織，百搭保暖','/images/products/tops/8/navy/product.jpg','深藍針織上衣','/images/products/tops/8/navy/outfit.png','WOMEN',449.00,'ACTIVE','韓系',2),(9,'DRESS','飄逸綠色長裙，清新優雅','/images/products/dresses/9/green/product.jpg','綠色長裙','/images/products/dresses/9/green/outfit.png','WOMEN',1099.00,'ACTIVE','法式',2),(10,'DRESS','清新碎花，溫柔浪漫','/images/products/dresses/10/floral/product.jpg','碎花長裙','/images/products/dresses/10/floral/outfit.png','WOMEN',1299.00,'ACTIVE','田園',2),(11,'SKIRT','經典丹寧長裙，遮肉修飾身形','/images/products/skirts/11/blue/product.jpg','丹寧牛仔長裙','/images/products/skirts/11/blue/outfit.png','WOMEN',899.00,'ACTIVE','丹寧',2),(12,'SKIRT','青春俏皮，夏日清爽百搭','/images/products/skirts/12/blue/product.jpg','丹寧牛仔短裙','/images/products/skirts/12/blue/outfit.png','WOMEN',699.00,'ACTIVE','丹寧',2),(13,'BOTTOM','機能多口袋，硬挺耐磨','/images/products/pants/13/olive/product.jpg','工裝褲','/images/products/pants/13/olive/outfit.png','MEN',899.00,'ACTIVE','工裝',1);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return_items`
--

DROP TABLE IF EXISTS `return_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_items` (
  `return_item_id` bigint NOT NULL AUTO_INCREMENT,
  `approval_quantity` int NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `picture` longtext,
  `reason` varchar(255) DEFAULT NULL,
  `refund` decimal(10,2) NOT NULL,
  `rejected_quantity` int NOT NULL,
  `status` varchar(50) NOT NULL,
  `order_item_id` bigint NOT NULL,
  `return_requests_id` bigint NOT NULL,
  PRIMARY KEY (`return_item_id`),
  UNIQUE KEY `UKkkis1ns6nnmbuen70aoixhh2d` (`return_requests_id`),
  KEY `FK2nfjf1atvp0qkpjvd517m6cq7` (`order_item_id`),
  CONSTRAINT `FK2nfjf1atvp0qkpjvd517m6cq7` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`order_item_id`),
  CONSTRAINT `FKjwrtkpojj4kh9pfl3x6jeu6j5` FOREIGN KEY (`return_requests_id`) REFERENCES `return_requests` (`return_requests_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return_items`
--

LOCK TABLES `return_items` WRITE;
/*!40000 ALTER TABLE `return_items` DISABLE KEYS */;
INSERT INTO `return_items` VALUES (1,1,'換成更大尺寸',NULL,'尺寸不合',299.00,0,'APPROVED',1,1),(2,0,'縫線脫落',NULL,'商品瑕疵',0.00,0,'PENDING_REVIEW',3,2);
/*!40000 ALTER TABLE `return_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return_requests`
--

DROP TABLE IF EXISTS `return_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_requests` (
  `return_requests_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `request_type` varchar(50) NOT NULL,
  `return_request_quantity` int NOT NULL,
  `status` varchar(50) NOT NULL,
  `order_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `vendor_id` bigint NOT NULL,
  PRIMARY KEY (`return_requests_id`),
  KEY `FKbski88d6kewx0cbj5pk7nes01` (`order_id`),
  KEY `FK6pd9hi2rbbct43io2pgcma1sh` (`user_id`),
  KEY `FK7gpj5wb9bic19im6sienneq5k` (`vendor_id`),
  CONSTRAINT `FK6pd9hi2rbbct43io2pgcma1sh` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK7gpj5wb9bic19im6sienneq5k` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`vendor_id`),
  CONSTRAINT `FKbski88d6kewx0cbj5pk7nes01` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return_requests`
--

LOCK TABLES `return_requests` WRITE;
/*!40000 ALTER TABLE `return_requests` DISABLE KEYS */;
INSERT INTO `return_requests` VALUES (1,'2026-09-07 17:18:13.810710','RETURN',1,'REVIEWED',1,1,1),(2,'2026-09-07 17:18:13.815358','EXCHANGE',1,'PENDING',2,2,2);
/*!40000 ALTER TABLE `return_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `birthday` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `creditcard` varchar(50) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `gender` varchar(50) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `password` varchar(200) NOT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `picture` varchar(255) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'1995-05-20','2026-09-07 17:18:13.283881','4242424242424242','user1@coode.com','MALE','小明','$2a$10$/olmAwrEkKYAAkXekr.6zeWGwLikRjWdCgIKJzxzywMskCFQ/5jwm','0912345678','user1.jpg','ACTIVE','2026-09-07 17:18:13.283881'),(2,'1998-11-03','2026-09-07 17:18:13.291222','4111111111111111','user2@coode.com','FEMALE','小美','$2a$10$MbpJd6R1wfMcd2rYi/0geeWDb1bzcahwOrpTVClwjgoht5sGe3/LC','0987654321','user2.jpg','ACTIVE','2026-09-07 17:18:13.291222');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendors`
--

DROP TABLE IF EXISTS `vendors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendors` (
  `vendor_id` bigint NOT NULL AUTO_INCREMENT,
  `activated_at` datetime(6) DEFAULT NULL,
  `contract_expires_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `status` varchar(50) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `vendor_name` varchar(100) NOT NULL,
  PRIMARY KEY (`vendor_id`),
  UNIQUE KEY `UK8xmc0rpdougqaftbgr1s2tolo` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendors`
--

LOCK TABLES `vendors` WRITE;
/*!40000 ALTER TABLE `vendors` DISABLE KEYS */;
INSERT INTO `vendors` VALUES (1,'2026-09-07 17:18:13.387327','2027-09-07 17:18:13.387327','2026-09-07 17:18:13.463398','vendor1@coode.com','$2a$10$waaw2bNldZ7nI6DOEylT/uAk25AyXN5WhWNNw5zM0MkwpjoXJW7cC','ACTIVE','2026-09-07 17:18:13.463398','潮流服飾店'),(2,'2026-09-07 17:18:13.461095','2027-09-07 17:18:13.461095','2026-09-07 17:18:13.468136','vendor2@coode.com','$2a$10$K7Nqq9IWZYhzpxuhKXAeY.m1lxZVein9WpzSwRfkXXxXxzEBUwsaW','ACTIVE','2026-09-07 17:18:13.468136','舒適鞋鋪');
/*!40000 ALTER TABLE `vendors` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-07 17:29:02
