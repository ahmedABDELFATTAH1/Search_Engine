CREATE TABLE `crawler_urls` (
  `url_id` bigint(20) AUTO_INCREMENT NOT NULL PRIMARY KEY,
  `url` varchar(2048) NOT NULL UNIQUE,
  `is_crawled` tinyint(1) NOT NULL DEFAULT 0,
  `revisit_priporty` tinyint(1) NOT NULL DEFAULT 0,
  `check_sum` int(11) COLLATE utf16_unicode_ci DEFAULT 0,
  `created_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `modified_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
)

CREATE TABLE `forbidden_urls` (
  `url_id` bigint(20) AUTO_INCREMENT NOT NULL PRIMARY KEY,
  `url` varchar(2048) COLLATE utf16_unicode_ci NOT NULL UNIQUE,
  `created_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `modified_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) 

CREATE TABLE `hosts_popularity` (
  `host_id` bigint(20) AUTO_INCREMENT NOT NULL PRIMARY KEY,
  `host_name` varchar(300) COLLATE utf16_unicode_ci NOT NULL UNIQUE,
  `host_ref_times` int(11) NOT NULL DEFAULT 0,
  `created_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `modified_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) 
