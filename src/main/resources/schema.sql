CREATE TABLE users(
	id SERIAL PRIMARY KEY,
	username VARCHAR(70) UNIQUE NOT NULL,
	full_name VARCHAR(255)
);

CREATE TABLE timesheet(
	id SERIAL PRIMARY KEY,
	user_id INT NOT NULL,
	project VARCHAR(255),
	task VARCHAR(255),
	hours INT,
	description TEXT,
	timesheet_date TIMESTAMP WITH TIME ZONE,
	updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create a trigger function to automatically update the "updated_at" field whenever a row in "timesheet" table is updated
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger to execute the trigger function
CREATE TRIGGER set_timestamp
BEFORE UPDATE ON timesheet
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();