<?php

require_once('for_php7.php');

class knjl012qQuery extends Query {
    //出身学校コードを取得
    function getNameMstA023() {
        $fin_schoolkind = (SCHOOLKIND == 'J') ? 'P' : 'J';

        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1   = '".$fin_schoolkind."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        $db = Query::dbCheckOut();
        $rtnRow = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験番号範囲取得
    function getEntexamNoDat($year, $applicantdiv, $flg) {
        $query  = " SELECT ";
        if ($flg == "examno") {
            $query .= "     * ";
        } else {
            $query .= "     SUM(INTEGER(EXAMNO_TO) - INTEGER(EXAMNO_FROM) + 1) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' AND ";
        if (SCHOOLKIND != 'J') {
            $query .= "     TESTDIV0        = '9' AND ";
        }
        $query .= "     TESTDIV         = '9' ";
        if ($flg == "examno") {
            $query .= " ORDER BY ";
            $query .= "     RECNO ";
        }

        return $query;
    }

    //生徒人数取得
    function getSchregCnt() {
        $query .= " SELECT ";
        $query .= "     COUNT(T1.SCHREGNO) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "          ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "         AND VALUE(T2.GRD_DIV,'') NOT IN ('2','3','6','7') ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "          ON T1.YEAR         = T3.YEAR ";
        $query .= "         AND T1.GRADE        = T3.GRADE ";
        if (SCHOOLKIND == 'J') {
            $query .= "         AND T3.SCHOOL_KIND  = 'P' ";
            $query .= "         AND T3.GRADE_CD     = '06' ";
        } else {
            $query .= "         AND T3.SCHOOL_KIND  = 'J' ";
            $query .= "         AND T3.GRADE_CD     = '03' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfo($model) {
        $query  = " WITH MAX_ADDR AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), T_ADDR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ZIPCD, ";
        $query .= "         T1.ADDR1, ";
        $query .= "         T1.ADDR2, ";
        $query .= "         T1.TELNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT T1, ";
        $query .= "         MAX_ADDR T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "         T1.ISSUEDATE    = T2.ISSUEDATE ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     TRANSLATE_HK_K(T2.NAME_KANA) AS NAME_KANA, ";
        $query .= "     T2.SEX, ";
        $query .= "     T2.BIRTHDAY, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDR1, ";
        $query .= "     L1.ADDR2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     L2.GUARD_NAME, ";
        $query .= "     TRANSLATE_HK_K(L2.GUARD_KANA) AS GUARD_KANA, ";
        $query .= "     L2.GUARD_ZIPCD, ";
        $query .= "     L2.GUARD_ADDR1, ";
        $query .= "     L2.GUARD_ADDR2, ";
        $query .= "     L2.GUARD_TELNO, ";
        $query .= "     L2.RELATIONSHIP ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "          ON T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "         AND VALUE(T2.GRD_DIV,'') NOT IN ('2','3','6','7') ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "          ON T1.YEAR         = T3.YEAR ";
        $query .= "         AND T1.GRADE        = T3.GRADE ";
        if (SCHOOLKIND == 'J') {
            $query .= "         AND T3.SCHOOL_KIND  = 'P' ";
            $query .= "         AND T3.GRADE_CD     = '06' ";
        } else {
            $query .= "         AND T3.SCHOOL_KIND  = 'J' ";
            $query .= "         AND T3.GRADE_CD     = '03' ";
        }
        $query .= "     LEFT JOIN T_ADDR L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     NAME_KANA, ";
        $query .= "     T2.SEX, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //元号取得
    function getEracd($birthday, $FIELD) {
        $birthday= str_replace("-", "/", $birthday);

        $query  = " SELECT ";
        $query .=       $FIELD ;
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L007' AND ";
        $query .= "     '".$birthday."' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $tableArray = array("ENTEXAM_APPLICANTBASE_DAT", "ENTEXAM_APPLICANTADDR_DAT", "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        foreach ($tableArray as $table) {
            //DELETE
            $query  = " DELETE FROM ";
            $query .=       $table." T1 ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
            $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
            $query .= "     EXISTS(SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 ENTEXAM_NO_DAT S1 ";
            $query .= "             WHERE ";
            $query .= "                 T1.ENTEXAMYEAR  = S1.ENTEXAMYEAR AND ";
            $query .= "                 T1.APPLICANTDIV = S1.APPLICANTDIV AND ";
            if (SCHOOLKIND != 'J') {
                $query .= "                 S1.TESTDIV0     = '9' AND ";
            }
            $query .= "                 S1.TESTDIV      = '9' AND ";
            $query .= "                 T1.EXAMNO BETWEEN S1.EXAMNO_FROM AND S1.EXAMNO_TO ";
            $query .= "             ) ";
            $db->query($query);
        }

        //受験番号範囲設定からデータ取得
        $examnoArray = array();
        $result = $db->query(knjl012qQuery::getEntexamNoDat($model->ObjYear, $model->applicantdiv, "examno"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($examno = $row["EXAMNO_FROM"]; $examno <= $row["EXAMNO_TO"]; $examno++) {
                $row["EXAMNO"] = $examno;
                $examnoArray[] = $row;
            }
        }

        //和暦の卒業年を取得
        $fs_grdyear = CTRL_YEAR + 1;
        $fs_m       = "03";
        $fs_day     = $fs_grdyear."-".$fs_m."-01";
        $fs_eracd   = $db->getOne(knjl012qQuery::getEracd($fs_day, "NAMECD2"));
        $fs_y       = $fs_grdyear - $db->getOne(knjl012qQuery::getEracd($fs_day, "NAMESPARE1")) + 1;

        //更新
        $counter = 0;
        $result = $db->query(knjl012qQuery::getStudentInfo($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd = $nendo = $birth_y = $birth_m = $birth_d = "";
            if ($row["BIRTHDAY"]) {
                list ($birth_y, $birth_m, $birth_d) = explode('-', $row["BIRTHDAY"]);
                $eracd = $db->getOne(knjl012qQuery::getEracd($row["BIRTHDAY"], "NAMECD2"));
                $nendo = $birth_y - $db->getOne(knjl012qQuery::getEracd($row["BIRTHDAY"], "NAMESPARE1")) + 1;
            }

            //ENTEXAM_APPLICANTBASE_DAT -- INSERT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examnoArray[$counter]["EXAMNO"];
            $data["TESTDIV"][TEXT]          = '9';
            if (SCHOOLKIND != 'J') {
                $data["TESTDIV0"][TEXT]     = '9';
            }
            $data["SHDIV"][TEXT]            = '1';
            $data["DESIREDIV"][TEXT]        = '1';
            $data["NAME"][TEXT]             = $row["NAME"];
            $data["NAME_KANA"][TEXT]        = $row["NAME_KANA"];
            $data["SEX"][TEXT]              = $row["SEX"];
            $data["ERACD"][TEXT]            = $eracd;
            $data["BIRTH_Y"][TEXT]          = $nendo;
            $data["BIRTH_M"][TEXT]          = $birth_m;
            $data["BIRTH_D"][TEXT]          = $birth_d;
            $data["BIRTHDAY"][TEXT]         = $row["BIRTHDAY"];
            $data["FS_CD"][TEXT]            = $model->fsCd;
            if (SCHOOLKIND != 'J') {
                $data["SLIDE_FLG"][TEXT]    = $examnoArray[$counter]["REMARK_DIV"];
                $data["FS_ERACD"][TEXT]     = $fs_eracd;
                $data["FS_Y"][TEXT]         = $fs_y;
                $data["FS_M"][TEXT]         = $fs_m;
                $data["FS_GRDDIV"][TEXT]    = "1"; //1:見込み
                $data["FS_GRDYEAR"][TEXT]   = $fs_grdyear;
                $data["FS_DAY"][TEXT]       = $fs_day;
            }
            $data["SUC_COURSECD"][TEXT]     = $examnoArray[$counter]["COURSECD"];
            $data["SUC_MAJORCD"][TEXT]      = $examnoArray[$counter]["MAJORCD"];
            $data["SUC_COURSECODE"][TEXT]   = $examnoArray[$counter]["EXAMCOURSECD"];
            $data["JUDGEMENT"][TEXT]        = "1"; //1:合格
            $data["PROCEDUREDIV"][TEXT]     = "1"; //1:済み
            if (SCHOOLKIND == 'J') {
                $data["ENTDIV"][TEXT]       = "1"; //1:済み
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT -- INSERT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examnoArray[$counter]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $row["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $row["ADDR1"];
            $data["ADDRESS2"][TEXT]         = $row["ADDR2"];
            $data["TELNO"][TEXT]            = $row["TELNO"];
            //保護者
            $data["GNAME"][TEXT]            = $row["GUARD_NAME"];
            $data["GKANA"][TEXT]            = $row["GUARD_KANA"];
            $data["GZIPCD"][TEXT]           = $row["GUARD_ZIPCD"];
            $data["GADDRESS1"][TEXT]        = $row["GUARD_ADDR1"];
            $data["GADDRESS2"][TEXT]        = $row["GUARD_ADDR2"];
            $data["GTELNO"][TEXT]           = $row["GUARD_TELNO"];
            $data["RELATIONSHIP"][TEXT]     = $row["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(001) -- INSERT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examnoArray[$counter]["EXAMNO"];
            $data["SEQ"][TEXT]              = '001';
            $data["REMARK8"][TEXT]          = $examnoArray[$counter]["COURSECD"];
            $data["REMARK9"][TEXT]          = $examnoArray[$counter]["MAJORCD"];
            $data["REMARK10"][TEXT]         = $examnoArray[$counter]["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);

            if (SCHOOLKIND != 'J') {
                //ENTEXAM_APPLICANTBASE_DETAIL_DAT(024) -- INSERT
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examnoArray[$counter]["EXAMNO"];
                $data["SEQ"][TEXT]              = '024';
                $data["REMARK1"][TEXT]          = $examnoArray[$counter]["EXAMHALLCD"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                $db->query($query);
            }

            $counter++;
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
