<?php

require_once('for_php7.php');
class knjxmenuQuery extends Query
{
    public function getMenuMstRow($prgId)
    {
        $query  = " SELECT ";
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
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$prgId}' ";
        return $query;
    }

    //メニューデータ取得
    public function selectQuery($menuid, $admingrp_flg, $form, $pastyear, $cnt = "", $kind = "", $model = "")
    {
        $properties = $model->properties;
        $staffMstUse = mb_substr($menuid, 0, 2);
        if ($staffMstUse == "ZZ") {
            $menumst = "MENU_STAFF_MST";
            $menusubmst = "";   //使用しないはず
            $securityCheckT = "SECURITY_CHK_MNU2";
        } else {
            $menumst = "MENU_MST";
            $menusubmst = "MENU_SUB_MST";
            $securityCheckT = "SECURITY_CHK_MNU";
        }

        $query .= "SELECT ";
        if ($kind != "SUB") {
            $query .= "  MENUID, ";
            $query .= "  SUBMENUID, ";
            $query .= "  PARENTMENUID, ";
            if ($staffMstUse == "ZZ") {
                $query .= "  MENUNAME, ";
            } else {
                $query .= "  MENUNAME{$model->menuField} AS MENUNAME, ";
            }
            $query .= "  PROGRAMMEMO, ";
            $query .= "  PROGRAMID AS MAIN, ";
            $query .= "  PROGRAMID, ";
            $query .= "  PROGRAMPATH ";
        } else {
            $query .= "    t1.SUBID AS MENUID, ";
            $query .= "    t1.SUBSUBID AS SUBMENUID, ";
            $query .= "    t1.PARENTSUBID AS PARENTMENUID, ";
            if ($staffMstUse == "ZZ") {
                $query .= "    t1.SUBNAME, ";
            } else {
                $query .= "    t1.SUBNAME{$model->menuField} AS SUBNAME, ";
            }
            $query .= "    t1.MAINID AS MAIN,";
            $query .= "    t2.MENUNAME{$model->menuField} AS MENUNAME, ";
            $query .= "    t2.PROGRAMMEMO, ";
            $query .= "    t2.PROGRAMID, ";
            $query .= "    t2.PROGRAMPATH ";
        }
        $query .= "FROM ";
        if ($kind != "SUB") {
            $query .= "  ".$menumst." t1 ";
        } else {      //教員のメニューは目的別メニューに表示されないはず。
            $query .= "    ".$menusubmst." t1 ";
            if ($properties["useSchool_KindMenu"] == "1") {
                $query .= "    left join ".$menumst." t2 on t1.MAINID = t2.MENUID and t2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else {
                $query .= "    left join ".$menumst." t2 on t1.MAINID = t2.MENUID ";
            }
        }
        $query .= "WHERE ";
        if ($model->ua == "tablet") {
            $query .= " INVALID_FLG = '9' AND ";
        }
        if ($kind != "SUB") {
            $query .= "  PARENTMENUID = '" .$menuid ."' AND ";
            if ($properties["useSchool_KindMenu"] == "1") {
                $query .= "  {$securityCheckT}('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."','".SCHOOLKIND."','".SCHOOLCD."') <> '9' ";
            } else {
                $query .= "  {$securityCheckT}('" .STAFFCD ."',MENUID,'" .CTRL_YEAR ."') <> '9' ";
            }
            if ($staffMstUse == "ZZ") {   //MENU_STAFF_MSTのときだけ
                $query .= " AND ";
                $query .= "     STAFFCD = '".STAFFCD."' ";
            }
        } else {
            $query .= "  PARENTSUBID = '" .$menuid ."' ";
            $query .= " AND ";
            if ($properties["useSchool_KindMenu"] == "1") {
                $query .= "    ({$securityCheckT}('" .STAFFCD ."',MAINID,'" .CTRL_YEAR ."','".SCHOOLKIND."','".SCHOOLCD."') <> '9' OR t1.MAINID = 'TITLE' OR t1.MAINID = 'ENGTITLE') ";
            } else {
                $query .= "    ({$securityCheckT}('" .STAFFCD ."',MAINID,'" .CTRL_YEAR ."') <> '9' OR t1.MAINID = 'TITLE' OR t1.MAINID = 'ENGTITLE') ";
            }
        }
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= " AND ";
            $query .= "     t1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND ";
            $query .= "     t1.SCHOOLCD = '".SCHOOLCD."' ";
        }
        if ($admingrp_flg == 0 && $pastyear) {
            if ($kind != "SUB") {
                $query .= "  AND t1.PROGRAMID in ('".$properties["useUnAdminMenuPrgid"]."') ";
            } else {
                $query .= "  AND t2.PROGRAMID in ('".$properties["useUnAdminMenuPrgid"]."') ";
            }
        }
        $query .= "ORDER BY ";
        if ($kind != "SUB") {
            $query .= " SUBMENUID,MENUID ";
        } else {
            $query .= "    SUBSUBID, ";
            $query .= "    SUBID ";
        }

        return $query;
    }

    //変更年度・学期取得
    public function selectChgTermQuery($admingrp_flg, $properties)
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
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= " AND ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND ";
            $query .= "     T1.SCHOOLCD = '".SCHOOLCD."' ";
        }
        $query .= " ORDER BY ";
        $query .= " T1.YEAR DESC, ";
        $query .= " T2.SEMESTER DESC";
        return $query;
    }
    //コントロールマスタメッセージ取得
    public function getCtrlMessage()
    {
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

    //USER_PWD_HIST_MSTテーブルにデータ存在するか？
    public function getCheckPassHist($model)
    {
        $db = Query::dbCheckOut();

        $oldPasswordEnableChangeCount = $model->properties["oldPasswordEnableChangeCount"] ;

        $query  = " WITH HIST AS (";
        $query .= " SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY UPDATED DESC) AS ORDER,  ";
        $query .= "     PASSWD   ";
        $query .= " FROM ";
        $query .= "     USER_PWD_HIST_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND UPDATED IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS COUNT  ";
        $query .= " FROM HIST ";
        $query .= " WHERE ";
        $query .= "     PASSWD = '".md5($model->password1)."' ";
        if ($oldPasswordEnableChangeCount == '0' || $oldPasswordEnableChangeCount > 0) {
            $query .= "     AND ORDER <= {$oldPasswordEnableChangeCount} ";
        }

        $histCnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return array($histCnt, $oldPasswordEnableChangeCount);
    }

    //USER_PWD_HIST_MSTテーブルINSERT
    public function getUpdateInsertHist($model)
    {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     COUNT(*) AS COUNT  ";
        $query .= " FROM USER_PWD_HIST_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        $query .= "     AND PASSWD = '".md5($model->password1)."' ";
        $count = $db->getOne($query);

        $data = array();
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($count > 0) {
            $where  = " WHERE STAFFCD = '" .STAFFCD ."' ";
            $where .= "   AND PASSWD  = '" .md5($model->password1)."' ";
            $query = Query::updateSQL($data, "USER_PWD_HIST_MST", $where);
        } else {
            $data["STAFFCD"][TEXT]          = STAFFCD;
            $data["PASSWD"][TEXT]           = md5($model->password1);
            $query = Query::insertSQL($data, "USER_PWD_HIST_MST");
        }

        $db->query($query);

        Query::dbCheckIn($db);

        return true;
    }

    //パスワード変更処理
    public function &getUpdateQuery($model)
    {
        $haveChgPwdField = knjxmenuQuery::getChgPwdField();

        $db = Query::dbCheckOut();

        $data = array();
        $data["PASSWD"][TEXT]           = md5($model->password1);
        if ($haveChgPwdField) {
            $data["CHG_PWD_FLG"][TEXT]      = null;
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        if ($model->properties["useSchool_KindMenu"] == "1") {
            $where = " WHERE STAFFCD = '" .STAFFCD ."' AND SCHOOLCD = '".SCHOOLCD."' AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $where = " WHERE STAFFCD = '" .STAFFCD ."' ";
        }

        $query = Query::updateSQL($data, "USER_MST", $where);

        $db->query($query);


        Query::dbCheckIn($db);
    }

    //次回パスワード強制変更フィールドの有無
    public function getChgPwdField()
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME = 'USER_MST' ";
        $query .= "     AND NAME = 'CHG_PWD_FLG' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $cnt > 0 ? true : false;
    }

    //次回パスワード強制変更かチェック
    public function getPwdHistCnt()
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     USER_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND CHG_PWD_FLG = '1' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $cnt > 0 ? true : false;
    }

    public function getCheckPasswordQuery($properties)
    {
        $db = Query::dbCheckOut();
        $query  = " WITH MAX_UPDATED AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAFFCD, ";
        $query .= "         MAX(UPDATED) AS UPDATED ";
        $query .= "     FROM ";
        $query .= "         USER_PWD_HIST_MST ";
        $query .= "     WHERE ";
        $query .= "         STAFFCD = '".STAFFCD."' ";
        $query .= "     GROUP BY ";
        $query .= "         STAFFCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     days(CAST(SYSDATE() AS DATE)) - days(CAST(T3.UPDATED AS DATE)) AS REMAININGDAYS, ";
        $query .= "     T1.PWDVALIDTERM, ";
        $query .= "     T2.PWDTERMCHK_FLG ";
        $query .= " FROM ";
        $query .= "     CONTROL_MST T1, ";
        $query .= "     USER_MST T2 ";
        $query .= "     LEFT JOIN MAX_UPDATED T3 ON T3.STAFFCD = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T2.STAFFCD = '".STAFFCD."' ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "     AND T2.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //usergroup取得
    public function getUsergroupQuery($properties)
    {
        $query = " SELECT ";
        $query .= " T1.GROUPCD ";
        $query .= " FROM ";
//       $query .= "  USERGROUP_DAT T1,CONTROL_MST T2 ";
        $query .= "  USERGROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= " T1.YEAR = '".CTRL_YEAR."' AND ";
//       $query .= " T1.YEAR = T2.CTRL_YEAR AND ";
        $query .= " T1.STAFFCD ='".STAFFCD."' AND ";
        $query .= " (T1.GROUPCD='9999' OR (T1.GROUPCD BETWEEN '7000' AND '7999')) ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= " AND ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND ";
            $query .= "     T1.SCHOOLCD = '".SCHOOLCD."' ";
        }

        return $query;
    }

    public function getDateCtrl()
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

    public function getMisyukketuPrt1($model, $toDate, $div = "")
    {
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

    public function getMisyukketuPrt2($model, $toDate, $div = "")
    {
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

    public function getBunsyoKanri()
    {
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

    public function getTuutatu($model, $div = "")
    {
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

    public function getEdboardSchoolcd()
    {
        $query  = " SELECT ";
        $query .= "     KYOUIKU_IINKAI_SCHOOLCD AS EDBOARD_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //開始終了
    public function getSemeDate($seme)
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
    public function getSchool()
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
    public function getUseDb2()
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

    public function getUpdTuutatu($model, $docNum)
    {
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

    //学校名取得
    public function getGakuname($para = "Z010")
    {
        $query .= " SELECT ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "    NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 ='".$para."' ";
        $query .= " AND ";
        $query .= "    NAMECD2 = '00' ";
        
        return $query;
    }

    //権限のある孫を持つ子メニューを基準にして子メニューと親メニューを一気に取得する
    public function getAuth($model, $admingrp_flg, $pastyear)
    {
        $properties = $model->properties;
        $securityCheckT = $properties["useMenuStaffDat"] == "1" ? "SECURITY_CHK_MNU2" : "SECURITY_CHK_MNU";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         MENUID, ";
        $query .= "         SUBMENUID, ";
        $query .= "         PROGRAMMEMO, ";
        $query .= "         MENUNAME{$model->menuField} AS MENUNAME, ";
        $query .= "         PARENTMENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_MST ";
        $query .= "     WHERE ";
        if ($model->ua == "tablet") {
            $query .= " INVALID_FLG='9' AND ";
        }
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "         SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            $query .= "         SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= "         {$securityCheckT}('".STAFFCD."', MENUID,'".CTRL_YEAR."','".SCHOOLKIND."','".SCHOOLCD."') <> '9' AND ";
        } else {
            $query .= "         {$securityCheckT}('".STAFFCD."', MENUID,'".CTRL_YEAR."') <> '9' AND ";
        }
        $query .= "         PARENTMENUID != 'Root' AND ";
        $query .= "         PROGRAMID IS NULL AND ";
        $query .= "         MENUID IN (SELECT DISTINCT ";
        $query .= "                         PARENTMENUID ";
        $query .= "                     FROM ";
        $query .= "                         MENU_MST ";
        $query .= "                     WHERE ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "                         SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            $query .= "                         SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= "                         {$securityCheckT}('".STAFFCD."', MENUID,'".CTRL_YEAR."','".SCHOOLKIND."','".SCHOOLCD."') <> '9' AND ";
        } else {
            $query .= "                         {$securityCheckT}('".STAFFCD."', MENUID,'".CTRL_YEAR."') <> '9' AND ";
        }
        $query .= "                         PARENTMENUID != 'Root' AND ";
        $query .= "                         PROGRAMID IS NOT NULL ";
        if ($admingrp_flg == 0 && $pastyear) {
            $query .= "                     AND PROGRAMID in ('".$properties["useUnAdminMenuPrgid"]."') ";
        }
        $query .= "                     ) ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT ";
        $query .= "                     MENUID as P_MENUID, ";
        $query .= "                     SUBMENUID as P_SUBMENUID, ";
        $query .= "                     PROGRAMMEMO as P_PROGRAMMEMO, ";
        $query .= "                     MENUNAME{$model->menuField} AS P_MENUNAME ";
        $query .= "                 FROM ";
        $query .= "                     MENU_MST ";
        $query .= "                 WHERE ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            $query .= "                     SCHOOLCD = '".SCHOOLCD."' AND ";
        }
        $query .= "                     PARENTMENUID = 'Root' ";
        $query .= "                 ) t2 on t1.PARENTMENUID = t2.P_MENUID ";
        $query .= " ORDER BY ";
        $query .= "     MENUID ";
                
        return $query;
    }
    
    //MENU_STAFF_MSTを読み込む
    public function getAuthSt($model, $admingrp_flg, $pastyear)
    {
        $properties = $model->properties;
        $securityCheckT = "SECURITY_CHK_MNU2";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         MENUID, ";
        $query .= "         SUBMENUID, ";
        $query .= "         PROGRAMMEMO, ";
        $query .= "         MENUNAME, ";
        $query .= "         PARENTMENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_STAFF_MST ";
        $query .= "     WHERE ";
        if ($model->ua == "tablet") {
            $query .= " INVALID_FLG ='9' AND ";
        }
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "         SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            $query .= "         SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= "         {$securityCheckT}('".STAFFCD."', MENUID,'".CTRL_YEAR."','".SCHOOLKIND."','".SCHOOLCD."') <> '9' AND ";
        } else {
            $query .= "         {$securityCheckT}('".STAFFCD."', MENUID,'".CTRL_YEAR."') <> '9' AND ";
        }
        $query .= "         PARENTMENUID != 'Root' AND ";
        $query .= "         PROGRAMID IS  NULL AND ";
        $query .= "         STAFFCD = '".STAFFCD."' AND ";
        $query .= "         MENUID IN (SELECT DISTINCT ";
        $query .= "                         PARENTMENUID ";
        $query .= "                     FROM ";
        $query .= "                         MENU_STAFF_MST ";
        $query .= "                     WHERE ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "                         SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            $query .= "                         SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= "                         {$securityCheckT}('".STAFFCD."', MENUID,'".CTRL_YEAR."','".SCHOOLKIND."','".SCHOOLCD."') <> '9' AND ";
        } else {
            $query .= "                         {$securityCheckT}('".STAFFCD."', MENUID,'".CTRL_YEAR."') <> '9' AND ";
        }
        $query .= "                         PARENTMENUID != 'Root' AND ";
        $query .= "                         PROGRAMID IS NOT NULL AND ";
        $query .= "                         STAFFCD = '".STAFFCD."' ";
        if ($admingrp_flg == 0 && $pastyear) {
            $query .= "                     AND PROGRAMID in ('".$properties["useUnAdminMenuPrgid"]."') ";
        }
        $query .= "                     ) ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT ";
        $query .= "                     MENUID as P_MENUID, ";
        $query .= "                     SUBMENUID as P_SUBMENUID, ";
        $query .= "                     PROGRAMMEMO as P_PROGRAMMEMO, ";
        $query .= "                     MENUNAME AS P_MENUNAME ";
        $query .= "                 FROM ";
        $query .= "                     MENU_STAFF_MST ";
        $query .= "                 WHERE ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            $query .= "                     SCHOOLCD = '".SCHOOLCD."' AND ";
        }
        $query .= "                     PARENTMENUID = 'Root' AND ";
        $query .= "                     STAFFCD = '".STAFFCD."' ";
        $query .= "                 ) t2 on t1.PARENTMENUID = t2.P_MENUID ";
        $query .= " ORDER BY ";
        $query .= "     MENUID ";
        
        return $query;
    }
    
    //SUBメニュー用SQL
    public function getSubmenu($model, $month, $admingrp_flg, $pastyear)
    {
        $properties = $model->properties;
        $securityCheckT = $properties["useMenuStaffDat"] == "1" ? "SECURITY_CHK_MNU2" : "SECURITY_CHK_MNU";
        $query  = " WITH SUB_MST AS ( ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SUBID, ";
        $query .= "         SUBSUBID, ";
        $query .= "         '' AS PROGRAMMEMO, ";
        $query .= "         SUBNAME{$model->menuField} AS SUBNAME, ";
        $query .= "         PARENTSUBID ";
        $query .= "     FROM ";
        $query .= "         MENU_SUB_MST ";
        $query .= "     WHERE ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "         SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            $query .= "         SCHOOLCD = '".SCHOOLCD."' AND ";
        }
        $query .= "         PARENTSUBID != 'Root' AND ";
        $query .= "         MAINID IS  NULL AND ";
        $query .= "         SUBID IN (SELECT DISTINCT ";
        $query .= "                         PARENTSUBID ";
        $query .= "                     FROM ";
        $query .= "                         MENU_SUB_MST ";
        $query .= "                     WHERE ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "                         SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            $query .= "                         SCHOOLCD = '".SCHOOLCD."' AND ";
            $query .= "                         {$securityCheckT}('".STAFFCD."', MAINID,'".CTRL_YEAR."','".SCHOOLKIND."','".SCHOOLCD."') <> '9' AND ";
        } else {
            $query .= "                         {$securityCheckT}('".STAFFCD."', MAINID,'".CTRL_YEAR."') <> '9' AND ";
        }
        $query .= "                         PARENTSUBID != 'Root' AND ";
        $query .= "                         MAINID IS NOT NULL ";
        if ($admingrp_flg == 0 && $pastyear) {
            $query .= "                     AND MAINID in ('".$properties["useUnAdminMenuPrgid"]."') ";
        }
        if ($model->ua == "tablet") {
            $query .= "                     AND MAINID IN ( SELECT MENUID FROM MENU_MST WHERE INVALID_FLG='9')";
        }
        $query .= "                     ) ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT ";
        $query .= "                     SUBID as P_SUBID, ";
        $query .= "                     SUBSUBID as P_SUBSUBID, ";
        $query .= "                     '' as P_PROGRAMMEMO, ";
        $query .= "                     SUBNAME{$model->menuField} AS P_SUBNAME, ";
        $query .= "                     SYORI_MONTH, ";
        $query .= "                     HIDE_FLG ";
        $query .= "                 FROM ";
        $query .= "                     MENU_SUB_MST ";
        $query .= "                 WHERE ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            $query .= "                     SCHOOLCD = '".SCHOOLCD."' AND ";
        }
        $query .= "                     PARENTSUBID = 'Root' ";
        $query .= "                 ) t2 on t1.PARENTSUBID = t2.P_SUBID ";
        $query .= " ORDER BY ";
        $query .= "     SUBID ";
        $query .= " ) ";
        $query .= "  ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.*, ";
        $query .= "         -1 as order ";
        $query .= "     FROM ";
        $query .= "         SUB_MST t1  ";
        $query .= "     WHERE ";
        $query .= "         t1.SYORI_MONTH = 0 AND ";
        $query .= "         t1.HIDE_FLG IS NULL ";
        $query .= "     ) a1 ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.*, ";
        $query .= "         t1.SYORI_MONTH - ".$month." as order ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             SUB_MST  ";
        $query .= "         WHERE ";
        $query .= "             SYORI_MONTH >= ".$month." AND ";
        $query .= "             SYORI_MONTH != 99  ";
        $query .= "         ) t1  ";
        $query .= "     WHERE ";
        $query .= "         t1.HIDE_FLG IS NULL ";
        $query .= "     ) a2 ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.*, ";
        $query .= "         t1.SYORI_MONTH + ".$month."0 as order ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             SUB_MST  ";
        $query .= "         WHERE ";
        $query .= "             SYORI_MONTH < ".$month." AND ";
        $query .= "             SYORI_MONTH != 0  ";
        $query .= "         ) t1 ";
        $query .= "     WHERE ";
        $query .= "         t1.HIDE_FLG IS NULL ";
        $query .= "     ) a3 ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         t1.*, ";
        $query .= "         999 as order ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             SUB_MST  ";
        $query .= "         WHERE ";
        $query .= "             SYORI_MONTH = 99 ";
        $query .= "         ) t1 ";
        $query .= "     WHERE ";
        $query .= "         t1.HIDE_FLG IS NULL  ";
        $query .= "     ) a4 ";
        $query .= " ORDER BY ";
        $query .= "     order , ";
        $query .= "     SUBSUBID, ";
        $query .= "     SUBID ";

        return $query;
    }

    //STAFF_DETAIL_SEQ_MSTのそれぞれの番号取得
    public function getStDetail($field)
    {
        $query  = " SELECT ";
        $query .= "    FIELD".$field;
        $query .= " FROM ";
        $query .= "    STAFF_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "    STAFFCD = '".STAFFCD."' ";
        $query .= " AND ";
        $query .= "    STAFF_SEQ = '001' ";
        
        return $query;
    }
    //学校名取得
    public function getGaku($model)
    {
        $query  = " SELECT ";
        if ($model->menuEnglish != 1) {
            $query .= "    SCHOOLNAME2 ";
        } else {
            $query .= "    SCHOOLNAME_ENG ";
        }
        $query .= " FROM ";
        $query .= "    SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->properties["useSchool_KindMenu"] == "1") {
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
        }
        
        return $query;
    }
    
    //お知らせ取得
    public function getOshirase($model, $properties, $today, $grp)
    {
        $query .= " SELECT ";
        $query .= "    OSHI_T.*, ";
        $query .= "    CASE WHEN NO_DISP.OSHIRASE_NO IS NOT NULL ";
        $query .= "         THEN '1' ";
        $query .= "         ELSE '0' ";
        $query .= "    END AS NOT_DISP_FLG ";
        $query .= " FROM ";
        $query .= "    OSHIRASE_TBL OSHI_T ";
        $query .= "    LEFT JOIN OSHIRASE_NOT_DISP NO_DISP ON OSHI_T.OSHIRASE_NO = NO_DISP.OSHIRASE_NO ";
        $query .= "         AND OSHI_T.DATA_DIV = NO_DISP.DATA_DIV ";
        $query .= "         AND NO_DISP.STAFFCD = '".STAFFCD."' ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "         AND ";
            $query .= "             NO_DISP.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "         AND ";
            $query .= "             NO_DISP.SCHOOLCD = '".SCHOOLCD."' ";
        }
        $query .= " WHERE ";
        $query .= "    ( ";
        $query .= "    OSHI_T.DATA_DIV IN ('98', '99') ";
        $query .= "    OR ";
        $query .= "    EXISTS (SELECT ";
        $query .= "                'x' ";
        $query .= "            FROM ";
        $query .= "                OSHIRASE_TBL t1 ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "                left join OSHIRASE_GRP t2 on t1.DATA_DIV = t2.DATA_DIV and t1.OSHIRASE_NO = t2.OSHIRASE_NO and t1.SCHOOLCD = t2.SCHOOLCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
            $query .= "                left join OSHIRASE_IND t3 on t1.DATA_DIV = t3.DATA_DIV and t1.OSHIRASE_NO = t3.OSHIRASE_NO and t1.SCHOOLCD = t3.SCHOOLCD and t1.SCHOOL_KIND = t3.SCHOOL_KIND ";
        } else {
            $query .= "                left join OSHIRASE_GRP t2 on t1.DATA_DIV = t2.DATA_DIV and t1.OSHIRASE_NO = t2.OSHIRASE_NO ";
            $query .= "                left join OSHIRASE_IND t3 on t1.DATA_DIV = t3.DATA_DIV and t1.OSHIRASE_NO = t3.OSHIRASE_NO ";
        }
        $query .= "            WHERE ";
        $query .= "                t1.START_DATE <= '".$today."' ";
        $query .= "            AND ";
        $query .= "                t1.END_DATE >= '".$today."' ";
        $query .= "            AND ";
        $query .= "               ( t2.GROUP_CD IN ('".$grp."') ";
        $query .= "            OR ";
        $query .= "                t3.STAFFCD = '".STAFFCD."' )";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= "            AND ";
            $query .= "                t1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "            AND ";
            $query .= "                t1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "            AND OSHI_T.SCHOOLCD = t1.SCHOOLCD ";
            $query .= "            AND OSHI_T.SCHOOL_KIND = t1.SCHOOL_KIND ";
        }
        $query .= "            AND OSHI_T.OSHIRASE_NO = t1.OSHIRASE_NO ";
        $query .= "            AND OSHI_T.DATA_DIV = t1.DATA_DIV ";
        $query .= "           ) ";
        $query .= "    ) ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= " AND ";
            $query .= "     OSHI_T.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND ";
            $query .= "     OSHI_T.SCHOOLCD = '".SCHOOLCD."' ";
        }
        if ($model->notDisp == "1") {
            $query .= "    AND NO_DISP.OSHIRASE_NO IS NULL ";
        }

        $query .= " ORDER BY ";
        $query .= "    OSHI_T.DATA_DIV DESC, ";
        $query .= "    OSHI_T.START_DATE, ";
        $query .= "    OSHI_T.END_DATE, ";
        $query .= "    OSHI_T.OSHIRASE_NO ";

        return $query;
    }
    
    //お知らせ非表示削除
    public function notDispDelQuery($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     OSHIRASE_NOT_DISP NO_DISP ";
        $query .= " WHERE ";
        $query .= "     NO_DISP.DATA_DIV = '{$model->dataDiv}' ";
        $query .= "     AND NO_DISP.OSHIRASE_NO = '{$model->oshiraseNo}' ";
        $query .= "     AND NO_DISP.STAFFCD = '".STAFFCD."' ";
        if ($model->properties["useSchool_KindMenu"] == "1") {
            $query .= "     AND ";
            $query .= "         NO_DISP.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "     AND ";
            $query .= "         NO_DISP.SCHOOLCD = '".SCHOOLCD."' ";
        }

        return $query;
    }
    
    //お知らせ非表示作成
    public function notDispInsQuery($model)
    {
        $data = array();
        if ($model->properties["useSchool_KindMenu"] == "1") {
            $data["SCHOOLCD"][TEXT]       = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]    = SCHOOLKIND;
        }
        $data["DATA_DIV"][TEXT]       = $model->dataDiv;
        $data["OSHIRASE_NO"][TEXT]    = $model->oshiraseNo;
        $data["STAFFCD"][TEXT]        = STAFFCD;
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "sysdate()";
        $query = Query::insertSQL($data, "OSHIRASE_NOT_DISP");
        return $query;
    }
    
    //STAFFNAME_SHOWを取得する
    public function getStaffName($staff)
    {
        $query  = " SELECT ";
        $query .= "    STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "    STAFF_MST ";
        $query .= " WHERE ";
        $query .= "    STAFFCD = '".$staff."' ";
        
        return $query;
    }

    //STAFFNAME_ENGを取得，ＮＵＬＬの場合は、STAFFNAME_SHOWを返す
    public function getStaffNameJpEng($staff)
    {
        $query  = " SELECT ";
        $query .= "   VALUE(T1.STAFFNAME, T1.STAFFNAME_SHOW) as STAFFNAME, ";
        $query .= "   VALUE(T1.STAFFNAME_ENG, T1.STAFFNAME) as STAFFNAME_ENG ";
        $query .= " FROM ";
        $query .= "    STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "    T1.STAFFCD = '".$staff."' ";
        
        return $query;
    }

    //ログインした人の所属するグループをとってくる
    public function getUserGroup($properties)
    {
        $query .= " SELECT ";
        $query .= "    GROUPCD ";
        $query .= " FROM ";
        $query .= "    USERGROUP_DAT ";
        $query .= " WHERE ";
        $query .= "    STAFFCD = '".STAFFCD."' ";
        if ($properties["useSchool_KindMenu"] == "1") {
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND ";
            $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
        }
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    GROUPCD DESC ";
        
        return $query;
    }
    
    //ダウンロードファイル取得
    public function getDownLoad($mode = "")
    {
        $query .= " SELECT ";
        if ($mode != "") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'X001' ";
        if ($mode == "") {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }
        
        return $query;
    }
    
    //commom::_updateのかわり
    //件数検索
    public function checkSdsm($where)
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    STAFF_DETAIL_SEQ_MST ";
        $query .= $where;
        
        return $query;
    }
    
    //update
    public function updateQuery($field, $update, $where)
    {
        $query  = " UPDATE ";
        $query .= "    STAFF_DETAIL_SEQ_MST ";
        $query .= " SET ";
        $query .= "    ( ";
        $query .= "     FIELD".$field.", ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= "    ) = ('".$update["FIELD{$field}"]."', ";
        $query .= "         '".$update["REGISTERCD"]."', ";
        $query .= "         ".$update["UPDATED"]." ";
        $query .= "        ) ";
        $query .= $where;
        
        return $query;
    }
    
    //insert
    public function insertQuery($field, $insert)
    {
        $query  = " INSERT INTO STAFF_DETAIL_SEQ_MST ";
        $query .= " ( ";
        $query .= "    STAFFCD, ";
        $query .= "    STAFF_SEQ, ";
        $query .= "    FIELD".$field.", ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= "    '".$insert["STAFFCD"]."', ";
        $query .= "    '".$insert["STAFF_SEQ"]."', ";
        $query .= "    '".$insert["FIELD{$field}"]."', ";
        $query .= "    '".$insert["REGISTERCD"]."', ";
        $query .= "    ".$insert["UPDATED"]." ";
        $query .= " ) ";
        
        return $query;
    }

    //*****************************************
    //20170711　学校切替用追加
    //*****************************************
    //変更可能学校取得
    public function getChangeSchool($div = "")
    {
        $query  = " SELECT ";
        if ($div == "CNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     'http://' || L1.LINK_ADDR || '/remote_login.php' AS VALUE, "; //v(^|^)_
            $query .= "     L1.EDBOARD_SCHOOLNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     EDBOARD_STAFF_CHANGE_SCHOOL_DAT T1 ";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST L1 ON T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".STAFFCD."' ";
        if ($div != "CNT") {
            $query .= " ORDER BY ";
            $query .= "     T1.EDBOARD_SCHOOLCD ";
        }

        return $query;
    }
}
