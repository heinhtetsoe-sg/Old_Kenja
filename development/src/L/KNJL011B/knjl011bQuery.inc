<?php

require_once('for_php7.php');

class knjl011bQuery extends Query 
{

    //MAX受験番号＋１を取得
    function getMaxExamnoQuery($model)
    {
        $db = Query::dbCheckOut();

        if ($model->field["TESTDIV"] == "1") {
            $setTop = "1";
        } else if ($model->field["TESTDIV1"] == "1") {
            $setTop = "3";
        } else {
            $setTop = "5";
        }

        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND SUBSTR(EXAMNO, 1, 1) = '{$setTop}' ";

        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
            $maxExam++;
        } else {
            $maxExam = $setTop."0001";
        }

        Query::dbCheckIn($db);
        return $maxExam;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        if ($cd == 'L008') {
            $query .= "     ABBV1, ";
            $query .= "     NAMECD2 AS VALUE ";
        } else if ($cd == 'L033') {
            $query .= "     ABBV2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称
    function testDayCheck($ObjYear,$testdivArray)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     TESTDAY AS NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' AND ";
        $query .= "     TESTDAY in (SELECT ";
        $query .= "                         TESTDAY ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_TESTDIV_MST ";
        $query .= "                     WHERE ";
        $query .= "                         ENTEXAMYEAR = '".$ObjYear."' AND ";
        $query .= "                         TESTDIV IN ('" . implode("','", $testdivArray) . "') ";
        $query .= "                     GROUP BY ";
        $query .= "                         TESTDAY ";
        $query .= "                     HAVING ";
        $query .= "                         1 < COUNT(*) ";
        $query .= "                     ) ";
        $query .= " ORDER BY ";
        $query .= "     TESTDIV ";

        $testDayCheck = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $testDayCheck[$row["NAMECD2"]] = $row["NAME1"];
        }

        Query::dbCheckIn($db);
        return $testDayCheck;
    }

    //MAX受験番号＋１を取得
    function getKakuyaku($model, $appDiv, $testDiv)
    {
        $query  = " SELECT ";
        $query .= "     T1.REMARK8 || ':' || L1.PROMISE_COURSE_NAME AS PROMISE_COURSE_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "          AND T1.REMARK8 = L1.JUDGMENT_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.EXAMNO = '{$model->examno}' ";
        $query .= "     AND T1.SEQ = '004' ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTDIV1, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.SPECIAL_REASON_DIV, ";
        $query .= "     T1.SELECT_SUBCLASS_DIV, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_AREA_CD, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
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
        $query .= "     T3.NAME1, ";
        $query .= "     L1.CONFIDENTIAL_RPT01, ";
        $query .= "     L1.CONFIDENTIAL_RPT02, ";
        $query .= "     L1.CONFIDENTIAL_RPT03, ";
        $query .= "     L1.CONFIDENTIAL_RPT04, ";
        $query .= "     L1.CONFIDENTIAL_RPT05, ";
        $query .= "     L1.CONFIDENTIAL_RPT06, ";
        $query .= "     L1.CONFIDENTIAL_RPT07, ";
        $query .= "     L1.CONFIDENTIAL_RPT08, ";
        $query .= "     L1.CONFIDENTIAL_RPT09, ";
        $query .= "     L1.CONFIDENTIAL_RPT10, ";
        $query .= "     L1.TOTAL_ALL, ";
        $query .= "     L1.AVERAGE_ALL, ";
        $query .= "     L1.ABSENCE_DAYS, ";
        $query .= "     L1.ABSENCE_DAYS2, ";
        $query .= "     L1.ABSENCE_DAYS3 ";        
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = T2.EXAMNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_NAME_MST T3 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T3.YEAR AND ";
        $query .= "     T1.ERACD = T3.NAMECD2 AND ";
        $query .= "     T3.NAMECD1 = 'L007' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     FINSCHOOL_MST FIN ";
        $query .= " ON ";
        $query .= "     T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = L1.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = L1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".sprintf("%05d", $model->examno)."'";
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
    function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //データ重複チェック
    function CheckData($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                       T1.EXAMNO,
                       T2.NAME1
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                    LEFT OUTER JOIN
                        V_NAME_MST T2
                    ON
                        T1.ENTEXAMYEAR = T2.YEAR AND
                        T1.APPLICANTDIV = T2.NAMECD2 AND
                        T2.NAMECD1 = 'L003'
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND
                        EXAMNO = '".$model->examno."' AND
                        APPLICANTDIV <> '".$model->applicantdiv."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //存在チェック
    function Existdata($model)
    {

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND     
                        EXAMNO = '".$model->examno."'";

        return $query;
    }

    //該当日付取得
    function CheckDate($db, $model)
    {
        $query = "  SELECT
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$model->year."' AND
                        NAMECD1 = 'L007' AND
                        NAMECD2 = '".$model->field["ERACD"]. "'";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        return $row;
    }

    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["TESTDIV1"][TEXT]      = $model->field["TESTDIV1"];
        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = '1';
        $data["RECEPTDATE"][DATE]    = $model->field["RECEPTDATE"];
        $data["SPECIAL_REASON_DIV"][TEXT]   = $model->field["SPECIAL_REASON_DIV"];
        $data["SELECT_SUBCLASS_DIV"][TEXT]  = $model->field["SELECT_SUBCLASS_DIV"]; //希望コース
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTHDAY"][DATE]      = $model->field["BIRTHDAY"];
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_AREA_CD"][TEXT]    = (strlen($model->field["FS_AREA_CD"]) ? sprintf("%02d",$model->field["FS_AREA_CD"]) : ""); //2005.12.28 minei
        $data["FS_DAY"][DATE]        = $model->field["FS_DAY"];
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["TESTDIV1"][TEXT]      = $model->field["TESTDIV1"];
        $data["SHDIV"][TEXT]         = '1';
        $data["DESIREDIV"][TEXT]     = '1';
        $data["RECEPTDATE"][DATE]    = $model->field["RECEPTDATE"];
        $data["SPECIAL_REASON_DIV"][TEXT]   = $model->field["SPECIAL_REASON_DIV"];
        $data["SELECT_SUBCLASS_DIV"][TEXT]  = $model->field["SELECT_SUBCLASS_DIV"]; //希望コース
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTHDAY"][DATE]      = $model->field["BIRTHDAY"];
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_AREA_CD"][TEXT]    = (strlen($model->field["FS_AREA_CD"]) ? sprintf("%02d",$model->field["FS_AREA_CD"]) : ""); //2005.12.28 minei
        $data["FS_DAY"][DATE]        = $model->field["FS_DAY"];
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       EXAMNO = '".$model->examno."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query.= "       EXAMNO = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT ---INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_JUDGE_TMP
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_DESIRE_DAT
        $query  = "DELETE FROM ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>