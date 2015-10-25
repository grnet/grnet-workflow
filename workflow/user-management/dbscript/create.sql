create table User (
	id int auto_increment,
	email varchar(200) not null,
	password varchar(100) not null,
	firstname varchar(200) not null,
	lastname varchar(200) not null,
	SECTION varchar(100) not null,
	status varchar(20) not null,
	accountType varchar(20) not null,
	activationKey varchar(200) null,
	refreshToken varchar(200) null,
	creationDate timestamp not null,
	expirationDate timestamp null,
	primary key (id)
);

create table Role (
	id int auto_increment,
	name varchar(100) not null,
	description varchar(250) null,
	primary key (id)
);

create table User_Role (
	user_id int not null,
	role_id int not null,
	primary key (id)
);