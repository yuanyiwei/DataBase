/*==============================================================*/
/* DBMS name:      ORACLE Version 11g                           */
/* Created on:     2018/6/12 23:25:30                           */
/*==============================================================*/


alter table �����˻�
   drop constraint FK_�����˻�_��Ϊ_�˻�;

alter table Ա��
   drop constraint FK_Ա��_����_֧��;

alter table �ͻ�
   drop constraint FK_�ͻ�_����_Ա��;

alter table ӵ���˻�
   drop constraint FK_ӵ���˻�_ӵ���˻�_֧��;

alter table ӵ���˻�
   drop constraint FK_ӵ���˻�_ӵ���˻�2_�ͻ�;

alter table ӵ���˻�
   drop constraint FK_ӵ���˻�_ӵ���˻�3_�˻�;

alter table ӵ�д���
   drop constraint FK_ӵ�д���_ӵ�д���_����;

alter table ӵ�д���
   drop constraint FK_ӵ�д���_ӵ�д���2_�ͻ�;

alter table ֧�����
   drop constraint FK_֧�����_���֧��_����;

alter table ֧Ʊ�˻�
   drop constraint FK_֧Ʊ�˻�_��Ϊ2_�˻�;

alter table ����
   drop constraint FK_����_����_֧��;

drop table �����˻� cascade constraints;

drop index ����_FK;

drop table Ա�� cascade constraints;

drop index ����_FK;

drop table �ͻ� cascade constraints;

drop index ӵ���˻�3_FK2;

drop index ӵ���˻�3_FK;

drop index ӵ���˻�_FK;

drop table ӵ���˻� cascade constraints;

drop index ӵ�д���2_FK;

drop index ӵ�д���_FK;

drop table ӵ�д��� cascade constraints;

drop index ���֧��_FK;

drop table ֧����� cascade constraints;

drop table ֧Ʊ�˻� cascade constraints;

drop table ֧�� cascade constraints;

drop table �˻� cascade constraints;

drop index ����_FK;

drop table ���� cascade constraints;

/*==============================================================*/
/* Table: �����˻�                                                  */
/*==============================================================*/
create table �����˻� 
(
   �˻���                  VARCHAR2(100)        not null,
   �˻�����                 VARCHAR2(20)         not null,
   ���                   FLOAT,
   ��������                 DATE,
   �����������               DATE,
   ����                   FLOAT,
   ��������                 VARCHAR2(10),
   constraint PK_�����˻� primary key (�˻���, �˻�����)
);

/*==============================================================*/
/* Table: Ա��                                                    */
/*==============================================================*/
create table Ա�� 
(
   ���֤��                 CHAR(18)             not null,
   ֧����                  VARCHAR2(100),
   ����                   VARCHAR2(20),
   �绰����                 VARCHAR2(20),
   ��ͥסַ                 VARCHAR2(1000),
   ��ʼ��������               DATE,
   constraint PK_Ա�� primary key (���֤��)
);

/*==============================================================*/
/* Index: ����_FK                                                 */
/*==============================================================*/
create index ����_FK on Ա�� (
   ֧���� ASC
);

/*==============================================================*/
/* Table: �ͻ�                                                    */
/*==============================================================*/
create table �ͻ� 
(
   ���֤��                 CHAR(18)             not null,
   Ա��_���֤��              CHAR(18),
   ����                   VARCHAR2(20),
   ��ϵ�绰                 VARCHAR2(20),
   ��ͥסַ                 VARCHAR2(1000),
   ��ϵ������                VARCHAR2(20),
   ��ϵ���ֻ���               VARCHAR2(20),
   "��ϵ��E-mail"          VARCHAR2(100),
   ��ͻ���ϵ                VARCHAR2(1000),
   constraint PK_�ͻ� primary key (���֤��)
);

/*==============================================================*/
/* Index: ����_FK                                                 */
/*==============================================================*/
create index ����_FK on �ͻ� (
   Ա��_���֤�� ASC
);

/*==============================================================*/
/* Table: ӵ���˻�                                                  */
/*==============================================================*/
create table ӵ���˻� 
(
   ֧����                  VARCHAR2(100)        not null,
   ���֤��                 CHAR(18)             not null,
   �˻���                  VARCHAR2(100)        not null,
   �˻�����                 VARCHAR2(20)         not null,
   constraint PK_ӵ���˻� primary key (֧����, ���֤��, �˻���, �˻�����),
   constraint AK_UQKEY1_ӵ���˻� unique (֧����, ���֤��, �˻�����)
);

/*==============================================================*/
/* Index: ӵ���˻�_FK                                               */
/*==============================================================*/
create index ӵ���˻�_FK on ӵ���˻� (
   ֧���� ASC
);

/*==============================================================*/
/* Index: ӵ���˻�3_FK                                              */
/*==============================================================*/
create index ӵ���˻�3_FK on ӵ���˻� (
   ���֤�� ASC
);

/*==============================================================*/
/* Index: ӵ���˻�3_FK2                                             */
/*==============================================================*/
create index ӵ���˻�3_FK2 on ӵ���˻� (
   �˻��� ASC,
   �˻����� ASC
);

/*==============================================================*/
/* Table: ӵ�д���                                                  */
/*==============================================================*/
create table ӵ�д��� 
(
   �����                  CHAR(100)            not null,
   ���֤��                 CHAR(18)             not null,
   constraint PK_ӵ�д��� primary key (�����, ���֤��)
);

/*==============================================================*/
/* Index: ӵ�д���_FK                                               */
/*==============================================================*/
create index ӵ�д���_FK on ӵ�д��� (
   ����� ASC
);

/*==============================================================*/
/* Index: ӵ�д���2_FK                                              */
/*==============================================================*/
create index ӵ�д���2_FK on ӵ�д��� (
   ���֤�� ASC
);

/*==============================================================*/
/* Table: ֧�����                                                  */
/*==============================================================*/
create table ֧����� 
(
   �����                  CHAR(100),
   ����                   DATE,
   ���                   FLOAT
);

/*==============================================================*/
/* Index: ���֧��_FK                                               */
/*==============================================================*/
create index ���֧��_FK on ֧����� (
   ����� ASC
);

/*==============================================================*/
/* Table: ֧Ʊ�˻�                                                  */
/*==============================================================*/
create table ֧Ʊ�˻� 
(
   �˻���                  VARCHAR2(100)        not null,
   �˻�����                 VARCHAR2(20)         not null,
   ���                   FLOAT,
   ��������                 DATE,
   �����������               DATE,
   ͸֧��                  FLOAT,
   constraint PK_֧Ʊ�˻� primary key (�˻���, �˻�����)
);

/*==============================================================*/
/* Table: ֧��                                                    */
/*==============================================================*/
create table ֧�� 
(
   ֧����                  VARCHAR2(100)        not null,
   �ʲ�                   FLOAT,
   constraint PK_֧�� primary key (֧����)
);

/*==============================================================*/
/* Table: �˻�                                                    */
/*==============================================================*/
create table �˻� 
(
   �˻���                  VARCHAR2(100)        not null,
   �˻�����                 VARCHAR2(20)         not null,
   ���                   FLOAT,
   ��������                 DATE,
   �����������               DATE,
   constraint PK_�˻� primary key (�˻���, �˻�����)
);

/*==============================================================*/
/* Table: ����                                                    */
/*==============================================================*/
create table ���� 
(
   �����                  CHAR(100)            not null,
   ֧����                  VARCHAR2(100),
   ���                   FLOAT,
   ����                   DATE,
   constraint PK_���� primary key (�����)
);

/*==============================================================*/
/* Index: ����_FK                                                 */
/*==============================================================*/
create index ����_FK on ���� (
   ֧���� ASC
);

alter table �����˻�
   add constraint FK_�����˻�_��Ϊ_�˻� foreign key (�˻���, �˻�����)
      references �˻� (�˻���, �˻�����);

alter table Ա��
   add constraint FK_Ա��_����_֧�� foreign key (֧����)
      references ֧�� (֧����);

alter table �ͻ�
   add constraint FK_�ͻ�_����_Ա�� foreign key (Ա��_���֤��)
      references Ա�� (���֤��);

alter table ӵ���˻�
   add constraint FK_ӵ���˻�_ӵ���˻�_֧�� foreign key (֧����)
      references ֧�� (֧����);

alter table ӵ���˻�
   add constraint FK_ӵ���˻�_ӵ���˻�2_�ͻ� foreign key (���֤��)
      references �ͻ� (���֤��);

alter table ӵ���˻�
   add constraint FK_ӵ���˻�_ӵ���˻�3_�˻� foreign key (�˻���, �˻�����)
      references �˻� (�˻���, �˻�����);

alter table ӵ�д���
   add constraint FK_ӵ�д���_ӵ�д���_���� foreign key (�����)
      references ���� (�����);

alter table ӵ�д���
   add constraint FK_ӵ�д���_ӵ�д���2_�ͻ� foreign key (���֤��)
      references �ͻ� (���֤��);

alter table ֧�����
   add constraint FK_֧�����_���֧��_���� foreign key (�����)
      references ���� (�����);

alter table ֧Ʊ�˻�
   add constraint FK_֧Ʊ�˻�_��Ϊ2_�˻� foreign key (�˻���, �˻�����)
      references �˻� (�˻���, �˻�����);

alter table ����
   add constraint FK_����_����_֧�� foreign key (֧����)
      references ֧�� (֧����);

