<?php

require_once('for_php7.php');
class knjp851Query extends Query
{

    //年度
    public function getSchoolMstYearQuery()
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    public function getSchkind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ctrlYear}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ctrlYear}' ";
        if ($model->field["SCHOOL_KIND"] != "ALL") {
            $query .= "  AND SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス取得
    public function getHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "      ON T2.YEAR  = T1.YEAR ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '{$model->ctrlYear}' AND ";
        $query .= "     T1.SEMESTER    = '{$model->semester}' ";
        if ($model->field["SCHOOL_KIND"] != "ALL") {
            $query .= " AND    T2.SCHOOL_KIND       = '{$model->field["SCHOOL_KIND"]}' ";
        }
        if ($model->field["GRADE"] != "ALL") {
            $query .= " AND    T1.GRADE       = '{$model->field["GRADE"]}' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND ";
            $query .= "    (T1.TR_CD1      = '".STAFFCD."' OR ";
            $query .= "     T1.TR_CD2      = '".STAFFCD."' OR ";
            $query .= "     T1.TR_CD3      = '".STAFFCD."' OR ";
            $query .= "     T1.SUBTR_CD1   = '".STAFFCD."' OR ";
            $query .= "     T1.SUBTR_CD2   = '".STAFFCD."' OR ";
            $query .= "     T1.SUBTR_CD3   = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //共通クエリ
    public function getSelectCommonQuery($model)
    {
        $query  = " WITH SCHREG_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.YEAR, ";
        $query .= "         MAX(REGD.SEMESTER) AS SEMESTER, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         GDAT.SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "          ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "         AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR            = '{$model->field["YEAR"]}' ";
        if ($model->field["SCHOOL_KIND"] != "ALL") {
            $query .= "         AND GDAT.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        }
        if ($model->field["GRADE"] != "ALL") {
            $query .= "         AND REGD.GRADE       = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["HR_CLASS"] != "ALL") {
            $query .= "         AND REGD.GRADE || '-' || REGD.HR_CLASS    = '{$model->field["HR_CLASS"]}' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         GDAT.SCHOOL_KIND ";
        $query .= " ), SCH_CLUB_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(CASE CLUB_ORDER WHEN '1' THEN CLUBCD ELSE NULL END) AS CLUBCD1, "; //所属している部活を最大2つ表示
        $query .= "         MAX(CASE CLUB_ORDER WHEN '2' THEN CLUBCD ELSE NULL END) AS CLUBCD2  ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "             SELECT ";
        $query .= "                 CL_H.SCHOOLCD, ";
        $query .= "                 CL_H.SCHOOL_KIND, ";
        $query .= "                 CL_H.SCHREGNO, ";
        $query .= "                 CL_H.CLUBCD, ";
        $query .= "                 ROW_NUMBER() OVER (PARTITION BY CL_H.SCHREGNO ORDER BY CL_H.CLUBCD) AS CLUB_ORDER ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_CLUB_HIST_DAT CL_H ";
        $query .= "                 INNER JOIN SCHREG_DATA REGD ";
        $query .= "                     ON REGD.SCHREGNO = CL_H.SCHREGNO ";
        $query .= "             WHERE ";
        $query .= "                 CL_H.SCHOOLCD         = '{$model->schoolcd}' ";
        $query .= "                 AND CL_H.SCHOOL_KIND  = REGD.SCHOOL_KIND ";
        $query .= "                 AND EXISTS ( ";
        $query .= "                     SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         SEMESTER_MST SEMES ";
        $query .= "                     WHERE ";
        $query .= "                         SEMES.YEAR = REGD.YEAR ";
        $query .= "                         AND SEMES.SEMESTER = '9' "; //固定 9:学年末
        $query .= "                         AND ( ";
        $query .= "                             CL_H.SDATE BETWEEN SEMES.SDATE AND SEMES.EDATE ";
        $query .= "                             OR VALUE (CL_H.EDATE, '9999-12-31') BETWEEN SEMES.SDATE AND SEMES.EDATE ";
        $query .= "                             OR (CL_H.SDATE <= SEMES.SDATE AND SEMES.EDATE <= VALUE (CL_H.EDATE, '9999-12-31')) ";
        $query .= "                         ) ";
        $query .= "                 ) ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         SCHREGNO ";
        $query .= " ), COMMON_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         MAIN.SCHREGNO, ";
        $query .= "         CASE GDAT.SCHOOL_KIND ";
        $query .= "             WHEN 'J' THEN '中学' ";
        $query .= "             WHEN 'H' THEN '高校' ";
        $query .= "             END AS SCHOOL_KIND_NAME, ";
        $query .= "         HDAT.HR_NAME, ";
        $query .= "         MAIN.ATTENDNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         BASE.NAME_KANA, ";
        $query .= "         CLUB1.CLUBNAME AS CLUBNAME1, ";
        $query .= "         CLUB2.CLUBNAME AS CLUBNAME2, ";
        $query .= "         CASE GDAT.SCHOOL_KIND ";
        $query .= "             WHEN 'J' THEN SGL_MAP.SIGEL_NO_J ";
        $query .= "             WHEN 'H' THEN SGL_MAP.SIGEL_NO_H ";
        $query .= "             END AS KOJIN_NO, ";
        $query .= "         CASE ";
        $query .= "             WHEN BASE.GRD_DIV IN ('2', '3', '6') ";
        $query .= "                 THEN BASE.GRD_DATE ";
        $query .= "             ELSE NULL ";
        $query .= "             END AS GRD_DATE, ";
        $query .= "         MAIN.GRADE, ";
        $query .= "         MAIN.HR_CLASS ";
        $query .= "     FROM ";
        $query .= "         SCHREG_DATA MAIN ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "             ON BASE.SCHREGNO    = MAIN.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "             ON GDAT.YEAR        = MAIN.YEAR ";
        $query .= "             AND GDAT.GRADE      = MAIN.GRADE ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "             ON HDAT.YEAR        = MAIN.YEAR ";
        $query .= "             AND HDAT.SEMESTER   = MAIN.SEMESTER ";
        $query .= "             AND HDAT.GRADE      = MAIN.GRADE ";
        $query .= "             AND HDAT.HR_CLASS   = MAIN.HR_CLASS ";
        $query .= "         LEFT JOIN SCH_CLUB_DATA SCH_CLUB ";
        $query .= "             ON SCH_CLUB.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "         LEFT JOIN CLUB_MST CLUB1 ";
        $query .= "             ON SCH_CLUB.SCHOOLCD     = CLUB1.SCHOOLCD ";
        $query .= "             AND SCH_CLUB.SCHOOL_KIND = CLUB1.SCHOOL_KIND ";
        $query .= "             AND SCH_CLUB.CLUBCD1     = CLUB1.CLUBCD ";
        $query .= "         LEFT JOIN CLUB_MST CLUB2 ";
        $query .= "             ON SCH_CLUB.SCHOOLCD     = CLUB2.SCHOOLCD ";
        $query .= "             AND SCH_CLUB.SCHOOL_KIND = CLUB2.SCHOOL_KIND ";
        $query .= "             AND SCH_CLUB.CLUBCD2     = CLUB2.CLUBCD ";
        $query .= "         LEFT JOIN COLLECT_SGL_SCHREGNO_MAPPING_DAT SGL_MAP ";
        $query .= "             ON SGL_MAP.SCHREGNO      = MAIN.SCHREGNO ";
        $query .= " ) ";

        return $query;
    }

    //入金計画・実績情報
    public function getSelectPlanPaidQuery($model)
    {
        $query  = knjp851Query::getSelectCommonQuery($model);
        $query .= ",";

        $query .= " COLLECT_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         PM_D.SCHOOLCD, ";
        $query .= "         PM_D.SCHOOL_KIND, ";
        $query .= "         PM_D.YEAR, ";
        $query .= "         PM_D.SCHREGNO, ";
        $query .= "         PM_D.SLIP_NO, ";
        $query .= "         PM_D.COLLECT_L_CD, ";
        $query .= "         PM_D.COLLECT_M_CD, ";
        $query .= "         PM_D.PLAN_YEAR, ";
        $query .= "         PM_D.PLAN_MONTH, ";
        $query .= "         PM_D.PLAN_MONEY, ";
        $query .= "         LIMI.PAID_LIMIT_DATE, ";
        $query .= "         PAID.PLAN_PAID_MONEY_DATE, ";
        $query .= "         PAID.PLAN_PAID_MONEY, ";
        $query .= "         PAID.PLAN_PAID_MONEY_DIV ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_M_DAT PM_D ";
        $query .= "         INNER JOIN SCHREG_DATA SCH_D ";
        $query .= "             ON SCH_D.SCHREGNO      = PM_D.SCHREGNO ";
        $query .= "         LEFT JOIN COLLECT_SLIP_DAT SL_D ";
        $query .= "             ON SL_D.SCHOOLCD        = PM_D.SCHOOLCD ";
        $query .= "             AND SL_D.SCHOOL_KIND    = PM_D.SCHOOL_KIND ";
        $query .= "             AND SL_D.YEAR           = PM_D.YEAR ";
        $query .= "             AND SL_D.SCHREGNO       = PM_D.SCHREGNO ";
        $query .= "             AND SL_D.SLIP_NO        = PM_D.SLIP_NO ";
        $query .= "             AND SL_D.CANCEL_DATE IS NULL ";
        $query .= "         LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMI ";
        $query .= "             ON LIMI.SCHOOLCD        = PM_D.SCHOOLCD ";
        $query .= "             AND LIMI.SCHOOL_KIND    = PM_D.SCHOOL_KIND ";
        $query .= "             AND LIMI.YEAR           <= PM_D.YEAR "; //過年度も取得しておく
        $query .= "             AND LIMI.SCHREGNO       = PM_D.SCHREGNO ";
        $query .= "             AND LIMI.SLIP_NO        = PM_D.SLIP_NO ";
        $query .= "             AND LIMI.PLAN_YEAR      = PM_D.PLAN_YEAR ";
        $query .= "             AND LIMI.PLAN_MONTH     = PM_D.PLAN_MONTH ";
        $query .= "         LEFT JOIN COLLECT_SLIP_PLAN_PAID_M_DAT PAID ";
        $query .= "             ON PAID.SCHOOLCD        = PM_D.SCHOOLCD ";
        $query .= "             AND PAID.SCHOOL_KIND    = PM_D.SCHOOL_KIND ";
        $query .= "             AND PAID.YEAR           = PM_D.YEAR ";
        $query .= "             AND PAID.SCHREGNO       = PM_D.SCHREGNO ";
        $query .= "             AND PAID.SLIP_NO        = PM_D.SLIP_NO ";
        $query .= "             AND PAID.COLLECT_L_CD   = PM_D.COLLECT_L_CD ";
        $query .= "             AND PAID.COLLECT_M_CD   = PM_D.COLLECT_M_CD ";
        $query .= "             AND PAID.PLAN_YEAR      = PM_D.PLAN_YEAR ";
        $query .= "             AND PAID.PLAN_MONTH     = PM_D.PLAN_MONTH ";
        $query .= " ), SCH_BANK_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         BANKCD, ";
        $query .= "         BRANCHCD ";
        $query .= "     FROM ";
        $query .= "         REGISTBANK_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "         AND SEQ  = '1' "; //固定
        $query .= " ), MAIN0 AS (";
        $query .= " SELECT ";
        $query .= "     COMMON.*, ";
        $query .= "     COLLECT.YEAR AS TAISYOU_YEAR, ";
        $query .= "     COLLECT.PLAN_YEAR, ";
        $query .= "     COLLECT.PLAN_MONTH, ";
        $query .= "     COLLECT.SLIP_NO, ";
        $query .= "     COLLECT.COLLECT_L_CD, ";
        $query .= "     CLMST.COLLECT_L_NAME, ";
        $query .= "     COLLECT.COLLECT_M_CD, ";
        $query .= "     CMMST.COLLECT_M_NAME, ";
        $query .= "     COLLECT.PAID_LIMIT_DATE, ";
        $query .= "     COLLECT.PLAN_MONEY, ";
        $query .= "     COLLECT.PLAN_PAID_MONEY_DATE, ";
        $query .= "     COLLECT.PLAN_PAID_MONEY, ";
        $query .= "     COLLECT.PLAN_PAID_MONEY_DIV, ";
        $query .= "     P004.NAME1 AS PLAN_PAID_MONEY_DIV_NAME, ";
        $query .= "     BANK.BANKCD, ";
        $query .= "     BANK.BANKNAME, ";
        $query .= "     (CASE WHEN CMMST.REDUCTION_DIV = '1' THEN REDCP.DECISION_MONEY ELSE 0 END) AS KIHON_MONEY, ";
        $query .= "     (CASE WHEN CMMST.REDUCTION_DIV = '1' THEN REDCP.ADD_DECISION_MONEY ELSE 0 END) AS KASAN_MONEY, ";
        $query .= "     REDP.DECISION_MONEY AS HOJYO_MONEY, ";
        $query .= "     REDSP.DECISION_MONEY AS GENMEN_MONEY, ";
        $query .= "     BURDEN.BURDEN_CHARGE ";
        $query .= " FROM ";
        $query .= "     COMMON_DATA COMMON ";
        $query .= "     LEFT JOIN COLLECT_DATA COLLECT ";
        $query .= "         ON COLLECT.SCHREGNO     = COMMON.SCHREGNO ";
        $query .= "     LEFT JOIN COLLECT_L_MST CLMST ";
        $query .= "         ON CLMST.SCHOOLCD        = COLLECT.SCHOOLCD ";
        $query .= "         AND CLMST.SCHOOL_KIND    = COLLECT.SCHOOL_KIND ";
        $query .= "         AND CLMST.YEAR           = COLLECT.YEAR ";
        $query .= "         AND CLMST.COLLECT_L_CD   = COLLECT.COLLECT_L_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST CMMST ";
        $query .= "         ON CMMST.SCHOOLCD        = COLLECT.SCHOOLCD ";
        $query .= "         AND CMMST.SCHOOL_KIND    = COLLECT.SCHOOL_KIND ";
        $query .= "         AND CMMST.YEAR           = COLLECT.YEAR ";
        $query .= "         AND CMMST.COLLECT_L_CD   = COLLECT.COLLECT_L_CD ";
        $query .= "         AND CMMST.COLLECT_M_CD   = COLLECT.COLLECT_M_CD ";
        $query .= "     LEFT JOIN V_NAME_MST P004 ";
        $query .= "         ON P004.YEAR = COLLECT.YEAR ";
        $query .= "         AND P004.NAMECD1        = 'P004' ";
        $query .= "         AND P004.NAMECD2        = COLLECT.PLAN_PAID_MONEY_DIV ";
        $query .= "     LEFT JOIN SCH_BANK_DATA SCH_BANK ";
        $query .= "         ON SCH_BANK.SCHOOLCD    = COLLECT.SCHOOLCD ";
        $query .= "         AND SCH_BANK.SCHREGNO   = COLLECT.SCHREGNO ";
        $query .= "     LEFT JOIN BANK_MST BANK ";
        $query .= "         ON BANK.BANKCD          = SCH_BANK.BANKCD ";
        $query .= "         AND BANK.BRANCHCD       = SCH_BANK.BRANCHCD ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDCP ";
        $query .= "         ON REDCP.SCHOOLCD       = COLLECT.SCHOOLCD ";
        $query .= "         AND REDCP.SCHOOL_KIND   = COLLECT.SCHOOL_KIND ";
        $query .= "         AND REDCP.YEAR          = COLLECT.YEAR ";
        $query .= "         AND REDCP.SLIP_NO       = COLLECT.SLIP_NO ";
        $query .= "         AND REDCP.PLAN_YEAR     = COLLECT.PLAN_YEAR ";
        $query .= "         AND REDCP.PLAN_MONTH    = COLLECT.PLAN_MONTH ";
        $query .= "         AND REDCP.SCHREGNO      = COLLECT.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_PLAN_DAT REDP ";
        $query .= "         ON REDP.SCHOOLCD            = COLLECT.SCHOOLCD ";
        $query .= "         AND REDP.SCHOOL_KIND        = COLLECT.SCHOOL_KIND ";
        $query .= "         AND REDP.YEAR               = COLLECT.YEAR ";
        $query .= "         AND REDP.REDUCTION_TARGET   = CMMST.GAKUNOKIN_DIV ";
        $query .= "         AND REDP.SLIP_NO            = COLLECT.SLIP_NO ";
        $query .= "         AND REDP.PLAN_YEAR          = COLLECT.PLAN_YEAR ";
        $query .= "         AND REDP.PLAN_MONTH         = COLLECT.PLAN_MONTH ";
        $query .= "         AND REDP.SCHREGNO           = COLLECT.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT REDSP ";
        $query .= "         ON REDSP.SCHOOLCD           = COLLECT.SCHOOLCD ";
        $query .= "         AND REDSP.SCHOOL_KIND       = COLLECT.SCHOOL_KIND ";
        $query .= "         AND REDSP.YEAR              = COLLECT.YEAR ";
        $query .= "         AND REDSP.REDUCTION_TARGET  = CMMST.GAKUNOKIN_DIV ";
        $query .= "         AND REDSP.SLIP_NO           = COLLECT.SLIP_NO ";
        $query .= "         AND REDSP.PLAN_YEAR         = COLLECT.PLAN_YEAR ";
        $query .= "         AND REDSP.PLAN_MONTH        = COLLECT.PLAN_MONTH ";
        $query .= "         AND REDSP.SCHREGNO          = COLLECT.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN ";
        $query .= "         ON BURDEN.SCHOOLCD           = COLLECT.SCHOOLCD ";
        $query .= "         AND BURDEN.SCHOOL_KIND       = COLLECT.SCHOOL_KIND ";
        $query .= "         AND BURDEN.YEAR              = COLLECT.YEAR ";
        $query .= "         AND BURDEN.REDUCTION_TARGET  = CMMST.GAKUNOKIN_DIV ";
        $query .= "         AND BURDEN.SLIP_NO           = COLLECT.SLIP_NO ";
        $query .= "         AND BURDEN.SCHREGNO          = COLLECT.SCHREGNO ";
        $query .= "         AND BURDEN.PLAN_YEAR         = COLLECT.PLAN_YEAR ";
        $query .= "         AND BURDEN.PLAN_MONTH        = COLLECT.PLAN_MONTH ";
        $query .= " WHERE ";
        $query .= "     VALUE(COLLECT.PLAN_MONEY, 0) > 0 ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         VALUE (T1.KIHON_MONEY, 0) + VALUE (T1.KASAN_MONEY, 0) + VALUE (T1.HOJYO_MONEY, 0) + VALUE(T1.GENMEN_MONEY, 0) SIEN_TOTAL_MONEY, "; //※学校負担金は支援金合計に含まない
        $query .= "         VALUE(T1.PLAN_MONEY, 0) - VALUE (T1.KIHON_MONEY, 0) - VALUE (T1.KASAN_MONEY, 0) - VALUE (T1.HOJYO_MONEY, 0) - VALUE(T1.GENMEN_MONEY, 0) - VALUE(T1.BURDEN_CHARGE, 0) AS HUTAN_MONEYFROM ";
        $query .= "     FROM ";
        $query .= "         MAIN0 T1";
        $query .= " ), CSV_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.PLAN_YEAR || '-' || T1.PLAN_MONTH AS PLAN_YEAR_MONTH, ";
        $query .= "     FISCALYEAR(T1.PLAN_YEAR || '-' || T1.PLAN_MONTH || '-01') AS DISP_YEAR, ";
        $query .= "     CASE ";
        $query .= "         WHEN T1.HUTAN_MONEYFROM > 0 THEN T1.HUTAN_MONEYFROM ";
        $query .= "         ELSE 0 ";
        $query .= "     END AS HUTAN_MONEY, ";
        $query .= "     CASE ";
        $query .= "         WHEN T1.PLAN_PAID_MONEY IS NULL THEN NULL ";
        $query .= "         WHEN T1.HUTAN_MONEYFROM > 0 THEN T1.HUTAN_MONEYFROM ";
        $query .= "         ELSE 0 ";
        $query .= "     END AS REDUCED_PLAN_PAID_MONEY, ";
        $query .= "     ROW_NUMBER() OVER(PARTITION BY SCHREGNO ORDER BY T1.PLAN_YEAR, T1.PLAN_MONTH, T1.PLAN_PAID_MONEY_DATE, T1.COLLECT_L_CD, T1.COLLECT_M_CD, T1.SLIP_NO) AS SCH_ROW_SORT "; //同学籍番号内でのソート順
        $query .= " FROM ";
        $query .= "     MAIN T1";
        $query .= "     WHERE ";
        if ($model->field["DATE_DIV"] == "1") { //徴収日
            $query .= "         T1.TAISYOU_YEAR = '{$model->field["YEAR"]}' ";
            $query .= "     AND MONTH(T1.PAID_LIMIT_DATE) IN ('".implode("','", $model->selectedMonth)."') ";
            if ($model->field["DATE_CHK1"] == "1") { //未納のみ出力
                $query .= "     AND T1.PLAN_PAID_MONEY_DATE IS NULL ";
                $query .= "     AND (CASE WHEN T1.HUTAN_MONEYFROM > 0 THEN T1.HUTAN_MONEYFROM ELSE 0 END) > 0 ";
            }
        } else { //入金日
            if ($model->field["DATE_CHK2"] == "1") {
                $query .= "        FISCALYEAR(T1.PLAN_PAID_MONEY_DATE) = '{$model->field["YEAR"]}' "; //入金日付の年度が対象
            } else {
                $query .= "        T1.TAISYOU_YEAR = '{$model->field["YEAR"]}' "; //伝票データの登録年度が対象
            }
            $query .= "     AND MONTH(T1.PLAN_PAID_MONEY_DATE) IN ('".implode("','", $model->selectedMonth)."') ";
        }
        $query .= " ) ";
        if ($model->field["SUM_CHK"] == "1") { //合計出力
            $query .= "  SELECT ";
            $query .= "      SCHREGNO, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN SCHOOL_KIND_NAME ELSE NULL END ) AS SCHOOL_KIND_NAME, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN HR_NAME ELSE NULL END ) AS HR_NAME, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN ATTENDNO ELSE NULL END ) AS ATTENDNO, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN NAME ELSE NULL END ) AS NAME, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN NAME_KANA ELSE NULL END ) AS NAME_KANA, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN CLUBNAME1 ELSE NULL END ) AS CLUBNAME1, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN CLUBNAME2 ELSE NULL END ) AS CLUBNAME2, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN KOJIN_NO ELSE NULL END ) AS KOJIN_NO, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN GRD_DATE ELSE NULL END ) AS GRD_DATE, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN DISP_YEAR ELSE NULL END ) AS DISP_YEAR, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN PLAN_YEAR_MONTH ELSE NULL END ) AS PLAN_YEAR_MONTH, ";
            $query .= "      SUM(PLAN_MONEY) AS PLAN_MONEY, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN PLAN_PAID_MONEY_DATE ELSE NULL END ) AS PLAN_PAID_MONEY_DATE, ";
            $query .= "      SUM(PLAN_PAID_MONEY) AS PLAN_PAID_MONEY, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN PLAN_PAID_MONEY_DIV ELSE NULL END ) AS PLAN_PAID_MONEY_DIV, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN PLAN_PAID_MONEY_DIV_NAME ELSE NULL END ) AS PLAN_PAID_MONEY_DIV_NAME, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN BANKCD ELSE NULL END ) AS BANKCD, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN BANKNAME ELSE NULL END ) AS BANKNAME, ";
            $query .= "      SUM(KIHON_MONEY) AS KIHON_MONEY, ";
            $query .= "      SUM(KASAN_MONEY) AS KASAN_MONEY, ";
            $query .= "      SUM(HOJYO_MONEY) AS HOJYO_MONEY, ";
            $query .= "      SUM(GENMEN_MONEY) AS GENMEN_MONEY, ";
            $query .= "      SUM(SIEN_TOTAL_MONEY) AS SIEN_TOTAL_MONEY, ";
            $query .= "      SUM(BURDEN_CHARGE) AS BURDEN_CHARGE, ";
            $query .= "      SUM(HUTAN_MONEY) AS HUTAN_MONEY, ";
            $query .= "      SUM(REDUCED_PLAN_PAID_MONEY) AS REDUCED_PLAN_PAID_MONEY, ";
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN GRADE ELSE NULL END ) AS GRADE, ";       //ソート用
            $query .= "      MAX(CASE SCH_ROW_SORT WHEN '1' THEN HR_CLASS ELSE NULL END ) AS HR_CLASS "; //ソート用
            $query .= "  FROM ";
            $query .= "      CSV_DATA ";
            $query .= "  GROUP BY ";
            $query .= "      SCHREGNO ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.* ";
            $query .= " FROM ";
            $query .= "     CSV_DATA T1";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    //減免情報
    public function getSelectReductionQuery($model)
    {
        $query  = knjp851Query::getSelectCommonQuery($model);

        $query .= " SELECT ";
        $query .= "     COMMON.*, ";
        $query .= "     SHIST.SCHOLARSHIP, ";
        $query .= "     RMST.REDUCTION_DIV_NAME, ";
        $query .= "     SHIST.FROM_DATE, ";
        $query .= "     SHIST.TO_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT SHIST ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_MST RMST ";
        $query .= "         ON RMST.SCHOOLCD     = SHIST.SCHOOLCD ";
        $query .= "         AND RMST.SCHOOL_KIND = SHIST.SCHOOL_KIND ";
        $query .= "         AND RMST.YEAR        = '{$model->field["YEAR"]}' ";
        $query .= "         AND RMST.REDUCTION_DIV_CD = SHIST.SCHOLARSHIP ";
        $query .= "     INNER JOIN COMMON_DATA COMMON ";
        $query .= "          ON SHIST.SCHREGNO = COMMON.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     '{$model->field["YEAR"]}' BETWEEN FISCALYEAR(SHIST.FROM_DATE) AND VALUE(FISCALYEAR(SHIST.TO_DATE), '9999') ";
        $query .= " ORDER BY ";
        $query .= "     COMMON.GRADE, ";
        $query .= "     COMMON.HR_CLASS, ";
        $query .= "     COMMON.ATTENDNO, ";
        $query .= "     SHIST.FROM_DATE, ";
        $query .= "     SHIST.SCHOLARSHIP ";

        return $query;
    }

    //寮情報
    public function getSelectDomitoryQuery($model)
    {
        $query  = knjp851Query::getSelectCommonQuery($model);

        $query .= " SELECT ";
        $query .= "     COMMON.*, ";
        $query .= "     DHIST.DOMI_CD, ";
        $query .= "     DMST.DOMI_NAME, ";
        $query .= "     DHIST.DOMI_ENTDAY, ";
        $query .= "     DHIST.DOMI_OUTDAY, ";
        $query .= "     DTMP.MONTH AS TMP_OUT_MONTH";
        $query .= " FROM ";
        $query .= "     SCHREG_DOMITORY_HIST_DAT DHIST ";
        $query .= "     LEFT JOIN DOMITORY_MST DMST ";
        $query .= "          ON DMST.DOMI_CD  = DHIST.DOMI_CD ";
        $query .= "     LEFT JOIN SCHREG_DOMITORY_TEMPORARY_OUT_DAT DTMP ";
        $query .= "          ON DTMP.YEAR     = '{$model->field["YEAR"]}' ";
        $query .= "         AND DTMP.SCHREGNO = DHIST.SCHREGNO ";
        $query .= "     INNER JOIN COMMON_DATA COMMON ";
        $query .= "          ON DHIST.SCHREGNO = COMMON.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     '{$model->field["YEAR"]}' BETWEEN FISCALYEAR(DHIST.DOMI_ENTDAY) AND VALUE(FISCALYEAR(DHIST.DOMI_OUTDAY), '9999') ";
        $query .= " ORDER BY ";
        $query .= "     COMMON.GRADE, ";
        $query .= "     COMMON.HR_CLASS, ";
        $query .= "     COMMON.ATTENDNO, ";
        $query .= "     DHIST.DOMI_ENTDAY, ";
        $query .= "     DHIST.DOMI_CD ";

        return $query;
    }

    //学校徴収金（預り金）情報 収入
    public function getSelectLevyIncomeQuery($model)
    {
        $query  = knjp851Query::getSelectCommonQuery($model);

        $query .= " SELECT ";
        $query .= "     COMMON.*, ";
        $query .= "     INCOME.YEAR, ";
        $query .= "     SCH_INCOME.REQUEST_NO, ";
        $query .= "     INCOME.REQUEST_DATE, ";
        $query .= "     INCOME.INCOME_DATE, ";
        $query .= "     INCOME.INCOME_APPROVAL, ";
        $query .= "     INCOME.COLLECT_L_CD, ";
        $query .= "     CLMST.COLLECT_L_NAME, ";
        $query .= "     INCOME.COLLECT_M_CD, ";
        $query .= "     CMMST.COLLECT_M_NAME, ";
        $query .= "     SCH_INCOME.INCOME_L_CD, ";
        $query .= "     LLMST.LEVY_L_NAME, ";
        $query .= "     SCH_INCOME.INCOME_M_CD, ";
        $query .= "     LMMST.LEVY_M_NAME, ";
        $query .= "     INCOME.REQUEST_STAFF, ";
        $query .= "     STF.STAFFNAME, ";
        $query .= "     INCOME.REQUEST_GK, ";
        $query .= "     INCOME.REQUEST_REASON, ";
        $query .= "     SCH_INCOME.LINE_NO, ";
        $query .= "     MEISAI.COMMODITY_NAME, ";
        $query .= "     MEISAI.COMMODITY_PRICE, ";
        $query .= "     MEISAI.COMMODITY_CNT, ";
        $query .= "     MEISAI.TOTAL_PRICE ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_SCHREG_DAT SCH_INCOME ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_MEISAI_DAT MEISAI ";
        $query .= "         ON MEISAI.YEAR = SCH_INCOME.YEAR ";
        $query .= "         AND MEISAI.SCHOOLCD = SCH_INCOME.SCHOOLCD ";
        $query .= "         AND MEISAI.SCHOOL_KIND = SCH_INCOME.SCHOOL_KIND ";
        $query .= "         AND MEISAI.INCOME_L_CD = SCH_INCOME.INCOME_L_CD ";
        $query .= "         AND MEISAI.INCOME_M_CD = SCH_INCOME.INCOME_M_CD ";
        $query .= "         AND MEISAI.REQUEST_NO = SCH_INCOME.REQUEST_NO ";
        $query .= "         AND MEISAI.INCOME_S_CD = SCH_INCOME.INCOME_S_CD ";
        $query .= "         AND MEISAI.LINE_NO = SCH_INCOME.LINE_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_DAT INCOME ";
        $query .= "         ON INCOME.YEAR = MEISAI.YEAR ";
        $query .= "         AND INCOME.SCHOOLCD = MEISAI.SCHOOLCD ";
        $query .= "         AND INCOME.SCHOOL_KIND = MEISAI.SCHOOL_KIND ";
        $query .= "         AND INCOME.INCOME_L_CD = MEISAI.INCOME_L_CD ";
        $query .= "         AND INCOME.INCOME_M_CD = MEISAI.INCOME_M_CD ";
        $query .= "         AND INCOME.REQUEST_NO = MEISAI.REQUEST_NO ";
        $query .= "     LEFT JOIN COLLECT_L_MST CLMST ";
        $query .= "         ON CLMST.SCHOOLCD = INCOME.SCHOOLCD ";
        $query .= "         AND CLMST.SCHOOL_KIND = INCOME.SCHOOL_KIND ";
        $query .= "         AND CLMST.YEAR = INCOME.YEAR ";
        $query .= "         AND CLMST.COLLECT_L_CD = INCOME.COLLECT_L_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST CMMST ";
        $query .= "         ON CMMST.SCHOOLCD = INCOME.SCHOOLCD ";
        $query .= "         AND CMMST.SCHOOL_KIND = INCOME.SCHOOL_KIND ";
        $query .= "         AND CMMST.YEAR = INCOME.YEAR ";
        $query .= "         AND CMMST.COLLECT_L_CD = INCOME.COLLECT_L_CD ";
        $query .= "         AND CMMST.COLLECT_M_CD = INCOME.COLLECT_M_CD ";
        $query .= "     LEFT JOIN LEVY_L_MST LLMST ";
        $query .= "         ON LLMST.SCHOOLCD = INCOME.SCHOOLCD ";
        $query .= "         AND LLMST.SCHOOL_KIND = INCOME.SCHOOL_KIND ";
        $query .= "         AND LLMST.YEAR = INCOME.YEAR ";
        $query .= "         AND LLMST.LEVY_L_CD = INCOME.INCOME_L_CD ";
        $query .= "     LEFT JOIN LEVY_M_MST LMMST ";
        $query .= "         ON LMMST.SCHOOLCD = INCOME.SCHOOLCD ";
        $query .= "         AND LMMST.SCHOOL_KIND = INCOME.SCHOOL_KIND ";
        $query .= "         AND LMMST.YEAR = INCOME.YEAR ";
        $query .= "         AND LMMST.LEVY_L_CD = INCOME.INCOME_L_CD ";
        $query .= "         AND LMMST.LEVY_M_CD = INCOME.INCOME_M_CD ";
        $query .= "     LEFT JOIN STAFF_MST STF ";
        $query .= "         ON STF.STAFFCD = INCOME.REQUEST_STAFF ";
        $query .= "     INNER JOIN COMMON_DATA COMMON ";
        $query .= "          ON SCH_INCOME.SCHREGNO = COMMON.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     INCOME.SCHOOLCD          = '{$model->schoolcd}' ";
        if ($model->field["SCHOOL_KIND"] != "ALL") {
            $query .= "        AND INCOME.SCHOOL_KIND   = '{$model->field["SCHOOL_KIND"]}' ";
        }
        $query .= "        AND INCOME.YEAR          = '{$model->field["YEAR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     COMMON.GRADE, ";
        $query .= "     COMMON.HR_CLASS, ";
        $query .= "     COMMON.ATTENDNO, ";
        $query .= "     SCH_INCOME.REQUEST_NO, ";
        $query .= "     SCH_INCOME.INCOME_L_CD, ";
        $query .= "     SCH_INCOME.INCOME_M_CD, ";
        $query .= "     SCH_INCOME.LINE_NO ";

        return $query;
    }

    //学校徴収金（預り金）情報 支出
    public function getSelectLevyOutgoQuery($model)
    {
        $query  = knjp851Query::getSelectCommonQuery($model);

        $query .= " SELECT ";
        $query .= "     COMMON.*, ";
        $query .= "     OUTGO.YEAR, ";
        $query .= "     OUTGO.REQUEST_NO, ";
        $query .= "     OUTGO.REQUEST_DATE, ";
        $query .= "     OUTGO.OUTGO_DATE, ";
        $query .= "     OUTGO.OUTGO_APPROVAL, ";
        $query .= "     OUTGO.INCOME_L_CD, ";
        $query .= "     LLMST1.LEVY_L_NAME AS INCOME_L_NAME, ";
        $query .= "     OUTGO.INCOME_M_CD, ";
        $query .= "     LMMST1.LEVY_M_NAME AS INCOME_M_NAME, ";
        $query .= "     SCH_OUTGO.OUTGO_L_CD, ";
        $query .= "     LLMST2.LEVY_L_NAME AS OUTGO_L_NAME, ";
        $query .= "     SCH_OUTGO.OUTGO_M_CD, ";
        $query .= "     LMMST2.LEVY_M_NAME AS OUTGO_M_NAME, ";
        $query .= "     MEISAI.OUTGO_S_CD, ";
        $query .= "     LSMST.LEVY_S_NAME AS OUTGO_S_NAME, ";
        $query .= "     OUTGO.REQUEST_STAFF, ";
        $query .= "     STF.STAFFNAME, ";
        $query .= "     OUTGO.REQUEST_GK, ";
        $query .= "     OUTGO.REQUEST_REASON, ";
        $query .= "     MEISAI.LINE_NO, ";
        $query .= "     LSMST.LEVY_S_NAME, ";
        $query .= "     MEISAI.COMMODITY_PRICE, ";
        $query .= "     MEISAI.COMMODITY_CNT, ";
        $query .= "     MEISAI.TOTAL_PRICE, ";
        $query .= "     MEISAI.TOTAL_TAX, ";
        $query .= "     OUTGO.TRADER_CD, ";
        $query .= "     OUTGO.TRADER_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT SCH_OUTGO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_MEISAI_DAT MEISAI ";
        $query .= "         ON MEISAI.YEAR = SCH_OUTGO.YEAR ";
        $query .= "         AND MEISAI.SCHOOLCD = SCH_OUTGO.SCHOOLCD ";
        $query .= "         AND MEISAI.SCHOOL_KIND = SCH_OUTGO.SCHOOL_KIND ";
        $query .= "         AND MEISAI.LINE_NO = SCH_OUTGO.LINE_NO ";
        $query .= "         AND MEISAI.OUTGO_L_CD = SCH_OUTGO.OUTGO_L_CD ";
        $query .= "         AND MEISAI.OUTGO_M_CD = SCH_OUTGO.OUTGO_M_CD ";
        $query .= "         AND MEISAI.OUTGO_S_CD = SCH_OUTGO.OUTGO_S_CD ";
        $query .= "         AND MEISAI.REQUEST_NO = SCH_OUTGO.REQUEST_NO ";
        $query .= "         AND MEISAI.LINE_NO = SCH_OUTGO.LINE_NO ";
        $query .= "     INNER JOIN LEVY_REQUEST_OUTGO_DAT OUTGO ";
        $query .= "         ON OUTGO.YEAR = MEISAI.YEAR ";
        $query .= "         AND OUTGO.SCHOOLCD = MEISAI.SCHOOLCD ";
        $query .= "         AND OUTGO.SCHOOL_KIND = MEISAI.SCHOOL_KIND ";
        $query .= "         AND OUTGO.OUTGO_L_CD = MEISAI.OUTGO_L_CD ";
        $query .= "         AND OUTGO.OUTGO_M_CD = MEISAI.OUTGO_M_CD ";
        $query .= "         AND OUTGO.REQUEST_NO = MEISAI.REQUEST_NO ";
        $query .= "     LEFT JOIN LEVY_L_MST LLMST1 ";
        $query .= "         ON LLMST1.SCHOOLCD = OUTGO.SCHOOLCD ";
        $query .= "         AND LLMST1.SCHOOL_KIND = OUTGO.SCHOOL_KIND ";
        $query .= "         AND LLMST1.YEAR = OUTGO.YEAR ";
        $query .= "         AND LLMST1.LEVY_L_CD = OUTGO.INCOME_L_CD ";
        $query .= "     LEFT JOIN LEVY_M_MST LMMST1 ";
        $query .= "         ON LMMST1.SCHOOLCD = OUTGO.SCHOOLCD ";
        $query .= "         AND LMMST1.SCHOOL_KIND = OUTGO.SCHOOL_KIND ";
        $query .= "         AND LMMST1.YEAR = OUTGO.YEAR ";
        $query .= "         AND LMMST1.LEVY_L_CD = OUTGO.INCOME_L_CD ";
        $query .= "         AND LMMST1.LEVY_M_CD = OUTGO.INCOME_M_CD ";
        $query .= "     LEFT JOIN LEVY_L_MST LLMST2 ";
        $query .= "         ON LLMST2.SCHOOLCD = OUTGO.SCHOOLCD ";
        $query .= "         AND LLMST2.SCHOOL_KIND = OUTGO.SCHOOL_KIND ";
        $query .= "         AND LLMST2.YEAR = OUTGO.YEAR ";
        $query .= "         AND LLMST2.LEVY_L_CD = OUTGO.OUTGO_L_CD ";
        $query .= "     LEFT JOIN LEVY_M_MST LMMST2 ";
        $query .= "         ON LMMST2.SCHOOLCD = OUTGO.SCHOOLCD ";
        $query .= "         AND LMMST2.SCHOOL_KIND = OUTGO.SCHOOL_KIND ";
        $query .= "         AND LMMST2.YEAR = OUTGO.YEAR ";
        $query .= "         AND LMMST2.LEVY_L_CD = OUTGO.OUTGO_L_CD ";
        $query .= "         AND LMMST2.LEVY_M_CD = OUTGO.OUTGO_M_CD ";
        $query .= "     LEFT JOIN LEVY_S_MST LSMST ";
        $query .= "         ON LSMST.SCHOOLCD = OUTGO.SCHOOLCD ";
        $query .= "         AND LSMST.SCHOOL_KIND = OUTGO.SCHOOL_KIND ";
        $query .= "         AND LSMST.YEAR = OUTGO.YEAR ";
        $query .= "         AND LSMST.LEVY_L_CD = OUTGO.OUTGO_L_CD ";
        $query .= "         AND LSMST.LEVY_M_CD = OUTGO.OUTGO_M_CD ";
        $query .= "         AND LSMST.LEVY_S_CD = MEISAI.OUTGO_S_CD ";
        $query .= "     LEFT JOIN STAFF_MST STF ";
        $query .= "         ON STF.STAFFCD = OUTGO.REQUEST_STAFF ";
        $query .= "     LEFT JOIN COMMON_DATA COMMON ";
        $query .= "         ON COMMON.SCHREGNO = SCH_OUTGO.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     OUTGO.SCHOOLCD  = '{$model->schoolcd}' ";
        if ($model->field["SCHOOL_KIND"] != "ALL") {
            $query .= "        AND OUTGO.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        }
        $query .= "        AND OUTGO.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "        AND OUTGO.OUTGO_L_CD <> '99' ";
        $query .= " ORDER BY ";
        $query .= "     COMMON.GRADE, ";
        $query .= "     COMMON.HR_CLASS, ";
        $query .= "     COMMON.ATTENDNO, ";
        $query .= "     SCH_OUTGO.REQUEST_NO, ";
        $query .= "     SCH_OUTGO.OUTGO_L_CD, ";
        $query .= "     SCH_OUTGO.OUTGO_M_CD, ";
        $query .= "     SCH_OUTGO.OUTGO_S_CD, ";
        $query .= "     SCH_OUTGO.LINE_NO ";

        return $query;
    }
}
