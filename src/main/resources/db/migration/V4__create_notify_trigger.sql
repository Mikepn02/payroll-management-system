CREATE OR REPLACE FUNCTION notify_on_payslip_approval()
RETURNS TRIGGER AS $$
DECLARE
v_message TEXT;
    v_institution TEXT := 'ERP';  -- Replace with actual name
    v_firstname TEXT;
BEGIN
    IF NEW.status = 'PAID' AND OLD.status = 'PENDING' THEN
        -- Join employees to users to get first name
SELECT u.first_name INTO v_firstname
FROM employees e
         JOIN users u ON u.id = e.profile_id
WHERE e.id = NEW.employee_id;

v_message := format(
            'Dear %s, your salary for %s/%s from %s amounting to %s has been credited to your account %s successfully.',
            v_firstname,
            NEW.month,
            NEW.year,
            v_institution,
            NEW.net_salary::TEXT,
            NEW.employee_id::TEXT
        );

INSERT INTO notifications(employee_id, message_content, month, year, status, email_sent, sent_at, payslip_id)
VALUES (NEW.employee_id, v_message, NEW.month, NEW.year, 'PENDING', FALSE, now(), NEW.id);
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;
