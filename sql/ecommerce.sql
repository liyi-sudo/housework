-- =============================================================
-- 电商平台数据库初始化脚本（B2B2C）
-- 执行方式: mysql -u root -p < ecommerce.sql
-- 说明：本脚本会创建数据库 ecommerce 并初始化演示数据
-- =============================================================

CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;

SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------------------------
-- 1. 用户表（网页端 + 小程序共用同一套账号）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `phone`        VARCHAR(20)  NOT NULL COMMENT '手机号（登录账号）',
  `password`     VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
  `nickname`     VARCHAR(50)  DEFAULT NULL,
  `avatar`       VARCHAR(500) DEFAULT NULL,
  `gender`       TINYINT      DEFAULT 0 COMMENT '0未知 1男 2女',
  `openid`       VARCHAR(64)  DEFAULT NULL COMMENT '微信小程序 openid（绑定后可一键登录）',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 2禁用',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -------------------------------------------------------------
-- 2. 收货地址表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT      NOT NULL,
  `receiver_name` VARCHAR(30) NOT NULL,
  `receiver_phone`VARCHAR(20) NOT NULL,
  `province`      VARCHAR(30) NOT NULL,
  `city`          VARCHAR(30) NOT NULL,
  `district`      VARCHAR(30) NOT NULL,
  `detail`        VARCHAR(200) NOT NULL,
  `is_default`    TINYINT     NOT NULL DEFAULT 0 COMMENT '1默认地址',
  `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- -------------------------------------------------------------
-- 3. 商品分类表（两级）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`        BIGINT      NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT      NOT NULL DEFAULT 0 COMMENT '0为一级分类',
  `name`      VARCHAR(30) NOT NULL,
  `sort`      INT         NOT NULL DEFAULT 0,
  `icon`      VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- -------------------------------------------------------------
-- 4. 商家表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `account`      VARCHAR(30)  NOT NULL COMMENT '商家登录账号',
  `password`     VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
  `contact_name` VARCHAR(30)  DEFAULT NULL,
  `contact_phone`VARCHAR(20)  DEFAULT NULL,
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 2冻结',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- -------------------------------------------------------------
-- 5. 店铺表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `merchant_id` BIGINT       NOT NULL,
  `name`        VARCHAR(60)  NOT NULL,
  `logo`        VARCHAR(500) DEFAULT NULL,
  `banner`      VARCHAR(500) DEFAULT NULL,
  `intro`       VARCHAR(500) DEFAULT NULL,
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 2冻结',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- -------------------------------------------------------------
-- 6. 入驻审核表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `merchant_apply`;
CREATE TABLE `merchant_apply` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `merchant_id`  BIGINT       NOT NULL,
  `shop_name`    VARCHAR(60)  NOT NULL,
  `category_ids` VARCHAR(200) COMMENT '主营类目',
  `contact_name` VARCHAR(30)  DEFAULT NULL,
  `contact_phone`VARCHAR(20)  DEFAULT NULL,
  `qualification`VARCHAR(500) COMMENT '资质信息',
  `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0待审核 1通过 2驳回',
  `reason`       VARCHAR(200) DEFAULT NULL COMMENT '驳回原因',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `audit_time`   DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入驻审核表';

-- -------------------------------------------------------------
-- 7. 商品表（SPU）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `shop_id`     BIGINT       NOT NULL,
  `category_id` BIGINT       NOT NULL,
  `name`        VARCHAR(120) NOT NULL,
  `subtitle`    VARCHAR(255) DEFAULT NULL,
  `main_image`  VARCHAR(500) NOT NULL,
  `images`      TEXT COMMENT '轮播图,逗号分隔',
  `detail`      TEXT COMMENT '图文详情',
  `price`       DECIMAL(10,2) NOT NULL COMMENT '起售价',
  `sales`       INT          NOT NULL DEFAULT 0 COMMENT '销量',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1在售 2下架 3审核中',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_shop` (`shop_id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- -------------------------------------------------------------
-- 8. SKU 表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `sku`;
CREATE TABLE `sku` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `goods_id`    BIGINT        NOT NULL,
  `spec`        VARCHAR(100)  NOT NULL COMMENT '规格属性值,如 颜色:红色;尺码:M',
  `price`       DECIMAL(10,2) NOT NULL,
  `stock`       INT           NOT NULL DEFAULT 0,
  `image`       VARCHAR(500)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格表';

-- -------------------------------------------------------------
-- 9. 购物车表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id`          BIGINT    NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT    NOT NULL,
  `goods_id`    BIGINT    NOT NULL,
  `sku_id`      BIGINT    NOT NULL,
  `quantity`    INT       NOT NULL DEFAULT 1,
  `checked`     TINYINT   NOT NULL DEFAULT 1 COMMENT '1勾选',
  `create_time` DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- -------------------------------------------------------------
-- 10. 订单表（状态机: 0待支付 1待发货 2待收货 3已完成 4已取消 5售后中）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`     VARCHAR(32)   NOT NULL COMMENT '订单号',
  `user_id`      BIGINT        NOT NULL,
  `shop_id`      BIGINT        NOT NULL,
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总价',
  `freight`      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '运费(模拟)',
  `discount`     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  `pay_amount`   DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `addr_snapshot`VARCHAR(300)  DEFAULT NULL COMMENT '地址快照',
  `coupon_id`    BIGINT        DEFAULT NULL,
  `remark`       VARCHAR(200)  DEFAULT NULL,
  `status`       TINYINT       NOT NULL DEFAULT 0,
  `pay_time`     DATETIME      DEFAULT NULL,
  `ship_time`    DATETIME      DEFAULT NULL,
  `finish_time`  DATETIME      DEFAULT NULL,
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orderno` (`order_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- -------------------------------------------------------------
-- 11. 订单项表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT        NOT NULL,
  `goods_id`      BIGINT        NOT NULL,
  `goods_name`    VARCHAR(120)  NOT NULL,
  `sku_id`        BIGINT        NOT NULL,
  `sku_spec`      VARCHAR(100)  DEFAULT NULL,
  `cover_image`   VARCHAR(500)  DEFAULT NULL,
  `price`         DECIMAL(10,2) NOT NULL,
  `quantity`      INT           NOT NULL,
  `review_status` TINYINT       NOT NULL DEFAULT 0 COMMENT '0未评价 1已评价',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- -------------------------------------------------------------
-- 12. 支付流水表（模拟支付）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `trade_no`   VARCHAR(32)   NOT NULL,
  `order_id`   BIGINT        NOT NULL,
  `user_id`    BIGINT        NOT NULL,
  `amount`     DECIMAL(10,2) NOT NULL,
  `method`     VARCHAR(20)   NOT NULL DEFAULT 'BALANCE' COMMENT 'BALANCE模拟余额 CARD模拟银行卡',
  `status`     TINYINT       NOT NULL DEFAULT 0 COMMENT '0待支付 1成功',
  `pay_time`   DATETIME      DEFAULT NULL,
  `create_time`DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tradeno` (`trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- -------------------------------------------------------------
-- 13. 物流单表（模拟）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `logistics`;
CREATE TABLE `logistics` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `order_id`    BIGINT      NOT NULL,
  `logistics_no`VARCHAR(32) NOT NULL,
  `company`     VARCHAR(30) NOT NULL DEFAULT '模拟快递',
  `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '0待揽收 1运输中 2已签收',
  `trace`       TEXT COMMENT '物流轨迹,分号分隔',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流表';

-- -------------------------------------------------------------
-- 14. 评价表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `order_item_id` BIGINT     NOT NULL,
  `goods_id`    BIGINT       NOT NULL,
  `shop_id`     BIGINT       NOT NULL,
  `rating`      TINYINT      NOT NULL COMMENT '1-5 星',
  `content`     VARCHAR(500) DEFAULT NULL,
  `images`      TEXT,
  `reply`       VARCHAR(500) DEFAULT NULL COMMENT '商家回复',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`),
  KEY `idx_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- -------------------------------------------------------------
-- 15. 售后表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `aftersale`;
CREATE TABLE `aftersale` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `order_id`    BIGINT       NOT NULL,
  `order_no`    VARCHAR(32)  NOT NULL,
  `user_id`     BIGINT       NOT NULL,
  `shop_id`     BIGINT       NOT NULL,
  `type`        TINYINT      NOT NULL COMMENT '1退款 2退货退款',
  `reason`      VARCHAR(200) NOT NULL,
  `refund_amount` DECIMAL(10,2) NOT NULL,
  `images`      TEXT,
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0待处理 1已同意 2已驳回 3已完成',
  `reply`       VARCHAR(200) DEFAULT NULL COMMENT '商家处理意见',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `handle_time` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后表';

-- -------------------------------------------------------------
-- 16. 优惠券表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(50)   NOT NULL,
  `threshold`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '满X元可用',
  `amount`     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '减免金额',
  `total`      INT           NOT NULL DEFAULT 0 COMMENT '发行量 0不限',
  `received`   INT           NOT NULL DEFAULT 0,
  `start_time` DATETIME      DEFAULT NULL,
  `end_time`   DATETIME      DEFAULT NULL,
  `status`     TINYINT       NOT NULL DEFAULT 1 COMMENT '1发放中 2停用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- -------------------------------------------------------------
-- 17. 用户优惠券表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `coupon_id`   BIGINT   NOT NULL,
  `status`      TINYINT  NOT NULL DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
  `receive_time`DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `use_time`    DATETIME DEFAULT NULL,
  `order_id`    BIGINT   DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- -------------------------------------------------------------
-- 18. 收藏表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `goods_id`    BIGINT   NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods` (`user_id`, `goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- -------------------------------------------------------------
-- 19. 浏览记录表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `browse_history`;
CREATE TABLE `browse_history` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `goods_id`    BIGINT   NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览记录表';

-- -------------------------------------------------------------
-- 20. 轮播图表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id`     BIGINT       NOT NULL AUTO_INCREMENT,
  `image`  VARCHAR(500) NOT NULL,
  `link`   VARCHAR(200) DEFAULT NULL COMMENT '跳转商品ID或空',
  `sort`   INT          NOT NULL DEFAULT 0,
  `status` TINYINT      NOT NULL DEFAULT 1 COMMENT '1展示 0隐藏',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- -------------------------------------------------------------
-- 21. 公告表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id`     BIGINT       NOT NULL AUTO_INCREMENT,
  `title`  VARCHAR(100) NOT NULL,
  `content`TEXT,
  `status` TINYINT      NOT NULL DEFAULT 1 COMMENT '1展示 0下线',
  `sort`   INT          NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- -------------------------------------------------------------
-- 22. 登录日志表（全端统一记录）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `username`   VARCHAR(50) NOT NULL COMMENT '登录账号',
  `user_type`  VARCHAR(20) NOT NULL COMMENT 'USER/MERCHANT/ADMIN',
  `client`     VARCHAR(20) DEFAULT NULL COMMENT 'PC/MINI/WEB',
  `ip`         VARCHAR(50) DEFAULT NULL,
  `result`     VARCHAR(20) NOT NULL COMMENT 'SUCCESS/FAIL/DISABLED',
  `message`    VARCHAR(200) DEFAULT NULL,
  `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- -------------------------------------------------------------
-- 23. 管理员表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `username`    VARCHAR(30)  NOT NULL,
  `password`    VARCHAR(100) NOT NULL,
  `nickname`    VARCHAR(30)  DEFAULT NULL,
  `status`      TINYINT      NOT NULL DEFAULT 1,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
-- 演示数据
-- =============================================================

-- 管理员: admin / admin123
INSERT INTO `admin` (`username`,`password`,`nickname`) VALUES
('admin', '$2a$10$YJavCoqoX2i0wqoyMxdO6edlFnsq0Vmwd8uLFI0zThTeVW4v1QVrK', '超级管理员');

-- 商家: merchant1 / 123456，店铺「优选数码旗舰店」
INSERT INTO `merchant` (`account`,`password`,`contact_name`,`contact_phone`) VALUES
('merchant1', '$2a$10$M9hRBeloKw73XZYun9s7eOuMXo6/AgCLxDgnQ3Q8tNqvl3ysdEv2u', '王掌柜', '13800000001');
INSERT INTO `shop` (`merchant_id`,`name`,`logo`,`banner`,`intro`,`status`) VALUES
(1, '优选数码旗舰店', 'https://loremflickr.com/200/200/shop,store?lock=800', '', '正品数码，七天无理由', 1);

-- 商品分类（两级）
INSERT INTO `category` (`parent_id`,`name`,`sort`) VALUES
(0,'数码家电',1),(0,'服饰鞋包',2),(0,'食品生鲜',3),(0,'美妆个护',4),(0,'家居生活',5);
INSERT INTO `category` (`parent_id`,`name`,`sort`) VALUES
(1,'手机',1),(1,'电脑',2),(1,'耳机音箱',3),
(2,'男装',1),(2,'女装',2),(2,'运动鞋',3),
(3,'休闲零食',1),(4,'护肤',1),(5,'厨具',1);

-- 演示商品（优选数码旗舰店）
INSERT INTO `goods` (`id`,`shop_id`,`category_id`,`name`,`subtitle`,`main_image`,`images`,`detail`,`price`,`sales`,`status`) VALUES
(1,1,1,'无线蓝牙耳机 Pro','主动降噪 / 30小时续航','https://loremflickr.com/600/600/headphones?lock=1','https://loremflickr.com/600/600/headphones?lock=1','D音质升级，支持SBC/AAC解码',199.00,320,1),
(2,1,1,'智能手环 6代','血氧监测 / 14天续航','https://loremflickr.com/600/600/smartwatch?lock=2','https://loremflickr.com/600/600/smartwatch?lock=2','50米防水，超长续航',149.00,560,1),
(3,1,1,'便携充电宝 20000mAh','22.5W快充 / 大容量','https://loremflickr.com/600/600/charger?lock=3','https://loremflickr.com/600/600/charger?lock=3','双向快充，可上飞机',99.00,890,1),
(4,1,6,'机械键盘 87键','热插拔轴体 / RGB背光','https://loremflickr.com/600/600/keyboard?lock=4','https://loremflickr.com/600/600/keyboard?lock=4','三模连接，电竞手感',259.00,210,1);

INSERT INTO `sku` (`goods_id`,`spec`,`price`,`stock`,`image`) VALUES
(1,'颜色:白色',199.00,100,'https://loremflickr.com/200/200/headphones?lock=101'),
(1,'颜色:黑色',199.00,120,'https://loremflickr.com/200/200/headphones?lock=102'),
(2,'颜色:曜石黑',149.00,150,'https://loremflickr.com/200/200/smartwatch?lock=103'),
(2,'颜色:星空蓝',149.00,80,'https://loremflickr.com/200/200/smartwatch?lock=104'),
(3,'颜色:白色',99.00,200,'https://loremflickr.com/200/200/charger?lock=105'),
(4,'轴体:红轴',259.00,60,'https://loremflickr.com/200/200/keyboard?lock=106'),
(4,'轴体:茶轴',259.00,60,'https://loremflickr.com/200/200/keyboard?lock=107');

-- 轮播图
INSERT INTO `banner` (`image`,`link`,`sort`,`status`) VALUES
('https://loremflickr.com/1200/400/digital,tech?lock=10',NULL,1,1),
('https://loremflickr.com/1200/400/gadget?lock=11','1',2,1),
('https://loremflickr.com/1200/400/product?lock=12',NULL,3,1);

-- 公告
INSERT INTO `notice` (`title`,`content`,`status`,`sort`) VALUES
('平台新用户注册立减', '新用户注册即送 20 元优惠券，快去领取吧！',1,1),
('模拟项目公告', '本系统为课程大作业演示项目，支付与物流均为模拟数据。',1,2);

-- 优惠券
INSERT INTO `coupon` (`name`,`threshold`,`amount`,`total`,`start_time`,`end_time`,`status`) VALUES
('新人专享券', 50.00, 20.00, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('满100减10', 100.00, 10.00, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1);

-- -------------------------------------------------------------
-- 演示业务数据（用户/订单/物流/评价/售后，便于商家端联调）
-- -------------------------------------------------------------

-- 演示用户（手机号登录，密码 123456）
INSERT INTO `user` (`id`,`phone`,`password`,`nickname`,`gender`) VALUES
(1,'13800001111','$2a$10$M9hRBeloKw73XZYun9s7eOuMXo6/AgCLxDgnQ3Q8tNqvl3ysdEv2u','演示买家',0);

-- 演示订单（店铺1）：1今天待发货 2待收货 3售后中 4/5/6已完成
INSERT INTO `orders` (`order_no`,`user_id`,`shop_id`,`total_amount`,`freight`,`discount`,`pay_amount`,`addr_snapshot`,`remark`,`status`,`pay_time`,`ship_time`,`finish_time`,`create_time`) VALUES
('M2026080001',1,1,457.00,0,20.00,437.00,'张同学 13800001111 广东省 深圳市 南山区 粤海街道科技园路1号','请尽快发货',1,NOW(),NULL,NULL,NOW() - INTERVAL 2 HOUR),
('M2026080002',1,1,199.00,0,0,199.00,'张同学 13800001111 广东省 深圳市 南山区 粤海街道科技园路1号',NULL,2,NOW() - INTERVAL 2 DAY,NOW() - INTERVAL 1 DAY,NULL,NOW() - INTERVAL 2 DAY - INTERVAL 3 HOUR),
('M2026080003',1,1,298.00,0,0,298.00,'张同学 13800001111 广东省 深圳市 南山区 粤海街道科技园路1号','手环送去退换',5,NOW() - INTERVAL 1 DAY,NOW(),NULL,NOW() - INTERVAL 1 DAY - INTERVAL 2 HOUR),
('M2026080004',1,1,99.00,0,0,99.00,'张同学 13800001111 广东省 深圳市 南山区 粤海街道科技园路1号',NULL,3,NOW() - INTERVAL 10 DAY,NOW() - INTERVAL 9 DAY,NOW() - INTERVAL 8 DAY,NOW() - INTERVAL 10 DAY),
('M2026080005',1,1,149.00,0,0,149.00,'张同学 13800001111 广东省 深圳市 南山区 粤海街道科技园路1号',NULL,3,NOW() - INTERVAL 20 DAY,NOW() - INTERVAL 19 DAY,NOW() - INTERVAL 18 DAY,NOW() - INTERVAL 20 DAY),
('M2026080006',1,1,199.00,0,0,199.00,'张同学 13800001111 广东省 深圳市 南山区 粤海街道科技园路1号',NULL,3,NOW() - INTERVAL 30 DAY,NOW() - INTERVAL 29 DAY,NOW() - INTERVAL 28 DAY,NOW() - INTERVAL 30 DAY);

INSERT INTO `order_item` (`order_id`,`goods_id`,`goods_name`,`sku_id`,`sku_spec`,`cover_image`,`price`,`quantity`) VALUES
(1,4,'机械键盘 87键',6,'轴体:红轴','https://loremflickr.com/200/200/keyboard?lock=501',259.00,1),
(1,3,'便携充电宝 20000mAh',5,'颜色:白色','https://loremflickr.com/200/200/charger?lock=502',99.00,2),
(2,1,'无线蓝牙耳机 Pro',1,'颜色:白色','https://loremflickr.com/200/200/headphones?lock=503',199.00,1),
(3,2,'智能手环 6代',3,'颜色:曜石黑','https://loremflickr.com/200/200/smartwatch?lock=504',149.00,2),
(4,3,'便携充电宝 20000mAh',5,'颜色:白色','https://loremflickr.com/200/200/charger?lock=505',99.00,1),
(5,2,'智能手环 6代',4,'颜色:星空蓝','https://loremflickr.com/200/200/smartwatch?lock=506',149.00,1),
(6,1,'无线蓝牙耳机 Pro',2,'颜色:黑色','https://loremflickr.com/200/200/headphones?lock=507',199.00,1);

INSERT INTO `payment` (`trade_no`,`order_id`,`user_id`,`amount`,`method`,`status`,`pay_time`) VALUES
('T2026080001',1,1,437.00,'BALANCE',1,NOW() - INTERVAL 2 HOUR),
('T2026080002',2,1,199.00,'CARD',1,NOW() - INTERVAL 2 DAY),
('T2026080003',3,1,298.00,'BALANCE',1,NOW() - INTERVAL 1 DAY),
('T2026080004',4,1,99.00,'BALANCE',1,NOW() - INTERVAL 10 DAY),
('T2026080005',5,1,149.00,'BALANCE',1,NOW() - INTERVAL 20 DAY),
('T2026080006',6,1,199.00,'CARD',1,NOW() - INTERVAL 30 DAY);

INSERT INTO `logistics` (`order_id`,`logistics_no`,`company`,`status`,`trace`) VALUES
(2,'SF1000000002','顺丰速运',1,'已揽收;运输中'),
(3,'SF1000000003','顺丰速运',1,'已揽收;运输中'),
(4,'SF1000000004','模拟快递',2,'已揽收;运输中;已签收'),
(5,'SF1000000005','模拟快递',2,'已揽收;运输中;已签收'),
(6,'SF1000000006','模拟快递',2,'已揽收;运输中;已签收');

-- 演示评价（订单4对应商品）
INSERT INTO `review` (`user_id`,`order_item_id`,`goods_id`,`shop_id`,`rating`,`content`,`images`,`reply`) VALUES
(1,5,3,1,5,'充电宝容量实在，发货也快，非常满意！','https://loremflickr.com/400/400/charger?lock=601','感谢亲的认可，欢迎再次光临！');

-- 演示售后（订单3，待处理）
INSERT INTO `aftersale` (`order_id`,`order_no`,`user_id`,`shop_id`,`type`,`reason`,`refund_amount`,`status`) VALUES
(3,'M2026080003',1,1,2,'手环心率测量不准，申请退货退款',298.00,0);

-- -------------------------------------------------------------
-- 管理者后台演示数据
-- -------------------------------------------------------------

-- 入驻申请（1 条待审核，1 条已通过，用于后台审核演示）
INSERT INTO `merchant_apply` (`merchant_id`,`shop_name`,`category_ids`,`contact_name`,`contact_phone`,`qualification`,`status`,`reason`,`audit_time`) VALUES
(1,'优选数码二店','1,4','王掌柜','13800000001','营业执照扫描件、法人身份证',0,NULL,NULL),
(1,'优选数码旗舰店','1,2,5','王掌柜','13800000001','营业执照扫描件、法人身份证',1,NULL,NOW() - INTERVAL 5 DAY);

-- 待审核商品（status=3），用于后台商品审核演示
INSERT INTO `goods` (`id`,`shop_id`,`category_id`,`name`,`subtitle`,`main_image`,`images`,`detail`,`price`,`sales`,`status`) VALUES
(5,1,1,'新品智能音箱 Mini','小身材大音量，语音控制','https://loremflickr.com/600/600/speaker?lock=5','https://loremflickr.com/600/600/speaker?lock=5','支持语音助手，双麦克风降噪',129.00,0,3);
INSERT INTO `sku` (`goods_id`,`spec`,`price`,`stock`,`image`) VALUES
(5,'颜色:深空灰',129.00,80,'https://loremflickr.com/200/200/speaker?lock=701'),
(5,'颜色:月光白',129.00,80,'https://loremflickr.com/200/200/speaker?lock=702');