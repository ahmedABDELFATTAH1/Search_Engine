
CREATE TABLE `crawler_urls` (
  `url_id` bigint(20) UNSIGNED NOT NULL,
  `url` varchar(2048) COLLATE utf16_unicode_ci NOT NULL,
  `is_crawled` tinyint(1) NOT NULL DEFAULT 0,
  `revisit_priporty` tinyint(1) NOT NULL DEFAULT 0,
  `hyperlinks_hash` text COLLATE utf16_unicode_ci DEFAULT NULL,
  `created_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `modified_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf16 COLLATE=utf16_unicode_ci;


CREATE TABLE `forbidden_urls` (
  `url_id` bigint(20) UNSIGNED NOT NULL,
  `url` varchar(2048) COLLATE utf16_unicode_ci NOT NULL,
  `created_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `modified_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf16 COLLATE=utf16_unicode_ci;


CREATE TABLE `hosts_popularity` (
  `host_id` bigint(20) UNSIGNED NOT NULL,
  `host_name` varchar(300) COLLATE utf16_unicode_ci NOT NULL,
  `host_ref_times` int(11) NOT NULL DEFAULT 0,
  `created_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `modified_timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf16 COLLATE=utf16_unicode_ci;


--
-- Indexes for table `crawler_urls`
--
ALTER TABLE `crawler_urls`
  ADD PRIMARY KEY (`url_id`),
  ADD UNIQUE KEY `url_id` (`url_id`),
  ADD UNIQUE KEY `url` (`url`) USING HASH;

--
-- Indexes for table `forbidden_urls`
--
ALTER TABLE `forbidden_urls`
  ADD PRIMARY KEY (`url_id`),
  ADD UNIQUE KEY `url_id` (`url_id`),
  ADD UNIQUE KEY `url` (`url`) USING HASH;

--
-- Indexes for table `hosts_popularity`
--
ALTER TABLE `hosts_popularity`
  ADD PRIMARY KEY (`host_id`),
  ADD UNIQUE KEY `host_id` (`host_id`),
  ADD UNIQUE KEY `host_name` (`host_name`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `crawler_urls`
--
ALTER TABLE `crawler_urls`
  MODIFY `url_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5040;

--
-- AUTO_INCREMENT for table `forbidden_urls`
--
ALTER TABLE `forbidden_urls`
  MODIFY `url_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=846;

--
-- AUTO_INCREMENT for table `hosts_popularity`
--
ALTER TABLE `hosts_popularity`
  MODIFY `host_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;
COMMIT;


-- insert into crawler_urls(url) values('https://www.google.com/search/howsearchworks/');
-- insert into hosts_popularity(host_name,host_ref_times)values('www.google.com',1);