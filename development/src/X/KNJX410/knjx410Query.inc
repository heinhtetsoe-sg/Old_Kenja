<?php

require_once('for_php7.php');

class knjx410Query extends Query {
    //メニューデータ取得
    function selectQuery($properties, $menuid, $admingrp_flg, $form, $pastyear, $cnt = "")
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  MENUID, ";
        $query .= "  SUBMENUID, ";
        $query .= "  PARENTMENUID, ";
        $query .= "  MENUNAME, ";
        $query .= "  PROGRAMID, ";
        $query .= "  PROGRAMPATH, ";
        $query .= "  PROCESSCD, ";
        $query .= "  INVALID_FLG ";
        $query .= "FROM ";
        $query .= "  MENU_HIGH_SECURITY_MST ";
        $query .= "ORDER BY ";
        $query .= " SUBMENUID,SHOWORDER,MENUID ";

        return $query;
    }
    //変更年度・学期取得
    function selectChgTermQuery($admingrp_flg, $properties)
    {

        $query  = " SELECT ";
        $query .= " T1.YEAR, ";
        $query .= " T2.SEMESTER, ";
        $query .= " T2.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= " SCHOOL_MST T1, ";
        $query .= " SEMESTER_MST T2, ";
        $query .= " CONTROL_MST T3 ";
        $query .= " WHERE ";
        $query .= " T1.YEAR=T2.YEAR AND ";
        $query .= " NOT SEMESTER = '9' AND ";
        $query .= " INTEGER(T1.YEAR) ";
        if ($admingrp_flg == 1) {
            $query .= " between INTEGER(T3.CTRL_YEAR) - ".$properties["useAdminYearPast"]." AND ";
            $query .= " INTEGER(T3.CTRL_YEAR) + ".$properties["useAdminYearFuture"]." ";
        } else {
            $query .= " between INTEGER(T3.CTRL_YEAR) - ".$properties["useUnAdminYearPast"]." AND ";
            $query .= " INTEGER(T3.CTRL_YEAR) + ".$properties["useUnAdminYearFuture"]." ";
        }
        $query .= " ORDER BY ";
        $query .= " T1.YEAR DESC, ";
        $query .= " T2.SEMESTER DESC";
        return $query;
    }
    //コントロールマスタメッセージ取得
    function getCtrlMessage(){

        $query = "";
        $query .= "SELECT ";
        $query .= "  MESSAGE, ";
        $query .= "  DATE(UPDATED) UPDATED, ";
        $query .= "  TIME(UPDATED) UPTIME ";
        $query .= "FROM ";
        $query .= "  CONTROL_MST ";
        $query .= "ORDER BY ";
        $query .= "  UPDATED DESC";

        return $query;
    }
    //パスワード変更処理
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["PASSWD"][TEXT]           = md5($model->password1);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        $where = " WHERE STAFFCD = '" .STAFFCD ."' ";

        $query = Query::updateSQL($data, "USER_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
    }

    function getCheckPasswordQuery()
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
//        $query .= " CAST(T2.UPDATED as DATE) - T1.CTRL_DATE as REMAININGDAYS, ";
        $query .= " T1.CTRL_DATE - CAST(T2.UPDATED as DATE) as REMAININGDAYS, ";
        $query .= " T1.PWDVALIDTERM, ";
        $query .= " T2.PWDTERMCHK_FLG ";
        $query .= " FROM ";
        $query .= " CONTROL_MST T1, ";
        $query .= " USER_MST T2 ";
        $query .= " WHERE ";
        $query .= " T2.STAFFCD='".STAFFCD."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;

    }
    //usergroup取得
   function getUsergroupQuery()
   {
       $query = " SELECT ";
       $query .= " T1.GROUPCD ";
       $query .= " FROM ";
       $query .= "  USERGROUP_DAT T1,CONTROL_MST T2 ";
       $query .= " WHERE ";
       $query .= " T1.YEAR = '".CTRL_YEAR."' AND ";
       $query .= " T1.YEAR = T2.CTRL_YEAR AND ";
       $query .= " T1.STAFFCD ='".STAFFCD."' AND ";
       $query .= " (T1.GROUPCD='9999' OR (T1.GROUPCD BETWEEN '7000' AND '7999')) ";

        return $query;
   }

   function getDateCtrl()
   {
       $query  = " SELECT ";
       $query .= "     * ";
       $query .= " FROM ";
       $query .= "     NAME_MST ";
       $query .= " WHERE ";
       $query .= "     NAMECD1 = 'Z026' ";
       $query .= " ORDER BY ";
       $query .= "     NAMECD2 ";

        return $query;
   }

    function getMisyukketuPrt1($model, $toDate, $div = "") {
        $query  = " WITH CHAIR_T AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= " ), SCH_STF AS ( ";
        $query .= " SELECT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE BETWEEN '".$model->syukketuSdate."' AND '".$toDate."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= " ) ";
        if ($div == "CNT") {
            $query .= " , CNT_T AS ( ";
        }
        $query .= " SELECT ";
        $query .= "     '1' AS FLG, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     L2.NAME1 AS PERIODNAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'B001' ";
        $query .= "          AND L2.NAMECD2 = T1.PERIODCD, ";
        $query .= "     CHAIR_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE BETWEEN '".$model->syukketuSdate."' AND '".$toDate."' ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "     AND VALUE(T1.EXECUTED, '0') != '1' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '2' AS FLG, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     L2.NAME1 AS PERIODNAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'B001' ";
        $query .= "          AND L2.NAMECD2 = T1.PERIODCD, ";
        $query .= "     SCH_STF T2 ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE = T2.EXECUTEDATE ";
        $query .= "     AND T1.PERIODCD = T2.PERIODCD ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "     AND VALUE(T1.EXECUTED, '0') != '1' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     CHAIRCD ";
        if ($div == "CNT") {
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     CNT_T ";
        }

        return $query;
    }

    function getMisyukketuPrt2($model, $toDate, $div = "") {
        $query .= " WITH TAISYOU_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T3.CHAIRCD, ";
        $query .= "     T3.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2, ";
        $query .= "     CHAIR_STD_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' ";
        $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
        $query .= "          OR T1.TR_CD3 = '".STAFFCD."' ";
        $query .= "          OR T1.SUBTR_CD1 = '".STAFFCD."' ";
        $query .= "          OR T1.SUBTR_CD2 = '".STAFFCD."' ";
        $query .= "          OR T1.SUBTR_CD3 = '".STAFFCD."') ";
        $query .= "     AND T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= "     AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     AND T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     T3.CHAIRCD, ";
        $query .= "     T3.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";
        $query .= " ), MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     L2.NAME1 AS PERIODNAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.CHAIRNAME, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'B001' ";
        $query .= "          AND L2.NAMECD2 = T1.PERIODCD, ";
        $query .= "     TAISYOU_T T2 ";
        $query .= "     LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND L1.SEMESTER = T2.SEMESTER ";
        $query .= "          AND L1.CHAIRCD = T2.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE BETWEEN '".$model->syukketuSdate."' AND '".$model->syukketuEdate."' ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "     AND VALUE(T1.EXECUTED, '0') != '1' ";
        $query .= " GROUP BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     L2.NAME1, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.CHAIRNAME, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS ";
        $query .= " ) ";
        if ($div == "CNT") {
            $query .= " , CNT_T AS ( ";
        }
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     T2.EXECUTEDATE ";
        $query .= "                 FROM ";
        $query .= "                     SCH_CHR_HRATE_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.EXECUTEDATE = T2.EXECUTEDATE ";
        $query .= "                     AND T1.PERIODCD = T2.PERIODCD ";
        $query .= "                     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "                     AND T1.GRADE = T2.GRADE ";
        $query .= "                     AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "                     AND VALUE(T2.EXECUTED, '0') = '1' ";
        $query .= "                ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";
        $query .= "  ";
        if ($div == "CNT") {
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     CNT_T ";
        }

        return $query;
    }

    //開始終了
    function getSemeDate($seme)
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$seme."' ";

        return $query;
    }

    //学校取得
    function getSchool()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        $query .= "     AND NAME1 IN ('KINDAI', 'KINJUNIOR') ";

        return $query;
    }
}
?>
