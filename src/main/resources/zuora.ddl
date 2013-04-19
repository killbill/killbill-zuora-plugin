/*! SET storage_engine=INNODB */;

DROP TABLE IF EXISTS _zuora_payments;
CREATE TABLE `_zuora_payments` (
  `kb_p_id` varchar(36) NOT NULL,
  `z_amount` decimal(10,4) NOT NULL,
  `z_created_date` datetime NOT NULL,
  `z_effective_date` datetime NOT NULL,
  `z_gateway_error` varchar(255) DEFAULT NULL,
  `z_gateway_error_code` varchar(32) DEFAULT NULL,
  `kb_account_id` varchar(36) NOT NULL,
  `z_reference_id` varchar(36) NOT NULL,
  `z_snd_reference_id` varchar(36) DEFAULT NULL,
  `z_status` varchar(64) NOT NULL,
  `z_p_id` varchar(64) NOT NULL,
  `last_updated` datetime DEFAULT NULL,
  PRIMARY KEY (`kb_p_id`),
  UNIQUE KEY `U__ZRTS_Z_P_ID` (`z_p_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS _zuora_payment_methods;
CREATE TABLE `_zuora_payment_methods` (
  `kb_pm_id` varchar(36) NOT NULL,
  `z_default` tinyint(1) DEFAULT '0',
  `kb_account_id` varchar(36) NOT NULL,
  `z_pm_id` varchar(64) NOT NULL,
  `last_updated` datetime DEFAULT NULL,
  PRIMARY KEY (`kb_pm_id`),
  UNIQUE KEY `U__ZRDS_Z_PM_ID` (`z_pm_id`),
  KEY `account_id` (`kb_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


