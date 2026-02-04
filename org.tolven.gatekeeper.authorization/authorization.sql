insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/rs',	'/**',									'tssl,rsauthz');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/gatekeeper/rs',	'/**',									'tssl,rsauthz');

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/rs',	'/application.wadl',					NULL);

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/html', '/index.jsp',							NULL);
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/html', '/styles/**',							NULL);
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/html', '/images/**',							NULL);
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/html', '/public/**',							NULL);
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/html', '/recoverloginpassword/**',				NULL);
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/gatekeeper/html', '/recoverloginpassword/**',				NULL);

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/html', '/**',									'tssl,tauthc');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/gatekeeper/html', '/**',									'tssl,tauthc');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/html', '/loginsecurityquestions/**',			'tssl,tauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/gatekeeper/html', '/loginsecurityquestions/**',			'tssl,tauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/html', '/register/**',							'tssl,tauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/gatekeeper/html', '/register/**',							'tssl,tauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/gatekeeper/html', '/resetloginpassword/**',				'tssl,tauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/gatekeeper/html', '/resetloginpassword/**',				'tssl,tauthc,troles[tolvenAdmin]');

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/**',									'tssl,rspreauthc,apiaf');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/**',									'tssl,rspreauthc,apiaf');

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/application.wadl',					NULL);

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/guest/headers',						'tssl,rspreauthc,apientervf');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/guest/milliseconds',					'tssl,rspreauthc,apientervf');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/guest/time',							'tssl,rspreauthc,apientervf');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/user/info',							'tssl,rspreauthc,apientervf');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/user/details',						'tssl,rspreauthc,apientervf');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/vestibule/accountList',				'tssl,rspreauthc,apientervf');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/vestibule/createAccount',				'tssl,rspreauthc,apientervf');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/vestibule/selectAccount',				'tssl,rspreauthc,apientervf,apiselectvf,apiexitvf,apiaf');

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/growthChartLoader/createGrowthChart',	'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/clearRuleBases',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/loader/createTrimHeader',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/createTrimHeader',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/loader/loadApplications',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/loadApplications',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/packageBody',					'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/loader/placeholders',					'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/placeholders',					'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/queueActivateNewTrimHeaders',	'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/storeReport',					'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/tolvenproperties/reset',		'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/loader/user/accountUsers',			'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/user/accountUsers',			'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/user/activate',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/loader/user/details',					'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loader/user/details',					'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/loaderustates/loadStateNames',		'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/loaderustates/loadStateNames',		'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/scheduler/interval',					'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/scheduler/stop',						'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/scheduler/timeout',					'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/tolvenproperties/find',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/tolvenproperties/find',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/api',				'/tolvenproperties/set',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/tolvenproperties/set',				'tssl,rspreauthc,troles[tolvenAdmin]');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/api',				'/tolvenproperties/remove',				'tssl,rspreauthc,troles[tolvenAdmin]');

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/**',									'tssl,preauthc');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/**',									'tssl,preauthc');

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/images/**',							NULL);
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/public/**',							NULL);
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/scripts/**',							NULL);
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/styles/**',							NULL);

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/vestibule/**',						'tssl,preauthc,entervf,selectvf,exitvf');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/vestibule/**',						'tssl,preauthc,entervf,selectvf,exitvf');

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/ajax/**',								'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/ajax/**',								'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/drilldown/**',						'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/drilldown/**',						'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/document',							'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/five/**',								'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/five/**',								'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/invitation/**',						'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/invitation/**',						'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/manage/**',							'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/manage/**',							'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/private/**',							'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/private/**',							'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/report/**',							'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/report/**',							'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/templates/**',						'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/templates/**',						'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/Tolven',			'/wizard/**',							'tssl,preauthc,af');
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/Tolven',			'/wizard/**',							'tssl,preauthc,af');

insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'GET',	'/rs',				'/**',									NULL);
insert into @tablePrefix@tolvenauth (policy, urlMethod, contextPath, url, filters) values ('default', 'POST',	'/rs',				'/**',									NULL);