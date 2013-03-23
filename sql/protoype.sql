--
-- PostgreSQL database dump
--

-- Started on 2013-03-24 01:58:38 MSK

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 1813 (class 1262 OID 74693)
-- Name: arcodb; Type: DATABASE; Schema: -; Owner: arcouser
--

CREATE DATABASE arcodb WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'C' LC_CTYPE = 'C';


ALTER DATABASE arcodb OWNER TO arcouser;

\connect arcodb

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 318 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1510 (class 1259 OID 74752)
-- Dependencies: 3
-- Name: ac_classes; Type: TABLE; Schema: public; Owner: arcouser; Tablespace: 
--

CREATE TABLE ac_classes (
    id integer NOT NULL,
    package integer NOT NULL,
    name text NOT NULL,
    parent text
);


ALTER TABLE public.ac_classes OWNER TO arcouser;

--
-- TOC entry 1509 (class 1259 OID 74750)
-- Dependencies: 3 1510
-- Name: ac_classes_id_seq; Type: SEQUENCE; Schema: public; Owner: arcouser
--

CREATE SEQUENCE ac_classes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.ac_classes_id_seq OWNER TO arcouser;

--
-- TOC entry 1816 (class 0 OID 0)
-- Dependencies: 1509
-- Name: ac_classes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: arcouser
--

ALTER SEQUENCE ac_classes_id_seq OWNED BY ac_classes.id;


--
-- TOC entry 1817 (class 0 OID 0)
-- Dependencies: 1509
-- Name: ac_classes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: arcouser
--

SELECT pg_catalog.setval('ac_classes_id_seq', 1, false);


--
-- TOC entry 1506 (class 1259 OID 74696)
-- Dependencies: 3
-- Name: ac_codebases; Type: TABLE; Schema: public; Owner: arcouser; Tablespace: 
--

CREATE TABLE ac_codebases (
    id integer NOT NULL,
    repository text NOT NULL,
    revision text NOT NULL
);


ALTER TABLE public.ac_codebases OWNER TO arcouser;

--
-- TOC entry 1505 (class 1259 OID 74694)
-- Dependencies: 3 1506
-- Name: ac_codebases_id_seq; Type: SEQUENCE; Schema: public; Owner: arcouser
--

CREATE SEQUENCE ac_codebases_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.ac_codebases_id_seq OWNER TO arcouser;

--
-- TOC entry 1818 (class 0 OID 0)
-- Dependencies: 1505
-- Name: ac_codebases_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: arcouser
--

ALTER SEQUENCE ac_codebases_id_seq OWNED BY ac_codebases.id;


--
-- TOC entry 1819 (class 0 OID 0)
-- Dependencies: 1505
-- Name: ac_codebases_id_seq; Type: SEQUENCE SET; Schema: public; Owner: arcouser
--

SELECT pg_catalog.setval('ac_codebases_id_seq', 1, true);


--
-- TOC entry 1512 (class 1259 OID 74768)
-- Dependencies: 3
-- Name: ac_methods; Type: TABLE; Schema: public; Owner: arcouser; Tablespace: 
--

CREATE TABLE ac_methods (
    id integer NOT NULL,
    class integer NOT NULL,
    name text NOT NULL,
    size integer,
    complexity integer
);


ALTER TABLE public.ac_methods OWNER TO arcouser;

--
-- TOC entry 1511 (class 1259 OID 74766)
-- Dependencies: 1512 3
-- Name: ac_methods_id_seq; Type: SEQUENCE; Schema: public; Owner: arcouser
--

CREATE SEQUENCE ac_methods_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.ac_methods_id_seq OWNER TO arcouser;

--
-- TOC entry 1820 (class 0 OID 0)
-- Dependencies: 1511
-- Name: ac_methods_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: arcouser
--

ALTER SEQUENCE ac_methods_id_seq OWNED BY ac_methods.id;


--
-- TOC entry 1821 (class 0 OID 0)
-- Dependencies: 1511
-- Name: ac_methods_id_seq; Type: SEQUENCE SET; Schema: public; Owner: arcouser
--

SELECT pg_catalog.setval('ac_methods_id_seq', 1, false);


--
-- TOC entry 1508 (class 1259 OID 74709)
-- Dependencies: 3
-- Name: ac_packages; Type: TABLE; Schema: public; Owner: arcouser; Tablespace: 
--

CREATE TABLE ac_packages (
    id integer NOT NULL,
    codebase integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.ac_packages OWNER TO arcouser;

--
-- TOC entry 1507 (class 1259 OID 74707)
-- Dependencies: 3 1508
-- Name: ac_packages_id_seq; Type: SEQUENCE; Schema: public; Owner: arcouser
--

CREATE SEQUENCE ac_packages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.ac_packages_id_seq OWNER TO arcouser;

--
-- TOC entry 1822 (class 0 OID 0)
-- Dependencies: 1507
-- Name: ac_packages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: arcouser
--

ALTER SEQUENCE ac_packages_id_seq OWNED BY ac_packages.id;


--
-- TOC entry 1823 (class 0 OID 0)
-- Dependencies: 1507
-- Name: ac_packages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: arcouser
--

SELECT pg_catalog.setval('ac_packages_id_seq', 7, true);


--
-- TOC entry 1792 (class 2604 OID 74755)
-- Dependencies: 1509 1510 1510
-- Name: id; Type: DEFAULT; Schema: public; Owner: arcouser
--

ALTER TABLE ac_classes ALTER COLUMN id SET DEFAULT nextval('ac_classes_id_seq'::regclass);


--
-- TOC entry 1790 (class 2604 OID 74699)
-- Dependencies: 1505 1506 1506
-- Name: id; Type: DEFAULT; Schema: public; Owner: arcouser
--

ALTER TABLE ac_codebases ALTER COLUMN id SET DEFAULT nextval('ac_codebases_id_seq'::regclass);


--
-- TOC entry 1793 (class 2604 OID 74771)
-- Dependencies: 1511 1512 1512
-- Name: id; Type: DEFAULT; Schema: public; Owner: arcouser
--

ALTER TABLE ac_methods ALTER COLUMN id SET DEFAULT nextval('ac_methods_id_seq'::regclass);


--
-- TOC entry 1791 (class 2604 OID 74712)
-- Dependencies: 1507 1508 1508
-- Name: id; Type: DEFAULT; Schema: public; Owner: arcouser
--

ALTER TABLE ac_packages ALTER COLUMN id SET DEFAULT nextval('ac_packages_id_seq'::regclass);


--
-- TOC entry 1809 (class 0 OID 74752)
-- Dependencies: 1510
-- Data for Name: ac_classes; Type: TABLE DATA; Schema: public; Owner: arcouser
--



--
-- TOC entry 1807 (class 0 OID 74696)
-- Dependencies: 1506
-- Data for Name: ac_codebases; Type: TABLE DATA; Schema: public; Owner: arcouser
--

INSERT INTO ac_codebases (id, repository, revision) VALUES (1, 'dummy_repo', 'dummy_rev');


--
-- TOC entry 1810 (class 0 OID 74768)
-- Dependencies: 1512
-- Data for Name: ac_methods; Type: TABLE DATA; Schema: public; Owner: arcouser
--



--
-- TOC entry 1808 (class 0 OID 74709)
-- Dependencies: 1508
-- Data for Name: ac_packages; Type: TABLE DATA; Schema: public; Owner: arcouser
--



--
-- TOC entry 1801 (class 2606 OID 74760)
-- Dependencies: 1510 1510
-- Name: ac_classes_pkey; Type: CONSTRAINT; Schema: public; Owner: arcouser; Tablespace: 
--

ALTER TABLE ONLY ac_classes
    ADD CONSTRAINT ac_classes_pkey PRIMARY KEY (id);


--
-- TOC entry 1795 (class 2606 OID 74704)
-- Dependencies: 1506 1506
-- Name: ac_codebases_pkey; Type: CONSTRAINT; Schema: public; Owner: arcouser; Tablespace: 
--

ALTER TABLE ONLY ac_codebases
    ADD CONSTRAINT ac_codebases_pkey PRIMARY KEY (id);


--
-- TOC entry 1797 (class 2606 OID 74706)
-- Dependencies: 1506 1506 1506
-- Name: ac_codebases_repository_key; Type: CONSTRAINT; Schema: public; Owner: arcouser; Tablespace: 
--

ALTER TABLE ONLY ac_codebases
    ADD CONSTRAINT ac_codebases_repository_key UNIQUE (repository, revision);


--
-- TOC entry 1803 (class 2606 OID 74776)
-- Dependencies: 1512 1512
-- Name: ac_methods_pkey; Type: CONSTRAINT; Schema: public; Owner: arcouser; Tablespace: 
--

ALTER TABLE ONLY ac_methods
    ADD CONSTRAINT ac_methods_pkey PRIMARY KEY (id);


--
-- TOC entry 1799 (class 2606 OID 74717)
-- Dependencies: 1508 1508
-- Name: ac_packages_pkey; Type: CONSTRAINT; Schema: public; Owner: arcouser; Tablespace: 
--

ALTER TABLE ONLY ac_packages
    ADD CONSTRAINT ac_packages_pkey PRIMARY KEY (id);


--
-- TOC entry 1805 (class 2606 OID 74761)
-- Dependencies: 1798 1508 1510
-- Name: ac_classes_package_fkey; Type: FK CONSTRAINT; Schema: public; Owner: arcouser
--

ALTER TABLE ONLY ac_classes
    ADD CONSTRAINT ac_classes_package_fkey FOREIGN KEY (package) REFERENCES ac_packages(id);


--
-- TOC entry 1806 (class 2606 OID 74777)
-- Dependencies: 1512 1800 1510
-- Name: ac_methods_class_fkey; Type: FK CONSTRAINT; Schema: public; Owner: arcouser
--

ALTER TABLE ONLY ac_methods
    ADD CONSTRAINT ac_methods_class_fkey FOREIGN KEY (class) REFERENCES ac_classes(id);


--
-- TOC entry 1804 (class 2606 OID 74718)
-- Dependencies: 1506 1794 1508
-- Name: ac_packages_codebase_fkey; Type: FK CONSTRAINT; Schema: public; Owner: arcouser
--

ALTER TABLE ONLY ac_packages
    ADD CONSTRAINT ac_packages_codebase_fkey FOREIGN KEY (codebase) REFERENCES ac_codebases(id);


--
-- TOC entry 1815 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2013-03-24 01:58:39 MSK

--
-- PostgreSQL database dump complete
--

