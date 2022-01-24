<?php

require_once('for_php7.php');

class knjd131hQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR     = '".CTRL_YEAR."' ";
        $query .= "AND SEMESTER <> '9' ";

        return $query;
    }

    //クラス一覧取得
    function getHrClassList() {
        $query  = " SELECT DISTINCT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ一覧取得
    function getMainQuery($model, $semsterCount) {
    
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T3.SPECIALACTREMARK, ";
        $query .= "     T3.TOTALSTUDYTIME, ";
        $query .= "     T4.COMMUNICATION AS COMMUNICATION1, ";
        $query .= "     T5.COMMUNICATION AS COMMUNICATION2 ";
        if ($semsterCount == "3") {
            $query .= "     ,T6.COMMUNICATION AS COMMUNICATION3 ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2   ON T1.SCHREGNO   = T2.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T3 ON T3.YEAR       = T1.YEAR AND ";
        $query .= "                                       T3.SEMESTER   = '9' AND ";
        $query .= "                                       T3.SCHREGNO   = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T4 ON T4.YEAR       = T1.YEAR AND ";
        $query .= "                                       T4.SEMESTER   = '1' AND ";
        $query .= "                                       T4.SCHREGNO   = T1.SCHREGNO ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT T5 ON T5.YEAR       = T1.YEAR AND ";
        $query .= "                                       T5.SEMESTER   = '2' AND ";
        $query .= "                                       T5.SCHREGNO   = T1.SCHREGNO ";
        if ($semsterCount == "3") {
            $query .= "     LEFT JOIN HREPORTREMARK_DAT T6 ON T6.YEAR       = T1.YEAR AND ";
            $query .= "                                       T6.SEMESTER   = '3' AND ";
            $query .= "                                       T6.SCHREGNO   = T1.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }


    //HREPORTREMARK_DAT 存在チェック
    function checkHreportremarkDat($schregno, $model, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //UPDATE
    function &getDetailUpdateQuery($db, $model, $schregno, $specialactremark, $totalstudytime, $communication1, $communication2, $communication3) {

        $db->autoCommit(false);
        $cnt1 = "";
        $cnt2 = "";
        $cnt3 = "";
        $cnt9 = "";
        //通年（既修得単位数、総修得単位数）
        $cnt9 = $db->getOne(knjd131hQuery::checkHreportremarkDat($schregno, $model, "9"));
        $data9["SPECIALACTREMARK"][TEXT]        = $specialactremark;
        $data9["REGISTERCD"][TEXT]       = STAFFCD;
        $data9["UPDATED"][NUMBER]        = "sysdate()";
        if ($cnt9 > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER = '9' AND ";
            $where .= "     SCHREGNO = '".$schregno."' ";

            $query = Query::updateSQL($data9, "HREPORTREMARK_DAT", $where);
            $db->query($query);
        } else {
            $data9["YEAR"][TEXT]      = CTRL_YEAR;
            $data9["SEMESTER"][TEXT]  = '9';
            $data9["SCHREGNO"][TEXT]  = $schregno;

            $query = Query::insertSQL($data9, "HREPORTREMARK_DAT");
            $db->query($query);
        }

        $db->commit();
        return;
    }
}
?>
