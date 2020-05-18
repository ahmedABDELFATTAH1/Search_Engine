
CREATE TABLE `crawler_urls` (
  `url_id` bigint(20) PRIMARY KEY NOT NULL auto_increment,
  `url` varchar(512) unique COLLATE utf16_unicode_ci NOT NULL,
  `is_crawled` tinyint(1) NOT NULL DEFAULT 0,
  `revisit_priporty` tinyint(1) NOT NULL DEFAULT 0,
  `hyperlinks_hash` text COLLATE utf16_unicode_ci DEFAULT NULL,
  `created_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `modified_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf16 COLLATE=utf16_unicode_ci;


CREATE TABLE `forbidden_urls` (
  `url_id` bigint(20) PRIMARY KEY NOT NULL auto_increment,
  `url` varchar(512) unique COLLATE utf16_unicode_ci NOT NULL,
  `created_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `modified_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf16 COLLATE=utf16_unicode_ci;


CREATE TABLE `hosts_popularity` (
  `host_id` bigint(20) PRIMARY KEY NOT NULL auto_increment,
  `host_name` varchar(300) unique COLLATE utf16_unicode_ci NOT NULL,
  `host_ref_times` int(11) NOT NULL DEFAULT 0,
  `created_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `modified_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf16 COLLATE=utf16_unicode_ci;


