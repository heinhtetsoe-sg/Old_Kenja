<?php

require_once('for_php7.php');

class knjh400_bukatuQuery extends Query
{
    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    //校種取得
    public function getSchoolKind($model)
    {
        $query .= " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        return $query;
    }
    //学籍部クラブ履歴データよりデータを取得
    public function selectQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= " T3.CLUBCD,T1.CLUBCD || '　' || T1.CLUBNAME AS CLUBNAME";
        $query .= " ,T3.SDATE,T3.EDATE,T3.EXECUTIVECD || '　' || T4.NAME1 AS EXECUTIVECD,T3.REMARK";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " , T3.REMARK1 ";
        }
        $query .= " FROM ";
        $query .= " ( ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    SELECT T001.*, T002.REMARK1 AS REMARK1 ";
            $query .= "    FROM ";
            $query .= "      SCHREG_CLUB_HIST_DAT T001 ";
            $query .= "      LEFT JOIN CLUB_DETAIL_DAT T002 ";
            $query .= "        ON T002.CLUBCD = T001.CLUBCD ";
            $query .= "       AND T002.YEAR = '{$model->year}' ";
            $query .= "       AND T002.SEQ = '001' ";
            $query .= "       AND T002.SCHOOLCD = T001.SCHOOLCD ";
            $query .= "       AND T002.SCHOOL_KIND = T001.SCHOOL_KIND ";
            $query .= "    WHERE T001.SCHREGNO   = '{$model->schregno}' ";
            $query .= "      AND T001.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "      AND T001.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= "    SELECT * ";
            $query .= "    FROM  SCHREG_CLUB_HIST_DAT ";
            $query .= "    WHERE SCHREGNO   = '{$model->schregno}' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ) T3 ";
        $query .= " INNER JOIN ( ";
        $query .= "    SELECT CLUBCD ";
        $query .= "    FROM  CLUB_YDAT ";
        $query .= "    WHERE YEAR    = '{$model->year}'";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ) T2 ON T2.CLUBCD  = T3.CLUBCD ";
        $query .= " INNER JOIN CLUB_MST T1 ON T1.CLUBCD = T3.CLUBCD ";
        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T3.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= "    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " LEFT JOIN ( ";
        $query .= "    SELECT NAMECD2,NAME1 ";
        $query .= "    FROM  NAME_MST ";
        $query .= "    WHERE NAMECD1    = 'J001'";
        $query .= " ) T4 ON T4.NAMECD2  = T3.EXECUTIVECD ";

        return $query;
    }

    //リスト取得
    public function selectQuery2($model)
    {
        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query  = " WITH CLUB AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         CLUB_MST T1, ";
        $query .= "         CLUB_YDAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.CLUBCD = T2.CLUBCD AND ";
        $query .= "         T2.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->schKind."' ";
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ), HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         CLUBCD, ";
        $query .= "         SDATE, ";
        $query .= "         CASE WHEN EDATE IS NULL THEN '9999-12-31' ELSE EDATE END AS EDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '{$model->schregno}' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ), SCH_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         T3.CLUBNAME, ";
        $query .= "         T1.SDATE, ";
        $query .= "         T1.EDATE ";
        $query .= "     FROM ";
        $query .= "         HIST T1 ";
        $query .= "         LEFT JOIN CLUB T3 ON T1.CLUBCD = T3.CLUBCD ";
        $query .= "     WHERE ";
        $query .= "         '".$sdate."' < T1.EDATE AND ";
        $query .= "         '".$edate."' > T1.SDATE ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     T2.CLUBNAME, ";
        $query .= "     T3.HOSTCD || ':' || T3.HOSTNAME AS HOSTCD, ";
        $query .= "     T4.ITEMCD || ':' || T4.ITEMNAME AS ITEMCD, ";
        $query .= "     T5.KINDCD || ':' || T5.KINDNAME AS KINDCD, ";
        $query .= "     T6.RECORDCD || ':' || T6.RECORDNAME AS RECORDCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT T1 ";
        $query .= "     LEFT JOIN SCH_HIST T2 ON T1.CLUBCD = T2.CLUBCD ";
        $query .= "     LEFT JOIN CLUB_HOST_MST T3 ON T1.HOSTCD = T3.HOSTCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T3.SCHOOLCD    = '".SCHOOLCD."' ";
            //(登録時の)生徒のSCHOOL_KINDを利用
            $query .= "    AND T3.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T3.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T3.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T3.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN CLUB_ITEM_MST T4 ON T1.ITEMCD = T4.ITEMCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T4.SCHOOLCD    = '".SCHOOLCD."' ";
            //(登録時の)生徒のSCHOOL_KINDを利用
            $query .= "    AND T4.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T4.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T4.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T4.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T4.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN CLUB_ITEM_KIND_MST T5 ON T1.ITEMCD = T5.ITEMCD AND T1.KINDCD = T5.KINDCD";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T5.SCHOOLCD    = '".SCHOOLCD."' ";
            //(登録時の)生徒のSCHOOL_KINDを利用
            $query .= "    AND T5.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T5.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T5.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T5.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T5.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN CLUB_RECORD_MST T6 ON T1.RECORDCD = T6.RECORDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T6.SCHOOLCD    = '".SCHOOLCD."' ";
            //(登録時の)生徒のSCHOOL_KINDを利用
            $query .= "    AND T6.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T6.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T6.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T6.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     '".$sdate."' <= T1.DETAIL_DATE AND ";
        $query .= "     '".$edate."' >= T1.DETAIL_DATE AND ";
        $query .= "     T1.DETAIL_DATE BETWEEN T2.SDATE AND T2.EDATE ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ ";

        return $query;
    }
}
