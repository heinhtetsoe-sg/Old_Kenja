drop function term_get

CREATE FUNCTION TERM_GET(in_year varchar(4), in_date date, in_mode varchar(1)) \
 RETURNS Varchar(10) \
 LANGUAGE SQL \
 DETERMINISTIC \
 NO EXTERNAL ACTION \ 
 BEGIN ATOMIC \
 DECLARE wk_charcode varchar(1) default ''; \
 DECLARE wk_semes_num varchar(1) default ''; \
 DECLARE wk_semes_chr varchar(10) default ''; \
 DECLARE wk_ym integer default 0; \
 \
    --引数チェック---------------------------------- \
--    IF integer(in_month) NOT BETWEEN 1 AND 12 THEN \
--        RETURN NULL; \
--    END IF; \
    SET wk_charcode = UPPER(in_mode); \
    IF wk_charcode != 'C' AND wk_charcode != 'N' THEN \
        RETURN NULL; \
    END IF; \
 \
 \
    --学期判定-------------------------------------- \
--    IF integer(in_month) BETWEEN 1 AND 3 THEN \
--              SET wk_ym = (integer(in_year) + 1) * 100 + integer(in_month); \
--    ELSE \
--              SET wk_ym =  integer(in_year)      * 100 + integer(in_month); \
--    END IF; \
 \
    SET (wk_semes_num, wk_semes_chr) = (SELECT semester,semestername FROM semester_mst \
                                         WHERE year     = in_year \
                                           AND semester < '9' \
                                           AND in_date BETWEEN sdate AND edate \
                                        ); \     
 \
        --学期外の月の場合--------------------------- \
    IF wk_semes_num IS NULL AND wk_charcode = 'N' THEN \
                RETURN '0'; \
    END IF; \
    IF wk_semes_num IS NULL AND wk_charcode = 'C' THEN \
                   RETURN '未設定'; \
    END IF; \
 \ 
    IF wk_charcode = 'N' THEN \
        RETURN wk_semes_num; \
    ELSEIF wk_charcode = 'C' THEN \
        RETURN wk_semes_chr; \ 
    END IF; \
    RETURN NULL; \
END
