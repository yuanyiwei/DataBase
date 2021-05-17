drop table if exists BANKS;

drop table if exists CUSTOMERS;

drop table if exists DEBITACCOUNT;

drop table if exists DEBITCREATE;

drop table if exists DEPARTS;

drop table if exists LOANS;

drop table if exists OWNS;

drop table if exists PAYS;

drop table if exists STAFFS;

drop table if exists VISAACCOUNT;

drop table if exists VISACREATE;

/*==============================================================*/
/* Table: BANKS                                                 */
/*==============================================================*/
create table BANKS
(
   NAME                 varchar(30) not null,
   CARDID2              bigint,
   CARDID               bigint,
   TOTAL_MONEY          varchar(30),
   CITY                 varchar(30),
   primary key (NAME)
);

/*==============================================================*/
/* Table: CUSTOMERS                                             */
/*==============================================================*/
create table CUSTOMERS
(
   ID                   varchar(15) not null,
   ID2                  varchar(15),
   DEPART_NAME          varchar(10),
   DEPART_TYPE          varchar(10),
   STAFF_TELE           varchar(15),
   primary key (ID)
);

/*==============================================================*/
/* Table: DEBITACCOUNT                                          */
/*==============================================================*/
create table DEBITACCOUNT
(
   CARDID               bigint not null,
   NAME                 varchar(30),
   MONEY                varchar(30),
   ACTIVATIONDATE       date,
   ACTIVATIONBANK       varchar(30),
   LATESTUSED           date,
   INTEREST             varchar(30),
   primary key (CARDID)
);

/*==============================================================*/
/* Table: DEBITCREATE                                           */
/*==============================================================*/
create table DEBITCREATE
(
   ID                   varchar(15) not null,
   CARDID               bigint not null,
   primary key (ID, CARDID)
);

/*==============================================================*/
/* Table: DEPARTS                                               */
/*==============================================================*/
create table DEPARTS
(
   ID3                  varchar(15) not null,
   DEPART_NAME          varchar(10),
   DEPART_TYPE          varchar(10),
   primary key (ID3)
);

/*==============================================================*/
/* Table: LOANS                                                 */
/*==============================================================*/
create table LOANS
(
   LOANID               varchar(15) not null,
   TOTAL_MONEY          varchar(30),
   primary key (LOANID)
);

/*==============================================================*/
/* Table: OWNS                                                  */
/*==============================================================*/
create table OWNS
(
   ID                   varchar(15) not null,
   LOANID               varchar(15) not null,
   primary key (ID, LOANID)
);

/*==============================================================*/
/* Table: PAYS                                                  */
/*==============================================================*/
create table PAYS
(
   PAY_DATE             datetime not null,
   LOANID               varchar(15),
   MONEY                varchar(30),
   primary key (PAY_DATE)
);

/*==============================================================*/
/* Table: STAFFS                                                */
/*==============================================================*/
create table STAFFS
(
   ID2                  varchar(15) not null,
   NAME                 varchar(30),
   ID3                  varchar(15),
   DEPART_NAME          varchar(10),
   DEPART_TYPE          varchar(10),
   STAFF_TELE           varchar(15),
   ISMANAGER            bool,
   HIREDATE             date,
   primary key (ID2)
);

/*==============================================================*/
/* Table: VISAACCOUNT                                           */
/*==============================================================*/
create table VISAACCOUNT
(
   CARDID2              bigint not null,
   NAME                 varchar(30),
   MONEY                varchar(30),
   ACTIVATIONDATE       date,
   ACTIVATIONBANK       varchar(30),
   LATESTUSED           date,
   QUOTA                varchar(30),
   primary key (CARDID2)
);

/*==============================================================*/
/* Table: VISACREATE                                            */
/*==============================================================*/
create table VISACREATE
(
   ID                   varchar(15) not null,
   CARDID2              bigint not null,
   primary key (ID, CARDID2)
);

alter table BANKS add constraint FK_DEBITBELONG2 foreign key (CARDID)
      references DEBITACCOUNT (CARDID) on delete restrict on update restrict;

alter table BANKS add constraint FK_VISABELONG2 foreign key (CARDID2)
      references VISAACCOUNT (CARDID2) on delete restrict on update restrict;

alter table CUSTOMERS add constraint FK_RESPONSIBLE foreign key (ID2)
      references STAFFS (ID2) on delete restrict on update restrict;

alter table DEBITACCOUNT add constraint FK_DEBITBELONG foreign key (NAME)
      references BANKS (NAME) on delete restrict on update restrict;

alter table DEBITCREATE add constraint FK_DEBITCREATE foreign key (ID)
      references CUSTOMERS (ID) on delete restrict on update restrict;

alter table DEBITCREATE add constraint FK_DEBITCREATE2 foreign key (CARDID)
      references DEBITACCOUNT (CARDID) on delete restrict on update restrict;

alter table OWNS add constraint FK_OWNS foreign key (ID)
      references CUSTOMERS (ID) on delete restrict on update restrict;

alter table OWNS add constraint FK_OWNS2 foreign key (LOANID)
      references LOANS (LOANID) on delete restrict on update restrict;

alter table PAYS add constraint FK_PAYMENTS foreign key (LOANID)
      references LOANS (LOANID) on delete restrict on update restrict;

alter table STAFFS add constraint FK_WORKSHIP foreign key (ID3)
      references DEPARTS (ID3) on delete restrict on update restrict;

alter table STAFFS add constraint FK_WORK_PLACE foreign key (NAME)
      references BANKS (NAME) on delete restrict on update restrict;

alter table VISAACCOUNT add constraint FK_VISABELONG foreign key (NAME)
      references BANKS (NAME) on delete restrict on update restrict;

alter table VISACREATE add constraint FK_VISACREATE foreign key (ID)
      references CUSTOMERS (ID) on delete restrict on update restrict;

alter table VISACREATE add constraint FK_VISACREATE2 foreign key (CARDID2)
      references VISAACCOUNT (CARDID2) on delete restrict on update restrict;
