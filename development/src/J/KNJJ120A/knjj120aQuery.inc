<?php

require_once('for_php7.php');

class knjj120aQuery extends Query {
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
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND NAME1 IN ('".implode("','", explode(':', $model->multi_schkind))."') ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //部活動校種取得
    function getClubMultiSchoolKind($model, $clubcd) {
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     CLUB_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLCD    = '".SCHOOLCD."' AND ";
        $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        $query .= "     CLUBCD      = '".$clubcd."' AND ";
        $query .= "     SEQ         = '001' ";

        return $query;
    }

    //部クラブ一覧取得
    function getClubList($model, $auth="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLUBCD AS VALUE, ";
        $query .= "     T1.CLUBCD || ' ' || T1.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST T1, ";
        $query .= "     CLUB_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD   = T2.CLUBCD AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        //権限（制限付）
        if ((AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) && $auth) {
            $query .= "     AND EXISTS (SELECT ";
            $query .= "                     'X' ";
            $query .= "                 FROM ";
            $query .= "                     CLUB_ADVISER_DAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR     = T2.YEAR AND ";
            $query .= "                     S1.CLUBCD   = T1.CLUBCD AND ";
            $query .= "                     S1.ADVISER  = '".STAFFCD."' ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= " AND S1.SCHOOLCD     = '".SCHOOLCD."' ";
                $query .= " AND S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND S1.SCHOOLCD     = '".SCHOOLCD."' ";
                $query .= " AND S1.SCHOOL_KIND  = '".$model->schkind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND S1.SCHOOLCD     = '".SCHOOLCD."' ";
                $query .= " AND S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            }
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得
    function getList($model, $div="", $row="") {
        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query  = " WITH KOJIN_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLUBCD, ";
        $query .= "         DETAIL_DATE, ";
        $query .= "         DETAIL_SEQ, ";
        $query .= "         MEET_NAME, ";
        $query .= "         DIV, ";
        $query .= "         GROUPCD, ";
        $query .= "         HOSTCD, ";
        $query .= "         ITEMCD, ";
        $query .= "         KINDCD, ";
        $query .= "         RECORDCD, ";
        $query .= "         DOCUMENT, ";
        $query .= "         DETAIL_REMARK, ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         DETAIL_SCHOOL_KIND, ";
        }
        $query .= "         0 AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HDETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         DIV = '1' AND ";
        $query .= "         DETAIL_DATE BETWEEN '".$sdate."' AND '".$edate."' ";
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
        $query .= " ), GROUP_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         T1.DETAIL_DATE, ";
        $query .= "         INT(T1.GROUPCD) AS DETAIL_SEQ, ";
        $query .= "         T2.MEET_NAME, ";
        $query .= "         '2' AS DIV, ";
        $query .= "         T1.GROUPCD, ";
        $query .= "         T2.HOSTCD, ";
        $query .= "         T2.ITEMCD, ";
        $query .= "         T2.KINDCD, ";
        $query .= "         T2.RECORDCD, ";
        $query .= "         T2.DOCUMENT, ";
        $query .= "         T2.DETAIL_REMARK, ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         T2.DETAIL_SCHOOL_KIND, ";
        }
        $query .= "         T1.CNT ";
        $query .= "     FROM         ";
        $query .= "         (SELECT ";
        $query .= "             CLUBCD, ";
        $query .= "             DETAIL_DATE, ";
        $query .= "             GROUPCD, ";
        $query .= "             MIN(SCHREGNO) AS SCHREGNO, ";
        $query .= "             COUNT(*) - 1 AS CNT ";
        $query .= "         FROM ";
        $query .= "             SCHREG_CLUB_HDETAIL_DAT ";
        $query .= "         WHERE ";
        $query .= "             DIV = '2' AND ";
        $query .= "             DETAIL_DATE BETWEEN '".$sdate."' AND '".$edate."' ";
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
        $query .= "         GROUP BY ";
        $query .= "             CLUBCD, ";
        $query .= "             DETAIL_DATE, ";
        $query .= "             GROUPCD ";
        $query .= "         )  T1, ";
        $query .= "         GROUP_CLUB_HDETAIL_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.CLUBCD       = T2.CLUBCD AND ";
        $query .= "         T1.DETAIL_DATE  = T2.DETAIL_DATE AND ";
        $query .= "         T1.GROUPCD      = T2.GROUPCD ";
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
        $query .= " ), SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T3.HR_NAME, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         LEFT JOIN SCHREG_BASE_MST T2  ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR     = T3.YEAR ";
        $query .= "                                      AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "                                      AND T1.GRADE    = T3.GRADE ";
        $query .= "                                      AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ), SUB_MAIN AS ( ";
        $query .= "     SELECT * FROM KOJIN_HIST ";
        $query .= "     UNION ";
        $query .= "     SELECT * FROM GROUP_HIST ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.MEET_NAME, ";
        $query .= "         T1.DETAIL_DATE, ";
        $query .= "         T1.DETAIL_SEQ, ";
        $query .= "         T1.DIV, ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         T2.CLUBNAME, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         S1.HR_NAME || S1.ATTENDNO || '番　' || S1.NAME_SHOW || ";
        $query .= "             (CASE WHEN T1.DIV = '2' AND T1.CNT > 0 THEN '、他' || RTRIM(CHAR(T1.CNT)) || '名' ELSE '' END) AS SCH_NAME, ";
        $query .= "         T1.HOSTCD || ':' || T3.HOSTNAME AS HOSTCD, ";
        $query .= "         T1.ITEMCD || ':' || T4.ITEMNAME AS ITEMCD, ";
        $query .= "         T1.KINDCD || ':' || T5.KINDNAME AS KINDCD, ";
        $query .= "         T1.RECORDCD || ':' || T6.RECORDNAME AS RECORDCD, ";
        $query .= "         T1.DOCUMENT, ";
        $query .= "         T1.DETAIL_REMARK ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "        ,T1.DETAIL_SCHOOL_KIND ";
        }
        $query .= "     FROM ";
        $query .= "         SUB_MAIN T1 ";
        $query .= "         LEFT JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ";
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
        $query .= "         LEFT JOIN CLUB_HOST_MST T3 ON T1.HOSTCD = T3.HOSTCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T3.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T3.SCHOOL_KIND  = T1.DETAIL_SCHOOL_KIND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T3.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T3.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T3.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T3.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "         LEFT JOIN CLUB_ITEM_MST T4 ON T1.ITEMCD = T4.ITEMCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T4.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T4.SCHOOL_KIND  = T1.DETAIL_SCHOOL_KIND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T4.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T4.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T4.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T4.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "         LEFT JOIN CLUB_ITEM_KIND_MST T5 ON T1.ITEMCD = T5.ITEMCD AND T1.KINDCD = T5.KINDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T5.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T5.SCHOOL_KIND  = T1.DETAIL_SCHOOL_KIND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T5.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T5.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T5.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T5.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "         LEFT JOIN CLUB_RECORD_MST T6 ON T1.RECORDCD = T6.RECORDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T6.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T6.SCHOOL_KIND  = T1.DETAIL_SCHOOL_KIND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T6.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T6.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T6.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T6.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "         ,SCH_INFO S1 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = S1.SCHREGNO ";
        if ($model->clublist) {
            $query .= "         AND T1.CLUBCD = '".$model->clublist."' ";
        }
        $query .= "     ORDER BY ";
        $query .= "         T1.MEET_NAME, ";
        $query .= "         T1.DETAIL_DATE, ";
        $query .= "         T1.DIV, ";
        $query .= "         T1.DETAIL_SEQ, ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ) ";

        if ($div) {
            $query .= " SELECT ";
            $query .= "     COUNT(*)  ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= " WHERE ";
            $query .= "     VALUE(MEET_NAME, '') = '".$row["MEET_NAME"]."' ";
        } else {
            $query .= " SELECT ";
            $query .= "     *  ";
            $query .= " FROM ";
            $query .= "     MAIN ";
        }

        return $query;
    }

    //１レコード取得
    function getRow($model) {
        $date = str_replace("/", "-", $model->date);

        if ($model->div == '2') {
            //団体
            $query  = " SELECT ";
            $query .= "     T1.*, ";
            $query .= "     '2' AS DIV ";
            $query .= " FROM ";
            $query .= "     GROUP_CLUB_HDETAIL_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     CLUBCD      = '".$model->clubcd."' AND ";
            $query .= "     DETAIL_DATE = '".$date."' AND ";
            $query .= "     GROUPCD     = '".$model->seq."' ";
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
        } else {
            //個人
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $query .= "     CLUBCD      = '".$model->clubcd."' AND ";
            $query .= "     DETAIL_DATE = '".$date."' AND ";
            $query .= "     DETAIL_SEQ  =  ".$model->seq." ";
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
        }

        return $query;
    }

    //大会一覧取得
    function getMeetList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     MEET_NAME AS VALUE, ";
        $query .= "     MEET_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "    (SELECT DISTINCT ";
        $query .= "         MEET_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HDETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         REPLACE(MEET_NAME, '　', '') <> '' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND DETAIL_SCHOOL_KIND = '".$model->detail_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         MEET_NAME ";
        $query .= "     UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         MEET_NAME ";
        $query .= "     FROM ";
        $query .= "         GROUP_CLUB_HDETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         REPLACE(MEET_NAME, '　', '') <> '' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND DETAIL_SCHOOL_KIND = '".$model->detail_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     ) T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //開催地域取得
    function getClubHost($model) {
        $query  = " SELECT ";
        $query .= "     HOSTCD || ' ' || HOSTNAME AS LABEL, ";
        $query .= "     HOSTCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_HOST_MST ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->detail_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種目取得
    function getClubItem($model, $clubcd) {
        $query  = " SELECT ";
        $query .= "     T1.ITEMCD || ' ' || T2.ITEMNAME AS LABEL, ";
        $query .= "     T1.ITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_ITEM_MST T2 ON T1.ITEMCD = T2.ITEMCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD = '".$clubcd."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".$model->detail_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種類取得
    function getClubItemKind($model, $itemcd) {
        $query  = " SELECT ";
        $query .= "     KINDCD || ' ' || KINDNAME AS LABEL, ";
        $query .= "     KINDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_KIND_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$itemcd."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->detail_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //成績取得
    function getClubRecord($model) {
        $query  = " SELECT ";
        $query .= "     RECORDCD || ' ' || RECORDNAME AS LABEL, ";
        $query .= "     RECORDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_RECORD_MST ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->detail_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得（個人）
    function getSchInfo($model, $clubcd) {
        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T4.HR_NAME, ";
        $query .= "     T3.NAME_SHOW, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->detail_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3  ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ON T2.YEAR     = T4.YEAR ";
        $query .= "                                  AND T2.SEMESTER = T4.SEMESTER ";
        $query .= "                                  AND T2.GRADE    = T4.GRADE ";
        $query .= "                                  AND T2.HR_CLASS = T4.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.CLUBCD   = '".$clubcd."' AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SDATE    <= '".$edate."' AND ";
        $query .= "     VALUE(T1.EDATE,'9999-12-31') >= '".$sdate."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //生徒一覧取得（団体）
    function getClubSchList($model, $clubcd, $schregno="") {
        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T4.HR_NAME, ";
        $query .= "     T3.NAME_SHOW, ";
        $query .= "     T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->detail_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3  ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ON T2.YEAR     = T4.YEAR ";
        $query .= "                                  AND T2.SEMESTER = T4.SEMESTER ";
        $query .= "                                  AND T2.GRADE    = T4.GRADE ";
        $query .= "                                  AND T2.HR_CLASS = T4.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.CLUBCD   = '".$clubcd."' AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SDATE    <= '".$edate."' AND ";
        $query .= "     VALUE(T1.EDATE,'9999-12-31') >= '".$sdate."' ";
        if ($schregno) {
            $query .= "     AND T1.SCHREGNO = '".$schregno."' ";
        }
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //団体所属生徒取得
    function getGroupSchList($model) {
        $date = str_replace("/", "-", $model->date);

        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->detail_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     ,SCHREG_CLUB_HDETAIL_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T2.CLUBCD       = '".$model->clubcd."' AND ";
        $query .= "     T2.DETAIL_DATE  = '".$date."' AND ";
        $query .= "     T2.DIV          = '2' AND ";
        $query .= "     T2.GROUPCD      = '".$model->seq."' ";
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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //在籍チェック
    function checkClubExist($model, $div="") {
        $date = str_replace("/", "-", $model->field["DETAIL_DATE"]);

        foreach ($model->selectdata as $key) {
            list($gha[], $schregno[]) = preg_split("/-/",$key);
        }

        $query  = " WITH CLUB_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SDATE, ";
        $query .= "         CASE WHEN EDATE IS NULL THEN '9999-12-31' ELSE EDATE END AS EDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HIST_DAT ";
        $query .= "     WHERE ";
        if ($this->field["DIV"] == "1") {
            $query .= "         SCHREGNO = '".$model->field["SCHREGNO"]."' AND ";
        } else {
            $query .= "         SCHREGNO IN ('".implode($schregno, "','")."') AND ";
        }
        $query .= "         CLUBCD   = '".$model->field["CLUBCD"]."' ";
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
        $query .= " ) ";

        if ($div == "") {
            $query .= " SELECT ";
            $query .= "     COUNT(DISTINCT SCHREGNO) ";
            $query .= " FROM ";
            $query .= "     CLUB_HIST ";
            $query .= " WHERE ";
            $query .= " '".$date."' BETWEEN SDATE AND EDATE ";
        } else {
            $query .= " , SCH_INFO AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T3.HR_NAME, ";
            $query .= "         T1.ATTENDNO, ";
            $query .= "         T2.NAME_SHOW ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND = '".$model->detail_schkind."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND = '".$model->schkind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "         LEFT JOIN SCHREG_BASE_MST T2  ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "         LEFT JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR     = T3.YEAR ";
            $query .= "                                      AND T1.SEMESTER = T3.SEMESTER ";
            $query .= "                                      AND T1.GRADE    = T3.GRADE ";
            $query .= "                                      AND T1.HR_CLASS = T3.HR_CLASS ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= " ), MIN_SCH AS ( ";
            $query .= "     SELECT ";
            $query .= "         COUNT(SCHREGNO) - 1 AS CNT, ";
            $query .= "         MIN(SCHREGNO) AS SCHREGNO ";
            $query .= "     FROM ";
            $query .= "         CLUB_HIST ";
            $query .= "     WHERE ";
            $query .= "         SCHREGNO NOT IN (SELECT ";
            $query .= "                             SCHREGNO ";
            $query .= "                         FROM ";
            $query .= "                             CLUB_HIST ";
            $query .= "                         WHERE ";
            $query .= "                             '".$date."' BETWEEN SDATE AND EDATE ";
            $query .= "                         ) ";
            $query .= " ) ";

            $query .= " SELECT ";
            $query .= "     T2.HR_NAME || T2.ATTENDNO || '番　' || T2.NAME_SHOW || ";
            $query .= "         (CASE WHEN T1.CNT > 0 THEN '、他' || RTRIM(CHAR(T1.CNT)) || '名' ELSE '' END) AS NAME ";
            $query .= " FROM ";
            $query .= "     MIN_SCH T1, ";
            $query .= "     SCH_INFO T2 ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        }

        return $query;
    }

    //SEQ取得
    function getSeq($model, $schregno) {
        $date = str_replace("/", "-", $model->field["DETAIL_DATE"]);

        $query  = " SELECT ";
        $query .= "     MAX(DETAIL_SEQ) AS DETAIL_SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".$date."' ";
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

    //INSERT（個人）
    function &getInsertQueryKojin($model) {

        if ($model->cmd == "update") {
            //削除
            knjj120aQuery::getDeleteQueryKojin($model);
            $db = Query::dbCheckOut();
        } else {
            $db = Query::dbCheckOut();
            //SEQ取得
            $seq = $db->getOne(knjj120aQuery::getSeq($model, $model->field["SCHREGNO"]));
            $model->field["DETAIL_SEQ"] = ($seq) ? $seq+1 : 1;
        }

        $data = array();
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
            $data["DETAIL_SCHOOL_KIND"][TEXT] = $model->detail_schkind;
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = $model->schkind;
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        }
        $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];
        $data["CLUBCD"][TEXT]           = $model->field["CLUBCD"];
        $data["DETAIL_DATE"][TEXT]      = str_replace("/", "-", $model->field["DETAIL_DATE"]);
        $data["DETAIL_SEQ"][NUMBER]     = $model->field["DETAIL_SEQ"];
        $data["MEET_NAME"][TEXT]        = $model->field["MEET_NAME"];
        $data["DIV"][TEXT]              = 1;
        $data["GROUPCD"][TEXT]          = "";
        $data["HOSTCD"][TEXT]           = $model->field["HOSTCD"];
        $data["ITEMCD"][TEXT]           = $model->field["ITEMCD"];
        $data["KINDCD"][TEXT]           = $model->field["KINDCD"];
        $data["RECORDCD"][TEXT]         = $model->field["RECORDCD"];
        $data["DOCUMENT"][TEXT]         = $model->field["DOCUMENT"];
        $data["DETAIL_REMARK"][TEXT]    = $model->field["DETAIL_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_CLUB_HDETAIL_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return $model->field["DETAIL_SEQ"];
    }

    //DELETE（個人）
    function &getDeleteQueryKojin($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->field["SCHREGNO"]."' AND ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".str_replace("/", "-", $model->field["DETAIL_DATE"])."' AND ";
        $query .= "     DETAIL_SEQ  = ".$model->field["DETAIL_SEQ"]." ";
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
        return;
    }

    //SEQ取得
    function getSeqDantai($model, $schregno) {
        $date = str_replace("/", "-", $model->field["DETAIL_DATE"]);

        $query  = " SELECT ";
        $query .= "     DETAIL_SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".$date."' AND ";
        $query .= "     DIV         = '2' AND ";
        $query .= "     GROUPCD     = '".$model->field["DETAIL_SEQ"]."' ";
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

    //GROUPCD取得
    function getGroupcd($model) {
        $date = str_replace("/", "-", $model->field["DETAIL_DATE"]);

        $query  = " SELECT ";
        $query .= "     MAX(GROUPCD) AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     GROUP_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".$date."' ";
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

    //INSERT（団体）
    function &getInsertQueryDantai($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->cmd == "update") {
            $temp = array();
            foreach ($model->selectdata as $key) {
                list($gha, $schregno) = preg_split("/-/",$key);
                //SEQ取得
                $seq = $db->getOne(knjj120aQuery::getSeqDantai($model, $schregno));
                $Maxseq = $db->getOne(knjj120aQuery::getSeq($model, $schregno));
                $temp[$schregno] = ($seq) ? $seq : $Maxseq+1;
            }
            //削除
            knjj120aQuery::getDeleteQueryDantai($db, $model);
        }

        foreach ($model->selectdata as $key) {
            list($gha, $schregno) = preg_split("/-/",$key);

            if ($model->cmd == "update") {
                //SEQ
                $seq = $temp[$schregno];
                //GROUPCD
                $groupcd = $model->field["DETAIL_SEQ"];
            } else {
                //SEQ取得
                $seq = $db->getOne(knjj120aQuery::getSeq($model, $schregno));
                $seq = ($seq) ? $seq+1 : 1;
                //GROUPCD取得
                $groupcd = $db->getOne(knjj120aQuery::getGroupcd($model));
                $groupcd = ($groupcd) ? $groupcd+1 : 1;
            }

            // SCHREG_CLUB_HDETAIL_DAT
            $data = array();
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
                $data["DETAIL_SCHOOL_KIND"][TEXT] = $model->detail_schkind;
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                $data["SCHOOL_KIND"][TEXT]  = $model->schkind;
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
            }
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["CLUBCD"][TEXT]           = $model->field["CLUBCD"];
            $data["DETAIL_DATE"][TEXT]      = str_replace("/", "-", $model->field["DETAIL_DATE"]);
            $data["DETAIL_SEQ"][NUMBER]     = $seq;
            $data["MEET_NAME"][TEXT]        = $model->field["MEET_NAME"];
            $data["DIV"][TEXT]              = 2;
            $data["GROUPCD"][TEXT]          = $groupcd;
            $data["HOSTCD"][TEXT]           = $model->field["HOSTCD"];
            $data["ITEMCD"][TEXT]           = $model->field["ITEMCD"];
            $data["KINDCD"][TEXT]           = $model->field["KINDCD"];
            $data["RECORDCD"][TEXT]         = $model->field["RECORDCD"];
            $data["DOCUMENT"][TEXT]         = $model->field["DOCUMENT"];
            $data["DETAIL_REMARK"][TEXT]    = $model->field["DETAIL_REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_CLUB_HDETAIL_DAT");
            $db->query($query);
        }

        // GROUP_CLUB_HDETAIL_DAT
        $data = array();
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
            $data["DETAIL_SCHOOL_KIND"][TEXT] = $model->detail_schkind;
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = $model->schkind;
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        }
        $data["CLUBCD"][TEXT]           = $model->field["CLUBCD"];
        $data["DETAIL_DATE"][TEXT]      = str_replace("/", "-", $model->field["DETAIL_DATE"]);
        $data["GROUPCD"][TEXT]          = $groupcd;
        $data["MEET_NAME"][TEXT]        = $model->field["MEET_NAME"];
        $data["HOSTCD"][TEXT]           = $model->field["HOSTCD"];
        $data["ITEMCD"][TEXT]           = $model->field["ITEMCD"];
        $data["KINDCD"][TEXT]           = $model->field["KINDCD"];
        $data["RECORDCD"][TEXT]         = $model->field["RECORDCD"];
        $data["DOCUMENT"][TEXT]         = $model->field["DOCUMENT"];
        $data["DETAIL_REMARK"][TEXT]    = $model->field["DETAIL_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "GROUP_CLUB_HDETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return $groupcd;
    }

    //DELETE（団体）
    function &getDeleteQueryDantai($db, $model) {

        //SCHREG_CLUB_HDETAIL_DAT
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".str_replace("/", "-", $model->field["DETAIL_DATE"])."' AND ";
        $query .= "     DIV         = '2' AND ";
        $query .= "     GROUPCD     = '".$model->field["DETAIL_SEQ"]."' ";
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

        //GROUP_CLUB_HDETAIL_DAT
        $query  = " DELETE FROM ";
        $query .= "     GROUP_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".str_replace("/", "-", $model->field["DETAIL_DATE"])."' AND ";
        $query .= "     GROUPCD     = '".$model->field["DETAIL_SEQ"]."' ";
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

        return;
    }
}
?>
