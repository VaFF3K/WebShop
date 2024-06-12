
CREATE DATABASE webapp;

CREATE TABLE webapp.users (id int AUTO_INCREMENT,
                           email VARCHAR(40) NOT NULL,
                           password VARCHAR(120) NOT NULL,
                           displayName varchar(20) NULL,
                           OPTIONS JSON NULL COMMENT 'full user profile',
                           created_at date NULL,
                           deleted_at date NULL,
                           CONSTRAINT users_pk PRIMARY KEY (id), CONSTRAINT users_pk_2 UNIQUE (email));

CREATE TABLE webapp.goods (id int AUTO_INCREMENT PRIMARY KEY,
                           name varchar(60) NOT NULL,
                           description text NULL,
                           brand varchar(20) NULL,
                           photo JSON NULL,
                           likes int NULL);

CREATE TABLE webapp.types (id int AUTO_INCREMENT PRIMARY KEY,
                           name varchar(15) NOT NULL,
                           deleted_at date NULL);

CREATE TABLE webapp.goods_types
(id int AUTO_INCREMENT PRIMARY KEY,
 good_id int NULL,
 type_id int NULL,
 CONSTRAINT goods_types_goods_id_fk
     FOREIGN KEY (good_id) REFERENCES webapp.goods (id) ON UPDATE
         SET NULL ON DELETE
         SET NULL,
 CONSTRAINT goods_types_types_id_fk
     FOREIGN KEY (type_id) REFERENCES webapp.types (id) ON UPDATE
         SET NULL ON DELETE
         SET NULL);

CREATE TABLE webapp.prices
(id int AUTO_INCREMENT PRIMARY KEY,
 from_supplier DOUBLE NOT NULL,
 for_client DOUBLE NOT NULL,
 created_at date NOT NULL,
 deleted_at date NULL,
 good_id int NULL,
 income int NULL,
 outcome int NULL,
 CONSTRAINT prices_goods_id_fk
     FOREIGN KEY (good_id) REFERENCES webapp.goods (id) ON UPDATE
         SET NULL ON DELETE
         SET NULL);

CREATE TABLE webapp.orders (id int AUTO_INCREMENT PRIMARY KEY,
                            user_id int NOT NULL,
                            price_id int NOT NULL,
                            is_paid bit DEFAULT b'0' NOT NULL,
                            created_at datetime NOT NULL,
                            deleted_at datetime NULL);
