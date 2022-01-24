<?php

require_once('for_php7.php');
class knjl536fQuery extends Query {
    //入試年度一覧取得
    function selectYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model) {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '{$model->year}' ";

        return $query;
    }

    //名称マスタから取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 AS DEFAULT, ";
        $query .= "     ABBV2 AS KIKOKU_FLG ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $namecd1Testdiv = ($model->applicantdiv == "2") ? "L004" : "L024";

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     T1.EXAMNO_FROM || ' ～ ' || T1.EXAMNO_TO AS EXAMNO_FROM_TO, ";
        $query .= "     T1.TESTDIV0 || ':' || N1.NAME1 AS TESTDIV0_NAME, ";
        $query .= "     T1.TESTDIV || ':' || N2.NAME1 AS TESTDIV_NAME, ";
        $query .= "     T1.EXAMCOURSECD || ':' || L1.EXAMCOURSE_NAME AS EXAMCOURSE_NAME, ";
        $query .= "     T1.REMARK_DIV || ':' || N3.NAME1 AS REMARK_DIV_NAME, ";
        $query .= "     T1.MEMO_KISO || ':' || N4.NAME1 AS MEMO_KISO_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "          ON L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.COURSECD     = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD      = T1.MAJORCD ";
        $query .= "         AND L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.NAMECD1      = 'L034' ";
        $query .= "         AND N1.NAMECD2      = T1.TESTDIV0 ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "          ON N2.NAMECD1      = '{$namecd1Testdiv}' ";
        $query .= "         AND N2.NAMECD2      = T1.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ";
        $query .= "          ON N3.NAMECD1      = 'L046' ";
        $query .= "         AND N3.NAMECD2      = T1.REMARK_DIV ";
        $query .= "     LEFT JOIN V_NAME_MST N4 ";
        $query .= "          ON N4.NAMECD1      = 'L005' ";
        $query .= "         AND N4.NAMECD2      = T1.MEMO_KISO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO_FROM, T1.REMARK_DIV, T1.TESTDIV, T1.TESTDIV0, T1.EXAMCOURSECD ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $recno) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD AS EXAMCOURSE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' AND ";
        $query .= "     RECNO           =  ".$recno." ";

        return $query;
    }

    //コース区分取得
    function getEntexamCourseMst($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= "    ,'' AS DEFAULT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR      = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV     = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV          = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験番号チェック
    function checkExamno($model, $kikokuFlg, $recno, $examno_from, $examno_to="") {
        $namecd1 = ($model->field["APPLICANTDIV"] == "2") ? "L004" : "L024";

        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    ENTEXAM_NO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->field["ENTEXAMYEAR"]."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' ";
        if ($recno) {
            $query .= " AND RECNO       != ".$recno." ";
        }
        if ($examno_to) {
            $query .= " AND (   EXAMNO_FROM BETWEEN '".$examno_from."' AND '".$examno_to."' ";
            $query .= "      OR EXAMNO_TO BETWEEN '".$examno_from."' AND '".$examno_to."') ";
        } else {
            $query .= " AND '".$examno_from."' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        }
        if ($kikokuFlg == "1") {
            $query .= " AND NOT EXISTS ( ";
            $query .= "     SELECT ";
            $query .= "        'X' ";
            $query .= "     FROM ";
            $query .= "        ENTEXAM_NO_DAT W1 ";
            $query .= "     WHERE ";
            $query .= "         W1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "         AND W1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "         AND W1.RECNO = T1.RECNO ";
            $query .= "         AND W1.TESTDIV IN (SELECT NAMECD2 FROM V_NAME_MST WHERE NAMECD1 = '{$namecd1}' AND ABBV2 = '{$kikokuFlg}') ";
            $query .= " ) ";
        }

        return $query;
    }

    //受験コースチェック
    function checkCourse($model, $recno, $examcourse) {
        $namecd1 = ($model->field["APPLICANTDIV"] == "2") ? "L004" : "L024";

        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    ENTEXAM_NO_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->field["ENTEXAMYEAR"]."' ";
        $query .= "     AND APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' ";
        if ($recno) {
            $query .= " AND RECNO       != ".$recno." ";
        }
        $query .= " AND TESTDIV         = '".$model->field["TESTDIV"]."' ";
        $query .= " AND MEMO_KISO       = '".$model->field["MEMO_KISO"]."' ";
        if ($examcourse) {
            $query .= " AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '".$examcourse."' ";
        } else {
            $query .= " AND (COURSECD IS NULL OR MAJORCD IS NULL OR EXAMCOURSECD IS NULL) ";
        }
        return $query;
    }

    //MAX自動発番取得
    function getMaxRecno($year, $applicantdiv) {
        $query  = " SELECT ";
        $query .= "     MAX(RECNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $maxRecno = $db->getOne(knjl536fQuery::getMaxRecno($fields["ENTEXAMYEAR"], $fields["APPLICANTDIV"]));
        $setRecno = ($maxRecno > 0) ? $maxRecno + 1 : 1;

        list ($coursecd, $majorcd, $examcoursecd) = explode('-', $fields["EXAMCOURSE"]);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["RECNO"][NUMBER]          = $setRecno;
        $data["EXAMNO_FROM"][TEXT]      = $fields["EXAMNO_FROM"];
        $data["EXAMNO_TO"][TEXT]        = $fields["EXAMNO_TO"];
        $data["TESTDIV0"][TEXT]         = $fields["TESTDIV0"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]         = $coursecd;
        $data["MAJORCD"][TEXT]          = $majorcd;
        $data["EXAMCOURSECD"][TEXT]     = $examcoursecd;
        $data["REMARK_DIV"][TEXT]       = $fields["REMARK_DIV"];
        $data["MEMO_KISO"][TEXT]        = $fields["MEMO_KISO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_NO_DAT");

        $db->query($query);
        Query::dbCheckIn($db);
        return $setRecno;
    }

    //UPDATE
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();

        list ($coursecd, $majorcd, $examcoursecd) = explode('-', $fields["EXAMCOURSE"]);

        $data = array();
        $data["EXAMNO_FROM"][TEXT]      = $fields["EXAMNO_FROM"];
        $data["EXAMNO_TO"][TEXT]        = $fields["EXAMNO_TO"];
        $data["TESTDIV0"][TEXT]         = $fields["TESTDIV0"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]         = $coursecd;
        $data["MAJORCD"][TEXT]          = $majorcd;
        $data["EXAMCOURSECD"][TEXT]     = $examcoursecd;
        $data["REMARK_DIV"][TEXT]       = $fields["REMARK_DIV"];
        $data["MEMO_KISO"][TEXT]        = $fields["MEMO_KISO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."' AND ";
        $where .= "     APPLICANTDIV    = '".$fields["APPLICANTDIV"]."' AND ";
        $where .= "     RECNO           =  ".$fields["RECNO"]." ";

        $query = Query::updateSQL($data, "ENTEXAM_NO_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."' AND ";
        $query .= "     APPLICANTDIV    = '".$fields["APPLICANTDIV"]."' AND ";
        $query .= "     RECNO           =  ".$fields["RECNO"]." ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //年度にデータが存在するのかチェック
    function checkEntexamNoDat($year, $applicantdiv) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' ";

        return $query;
    }

    //COPY
    function getCopyQuery($thisYear, $nextYear, $applicantdiv) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     RECNO, ";
        $query .= "     EXAMNO_FROM, ";
        $query .= "     EXAMNO_TO, ";
        $query .= "     TESTDIV0, ";
        $query .= "     TESTDIV, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     REMARK_DIV, ";
        $query .= "     MEMO_KISO, ";
        $query .= "     MEMO_HIKKI, ";
        $query .= "     MEMO_MENSETU, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$thisYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' ";

        return $query;
    }
}
?>
