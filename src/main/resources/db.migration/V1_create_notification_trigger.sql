-- Create the trigger function to insert notification after payslip is approved (status changes from PENDING to PAID)
CREATE OR REPLACE FUNCTION notify_on_payslip_approval()
RETURNS TRIGGER AS $$
DECLARE
v_message TEXT;
    v_institution TEXT := 'Your Institution';  -- Change this to your actual institution name
    v_firstname TEXT;
BEGIN
    IF NEW.status = 'PAID' AND OLD.status = 'PENDING' THEN
SELECT first_name INTO v_firstname FROM employees WHERE id = NEW.employee_id;

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

-- Drop existing trigger if exists (to avoid conflicts on re-run)
DROP TRIGGER IF EXISTS trigger_notify_on_payslip_approval ON payslips;

-- Create the trigger on payslips table AFTER UPDATE
CREATE TRIGGER trigger_notify_on_payslip_approval
    AFTER UPDATE ON payslips
    FOR EACH ROW
    EXECUTE FUNCTION notify_on_payslip_approval();
