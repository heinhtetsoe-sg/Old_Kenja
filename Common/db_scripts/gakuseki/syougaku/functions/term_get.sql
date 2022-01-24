drop function term_get

CREATE FUNCTION TERM_GET(in_month varchar(2), in_charcode varchar(1)) \
 RETURNS Varchar(10) \
 LANGUAGE SQL \
 BEGIN ATOMIC \
 DECLARE wk_charcode varchar(1) default ''; \
 DECLARE wk_semes_num varchar(1) default ''; \
 DECLARE wk_arg_chk smallint default 0; \
 \
	--引数チェック---------------------------------- \
	SET wk_arg_chk = SMALLINT(in_month); \
	IF wk_arg_chk < 1 OR wk_arg_chk > 12 THEN \
		RETURN NULL; \
	END IF; \
	SET wk_charcode = UPPER(in_charcode); \
	IF wk_charcode != 'C' OR wk_charcode != 'N' THEN \
		RETURN NULL; \
	END IF; \
 \
	--学期判定-------------------------------------- \
        SET wk_semes_num = (SELECT SUBSTR(ctrl_cd2,4,1) FROM control_mst \
                             WHERE ctrl_cd1 = 'B201' \
                               AND ctrl_cd2 in ('1101','1102','1103') \
                               AND in_month between ctrl_char1 and ctrl_char2); \ 	
 \
        --学期外の月の場合--------------------------- \
        IF wk_semes_num IS NULL AND wk_charcode = 'N' THEN \
                RETURN '0'; \
        ELSEIF wk_semes_num IS NULL AND wk_charcode = 'C' THEN \
                RETURN '未設定'; \
        END IF; \
 \ 
	IF wk_charcode = 'N' THEN \
		RETURN wk_semes_num; \
	ELSEIF wk_charcode = 'C' THEN \
		IF wk_semes_num = '1' THEN \
			RETURN SELECT ctrl_char1 FROM control_mst WHERE ctrl_cd1 = 'Z002' AND ctrl_cd2 = '0001'; \
		ELSEIF wk_semes_num = '2' THEN \
			RETURN SELECT ctrl_char2 FROM control_mst WHERE ctrl_cd1 = 'Z002' AND ctrl_cd2 = '0001'; \
		ELSEIF wk_semes_num = '3' THEN \
			RETURN SELECT ctrl_char3 FROM control_mst WHERE ctrl_cd1 = 'Z002' AND ctrl_cd2 = '0001'; \
		END IF; \
	END IF; \
END
