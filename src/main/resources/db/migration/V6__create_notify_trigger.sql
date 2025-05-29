CREATE OR REPLACE FUNCTION notify_on_payslip_approval()
RETURNS TRIGGER AS $$
DECLARE
v_message TEXT;
    v_institution TEXT := 'Enterprise Resource Planning';
    v_firstname TEXT := 'Employee';  -- fallback
    v_employee_exists BOOLEAN;
BEGIN
    -- Debug information
    RAISE NOTICE 'Trigger fired for payslip ID: %, Old status: %, New status: %',
                 NEW.id, OLD.status, NEW.status;

    -- Check if this is the correct status change
    IF NEW.status = 'PAID' AND OLD.status = 'PENDING' THEN
        RAISE NOTICE 'Status change detected from PENDING to PAID';

        -- First verify employee exists
SELECT EXISTS(SELECT 1 FROM employees WHERE id = NEW.employee_id) INTO v_employee_exists;

IF NOT v_employee_exists THEN
            RAISE NOTICE 'Employee with ID % not found', NEW.employee_id;
RETURN NEW; -- Exit but allow update
END IF;

        -- Get employee's first name
BEGIN
SELECT u.first_name INTO v_firstname
FROM employees e
         LEFT JOIN users u ON u.id = e.profile_id
WHERE e.id = NEW.employee_id;

RAISE NOTICE 'Retrieved firstname: %', v_firstname;

            IF v_firstname IS NULL THEN
                v_firstname := 'Employee';
                RAISE NOTICE 'Using default firstname: %', v_firstname;
END IF;
EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Error retrieving employee name: %', SQLERRM;
            v_firstname := 'Employee'; -- Fallback on exception
END;

        -- Check for NULL values in message parameters
        IF NEW.month IS NULL OR NEW.year IS NULL OR NEW.net_salary IS NULL THEN
            RAISE NOTICE 'Missing required fields: month: %, year: %, net_salary: %',
                         NEW.month, NEW.year, NEW.net_salary;
RETURN NEW; -- Exit but allow update
END IF;

        -- Format message
        v_message := format(
            'Dear %s, your salary for %s/%s from %s amounting to %s has been credited to your account %s successfully.',
            v_firstname,
            NEW.month,
            NEW.year,
            v_institution,
            COALESCE(NEW.net_salary::TEXT, '0'),
            NEW.employee_id::TEXT
        );

        -- Insert notification
BEGIN
INSERT INTO notifications(
    id, employee_id, message_content, month, year,
    status, email_sent, sent_at, payslip_id
)
VALUES (
           gen_random_uuid(), NEW.employee_id, v_message, NEW.month, NEW.year,
           'PENDING', FALSE, now(), NEW.id
       );

RAISE NOTICE 'Notification inserted successfully for employee: %', NEW.employee_id;
EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Error inserting notification: %', SQLERRM;
            -- Continue despite notification error
END;
ELSE
        RAISE NOTICE 'Skipping trigger: not a PENDING to PAID transition';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;