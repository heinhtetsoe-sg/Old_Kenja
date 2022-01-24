drop function y2t

CREATE FUNCTION y2t(in_date date, in_mode varchar(1)) \
 RETURNS Varchar(10) \
 LANGUAGE SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE wk_charcode varchar(1) default ''; \
 DECLARE wk_semes_num varchar(1) default ''; \
 DECLARE wk_semes_chr varchar(10) default ''; \
 DECLARE wk_ym integer default 0; \
 \
       set wk_charcode = UPPER(in_mode); \
 \   
       SET (wk_semes_num, wk_semes_chr) = (SELECT semester,semestername FROM semester_mst \
                                            WHERE year     = fiscalyear(in_date) \
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
