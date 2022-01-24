<?php

require_once('for_php7.php');

class knjd131fQuery extends Query {

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
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (T1.TR_CD1 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.TR_CD2 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.TR_CD3 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.SUBTR_CD1 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD2 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD3 = '". STAFFCD ."') ";      //2005/06/29
        }
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
        $cnt9 = $db->getOne(knjd131fQuery::checkHreportremarkDat($schregno, $model, "9"));
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
        
        //学期ごと（通信欄）
        //1学期(前期)
        $cnt1 = $db->getOne(knjd131fQuery::checkHreportremarkDat($schregno, $model, "1"));
        $data1["COMMUNICATION"][TEXT]    = $communication1;
        $data1["REGISTERCD"][TEXT]       = STAFFCD;
        $data1["UPDATED"][NUMBER]        = "sysdate()";

        if ($cnt1 > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER = '1' AND ";
            $where .= "     SCHREGNO = '".$schregno."' ";

            $query = Query::updateSQL($data1, "HREPORTREMARK_DAT", $where);
            $db->query($query);
        } else {
            $data1["YEAR"][TEXT]      = CTRL_YEAR;
            $data1["SEMESTER"][TEXT]  = '1';
            $data1["SCHREGNO"][TEXT]  = $schregno;

            $query = Query::insertSQL($data1, "HREPORTREMARK_DAT");
            $db->query($query);
        }

        $cnt2 = $db->getOne(knjd131fQuery::checkHreportremarkDat($schregno, $model, "2"));
        $data2["COMMUNICATION"][TEXT]    = $communication2;
        $data2["REGISTERCD"][TEXT]       = STAFFCD;
        $data2["UPDATED"][NUMBER]        = "sysdate()";
        
        //2学期(後期)
        if ($cnt2 > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER = '2' AND ";
            $where .= "     SCHREGNO = '".$schregno."' ";

            $query = Query::updateSQL($data2, "HREPORTREMARK_DAT", $where);
            $db->query($query);
        } else {
            $data2["YEAR"][TEXT]      = CTRL_YEAR;
            $data2["SEMESTER"][TEXT]  = '2';
            $data2["SCHREGNO"][TEXT]  = $schregno;

            $query = Query::insertSQL($data2, "HREPORTREMARK_DAT");
            $db->query($query);
        }
        
        //3学期
        if ($model->semsterCount == "3") {
            $cnt3 = $db->getOne(knjd131fQuery::checkHreportremarkDat($schregno, $model, "3"));
            $data3["COMMUNICATION"][TEXT]    = $communication3;
            $data3["REGISTERCD"][TEXT]       = STAFFCD;
            $data3["UPDATED"][NUMBER]        = "sysdate()";

            if ($cnt3 > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER = '3' AND ";
                $where .= "     SCHREGNO = '".$schregno."' ";

                $query = Query::updateSQL($data3, "HREPORTREMARK_DAT", $where);
                $db->query($query);
            } else {
                $data3["YEAR"][TEXT]      = CTRL_YEAR;
                $data3["SEMESTER"][TEXT]  = '3';
                $data3["SCHREGNO"][TEXT]  = $schregno;

                $query = Query::insertSQL($data3, "HREPORTREMARK_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        return;
    }
}
?>
