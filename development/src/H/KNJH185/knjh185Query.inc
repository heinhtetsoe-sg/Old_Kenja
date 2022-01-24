<?php

require_once('for_php7.php');

class knjh185Query extends Query {
    //預かり金一覧取得
    function getFareList() {
        $query .= " SELECT ";
        $query .= "     T1.FARE_CD, ";
        $query .= "     T1.FARE ";
        $query .= " FROM ";
        $query .= "     CHILDCARE_FARE_MST T1, ";
        $query .= "     CHILDCARE_FARE_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.FARE_CD  = T2.FARE_CD AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.FARE_CD ";

        return $query;
    }

    //対象幼稚園取得
    function getKindergarten() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECODE || ':' || VALUE(T2.COURSECODENAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T2 ";
        $query .= "              ON T1.YEAR         = T2.YEAR ";
        $query .= "             AND T1.COURSECODE   = T2.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '".$model->field["COURSECODE"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getLeftStudent($model) {
        $date = str_replace("/", "-", $model->field["CARE_DATE"]);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     CASE L2.SCHEDULE_CD WHEN '1' THEN '前半' WHEN '2' THEN '後半' ELSE '' END AS BUS_COURSE, ";
        $query .= "     T2.FARE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO, ";
        $query .= "     CHILDCARE_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_ENVIR_DAT ENVIR ";
        $query .= "              ON T2.SCHREGNO          = ENVIR.SCHREGNO ";
        $query .= "             AND ENVIR.HOWTOCOMMUTECD = '1' ";
        $query .= "             AND ENVIR.FLG_2          = '3' ";
        $query .= "     LEFT JOIN CHILDCARE_BUS_YMST L2 ON T2.YEAR = L2.YEAR AND ENVIR.ROSEN_2 = L2.COURSE_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T2.CARE_DATE    = '".$date."' AND ";
        $query .= "     T2.FARE_CD IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //生徒取得
    function getRightStudent($model, $flg, $sch="") {
        if ($flg == "info") {
            list ($schregno, $attendno, $farecd, $changeflg, $original) = explode('-', $sch);
        } else {
            $farecd = "";
            $changeflg = 0;
            $original = "R";
            $date = str_replace("/", "-", $model->field["CARE_DATE"]);
        }

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     CASE L3.SCHEDULE_CD WHEN '1' THEN '前半' WHEN '2' THEN '後半' ELSE '' END AS BUS_COURSE, ";
        $query .= "     '".$farecd."' AS FARE_CD, ";
        $query .= "     '".$changeflg."' AS CHANGEFLG, ";
        $query .= "     '".$original."' AS ORIGINAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "              ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN CHILDCARE_DAT L2 ";
        $query .= "              ON T1.YEAR         = L2.YEAR ";
        $query .= "             AND T1.SCHREGNO     = L2.SCHREGNO ";
        $query .= "             AND L2.CARE_DATE    = '".$date."' ";
        $query .= "     LEFT JOIN SCHREG_ENVIR_DAT ENVIR ";
        $query .= "              ON T1.SCHREGNO          = ENVIR.SCHREGNO ";
        $query .= "             AND ENVIR.HOWTOCOMMUTECD = '1' ";
        $query .= "             AND ENVIR.FLG_2          = '3' ";
        $query .= "     LEFT JOIN CHILDCARE_BUS_YMST L3 ";
        $query .= "              ON T1.YEAR       = L3.YEAR ";
        $query .= "             AND ENVIR.ROSEN_2 = L3.COURSE_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($flg == "info") {
            $query .= "     AND T1.SCHREGNO = '".$schregno."' ";
        } else {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
            if ($flg == "list") {
                $query .= "     AND NOT EXISTS (SELECT ";
                $query .= "                         'X' ";
                $query .= "                     FROM ";
                $query .= "                         CHILDCARE_DAT S1 ";
                $query .= "                     WHERE ";
                $query .= "                         S1.YEAR      = T1.YEAR AND ";
                $query .= "                         S1.SCHREGNO  = T1.SCHREGNO AND ";
                $query .= "                         S1.CARE_DATE = '".$date."' AND ";
                $query .= "                         S1.FARE_CD IS NOT NULL ";
                $query .= "                     ) ";
                $query .= " ORDER BY ";
                $query .= "     T1.ATTENDNO ";
            }
        }

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $date = str_replace("/", "-", $model->field["CARE_DATE"]);

        //左リスト（対象園児）
        if ($model->selectdataL) {
            $selectdataL = explode(',', $model->selectdataL);
            for ($i=0; $i < get_count($selectdataL); $i++) {
                list ($schregno, $attendno, $farecd, $changeflg, $original) = explode('-', $selectdataL[$i]);

                //存在チェック
                $exists = $db->getOne(knjh185Query::checkExists($schregno, $date));

                $data = array();
                if ($exists == 0) {
                    //追加
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SCHREGNO"][TEXT]         = $schregno;
                    $data["CARE_DATE"][TEXT]        = $date;
                    $data["FARE_CD"][TEXT]          = $farecd;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $db->query(Query::insertSQL($data, "CHILDCARE_DAT"));
                } else {
                    //更新
                    $data["FARE_CD"][TEXT]          = $farecd;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                    $where .= "     SCHREGNO    = '".$schregno."' AND ";
                    $where .= "     CARE_DATE   = '".$date."' ";

                    $db->query(Query::updateSQL($data, "CHILDCARE_DAT", $where));
                }
            }
        }

        //右リスト（対象外園児）
        if ($model->selectdataR) {
            $selectdataR = explode(',', $model->selectdataR);
            for ($i=0; $i < get_count($selectdataR); $i++) {
                list ($schregno, $attendno, $farecd, $changeflg, $original) = explode('-', $selectdataR[$i]);

                //存在チェック
                $exists = $db->getOne(knjh185Query::checkExists($schregno, $date));

                $data = array();
                if ($exists > 0) {
                    //更新
                    $data["FARE_CD"][TEXT]          = NULL;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                    $where .= "     SCHREGNO    = '".$schregno."' AND ";
                    $where .= "     CARE_DATE   = '".$date."' ";

                    $db->query(Query::updateSQL($data, "CHILDCARE_DAT", $where));
                }
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //存在チェック
    function checkExists($schregno, $date) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHILDCARE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     CARE_DATE   = '".$date."' ";

        return $query;
    }
}
?>
