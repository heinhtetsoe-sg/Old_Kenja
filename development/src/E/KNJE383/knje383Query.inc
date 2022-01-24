<?php

require_once('for_php7.php');

class knje383query extends Query {
    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHOOL_KIND AS VALUE, ";
        $query .= "     T2.ABBV1 AS LABEL, ";
        $query .= "     T2.NAMECD2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1, ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND T2.NAMECD1  = 'A023' ";
        $query .= "     AND T2.NAME1    = T1.SCHOOL_KIND ";

        $query .= " ORDER BY ";
        $query .= "     T2.NAMECD2 ";

        return $query;
    }

    //アンケート一覧取得
    function getQuestionnaireList() {
        $query  = " SELECT ";
        $query .= "     T1.QUESTIONNAIRECD AS VALUE, ";
        $query .= "     T1.QUESTIONNAIRECD || '：' || VALUE(T2.QUESTIONNAIRENAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT T1 ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST T2 ON T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度内期間取得
    function getSemesterMst() {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '9' ";

        return $query;
    }

    //履歴一覧取得
    function getListRireki($model) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.ABBV1 AS SCHOOL_KIND_LABEL, ";
        $query .= "     T1.QUESTIONNAIRECD || '：' || VALUE(L2.QUESTIONNAIRENAME,'') AS QUESTIONNAIRECD_LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_EXEC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1  = 'A023' ";
        $query .= "         AND L1.NAME1    = T1.SCHOOL_KIND ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST L2 ";
        $query .= "          ON L2.QUESTIONNAIRECD = T1.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC ";

        return $query;
    }

    //実行処理
    function ExecuteQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //フィールド一覧取得
        $fieldArray = knje383Query::getFieldArray($db, 'AFT_GRAD_COURSE_DAT');

        //SEQのMAX値取得
        $maxSeq = $db->getOne(knje383Query::getMaxSeq($model));

        $entrydate = str_replace("/", "-", $model->field["ENTRYDATE"]);

        //INSERT -- AFT_GRAD_COURSE_DAT
        $dataflg = false;
        $query = knje383Query::getCourseHopeDat($model, 'list');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $maxSeq = ($maxSeq) ? $maxSeq + 1 : 1;

            //読み替え
            $row["YEAR"]            = CTRL_YEAR;
            $row["SEQ"]             = $maxSeq;
            $row["SENKOU_KIND"]     = "0";
            $row["STAT_CD"]         = $row["SCHOOL_CD"];
            $row["TOROKU_DATE"]     = $entrydate;
            $row["REGISTERCD"]      = STAFFCD;

            $data = array();
            foreach ($fieldArray as $key => $val) {
                list ($field, $coltype) = $val;
                $coltype = trim($coltype);
                if ($coltype == 'DATE') {
                    $type = "DATE";
                } else if ($coltype == 'VARCHAR') {
                    $type = "TEXT";
                } else {
                    $type = "NUMBER";
                }

                if ($field == 'UPDATED') {
                    $data[$field][FUNC]     = "sysdate()";
                } else {
                    $data[$field][$type]    = $row[$field];
                }
            }
            $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DAT"));
            $dataflg = true;
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        return $dataflg;
    }

    //フィールド一覧取得
    function getFieldList($table) {
        $query  = " SELECT ";
        $query .= "     COLNO, ";
        $query .= "     NAME, ";
        $query .= "     COLTYPE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME = '".$table."' ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";

        return $query;
    }
    function getFieldArray(&$db, $table) {
        $fieldArray= array();
        $query = knje383Query::getFieldList($table);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $fieldArray[$row["COLNO"]] = array($row["NAME"], $row["COLTYPE"]);
        }
        $result->free();

        return $fieldArray;
    }

    //SEQのMAX値取得
    function getMaxSeq($model) {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SEQ ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         SEQ ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     ) T1 ";

        return $query;
    }

    //データ取得 -- COURSE_HOPE_DAT
    function getCourseHopeDat($model, $flg) {
        $query  = " WITH LIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.ENTRYDATE, ";
        $query .= "         T1.SEQ, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         COURSE_HOPE_DAT T1 ";
        $query .= "         INNER JOIN (SELECT ";
        $query .= "                         SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                         INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "                              ON S1.YEAR         = S2.YEAR ";
            $query .= "                             AND S1.GRADE        = S2.GRADE ";
            $query .= "                             AND S2.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= "                     WHERE ";
        $query .= "                         S1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "                         S1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                     ) T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         FISCALYEAR(T1.ENTRYDATE)    = '".CTRL_YEAR."' AND ";
        $query .= "         T1.COURSE_KIND              = '".$model->field["COURSE_KIND"]."' AND ";
        $query .= "         T1.QUESTIONNAIRECD          = '".$model->field["QUESTIONNAIRECD"]."' ";
        $query .= " ), MAX_LIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ENTRYDATE, ";
        $query .= "         MAX(T1.SEQ) AS SEQ ";
        $query .= "     FROM ";
        $query .= "         LIST T1 ";
        $query .= "         INNER JOIN (SELECT ";
        $query .= "                         SCHREGNO, ";
        $query .= "                         MAX(ENTRYDATE) AS ENTRYDATE ";
        $query .= "                     FROM ";
        $query .= "                         LIST ";
        $query .= "                     GROUP BY ";
        $query .= "                         SCHREGNO ";
        $query .= "                     ) T2 ON T1.SCHREGNO = T2.SCHREGNO AND T1.ENTRYDATE = T2.ENTRYDATE ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ENTRYDATE ";
        $query .= " ), SUBMAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         COURSE_HOPE_DAT T1 ";
        $query .= "         INNER JOIN MAX_LIST T2 ";
        $query .= "              ON T1.ENTRYDATE    = T2.ENTRYDATE ";
        $query .= "             AND T1.SEQ          = T2.SEQ ";
        $query .= "             AND T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         1 AS HOPE_NUM, ";
        $query .= "         SCHOOL_GROUP1 AS SCHOOL_GROUP, ";
        $query .= "         SCHOOL_CD1 AS SCHOOL_CD, ";
        $query .= "         FACULTYCD1 AS FACULTYCD, ";
        $query .= "         DEPARTMENTCD1 AS DEPARTMENTCD, ";
        $query .= "         HOWTOEXAM1 AS HOWTOEXAM ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN ";
        $query .= "     WHERE ";
        $query .= "         SCHOOL_CD1 IS NOT NULL ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         2 AS HOPE_NUM, ";
        $query .= "         SCHOOL_GROUP2 AS SCHOOL_GROUP, ";
        $query .= "         SCHOOL_CD2 AS SCHOOL_CD, ";
        $query .= "         FACULTYCD2 AS FACULTYCD, ";
        $query .= "         DEPARTMENTCD2 AS DEPARTMENTCD, ";
        $query .= "         HOWTOEXAM2 AS HOWTOEXAM ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN ";
        $query .= "     WHERE ";
        $query .= "         SCHOOL_CD2 IS NOT NULL ";
        for ($i = 3; $i <= 6; $i++) {
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         HOPE_NUM, ";
            $query .= "         SCHOOL_GROUP, ";
            $query .= "         SCHOOL_CD, ";
            $query .= "         FACULTYCD, ";
            $query .= "         DEPARTMENTCD, ";
            $query .= "         HOWTOEXAM ";
            $query .= "     FROM ";
            $query .= "         COURSE_HOPE_DETAIL_DAT T1 ";
            $query .= "         INNER JOIN SUBMAIN T2 ON T1.ENTRYDATE = T2.ENTRYDATE AND T1.SEQ = T2.SEQ AND T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     WHERE ";
            $query .= "         HOPE_NUM = {$i} AND ";
            $query .= "         SCHOOL_CD IS NOT NULL ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        if ($flg == 'cnt') {
            $query .= "     COUNT(SCHREGNO) AS CNT "; 
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     MAIN ";
        if ($flg == 'list') {
            $query .= " ORDER BY ";
            $query .= "     SCHREGNO, ";
            $query .= "     HOPE_NUM ";
        }

        return $query;
    }

    //実行履歴
    function executeRireki($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knje383Query::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];  //実行日付
        $calcTime = $calcRow["CALC_TIME"];  //実行時間

        //実行履歴データ・追加
        $data = array();
        $data["CALC_DATE"][TEXT]        = $calcDate;
        $data["CALC_TIME"][TEXT]        = $calcTime;
        $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHOOL_KIND"];
        $data["COURSE_KIND"][TEXT]      = $model->field["COURSE_KIND"];
        $data["QUESTIONNAIRECD"][TEXT]  = $model->field["QUESTIONNAIRECD"];
        $data["ENTRYDATE"][TEXT]        = str_replace("/", "-", $model->field["ENTRYDATE"]);
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = CTRL_SEMESTER;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_EXEC_DAT"));

        $db->commit();
        Query::dbCheckIn($db);
    }
    //実行日付・時間を取得
    function getCalcDateTime() {
        $query  = " WITH T_DATE_TIME (CALC_DATE, CALC_TIME) AS ( ";
        $query .= " VALUES ( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= " SELECT * FROM T_DATE_TIME ";

        return $query;
    }
}
?>
