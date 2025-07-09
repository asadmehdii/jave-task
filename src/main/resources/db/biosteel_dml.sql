INSERT INTO
    biosteel.role (role_id, name, description)
VALUES
    -- System Roles
    (
        gen_random_uuid (),
        'ROLE_SUPER_ADMIN',
        'Complete system access with ability to manage all aspects of the platform'
    ),
    (
        gen_random_uuid (),
        'ROLE_COACH',
        'Access to team management, training plans, and performance data'
    ),
    (
        gen_random_uuid (),
        'ROLE_USER',
        'Parent/ Guardian. Basic authenticated user access'
    ),
    (
        gen_random_uuid (),
        'ROLE_MANAGER',
        'Manager of a team. Access to team management, training plans, and performance data'
    ),
    (
        gen_random_uuid (),
        'ROLE_PLAYER',
        'Basic authenticated user access, player of a team'
    );

-- Clear existing data
TRUNCATE TABLE biosteel.sport_attribute_mapping CASCADE;

TRUNCATE TABLE biosteel.sport_attribute_definition CASCADE;

TRUNCATE TABLE biosteel.sport_type CASCADE;

-- Insert Sport Types
INSERT INTO
    biosteel.sport_type (code, name, description)
VALUES
    (
        'HOCKEY',
        'Ice Hockey',
        'Ice Hockey with standard rules and regulations'
    ),
    (
        'BASKETBALL',
        'Basketball',
        'Basketball with standard rules and regulations'
    ),
    (
        'FOOTBALL',
        'American Football',
        'American Football with standard rules and regulations'
    ),
    (
        'SOCCER',
        'Soccer',
        'Association Football/Soccer with standard rules and regulations'
    ),
    (
        'BASEBALL',
        'Baseball',
        'Baseball with standard rules and regulations'
    ),
    (
        'PICKLEBALL',
        'Pickleball',
        'Pickleball with standard rules and regulations'
    );

-- Insert Common Attributes for Teams
INSERT INTO
    biosteel.sport_attribute_definition (
        code,
        name,
        description,
        data_type,
        is_required,
        validation_rules,
        entity_type
    )
VALUES
    (
        'DIVISION_TYPE',
        'Division Type',
        'Team division category',
        'ENUM',
        true,
        '{"values": ["Varsity", "Junior Varsity", "Youth", "Professional", "Amateur", "Recreation"]}',
        'TEAM'
    ),
    (
        'AGE_GROUP',
        'Age Group',
        'Age group category',
        'ENUM',
        true,
        '{"values": ["U8", "U10", "U12", "U14", "U16", "U18", "Adult"]}',
        'TEAM'
    ),
    (
        'ROSTER_SIZE',
        'Roster Size',
        'Maximum roster size',
        'NUMBER',
        true,
        '{"min": 1, "max": 100}',
        'TEAM'
    ),
    (
        'SEASON_TYPE',
        'Season Type',
        'Type of season',
        'ENUM',
        true,
        '{"values": ["Fall", "Winter", "Spring", "Summer", "Year-Round"]}',
        'TEAM'
    ),
    (
        'SKILL_LEVEL',
        'Skill Level',
        'Team skill level category',
        'ENUM',
        false,
        '{"values": ["Beginner", "Intermediate", "Advanced", "Elite"]}',
        'TEAM'
    );

-- Insert Common Attributes for Players
INSERT INTO
    biosteel.sport_attribute_definition (
        code,
        name,
        description,
        data_type,
        is_required,
        validation_rules,
        entity_type
    )
VALUES
    (
        'JERSEY_NUMBER',
        'Jersey Number',
        'Player jersey number',
        'STRING',
        true,
        '{"pattern": "^[0-9]{1,3}$"}',
        'PLAYER'
    ),
    (
        'EXPERIENCE_YEARS',
        'Years of Experience',
        'Years playing the sport',
        'NUMBER',
        false,
        '{"min": 0, "max": 50}',
        'PLAYER'
    );

INSERT INTO
    biosteel.sport_attribute_definition (
        code,
        name,
        description,
        data_type,
        is_required,
        validation_rules,
        entity_type
    )
VALUES
    (
        'TEAM_NAME',
        'Team Name',
        'Name of the team',
        'STRING',
        true,
        '{"minLength": 3, "maxLength": 50}',
        'TEAM'
    ),
    (
        'TEAM_DESCRIPTION',
        'Team Description',
        'Description of the team',
        'STRING',
        true,
        '{"minLength": 3, "maxLength": 255}',
        'TEAM'
    );

-- Hockey-specific attributes
INSERT INTO
    biosteel.sport_attribute_definition (
        code,
        name,
        description,
        data_type,
        is_required,
        validation_rules,
        entity_type
    )
VALUES
    (
        'HOCKEY_POSITION',
        'Position',
        'Primary hockey position',
        'ENUM',
        true,
        '{"values": ["Center", "Left Wing", "Right Wing", "Defenseman", "Goalie"]}',
        'PLAYER'
    ),
    (
        'HOCKEY_SHOOTS',
        'Shoots',
        'Player shooting side',
        'ENUM',
        true,
        '{"values": ["Left", "Right"]}',
        'PLAYER'
    ),
    (
        'HOCKEY_PERIODS',
        'Periods',
        'Number of periods per game',
        'NUMBER',
        true,
        '{"value": 3}',
        'TEAM'
    ),
    (
        'HOCKEY_PERIOD_DURATION',
        'Period Duration',
        'Duration of each period in minutes',
        'NUMBER',
        true,
        '{"value": 20}',
        'TEAM'
    );

-- Basketball-specific attributes
INSERT INTO
    biosteel.sport_attribute_definition (
        code,
        name,
        description,
        data_type,
        is_required,
        validation_rules,
        entity_type
    )
VALUES
    (
        'BASKETBALL_POSITION',
        'Position',
        'Primary basketball position',
        'ENUM',
        true,
        '{"values": ["Point Guard", "Shooting Guard", "Small Forward", "Power Forward", "Center"]}',
        'PLAYER'
    ),
    (
        'BASKETBALL_QUARTERS',
        'Quarters',
        'Number of quarters per game',
        'NUMBER',
        true,
        '{"value": 4}',
        'TEAM'
    ),
    (
        'BASKETBALL_QUARTER_DURATION',
        'Quarter Duration',
        'Duration of each quarter in minutes',
        'NUMBER',
        true,
        '{"value": 12}',
        'TEAM'
    );

-- Football-specific attributes
INSERT INTO
    biosteel.sport_attribute_definition (
        code,
        name,
        description,
        data_type,
        is_required,
        validation_rules,
        entity_type
    )
VALUES
    (
        'FOOTBALL_POSITION',
        'Position',
        'Primary football position',
        'ENUM',
        true,
        '{"values": ["Quarterback", "Running Back", "Wide Receiver", "Tight End", "Offensive Lineman", 
                 "Defensive Lineman", "Linebacker", "Cornerback", "Safety", "Kicker", "Punter"]}',
        'PLAYER'
    ),
    (
        'FOOTBALL_SQUAD',
        'Squad',
        'Player squad designation',
        'ENUM',
        true,
        '{"values": ["Offense", "Defense", "Special Teams"]}',
        'PLAYER'
    ),
    (
        'FOOTBALL_QUARTERS',
        'Quarters',
        'Number of quarters per game',
        'NUMBER',
        true,
        '{"value": 4}',
        'TEAM'
    ),
    (
        'FOOTBALL_QUARTER_DURATION',
        'Quarter Duration',
        'Duration of each quarter in minutes',
        'NUMBER',
        true,
        '{"value": 15}',
        'TEAM'
    );

-- Soccer-specific attributes
INSERT INTO
    biosteel.sport_attribute_definition (
        code,
        name,
        description,
        data_type,
        is_required,
        validation_rules,
        entity_type
    )
VALUES
    (
        'SOCCER_POSITION',
        'Position',
        'Primary soccer position',
        'ENUM',
        true,
        '{"values": ["Goalkeeper", "Defender", "Midfielder", "Forward"]}',
        'PLAYER'
    ),
    (
        'SOCCER_PREFERRED_FOOT',
        'Preferred Foot',
        'Player preferred foot',
        'ENUM',
        true,
        '{"values": ["Left", "Right", "Both"]}',
        'PLAYER'
    ),
    (
        'SOCCER_HALVES',
        'Halves',
        'Number of halves per game',
        'NUMBER',
        true,
        '{"value": 2}',
        'TEAM'
    ),
    (
        'SOCCER_HALF_DURATION',
        'Half Duration',
        'Duration of each half in minutes',
        'NUMBER',
        true,
        '{"value": 45}',
        'TEAM'
    );

-- Baseball-specific attributes
INSERT INTO
    biosteel.sport_attribute_definition (
        code,
        name,
        description,
        data_type,
        is_required,
        validation_rules,
        entity_type
    )
VALUES
    (
        'BASEBALL_POSITION',
        'Position',
        'Primary baseball position',
        'ENUM',
        true,
        '{"values": ["Pitcher", "Catcher", "First Baseman", "Second Baseman", "Third Baseman", "Shortstop", 
                 "Left Fielder", "Center Fielder", "Right Fielder", "Designated Hitter"]}',
        'PLAYER'
    ),
    (
        'BASEBALL_BATS',
        'Bats',
        'Player batting side',
        'ENUM',
        true,
        '{"values": ["Left", "Right", "Switch"]}',
        'PLAYER'
    ),
    (
        'BASEBALL_THROWS',
        'Throws',
        'Player throwing hand',
        'ENUM',
        true,
        '{"values": ["Left", "Right"]}',
        'PLAYER'
    ),
    (
        'BASEBALL_INNINGS',
        'Innings',
        'Number of innings per game',
        'NUMBER',
        true,
        '{"value": 9}',
        'TEAM'
    );

-- Pickleball-specific attributes
INSERT INTO
    biosteel.sport_attribute_definition (
        code,
        name,
        description,
        data_type,
        is_required,
        validation_rules,
        entity_type
    )
VALUES
    (
        'PICKLEBALL_STYLE',
        'Style',
        'Player style',
        'ENUM',
        true,
        '{"values": ["Singles", "Doubles", "Mixed Doubles"]}',
        'PLAYER'
    ),
    (
        'PICKLEBALL_RATING',
        'USAPA Rating',
        'Player USAPA rating',
        'NUMBER',
        false,
        '{"min": 1.0, "max": 6.0, "step": 0.5}',
        'PLAYER'
    ),
    (
        'PICKLEBALL_DOMINANT_HAND',
        'Dominant Hand',
        'Player dominant hand',
        'ENUM',
        true,
        '{"values": ["Left", "Right"]}',
        'PLAYER'
    );

-- Now map these attributes to sports
-- Hockey Mappings
INSERT INTO
    biosteel.sport_attribute_mapping (
        sport_type_code,
        attribute_code,
        is_required,
        display_order
    )
VALUES
    -- Team Attributes
    ('HOCKEY', 'TEAM_NAME', true, 1),
    ('HOCKEY', 'TEAM_DESCRIPTION', false, 2),
    ('HOCKEY', 'DIVISION_TYPE', true, 3),
    ('HOCKEY', 'AGE_GROUP', true, 4),
    ('HOCKEY', 'ROSTER_SIZE', true, 5),
    ('HOCKEY', 'SEASON_TYPE', true, 6),
    ('HOCKEY', 'SKILL_LEVEL', false, 7),
    ('HOCKEY', 'HOCKEY_PERIODS', true, 8),
    ('HOCKEY', 'HOCKEY_PERIOD_DURATION', true, 9),
    -- Player Attributes
    ('HOCKEY', 'JERSEY_NUMBER', true, 1),
    ('HOCKEY', 'EXPERIENCE_YEARS', false, 4),
    ('HOCKEY', 'HOCKEY_POSITION', true, 5),
    ('HOCKEY', 'HOCKEY_SHOOTS', true, 6);

-- Basketball Mappings
INSERT INTO
    biosteel.sport_attribute_mapping (
        sport_type_code,
        attribute_code,
        is_required,
        display_order
    )
VALUES
    -- Team Attributes
    ('BASKETBALL', 'TEAM_NAME', true, 1),
    ('BASKETBALL', 'TEAM_DESCRIPTION', false, 2),
    ('BASKETBALL', 'DIVISION_TYPE', true, 3),
    ('BASKETBALL', 'AGE_GROUP', true, 4),
    ('BASKETBALL', 'ROSTER_SIZE', true, 5),
    ('BASKETBALL', 'SEASON_TYPE', true, 6),
    ('BASKETBALL', 'SKILL_LEVEL', false, 7),
    ('BASKETBALL', 'BASKETBALL_QUARTERS', true, 8),
    (
        'BASKETBALL',
        'BASKETBALL_QUARTER_DURATION',
        true,
        9
    ),
    -- Player Attributes
    ('BASKETBALL', 'JERSEY_NUMBER', true, 1),
    ('BASKETBALL', 'EXPERIENCE_YEARS', false, 4),
    ('BASKETBALL', 'BASKETBALL_POSITION', true, 5);

-- Football Mappings
INSERT INTO
    biosteel.sport_attribute_mapping (
        sport_type_code,
        attribute_code,
        is_required,
        display_order
    )
VALUES
    -- Team Attributes
    ('FOOTBALL', 'TEAM_NAME', true, 1),
    ('FOOTBALL', 'TEAM_DESCRIPTION', false, 2),
    ('FOOTBALL', 'DIVISION_TYPE', true, 3),
    ('FOOTBALL', 'AGE_GROUP', true, 4),
    ('FOOTBALL', 'ROSTER_SIZE', true, 5),
    ('FOOTBALL', 'SEASON_TYPE', true, 6),
    ('FOOTBALL', 'SKILL_LEVEL', false, 7),
    ('FOOTBALL', 'FOOTBALL_QUARTERS', true, 8),
    ('FOOTBALL', 'FOOTBALL_QUARTER_DURATION', true, 9),
    -- Player Attributes
    ('FOOTBALL', 'JERSEY_NUMBER', true, 1),
    ('FOOTBALL', 'EXPERIENCE_YEARS', false, 4),
    ('FOOTBALL', 'FOOTBALL_POSITION', true, 5),
    ('FOOTBALL', 'FOOTBALL_SQUAD', true, 6);

-- Soccer Mappings
INSERT INTO
    biosteel.sport_attribute_mapping (
        sport_type_code,
        attribute_code,
        is_required,
        display_order
    )
VALUES
    -- Team Attributes
    ('SOCCER', 'TEAM_NAME', true, 1),
    ('SOCCER', 'TEAM_DESCRIPTION', false, 2),
    ('SOCCER', 'DIVISION_TYPE', true, 3),
    ('SOCCER', 'AGE_GROUP', true, 4),
    ('SOCCER', 'ROSTER_SIZE', true, 5),
    ('SOCCER', 'SEASON_TYPE', true, 6),
    ('SOCCER', 'SKILL_LEVEL', false, 7),
    ('SOCCER', 'SOCCER_HALVES', true, 8),
    ('SOCCER', 'SOCCER_HALF_DURATION', true, 9),
    -- Player Attributes
    ('SOCCER', 'JERSEY_NUMBER', true, 1),
    ('SOCCER', 'EXPERIENCE_YEARS', false, 4),
    ('SOCCER', 'SOCCER_POSITION', true, 5),
    ('SOCCER', 'SOCCER_PREFERRED_FOOT', true, 6);

-- Baseball Mappings
INSERT INTO
    biosteel.sport_attribute_mapping (
        sport_type_code,
        attribute_code,
        is_required,
        display_order
    )
VALUES
    -- Team Attributes
    ('BASEBALL', 'TEAM_NAME', true, 1),
    ('BASEBALL', 'TEAM_DESCRIPTION', false, 2),
    ('BASEBALL', 'DIVISION_TYPE', true, 3),
    ('BASEBALL', 'AGE_GROUP', true, 4),
    ('BASEBALL', 'ROSTER_SIZE', true, 5),
    ('BASEBALL', 'SEASON_TYPE', true, 6),
    ('BASEBALL', 'SKILL_LEVEL', false, 7),
    ('BASEBALL', 'BASEBALL_INNINGS', true, 8),
    -- Player Attributes
    ('BASEBALL', 'JERSEY_NUMBER', true, 1),
    ('BASEBALL', 'EXPERIENCE_YEARS', false, 4),
    ('BASEBALL', 'BASEBALL_POSITION', true, 5),
    ('BASEBALL', 'BASEBALL_BATS', true, 6),
    ('BASEBALL', 'BASEBALL_THROWS', true, 7);

-- Pickleball Mappings
INSERT INTO
    biosteel.sport_attribute_mapping (
        sport_type_code,
        attribute_code,
        is_required,
        display_order
    )
VALUES
    -- Team Attributes
    ('PICKLEBALL', 'TEAM_NAME', true, 1),
    ('PICKLEBALL', 'TEAM_DESCRIPTION', false, 2),
    ('PICKLEBALL', 'DIVISION_TYPE', true, 3),
    ('PICKLEBALL', 'AGE_GROUP', true, 4),
    ('PICKLEBALL', 'ROSTER_SIZE', true, 5),
    ('PICKLEBALL', 'SEASON_TYPE', true, 6),
    ('PICKLEBALL', 'SKILL_LEVEL', false, 7),
    -- Player Attributes
    ('PICKLEBALL', 'JERSEY_NUMBER', false, 1), -- Optional for pickleball
    ('PICKLEBALL', 'EXPERIENCE_YEARS', false, 4),
    ('PICKLEBALL', 'PICKLEBALL_STYLE', true, 5),
    ('PICKLEBALL', 'PICKLEBALL_RATING', false, 6),
    ('PICKLEBALL', 'PICKLEBALL_DOMINANT_HAND', true, 7);

-- Hockey Score Types
INSERT INTO
    biosteel.score_type (
        score_type_id,
        sport_type,
        name,
        points,
        description
    )
VALUES
    (
        gen_random_uuid (),
        'HOCKEY',
        'Goal',
        1,
        'Regular goal scored during play'
    ),
    (
        gen_random_uuid (),
        'HOCKEY',
        'Power Play Goal',
        1,
        'Goal scored during power play'
    ),
    (
        gen_random_uuid (),
        'HOCKEY',
        'Short Handed Goal',
        1,
        'Goal scored while team is short-handed'
    ),
    (
        gen_random_uuid (),
        'HOCKEY',
        'Empty Net Goal',
        1,
        'Goal scored on empty net'
    ),
    (
        gen_random_uuid (),
        'HOCKEY',
        'Shootout Goal',
        1,
        'Goal scored during shootout'
    );

-- Basketball Score Types
INSERT INTO
    biosteel.score_type (
        score_type_id,
        sport_type,
        name,
        points,
        description
    )
VALUES
    (
        gen_random_uuid (),
        'BASKETBALL',
        'Free Throw',
        1,
        'Free throw shot'
    ),
    (
        gen_random_uuid (),
        'BASKETBALL',
        'Field Goal',
        2,
        'Regular two-point shot'
    ),
    (
        gen_random_uuid (),
        'BASKETBALL',
        'Three Pointer',
        3,
        'Shot from beyond the three-point line'
    );

-- Football Score Types
INSERT INTO
    biosteel.score_type (
        score_type_id,
        sport_type,
        name,
        points,
        description
    )
VALUES
    (
        gen_random_uuid (),
        'FOOTBALL',
        'Touchdown',
        6,
        'Touchdown scored'
    ),
    (
        gen_random_uuid (),
        'FOOTBALL',
        'Field Goal',
        3,
        'Field goal kicked'
    ),
    (
        gen_random_uuid (),
        'FOOTBALL',
        'Extra Point',
        1,
        'Point after touchdown (PAT)'
    ),
    (
        gen_random_uuid (),
        'FOOTBALL',
        'Two Point Conversion',
        2,
        'Two-point conversion after touchdown'
    ),
    (
        gen_random_uuid (),
        'FOOTBALL',
        'Safety',
        2,
        'Safety scored'
    );

-- Soccer Score Types
INSERT INTO
    biosteel.score_type (
        score_type_id,
        sport_type,
        name,
        points,
        description
    )
VALUES
    (
        gen_random_uuid (),
        'SOCCER',
        'Goal',
        1,
        'Regular goal scored during play'
    ),
    (
        gen_random_uuid (),
        'SOCCER',
        'Penalty Goal',
        1,
        'Goal scored from penalty kick'
    ),
    (
        gen_random_uuid (),
        'SOCCER',
        'Own Goal',
        1,
        'Goal accidentally scored in own team''s net'
    );

-- Baseball Score Types
INSERT INTO
    biosteel.score_type (
        score_type_id,
        sport_type,
        name,
        points,
        description
    )
VALUES
    (
        gen_random_uuid (),
        'BASEBALL',
        'Run',
        1,
        'Regular run scored'
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'Home Run',
        1,
        'Run scored via home run'
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'Grand Slam',
        1,
        'Home run with bases loaded'
    );

-- Pickleball Score Types
INSERT INTO
    biosteel.score_type (
        score_type_id,
        sport_type,
        name,
        points,
        description
    )
VALUES
    (
        gen_random_uuid (),
        'PICKLEBALL',
        'Point',
        1,
        'Point scored during rally'
    ),
    (
        gen_random_uuid (),
        'PICKLEBALL',
        'Game Point',
        1,
        'Point that wins the game'
    );

-- Populate period_type table
INSERT INTO
    biosteel.period_type (code, description)
VALUES
    ('REGULAR', 'Regular game period'),
    ('OVERTIME', 'Overtime period'),
    ('SHOOTOUT', 'Shootout/Penalty shootout'),
    ('EXTRA_TIME', 'Extra time (Soccer)'),
    ('EXTRA_INNING', 'Extra inning (Baseball)'),
    ('HALFTIME', 'Halftime break');

-- Populate sport_period_definition table for Hockey
INSERT INTO
    biosteel.sport_period_definition (
        sport_period_definition_id,
        sport_type_code,
        period_type_code,
        period_number,
        period_name,
        default_duration_minutes,
        display_order,
        is_scoring_period
    )
VALUES
    (
        gen_random_uuid (),
        'HOCKEY',
        'REGULAR',
        1,
        '1st Period',
        20,
        1,
        TRUE
    ),
    (
        gen_random_uuid (),
        'HOCKEY',
        'REGULAR',
        2,
        '2nd Period',
        20,
        2,
        TRUE
    ),
    (
        gen_random_uuid (),
        'HOCKEY',
        'REGULAR',
        3,
        '3rd Period',
        20,
        3,
        TRUE
    ),
    (
        gen_random_uuid (),
        'HOCKEY',
        'OVERTIME',
        4,
        'Overtime',
        5,
        4,
        TRUE
    ),
    (
        gen_random_uuid (),
        'HOCKEY',
        'SHOOTOUT',
        5,
        'Shootout',
        NULL,
        5,
        TRUE
    );

-- Populate sport_period_definition table for Basketball
INSERT INTO
    biosteel.sport_period_definition (
        sport_period_definition_id,
        sport_type_code,
        period_type_code,
        period_number,
        period_name,
        default_duration_minutes,
        display_order,
        is_scoring_period
    )
VALUES
    (
        gen_random_uuid (),
        'BASKETBALL',
        'REGULAR',
        1,
        '1st Quarter',
        12,
        1,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASKETBALL',
        'REGULAR',
        2,
        '2nd Quarter',
        12,
        2,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASKETBALL',
        'HALFTIME',
        3,
        'Halftime',
        15,
        3,
        FALSE
    ),
    (
        gen_random_uuid (),
        'BASKETBALL',
        'REGULAR',
        4,
        '3rd Quarter',
        12,
        4,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASKETBALL',
        'REGULAR',
        5,
        '4th Quarter',
        12,
        5,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASKETBALL',
        'OVERTIME',
        6,
        'Overtime',
        5,
        6,
        TRUE
    );

-- Populate sport_period_definition table for Football
INSERT INTO
    biosteel.sport_period_definition (
        sport_period_definition_id,
        sport_type_code,
        period_type_code,
        period_number,
        period_name,
        default_duration_minutes,
        display_order,
        is_scoring_period
    )
VALUES
    (
        gen_random_uuid (),
        'FOOTBALL',
        'REGULAR',
        1,
        '1st Quarter',
        15,
        1,
        TRUE
    ),
    (
        gen_random_uuid (),
        'FOOTBALL',
        'REGULAR',
        2,
        '2nd Quarter',
        15,
        2,
        TRUE
    ),
    (
        gen_random_uuid (),
        'FOOTBALL',
        'HALFTIME',
        3,
        'Halftime',
        12,
        3,
        FALSE
    ),
    (
        gen_random_uuid (),
        'FOOTBALL',
        'REGULAR',
        4,
        '3rd Quarter',
        15,
        4,
        TRUE
    ),
    (
        gen_random_uuid (),
        'FOOTBALL',
        'REGULAR',
        5,
        '4th Quarter',
        15,
        5,
        TRUE
    ),
    (
        gen_random_uuid (),
        'FOOTBALL',
        'OVERTIME',
        6,
        'Overtime',
        15,
        6,
        TRUE
    );

-- Populate sport_period_definition table for Soccer
INSERT INTO
    biosteel.sport_period_definition (
        sport_period_definition_id,
        sport_type_code,
        period_type_code,
        period_number,
        period_name,
        default_duration_minutes,
        display_order,
        is_scoring_period
    )
VALUES
    (
        gen_random_uuid (),
        'SOCCER',
        'REGULAR',
        1,
        '1st Half',
        45,
        1,
        TRUE
    ),
    (
        gen_random_uuid (),
        'SOCCER',
        'HALFTIME',
        2,
        'Halftime',
        15,
        2,
        FALSE
    ),
    (
        gen_random_uuid (),
        'SOCCER',
        'REGULAR',
        3,
        '2nd Half',
        45,
        3,
        TRUE
    ),
    (
        gen_random_uuid (),
        'SOCCER',
        'EXTRA_TIME',
        4,
        'Extra Time 1',
        15,
        4,
        TRUE
    ),
    (
        gen_random_uuid (),
        'SOCCER',
        'EXTRA_TIME',
        5,
        'Extra Time 2',
        15,
        5,
        TRUE
    ),
    (
        gen_random_uuid (),
        'SOCCER',
        'SHOOTOUT',
        6,
        'Penalties',
        NULL,
        6,
        TRUE
    );

-- Populate sport_period_definition table for Baseball
INSERT INTO
    biosteel.sport_period_definition (
        sport_period_definition_id,
        sport_type_code,
        period_type_code,
        period_number,
        period_name,
        default_duration_minutes,
        display_order,
        is_scoring_period
    )
VALUES
    (
        gen_random_uuid (),
        'BASEBALL',
        'REGULAR',
        1,
        '1st Inning',
        NULL,
        1,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'REGULAR',
        2,
        '2nd Inning',
        NULL,
        2,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'REGULAR',
        3,
        '3rd Inning',
        NULL,
        3,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'REGULAR',
        4,
        '4th Inning',
        NULL,
        4,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'REGULAR',
        5,
        '5th Inning',
        NULL,
        5,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'REGULAR',
        6,
        '6th Inning',
        NULL,
        6,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'REGULAR',
        7,
        '7th Inning',
        NULL,
        7,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'REGULAR',
        8,
        '8th Inning',
        NULL,
        8,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'REGULAR',
        9,
        '9th Inning',
        NULL,
        9,
        TRUE
    ),
    (
        gen_random_uuid (),
        'BASEBALL',
        'EXTRA_INNING',
        10,
        'Extra Innings',
        NULL,
        10,
        TRUE
    );

-- Populate sport_period_definition table for Pickleball
INSERT INTO
    biosteel.sport_period_definition (
        sport_period_definition_id,
        sport_type_code,
        period_type_code,
        period_number,
        period_name,
        default_duration_minutes,
        display_order,
        is_scoring_period
    )
VALUES
    (
        gen_random_uuid (),
        'PICKLEBALL',
        'REGULAR',
        1,
        '1st Set',
        NULL,
        1,
        TRUE
    ),
    (
        gen_random_uuid (),
        'PICKLEBALL',
        'REGULAR',
        2,
        '2nd Set',
        NULL,
        2,
        TRUE
    ),
    (
        gen_random_uuid (),
        'PICKLEBALL',
        'REGULAR',
        3,
        '3rd Set',
        NULL,
        3,
        TRUE
    );