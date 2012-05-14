insert into Salutation values (1, 'Mister');
insert into Salutation values (2, 'Mrs');

insert into Healthinsurance values (1, 'Vienna', 'Health And Care', 'Mainstreet', 1010);
insert into Healthinsurance values (2, 'Lower Austria', 'National Insurance', 'Downstreet', 3030);
insert into Healthinsurance values (3, 'Upper Austria', 'Social Insurance', 'Greenstreet', 2020);

insert into Education values (1, 'Primary School');
insert into Education values (2, 'College');
insert into Education values (3, 'University');
insert into Education values (4, 'Technical University');

insert into customer values (1, null, null, 'John Q', null, 'Public', true, 'john.public@public.at', '02747473637', '06640509404', 1, 1);
insert into customer values (2, null, null, 'Max', null, 'Power', false, 'max.power@max.at', '02746573738', '06802000040', 1, 2);

insert into Address values (1, 'Vienna', 'Schönbrunnerstraße 12', 1120, 1);
insert into Address values (2, 'Sankt Pölten', 'Mainstreet 13', 3100, 1);
insert into Address values (3, 'Vienna', 'Myplace 24', 1010, 2);

insert into customer_education values (1, 1);
insert into customer_education values (1, 4);
insert into customer_education values (2, 2);  