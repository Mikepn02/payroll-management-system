--
-- PostgreSQL database dump
--

-- Dumped from database version 17.1
-- Dumped by pg_dump version 17.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: notify_on_payslip_approval(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.notify_on_payslip_approval() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.notify_on_payslip_approval() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: deductions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.deductions (
    id uuid NOT NULL,
    created_by uuid,
    created_date timestamp(6) without time zone,
    last_modified_by uuid,
    last_modified_date timestamp(6) without time zone,
    code character varying(255) NOT NULL,
    deduction_name character varying(255) NOT NULL,
    percentage numeric(5,2)
);


ALTER TABLE public.deductions OWNER TO postgres;

--
-- Name: employees; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.employees (
    id uuid NOT NULL,
    created_by uuid,
    created_date timestamp(6) without time zone,
    last_modified_by uuid,
    last_modified_date timestamp(6) without time zone,
    code character varying(255) NOT NULL,
    status character varying(255),
    profile_id uuid,
    CONSTRAINT employees_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'DISABLED'::character varying])::text[])))
);


ALTER TABLE public.employees OWNER TO postgres;

--
-- Name: employees_notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.employees_notifications (
    employee_id uuid NOT NULL,
    notifications_id uuid NOT NULL
);


ALTER TABLE public.employees_notifications OWNER TO postgres;

--
-- Name: employments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.employments (
    id uuid NOT NULL,
    created_by uuid,
    created_date timestamp(6) without time zone,
    last_modified_by uuid,
    last_modified_date timestamp(6) without time zone,
    base_salary numeric(38,2),
    code character varying(255) NOT NULL,
    department character varying(255),
    joining_date date,
    "position" character varying(255),
    status character varying(255),
    employee_id uuid,
    CONSTRAINT employments_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
);


ALTER TABLE public.employments OWNER TO postgres;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO postgres;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    created_by uuid,
    created_date timestamp(6) without time zone,
    last_modified_by uuid,
    last_modified_date timestamp(6) without time zone,
    email_sent boolean,
    message_content character varying(1000),
    month integer,
    sent_at timestamp(6) without time zone,
    status character varying(255),
    year integer,
    employee_id uuid NOT NULL,
    payslip_id uuid,
    CONSTRAINT notifications_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying])::text[])))
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- Name: payslips; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.payslips (
    id uuid NOT NULL,
    created_by uuid,
    created_date timestamp(6) without time zone,
    last_modified_by uuid,
    last_modified_date timestamp(6) without time zone,
    approved_at timestamp(6) without time zone,
    approved_by character varying(255),
    base_salary numeric(15,2),
    employee_tax_amount numeric(15,2),
    gross_salary numeric(15,2),
    house_amount numeric(15,2),
    medical_insurance_amount numeric(15,2),
    month integer,
    net_salary numeric(15,2),
    other_tax_amount numeric(15,2),
    pension_amount numeric(15,2),
    status character varying(255) NOT NULL,
    transport_amount numeric(15,2),
    year integer,
    employee_id uuid NOT NULL,
    CONSTRAINT payslips_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PAID'::character varying])::text[])))
);


ALTER TABLE public.payslips OWNER TO postgres;

--
-- Name: payslips_notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.payslips_notifications (
    payslip_id uuid NOT NULL,
    notifications_id uuid NOT NULL
);


ALTER TABLE public.payslips_notifications OWNER TO postgres;

--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id uuid NOT NULL,
    name character varying(255),
    CONSTRAINT roles_name_check CHECK (((name)::text = ANY ((ARRAY['ADMIN'::character varying, 'MANAGER'::character varying, 'EMPLOYEE'::character varying])::text[])))
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_by uuid,
    created_date timestamp(6) without time zone,
    last_modified_by uuid,
    last_modified_date timestamp(6) without time zone,
    date_of_birth date,
    first_name character varying(255),
    last_name character varying(255),
    phone_number character varying(255),
    email character varying(255),
    is_verified boolean NOT NULL,
    password character varying(255),
    password_reset_code character varying(255),
    password_reset_code_generated_at timestamp(6) without time zone,
    verification_code character varying(255),
    verification_code_generated_at timestamp(6) without time zone
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Data for Name: deductions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.deductions (id, created_by, created_date, last_modified_by, last_modified_date, code, deduction_name, percentage) FROM stdin;
461d583d-46f0-48d7-98c6-0bb3aef448a7	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:18:30.721785	\N	\N	DED-20250529-E92436	EmployeeTax	30.00
d434670a-1ee0-41ff-a266-386b8667829e	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:18:44.62152	\N	\N	DED-20250529-17E386	Pension	6.00
4af70ebd-4221-4700-9cc6-40026a79fa78	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:19:38.851507	\N	\N	DED-20250529-51089A	Others	5.00
1e67ca8e-8443-40ad-88c1-c7296443accc	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:19:49.217103	\N	\N	DED-20250529-D16E97	Housing	14.00
4f7dd283-275c-464b-b65c-ed5826173c1e	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:20:00.190095	\N	\N	DED-20250529-A49847	Transport	14.00
01059950-0fc1-4524-99c5-97c3d7fb939b	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:19:19.713043	\N	\N	DED-20250529-CF9CF5	MedicalInsurance	5.00
\.


--
-- Data for Name: employees; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.employees (id, created_by, created_date, last_modified_by, last_modified_date, code, status, profile_id) FROM stdin;
f07c6e34-eaaa-41c8-af0e-a3499d28c550	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:20:22.940623	\N	\N	EMP-20250529-F14E03	\N	77583684-90e9-450b-9581-e1b8d0fa55d8
\.


--
-- Data for Name: employees_notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.employees_notifications (employee_id, notifications_id) FROM stdin;
\.


--
-- Data for Name: employments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.employments (id, created_by, created_date, last_modified_by, last_modified_date, base_salary, code, department, joining_date, "position", status, employee_id) FROM stdin;
d9d41fcb-55f3-47da-a95a-d75c7f115c6d	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:20:22.947626	\N	\N	100000.00	EMPLOY-20250529-F1A2DA	IT	2025-05-29	Software Engineer	ACTIVE	f07c6e34-eaaa-41c8-af0e-a3499d28c550
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	<< Flyway Baseline >>	BASELINE	<< Flyway Baseline >>	\N	postgres	2025-05-29 17:08:09.46982	0	t
2	6	create notify trigger	SQL	V6__create_notify_trigger.sql	-2096033213	postgres	2025-05-29 17:08:09.52293	6	t
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notifications (id, created_by, created_date, last_modified_by, last_modified_date, email_sent, message_content, month, sent_at, status, year, employee_id, payslip_id) FROM stdin;
dd5e56eb-d966-440e-8711-573123a48a42	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:28:27.85786	\N	\N	\N	Your salary has been approved.	12	2025-05-29 17:28:27.82452	SENT	2024	f07c6e34-eaaa-41c8-af0e-a3499d28c550	28d35701-9a5e-4620-a905-071153cb29b9
3fef63af-795f-418a-bc70-9065a4871b45	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:38:23.335848	\N	\N	\N	Your salary has been approved.	5	2025-05-29 17:38:23.308864	SENT	2025	f07c6e34-eaaa-41c8-af0e-a3499d28c550	dc30264c-b49c-49c4-8f6d-09eb9373c6d6
\.


--
-- Data for Name: payslips; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.payslips (id, created_by, created_date, last_modified_by, last_modified_date, approved_at, approved_by, base_salary, employee_tax_amount, gross_salary, house_amount, medical_insurance_amount, month, net_salary, other_tax_amount, pension_amount, status, transport_amount, year, employee_id) FROM stdin;
28d35701-9a5e-4620-a905-071153cb29b9	10337728-8a63-4fe4-bf9f-08c1d39961dc	2025-05-29 17:21:35.67966	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:28:27.874538	2025-05-29 17:28:21.843321	nzaberamikepeter@gmail.com	100000.00	30000.00	128000.00	14000.00	5000.00	12	82000.00	5000.00	6000.00	PAID	14000.00	2024	f07c6e34-eaaa-41c8-af0e-a3499d28c550
dc30264c-b49c-49c4-8f6d-09eb9373c6d6	10337728-8a63-4fe4-bf9f-08c1d39961dc	2025-05-29 17:32:40.10032	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:38:23.34873	2025-05-29 17:38:18.378206	nzaberamikepeter@gmail.com	100000.00	30000.00	128000.00	14000.00	5000.00	5	82000.00	5000.00	6000.00	PAID	14000.00	2025	f07c6e34-eaaa-41c8-af0e-a3499d28c550
\.


--
-- Data for Name: payslips_notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.payslips_notifications (payslip_id, notifications_id) FROM stdin;
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id, name) FROM stdin;
241e2501-8246-4726-9234-0d36bd2f14d4	EMPLOYEE
b80507a7-7d77-472c-af3a-4e99a7ce10c1	MANAGER
e8942e0e-fece-4711-adfd-a09094fa859f	ADMIN
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_roles (user_id, role_id) FROM stdin;
16d8627a-d2f6-41dc-9128-012325080b5d	e8942e0e-fece-4711-adfd-a09094fa859f
10337728-8a63-4fe4-bf9f-08c1d39961dc	b80507a7-7d77-472c-af3a-4e99a7ce10c1
77583684-90e9-450b-9581-e1b8d0fa55d8	241e2501-8246-4726-9234-0d36bd2f14d4
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, created_by, created_date, last_modified_by, last_modified_date, date_of_birth, first_name, last_name, phone_number, email, is_verified, password, password_reset_code, password_reset_code_generated_at, verification_code, verification_code_generated_at) FROM stdin;
16d8627a-d2f6-41dc-9128-012325080b5d	\N	2025-05-29 17:16:53.150242	\N	\N	\N	Nzabera	Mike	0788671061	nzaberamikepeter@gmail.com	f	$2a$10$QrqRol3SQ.qsZRBUo2kFZuZ1QGYNqb0L6zkDCMoX1bXLGZdShraHC	\N	\N	\N	\N
10337728-8a63-4fe4-bf9f-08c1d39961dc	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:17:53.303999	\N	\N	\N	John	Doe	0788671062	manager@gmail.com	f	$2a$10$Qj/rIJMK6aGLoH0eTKq87erFyZcsMWP4fRcpYa56GsbtpBypINr/6	\N	\N	\N	\N
77583684-90e9-450b-9581-e1b8d0fa55d8	16d8627a-d2f6-41dc-9128-012325080b5d	2025-05-29 17:20:22.928669	\N	\N	1990-01-01	Mukama	Foro	\N	damascene10@gmail.com	f	$2a$10$GvzcviutsamygWQRt/QS9ermU8NQGqRR/CCjHZFOIOpB529owv6KO	\N	\N	\N	\N
\.


--
-- Name: deductions deductions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deductions
    ADD CONSTRAINT deductions_pkey PRIMARY KEY (id);


--
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);


--
-- Name: employments employments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employments
    ADD CONSTRAINT employments_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: payslips payslips_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payslips
    ADD CONSTRAINT payslips_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: employees uk3um79qgwg340lpaw7phtwudtc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT uk3um79qgwg340lpaw7phtwudtc UNIQUE (code);


--
-- Name: employments uk3v0y1vqxyua9lvkiew11mnw1o; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employments
    ADD CONSTRAINT uk3v0y1vqxyua9lvkiew11mnw1o UNIQUE (code);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: users uk9q63snka3mdh91as4io72espi; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk9q63snka3mdh91as4io72espi UNIQUE (phone_number);


--
-- Name: employments ukcpqqfxxkijtphyy2sde67tclx; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employments
    ADD CONSTRAINT ukcpqqfxxkijtphyy2sde67tclx UNIQUE (employee_id);


--
-- Name: deductions ukh8uwnwyducexwvadtyg7f3qes; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.deductions
    ADD CONSTRAINT ukh8uwnwyducexwvadtyg7f3qes UNIQUE (code);


--
-- Name: employees ukix7vms5pmb366ocjb6eq2sj0o; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT ukix7vms5pmb366ocjb6eq2sj0o UNIQUE (profile_id);


--
-- Name: payslips_notifications ukmuhqoxfl858wywx5tfv1fxp1s; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payslips_notifications
    ADD CONSTRAINT ukmuhqoxfl858wywx5tfv1fxp1s UNIQUE (notifications_id);


--
-- Name: employees_notifications ukpa7rjb3qt5aq0x5j1ogck05l3; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employees_notifications
    ADD CONSTRAINT ukpa7rjb3qt5aq0x5j1ogck05l3 UNIQUE (notifications_id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: payslips_notifications fk1sl8s2pkbu49judkwb1nl7n3v; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payslips_notifications
    ADD CONSTRAINT fk1sl8s2pkbu49judkwb1nl7n3v FOREIGN KEY (notifications_id) REFERENCES public.notifications(id);


--
-- Name: employees_notifications fk4icq6r9wm9ajounat96cei5l5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employees_notifications
    ADD CONSTRAINT fk4icq6r9wm9ajounat96cei5l5 FOREIGN KEY (notifications_id) REFERENCES public.notifications(id);


--
-- Name: notifications fkahmaat8ahv73ji76leqbhu1hr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fkahmaat8ahv73ji76leqbhu1hr FOREIGN KEY (payslip_id) REFERENCES public.payslips(id);


--
-- Name: employments fkd0t2nvcnyco5o5kj188l4fsql; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employments
    ADD CONSTRAINT fkd0t2nvcnyco5o5kj188l4fsql FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employees fkduddi0ns1wnlnn8dujqfrf7fa; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT fkduddi0ns1wnlnn8dujqfrf7fa FOREIGN KEY (profile_id) REFERENCES public.users(id);


--
-- Name: payslips_notifications fkfcaxpceh784c964vkutwbc06r; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payslips_notifications
    ADD CONSTRAINT fkfcaxpceh784c964vkutwbc06r FOREIGN KEY (payslip_id) REFERENCES public.payslips(id);


--
-- Name: user_roles fkh8ciramu9cc9q3qcqiv4ue8a6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: user_roles fkhfh9dx7w3ubf1co1vdev94g3f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: payslips fki2u90djkfkqooebb9b26gxqmi; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payslips
    ADD CONSTRAINT fki2u90djkfkqooebb9b26gxqmi FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employees_notifications fkqqlwtn3alqjo2l1h46gag8xy0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employees_notifications
    ADD CONSTRAINT fkqqlwtn3alqjo2l1h46gag8xy0 FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: notifications fkt73pmdga1salintddb9hn6es9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fkt73pmdga1salintddb9hn6es9 FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- PostgreSQL database dump complete
--

