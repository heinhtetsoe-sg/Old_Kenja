<?php

require_once('for_php7.php');

class knjg084Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //画面左側の表データ取得
    function getEntryList($model) {
        $query  = "WITH CLASSCNT AS ( ";
        $query .= " SELECT ";
        $query .= "   H1.GRADE || H1.HR_CLASS AS GRADE_CLASS, ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_NOT_SENDER_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                              AND L2.YEAR      = T1.YEAR ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
        $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND H1.GRADE    = T1.GRADE ";
        $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_GDAT G1 ON G1.YEAR     = T1.YEAR ";
        $query .= "                        AND G1.GRADE    = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   AND L2.SCHREGNO IS NOT NULL ";
        if ($model->field["SCHKIND"] != "" AND $model->field["SCHKIND"] != "ALL") {
            $query .= "                AND G1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else {
            if ($model->selectSchoolKind) {
                $query .= "     AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " GROUP BY ";
        $query .= "   H1.GRADE, H1.HR_CLASS ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "  H1.GRADE || H1.HR_CLASS AS GRADE_CLASS, ";
        $query .= "  H1.HR_NAME AS HR_NAME, ";
        $query .= "  T1.ATTENDNO || L1.NAME AS SET_NAME, ";
        $query .= "  CCNT1.CNT AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_NOT_SENDER_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                              AND L2.YEAR      = T1.YEAR ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
        $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND H1.GRADE    = T1.GRADE ";
        $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_GDAT G1 ON G1.YEAR     = T1.YEAR ";
        $query .= "                        AND G1.GRADE    = T1.GRADE ";
        $query .= " LEFT JOIN ";
        $query .= "     CLASSCNT CCNT1 ON H1.GRADE || H1.HR_CLASS = CCNT1.GRADE_CLASS ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   AND L2.SCHREGNO IS NOT NULL ";
        if ($model->field["SCHKIND"] != "" AND $model->field["SCHKIND"] != "ALL") {
            $query .= "                AND G1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else {
            if ($model->selectSchoolKind) {
                $query .= "     AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "   H1.GRADE, ";
        $query .= "   H1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO ";
        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "    T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "    T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR='".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER='".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' ";
            if ($model->field["SCHKIND"] != "" && $model->field["SCHKIND"] != "ALL") {
                $query .= "                     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else {
                if ($model->selectSchoolKind) {
                    $query .= "      AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            }
            $query .= "             ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "             ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得(左側に表示する生徒)
    function getStudent_left($model) {
        $query  = " SELECT ";
        $query .= "     T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_NOT_SENDER_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                              AND L2.YEAR      = T1.YEAR ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
        $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND H1.GRADE    = T1.GRADE ";
        $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->field["GRADE_CLASS"]}' AND ";
        $query .= "     L2.SCHREGNO IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //生徒取得(右側に表示する生徒)
    function getStudent($model) {

        $query  = " SELECT ";
        $query .= "     T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_NOT_SENDER_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                              AND L2.YEAR      = T1.YEAR ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
        $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND H1.GRADE    = T1.GRADE ";
        $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->field["GRADE_CLASS"]}' AND ";
        $query .= "     L2.SCHREGNO IS NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //データ削除更新
    function &deleteNotSenderQuery($db, $model, $schregno) {

        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_NOT_SENDER_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO IN ( ";
        $query .= "         SELECT T2.SCHREGNO ";
        $query .= "         FROM SCHREG_REGD_DAT T2 ";
        $query .= "         WHERE ";
        $query .= "           T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_CLASS"]."' ";
        $query .= "           AND T2.YEAR = T1.YEAR ";
        $query .= "           AND T2.SCHREGNO = T1.SCHREGNO ) ";
        return $query;
    }

    //データ削除更新
    function &updateNotSenderQuery($db, $model, $schregno) {
        $data = array();
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_NOT_SENDER_DAT");

        return $query;

    }

    //前年度コピー
    function &insertCopyLastYearQuery() {
        $query  = " INSERT INTO SCHREG_NOT_SENDER_DAT( ";
        $query .= " SCHREGNO, ";
        $query .= " YEAR, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    '".CTRL_YEAR."', ";
        $query .= "    '".STAFFCD."', ";
        $query .= "    SYSDATE() ";
        $query .= "  FROM ";
        $query .= "    SCHREG_NOT_SENDER_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ";
        $query .= "  WHERE ";
        $query .= "    T1.YEAR = '".(CTRL_YEAR-1)."' ";
        $query .= "    AND B1.GRD_DIV IS NULL ";
        $query .= " ) ";
        return $query;
    }

    //コピー元データ件数
    function cntExistCopyData() {
        $query .= " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   SCHREG_NOT_SENDER_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".(CTRL_YEAR-1)."' ";
        $query .= "   AND B1.GRD_DIV IS NULL ";
        return $query;
    }

    //指定年度データ存在チェック
    function &checkYearDataQuery($year) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   SCHREG_NOT_SENDER_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."'";
        return $query;
    }
}
?>
