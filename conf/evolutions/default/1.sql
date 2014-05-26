# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table users (
  id                        varchar(255) not null,
  uid                       varchar(255),
  pwd                       varchar(255),
  token                     varchar(255),
  constraint pk_users primary key (id))
;

create sequence users_seq;




# --- !Downs

drop table if exists users cascade;

drop sequence if exists users_seq;

