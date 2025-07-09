-- UPDATED BY SYED IFTIKHAR: 2025-06-27 at 09:23:16 EDT
DROP SCHEMA IF EXISTS biosteel CASCADE;

CREATE SCHEMA biosteel;

ALTER SCHEMA biosteel OWNER TO biosteel;

CREATE TABLE
	biosteel.user_account (
		user_id UUID NOT NULL,
		email VARCHAR(255) NOT NULL UNIQUE,
		password_hash TEXT,
		enabled bool NULL,
		first_name VARCHAR(100),
		last_name VARCHAR(100),
		phone VARCHAR(20),
		date_of_birth DATE,
		gender VARCHAR(50),
		profile_photo_media_id UUID,
		registration_date timestamp(6) NULL,
		access_token VARCHAR(1024),
		refresh_token VARCHAR(1024),
		token_expiry_date timestamp(6),
		failed_login_attempts int4 NULL,
		"locked" bool NULL,
		last_activity_date timestamp(6) NULL,
		locked_date timestamp(6) NULL,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT user_account_pk PRIMARY KEY (user_id)
	);

ALTER TABLE biosteel.user_account OWNER TO biosteel;

CREATE TABLE
	biosteel.role (
		role_id UUID NOT NULL,
		name VARCHAR(255) NOT NULL,
		description TEXT,
		CONSTRAINT role_pk PRIMARY KEY (role_id)
	);

ALTER TABLE biosteel.role OWNER TO biosteel;

CREATE TABLE
	biosteel.user_role (user_id UUID NOT NULL, role_id UUID NOT NULL);

ALTER TABLE biosteel.user_role OWNER TO biosteel;

CREATE TABLE
	biosteel.user_verification_token (
		user_verification_token_id UUID NOT NULL,
		user_id UUID NOT NULL,
		expiry_date timestamp(6) NULL,
		"token" varchar(255) NULL,
		CONSTRAINT user_verification_token_pk PRIMARY KEY (user_verification_token_id)
	);

ALTER TABLE biosteel.user_verification_token OWNER TO biosteel;

CREATE TABLE
	biosteel.user_password_reset_token (
		user_password_reset_token_id UUID NOT NULL,
		user_id UUID NOT NULL,
		expiry_date timestamp(6) NULL,
		"token" varchar(255) NULL,
		CONSTRAINT user_password_reset_token_pk PRIMARY KEY (user_password_reset_token_id)
	);

ALTER TABLE biosteel.user_password_reset_token OWNER TO biosteel;

CREATE TABLE
	biosteel.user_preference (
		user_preference_id UUID NOT NULL,
		user_id UUID NOT NULL,
		notification_preferences TEXT,
		timezone VARCHAR(50),
		language VARCHAR(10),
		push_notification_enabled BOOLEAN DEFAULT TRUE,
		team_notifications BOOLEAN DEFAULT TRUE,
		game_notifications BOOLEAN DEFAULT TRUE,
		practice_notifications BOOLEAN DEFAULT TRUE,
		message_notifications BOOLEAN DEFAULT TRUE,
		announcement_notifications BOOLEAN DEFAULT TRUE,
		created_at TIMESTAMP
		WITH
			TIME ZONE DEFAULT NOW (),
			updated_at TIMESTAMP
		WITH
			TIME ZONE,
			CONSTRAINT user_preference_pk PRIMARY KEY (user_preference_id)
	);

ALTER TABLE biosteel.user_preference OWNER TO biosteel;

CREATE TABLE
	biosteel.user_notification (
		user_notification_id UUID NOT NULL,
		user_id UUID NOT NULL,
		notification_type character varying(255),
		title VARCHAR(255),
		content TEXT,
		related_id UUID,
		is_read BOOLEAN DEFAULT FALSE,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT user_notification_pk PRIMARY KEY (user_notification_id)
	);

ALTER TABLE biosteel.user_notification OWNER TO biosteel;

CREATE TABLE
	biosteel.player (
		player_id UUID NOT NULL,
		parent_user_id UUID NULL,
		first_name VARCHAR(100),
		last_name VARCHAR(100),
		email VARCHAR(255),
		phone VARCHAR(20),
		date_of_birth DATE,
		gender VARCHAR(50),
		jersey_number VARCHAR(10),
		height_cm INTEGER,
		weight_kg DECIMAL(5, 2),
		primary_position VARCHAR(50),
		secondary_position VARCHAR(50),
		medical_notes TEXT,
		logo_media_id UUID,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT player_pk PRIMARY KEY (player_id)
	);

ALTER TABLE biosteel.player OWNER TO biosteel;

CREATE TABLE
	biosteel.contact (
		contact_id UUID NOT NULL,
		first_name VARCHAR(100),
		last_name VARCHAR(100),
		email VARCHAR(255),
		phone VARCHAR(20),
		date_of_birth DATE,
		gender VARCHAR(50),
		logo_media_id UUID,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT contact_pk PRIMARY KEY (contact_id)
	);

ALTER TABLE biosteel.contact OWNER TO biosteel;

CREATE TABLE
	biosteel.sport_type (
		code VARCHAR(50) NOT NULL,
		name VARCHAR(100) NOT NULL,
		description TEXT,
		created_at TIMESTAMP
		WITH
			TIME ZONE DEFAULT NOW (),
			updated_at TIMESTAMP
		WITH
			TIME ZONE,
			deleted_at TIMESTAMP
		WITH
			TIME ZONE,
			CONSTRAINT sport_type_pk PRIMARY KEY (code)
	);

ALTER TABLE biosteel.sport_type OWNER TO biosteel;

CREATE TABLE
	biosteel.sport_attribute_definition (
		code VARCHAR(50) NOT NULL,
		name VARCHAR(100) NOT NULL,
		description TEXT,
		data_type VARCHAR(20) NOT NULL CHECK (
			data_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'ENUM')
		),
		is_required BOOLEAN DEFAULT false,
		validation_rules text,
		entity_type VARCHAR(20) NOT NULL CHECK (entity_type IN ('TEAM', 'PLAYER')),
		created_at TIMESTAMP
		WITH
			TIME ZONE DEFAULT NOW (),
			updated_at TIMESTAMP
		WITH
			TIME ZONE,
			deleted_at TIMESTAMP
		WITH
			TIME ZONE,
			CONSTRAINT sport_attribute_definition_pk PRIMARY KEY (code)
	);

ALTER TABLE biosteel.sport_attribute_definition OWNER TO biosteel;

CREATE TABLE
	biosteel.sport_attribute_mapping (
		sport_type_code VARCHAR(50) NOT NULL,
		attribute_code VARCHAR(50) NOT NULL,
		is_required BOOLEAN DEFAULT false,
		display_order INTEGER,
		created_at TIMESTAMP
		WITH
			TIME ZONE DEFAULT NOW (),
			updated_at TIMESTAMP
		WITH
			TIME ZONE,
			deleted_at TIMESTAMP
		WITH
			TIME ZONE,
			PRIMARY KEY (sport_type_code, attribute_code)
	);

ALTER TABLE biosteel.sport_attribute_mapping OWNER TO biosteel;

CREATE TABLE
	biosteel.team_attribute_value (
		team_id UUID NOT NULL,
		attribute_code VARCHAR(50) NOT NULL,
		value TEXT,
		created_at TIMESTAMP
		WITH
			TIME ZONE DEFAULT NOW (),
			updated_at TIMESTAMP
		WITH
			TIME ZONE,
			deleted_at TIMESTAMP
		WITH
			TIME ZONE,
			PRIMARY KEY (team_id, attribute_code)
	);

ALTER TABLE biosteel.team_attribute_value OWNER TO biosteel;

CREATE TABLE
	biosteel.team_member_attribute_value (
		team_member_id UUID NOT NULL,
		attribute_code VARCHAR(50) NOT NULL,
		value TEXT,
		created_at TIMESTAMP
		WITH
			TIME ZONE DEFAULT NOW (),
			updated_at TIMESTAMP
		WITH
			TIME ZONE,
			deleted_at TIMESTAMP
		WITH
			TIME ZONE,
			PRIMARY KEY (team_member_id, attribute_code)
	);

ALTER TABLE biosteel.team_member_attribute_value OWNER TO biosteel;

CREATE TABLE
	biosteel.team (
		team_id UUID NOT NULL,
		user_id UUID,
		name VARCHAR(255) NOT NULL,
		logo_media_id UUID,
		description TEXT,
		sport_type_code VARCHAR(50),
		age_group VARCHAR(50),
		division VARCHAR(100),
		season_year INTEGER,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT team_pk PRIMARY KEY (team_id)
	);

ALTER TABLE biosteel.team OWNER TO biosteel;

CREATE TABLE
	biosteel.team_member (
		team_member_id UUID NOT NULL,
		team_id UUID NOT NULL,
		user_id UUID,
		is_player BOOLEAN,
		is_contact BOOLEAN,
		player_id UUID,
		contact_id UUID,
		invitation_id UUID,
		role VARCHAR(50) NOT NULL,
		joined_at TIMESTAMP
		WITH
			TIME ZONE DEFAULT NOW (),
			left_at TIMESTAMP
		WITH
			TIME ZONE,
			CONSTRAINT team_member_pk PRIMARY KEY (team_member_id)
	);

ALTER TABLE biosteel.team_member OWNER TO biosteel;

CREATE TABLE
	biosteel.player_contact (
		player_contact_id UUID NOT NULL,
		player_member_id UUID,
		contact_member_id UUID,
		relationship_type VARCHAR(50),
		is_primary_contact BOOLEAN,
		can_pickup BOOLEAN,
		is_emergency_contact BOOLEAN,
		can_view_medical_info BOOLEAN,
		receives_notifications BOOLEAN,
		notes TEXT,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT player_contact_pk PRIMARY KEY (player_contact_id)
	);

ALTER TABLE biosteel.player_contact OWNER TO biosteel;

CREATE TABLE
	biosteel.event (
		event_id UUID NOT NULL,
		team_id UUID NOT NULL,
		user_id UUID NOT NULL,
		event_type character varying(255),
		event_status character varying(255),
		title VARCHAR(255) NOT NULL,
		description TEXT,
		stream_id TEXT,
		location TEXT,
		location_id UUID,
		start_time TIMESTAMP
		WITH
			TIME ZONE NOT NULL,
			end_time TIMESTAMP
		WITH
			TIME ZONE NOT NULL,
			arrival_time TIMESTAMP
		WITH
			TIME ZONE,
			is_recurring BOOLEAN DEFAULT FALSE,
			recurrence_rule TEXT,
			banner_media_id UUID,
			logo_media_id UUID,
			game_id UUID,
			practice_id UUID,
			created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT event_pk PRIMARY KEY (event_id)
	);

ALTER TABLE biosteel.event OWNER TO biosteel;

CREATE TABLE
	biosteel.event_attendance (
		event_attendance_id UUID NOT NULL,
		event_id UUID NOT NULL,
		team_member_id UUID,
		event_attendance_status character varying(255),
		notes TEXT,
		registered_at timestamp
		with
			time zone,
			checked_in_at timestamp
		with
			time zone,
			created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT event_attendance_pk PRIMARY KEY (event_attendance_id)
	);

ALTER TABLE biosteel.event_attendance OWNER TO biosteel;

CREATE TABLE
	biosteel.practice (
		practice_id UUID NOT NULL,
		instructions text,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT practice_pk PRIMARY KEY (practice_id)
	);

ALTER TABLE biosteel.practice OWNER TO biosteel;

CREATE TABLE
	biosteel.period_type (
		code VARCHAR(50) NOT NULL,
		description TEXT,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT period_type_pk PRIMARY KEY (code)
	);

ALTER TABLE biosteel.period_type OWNER TO biosteel;

CREATE TABLE
	biosteel.sport_period_definition (
		sport_period_definition_id UUID NOT NULL,
		sport_type_code VARCHAR(50) NOT NULL,
		period_type_code VARCHAR(50) NOT NULL,
		period_number INTEGER,
		period_name VARCHAR(50),
		default_duration_minutes INTEGER,
		display_order INTEGER,
		is_scoring_period BOOLEAN DEFAULT TRUE,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT sport_period_definition_pk PRIMARY KEY (sport_period_definition_id)
	);

ALTER TABLE biosteel.sport_period_definition OWNER TO biosteel;

CREATE TABLE
	biosteel.game (
		game_id UUID NOT NULL,
		home_team_id UUID,
		away_team_id UUID,
		home_score INTEGER DEFAULT 0,
		away_score INTEGER DEFAULT 0,
		current_period INTEGER,
		current_period_definition_id UUID,
		period_count INTEGER,
		is_home_game BOOLEAN DEFAULT true,
		away_team_name text,
		status VARCHAR(50) DEFAULT 'SCHEDULED',
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT game_pk PRIMARY KEY (game_id)
	);

ALTER TABLE biosteel.game OWNER TO biosteel;

CREATE TABLE
	biosteel.game_score (
		score_id UUID NOT NULL,
		game_id UUID,
		team_id UUID NULL,
		player_id UUID,
		user_id UUID,
		score_type_id UUID,
		period_definition_id UUID NULL,
		score_value text,
		period INTEGER,
		is_home_team BOOLEAN DEFAULT true,
		player_name VARCHAR(255),
		team_name VARCHAR(255),
		recorded_at TIMESTAMP
		WITH
			TIME ZONE NOT NULL,
			created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT game_score_pk PRIMARY KEY (score_id)
	);

ALTER TABLE biosteel.game_score OWNER TO biosteel;

CREATE TABLE
	biosteel.game_statistic (
		stat_id UUID NOT NULL,
		game_id UUID,
		team_id UUID NULL,
		player_id UUID,
		user_id UUID,
		stat_type VARCHAR(50) NOT NULL,
		stat_value INTEGER,
		sport_period_definition_id UUID,
		game_score_id UUID,
		home_score_total INTEGER,
		away_score_total INTEGER,
		metadata TEXT,
		recorded_at TIMESTAMP
		WITH
			TIME ZONE DEFAULT NOW (),
			created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT game_statistic_pk PRIMARY KEY (stat_id)
	);

ALTER TABLE biosteel.game_statistic OWNER TO biosteel;

CREATE TABLE
	biosteel.score_type (
		score_type_id UUID NOT NULL,
		sport_type VARCHAR(100) NOT NULL,
		name VARCHAR(100) NOT NULL,
		points INTEGER NOT NULL,
		description TEXT,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT score_type_pk PRIMARY KEY (score_type_id)
	);

ALTER TABLE biosteel.score_type OWNER TO biosteel;

CREATE TABLE
	biosteel.post (
		post_id UUID NOT NULL,
		team_id UUID NOT NULL,
		event_id UUID,
		user_id UUID NOT NULL,
		post_type character varying(255),
		layout_type character varying(255),
		location_id UUID,
		content TEXT NOT NULL,
		visibility VARCHAR(50) DEFAULT 'team_only',
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT post_pk PRIMARY KEY (post_id)
	);

ALTER TABLE biosteel.post OWNER TO biosteel;

CREATE TABLE
	biosteel.post_media (
		post_media_id UUID NOT NULL,
		post_id UUID,
		media_id UUID,
		display_order INTEGER,
		title text,
		body text,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT post_media_pk PRIMARY KEY (post_media_id)
	);

ALTER TABLE biosteel.post_media OWNER TO biosteel;

CREATE TABLE
	biosteel.post_reaction (
		post_reaction_id UUID NOT NULL,
		user_id UUID NOT NULL,
		post_id UUID NOT NULL,
		reaction_type VARCHAR(50),
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT post_reaction_pk PRIMARY KEY (post_reaction_id)
	);

ALTER TABLE biosteel.post_reaction OWNER TO biosteel;

CREATE TABLE
	biosteel.post_share (
		post_share_id UUID NOT NULL,
		user_id UUID NOT NULL,
		post_id UUID NOT NULL,
		body TEXT,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT post_share_pk PRIMARY KEY (post_share_id)
	);

ALTER TABLE biosteel.post_share OWNER TO biosteel;

CREATE TABLE
	biosteel.post_comment (
		post_comment_id UUID NOT NULL,
		user_id UUID NOT NULL,
		post_id UUID NOT NULL,
		content TEXT NOT NULL,
		parent_comment_id UUID,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT post_comment_pk PRIMARY KEY (post_comment_id)
	);

ALTER TABLE biosteel.post_comment OWNER TO biosteel;

CREATE TABLE
	biosteel.team_chat (
		team_chat_id UUID NOT NULL,
		team_id UUID NOT NULL,
		event_id UUID,
		user_id UUID NOT NULL,
		message text,
		message_type VARCHAR(50),
		media_id UUID,
		parent_chat_id UUID,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT team_chat_pk PRIMARY KEY (team_chat_id)
	);

ALTER TABLE biosteel.team_chat OWNER TO biosteel;

CREATE TABLE
	biosteel.invitation (
		invitation_id UUID NOT NULL,
		team_id UUID NOT NULL,
		event_id UUID,
		user_id UUID,
		email VARCHAR(255),
		first_name VARCHAR(100),
		last_name VARCHAR(100),
		team_role VARCHAR(50),
		invitation_code VARCHAR(6) NOT NULL UNIQUE,
		invitation_status character varying(255) DEFAULT 'PENDING',
		expires_at timestamp
		with
			time zone,
			used_at timestamp
		with
			time zone,
			created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT invitation_pk PRIMARY KEY (invitation_id)
	);

ALTER TABLE biosteel.invitation OWNER TO biosteel;

CREATE TABLE
	biosteel.media (
		media_id UUID NOT NULL,
		team_id UUID,
		user_id UUID,
		media_type character varying(255),
		file_system_id VARCHAR(255) NULL,
		file_name VARCHAR(255) NOT NULL,
		file_extension VARCHAR(10) NULL,
		file_size BIGINT NULL,
		title text,
		body text,
		mime_type VARCHAR(100) NULL,
		url TEXT,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT media_pk PRIMARY KEY (media_id)
	);

ALTER TABLE biosteel.media OWNER TO biosteel;

CREATE TABLE
	biosteel.email_notification (
		email_notification_id UUID NOT NULL,
		recipient_email VARCHAR(255) NOT NULL,
		email_template_id UUID NOT NULL,
		email_status character varying(255),
		entity_type character varying(255),
		entity_id integer,
		subject VARCHAR(255),
		body TEXT,
		sent_at TIMESTAMP
		WITH
			TIME ZONE,
			delivered_at TIMESTAMP
		WITH
			TIME ZONE,
			opened_at TIMESTAMP
		WITH
			TIME ZONE,
			clicked_at TIMESTAMP
		WITH
			TIME ZONE,
			bounced_at TIMESTAMP
		WITH
			TIME ZONE,
			failed_at TIMESTAMP
		WITH
			TIME ZONE,
			unsubscribed_at TIMESTAMP
		WITH
			TIME ZONE,
			created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT email_notification_pk PRIMARY KEY (email_notification_id)
	);

ALTER TABLE biosteel.email_notification OWNER TO biosteel;

CREATE TABLE
	biosteel.email_template (
		email_template_id UUID NOT NULL,
		email_type character varying(255),
		template_name VARCHAR(100) NOT NULL,
		subject VARCHAR(255),
		body TEXT,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT email_template_pk PRIMARY KEY (email_template_id)
	);

ALTER TABLE biosteel.email_template OWNER TO biosteel;

CREATE TABLE
	biosteel.audit_log (
		audit_log_id UUID NOT NULL,
		date TIMESTAMP,
		action VARCHAR(255),
		action_status VARCHAR(50),
		user_id UUID,
		email VARCHAR(255),
		ip_address VARCHAR(50),
		session_id VARCHAR(100),
		message TEXT,
		user_agent TEXT,
		extra_data TEXT,
		CONSTRAINT audit_log_pk PRIMARY KEY (audit_log_id)
	);

ALTER TABLE biosteel.audit_log OWNER TO biosteel;

CREATE TABLE
	biosteel.location (
		location_id UUID NOT NULL,
		address_line_1 text,
		address_line_2 text,
		major_intersection text,
		postal_zip VARCHAR(100),
		city VARCHAR(100),
		province VARCHAR(100),
		country VARCHAR(100),
		place_name text,
		place_guid character varying(255),
		longitude numeric,
		latitude numeric,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT location_pk PRIMARY KEY (location_id)
	);

ALTER TABLE biosteel.location OWNER TO biosteel;

CREATE TABLE
	biosteel.device_token (
		device_token_id UUID NOT NULL,
		user_id UUID,
		token VARCHAR(255) NOT NULL UNIQUE,
		device_type VARCHAR(50),
		app_version VARCHAR(50),
		is_active BOOLEAN DEFAULT TRUE,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT device_token_pk PRIMARY KEY (device_token_id)
	);

ALTER TABLE biosteel.device_token OWNER TO biosteel;

CREATE TABLE
	biosteel.notification_log (
		notification_log_id UUID NOT NULL,
		user_id UUID,
		team_id UUID,
		title VARCHAR(255),
		body TEXT,
		image_url TEXT,
		data TEXT,
		notification_type VARCHAR(100),
		target_type VARCHAR(50),
		delivery_status VARCHAR(50),
		error_message TEXT,
		created_at timestamp
		with
			time zone DEFAULT NOW (),
			CONSTRAINT notification_log_pk PRIMARY KEY (notification_log_id)
	);

ALTER TABLE biosteel.notification_log OWNER TO biosteel;

CREATE TABLE
	biosteel.advertisement (
		ad_id UUID NOT NULL,
		title VARCHAR(255),
		description TEXT,
		image_media_id UUID,
		image_url TEXT,
		target_url TEXT,
		analytic_event TEXT,
		settings_json TEXT,
		type VARCHAR(50) DEFAULT 'CARD',
		placement VARCHAR(50) DEFAULT 'EVENTS',
		is_active BOOLEAN DEFAULT true,
		start_date TIMESTAMP
		WITH
			TIME ZONE,
			end_date TIMESTAMP
		WITH
			TIME ZONE,
			created_at timestamp
		with
			time zone DEFAULT NOW (),
			updated_at timestamp
		with
			time zone,
			deleted_at timestamp
		with
			time zone,
			CONSTRAINT advertisement_pk PRIMARY KEY (ad_id)
	);

ALTER TABLE biosteel.advertisement OWNER TO biosteel;

ALTER TABLE biosteel.user_account ADD CONSTRAINT media_user_account_fk FOREIGN KEY (profile_photo_media_id) REFERENCES biosteel.media (media_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_profile_photo_media_id_on_user_account on biosteel.user_account using btree (profile_photo_media_id);

ALTER TABLE biosteel.user_role ADD CONSTRAINT user_account_user_role_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_user_role on biosteel.user_role using btree (user_id);

ALTER TABLE biosteel.user_role ADD CONSTRAINT role_user_role_fk FOREIGN KEY (role_id) REFERENCES biosteel.role (role_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_role_id_on_user_role on biosteel.user_role using btree (role_id);

ALTER TABLE biosteel.user_verification_token ADD CONSTRAINT user_account_user_verification_token_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_user_verification_token on biosteel.user_verification_token using btree (user_id);

ALTER TABLE biosteel.user_password_reset_token ADD CONSTRAINT user_account_user_password_reset_token_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_user_password_reset_token on biosteel.user_password_reset_token using btree (user_id);

ALTER TABLE biosteel.user_preference ADD CONSTRAINT user_account_user_preference_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_user_preference on biosteel.user_preference using btree (user_id);

ALTER TABLE biosteel.user_notification ADD CONSTRAINT user_account_user_notification_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_user_notification on biosteel.user_notification using btree (user_id);

ALTER TABLE biosteel.player ADD CONSTRAINT user_account_player_fk FOREIGN KEY (parent_user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_parent_user_id_on_player on biosteel.player using btree (parent_user_id);

ALTER TABLE biosteel.player ADD CONSTRAINT media_player_fk FOREIGN KEY (logo_media_id) REFERENCES biosteel.media (media_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_logo_media_id_on_player on biosteel.player using btree (logo_media_id);

ALTER TABLE biosteel.contact ADD CONSTRAINT media_contact_fk FOREIGN KEY (logo_media_id) REFERENCES biosteel.media (media_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_logo_media_id_on_contact on biosteel.contact using btree (logo_media_id);

ALTER TABLE biosteel.sport_attribute_mapping ADD CONSTRAINT sport_type_sport_attribute_mapping_fk FOREIGN KEY (sport_type_code) REFERENCES biosteel.sport_type (code) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_sport_type_code_on_sport_attribute_mapping on biosteel.sport_attribute_mapping using btree (sport_type_code);

ALTER TABLE biosteel.sport_attribute_mapping ADD CONSTRAINT sport_attribute_definition_sport_attribute_mapping_fk FOREIGN KEY (attribute_code) REFERENCES biosteel.sport_attribute_definition (code) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_attribute_code_on_sport_attribute_mapping on biosteel.sport_attribute_mapping using btree (attribute_code);

ALTER TABLE biosteel.team_attribute_value ADD CONSTRAINT team_team_attribute_value_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_team_attribute_value on biosteel.team_attribute_value using btree (team_id);

ALTER TABLE biosteel.team_attribute_value ADD CONSTRAINT sport_attribute_definition_team_attribute_value_fk FOREIGN KEY (attribute_code) REFERENCES biosteel.sport_attribute_definition (code) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_attribute_code_on_team_attribute_value on biosteel.team_attribute_value using btree (attribute_code);

ALTER TABLE biosteel.team_member_attribute_value ADD CONSTRAINT team_member_team_member_attribute_value_fk FOREIGN KEY (team_member_id) REFERENCES biosteel.team_member (team_member_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_member_id_on_team_member_attribute_value on biosteel.team_member_attribute_value using btree (team_member_id);

ALTER TABLE biosteel.team_member_attribute_value ADD CONSTRAINT sport_attribute_definition_team_member_attribute_value_fk FOREIGN KEY (attribute_code) REFERENCES biosteel.sport_attribute_definition (code) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_attribute_code_on_team_member_attribute_value on biosteel.team_member_attribute_value using btree (attribute_code);

ALTER TABLE biosteel.team ADD CONSTRAINT user_account_team_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_team on biosteel.team using btree (user_id);

ALTER TABLE biosteel.team ADD CONSTRAINT media_team_fk FOREIGN KEY (logo_media_id) REFERENCES biosteel.media (media_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_logo_media_id_on_team on biosteel.team using btree (logo_media_id);

ALTER TABLE biosteel.team ADD CONSTRAINT sport_type_team_fk FOREIGN KEY (sport_type_code) REFERENCES biosteel.sport_type (code) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_sport_type_code_on_team on biosteel.team using btree (sport_type_code);

ALTER TABLE biosteel.team_member ADD CONSTRAINT team_team_member_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_team_member on biosteel.team_member using btree (team_id);

ALTER TABLE biosteel.team_member ADD CONSTRAINT user_account_team_member_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_team_member on biosteel.team_member using btree (user_id);

ALTER TABLE biosteel.team_member ADD CONSTRAINT player_team_member_fk FOREIGN KEY (player_id) REFERENCES biosteel.player (player_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_player_id_on_team_member on biosteel.team_member using btree (player_id);

ALTER TABLE biosteel.team_member ADD CONSTRAINT contact_team_member_fk FOREIGN KEY (contact_id) REFERENCES biosteel.contact (contact_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_contact_id_on_team_member on biosteel.team_member using btree (contact_id);

ALTER TABLE biosteel.team_member ADD CONSTRAINT invitation_team_member_fk FOREIGN KEY (invitation_id) REFERENCES biosteel.invitation (invitation_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_invitation_id_on_team_member on biosteel.team_member using btree (invitation_id);

ALTER TABLE biosteel.player_contact ADD CONSTRAINT team_member_player_contact_fk FOREIGN KEY (player_member_id) REFERENCES biosteel.team_member (team_member_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_player_member_id_on_player_contact on biosteel.player_contact using btree (player_member_id);

ALTER TABLE biosteel.player_contact ADD CONSTRAINT team_member_player_contact_fk_1 FOREIGN KEY (contact_member_id) REFERENCES biosteel.team_member (team_member_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_contact_member_id_on_player_contact on biosteel.player_contact using btree (contact_member_id);

ALTER TABLE biosteel.event ADD CONSTRAINT team_event_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_event on biosteel.event using btree (team_id);

ALTER TABLE biosteel.event ADD CONSTRAINT user_account_event_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_event on biosteel.event using btree (user_id);

ALTER TABLE biosteel.event ADD CONSTRAINT location_event_fk FOREIGN KEY (location_id) REFERENCES biosteel.location (location_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_location_id_on_event on biosteel.event using btree (location_id);

ALTER TABLE biosteel.event ADD CONSTRAINT media_event_fk FOREIGN KEY (banner_media_id) REFERENCES biosteel.media (media_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_banner_media_id_on_event on biosteel.event using btree (banner_media_id);

ALTER TABLE biosteel.event ADD CONSTRAINT media_event_fk_1 FOREIGN KEY (logo_media_id) REFERENCES biosteel.media (media_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_logo_media_id_on_event on biosteel.event using btree (logo_media_id);

ALTER TABLE biosteel.event ADD CONSTRAINT game_event_fk FOREIGN KEY (game_id) REFERENCES biosteel.game (game_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_game_id_on_event on biosteel.event using btree (game_id);

ALTER TABLE biosteel.event ADD CONSTRAINT practice_event_fk FOREIGN KEY (practice_id) REFERENCES biosteel.practice (practice_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_practice_id_on_event on biosteel.event using btree (practice_id);

ALTER TABLE biosteel.event_attendance ADD CONSTRAINT event_event_attendance_fk FOREIGN KEY (event_id) REFERENCES biosteel.event (event_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_event_id_on_event_attendance on biosteel.event_attendance using btree (event_id);

ALTER TABLE biosteel.event_attendance ADD CONSTRAINT team_member_event_attendance_fk FOREIGN KEY (team_member_id) REFERENCES biosteel.team_member (team_member_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_member_id_on_event_attendance on biosteel.event_attendance using btree (team_member_id);

ALTER TABLE biosteel.sport_period_definition ADD CONSTRAINT sport_type_sport_period_definition_fk FOREIGN KEY (sport_type_code) REFERENCES biosteel.sport_type (code) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_sport_type_code_on_sport_period_definition on biosteel.sport_period_definition using btree (sport_type_code);

ALTER TABLE biosteel.sport_period_definition ADD CONSTRAINT period_type_sport_period_definition_fk FOREIGN KEY (period_type_code) REFERENCES biosteel.period_type (code) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_period_type_code_on_sport_period_definition on biosteel.sport_period_definition using btree (period_type_code);

ALTER TABLE biosteel.game ADD CONSTRAINT team_game_fk FOREIGN KEY (home_team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_home_team_id_on_game on biosteel.game using btree (home_team_id);

ALTER TABLE biosteel.game ADD CONSTRAINT team_game_fk_1 FOREIGN KEY (away_team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_away_team_id_on_game on biosteel.game using btree (away_team_id);

ALTER TABLE biosteel.game ADD CONSTRAINT sport_period_definition_game_fk FOREIGN KEY (current_period_definition_id) REFERENCES biosteel.sport_period_definition (sport_period_definition_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_current_period_definition_id_on_game on biosteel.game using btree (current_period_definition_id);

ALTER TABLE biosteel.game_score ADD CONSTRAINT game_game_score_fk FOREIGN KEY (game_id) REFERENCES biosteel.game (game_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_game_id_on_game_score on biosteel.game_score using btree (game_id);

ALTER TABLE biosteel.game_score ADD CONSTRAINT team_game_score_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_game_score on biosteel.game_score using btree (team_id);

ALTER TABLE biosteel.game_score ADD CONSTRAINT player_game_score_fk FOREIGN KEY (player_id) REFERENCES biosteel.player (player_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_player_id_on_game_score on biosteel.game_score using btree (player_id);

ALTER TABLE biosteel.game_score ADD CONSTRAINT user_account_game_score_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_game_score on biosteel.game_score using btree (user_id);

ALTER TABLE biosteel.game_score ADD CONSTRAINT score_type_game_score_fk FOREIGN KEY (score_type_id) REFERENCES biosteel.score_type (score_type_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_score_type_id_on_game_score on biosteel.game_score using btree (score_type_id);

ALTER TABLE biosteel.game_score ADD CONSTRAINT sport_period_definition_game_score_fk FOREIGN KEY (period_definition_id) REFERENCES biosteel.sport_period_definition (sport_period_definition_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_period_definition_id_on_game_score on biosteel.game_score using btree (period_definition_id);

ALTER TABLE biosteel.game_statistic ADD CONSTRAINT game_game_statistic_fk FOREIGN KEY (game_id) REFERENCES biosteel.game (game_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_game_id_on_game_statistic on biosteel.game_statistic using btree (game_id);

ALTER TABLE biosteel.game_statistic ADD CONSTRAINT team_game_statistic_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_game_statistic on biosteel.game_statistic using btree (team_id);

ALTER TABLE biosteel.game_statistic ADD CONSTRAINT player_game_statistic_fk FOREIGN KEY (player_id) REFERENCES biosteel.player (player_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_player_id_on_game_statistic on biosteel.game_statistic using btree (player_id);

ALTER TABLE biosteel.game_statistic ADD CONSTRAINT user_account_game_statistic_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_game_statistic on biosteel.game_statistic using btree (user_id);

ALTER TABLE biosteel.game_statistic ADD CONSTRAINT sport_period_definition_game_statistic_fk FOREIGN KEY (sport_period_definition_id) REFERENCES biosteel.sport_period_definition (sport_period_definition_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_sport_period_definition_id_on_game_statistic on biosteel.game_statistic using btree (sport_period_definition_id);

ALTER TABLE biosteel.game_statistic ADD CONSTRAINT game_score_game_statistic_fk FOREIGN KEY (game_score_id) REFERENCES biosteel.game_score (score_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_game_score_id_on_game_statistic on biosteel.game_statistic using btree (game_score_id);

ALTER TABLE biosteel.post ADD CONSTRAINT team_post_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_post on biosteel.post using btree (team_id);

ALTER TABLE biosteel.post ADD CONSTRAINT event_post_fk FOREIGN KEY (event_id) REFERENCES biosteel.event (event_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_event_id_on_post on biosteel.post using btree (event_id);

ALTER TABLE biosteel.post ADD CONSTRAINT user_account_post_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_post on biosteel.post using btree (user_id);

ALTER TABLE biosteel.post ADD CONSTRAINT location_post_fk FOREIGN KEY (location_id) REFERENCES biosteel.location (location_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_location_id_on_post on biosteel.post using btree (location_id);

ALTER TABLE biosteel.post_media ADD CONSTRAINT post_post_media_fk FOREIGN KEY (post_id) REFERENCES biosteel.post (post_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_post_id_on_post_media on biosteel.post_media using btree (post_id);

ALTER TABLE biosteel.post_media ADD CONSTRAINT media_post_media_fk FOREIGN KEY (media_id) REFERENCES biosteel.media (media_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_media_id_on_post_media on biosteel.post_media using btree (media_id);

ALTER TABLE biosteel.post_reaction ADD CONSTRAINT user_account_post_reaction_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_post_reaction on biosteel.post_reaction using btree (user_id);

ALTER TABLE biosteel.post_reaction ADD CONSTRAINT post_post_reaction_fk FOREIGN KEY (post_id) REFERENCES biosteel.post (post_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_post_id_on_post_reaction on biosteel.post_reaction using btree (post_id);

ALTER TABLE biosteel.post_share ADD CONSTRAINT user_account_post_share_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_post_share on biosteel.post_share using btree (user_id);

ALTER TABLE biosteel.post_share ADD CONSTRAINT post_post_share_fk FOREIGN KEY (post_id) REFERENCES biosteel.post (post_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_post_id_on_post_share on biosteel.post_share using btree (post_id);

ALTER TABLE biosteel.post_comment ADD CONSTRAINT user_account_post_comment_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_post_comment on biosteel.post_comment using btree (user_id);

ALTER TABLE biosteel.post_comment ADD CONSTRAINT post_post_comment_fk FOREIGN KEY (post_id) REFERENCES biosteel.post (post_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_post_id_on_post_comment on biosteel.post_comment using btree (post_id);

ALTER TABLE biosteel.post_comment ADD CONSTRAINT post_comment_post_comment_fk FOREIGN KEY (parent_comment_id) REFERENCES biosteel.post_comment (post_comment_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_parent_comment_id_on_post_comment on biosteel.post_comment using btree (parent_comment_id);

ALTER TABLE biosteel.team_chat ADD CONSTRAINT team_team_chat_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_team_chat on biosteel.team_chat using btree (team_id);

ALTER TABLE biosteel.team_chat ADD CONSTRAINT event_team_chat_fk FOREIGN KEY (event_id) REFERENCES biosteel.event (event_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_event_id_on_team_chat on biosteel.team_chat using btree (event_id);

ALTER TABLE biosteel.team_chat ADD CONSTRAINT user_account_team_chat_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_team_chat on biosteel.team_chat using btree (user_id);

ALTER TABLE biosteel.team_chat ADD CONSTRAINT media_team_chat_fk FOREIGN KEY (media_id) REFERENCES biosteel.media (media_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_media_id_on_team_chat on biosteel.team_chat using btree (media_id);

ALTER TABLE biosteel.team_chat ADD CONSTRAINT team_chat_team_chat_fk FOREIGN KEY (parent_chat_id) REFERENCES biosteel.team_chat (team_chat_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_parent_chat_id_on_team_chat on biosteel.team_chat using btree (parent_chat_id);

ALTER TABLE biosteel.invitation ADD CONSTRAINT team_invitation_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_invitation on biosteel.invitation using btree (team_id);

ALTER TABLE biosteel.invitation ADD CONSTRAINT event_invitation_fk FOREIGN KEY (event_id) REFERENCES biosteel.event (event_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_event_id_on_invitation on biosteel.invitation using btree (event_id);

ALTER TABLE biosteel.invitation ADD CONSTRAINT user_account_invitation_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_invitation on biosteel.invitation using btree (user_id);

ALTER TABLE biosteel.media ADD CONSTRAINT team_media_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_media on biosteel.media using btree (team_id);

ALTER TABLE biosteel.media ADD CONSTRAINT user_account_media_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_media on biosteel.media using btree (user_id);

ALTER TABLE biosteel.email_notification ADD CONSTRAINT email_template_email_notification_fk FOREIGN KEY (email_template_id) REFERENCES biosteel.email_template (email_template_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_email_template_id_on_email_notification on biosteel.email_notification using btree (email_template_id);

ALTER TABLE biosteel.audit_log ADD CONSTRAINT user_account_audit_log_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_audit_log on biosteel.audit_log using btree (user_id);

ALTER TABLE biosteel.device_token ADD CONSTRAINT user_account_device_token_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_device_token on biosteel.device_token using btree (user_id);

ALTER TABLE biosteel.notification_log ADD CONSTRAINT user_account_notification_log_fk FOREIGN KEY (user_id) REFERENCES biosteel.user_account (user_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_user_id_on_notification_log on biosteel.notification_log using btree (user_id);

ALTER TABLE biosteel.notification_log ADD CONSTRAINT team_notification_log_fk FOREIGN KEY (team_id) REFERENCES biosteel.team (team_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_team_id_on_notification_log on biosteel.notification_log using btree (team_id);

ALTER TABLE biosteel.advertisement ADD CONSTRAINT media_advertisement_fk FOREIGN KEY (image_media_id) REFERENCES biosteel.media (media_id) MATCH FULL ON DELETE CASCADE ON UPDATE CASCADE;

CREATE index concurrently index_image_media_id_on_advertisement on biosteel.advertisement using btree (image_media_id);