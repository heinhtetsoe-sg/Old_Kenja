<?php

require_once('for_php7.php');

class knjl011qQuery extends Query {
    //実践模試データ取込
    function torikomiShigansya($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     TRANSLATE_HK_H(KANA1) || '　' || TRANSLATE_HK_H(KANA2)AS NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     NAME1 AS NAME, ";
        $query .= "     BIRTHDAY, ";
        $query .= "     ZIPCODE AS ZIPCD, ";
        $query .= "     ADDR1 AS ADDRESS1, ";
        $query .= "     ADDR2 AS ADDRESS2, ";
        $query .= "     TELNO1 AS TELNO, ";
        $query .= "     ZIPCODE AS GZIPCD, ";
        $query .= "     ADDR1 AS GADDRESS1, ";
        $query .= "     ADDR2 AS GADDRESS2, ";
        $query .= "     TELNO1 AS GTELNO, ";
        $query .= "     SCHOOLCD AS FS_CD ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR   = '".CTRL_YEAR."' ";
        $query .= "     AND SAT_NO = '".$model->field["JIZEN_BANGOU"]."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //受験番号帯の受験区分取得
    function getKubun($model) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.NAME1 AS KUBUN_TESTDIV0, ";
        $query .= "     T3.NAME1 AS KUBUN_TESTDIV, ";
        $query .= "     T4.NAME1 AS KUBUN_REMARK_DIV, ";
        $query .= "     T5.EXAMHALL_NAME AS KUBUN_EXAMHALLCD, ";
        $query .= "     T6.EXAMCOURSE_NAME AS KUBUN_EXAMCOURSE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'L045' AND T2.NAMECD2 = T1.TESTDIV0 ";
        $query .= "     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = '{$model->nameMstTest}' AND T3.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'L046' AND T4.NAMECD2 = T1.REMARK_DIV ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T5.APPLICANTDIV = T1.APPLICANTDIV AND T5.TESTDIV = T1.TESTDIV AND T5.EXAMHALLCD = T1.EXAMHALLCD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST T6 ON T6.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T6.APPLICANTDIV = T1.APPLICANTDIV AND T6.TESTDIV = T1.TESTDIV AND T6.COURSECD = T1.COURSECD AND T6.MAJORCD = T1.MAJORCD AND T6.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND '".$model->examno."' BETWEEN T1.EXAMNO_FROM AND T1.EXAMNO_TO ";
        return $query;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入寮希望(固定)
    function getDormitory() {
        $query  = " WITH T_COMMON(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '1:有') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //スカラー希望(固定)
    function getScholar() {
        $query  = " WITH T_COMMON(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '1:特別') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '2:一般') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //都道府県
    function getPref($model) {
        $query  = " SELECT ";
        $query .= "     PREF_CD AS VALUE, ";
        $query .= "     PREF_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTDIV0, ";
        $query .= "     T1.DAI1_COURSECD || '-' || T1.DAI1_MAJORCD || '-' || T1.DAI1_COURSECODE AS EXAMCOURSE, ";
        $query .= "     T1.SIKEN_KAIJOU_CD1 AS EXAMHALLCD, ";
        $query .= "     T1.SLIDE_FLG AS REMARK_DIV, ";
        $query .= "     T1.GENERAL_FLG, ";
        $query .= "     T1.DORMITORY_FLG, ";
        $query .= "     T1.SCHOLAR_KIBOU, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     T1.SH_SCHOOLCD1, ";
        $query .= "     T1.SH_SCHOOLCD2, ";
        $query .= "     T1.SH_SCHOOLCD3, ";
        $query .= "     T1.SH_SCHOOLCD4, ";
        $query .= "     T1.SH_GAKKAMEI1, ";
        $query .= "     T1.SH_GAKKAMEI2, ";
        $query .= "     T1.SH_GAKKAMEI3, ";
        $query .= "     T1.SH_GAKKAMEI4, ";
        $query .= "     T1.SAT_GROUPCD, ";
        $query .= "     T1.JIZEN_BANGOU, ";
        $query .= "     T1.SIMAI_SCHOOL_KIND1, ";
        $query .= "     T1.SIMAI_NAME1, ";
        $query .= "     T1.SIMAI_SCHOOL_KIND2, ";
        $query .= "     T1.SIMAI_NAME2, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_GRDYEAR, ";
        $query .= "     T1.FS_ERACD, ";
        $query .= "     N1.NAME1 AS FS_WNAME, ";
        $query .= "     T1.FS_Y, ";
        $query .= "     T1.FS_M, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T2.FAMILY_REGISTER, ";
        $query .= "     CONFRPT.ABSENCE_DAYS2 AS ABSENCE2, ";
        $query .= "     CONFRPT.ABSENCE_DAYS3 AS ABSENCE3, ";
        $query .= "     T1.RECOM_EXAMNO, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.GNAME, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.RELATIONSHIP, ";
        $query .= "     T2.GZIPCD, ";
        $query .= "     T2.GADDRESS1, ";
        $query .= "     T2.GADDRESS2, ";
        $query .= "     T2.GTELNO, ";
        $query .= "     T2.GJOB, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ014 ";
        $query .= "          ON SEQ014.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND SEQ014.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND SEQ014.EXAMNO = T1.EXAMNO ";
        $query .= "         AND SEQ014.SEQ = '014' ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ";
        $query .= "          ON T3.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.NAMECD1 = 'L007' ";
        $query .= "         AND T3.NAMECD2 = T1.ERACD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "         ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1 = 'L007' ";
        $query .= "         AND N1.NAMECD2 = T1.FS_ERACD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON T1.ENTEXAMYEAR = CONFRPT.ENTEXAMYEAR ";
        $query .= "          AND T1.APPLICANTDIV = CONFRPT.APPLICANTDIV ";
        $query .= "          AND T1.EXAMNO = CONFRPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query.= " T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd) {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";

        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //団体コード
    function getSatGroup() {
        $query  = " SELECT ";
        $query .= "     GROUPCD || ':' || GROUPNAME AS LABEL, ";
        $query .= "     GROUPCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //存在チェック
    function getExistdataCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(EXAMNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND EXAMNO = '".$model->examno."' ";
        return $query;
    }

    //受験番号帯に含まれているか確認
    function getExistNoCnt($model, $div = "") {
        $query  = " SELECT ";
        if ($div == "1") {
            $query .= "     * ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND '".$model->examno."' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        return $query;
    }

    //追加・更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $kubunRow = $db->getRow(knjl011qQuery::getExistNoCnt($model, "1"), DB_FETCHMODE_ASSOC);

        $result = $db->query(knjl011qQuery::get_calendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        //卒業日の変換(和暦→西暦)
        $fs_date = $fs_grdyear = "";
        if (strlen($model->field["FS_ERACD"]) && strlen($model->field["FS_Y"]) && strlen($model->field["FS_M"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["FS_ERACD"] == $eracd[$i]) {
                    $fs_grdyear = ((int)$defyear[$i]) + ((int)$model->field["FS_Y"] - 1);
                    $fs_date = $fs_grdyear. "-".sprintf("%02d",$model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        //小学入試は固定１をセット
        if (SCHOOLKIND == "P") {
            $model->field["SHDIV"] = "1";
        }
        // 「1:海外入試」の場合、ここではセットしない。
        if ($kubunRow["TESTDIV0"] != "1") {
            $data["SHDIV"][TEXT]         = $model->field["SHDIV"];
        }
        $data["DESIREDIV"][TEXT]     = "1";
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d",$model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d",$model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["DORMITORY_FLG"][TEXT] = $model->field["DORMITORY_FLG"]; //入寮希望 1:有 2:無
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["RECOM_EXAMNO"][TEXT]  = $model->field["RECOM_EXAMNO"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        if ($model->cmd == "add") {
            $data["ENTEXAMYEAR"][TEXT]   = $model->year;
            $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]        = $model->examno;
            //受験番号範囲設定にある項目(追加ボタンを押した時のみセット)
            $data["TESTDIV"][TEXT]       = $kubunRow["TESTDIV"];
            $data["TESTDIV0"][TEXT]      = $kubunRow["TESTDIV0"];
            $data["SLIDE_FLG"][TEXT]     = $kubunRow["REMARK_DIV"]; //備考区分 1:県外SAT B 2:県外SAT A 3:海外
            // 「1:海外入試」の場合、追加の時に固定０をセットする。更新の時はセットしない。
            if ($kubunRow["TESTDIV0"] == "1") {
                $data["SHDIV"][TEXT]         = "0";
            }
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO = '".$model->examno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        if ($model->cmd != "add") {
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "   AND EXAMNO = '".$model->examno."' ";
            $db->query($query);
        }
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["FAMILY_REGISTER"][TEXT]  = $model->field["FAMILY_REGISTER"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]         = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]         = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]            = $model->field["TELNO"];
        $data["GNAME"][TEXT]            = $model->field["GNAME"];
        $data["GKANA"][TEXT]            = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]           = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]        = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]        = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]           = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]     = $model->field["RELATIONSHIP"];
        $data["GJOB"][TEXT]             = $model->field["GJOB"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        if (SCHOOLKIND == "J") {
            //ENTEXAM_APPLICANTCONFRPT_DAT
            //DELETE
            $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "   AND EXAMNO = '".$model->examno."' ";
            $db->query($query);

            //INSERT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["ABSENCE_DAYS2"][NUMBER]  = $model->field["ABSENCE2"];
            $data["ABSENCE_DAYS3"][NUMBER]  = $model->field["ABSENCE3"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            $db->query($query);
        }

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        //cnt
        $getCount001 = $db->getOne(knjl011qQuery::getCountDetail($model, "001"));
        $getCount002 = $db->getOne(knjl011qQuery::getCountDetail($model, "002"));
        $getCount005 = $db->getOne(knjl011qQuery::getCountDetail($model, "005"));
        $getCount014 = $db->getOne(knjl011qQuery::getCountDetail($model, "014"));
        $getCount023 = $db->getOne(knjl011qQuery::getCountDetail($model, "023"));
        $getCount024 = $db->getOne(knjl011qQuery::getCountDetail($model, "024"));
        //where
        $getWhere001 = knjl011qQuery::getWhereDetail($model, "001");
        $getWhere002 = knjl011qQuery::getWhereDetail($model, "002");
        $getWhere005 = knjl011qQuery::getWhereDetail($model, "005");
        $getWhere014 = knjl011qQuery::getWhereDetail($model, "014");
        $getWhere023 = knjl011qQuery::getWhereDetail($model, "023");
        $getWhere024 = knjl011qQuery::getWhereDetail($model, "024");

        //受験番号範囲設定にある項目(追加ボタンを押した時のみセット)
        if ($model->cmd == "add") {
            //第１志望コース
            $data = array();
            $data["REMARK8"][TEXT]          = $kubunRow["COURSECD"];
            $data["REMARK9"][TEXT]          = $kubunRow["MAJORCD"];
            $data["REMARK10"][TEXT]         = $kubunRow["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            if ($getCount001 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = "001";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere001);
            }
            $db->query($query);
        }

        if (SCHOOLKIND != "P") {
            //実践模試受験番号、団体コード
            $data = array();
            $data["REMARK1"][TEXT]          = $model->field["JIZEN_BANGOU"];
            if (SCHOOLKIND == "J") {
                $data["REMARK3"][TEXT]          = $model->field["SAT_GROUPCD"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            if ($getCount002 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = "002";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere002);
            }
            $db->query($query);

            //志望校
            $data = array();
            $data["REMARK1"][TEXT]          = $model->field["SH_SCHOOLCD1"];
            $data["REMARK2"][TEXT]          = $model->field["SH_SCHOOLCD2"];
            $data["REMARK3"][TEXT]          = $model->field["SH_SCHOOLCD3"];
            $data["REMARK4"][TEXT]          = $model->field["SH_SCHOOLCD4"];
            $data["REMARK5"][TEXT]          = $model->field["SH_GAKKAMEI1"];
            $data["REMARK6"][TEXT]          = $model->field["SH_GAKKAMEI2"];
            $data["REMARK7"][TEXT]          = $model->field["SH_GAKKAMEI3"];
            $data["REMARK8"][TEXT]          = $model->field["SH_GAKKAMEI4"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            if ($getCount005 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = "005";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere005);
            }
            $db->query($query);
        }

        if (SCHOOLKIND == "J") {
            //兄弟情報(校種)(年組・氏名)
            $data = array();
            $data["REMARK1"][TEXT]          = strlen($model->field["SIMAI_NAME1"]) ? $model->field["SIMAI_SCHOOL_KIND1"] : "";
            $data["REMARK2"][TEXT]          = strlen($model->field["SIMAI_NAME1"]) ? $model->field["SIMAI_NAME1"] : "";
            $data["REMARK3"][TEXT]          = strlen($model->field["SIMAI_NAME2"]) ? $model->field["SIMAI_SCHOOL_KIND2"] : "";
            $data["REMARK4"][TEXT]          = strlen($model->field["SIMAI_NAME2"]) ? $model->field["SIMAI_NAME2"] : "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            if ($getCount014 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = "014";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere014);
            }
            $db->query($query);
        }

        if (SCHOOLKIND == "H") {
            //スカラー希望 1:特別 2:一般
            $data = array();
            $data["REMARK1"][TEXT]          = $model->field["SCHOLAR_KIBOU"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            if ($getCount023 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = "023";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere023);
            }
            $db->query($query);

            //受験番号範囲設定にある項目(追加ボタンを押した時のみセット)
            if ($model->cmd == "add") {
                //試験会場 1:甲府 2:東京
                $data = array();
                $data["REMARK1"][TEXT]          = $kubunRow["EXAMHALLCD"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                if ($getCount024 == 0) {
                    $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                    $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                    $data["EXAMNO"][TEXT]           = $model->examno;
                    $data["SEQ"][TEXT]              = "024";
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere024);
                }
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function getCountDetail($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    function getWhereDetail($model, $seq) {
        $query  = " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    //--- DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_INTERVIEW_DAT
        $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "            AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "            AND EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>