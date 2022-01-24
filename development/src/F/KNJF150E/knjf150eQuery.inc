<?php

require_once('for_php7.php');

class knjf150eQuery extends Query {
    //学期一覧取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER   <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //直近データ
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SEQNO, ";
        $query .= "     DATE(T1.FIRST_DATE) AS FIRST_DATE1, ";
        $query .= "     TIME(T1.FIRST_DATE) AS FIRST_DATE2 ";
        $query .= " FROM ";
        $query .= "     ATTEND_BATCH_INPUT_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ";
        $query .= "          ON T2.YEAR     = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = '".$model->semester."' ";
        $query .= "         AND T1.FROM_DATE BETWEEN T2.SDATE AND T2.EDATE  ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEQNO DESC ";
        $query .= " FETCH FIRST 50 ROWS ONLY ";

        return $query;
    }

    //１レコード取得
    function getListRow($model) {
        $query  = " SELECT ";
        $query .= "     *";
        $query .= " FROM ";
        $query .= "     ATTEND_BATCH_INPUT_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SEQNO   =  ".$model->field["SEQNO"]." ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($model, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "C001") {
            $array = array();
            if ($model->Properties["useVirus"] == "true") $array[] = '19';
            if ($model->Properties["useKoudome"] == "true") $array[] = '25';
            $query .= "     AND NAMECD2 IN ('".implode($array,"','")."') ";
        } else if ($namecd1 == "C900") {
            $query .= "     AND NAMECD2 NOT LIKE '2__'";
        }
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年一覧取得
    function getGrade($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL, ";
        if ($model->field["SEQNO"]) {
            $query .= "     CASE WHEN L1.SEQNO IS NOT NULL THEN 1 ELSE 0 END AS FLG ";
        } else {
            $query .= "     0 AS FLG ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        if ($model->field["SEQNO"]) {
            $query .= "     LEFT JOIN ATTEND_BATCH_INPUT_HR_DAT L1 ";
            $query .= "          ON L1.YEAR     = T1.YEAR ";
            $query .= "         AND L1.GRADE    = T1.GRADE ";
            $query .= "         AND L1.SEQNO    = ".$model->field["SEQNO"]." ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] != "1" && $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getHr($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        if ($model->field["SEQNO"]) {
            $query .= "     CASE WHEN L1.SEQNO IS NOT NULL THEN 1 ELSE 0 END AS FLG ";
        } else {
            $query .= "     0 AS FLG ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] != "1" && $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = T1.YEAR AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        if ($model->field["SEQNO"]) {
            $query .= "     LEFT JOIN ATTEND_BATCH_INPUT_HR_DAT L1 ";
            $query .= "          ON L1.YEAR     = T1.YEAR ";
            $query .= "         AND L1.GRADE    = T1.GRADE ";
            $query .= "         AND L1.HR_CLASS = T1.HR_CLASS ";
            $query .= "         AND L1.SEQNO    = ".$model->field["SEQNO"]." ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //時間割一覧取得
    function getSchChrDat($model) {
        $from_date = str_replace("/","-",$model->field["FROM_DATE"]);
        $to_date = str_replace("/","-",$model->field["TO_DATE"]);

        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T3.* ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         CHAIR_STD_DAT T2, ";
        $query .= "         SCH_CHR_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.CHAIRCD  = T3.CHAIRCD AND ";
        $query .= "         (T2.APPDATE BETWEEN '".$from_date."' AND '".$to_date."' OR T2.APPENDDATE BETWEEN '".$from_date."' AND '".$to_date."' OR '".$from_date."' BETWEEN T2.APPDATE AND T2.APPENDDATE) AND ";
        $query .= "         T3.EXECUTEDATE BETWEEN '".$from_date."' AND '".$to_date."' AND ";
        if ($model->field["INPUT_TYPE"] == '3') {
            $query .= "         T1.GRADE || T1.HR_CLASS IN ('".implode($model->selectdata,"','")."') ";
        } else {
            $query .= "         T1.GRADE IN ('".implode($model->selectdata,"','")."') ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        if ($model->field["FROM_DATE"] == $model->field["TO_DATE"]) {
            $query .= "     PERIODCD BETWEEN '".$model->field["FROM_PERIOD"]."' AND '".$model->field["TO_PERIOD"]."' ";
        } else {
            $query .= "     CASE EXECUTEDATE WHEN '".$from_date."' ";
            $query .= "                      THEN (PERIODCD BETWEEN '".$model->field["FROM_PERIOD"]."' AND '".$model->e_period."') ";
            $query .= "                      WHEN '".$to_date."' ";
            $query .= "                      THEN (PERIODCD BETWEEN '".$model->s_period."' AND '".$model->field["TO_PERIOD"]."') END ";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= " WHERE ";
            $query .= "     EXECUTEDATE NOT IN ('".$from_date."', '".$to_date."')";
        }

        $db = Query::dbCheckOut();
        $rtnArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rtnArray[] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        return $rtnArray;
    }

    //ATTEND_DAY_DAT一覧取得
    function getAttendDayDat($model) {
        $from = date_create(str_replace("-", "/", $model->field["FROM_DATE"]));
        $to = date_create(str_replace("-", "/", $model->field["TO_DATE"]));
        $_1day = date_interval_create_from_date_string("1 day");

        $dates = array();
        $i = 0;
        for ($d = $from; $d <= $to;) {
            $dates[] = " ('".str_replace("/", "-", date_format($d, 'Y-m-d'))."') ";
            date_add($d, $_1day);
        }

        $query  = " WITH DATES(EXECUTEDATE) AS (VALUES ";
        $query .= implode(", ", $dates);
        $query .= " ) ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T3.EXECUTEDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         DATES T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".$model->semester."' AND ";
        if ($model->field["INPUT_TYPE"] == '3') {
            $query .= "         T1.GRADE || T1.HR_CLASS IN ('".implode($model->selectdata,"','")."') ";
        } else {
            $query .= "         T1.GRADE IN ('".implode($model->selectdata,"','")."') ";
        }

        $db = Query::dbCheckOut();
        $rtnArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rtnArray[] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        return $rtnArray;
    }

    //MAX値取得
    function getMaxSeqno() {
        $query  = " SELECT ";
        $query .= "     MAX(SEQNO) ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SEQNO ";
        $query .= "     FROM ";
        $query .= "         ATTEND_BATCH_INPUT_HDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         SEQNO ";
        $query .= "     FROM ";
        $query .= "         ATTEND_BATCH_INPUT_HR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     ) ";

        $db = Query::dbCheckOut();
        $max_seqno = $db->getOne($query);
        Query::dbCheckIn($db);

        return $max_seqno;
    }

    //年組取得
    function getGradeHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        if ($model->field["INPUT_TYPE"] == '3') {
            $query .= "     GRADE || HR_CLASS IN ('".implode($model->selectdata,"','")."') ";
        } else {
            $query .= "     GRADE IN ('".implode($model->selectdata,"','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";

        return $query;
    }

    //追加
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //INSERT -- ATTEND_BATCH_INPUT_HDAT
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQNO"][NUMBER]          = $model->field["SEQNO"];
        $data["INPUT_TYPE"][TEXT]       = $model->field["INPUT_TYPE"];
        $data["FIRST_DATE"][NUMBER]     = "SYSDATE()";
        $data["FIRST_REGISTER"][TEXT]   = STAFFCD;
        $data["FROM_DATE"][TEXT]        = str_replace("/","-",$model->field["FROM_DATE"]);
        $data["FROM_PERIOD"][TEXT]      = $model->field["FROM_PERIOD"];
        $data["TO_DATE"][TEXT]          = str_replace("/","-",$model->field["TO_DATE"]);
        $data["TO_PERIOD"][TEXT]        = $model->field["TO_PERIOD"];
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];
        $data["DI_REMARK_CD"][TEXT]     = $model->field["DI_REMARK_CD"];
        $data["DI_REMARK"][TEXT]        = $model->field["DI_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";
        $query = Query::insertSQL($data, "ATTEND_BATCH_INPUT_HDAT");
        $db->query($query);

        //INSERT -- ATTEND_BATCH_INPUT_HR_DAT
        $result = $db->query(knjf150eQuery::getGradeHrClass($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEQNO"][NUMBER]      = $model->field["SEQNO"];
            $data["GRADE"][TEXT]        = $row["GRADE"];
            $data["HR_CLASS"][TEXT]     = $row["HR_CLASS"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";
            $query = Query::insertSQL($data, "ATTEND_BATCH_INPUT_HR_DAT");
            $db->query($query);
        }
        $result->free();

        if ($model->Properties["hibiNyuuryoku"] != 'ATTEND_DAY_DAT') {
            //DELETE -- ATTEND_DAT
            knjf150eQuery::deleteAttendDat($db, $model, $model->field, $model->selectdata);

            //INSERT -- ATTEND_DAT
            foreach ($model->array as $key => $val) {
                knjf150eQuery::insertAttendDat($db, $model, $val);
            }
        } else {
            //DELETE -- ATTEND_DAY_DAT
            knjf150eQuery::deleteAttendDayDat($db, $model, $model->field, $model->selectdata);

            //INSERT -- ATTEND_DAY_DAT
            foreach ($model->array as $key => $val) {
                knjf150eQuery::insertAttendDayDat($db, $model, $val);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //削除
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データ取得 -- ATTEND_BATCH_INPUT_HDAT
        $Row = $db->getRow(knjf150eQuery::getListRow($model), DB_FETCHMODE_ASSOC);

        //データ取得 -- ATTEND_BATCH_INPUT_HDAT
        $query  = " SELECT ";
        if ($Row["INPUT_TYPE"] == '3') {
            $query .= "     GRADE || HR_CLASS AS VALUE ";
        } else {
            $query .= "     GRADE AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     ATTEND_BATCH_INPUT_HR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SEQNO   = ".$model->field["SEQNO"]." ";
        $selectdata = $db->getCol($query);

        //DELETE -- ATTEND_BATCH_INPUT_HDAT
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_BATCH_INPUT_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SEQNO   = ".$model->field["SEQNO"]." ";
        $db->query($query);

        //DELETE -- ATTEND_BATCH_INPUT_HR_DAT
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_BATCH_INPUT_HR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SEQNO   = ".$model->field["SEQNO"]." ";
        $db->query($query);

        if ($model->Properties["hibiNyuuryoku"] != 'ATTEND_DAY_DAT') {
            //DELETE -- ATTEND_DAT
            knjf150eQuery::deleteAttendDat($db, $model, $Row, $selectdata);
        } else {
            //DELETE -- ATTEND_DAY_DAT
            knjf150eQuery::deleteAttendDayDat($db, $model, $Row, $selectdata);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //追加(出欠データ)
    function insertAttendDat($db, $model, $val) {
        $data = array();
        $data["SCHREGNO"][TEXT]         = $val["SCHREGNO"];
        $data["ATTENDDATE"][TEXT]       = $val["EXECUTEDATE"];
        $data["PERIODCD"][TEXT]         = $val["PERIODCD"];
        $data["CHAIRCD"][TEXT]          = $val["CHAIRCD"];
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];
        $data["DI_REMARK_CD"][TEXT]     = $model->field["DI_REMARK_CD"];
        $data["DI_REMARK"][TEXT]        = $model->field["DI_REMARK"];
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "ATTEND_DAT");
        $db->query($query);

        return;
    }

    //削除（出欠データ）
    function deleteAttendDat($db, $model, $fields, $selectdata) {
        $from_date = str_replace("/","-",$fields["FROM_DATE"]);
        $to_date = str_replace("/","-",$fields["TO_DATE"]);

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     (SCHREGNO, ATTENDDATE, PERIODCD) ";
        $query .= "         IN (SELECT ";
        $query .= "                 SCHREGNO, ";
        $query .= "                 ATTENDDATE, ";
        $query .= "                 PERIODCD ";
        $query .= "             FROM ";
        $query .= "                 (SELECT ";
        $query .= "                     T2.* ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT T1, ";
        $query .= "                     ATTEND_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR     = T2.YEAR AND ";
        $query .= "                     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.SEMESTER = '".$model->semester."' AND ";
        if ($fields["INPUT_TYPE"] == '3') {
            $query .= "                     T1.GRADE || T1.HR_CLASS IN ('".implode($selectdata,"','")."') AND ";
        } else {
            $query .= "                     T1.GRADE IN ('".implode($selectdata,"','")."') AND ";
        }
        $query .= "                     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "                     T2.ATTENDDATE BETWEEN '".$from_date."' AND '".$to_date."' ";
        $query .= "                 ) S1 ";
        $query .= "             WHERE ";
        if ($fields["FROM_DATE"] == $fields["TO_DATE"]) {
            $query .= "                 PERIODCD BETWEEN '".$fields["FROM_PERIOD"]."' AND '".$fields["TO_PERIOD"]."' ";
        } else {
            $query .= "                 CASE ATTENDDATE WHEN '".$from_date."' ";
            $query .= "                                 THEN (PERIODCD BETWEEN '".$fields["FROM_PERIOD"]."' AND '".$model->e_period."') ";
            $query .= "                                 WHEN '".$to_date."' ";
            $query .= "                                 THEN (PERIODCD BETWEEN '".$model->s_period."' AND '".$fields["TO_PERIOD"]."') END ";
            $query .= "             UNION ";
            $query .= "             SELECT ";
            $query .= "                 SCHREGNO, ";
            $query .= "                 ATTENDDATE, ";
            $query .= "                 PERIODCD ";
            $query .= "             FROM ";
            $query .= "                 (SELECT ";
            $query .= "                     T2.* ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_DAT T1, ";
            $query .= "                     ATTEND_DAT T2 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.YEAR     = T2.YEAR AND ";
            $query .= "                     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                     T1.SEMESTER = '".$model->semester."' AND ";
            if ($fields["INPUT_TYPE"] == '3') {
                $query .= "                     T1.GRADE || T1.HR_CLASS IN ('".implode($selectdata,"','")."') AND ";
            } else {
                $query .= "                     T1.GRADE IN ('".implode($selectdata,"','")."') AND ";
            }
            $query .= "                     T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "                     T2.ATTENDDATE BETWEEN '".$from_date."' AND '".$to_date."' ";
            $query .= "                 ) S1 ";
            $query .= "             WHERE ";
            $query .= "                 ATTENDDATE NOT IN ('".$from_date."', '".$to_date."') ";
        }
        $query .= "             ) ";
        $db->query($query);

        return;
    }

    //追加(出欠データ)
    function insertAttendDayDat($db, $model, $val) {
        $data = array();
        $data["SCHREGNO"][TEXT]         = $val["SCHREGNO"];
        $data["ATTENDDATE"][TEXT]       = $val["EXECUTEDATE"];
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];
        $data["DI_REMARK"][TEXT]        = $model->field["DI_REMARK"];
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "ATTEND_DAY_DAT");
        $db->query($query);

        return;
    }

    //削除（出欠データ）
    function deleteAttendDayDat($db, $model, $fields, $selectdata) {
        $from_date = str_replace("/","-",$fields["FROM_DATE"]);
        $to_date = str_replace("/","-",$fields["TO_DATE"]);

        $query  = " DELETE FROM ";
        $query .= "     ATTEND_DAY_DAT ";
        $query .= " WHERE ";
        $query .= "     (SCHREGNO, ATTENDDATE) ";
        $query .= "         IN (SELECT ";
        $query .= "                 SCHREGNO, ";
        $query .= "                 ATTENDDATE ";
        $query .= "             FROM ";
        $query .= "                 (SELECT ";
        $query .= "                     T2.* ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT T1, ";
        $query .= "                     ATTEND_DAY_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR     = T2.YEAR AND ";
        $query .= "                     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                     T1.SEMESTER = '".$model->semester."' AND ";
        if ($fields["INPUT_TYPE"] == '3') {
            $query .= "                     T1.GRADE || T1.HR_CLASS IN ('".implode($selectdata,"','")."') AND ";
        } else {
            $query .= "                     T1.GRADE IN ('".implode($selectdata,"','")."') AND ";
        }
        $query .= "                     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "                     T2.ATTENDDATE BETWEEN '".$from_date."' AND '".$to_date."' ";
        $query .= "                 ) S1 ";
        $query .= "             ) ";
        $db->query($query);

        return;
    }
}
?>
