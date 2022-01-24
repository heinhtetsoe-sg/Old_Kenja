<?php

require_once('for_php7.php');
class knjxmenuQuery extends Query {
    //メニューデータ取得
    function selectQuery($model, $properties, $menuid, $admingrp_flg, $form, $pastyear, $cnt = "", $parentmenuid = "")
    {
        $useTable = $properties["useMenuStaffDat"] == "1" ? "MENU_STAFF_MST" : "MENU_MST";
        $securityCheckT = $properties["useMenuStaffDat"] == "1" ? "SECURITY_CHK_MNU2" : "SECURITY_CHK_MNU";
        $query = "";
        if ($properties["useMenuStaffDat"] == "1") {
            $query .= " WITH MAIN_MENU_T AS( ";
            $query .= " SELECT ";
            $query .= "     MENUID, ";
            $query .= "     SUBMENUID, ";
            $query .= "     PARENTMENUID, ";
            $query .= "     MENUNAME, ";
            $query .= "     PROGRAMID, ";
            $query .= "     PROGRAMPATH, ";
            $query .= "     PROCESSCD, ";
            $query .= "     INVALID_FLG, ";
            $query .= "     PROGRAMMEMO, ";
            $query .= "     SHOWORDER, ";
            $query .= "     SSL ";
            $query .= " FROM ";
            $query .= "     MENU_MST ";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     MENUID, ";
            $query .= "     SUBMENUID, ";
            $query .= "     PARENTMENUID, ";
            $query .= "     MENUNAME, ";
            $query .= "     PROGRAMID, ";
            $query .= "     PROGRAMPATH, ";
            $query .= "     PROCESSCD, ";
            $query .= "     INVALID_FLG, ";
            $query .= "     PROGRAMMEMO, ";
            $query .= "     SHOWORDER, ";
            $query .= "     SSL ";
            $query .= " FROM ";
            $query .= "     MENU_STAFF_MST ";
            $query .= " WHERE ";
            $query .= "     STAFFCD = '".STAFFCD."' ";
            $query .= " ) ";
        }
        $query .= "SELECT ";
        if ($cnt == "") {
            $query .= "  MENUID, ";
            $query .= "  SUBMENUID, ";
            $query .= "  PARENTMENUID, ";
            $query .= "  MENUNAME, ";
            $query .= "  PROGRAMID, ";
            $query .= "  PROGRAMPATH, ";
            $query .= "  PROCESSCD, ";
            $query .= "  INVALID_FLG ";
        } else {
            $query .= "  COUNT(*) AS CNT ";
        }
        $query .= "FROM ";
        if ($properties["useMenuStaffDat"] != "1") {
            $query .= "  MENU_MST ";
        } else {
            $query .= "  MAIN_MENU_T ";
        }
        $query .= "WHERE ";
        if ($model->ua == "tablet") {
            $query .= "  INVALID_FLG = '9' AND ";
        }
        if ($properties["useMenuUnuseStaffDat"] == "1") {
            $query .= " MENUID NOT IN (SELECT MENUID FROM MENU_UNUSE_STAFF_DAT WHERE YEAR = '".CTRL_YEAR."' AND STAFFCD = '".STAFFCD."') AND ";
            $query .= " MENUID NOT IN ( ";
            $query .= "     SELECT ";
            $query .= "         T4.PARENTMENUID ";
            $query .= "     FROM ";
            $query .= "         USER_MST T1, ";
            $query .= "         USERGROUP_DAT T2, ";
            $query .= "         GROUPAUTH_DAT T3, ";
            $query .= "         {$useTable}      T4 ";
            $query .= "         LEFT JOIN MENU_UNUSE_STAFF_DAT MS ON MS.YEAR = '".CTRL_YEAR."' ";
            $query .= "              AND MS.STAFFCD = '".STAFFCD."' ";
            $query .= "              AND MS.MENUID = T4.MENUID ";
            $query .= "     WHERE ";
            $query .= "         T1.STAFFCD = T2.STAFFCD ";
            $query .= "         AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "         AND T2.YEAR    = '".CTRL_YEAR."' ";
            $query .= "         AND T2.GROUPCD = T3.GROUPCD ";
            $query .= "         AND T3.MENUID  = T4.MENUID ";
            $query .= "     GROUP BY ";
            $query .= "         T4.PARENTMENUID ";
            $query .= "     HAVING ";
            $query .= "         SUM(CASE WHEN MS.MENUID IS NULL ";
            $query .= "                  THEN 1 ";
            $query .= "                  ELSE 0 ";
            $query .= "             END) = 0 ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         T3.PARENTMENUID ";
            $query .= "     FROM ";
            $query .= "         USER_MST     T1, ";
            $query .= "         USERAUTH_DAT T2, ";
            $query .= "         {$useTable}     T3 ";
            $query .= "         LEFT JOIN MENU_UNUSE_STAFF_DAT MS ON MS.YEAR = '".CTRL_YEAR."' ";
            $query .= "              AND MS.STAFFCD = '".STAFFCD."' ";
            $query .= "              AND MS.MENUID = T3.MENUID ";
            $query .= "     WHERE ";
            $query .= "         T1.STAFFCD = T2.STAFFCD ";
            $query .= "         AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "         AND T2.MENUID  = T3.MENUID ";
            $query .= "     GROUP BY ";
            $query .= "         T3.PARENTMENUID ";
            $query .= "     HAVING ";
            $query .= "         SUM(CASE WHEN MS.MENUID IS NULL ";
            $query .= "                  THEN 1 ";
            $query .= "                  ELSE 0 ";
            $query .= "             END) = 0 ";
            $query .= " ) AND ";
        }

        if ($form == 1){
            $query .= "  PROGRAMID IS NULL ";
            $query .= "  AND {$securityCheckT}('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."') <> '9' ";
            if ($admingrp_flg == 0 && $pastyear && $properties["useUnAdminMenuPrgid"] != "") {
                $query .= " AND MENUID IN ( ";
                $query .= "         SELECT ";
                $query .= "             PARENTMENUID ";
                $query .= "         FROM ";
                $query .= "             {$useTable} ";
                $query .= "         WHERE ";
                $query .= "             PROGRAMID IN ('".$properties["useUnAdminMenuPrgid"]."') AND ";
                $query .= "             {$securityCheckT}('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."') <> '9' ";
                $query .= "         UNION ";
                $query .= "         SELECT ";
                $query .= "             PARENTMENUID ";
                $query .= "         FROM ";
                $query .= "             {$useTable} ";
                $query .= "         WHERE ";
                $query .= "             MENUID IN ( ";
                $query .= "                 SELECT ";
                $query .= "                     PARENTMENUID ";
                $query .= "                 FROM ";
                $query .= "                     {$useTable} ";
                $query .= "                 WHERE ";
                $query .= "                     PROGRAMID IN ('".$properties["useUnAdminMenuPrgid"]."') AND ";
                $query .= "                     {$securityCheckT}('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."') <> '9' ";
                $query .= "             ) ";
                $query .= "     ) ";
            }
            if ($properties["useSubMenuId"] != "") {
                $query .= " AND MENUID IN ( ";
                $query .= "         SELECT ";
                $query .= "             PARENTMENUID ";
                $query .= "         FROM ";
                $query .= "             {$useTable} ";
                $query .= "         WHERE ";
                $query .= "              IN ('".$properties["useSubMenuId"]."') AND ";
                $query .= "             {$securityCheckT}('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."') <> '9' ";
                $query .= "     ) ";
            }
        } else {
            $query .= "  PARENTMENUID = '" .$menuid ."' AND ";
            $query .= "  {$securityCheckT}('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."') <> '9' ";
            if ($admingrp_flg == 0 && $pastyear && $properties["useUnAdminMenuPrgid"] != "") {
                $query .= "  AND PROGRAMID IN ('".$properties["useUnAdminMenuPrgid"]."') ";
            }
            if ($properties["useSubMenuId"] != "") {
                $query .= "  AND SUBMENUID IN ('".$properties["useSubMenuId"]."') ";
            }
        }
        if ($cnt == "ROOT_CNT") {
            $query .= "  AND PARENTMENUID = '{$parentmenuid}' ";
        } else if ($cnt == "") {
            $query .= "ORDER BY ";
            $query .= " SUBMENUID,SHOWORDER,MENUID ";
        }

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
        $query .= "     SEMESTER, ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= " ), SCH_STF AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.SEMESTER, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_STF_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND L1.SEMESTER < '9' ";
        $query .= "           AND T1.EXECUTEDATE BETWEEN L1.SDATE AND L1.EDATE ";
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
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
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
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
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

    function getBunsyoKanri() {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND (KATAGAKI1_F1 = '1051' OR KATAGAKI2_F1 = '1051' OR KATAGAKI3_F1 = '1051') ";

        return $query;
    }

    function getTuutatu($model, $div = "") {

        $query  = " SELECT ";
        if ($div == "CNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.DOC_NUMBER, ";
            $query .= "     L1.SUBMISSION_DATE, ";
            $query .= "     L1.TRANSMISSION_DATE, ";
            $query .= "     L1.NOTICE_TITLE, ";
            $query .= "     L1.NOTICE_MESSAGE, ";
            $query .= "     CASE WHEN (VALUE(T1.ANSWER_FLG, '0') = '0' AND VALUE(L1.SUBMISSION_DATE, '1800-04-01') < '".CTRL_DATE."') ";
            $query .= "          THEN 'RED' ";
            $query .= "          ELSE 'BLUE' ";
            $query .= "     END AS COLOR ";
        }
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.DOC_NUMBER = L1.DOC_NUMBER ";
        $query .= "           AND L1.TRANSMISSION_DATE IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.EDBOARD_SCHOOLCD = '{$model->edboardSchoolcd}' ";
        $query .= "     AND ((VALUE(T1.RECEIVE_FLG, '0') = '0' AND '".CTRL_DATE."' BETWEEN VALUE(L1.VIEWING_PERIOD_FROM, '".CTRL_YEAR."-04-01') AND VALUE(L1.VIEWING_PERIOD_TO, '".(CTRL_YEAR + 1)."-03-31')) ";
        $query .= "          OR ";
        $query .= "          (VALUE(L1.REQUEST_ANSWER_FLG, '0') = '1' AND VALUE(T1.ANSWER_FLG, '0') = '0' AND VALUE(L1.SUBMISSION_DATE, '1800-04-01') < '".CTRL_DATE."') ";
        $query .= "         ) ";
        if ($div != "CNT") {
            $query .= " ORDER BY ";
            $query .= "     T1.DOC_NUMBER ";
        }

        return $query;
    }

    function getEdboardSchoolcd() {

        $query  = " SELECT ";
        $query .= "     KYOUIKU_IINKAI_SCHOOLCD AS EDBOARD_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

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

    //DB２使用
    function getUseDb2()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        $query .= "     AND VALUE(ABBV1, '0') IN ('1', '2') ";

        return $query;
    }

    function getUpdTuutatu($model, $docNum) {

        $data = array();
        $data["RECEIVE_FLG"][TEXT]      = "1";
        $data["RECEIVE_DATE"][FUNC]     = "SYSDATE()";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".CTRL_YEAR."' ";
        $where .= "     AND DOC_NUMBER = {$docNum} ";
        $where .= "     AND EDBOARD_SCHOOLCD = '{$model->edboardSchoolcd}' ";

        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
        return $query;
    }

}
?>
