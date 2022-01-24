<?php

require_once('for_php7.php');

class knjj040Query extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //部クラブ取得
    function getClubNameList($model) {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD AS CLUBCD, ";
        $query .= "     T1.CLUBCD || '　' || T2.CLUBNAME AS CLUBNAME ";
        $query .= " FROM ";
        $query .= "     CLUB_YDAT T1, ";
        $query .= "     CLUB_MST T2 ";
        //参照・更新可（制限付き
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     INNER JOIN (SELECT ";
            $query .= "                     CLUBCD ";
            $query .= "                 FROM ";
            $query .= "                     CLUB_ADVISER_DAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     ADVISER     = '".STAFFCD."' ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= "                 AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "                 AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "                 AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "                 AND SCHOOL_KIND = '".$model->schkind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                 AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "                 AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "                 ) T3 ON T3.CLUBCD = T2.CLUBCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.CLUBCD   = T2.CLUBCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= " AND T2.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".$model->schkind."' ";
            $query .= " AND T2.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= " AND T2.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //役職区分取得
    function getExecutivecd($model) {
        $query  = " SELECT ";
        $query .= "     M.NAMECD2 AS EXECUTIVECD, ";
        $query .= "     M.NAME1 AS ROLENAME ";
        $query .= " FROM ";
        $query .= "     NAME_YDAT D ";
        $query .= "     INNER JOIN NAME_MST M ";
        $query .= "          ON D.NAMECD1 = M.NAMECD1 ";
        $query .= "         AND D.NAMECD2 = M.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     D.YEAR      = '".CTRL_YEAR."' AND ";
        $query .= "     M.NAMECD1   = 'J001' ";
        $query .= " ORDER BY ";
        $query .= "     M.NAMECD2 ";

        return $query;
    }

    //部員名簿一覧取得
    function getStudentList($model, $selectclub) {
        $query  = " WITH SCHREG_CLUB AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLUBCD, ";
        $query .= "         SDATE, ";
        $query .= "         EDATE, ";
        $query .= "         EXECUTIVECD, ";
        $query .= "         REMARK ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         CLUBCD = '".$selectclub."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ), SCHREG_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.YEAR, ";
        $query .= "         T2.SEMESTER, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.ATTENDNO, ";
        $query .= "         T3.NAME_SHOW, ";
        $query .= "         T3.SEX, ";
        $query .= "         T4.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T2, ";
        $query .= "         SCHREG_REGD_HDAT T4, ";
        $query .= "         SCHREG_BASE_MST T3 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "       , SCHREG_REGD_GDAT T5 ";
        }
        $query .= "     WHERE ";
        $query .= "         T4.YEAR     = T2.YEAR AND ";
        $query .= "         T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T4.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T4.GRADE    = T2.GRADE AND ";
        $query .= "         T4.HR_CLASS = T2.HR_CLASS AND ";
        $query .= "         T3.SCHREGNO = T2.SCHREGNO ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "     AND T5.YEAR         = T4.YEAR ";
            $query .= "     AND T5.GRADE        = T4.GRADE ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T5.YEAR         = T4.YEAR ";
            $query .= "     AND T5.GRADE        = T4.GRADE ";
            $query .= "     AND T5.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T5.YEAR         = T4.YEAR ";
            $query .= "     AND T5.GRADE        = T4.GRADE ";
            $query .= "     AND T5.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T1.EXECUTIVECD, ";
        $query .= "     T1.REMARK, ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB T1 ";
        $query .= "     INNER JOIN SCHREG_REGD T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.SEX, ";
        $query .= "     T1.EXECUTIVECD, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //１レコード取得（学籍部クラブ履歴データ）edit.php
    function getClubHistory_DatEdit($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->gakusekino."' AND ";
        $query .= "     CLUBCD      = '".$model->clubcd."' AND ";
        $query .= "     SDATE       = '".str_replace("/","-",$model->enterdate)."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //今年度の生徒の名前を取得（上のリストで選んだデータの生徒のみ）
    function getStudent_data_One($snendo, $sgakki, $sno) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS SCHREGNO, ";
        $query .= "     T2.NAME_SHOW AS STUDENTNAME, ";
        $query .= "     T3.HR_NAME || '-' || T1.ATTENDNO || '番' AS NENKUMI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T3.YEAR AND ";
        $query .= "     T1.YEAR         = '".$snendo."' AND ";
        $query .= "     T1.SEMESTER     = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = '".$sgakki."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO     = '".$sno."' AND ";
        $query .= "     T1.GRADE        = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS     = T3.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //NAME_MSTから役職名を取得
    function getName_Data($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.NAMECD2, ";
        $query .= "     T1.NAMECD2 || '　' || T1.NAME1 AS NAME ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1, ";
        $query .= "     NAME_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1  = '".$model->namecd1."' AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.NAMECD1  = T1.NAMECD1 AND ";
        $query .= "     T2.NAMECD2  = T1.NAMECD2 ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";

        return $query;
    }

    //在校生検索ボタン
    function getSchregregd_data($model, $ych, $gch, $sch) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS SCHREGNO, ";
        $query .= "     T2.NAME AS STUDENTNAME, ";
        $query .= "     T3.HR_NAME || '-' || T1.ATTENDNO || '番' AS NENKUMI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "      ON T3.YEAR         = G1.YEAR ";
            $query .= "     AND T3.GRADE        = G1.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "      ON T3.YEAR         = G1.YEAR ";
            $query .= "     AND T3.GRADE        = G1.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T3.YEAR AND ";
        $query .= "     T1.YEAR         = '".$ych."' AND ";
        $query .= "     T1.SEMESTER     = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = '".$gch."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO     = '".$sch."' AND ";
        $query .= "     T1.GRADE        = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS     = T3.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //１レコード取得（学籍部クラブ履歴データ）追加・更新・削除時チェック
    function getClubHistory_Dat($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->field["SCHREGNO"]."' AND ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCODEALL"]."' AND ";
        $query .= "     SDATE       = '".str_replace("/","-",$model->field["SDATE"])."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //（学籍番号と校種に矛盾がないか）追加時チェック
    function getSchregKind($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "      ON REGD.YEAR        = GDAT.YEAR ";
        $query .= "     AND REGD.GRADE       = GDAT.GRADE ";
        $query .= "     AND GDAT.SCHOOL_KIND = '".$model->schkind."' ";
        $query .= " WHERE ";
        $query .= "         REGD.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        $query .= "     AND REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //記録詳細存在チェック
    function chkDetailExist($model, $kind) {
        $db = Query::dbCheckOut();

        $query  = " WITH HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLUBCD, ";
        if ($kind == "update") {
            $query .= "         CASE WHEN EDATE IS NULL THEN '9999-12-31' ELSE EDATE END AS EDATE1, ";
            $query .= "         CASE WHEN '".str_replace("/","-",$model->field["EDATE"])."' = '' THEN '9999-12-31' ELSE '".str_replace("/","-",$model->field["EDATE"])."' END AS EDATE2 ";
        } else {
            $query .= "         SDATE, ";
            $query .= "         CASE WHEN EDATE IS NULL THEN '9999-12-31' ELSE EDATE END AS EDATE ";
        }
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO    = '".$model->field["SCHREGNO"]."' AND ";
        $query .= "         CLUBCD      = '".$model->field["CLUBCODEALL"]."' AND ";
        $query .= "         SDATE       = '".str_replace("/","-",$model->field["SDATE"])."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ), DETAIL AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         T2.DETAIL_DATE ";
        $query .= "     FROM ";
        $query .= "         HIST T1, ";
        $query .= "         SCHREG_CLUB_HDETAIL_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.CLUBCD   = T2.CLUBCD AND ";
        if ($kind == "update") {
            $query .= "         T1.EDATE2       < T1.EDATE1 AND ";
            $query .= "         T2.DETAIL_DATE <= T1.EDATE1 AND ";
            $query .= "         T2.DETAIL_DATE  > T1.EDATE2 ";
        } else {
            $query .= "         T2.DETAIL_DATE BETWEEN T1.SDATE AND T1.EDATE ";
        }
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "     AND T2.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= "     AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T2.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= "     AND T2.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T2.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= "     AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.DETAIL_DATE ";
        $query .= " FROM ";
        $query .= "     DETAIL T1 ";
        $query .= " WHERE ";
        $query .= "     T1.DETAIL_DATE NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             S1.DETAIL_DATE ";
        $query .= "         FROM ";
        $query .= "             DETAIL S1, ";
        $query .= "             SCHREG_CLUB_HIST_DAT S2 ";
        $query .= "         WHERE ";
        $query .= "             S1.SCHREGNO = S2.SCHREGNO AND ";
        $query .= "             S1.CLUBCD   = S2.CLUBCD AND ";
        $query .= "             S2.SDATE   <> '".str_replace("/","-",$model->field["SDATE"])."' AND ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " S2.SCHOOLCD     = '".SCHOOLCD."' AND ";
            $query .= " S2.SCHOOL_KIND  = '".SCHOOLKIND."' AND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " S2.SCHOOLCD     = '".SCHOOLCD."' AND ";
            $query .= " S2.SCHOOL_KIND  = '".$model->schkind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " S2.SCHOOLCD     = '".SCHOOLCD."' AND ";
            $query .= " S2.SCHOOL_KIND  = '".SCHOOLKIND."' AND ";
        }
        $query .= "             S1.DETAIL_DATE BETWEEN S2.SDATE AND ";
        $query .= "             CASE WHEN S2.EDATE IS NULL THEN '9999-12-31' ELSE S2.EDATE END) ";

        $cnt = get_count($db->getCol($query));

        Query::dbCheckIn($db);
        return $cnt;
    }

    //--- UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $code = $fields["CLUBCODE1"];

        $data = array();
        $data["SCHREGNO"][TEXT]     = $fields["SCHREGNO"];
        $data["CLUBCD"][TEXT]       = $fields["CLUBCODEALL"];
        $data["SDATE"][TEXT]        = str_replace("/","-",$fields["SDATE"]);
        $data["EDATE"][TEXT]        = str_replace("/","-",$fields["EDATE"]);
        $data["EXECUTIVECD"][TEXT]  = $fields["EXECUTIVECD"];
        $data["REMARK"][TEXT]       = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE SCHREGNO  = '" .$fields["SCHREGNO"] ."' ";
        $where .= "   AND CLUBCD    = '" .$fields["CLUBCODEALL"] ."' ";
        $where .= "   AND SDATE     = '" .str_replace("/","-",$fields["SDATE"]) ."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T2.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T2.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }

        $query = Query::updateSQL($data, "SCHREG_CLUB_HIST_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //--- INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = $model->schkind;
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        }
        $data["SCHREGNO"][TEXT]     = $fields["SCHREGNO"];
        $data["CLUBCD"][TEXT]       = $fields["CLUBCODEALL"];
        $data["SDATE"][TEXT]        = str_replace("/","-",$fields["SDATE"]);
        $data["EDATE"][TEXT]        = str_replace("/","-",$fields["EDATE"]);
        $data["EXECUTIVECD"][TEXT]  = $fields["EXECUTIVECD"];
        $data["REMARK"][TEXT]       = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_CLUB_HIST_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //--- DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $code = $fields["CLUBCODE1"];

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '" .$fields["SCHREGNO"] ."' ";
        $query .= " AND CLUBCD      = '" .$fields["CLUBCODEALL"] ."' ";
        $query .= " AND SDATE       = '" .str_replace("/","-",$fields["SDATE"]) ."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    //存在チェック
    function checkExist($model, $schregno) {
        $sdate = str_replace("/","-",$model->field["SUB_SDATE"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCODEALL"]."' AND ";
        $query .= "     ( EDATE IS NULL OR EDATE > '".$sdate."' OR SDATE = '".$sdate."' ) ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //--- INSERT（入部日付）
    function &getSubInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $schregno = explode(',', $model->field["SUB_SCHREGNO"]);
        $cnt = 0;

        if ($model->field["SUB_SCHREGNO"]) {
            for ($i = 0; $i < get_count($schregno); $i++) {

                //存在チェック
                $check = $db->getOne(knjj040Query::checkExist($model, $schregno[$i]));
                if (!$check) {
                    if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                        $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                        $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
                    } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                        $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                        $data["SCHOOL_KIND"][TEXT]  = $model->schkind;
                    } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                        $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
                    }
                    $data["SCHREGNO"][TEXT]    = $schregno[$i];
                    $data["CLUBCD"][TEXT]      = $model->field["CLUBCODEALL"];
                    $data["SDATE"][TEXT]       = str_replace("/","-",$model->field["SUB_SDATE"]);
                    $data["EDATE"][TEXT]       = "";
                    $data["EXECUTIVECD"][TEXT] = "";
                    $data["REMARK"][TEXT]      = "";
                    $data["REGISTERCD"][TEXT]  = STAFFCD;
                    $data["UPDATED"][NUMBER]   = "sysdate()";

                    $query = Query::insertSQL($data, "SCHREG_CLUB_HIST_DAT");
                    $db->query($query);
                } else {
                    $cnt++;
                    continue;
                }
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $cnt;
    }

    //入学日付取得
    function getSdate($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     SDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCODEALL"]."' AND ";
        $query .= "     EDATE IS NULL ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //--- UPDATE（退部日付）
    function &getSubUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $schregno = explode(',', $model->field["SUB_SCHREGNO"]);
        $edate = str_replace("/","-",$model->field["SUB_EDATE"]);
        $t_cnt = $f_cnt = 0;

        if ($model->field["SUB_SCHREGNO"]) {
            for ($i = 0; $i < get_count($schregno); $i++) {

                $sdate = $db->getCol(knjj040Query::getSdate($model, $schregno[$i]));

                for ($j = 0; $j < get_count($sdate); $j++) {
                    if($sdate[$j] <= $edate){

                        $data["EDATE"][TEXT]       = $edate;
                        $data["REGISTERCD"][TEXT]  = STAFFCD;
                        $data["UPDATED"][NUMBER]   = "sysdate()";

                        $where  = " WHERE SCHREGNO  = '" .$schregno[$i]."' ";
                        $where .= "   AND CLUBCD    = '" .$model->field["CLUBCODEALL"]."' ";
                        $where .= "   AND SDATE     = '" .$sdate[$j]."' ";
                        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                            $where .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
                            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                            $where .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
                            $where .= " AND SCHOOL_KIND = '".$model->schkind."' ";
                        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                            $where .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
                            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                        }
                        $query = Query::updateSQL($data, "SCHREG_CLUB_HIST_DAT", $where);
                        $db->query($query);
                    } else {
                        $f_cnt++;
                    }
                    $t_cnt++;
                }
            }
        }
        $db->commit();      // トランザクションをコミットする。

        if($f_cnt == 0){
            $judgement = "0";   //全て更新
        } else if($t_cnt == $f_cnt){
            $judgement = "1";   //全て失敗
        } else {
            $judgement = "2";   //一部更新
        }

        Query::dbCheckIn($db);
        return $judgement;
    }

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " INNER JOIN CLUB_DETAIL_DAT T3 ";
            $query .= "   ON T3.YEAR = T2.YEAR ";
            $query .= "  AND T3.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "  AND T3.CLUBCD = '".$model->field["CLUBCODEALL"]."' ";
            $query .= "  AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "     AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            //SEQはMAXで指定
            $query .= "  AND T3.SEQ = ( ";
            $query .= "    SELECT MAX(SEQ) ";
            $query .= "    FROM CLUB_DETAIL_DAT T3W ";
            $query .= "    WHERE T3W.YEAR = T2.YEAR AND T3W.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "      AND T3W.SCHOOL_KIND = '".SCHOOLKIND."' AND T3.CLUBCD = '".$model->field["CLUBCODEALL"]."') ";
            //SCHOOL_KINDは、REMARK1に登録されている(文字列長の変化でチェック)事で紐づけ
            $query .= "  AND LENGTH(T3.REMARK1) - LENGTH(REPLACE(T3.REMARK1, T2.SCHOOL_KIND, '')) > 0 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    function getStudent($model, $flg) {
        $schregno = implode("','",explode(',', $model->field["SUB_SCHREGNO"]));

        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.ATTENDNO || '番 ' || T1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        if ($flg == "2") {
            $query .= "     AND T2.SCHREGNO IN ('".$schregno."') ";
        } else {
            $query .= "     AND T2.SCHREGNO NOT IN ('".$schregno."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //対象生徒
    function getClubStudent($model, $flg) {
        $schregno = implode("','",explode(',', $model->field["SUB_SCHREGNO"]));

        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || ':' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T4.HR_NAME || ' ' || T2.ATTENDNO || '番 ' || T3.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ";
        $query .= "          ON T2.YEAR     = T4.YEAR ";
        $query .= "         AND T2.SEMESTER = T4.SEMESTER ";
        $query .= "         AND T2.GRADE    = T4.GRADE ";
        $query .= "         AND T2.HR_CLASS = T4.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "     T1.CLUBCD   = '".$model->field["CLUBCODEALL"]."' AND ";
        $query .= "     T1.EDATE IS NULL ";
        if ($flg == "2") {
            $query .= "     AND T1.SCHREGNO IN ('".$schregno."') ";
        } else {
            $query .= "     AND T1.SCHREGNO NOT IN ('".$schregno."') ";
        }
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "     AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= "     AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= "     AND T1.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= "     AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
