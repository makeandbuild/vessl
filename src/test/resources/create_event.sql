DROP TABLE IF EXISTS `event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event` (
  `event_id` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `parent_event_id` varchar(255) DEFAULT NULL,
  `user_id` int UNSIGNED NULL,
  PRIMARY KEY (`event_id`),
  KEY `FK_parent_event` (`parent_event_id`),
  CONSTRAINT `FK_parent_event` FOREIGN KEY (`parent_event_id`) REFERENCES `event` (`event_id`),
  KEY `FK_event_user` (`user_id`),
  CONSTRAINT `FK_event_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;