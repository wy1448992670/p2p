-- MySQL dump 10.13  Distrib 5.6.31, for Linux (x86_64)
--
-- Host: localhost    Database: p2p_dev
-- ------------------------------------------------------
-- Server version	5.6.31

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping routines for database 'p2p_dev'
--
/*!50003 DROP FUNCTION IF EXISTS `f_credit_levels` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `f_credit_levels`(userId bigint) RETURNS int(11)
BEGIN 





Declare auditItems INT(5) DEFAULT 0; 





Declare creditScore INT(10) DEFAULT 0; 





Declare size INT(2) DEFAULT 0; 





Declare overdueCount INT(10) DEFAULT 0; 





Declare minAuditItems INT(5) DEFAULT 0; 





Declare minCreditScore INT(10) DEFAULT 0; 





Declare isOverdue INT(2) DEFAULT 0; 





DECLARE i BIGINT(2) DEFAULT 0; 





DECLARE result BIGINT(2) DEFAULT 0; 





DECLARE isAllowOverdue INT(2) DEFAULT 0; 











SELECT COUNT(audit_item_id) INTO auditItems FROM t_user_audit_items WHERE user_id = userId AND status = 2 ORDER BY audit_item_id; 





SELECT credit_score INTO creditScore FROM t_users WHERE id = userId; 





SELECT COUNT(a.id) INTO overdueCount FROM t_bills as a JOIN t_bids AS b ON a.bid_id = b.id WHERE a.overdue_mark IN(-1,-2,-3) AND b.user_id = userId ORDER BY a.id; 





SELECT COUNT(id) INTO size FROM t_credit_levels; 











IF overdueCount > 0 THEN 





SET isOverdue = 1; 





end if; 











myloop:LOOP 





SET i = i + 1; 





IF i > size THEN 





LEAVE myloop; 





END IF; 











SELECT min_audit_items, min_credit_score, is_allow_overdue INTO minAuditItems, minCreditScore, isAllowOverdue FROM t_credit_levels WHERE order_sort = i; 











IF isAllowOverdue = 1 THEN 





IF minAuditItems <= auditItems && minCreditScore <= creditScore && isOverdue = 0 THEN 





SET result = i; LEAVE myloop; 





end if; 





end if; 











IF minAuditItems <= auditItems && minCreditScore <= creditScore THEN 





SET result = i; 





LEAVE myloop; 





END IF; 











END LOOP myloop; 











RETURN result; 





END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `f_get_split_count` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `f_get_split_count`(str varchar(1000), delimiter varchar(5)) RETURNS int(11)
BEGIN





	if length(str) = 0 then





        return 0;





    end if;











    return 1 + (length(str) - length(replace(str,delimiter,''))) / length(delimiter);





END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `f_user_audit_allitem` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `f_user_audit_allitem`(`p_user_id` bigint(20),`p_mark` varchar(100),`p_status` int(11)) RETURNS int(11)
BEGIN
Declare result INT(11) DEFAULT 0;
select count(temp.id) INTO  result from (select id from t_user_audit_items where user_id = p_user_id and status = p_status group by audit_item_id having audit_item_id in
(select audit_item_id from t_product_audit_items_log where mark = p_mark)) temp;
return result;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `f_user_audit_item` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `f_user_audit_item`(p_user_id bigint(20), p_mark varchar(100), p_status int(11)) RETURNS int(11)
BEGIN





Declare result INT(11) DEFAULT 0;





select count(temp.id) INTO  result from (select id from t_user_audit_items where user_id = p_user_id and status = p_status group by audit_item_id having audit_item_id in





(select audit_item_id from t_product_audit_items_log where mark = p_mark and type = 1)) temp;





return result;





END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `f_user_audit_submit_item` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` FUNCTION `f_user_audit_submit_item`(`p_user_id` bigint(20),`p_mark` varchar(100)) RETURNS int(11)
BEGIN
Declare result INT(11) DEFAULT 0;
select count(temp.id) INTO  result from (select id from t_user_audit_items where user_id = p_user_id and status != 0 group by audit_item_id having audit_item_id in
(select audit_item_id from t_product_audit_items where mark = p_mark)) temp;
return result;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `updateAgencies` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `updateAgencies`()
BEGIN
	update t_agencies t set t.bid_count = (SELECT	count(b.id)FROM	t_bids b WHERE(b.agency_id = t.id));		
	update t_agencies t set t.bid_avg_apr = (SELECT	avg(b.apr)FROM t_bids b	WHERE	(b.agency_id = t.id));	
	update t_agencies t set t.success_bid_count = (SELECT	count(b.id)	FROM t_bids b	WHERE((b.agency_id = t.id)AND(b.status = 4)));	
	update t_agencies t set t.overdue_bid_count = (SELECT	count(b.id) FROM t_bids b	JOIN t_bills bl WHERE((b.agency_id = t.id)AND(bl.bid_id = b.id)AND(bl.overdue_mark IN(-1 ,-2))));
	update t_agencies t set t.bad_bid_count = (SELECT count(b.id) 	FROM(t_bids b	JOIN t_bills bl	)	WHERE	((b.agency_id = t.id)	AND(bl.bid_id = b.id)	AND(bl.overdue_mark = -3)));	
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-08-28 16:20:32
