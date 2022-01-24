<?php

require_once('for_php7.php');

class knjp727Query extends Query {

    function getRegdWith($model) {
        if ($model->search["STD_DIV"] == "1") {
            $query  = " WITH REGD_T AS ( ";
            $query .= " SELECT ";
            $query .= "     FRESHMAN.ENTERYEAR AS YEAR, ";
            $query .= "     '1' AS SEMESTER, ";
            $query .= "     FRESHMAN.SCHREGNO, ";
            $query .= "     FRESHMAN.GRADE, ";
            $query .= "     FRESHMAN.HR_CLASS, ";
            $query .= "     FRESHMAN.ATTENDNO, ";
            $query .= "     FRESHMAN.COURSECD, ";
            $query .= "     FRESHMAN.MAJORCD, ";
            $query .= "     FRESHMAN.COURSECODE ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT FRESHMAN ";
            $query .= " WHERE ";
            $query .= "     FRESHMAN.ENTERYEAR = '{$model->exeYear}' ";
            if (isset($model->search["HR_CLASS"]) && $model->search["HR_CLASS"] != "00-000") {
                $query .= "   AND FRESHMAN.GRADE || '-' || FRESHMAN.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
            } else if ($model->search["GRADE"]) {
                $query .= "   AND FRESHMAN.GRADE = '".$model->search["GRADE"]."' ";
            } else {
                $query .= "   AND FRESHMAN.GRADE = '' ";
            }
        } else {
            $query  = " WITH REGD_MAX_SEM AS ( ";
            $query .= " SELECT ";
            $query .= "     YEAR, ";
            $query .= "     MAX(SEMESTER) AS SEMESTER, ";
            $query .= "     SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->exeYear}' ";
            $query .= " GROUP BY ";
            $query .= "     YEAR, ";
            $query .= "     SCHREGNO ";
            $query .= " ), REGD_T AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.COURSECODE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1, ";
            $query .= "     REGD_MAX_SEM T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR ";
            if (isset($model->search["HR_CLASS"]) && $model->search["HR_CLASS"] != "00-000") {
                $query .= "   AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
            }else if ($model->search["GRADE"]) {
                $query .= "   AND T1.GRADE = '".$model->search["GRADE"]."' ";
            } else {
                $query .= "   AND T1.GRADE = '' ";
            }
            $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= " ), SLIP_T AS ( ";
        $query .= " SELECT ";
        $query .= "     REGD_T.YEAR, ";
        $query .= "     REGD_T.SEMESTER, ";
        $query .= "     REGD_T.SCHREGNO, ";
        $query .= "     REGD_T.GRADE, ";
        $query .= "     REGD_T.HR_CLASS, ";
        $query .= "     REGD_T.ATTENDNO, ";
        $query .= "     SLIP_D.SLIP_NO, ";
        $query .= "     VALUE(MAX(COL_M.IS_CREDITCNT), '0') AS IS_CREDITCNT ";
        $query .= " FROM ";
        $query .= "     REGD_T, ";
        $query .= "     COLLECT_SLIP_DAT SLIP_D ";
        $query .= "     INNER JOIN COLLECT_SLIP_M_DAT SLIP_MD ON SLIP_MD.SCHOOLCD = SLIP_D.SCHOOLCD ";
        $query .= "           AND SLIP_MD.SCHOOL_KIND = SLIP_D.SCHOOL_KIND ";
        $query .= "           AND SLIP_MD.YEAR = SLIP_D.YEAR ";
        $query .= "           AND SLIP_MD.SLIP_NO = SLIP_D.SLIP_NO ";
        $query .= "     INNER JOIN COLLECT_M_MST COL_M ON COL_M.COLLECT_L_CD = SLIP_MD.COLLECT_L_CD ";
        $query .= "           AND COL_M.COLLECT_M_CD = SLIP_MD.COLLECT_M_CD ";
        $query .= "           AND COL_M.REDUCTION_DIV IN ('1', '2') ";
        $query .= " WHERE ";
        $query .= "     SLIP_D.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SLIP_D.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND SLIP_D.CANCEL_DATE IS NULL ";
        $query .= "     AND REGD_T.YEAR = SLIP_D.YEAR ";
        $query .= "     AND REGD_T.SCHREGNO = SLIP_D.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     REGD_T.YEAR, ";
        $query .= "     REGD_T.SEMESTER, ";
        $query .= "     REGD_T.SCHREGNO, ";
        $query .= "     REGD_T.GRADE, ";
        $query .= "     REGD_T.HR_CLASS, ";
        $query .= "     REGD_T.ATTENDNO, ";
        $query .= "     SLIP_D.SLIP_NO ";
        $query .= " ) ";

        return $query;
    }

    function selectQuery($model) {
        //変数
        $sdate =  $model->exeYear      ."-04-01";
        $edate = ($model->exeYear + 1) ."-03-31";
        //SQL
        $query  = knjp727Query::getRegdWith($model);
        $query .= " , STD_CREDIT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(T1.CREDITS) AS CREDITS ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD, ";
        $query .= "         SUM(VALUE(CRE.CREDITS, 0)) AS CREDITS ";
        $query .= "     FROM ";
        $query .= "         REGD_T REGD ";
        $query .= "         INNER JOIN (SELECT ";
        $query .= "                         I_SUB_STD.SCHREGNO, ";
        $query .= "                         I_SUB_STD.CLASSCD, ";
        $query .= "                         I_SUB_STD.SCHOOL_KIND, ";
        $query .= "                         I_SUB_STD.CURRICULUM_CD, ";
        $query .= "                         I_SUB_STD.SUBCLASSCD ";
        $query .= "                     FROM ";
        if ($model->Properties["UnUseSubclassStdSelectRirekiDat"] == "1") {
            $query .= "                         SUBCLASS_STD_SELECT_DAT I_SUB_STD ";
        } else {
            $query .= "                     (SELECT ";
            $query .= "                         RIREKI.* ";
            $query .= "                     FROM ";
            $query .= "                         SUBCLASS_STD_SELECT_RIREKI_DAT RIREKI, ";
            $query .= "                         (SELECT ";
            $query .= "                              MAX_T.YEAR, ";
            $query .= "                              MAX_T.SCHREGNO, ";
            $query .= "                              MAX(MAX_T.RIREKI_CODE) AS RIREKI_CODE ";
            $query .= "                          FROM ";
            $query .= "                              SUBCLASS_STD_SELECT_RIREKI_DAT MAX_T ";
            $query .= "                          WHERE ";
            $query .= "                              MAX_T.YEAR = '{$model->exeYear}' ";
            $query .= "                              AND MAX_T.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "                          GROUP BY ";
            $query .= "                              MAX_T.YEAR, ";
            $query .= "                              MAX_T.SCHREGNO ";
            $query .= "                         ) RIREKI_MAX ";
            $query .= "                     WHERE ";
            $query .= "                         RIREKI.YEAR = RIREKI_MAX.YEAR ";
            $query .= "                         AND RIREKI.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "                         AND RIREKI.SCHREGNO = RIREKI_MAX.SCHREGNO ";
            $query .= "                         AND RIREKI.RIREKI_CODE = RIREKI_MAX.RIREKI_CODE ";
            $query .= "                     ) I_SUB_STD ";
        }
        $query .= "                     WHERE ";
        $query .= "                         I_SUB_STD.YEAR = '{$model->exeYear}' ";
        $query .= "                         AND I_SUB_STD.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                     GROUP BY ";
        $query .= "                         I_SUB_STD.SCHREGNO, ";
        $query .= "                         I_SUB_STD.CLASSCD, ";
        $query .= "                         I_SUB_STD.SCHOOL_KIND, ";
        $query .= "                         I_SUB_STD.CURRICULUM_CD, ";
        $query .= "                         I_SUB_STD.SUBCLASSCD ";
        $query .= "                     ) SUB_STD ON REGD.SCHREGNO = SUB_STD.SCHREGNO ";
        $query .= "         LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = '{$model->exeYear}' ";
        $query .= "              AND REGD.COURSECD      = CRE.COURSECD ";
        $query .= "              AND REGD.MAJORCD       = CRE.MAJORCD ";
        $query .= "              AND REGD.GRADE         = CRE.GRADE ";
        $query .= "              AND REGD.COURSECODE    = CRE.COURSECODE ";
        $query .= "              AND SUB_STD.CLASSCD    = CRE.CLASSCD ";
        $query .= "              AND SUB_STD.SCHOOL_KIND = CRE.SCHOOL_KIND ";
        $query .= "              AND SUB_STD.CURRICULUM_CD = CRE.CURRICULUM_CD ";
        $query .= "              AND SUB_STD.SUBCLASSCD = CRE.SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         SUB_STD.SCHREGNO, ";
        $query .= "         SUB_STD.CLASSCD, ";
        $query .= "         SUB_STD.SCHOOL_KIND, ";
        $query .= "         SUB_STD.CURRICULUM_CD, ";
        $query .= "         SUB_STD.SUBCLASSCD ";
        $query .= "     ) T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "   STD_INFO.SCHREGNO, ";
        $query .= "   STD_INFO.NAME, ";
        $query .= "   STD_INFO.GRADE, ";
        $query .= "   STD_INFO.HR_CLASS, ";
        $query .= "   STD_INFO.HR_NAMEABBV, ";
        $query .= "   STD_INFO.ATTENDNO, ";
        $query .= "   STD_INFO.SLIP_NO, ";
        $query .= "   VALUE(STD_INFO.IS_CREDITCNT, '0') AS IS_CREDITCNT, ";
        $query .= "   VALUE(STD_CREDIT.CREDITS, 0) AS CREDITS, ";

        $query .= "   COUNTRY_D.SEQ_1 AS REDUCTION_SEQ_1, ";
        $query .= "   COUNTRY_D.INCOME_1 AS REDUC_INCOME_1, ";
        $query .= "   COUNTRY_D.INCOME_1_1 AS REDUC_INCOME_1_1, ";
        $query .= "   COUNTRY_D.INCOME_1_2 AS REDUC_INCOME_1_2, ";
        $query .= "   COUNTRY_D.INCOME_1_3 AS REDUC_INCOME_1_3, ";
        $query .= "   COUNTRY_D.INCOME_1_4 AS REDUC_INCOME_1_4, ";
        $query .= "   COUNTRY_D.SEQ_2 AS REDUCTION_SEQ_2, ";
        $query .= "   COUNTRY_D.INCOME_2 AS REDUC_INCOME_2, ";
        $query .= "   COUNTRY_D.INCOME_2_1 AS REDUC_INCOME_2_1, ";
        $query .= "   COUNTRY_D.INCOME_2_2 AS REDUC_INCOME_2_2, ";
        $query .= "   COUNTRY_D.INCOME_2_3 AS REDUC_INCOME_2_3, ";
        $query .= "   COUNTRY_D.INCOME_2_4 AS REDUC_INCOME_2_4, ";
        $query .= "   RARE.NAMECD2 || ':' || RARE.NAMESPARE1 AS COUNTRY_RARE_CASE_CD, ";
        $query .= "   RARE.NAMESPARE1 AS CUNTRY_TEXT_DISP, ";

        $query .= "   COUNTRY_D.RANK_1 AS COUNTRY_RANK_1, ";
        $query .= "   COUNTRY_D.MONEY_1 AS COUNTRY_MONEY_1, ";
        $query .= "   COUNTRY_D.DEC_FLG_1 AS COUNTRY_BASE_FLG_1, ";
        $query .= "   COUNTRY_D.ADD_MONEY_1 AS COUNTRY_ADD_MONEY_1, ";
        $query .= "   COUNTRY_D.ADD_DEC_FLG_1 AS COUNTRY_ADD_FLG_1, ";
        $query .= "   COUNTRY_D.RANK_2 AS COUNTRY_RANK_2, ";
        $query .= "   COUNTRY_D.MONEY_2 AS COUNTRY_MONEY_2, ";
        $query .= "   COUNTRY_D.DEC_FLG_2 AS COUNTRY_BASE_FLG_2, ";
        $query .= "   COUNTRY_D.ADD_MONEY_2 AS COUNTRY_ADD_MONEY_2, ";
        $query .= "   COUNTRY_D.ADD_DEC_FLG_2 AS COUNTRY_ADD_FLG_2, ";
        $query .= "   VALUE(COUNTRY_D.MONEY_1, 0) + VALUE(COUNTRY_D.MONEY_2, 0) AS TOTAL_COUNTRY_MONEY, ";

        $query .= "   GUARDIAN.PREF AS G_PREF, ";
        $query .= "   GUARDIAN.PREFECTURESCD AS G_PREF_CD, ";
        $query .= "   REDUCTION_D.RANK_1 AS PREF_RANK_1, ";
        $query .= "   REDUCTION_D.MONEY_1 AS PREF_MONEY_1, ";
        $query .= "   REDUCTION_D.DEC_FLG_1 AS PREF_FLG_1, ";
        $query .= "   REDUCTION_D.RANK_2 AS PREF_RANK_2, ";
        $query .= "   REDUCTION_D.MONEY_2 AS PREF_MONEY_2, ";
        $query .= "   REDUCTION_D.DEC_FLG_2 AS PREF_FLG_2, ";
        $query .= "   REDUCTION_D.PREFECTURESCD AS REDUC_PREF_CD, ";
        $query .= "   NM1.NAME1 AS REDUC_PREF, ";
        $query .= "   PREF_GRADE.PREFECTURESCD AS PREF_GRADE, ";

        $query .= "   value(TRANSFER.COUNTTRANSFER,0) + value(GRD.COUNTTRANSFER,0) + value(ENT.COUNTTRANSFER,0) AS COUNTTRANSFER, ";
        $query .= "   value(GENMEN.COUNTGENMEN, 0) AS COUNTGENMEN, ";
        $query .= "   COUNTRY_PLAN.ZENKI_CNT, ";
        $query .= "   COUNTRY_PLAN.KOUKI_CNT, ";
        $query .= "   COUNTRY_PLAN.ZENKI_ADD_CNT, ";
        $query .= "   COUNTRY_PLAN.KOUKI_ADD_CNT, ";

        $query .= "   PREF_PLAN.ZENKI_PREF_CNT, ";
        $query .= "   PREF_PLAN.KOUKI_PREF_CNT, ";

        $query .= "   COUNTRY_D.REDUCTION_JUGYOURYOU, ";
        $query .= "   COUNTRY_D.REDUCTION_JUGYOURYOUTOU, ";
        $query .= "   REDUCTION_SCHOOL_D2.DEC_FLG_1 AS REDUC_SCHOOL_FLG_1, ";
        $query .= "   REDUCTION_SCHOOL_D1.DEC_FLG_2 AS REDUC_SCHOOL_FLG_2, ";

        $query .= "   CASE WHEN COUNTRY_D.REMARK IS NOT NULL ";
        $query .= "        THEN COUNTRY_D.REMARK ";
        $query .= "        ELSE REDUCTION_D.REMARK ";
        $query .= "   END AS REMARK ";
        $query .= " FROM ";
        $query .= "   (SELECT ";
        $query .= "     ST2.YEAR, ";
        $query .= "     ST2.SEMESTER, ";
        $query .= "     ST1.SCHREGNO, ";
        $query .= "     ST1.NAME, ";
        $query .= "     ST2.GRADE, ";
        $query .= "     ST2.HR_CLASS, ";
        $query .= "     ST2.ATTENDNO, ";
        if ($model->search["STD_DIV"] == "1") {
            $query .= "     '新入生' || VALUE(ST1.HR_CLASS, '000') AS HR_NAMEABBV, ";
        } else {
            $query .= "     ST3.HR_NAMEABBV, ";
        }
        $query .= "     ST2.SLIP_NO, ";
        $query .= "     ST2.IS_CREDITCNT ";
        $query .= "   FROM ";
        if ($model->search["STD_DIV"] == "1") {
            $query .= "     FRESHMAN_DAT ST1, ";
        } else {
            $query .= "     SCHREG_BASE_MST ST1, ";
        }
        $query .= "     SLIP_T ST2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT ST3 ON ST2.YEAR = ST3.YEAR ";
        $query .= "          AND ST2.SEMESTER = ST3.SEMESTER ";
        $query .= "          AND ST2.GRADE = ST3.GRADE ";
        $query .= "          AND ST2.HR_CLASS = ST3.HR_CLASS ";
        $query .= "   WHERE ";
        $query .= "     ST1.SCHREGNO = ST2.SCHREGNO ";
        $query .= "   ) STD_INFO ";
        //単位
        $query .= "       LEFT JOIN STD_CREDIT ON STD_INFO.SCHREGNO = STD_CREDIT.SCHREGNO ";
        //保護者
        $query .= "       LEFT JOIN (SELECT DISTINCT ";
        $query .= "                      ST1.SCHREGNO, ";
        $query .= "                      SUBSTR(ST2.CITYCD,1,2) AS PREFECTURESCD, ";
        $query .= "                      ST3.NAME1 AS PREF ";
        $query .= "                  FROM ";
        $query .= "                      GUARDIAN_DAT ST1 ";
        $query .= "                      LEFT JOIN ZIPCD_MST ST2  ";
        $query .= "                          ON ST2.NEW_ZIPCD = (CASE WHEN LENGTH(ST1.GUARANTOR_ZIPCD) > 0 THEN ST1.GUARANTOR_ZIPCD ELSE ST1.GUARD_ZIPCD END) ";
        $query .= "                      LEFT JOIN NAME_MST ST3  ";
        $query .= "                          ON ST3.NAMECD1 = 'G202' ";
        $query .= "                         AND ST3.NAMECD2 = SUBSTR(ST2.CITYCD,1,2) ";
        $query .= "                  ) GUARDIAN ON STD_INFO.SCHREGNO = GUARDIAN.SCHREGNO  ";
        //支援金
        $query .= "       LEFT JOIN REDUCTION_COUNTRY_DAT COUNTRY_D ON COUNTRY_D.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "            AND COUNTRY_D.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "            AND COUNTRY_D.YEAR = '{$model->exeYear}' ";
        $query .= "            AND STD_INFO.SLIP_NO = COUNTRY_D.SLIP_NO ";
        $query .= "       LEFT JOIN NAME_MST RARE ON RARE.NAMECD1 = 'P711' ";
        $query .= "            AND RARE.NAMECD2 = COUNTRY_D.RARE_CASE_CD_1 ";
        //補助金
        $query .= "       LEFT JOIN REDUCTION_DAT REDUCTION_D ON REDUCTION_D.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "            AND REDUCTION_D.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "            AND STD_INFO.YEAR = REDUCTION_D.YEAR ";
        $query .= "            AND STD_INFO.SLIP_NO = REDUCTION_D.SLIP_NO ";
        $query .= "       LEFT JOIN NAME_MST NM1 ON NM1.NAMECD1 = 'G202' ";
        $query .= "            AND NM1.NAMECD2 = REDUCTION_D.PREFECTURESCD ";
        //家族減免
        $query .= "       LEFT JOIN REDUCTION_SCHOOL_DAT REDUCTION_SCHOOL_D2 ON REDUCTION_SCHOOL_D2.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "            AND REDUCTION_SCHOOL_D2.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "            AND REDUCTION_SCHOOL_D2.REDUCTION_TARGET = '2' ";
        $query .= "            AND STD_INFO.YEAR = REDUCTION_SCHOOL_D2.YEAR ";
        $query .= "            AND STD_INFO.SLIP_NO = REDUCTION_SCHOOL_D2.SLIP_NO ";
        //学力減免
        $query .= "       LEFT JOIN REDUCTION_SCHOOL_DAT REDUCTION_SCHOOL_D1 ON REDUCTION_SCHOOL_D1.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "            AND REDUCTION_SCHOOL_D1.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "            AND REDUCTION_SCHOOL_D1.REDUCTION_TARGET = '1' ";
        $query .= "            AND STD_INFO.YEAR = REDUCTION_SCHOOL_D1.YEAR ";
        $query .= "            AND STD_INFO.SLIP_NO = REDUCTION_SCHOOL_D1.SLIP_NO ";
        //減免
        $query .= "       LEFT JOIN (SELECT ";
        $query .= "                            COUNT(*) AS COUNTGENMEN, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_SCHOLARSHIP_HIST_DAT ";
        $query .= "                        WHERE ";
        $query .= "                            SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                            AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                            AND ('".$model->exeYear."-04-01' BETWEEN FROM_DATE AND TO_DATE ";
        $query .= "                                 OR ";
        $query .= "                                 '".($model->exeYear + 1)."-03-31' BETWEEN FROM_DATE AND TO_DATE ";
        $query .= "                                ) ";
        $query .= "                        GROUP BY ";
        $query .= "                            SCHREGNO ";
        $query .= "                       ) GENMEN ON STD_INFO.SCHREGNO = GENMEN.SCHREGNO ";
        //異動
        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                            COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                            SCHREGNO ";
        $query .= "                        FROM ";
        $query .= "                            SCHREG_TRANSFER_DAT ";
        $query .= "                        WHERE ";
        $query .= "                            TRANSFERCD IN ('1','2') ";
        $query .= "                            AND ((TRANSFER_SDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')) ";
        $query .= "                             OR  (TRANSFER_EDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))) ";
        $query .= "                        GROUP BY ";
        $query .= "                            SCHREGNO ";
        $query .= "                       ) TRANSFER ON STD_INFO.SCHREGNO = TRANSFER.SCHREGNO ";
        $query .= "       LEFT JOIN (SELECT ";
        $query .= "                      COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                      SCHREGNO ";
        $query .= "                  FROM ";
        $query .= "                      SCHREG_BASE_MST ";
        $query .= "                  WHERE ";
        $query .= "                      GRD_DIV > '1' ";
        $query .= "                      AND GRD_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= "                  GROUP BY ";
        $query .= "                      SCHREGNO ";
        $query .= "                  ) GRD ON STD_INFO.SCHREGNO = GRD.SCHREGNO ";
        $query .= "       LEFT JOIN (SELECT ";
        $query .= "                      COUNT(*) AS COUNTTRANSFER, ";
        $query .= "                      SCHREGNO ";
        $query .= "                  FROM ";
        $query .= "                      SCHREG_BASE_MST ";
        $query .= "                  WHERE ";
        $query .= "                      ENT_DIV > '3' ";
        $query .= "                      AND ENT_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}') ";
        $query .= "                  GROUP BY ";
        $query .= "                      SCHREGNO ";
        $query .= "                 ) ENT ON STD_INFO.SCHREGNO = ENT.SCHREGNO ";
        //支援金計画カウント
        $query .= "       LEFT JOIN (SELECT ";
        $query .= "                    SLIP_NO, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".$model->exeYear."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS ZENKI_CNT, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".$model->exeYear."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS KOUKI_CNT, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".$model->exeYear."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS ZENKI_ADD_CNT, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".$model->exeYear."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS KOUKI_ADD_CNT ";
        $query .= "                  FROM ";
        $query .= "                      REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= "                  WHERE ";
        $query .= "                      SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                      AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                      AND YEAR = '{$model->exeYear}' ";
        $query .= "                  GROUP BY ";
        $query .= "                      SLIP_NO ";
        $query .= "                 ) COUNTRY_PLAN ON STD_INFO.SLIP_NO = COUNTRY_PLAN.SLIP_NO ";
        //補助金計画カウント
        $query .= "       LEFT JOIN (SELECT ";
        $query .= "                    SLIP_NO, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".$model->exeYear."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS ZENKI_PREF_CNT, ";
        $query .= "                    SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".$model->exeYear."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "                             THEN 1 ";
        $query .= "                             ELSE 0 ";
        $query .= "                        END ";
        $query .= "                    ) AS KOUKI_PREF_CNT ";
        $query .= "                  FROM ";
        $query .= "                      REDUCTION_PLAN_DAT ";
        $query .= "                  WHERE ";
        $query .= "                      SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "                      AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "                      AND YEAR = '{$model->exeYear}' ";
        $query .= "                  GROUP BY ";
        $query .= "                      SLIP_NO ";
        $query .= "                 ) PREF_PLAN ON STD_INFO.SLIP_NO = PREF_PLAN.SLIP_NO ";
        $query .= "       LEFT OUTER JOIN (SELECT ";
        $query .= "                          PREF_GRADE.* ";
        $query .= "                        FROM ";
        $query .= "                          REDUCTION_PREF_GRADE_MST PREF_GRADE ";
        $query .= "                          INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'G202' ";
        $query .= "                                AND N1.NAMECD2 = PREF_GRADE.PREFECTURESCD ";
        $query .= "                        WHERE ";
        $query .= "                          PREF_GRADE.YEAR = '{$model->exeYear}' ";
        $query .= "                         ) PREF_GRADE ON STD_INFO.GRADE = PREF_GRADE.GRADE AND GUARDIAN.PREFECTURESCD = PREF_GRADE.PREFECTURESCD  ";
        $query .= " ORDER BY ";
        $query .= "   GRADE, ";
        $query .= "   HR_CLASS, ";
        $query .= "   ATTENDNO, ";
        $query .= "   STD_INFO.SLIP_NO ";

        return $query;
    }

    //年組
    function selectQueryGrade(&$model) {
        if ($model->search["STD_DIV"] == "1") {
            $query  = " SELECT DISTINCT ";
            $query .= "     FRESHMAN.GRADE, ";
            $query .= "     VALUE(FRESHMAN.HR_CLASS, '000') AS HR_CLASS, ";
            $query .= "     '新入生' || FRESHMAN.GRADE AS HR_NAME, ";
            $query .= "     '新入生' || VALUE(FRESHMAN.HR_CLASS, '000') AS HR_NAMEABBV, ";
            $query .= "     REGD_G.SCHOOL_KIND ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT FRESHMAN ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON FRESHMAN.ENTERYEAR = REGD_G.YEAR ";
            $query .= "          AND FRESHMAN.GRADE = REGD_G.GRADE ";
            $query .= " WHERE ";
            $query .= "      FRESHMAN.ENTERYEAR = '{$model->exeYear}' ";
            $query .= "      AND FRESHMAN.GRADE IS NOT NULL ";
            $query .= " ORDER BY ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS ";
        } else {
            $query  = " SELECT ";
            $query .= "     REGD_H.GRADE, ";
            $query .= "     REGD_H.HR_CLASS, ";
            $query .= "     REGD_H.HR_NAME, ";
            $query .= "     REGD_H.HR_NAMEABBV, ";
            $query .= "     REGD_G.SCHOOL_KIND, ";
            $query .= "     T2.STAFFCD, ";
            $query .= "     T2.STAFFNAME_SHOW ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REGD_G ON REGD_H.YEAR = REGD_G.YEAR ";
            $query .= "          AND REGD_H.GRADE = REGD_G.GRADE, ";
            $query .= "     STAFF_MST T2 ";
            $query .= " WHERE ";
            if ($model->usr_auth == DEF_UPDATE_RESTRICT) {
                $query .= "     REGD_H.TR_CD1 = '". STAFFCD ."' AND";
            }
            $query .= "      REGD_H.TR_CD1 = T2.STAFFCD AND ";
            $query .= "      REGD_H.YEAR = '{$model->exeYear}' AND ";
            $query .= "      REGD_H.SEMESTER = '" .CTRL_SEMESTER ."' ";
            $query .= " ORDER BY ";
            $query .= "     REGD_H.GRADE, ";
            $query .= "     REGD_H.HR_CLASS ";
        }

        return $query;
    }

    //生徒授業料
    function getSlipPlan($model, $schregNo = "") {
        $query  = knjp727Query::getRegdWith($model);
        $query .= " SELECT ";
        $query .= "     SLIP_PLAN.SCHREGNO, ";
        $query .= "     SLIP_T.SLIP_NO, ";
        $query .= "     SUM(CASE WHEN VALUE(M_MST.GAKUNOKIN_DIV, '') = '1' AND SLIP_PLAN.PLAN_MONTH IN ('04', '05', '06') ";
        $query .= "              THEN SLIP_PLAN.PLAN_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS ZENKI_JUGYOU, ";
        $query .= "     SUM(CASE WHEN VALUE(M_MST.GAKUNOKIN_DIV, '') = '1' AND SLIP_PLAN.PLAN_MONTH NOT IN ('04', '05', '06') ";
        $query .= "              THEN SLIP_PLAN.PLAN_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS KOUKI_JUGYOU, ";
        $query .= "     SUM(CASE WHEN VALUE(M_MST.GAKUNOKIN_DIV, '') = '1' ";
        $query .= "              THEN SLIP_PLAN.PLAN_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS TOTAL_JUGYOU, ";
        $query .= "     SUM(CASE WHEN VALUE(M_MST.REDUCTION_DIV, '') = '2' AND SLIP_PLAN.PLAN_MONTH IN ('04', '05', '06') ";
        $query .= "              THEN SLIP_PLAN.PLAN_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS ZENKI_JUGYOUTOU, ";
        $query .= "     SUM(CASE WHEN VALUE(M_MST.REDUCTION_DIV, '') = '2' AND SLIP_PLAN.PLAN_MONTH NOT IN ('04', '05', '06') ";
        $query .= "              THEN SLIP_PLAN.PLAN_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS KOUKI_JUGYOUTOU, ";
        $query .= "     SUM(CASE WHEN VALUE(M_MST.REDUCTION_DIV, '') = '2' ";
        $query .= "              THEN SLIP_PLAN.PLAN_MONEY ";
        $query .= "              ELSE 0 ";
        $query .= "         END ";
        $query .= "     ) AS TOTAL_JUGYOUTOU ";
        $query .= " FROM ";
        $query .= "     SLIP_T, ";
        $query .= "     COLLECT_SLIP_DAT SLIP_D, ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT SLIP_PLAN, ";
        $query .= "     COLLECT_M_MST M_MST ";
        $query .= " WHERE ";
        $query .= "     SLIP_T.SCHREGNO = SLIP_D.SCHREGNO ";
        if ($schregNo) {
            $query .= "     AND SLIP_T.SCHREGNO = '{$schregNo}' ";
        }
        $query .= "     AND SLIP_T.SLIP_NO = SLIP_D.SLIP_NO ";
        $query .= "     AND SLIP_D.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SLIP_D.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND SLIP_D.YEAR = '{$model->exeYear}' ";
        $query .= "     AND SLIP_D.CANCEL_DATE IS NULL ";
        $query .= "     AND SLIP_D.SCHOOLCD = SLIP_PLAN.SCHOOLCD ";
        $query .= "     AND SLIP_D.SCHOOL_KIND = SLIP_PLAN.SCHOOL_KIND ";
        $query .= "     AND SLIP_D.YEAR = SLIP_PLAN.YEAR ";
        $query .= "     AND SLIP_D.SLIP_NO = SLIP_PLAN.SLIP_NO ";
        $query .= "     AND SLIP_D.SCHREGNO = SLIP_PLAN.SCHREGNO ";
        $query .= "     AND SLIP_PLAN.SCHOOLCD = M_MST.SCHOOLCD ";
        $query .= "     AND SLIP_PLAN.SCHOOL_KIND = M_MST.SCHOOL_KIND ";
        $query .= "     AND SLIP_PLAN.YEAR = M_MST.YEAR ";
        $query .= "     AND SLIP_PLAN.COLLECT_L_CD = M_MST.COLLECT_L_CD ";
        $query .= "     AND SLIP_PLAN.COLLECT_M_CD = M_MST.COLLECT_M_CD ";
        $query .= "     AND VALUE(M_MST.KOUHI_SHIHI, '') = '1' ";
        $query .= "     AND (VALUE(M_MST.GAKUNOKIN_DIV, '') = '1' ";
        $query .= "          OR ";
        $query .= "          VALUE(M_MST.REDUCTION_DIV, '') = '2' ";
        $query .= "         ) ";
        $query .= " GROUP BY ";
        $query .= "     SLIP_PLAN.SCHREGNO, ";
        $query .= "     SLIP_T.SLIP_NO ";
        $query .= " ORDER BY ";
        $query .= "     SLIP_PLAN.SCHREGNO, ";
        $query .= "     SLIP_T.SLIP_NO ";

        return $query;
    }

    //府県補助マスタ
    function getPrefGradeMst($model) {
        $query  = " SELECT ";
        $query .= "     PREFECTURESCD, ";
        $query .= "     GRADE, ";
        $query .= "     REFER_YEAR_DIV1, ";
        $query .= "     TOTALL_MONEY_1, ";
        $query .= "     REFER_YEAR_DIV2, ";
        $query .= "     TOTALL_MONEY_2, ";
        $query .= "     USE_RANK, ";
        $query .= "     ZENKI_KAISI_YEAR, ";
        $query .= "     KOUKI_KAISI_YEAR, ";
        $query .= "     STANDARD_SCHOOL_FEE ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PREF_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR = '{$model->exeYear}' ";
        $query .= "     AND GRADE = '{$model->search["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     PREFECTURESCD ";

        return $query;
    }

    //補助金MST
    function getReductionMst($model) {
        $query  = " SELECT ";
        $query .= "     REDUC_M.PREFECTURESCD, ";
        $query .= "     REDUC_M.MONEY_DIV1, ";
        $query .= "     REDUC_M.NUMERATOR1, ";
        $query .= "     REDUC_M.DENOMINATOR1, ";
        $query .= "     REDUC_M.REDUCTIONMONEY_1, ";
        $query .= "     REDUC_M.INCOME_LOW1, ";
        $query .= "     REDUC_M.INCOME_HIGH1, ";
        $query .= "     REDUC_M.INCOME_RANK1, ";
        $query .= "     REDUC_M.MONEY_DIV2, ";
        $query .= "     REDUC_M.NUMERATOR2, ";
        $query .= "     REDUC_M.DENOMINATOR2, ";
        $query .= "     REDUC_M.REDUCTIONMONEY_2, ";
        $query .= "     REDUC_M.INCOME_LOW2, ";
        $query .= "     REDUC_M.INCOME_HIGH2, ";
        $query .= "     REDUC_M.INCOME_RANK2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_MST REDUC_M ";
        $query .= " WHERE ";
        $query .= "     REDUC_M.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND REDUC_M.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND REDUC_M.YEAR = '{$model->exeYear}' ";
        $query .= "     AND REDUC_M.GRADE = '{$model->search["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     REDUC_M.PREFECTURESCD ";

        return $query;
    }

    //生徒減免
    function getReductionStd($model, $timing) {
        $query  = knjp727Query::getRegdWith($model);
        $query .= " SELECT ";
        $query .= "     SCHOLAR_H.SCHREGNO, ";
        $query .= "     SCHOLAR_H.FROM_DATE, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_CD, ";
        $query .= "     CASE WHEN '".$model->exeYear."-04-01' > SCHOLAR_H.FROM_DATE ";
        $query .= "          THEN '".$model->exeYear."-04-01' ";
        $query .= "          ELSE SCHOLAR_H.FROM_DATE ";
        $query .= "     END AS S_YEAR_MONTH, ";
        $query .= "     CASE WHEN '".($model->exeYear + 1)."-03-31' < SCHOLAR_H.TO_DATE ";
        $query .= "          THEN '".($model->exeYear + 1)."-03-31' ";
        $query .= "          ELSE SCHOLAR_H.TO_DATE ";
        $query .= "     END AS E_YEAR_MONTH, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_NAME, ";
        $query .= "     REDUC_SCHOOL.SEMESTER_DIV, ";
        $query .= "     REDUC_SCHOOL.VALID_MONTH, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV, ";
        $query .= "     REDUC_SCHOOL.MONEY_DIV, ";
        $query .= "     REDUC_SCHOOL.NUMERATOR, ";
        $query .= "     REDUC_SCHOOL.DENOMINATOR, ";
        $query .= "     REDUC_SCHOOL.MONEY, ";
        $query .= "     SCHOLAR_H.REMARK ";
        $query .= " FROM ";
        $query .= "     REGD_T, ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT SCHOLAR_H, ";
        $query .= "     REDUCTION_SCHOOL_MST REDUC_SCHOOL ";
        $query .= " WHERE ";
        $query .= "     REGD_T.SCHREGNO = SCHOLAR_H.SCHREGNO ";
        $query .= "     AND SCHOLAR_H.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOLAR_H.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND ('".$model->exeYear."-04-01' BETWEEN SCHOLAR_H.FROM_DATE AND SCHOLAR_H.TO_DATE ";
        $query .= "          OR ";
        $query .= "          '".($model->exeYear + 1)."-03-31' BETWEEN SCHOLAR_H.FROM_DATE AND SCHOLAR_H.TO_DATE ";
        $query .= "         ) ";
        $query .= "     AND SCHOLAR_H.SCHOOLCD = REDUC_SCHOOL.SCHOOLCD ";
        $query .= "     AND SCHOLAR_H.SCHOOL_KIND = REDUC_SCHOOL.SCHOOL_KIND ";
        $query .= "     AND REDUC_SCHOOL.YEAR = '{$model->exeYear}' ";
        $query .= "     AND SCHOLAR_H.SCHOLARSHIP = REDUC_SCHOOL.REDUCTION_DIV_CD ";
        $query .= "     AND REDUC_SCHOOL.REDUCTION_TIMING = '{$timing}' ";
        $query .= " ORDER BY ";
        $query .= "     SCHOLAR_H.SCHREGNO, ";
        $query .= "     SCHOLAR_H.FROM_DATE ";

        return $query;
    }

    //支援金
    function countryMstMonty($model) {
        $query  = " SELECT ";
        $query .= "     COUNTRY_M.REDUCTIONMONEY1, ";
        $query .= "     COUNTRY_M.INCOME_LOW1, ";
        $query .= "     COUNTRY_M.INCOME_HIGH1, ";
        $query .= "     COUNTRY_M.INCOME_RANK1, ";
        $query .= "     COUNTRY_M.REDUCTIONMONEY2, ";
        $query .= "     COUNTRY_M.INCOME_LOW2, ";
        $query .= "     COUNTRY_M.INCOME_HIGH2, ";
        $query .= "     COUNTRY_M.INCOME_RANK2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST COUNTRY_M ";
        $query .= " WHERE ";
        $query .= "     COUNTRY_M.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND COUNTRY_M.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND COUNTRY_M.YEAR = '{$model->exeYear}' ";
        $query .= "     AND COUNTRY_M.GRADE = '".$model->search["GRADE"]."' ";

        return $query;
    }

    //支援金加算
    function countryAddMstMonty($model) {
        $query  = " SELECT ";
        $query .= "     COUNTRY_ADD.REDUCTION_ADD_MONEY1, ";
        $query .= "     COUNTRY_ADD.INCOME_LOW1, ";
        $query .= "     COUNTRY_ADD.INCOME_HIGH1, ";
        $query .= "     COUNTRY_ADD.INCOME_RANK1, ";
        $query .= "     COUNTRY_ADD.REDUCTION_ADD_MONEY2, ";
        $query .= "     COUNTRY_ADD.INCOME_LOW2, ";
        $query .= "     COUNTRY_ADD.INCOME_HIGH2, ";
        $query .= "     COUNTRY_ADD.INCOME_RANK2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST COUNTRY_ADD ";
        $query .= " WHERE ";
        $query .= "     COUNTRY_ADD.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND COUNTRY_ADD.SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND COUNTRY_ADD.YEAR = '{$model->exeYear}' ";
        $query .= "     AND COUNTRY_ADD.GRADE = '".$model->search["GRADE"]."' ";

        return $query;
    }

    //名称マスタ
    function getNameMstSql($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "   NAMECD2 AS VALUE, ";
        $query .= "   NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->exeYear}' ";
        $query .= "   AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "   NAMECD2 ";
        return $query;
    }

    //軽減額取得
    function selectQueryReductionMonty(&$model) {
        $query  = " SELECT ";
        $query .= "     T1.REDUCTION_SEQ, ";
        $query .= "     T1.REDUCTIONMONEY1 AS COUNTRY_MONEY1, ";
        $query .= "     T1.INCOME_RANK1 AS COUNTRY_RANK1, ";
        $query .= "     T1.REDUCTIONMONEY2 AS COUNTRY_MONEY2, ";
        $query .= "     T1.INCOME_RANK2 AS COUNTRY_RANK2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '{$model->income["GRADE"]}' ";
        $query .= "     AND T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SCHOOLDIV = '{$model->slipInfo[$model->income["SLIP_NO"]]["IS_CREDITCNT"]}' ";
        $query .= " ORDER BY ";
        $query .= "     REDUCTION_SEQ ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //軽減額取得
    function selectQueryReductionAddMonty(&$model) {
        $query  = " SELECT ";
        $query .= "     T1.REDUCTION_SEQ, ";
        $query .= "     T1.REDUCTION_ADD_MONEY1 AS COUNTRY_ADD_MONEY1, ";
        $query .= "     T1.REDUCTION_ADD_MONEY2 AS COUNTRY_ADD_MONEY2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$model->income["GRADE"]."' ";
        $query .= "     AND T1.YEAR = '{$model->exeYear}' ";
        $query .= "     AND T1.SCHOOLDIV = '{$model->slipInfo[$model->income["SLIP_NO"]]["IS_CREDITCNT"]}' ";
        $query .= " ORDER BY ";
        $query .= "     REDUCTION_SEQ ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //都道府県コード
    function selectQueryReductionPref(&$model) {
        $query = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'G202' AND ";
        if (is_numeric($model->income["PREFECTURESCD"])) {     //軽減対象都道府県設定値
            $query .= "     T1.NAMECD2 = '".$model->income["PREFECTURESCD"]."' ";
        } else {                                                //保護者都道府県
            $query .= "     T1.NAMECD2 = (SELECT ";
            $query .= "                       SUBSTR(MIN(ST1.CITYCD),1,2) ";
            $query .= "                   FROM ";
            $query .= "                       ZIPCD_MST ST1, ";
            $query .= "                       GUARDIAN_DAT ST2 ";
            $query .= "                   WHERE ";
            $query .= "                       ST1.NEW_ZIPCD = ST2.GUARANTOR_ZIPCD AND ";
            $query .= "                       ST2.SCHREGNO = '".$model->income["SCHREGNO"]."' ";
            $query .= "                   ) ";
        }
        return $query;
    }

    function getPlanCnt($model, $slipNo) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS ALLCNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".$model->exeYear."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS ZENKI_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".$model->exeYear."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS KOUKI_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".$model->exeYear."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS ZENKI_ADD_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".$model->exeYear."07' AND ADD_PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS KOUKI_ADD_CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR = '{$model->exeYear}' ";
        $query .= "     AND SLIP_NO = '".$slipNo."' ";
        return $query;
    }

    function getPrefPlanCnt($model, $slipNo) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS ALLCNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH < '".$model->exeYear."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS ZENKI_CNT, ";
        $query .= "   SUM(CASE WHEN PLAN_YEAR || PLAN_MONTH >= '".$model->exeYear."07' AND PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS KOUKI_CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR = '{$model->exeYear}' ";
        $query .= "     AND SLIP_NO = '{$slipNo}' ";
        return $query;
    }

    function getReducSchoolPlanCnt($model, $slipNo, $reducTarget) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS ALLCNT, ";
        $query .= "   SUM(CASE WHEN PLAN_MONEY IS NOT NULL ";
        $query .= "            THEN 1 ";
        $query .= "            ELSE 0 ";
        $query .= "       END ";
        $query .= "   ) AS PLAN_CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR = '{$model->exeYear}' ";
        $query .= "     AND SLIP_NO = '{$slipNo}' ";
        $query .= "     AND REDUCTION_TARGET = '{$reducTarget}' ";
        return $query;
    }

    function update($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        for($i = 0; $i < get_count($model->slipno); $i++) {
            $slipNo = $model->slipno[$i];
            $schregNo = $model->slipSchreg[$slipNo];
            knjp727Query::countryUpdate($db, $model, $slipNo, $schregNo, $i);
//            knjp727Query::prefUpdate($db, $model, $slipNo, $schregNo, $i);
            knjp727Query::reductionSchoolUpdate($db, $model, $slipNo, $schregNo, $i);
            knjp727Query::updatePlanSum($db, $model, $slipNo, $schregNo);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    function getCountryClearDecisionMonth($model, $slipNo, $money, $setMonth) {
        $query  = " SELECT ";
        $query .= "     MAX(PLAN_MONTH) AS MAX_MONTH, ";
        $query .= "     MIN(PLAN_MONTH) AS MIN_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_COUNTRY_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR = '{$model->exeYear}' ";
        $query .= "     AND SLIP_NO = '{$slipNo}' ";
        $query .= "     AND VALUE({$setMonth}DECISION_MONEY, 0) > {$money} ";

        return $query;
    }

    function countryUpdate($db, $model, $slipNo, $schregNo, $rowCnt) {
        /*************************/
        /* REDUCTION_COUNTRY_DAT */
        /*************************/
        if (is_array($model->field["COUNTRY_BASE_FLG_1"])) {
            $baseFlg1 = in_array($slipNo, $model->field["COUNTRY_BASE_FLG_1"])? 1 : NULL;
        } else {
            $baseFlg1 = NULL;
        }
        if (is_array($model->field["COUNTRY_ADD_FLG_1"])) {
            $addFlg1 = in_array($slipNo, $model->field["COUNTRY_ADD_FLG_1"])? 1 : NULL;
        } else {
            $addFlg1 = NULL;
        }
        if (is_array($model->field["COUNTRY_BASE_FLG_2"])) {
            $baseFlg2 = in_array($slipNo, $model->field["COUNTRY_BASE_FLG_2"])? 1 : NULL;
        } else {
            $baseFlg2 = NULL;
        }
        if (is_array($model->field["COUNTRY_ADD_FLG_2"])) {
            $addFlg2 = in_array($slipNo, $model->field["COUNTRY_ADD_FLG_2"])? 1 : NULL;
        } else {
            $addFlg2 = NULL;
        }

        $data = array();
        $data["SCHREGNO"][TEXT]                     = $schregNo;
        $data["GRADE"][TEXT]                        = $model->search["GRADE"];

        list($rareCd, $baseDummy1) = preg_split("/:/", $model->field["COUNTRY_RARE_CASE_CD"][$rowCnt]);
        $model->field["COUNTRY_RARE_CASE_CD"][$rowCnt] = ($rareCd == 'DUMMY') ? '' : $rareCd;
        $data["RARE_CASE_CD_1"][TEXT]               = $model->field["COUNTRY_RARE_CASE_CD"][$rowCnt] != "" ? $model->field["COUNTRY_RARE_CASE_CD"][$rowCnt] : NULL;

        $data["SEQ_1"][NUMBER]                      = is_numeric($model->field["REDUCTION_SEQ_1"][$rowCnt]) ? $model->field["REDUCTION_SEQ_1"][$rowCnt] : NULL;
        $data["RANK_1"][TEXT]                       = $model->field["COUNTRY_RANK_1"][$rowCnt];
        $data["MONEY_1"][NUMBER]                    = strlen($model->field["COUNTRY_MONEY_1"][$rowCnt]) > 0 ? $model->field["COUNTRY_MONEY_1"][$rowCnt] : NULL;
        $data["DEC_FLG_1"][TEXT]                    = $baseFlg1;
        $data["ADD_MONEY_1"][NUMBER]                = strlen($model->field["COUNTRY_ADD_MONEY_1"][$rowCnt]) > 0 ? $model->field["COUNTRY_ADD_MONEY_1"][$rowCnt]   : NULL;
        $data["ADD_DEC_FLG_1"][TEXT]                = $addFlg1;

        $data["INCOME_1"][NUMBER]                   = strlen($model->field["REDUC_INCOME_1"][$rowCnt]) >   0 ? $model->field["REDUC_INCOME_1"][$rowCnt]   : NULL;
        $data["INCOME_1_1"][NUMBER]                 = strlen($model->field["REDUC_INCOME_1_1"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_1"][$rowCnt] : NULL;
        $data["INCOME_1_2"][NUMBER]                 = strlen($model->field["REDUC_INCOME_1_2"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_2"][$rowCnt] : NULL;
        $data["INCOME_1_3"][NUMBER]                 = strlen($model->field["REDUC_INCOME_1_3"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_3"][$rowCnt] : NULL;
        $data["INCOME_1_4"][NUMBER]                 = strlen($model->field["REDUC_INCOME_1_4"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_4"][$rowCnt] : NULL;

        $data["RARE_CASE_CD_2"][TEXT]               = $model->field["COUNTRY_RARE_CASE_CD"][$rowCnt] != "" ? $model->field["COUNTRY_RARE_CASE_CD"][$rowCnt] : NULL;
        $data["SEQ_2"][NUMBER]                      = is_numeric($model->field["REDUCTION_SEQ_2"][$rowCnt]) ? $model->field["REDUCTION_SEQ_2"][$rowCnt] : NULL;

        $data["RANK_2"][TEXT]                       = $model->field["COUNTRY_RANK_2"][$rowCnt];
        $data["MONEY_2"][NUMBER]                    = strlen($model->field["COUNTRY_MONEY_2"][$rowCnt]) > 0 ? $model->field["COUNTRY_MONEY_2"][$rowCnt]   : NULL;
        $data["DEC_FLG_2"][TEXT]                    = $baseFlg2;
        $data["ADD_MONEY_2"][NUMBER]                = strlen($model->field["COUNTRY_ADD_MONEY_2"][$rowCnt]) > 0 ? $model->field["COUNTRY_ADD_MONEY_2"][$rowCnt] : NULL;
        $data["ADD_DEC_FLG_2"][TEXT]                = $addFlg2;

        $data["INCOME_2"][NUMBER]                   = strlen($model->field["REDUC_INCOME_2"][$rowCnt])   > 0 ? $model->field["REDUC_INCOME_2"][$rowCnt]   : NULL;
        $data["INCOME_2_1"][NUMBER]                 = strlen($model->field["REDUC_INCOME_2_1"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_1"][$rowCnt] : NULL;
        $data["INCOME_2_2"][NUMBER]                 = strlen($model->field["REDUC_INCOME_2_2"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_2"][$rowCnt] : NULL;
        $data["INCOME_2_3"][NUMBER]                 = strlen($model->field["REDUC_INCOME_2_3"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_3"][$rowCnt] : NULL;
        $data["INCOME_2_4"][NUMBER]                 = strlen($model->field["REDUC_INCOME_2_4"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_4"][$rowCnt] : NULL;

        $data["REDUCTION_JUGYOURYOU"][NUMBER]       = $model->slipSchoolStd[$slipNo]["SAKI1"];
        $data["REDUCTION_JUGYOURYOUTOU"][NUMBER]    = $model->slipSchoolStd[$slipNo]["SAKI2"];

        $data["REMARK"][TEXT]                       = $model->field["REDUC_REMARK"][$rowCnt] ? $model->field["REDUC_REMARK"][$rowCnt] : NULL;
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";

        $query  = " SELECT * FROM";
        $query .= "     REDUCTION_COUNTRY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR        = '{$model->exeYear}' ";
        $query .= "     AND SLIP_NO     = '{$slipNo}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            /**********/
            /* UPDATE */
            /**********/

            $where  = " WHERE ";
            $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR        = '{$model->exeYear}' ";
            $where .= "     AND SLIP_NO     = '{$slipNo}' ";

            $query = Query::updateSQL($data, "REDUCTION_COUNTRY_DAT", $where);
            $result = $db->query($query);
        } else {

            /**********/
            /* INSERT */
            /**********/
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $model->schoolKindAry[$model->search["GRADE"]];
            $data["YEAR"][TEXT]         = $model->exeYear;
            $data["SLIP_NO"][TEXT]      = $slipNo;

            $query = Query::insertSQL($data, "REDUCTION_COUNTRY_DAT");
            $result = $db->query($query);
        }

        /******************************/
        /* REDUCTION_COUNTRY_PLAN_DAT */
        /******************************/
        //計画データの有無
        $planCnt = $db->getRow(knjp727Query::getPlanCnt($model, $slipNo), DB_FETCHMODE_ASSOC);
        $zenkiData = array("DEC" => $baseFlg1 == "1" ? $model->field["COUNTRY_MONEY_1"][$rowCnt] : NULL,
                           "ADD" => $addFlg1  == "1" ? $model->field["COUNTRY_ADD_MONEY_1"][$rowCnt] : NULL);
        $koukiData = array("DEC" => $baseFlg2 == "1" ? $model->field["COUNTRY_MONEY_2"][$rowCnt] : NULL,
                           "ADD" => $addFlg2  == "1" ? $model->field["COUNTRY_ADD_MONEY_2"][$rowCnt] : NULL);

        $planCnt["ZENKI_FLG"]       = ($baseFlg1 == '1' && $row["DEC_FLG_1"] != "1") ? 1 : 0;
        $planCnt["ZENKI_ADD_FLG"]   = ($addFlg1 == '1' && $row["ADD_DEC_FLG_1"] != "1") ? 1 : 0;
        $planCnt["KOUKI_FLG"]       = ($baseFlg2 == '1' && $row["DEC_FLG_2"] != "1") ? 1 : 0;
        $planCnt["KOUKI_ADD_FLG"]   = ($addFlg2 == '1' && $row["ADD_DEC_FLG_2"] != "1") ? 1 : 0;

        //計画データ削除(確定フラグ:OFF)
        if ($baseFlg1 != "1" && $addFlg1 != "1" && $baseFlg2 != "1" && $addFlg2 != "1") {
            $query = knjp727Query::planDecisionMoneyAllClear($model, $slipNo);
            $db->query($query);
        } else {
            //前期
            $zenkiMonthArray = array("04", "05", "06");
            if ($baseFlg1 != "1") {
                $query = knjp727Query::planMoneyClear($model, $slipNo, "", $zenkiMonthArray);
                $db->query($query);
                $query = knjp727Query::planDecisionMoneyClear($model, $slipNo, "", $zenkiMonthArray);
                $db->query($query);
            }
            if ($addFlg1 != "1") {
                $query = knjp727Query::planMoneyClear($model, $slipNo, "ADD_", $zenkiMonthArray);
                $db->query($query);
                if ($baseFlg1 != "1") {
                    $query = knjp727Query::planDecisionMoneyClear($model, $slipNo, "ADD_", $zenkiMonthArray);
                    $db->query($query);
                }
            }
            //後期
            $koukiMonthArray = array("07", "08", "09", "10", "11", "12", "01", "02", "03");
            if ($baseFlg2 != "1") {
                $query = knjp727Query::planMoneyClear($model, $slipNo, "", $koukiMonthArray);
                $db->query($query);
                $query = knjp727Query::planDecisionMoneyClear($model, $slipNo, "", $koukiMonthArray);
                $db->query($query);
            }
            if ($addFlg2 != "1") {
                $query = knjp727Query::planMoneyClear($model, $slipNo, "ADD_", $koukiMonthArray);
                $db->query($query);
                if ($baseFlg2 != "1") {
                    $query = knjp727Query::planDecisionMoneyClear($model, $slipNo, "ADD_", $koukiMonthArray);
                    $db->query($query);
                }
            }
        }

        if ($baseDummy1 == "1") {
            $zenkiMonth = array("06");
            $koukiMonth = array("03");
        } else {
            $zenkiMonth = array("04", "05", "06");
            $koukiMonth = array("07", "08", "09", "10", "11", "12", "01", "02", "03");
        }
        $setDecisionMoneyArray = array();
        $setDecisionMoney = array();
        $checkMonth1 = $model->decMonth["COUNTRY_DEC_MONTH1"] < "04" ? $model->decMonth["COUNTRY_DEC_MONTH1"] + 12 : $model->decMonth["COUNTRY_DEC_MONTH1"];
        $checkMonth2 = $model->decMonth["COUNTRY_DEC_MONTH2"] < "04" ? $model->decMonth["COUNTRY_DEC_MONTH2"] + 12 : $model->decMonth["COUNTRY_DEC_MONTH2"];
        if ($planCnt["ALLCNT"] == 0) {
            foreach ($zenkiMonth as $key => $val) {
                $setDecisionMoney["BASE"] = $zenkiData["DEC"];
                $setDecisionMoney["ADD"] = $zenkiData["ADD"];
                if ($val <= $checkMonth1) {
                    $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH1"]] += $zenkiData["DEC"];
                    $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH1"]] += $zenkiData["ADD"];
                    $setDecisionMoney["BASE"] = 0;
                    $setDecisionMoney["ADD"] = 0;
                }
                knjp727Query::insertPlan($db, $model, $slipNo, $schregNo, $val, $zenkiData, $setDecisionMoney);
            }
            foreach ($koukiMonth as $key => $val) {
                $setDecisionMoney["BASE"] = $koukiData["DEC"];
                $setDecisionMoney["ADD"] = $koukiData["ADD"];
                $checkVal = $val < "04" ? $val + 12 : $val;
                if ($checkVal <= $checkMonth2) {
                    $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH2"]] += $koukiData["DEC"];
                    $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH2"]] += $koukiData["ADD"];
                    $setDecisionMoney["BASE"] = 0;
                    $setDecisionMoney["ADD"] = 0;
                }
                knjp727Query::insertPlan($db, $model, $slipNo, $schregNo, $val, $koukiData, $setDecisionMoney);
            }
        } else {
            if ($planCnt["ZENKI_FLG"] == "1" || $planCnt["ZENKI_ADD_FLG"] == "1") {
                foreach ($zenkiMonth as $key => $val) {
                    $setDecisionMoney["BASE"] = $zenkiData["DEC"];
                    $setDecisionMoney["ADD"] = $zenkiData["ADD"];
                    if ($val <= $checkMonth1) {
                        $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH1"]] += $setDecisionMoney["BASE"];
                        $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH1"]] += $setDecisionMoney["ADD"];
                        $setDecisionMoney["BASE"] = 0;
                        $setDecisionMoney["ADD"] = 0;
                    }
                    knjp727Query::updatePlan($db, $model, $slipNo, $val, $zenkiData, $planCnt, "ZENKI", $setDecisionMoney);
                }
            }
            if ($planCnt["KOUKI_FLG"] == "1" || $planCnt["KOUKI_ADD_FLG"] == "1") {
                foreach ($koukiMonth as $key => $val) {
                    $setDecisionMoney["BASE"] = $koukiData["DEC"];
                    $setDecisionMoney["ADD"] = $koukiData["ADD"];
                    $checkVal = $val < "04" ? $val + 12 : $val;
                    if ($checkVal <= $checkMonth2) {
                        $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH2"]] += $setDecisionMoney["BASE"];
                        $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH2"]] += $setDecisionMoney["ADD"];
                        $setDecisionMoney["BASE"] = 0;
                        $setDecisionMoney["ADD"] = 0;
                    }
                    knjp727Query::updatePlan($db, $model, $slipNo, $val, $koukiData, $planCnt, "KOUKI", $setDecisionMoney);
                }
            }
        }
        //確定月以前の確定金額を確定月に寄せる処理
        knjp727Query::updateCountryfDecPlan($db, $model, $slipNo, $model->decMonth["COUNTRY_DEC_MONTH1"], $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH1"]], "");
        knjp727Query::updateCountryfDecPlan($db, $model, $slipNo, $model->decMonth["COUNTRY_DEC_MONTH1"], $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH1"]], "ADD_");
        knjp727Query::updateCountryfDecPlan($db, $model, $slipNo, $model->decMonth["COUNTRY_DEC_MONTH2"], $setDecisionMoneyArray["BASE"][$model->decMonth["COUNTRY_DEC_MONTH2"]], "");
        knjp727Query::updateCountryfDecPlan($db, $model, $slipNo, $model->decMonth["COUNTRY_DEC_MONTH2"], $setDecisionMoneyArray["ADD"][$model->decMonth["COUNTRY_DEC_MONTH2"]], "ADD_");
    }

    function getPrefClearDecisionMonth($model, $slipNo, $money) {
        $query  = " SELECT ";
        $query .= "     MAX(PLAN_MONTH) AS MAX_MONTH, ";
        $query .= "     MIN(PLAN_MONTH) AS MIN_MONTH ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR = '{$model->exeYear}' ";
        $query .= "     AND SLIP_NO = '{$slipNo}' ";
        $query .= "     AND VALUE(DECISION_MONEY, 0) > {$money} ";

        return $query;
    }

    function prefUpdate($db, $model, $slipNo, $schregNo, $rowCnt) {
        /*****************/
        /* REDUCTION_DAT */
        /*****************/
        if (is_array($model->field["PREF_FLG_1"])) {
            $prefFlg1 = in_array($slipNo, $model->field["PREF_FLG_1"])? 1 : NULL;
        } else {
            $prefFlg1 = NULL;
        }
        if (is_array($model->field["PREF_FLG_2"])) {
            $prefFlg2 = in_array($slipNo, $model->field["PREF_FLG_2"])? 1 : NULL;
        } else {
            $prefFlg2 = NULL;
        }

        $data = array();
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        $data["PREFECTURESCD"][TEXT]            = $model->slipnoPref[$slipNo];
        $data["GRADE"][TEXT]                    = $model->search["GRADE"];

        list($rareCd, $baseDummy1) = preg_split("/:/", $model->field["PREF_RARE_CASE_CD"][$rowCnt]);
        $model->field["PREF_RARE_CASE_CD"][$rowCnt] = ($rareCd == 'DUMMY') ? '' : $rareCd;
        $data["RARE_CASE_CD_1"][TEXT]           = $model->field["PREF_RARE_CASE_CD"][$rowCnt] != "" ? $model->field["PREF_RARE_CASE_CD"][$rowCnt] : NULL;

        $data["SEQ_1"][NUMBER]                  = is_numeric($model->field["REDUCTION_SEQ_1"][$rowCnt]) ? $model->field["REDUCTION_SEQ_1"][$rowCnt] : NULL;
        $data["RANK_1"][TEXT]                   = $model->field["PREF_RANK_1"][$rowCnt];
        $data["MONEY_1"][NUMBER]                = strlen($model->field["PREF_MONEY_1"][$rowCnt]) > 0 ? $model->field["PREF_MONEY_1"][$rowCnt] : NULL;
        $data["DEC_FLG_1"][TEXT]                = $prefFlg1;

        $data["INCOME_1"][NUMBER]               = strlen($model->field["REDUC_INCOME_1"][$rowCnt])   > 0 ? $model->field["REDUC_INCOME_1"][$rowCnt]   : NULL;
        $data["INCOME_1_1"][NUMBER]             = strlen($model->field["REDUC_INCOME_1_1"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_1"][$rowCnt] : NULL;
        $data["INCOME_1_2"][NUMBER]             = strlen($model->field["REDUC_INCOME_1_2"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_2"][$rowCnt] : NULL;
        $data["INCOME_1_3"][NUMBER]             = strlen($model->field["REDUC_INCOME_1_3"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_3"][$rowCnt] : NULL;
        $data["INCOME_1_4"][NUMBER]             = strlen($model->field["REDUC_INCOME_1_4"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_1_4"][$rowCnt] : NULL;

        $data["RARE_CASE_CD_2"][TEXT]           = $model->field["PREF_RARE_CASE_CD"][$rowCnt] != "" ? $model->field["PREF_RARE_CASE_CD"][$rowCnt] : NULL;
        $data["SEQ_2"][NUMBER]                  = is_numeric($model->field["REDUCTION_SEQ_2"][$rowCnt]) ? $model->field["REDUCTION_SEQ_2"][$rowCnt] : NULL;

        $data["RANK_2"][TEXT]                   = $model->field["PREF_RANK_2"][$rowCnt];
        $data["MONEY_2"][NUMBER]                = strlen($model->field["PREF_MONEY_2"][$rowCnt]) > 0 ? $model->field["PREF_MONEY_2"][$rowCnt]   : NULL;
        $data["DEC_FLG_2"][TEXT]                = $prefFlg2;

        $data["INCOME_2"][NUMBER]               = strlen($model->field["REDUC_INCOME_2"][$rowCnt])   > 0 ? $model->field["REDUC_INCOME_2"][$rowCnt]   : NULL;
        $data["INCOME_2_1"][NUMBER]             = strlen($model->field["REDUC_INCOME_2_1"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_1"][$rowCnt] : NULL;
        $data["INCOME_2_2"][NUMBER]             = strlen($model->field["REDUC_INCOME_2_2"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_2"][$rowCnt] : NULL;
        $data["INCOME_2_3"][NUMBER]             = strlen($model->field["REDUC_INCOME_2_3"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_3"][$rowCnt] : NULL;
        $data["INCOME_2_4"][NUMBER]             = strlen($model->field["REDUC_INCOME_2_4"][$rowCnt]) > 0 ? $model->field["REDUC_INCOME_2_4"][$rowCnt] : NULL;

        $data["REMARK"][TEXT]                   = $model->field["REDUC_REMARK"][$rowCnt] ? $model->field["REDUC_REMARK"][$rowCnt] : NULL;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query  = " SELECT * FROM";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR        = '{$model->exeYear}' ";
        $query .= "     AND SLIP_NO     = '{$slipNo}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            /**********/
            /* UPDATE */
            /**********/

            $where  = " WHERE ";
            $where .= "     SCHOOLCD               = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND        = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR               = '{$model->exeYear}' ";
            $where .= "     AND SLIP_NO            = '{$slipNo}' ";

            $query = Query::updateSQL($data, "REDUCTION_DAT", $where);
            $result = $db->query($query);
        } else {

            /**********/
            /* INSERT */
            /**********/
            $data["SCHOOLCD"][TEXT]           = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]        = $model->schoolKindAry[$model->search["GRADE"]];
            $data["YEAR"][TEXT]               = $model->exeYear;
            $data["REDUCTION_TARGET"][TEXT]   = '1';
            $data["SLIP_NO"][TEXT]            = $slipNo;

            $query = Query::insertSQL($data, "REDUCTION_DAT");
            $result = $db->query($query);
        }

        /**********************/
        /* REDUCTION_PLAN_DAT */
        /**********************/
        //メインデータ取得
        //計画データの有無
        $prefPlanCnt = $db->getRow(knjp727Query::getPrefPlanCnt($model, $slipNo), DB_FETCHMODE_ASSOC);
        $zenkiData = array("DEC" => $prefFlg1 == "1" ? $model->field["PREF_MONEY_1"][$rowCnt] : NULL);
        $koukiData = array("DEC" => $prefFlg2 == "1" ? $model->field["PREF_MONEY_2"][$rowCnt] : NULL);
        if ($baseDummy1 == "1") {
            $zenkiMonth = array("06");
            $koukiMonth = array("03");
        } else {
            $zenkiMonth = array("04", "05", "06");
            $koukiMonth = array("07", "08", "09", "10", "11", "12", "01", "02", "03");
        }
        $monthMoney = $zenkiData["DEC"] / 3;
        $monthMoneyHasuu = $monthMoney + ($zenkiData["DEC"] % 3);
        $zenkiPrefMoney = $monthMoneyHasuu ? $monthMoneyHasuu : 0;
        $query = knjp727Query::getPrefClearDecisionMonth($model, $slipNo, $zenkiPrefMoney);
        $decisionMonthRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $clearDecisionMonth = $decisionMonthRow["MAX_MONTH"] != $decisionMonthRow["MIN_MONTH"] ? $decisionMonthRow["MAX_MONTH"] : "";

        //計画データ削除(確定フラグ:OFF)
        if ($prefFlg1 != "1") {
            $query = knjp727Query::prefPlanMoneyClear($model, $slipNo, "", array("04", "05", "06"));
            $db->query($query);
            $query = knjp727Query::planPrefDecisionMoneyClear($model, $slipNo, "", $decisionMonthRow["MIN_MONTH"]);
            $db->query($query);
        }
        if ($prefFlg2 != "1") {
            $query = knjp727Query::prefPlanMoneyClear($model, $slipNo, "", array("07", "08", "09", "10", "11", "12", "01", "02", "03"));
            $db->query($query);
            if ($clearDecisionMonth) {
                $query = knjp727Query::planPrefDecisionMoneyClear($model, $slipNo, "", $clearDecisionMonth);
                $db->query($query);
            }
        }

        $setDecisionMoneyArray = array();
        $setMoney = 0;
        $setDecisionMoney = 0;
        $checkMonth1 = $model->decMonth["PREF_DEC_MONTH1"] < "04" ? $model->decMonth["PREF_DEC_MONTH1"] + 12 : $model->decMonth["PREF_DEC_MONTH1"];
        $checkMonth2 = $model->decMonth["PREF_DEC_MONTH2"] < "04" ? $model->decMonth["PREF_DEC_MONTH2"] + 12 : $model->decMonth["PREF_DEC_MONTH2"];
        if ($prefPlanCnt["ALLCNT"] == 0) {
            $monthMoney = $zenkiData["DEC"] / 3;
            $monthMoneyHasuu = $monthMoney + ($zenkiData["DEC"] % 3);
            foreach ($zenkiMonth as $key => $val) {
                if ($val == "06") {
                    $setMoney = $monthMoneyHasuu;
                    $setDecisionMoney = $monthMoneyHasuu;
                } else {
                    $setMoney = $monthMoney;
                    $setDecisionMoney = $monthMoney;
                }
                if ($val <= $checkMonth1) {
                    $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH1"]] += $setMoney;
                    $setDecisionMoney = 0;
                }
                $setMoney = $prefFlg1 == "1" ? $setMoney : NULL;
                knjp727Query::insertPrefPlan($db, $model, $slipNo, $schregNo, $val, $setMoney, $setDecisionMoney);
            }
            $monthMoney = $koukiData["DEC"] / 9;
            $monthMoneyHasuu = $monthMoney + ($koukiData["DEC"] % 9);
            foreach ($koukiMonth as $key => $val) {
                if ($val == "03") {
                    $setMoney = $monthMoneyHasuu;
                    $setDecisionMoney = $monthMoneyHasuu;
                } else {
                    $setMoney = $monthMoney;
                    $setDecisionMoney = $monthMoney;
                }
                $checkVal = $val < "04" ? $val + 12 : $val;
                if ($checkVal <= $checkMonth2) {
                    $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH2"]] += $setMoney;
                    $setDecisionMoney = 0;
                }
                $setMoney = $prefFlg2 == "1" ? $setMoney : NULL;
                knjp727Query::insertPrefPlan($db, $model, $slipNo, $schregNo, $val, $setMoney, $setDecisionMoney);
            }
            knjp727Query::updatePrefDecPlan($db, $model, $slipNo, $model->decMonth["PREF_DEC_MONTH1"], $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH1"]]);
            knjp727Query::updatePrefDecPlan($db, $model, $slipNo, $model->decMonth["PREF_DEC_MONTH2"], $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH2"]]);
        } else {
            $monthMoney = $zenkiData["DEC"] / 3;
            $monthMoneyHasuu = $monthMoney + ($zenkiData["DEC"] % 3);
            foreach ($zenkiMonth as $key => $val) {
                if ($val == "06") {
                    $setMoney = $monthMoneyHasuu;
                    $setDecisionMoney = $monthMoneyHasuu;
                } else {
                    $setMoney = $monthMoney;
                    $setDecisionMoney = $monthMoney;
                }
                if ($val <= $checkMonth1) {
                    $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH1"]] += $setMoney;
                    $setDecisionMoney = 0;
                }
                $setMoney = $prefFlg1 == "1" ? $setMoney : NULL;
                knjp727Query::updatePrefPlan($db, $model, $slipNo, $val, $setMoney, $prefPlanCnt, "ZENKI", $setDecisionMoney);
            }
            $monthMoney = $koukiData["DEC"] / 9;
            $monthMoneyHasuu = $monthMoney + ($koukiData["DEC"] % 9);
            foreach ($koukiMonth as $key => $val) {
                if ($val == "03") {
                    $setMoney = $monthMoneyHasuu;
                    $setDecisionMoney = $monthMoneyHasuu;
                } else {
                    $setMoney = $monthMoney;
                    $setDecisionMoney = $monthMoney;
                }
                $checkVal = $val < "04" ? $val + 12 : $val;
                if ($checkVal <= $checkMonth2) {
                    $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH2"]] += $setMoney;
                    $setDecisionMoney = 0;
                }
                $setMoney = $prefFlg2 == "1" ? $setMoney : NULL;
                knjp727Query::updatePrefPlan($db, $model, $slipNo, $val, $setMoney, $prefPlanCnt, "KOUKI", $setDecisionMoney);
            }
            knjp727Query::updatePrefDecPlan($db, $model, $slipNo, $model->decMonth["PREF_DEC_MONTH1"], $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH1"]]);
            knjp727Query::updatePrefDecPlan($db, $model, $slipNo, $model->decMonth["PREF_DEC_MONTH2"], $setDecisionMoneyArray[$model->decMonth["PREF_DEC_MONTH2"]]);
        }
    }

    function reductionSchoolUpdate($db, $model, $slipNo, $schregNo, $rowCnt) {
        if (is_array($model->field["REDUC_SCHOOL_FLG_1"])) {
            $reducSchoolFlg1 = in_array($slipNo, $model->field["REDUC_SCHOOL_FLG_1"])? 1 : NULL;
        } else {
            $reducSchoolFlg1 = NULL;
        }
        if (is_array($model->field["REDUC_SCHOOL_FLG_2"])) {
            $reducSchoolFlg2 = in_array($slipNo, $model->field["REDUC_SCHOOL_FLG_2"])? 1 : NULL;
        } else {
            $reducSchoolFlg2 = NULL;
        }

        /************************/
        /* REDUCTION_SCHOOL_DAT */
        /************************/
        knjp727Query::setReductionSchoolDat($db, $model, $slipNo, $schregNo, $model->slipSchoolStd[$slipNo]["ATO1"], $model->slipSchoolStd[$slipNo]["ATO2"], $rowCnt, "1", $reducSchoolFlg2);
        knjp727Query::setReductionSchoolDat($db, $model, $slipNo, $schregNo, $model->slipSchoolStd[$slipNo]["SAKI1"], $model->slipSchoolStd[$slipNo]["SAKI2"], $rowCnt, "2", $reducSchoolFlg1);

        /**********************/
        /* REDUCTION_PLAN_DAT */
        /**********************/
        knjp727Query::setReductionSchoolPlanDat($db, $model, $slipNo, $schregNo, $model->slipSchoolStd[$slipNo]["ATO1"], $model->slipSchoolStd[$slipNo]["ATO2"], $rowCnt, "1", $reducSchoolFlg2);
        knjp727Query::setReductionSchoolPlanDat($db, $model, $slipNo, $schregNo, $model->slipSchoolStd[$slipNo]["SAKI1"], $model->slipSchoolStd[$slipNo]["SAKI2"], $rowCnt, "2", $reducSchoolFlg1);
    }

    function setReductionSchoolDat($db, $model, $slipNo, $schregNo, $money1, $money2, $rowCnt, $reducTarget, $reducSchoolFlg) {
        $data = array();
        $data["SCHREGNO"][TEXT]                 = $schregNo;

        $data["MONEY_1"][NUMBER]                = strlen($money1) > 0 ? $money1 : NULL;
        $data["DEC_FLG_1"][TEXT]                = $reducSchoolFlg;

        $data["MONEY_2"][NUMBER]                = strlen($money2) > 0 ? $money2 : NULL;
        $data["DEC_FLG_2"][TEXT]                = $reducSchoolFlg;

        $data["REMARK"][TEXT]                   = $model->field["REDUC_REMARK"][$rowCnt] ? $model->field["REDUC_REMARK"][$rowCnt] : NULL;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query  = " SELECT * FROM";
        $query .= "     REDUCTION_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND   = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $query .= "     AND YEAR          = '{$model->exeYear}' ";
        $query .= "     AND SLIP_NO       = '{$slipNo}' ";
        $query .= "     AND REDUCTION_TARGET = '{$reducTarget}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($row)) {
            /**********/
            /* UPDATE */
            /**********/

            $where  = " WHERE ";
            $where .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND   = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR          = '{$model->exeYear}' ";
            $where .= "     AND SLIP_NO       = '{$slipNo}' ";
            $where .= "     AND REDUCTION_TARGET = '{$reducTarget}' ";

            $query = Query::updateSQL($data, "REDUCTION_SCHOOL_DAT", $where);
            $result = $db->query($query);
        } else {

            /**********/
            /* INSERT */
            /**********/
            $data["SCHOOLCD"][TEXT]      = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]   = $model->schoolKindAry[$model->search["GRADE"]];
            $data["YEAR"][TEXT]          = $model->exeYear;
            $data["SLIP_NO"][TEXT]       = $slipNo;
            $data["REDUCTION_TARGET"][TEXT] = $reducTarget;

            $query = Query::insertSQL($data, "REDUCTION_SCHOOL_DAT");
            $result = $db->query($query);
        }
    }

    function setReductionSchoolPlanDat($db, $model, $slipNo, $schregNo, $money1, $money2, $rowCnt, $reducTarget, $reducSchoolFlg) {
        //計画データの有無
        $reducSchoolPlanCnt = $db->getRow(knjp727Query::getReducSchoolPlanCnt($model, $slipNo, $reducTarget), DB_FETCHMODE_ASSOC);

        $setDataArray = array("04" => NULL,
                              "05" => NULL,
                              "06" => NULL,
                              "07" => NULL,
                              "08" => NULL,
                              "09" => NULL,
                              "10" => NULL,
                              "11" => NULL,
                              "12" => NULL,
                              "01" => NULL,
                              "02" => NULL,
                              "03" => NULL,);
        //減免先
        if ($model->reducStdSaki[$schregNo]["REDUCTION_TARGET"] == $reducTarget) {
            $sakiInfo = $model->reducStdSaki[$schregNo];
            //授業料
            for ($startM = $sakiInfo["S_MONTH"]; $startM <= $sakiInfo["E_MONTH"]; $startM++) {
                $setMonth = $startM > "12" ? ($startM - 12) : $startM;
                $setMonth = sprintf("%02d", $setMonth);
                $setDataArray[$setMonth] += $sakiInfo["MONTH_MONEY"];
            }
            //授業料等
            for ($startM = $sakiInfo["S_MONTH_TOU"]; $startM <= $sakiInfo["E_MONTH_TOU"]; $startM++) {
                $setMonth = $startM > "12" ? ($startM - 12) : $startM;
                $setMonth = sprintf("%02d", $setMonth);
                $setDataArray[$setMonth] += $sakiInfo["MONTH_MONEY_TOU"];
            }
        }

        //減免後
        foreach ($model->reducStdAto[$schregNo] as $scoolStdKey => $scoolStdVal) {
            if ($scoolStdVal["REDUCTION_TARGET"] == $reducTarget) {
                //授業料
                for ($startM = $scoolStdVal["S_MONTH"]; $startM <= $scoolStdVal["E_MONTH"]; $startM++) {
                    $setMonth = $startM > "12" ? ($startM - 12) : $startM;
                    $setMonth = sprintf("%02d", $setMonth);
                    $setDataArray[$setMonth] += $scoolStdVal["MONTH_MONEY"];
                }
                //授業料等
                for ($startM = $scoolStdVal["S_MONTH_TOU"]; $startM <= $scoolStdVal["E_MONTH_TOU"]; $startM++) {
                    $setMonth = $startM > "12" ? ($startM - 12) : $startM;
                    $setMonth = sprintf("%02d", $setMonth);
                    $setDataArray[$setMonth] += $scoolStdVal["MONTH_MONEY_TOU"];
                }
            }
        }

        //計画データ削除(確定フラグ:OFF)
        if ($reducSchoolFlg != "1") {
            $query = knjp727Query::planReducSchoolMoneyClear($model, $slipNo, $reducTarget);
            $db->query($query);
        }

        if ($reducSchoolFlg == "1") {
            if ($reducSchoolPlanCnt["ALLCNT"] == 0) {
                foreach ($setDataArray as $setMonth => $setData) {
                    $setMoney = $setData;
                    $setDecisionMoney = $setData;
                    $setMoney = $reducSchoolFlg == "1" ? $setMoney : NULL;
                    $setDecisionMoney = $reducSchoolFlg == "1" ? $setDecisionMoney : NULL;
                    knjp727Query::insertReducSchoolPlan($db, $model, $slipNo, $reducTarget, $schregNo, $setMonth, $setMoney, $setDecisionMoney);
                }
            } else {
                foreach ($setDataArray as $setMonth => $setData) {
                    $setMoney = $setData;
                    $setDecisionMoney = $setData;
                    $setMoney = $reducSchoolFlg == "1" ? $setMoney : NULL;
                    $setDecisionMoney = $reducSchoolFlg == "1" ? $setDecisionMoney : NULL;
                    knjp727Query::updateReducSchoolPlan($db, $model, $slipNo, $reducTarget, $setMonth, $setMoney, $setDecisionMoney);
                }
            }
        }
    }

    function getRaducRareCase($schregNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->exeYear}' ";
        $query .= "     AND SCHREGNO    = '{$schregNo}' ";
        $query .= "     AND REDUC_RARE_CASE_CD    = 'T' ";

        return $query;
    }

    function getRaducIncome1($schregNo) {
        $query  = " SELECT ";
        $query .= "     REDUC_INCOME_1, ";
        $query .= "     REDUC_INCOME_2 ";
        $query .= " FROM ";
        $query .= "     REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->exeYear}' ";
        $query .= "     AND SCHREGNO    = '{$schregNo}' ";

        return $query;
    }

    function delReductionDat($schregNo) {
        $query  = " DELETE FROM REDUCTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->exeYear}' AND ";
        $query .= "     SCHREGNO    = '{$schregNo}' ";

        return $query;
    }

    function planMoneyClear($model, $slipNo, $fieldName, $monthArray) {
        $data = array();

        $data[$fieldName."PLAN_MONEY"][NUMBER]      = 0;
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";

        $instate = "(";
        $sep = "";
        foreach ($monthArray as $key => $month) {
            $setYear = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
            $instate .= $sep."'{$setYear}{$month}'";
            $sep = ",";
        }
        $instate .= ")";
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR || PLAN_MONTH IN {$instate} ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);

        return $query;
    }

    function planDecisionMoneyAllClear($model, $slipNo) {
        $data = array();

        $data["PLAN_MONEY"][NUMBER]             = 0;
        $data["ADD_PLAN_MONEY"][NUMBER]         = 0;
        $data["DECISION_MONEY"][NUMBER]         = 0;
        $data["ADD_DECISION_MONEY"][NUMBER]     = 0;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);
        return $query;
    }

    function planDecisionMoneyClear($model, $slipNo, $fieldName, $monthArray) {
        $data = array();

        $data[$fieldName."DECISION_MONEY"][NUMBER]  = 0;
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";

        $instate = "(";
        $sep = "";
        foreach ($monthArray as $key => $month) {
            $setYear = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
            $instate .= $sep."'{$setYear}{$month}'";
            $sep = ",";
        }
        $instate .= ")";
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '".$model->exeYear ."' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR || PLAN_MONTH IN {$instate} ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);
        return $query;
    }

    function prefPlanMoneyClear($model, $slipNo, $fieldName, $monthArray) {
        $data = array();

        $data[$fieldName."PLAN_MONEY"][NUMBER]      = NULL;
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";

        $instate = "(";
        $sep = "";
        foreach ($monthArray as $key => $month) {
            $setYear = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
            $instate .= $sep."'{$setYear}{$month}'";
            $sep = ",";
        }
        $instate .= ")";
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR || PLAN_MONTH IN {$instate} ";

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        return $query;
    }

    function planPrefDecisionMoneyClear($model, $slipNo, $fieldName, $clearDecisionMonth) {
        $data = array();

        $data[$fieldName."DECISION_MONEY"][NUMBER]  = NULL;
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";

        $setYear = $clearDecisionMonth < "04" ? $model->exeYear + 1 : $model->exeYear;

        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR || PLAN_MONTH >= '{$setYear}{$clearDecisionMonth}' ";

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        return $query;
    }

    function planReducSchoolMoneyClear($model, $slipNo, $reducTarget) {
        $data = array();

        $data["PLAN_MONEY"][NUMBER]     = NULL;
        $data["DECISION_MONEY"][NUMBER] = NULL;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD             = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND      = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR             = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO          = '{$slipNo}' ";
        $where .= "     AND REDUCTION_TARGET = '{$reducTarget}' ";

        $query = Query::updateSQL($data, "REDUCTION_SCHOOL_PLAN_DAT", $where);

        return $query;
    }

    function insertPlan($db, $model, $slipNo, $schregNo, $month, $moneyData, $setDecisionMoney) {
        $data = array();

        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKindAry[$model->search["GRADE"]];
        $data["YEAR"][TEXT]                 = $model->exeYear;
        $data["SLIP_NO"][TEXT]              = $slipNo;
        $data["SCHREGNO"][TEXT]             = $schregNo;
        $data["PLAN_YEAR"][TEXT]            = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
        $data["PLAN_MONTH"][TEXT]           = $month;
        $data["PLAN_MONEY"][NUMBER]         = $moneyData["DEC"];
        $data["ADD_PLAN_MONEY"][NUMBER]     = $moneyData["ADD"];
        $data["DECISION_MONEY"][NUMBER]     = $setDecisionMoney["BASE"];
        $data["ADD_DECISION_MONEY"][NUMBER] = $setDecisionMoney["ADD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_COUNTRY_PLAN_DAT");
        $db->query($query);
    }

    function updatePlan($db, $model, $slipNo, $month, $moneyData, $planCnt, $div, $setDecisionMoney) {
        $data = array();

        if ($planCnt[$div."_FLG"] == "1") {
            $data["PLAN_MONEY"][NUMBER]         = $moneyData["DEC"];
            $data["DECISION_MONEY"][NUMBER]     = $setDecisionMoney["BASE"];
        }
        if ($planCnt[$div."_ADD_FLG"] == "1") {
            $data["ADD_PLAN_MONEY"][NUMBER]     = $moneyData["ADD"];
            $data["ADD_DECISION_MONEY"][NUMBER] = $setDecisionMoney["ADD"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $setYear = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR   = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH  = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);
        $db->query($query);
    }

    function updateCountryfDecPlan($db, $model, $slipNo, $month, $setDecisionMoney, $setDiv) {
        if (!$setDecisionMoney) {
            return;
        }
        $data = array();

        $data[$setDiv."DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR   = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH  = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_COUNTRY_PLAN_DAT", $where);
        $db->query($query);
    }

    function insertPrefPlan($db, $model, $slipNo, $schregNo, $month, $setMoney, $setDecisionMoney) {
        $data = array();

        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKindAry[$model->search["GRADE"]];
        $data["YEAR"][TEXT]             = $model->exeYear;
        $data["REDUCTION_TARGET"][TEXT] = '1';
        $data["SLIP_NO"][TEXT]          = $slipNo;
        $data["PLAN_YEAR"][TEXT]        = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
        $data["PLAN_MONTH"][TEXT]       = $month;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["PLAN_MONEY"][NUMBER]     = $setMoney;
        $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_PLAN_DAT");
        $db->query($query);
    }

    function updatePrefPlan($db, $model, $slipNo, $month, $setMoney, $planCnt, $div, $setDecisionMoney) {
        $data = array();

        if ($planCnt[$div."_CNT"] == "0") {
            $data["PLAN_MONEY"][NUMBER]     = $setMoney;
        }
        $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR   = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH  = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        $db->query($query);
    }

    function updatePrefDecPlan($db, $model, $slipNo, $month, $setDecisionMoney) {
        $data = array();

        $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR        = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO     = '{$slipNo}' ";
        $where .= "     AND PLAN_YEAR   = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH  = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_PLAN_DAT", $where);
        $db->query($query);
    }

    function insertReducSchoolPlan($db, $model, $slipNo, $reducTarget, $schregNo, $month, $setMoney, $setDecisionMoney) {
        $data = array();

        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKindAry[$model->search["GRADE"]];
        $data["YEAR"][TEXT]             = $model->exeYear;
        $data["SLIP_NO"][TEXT]          = $slipNo;
        $data["REDUCTION_TARGET"][TEXT] = $reducTarget;
        $data["PLAN_YEAR"][TEXT]        = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
        $data["PLAN_MONTH"][TEXT]       = $month;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["PLAN_MONEY"][NUMBER]     = $setMoney;
        $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "REDUCTION_SCHOOL_PLAN_DAT");
        $db->query($query);
    }

    function updateReducSchoolPlan($db, $model, $slipNo, $reducTarget, $month, $setMoney, $setDecisionMoney) {
        $data = array();

        $data["PLAN_MONEY"][NUMBER]     = $setMoney;
        $data["DECISION_MONEY"][NUMBER] = $setDecisionMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $setYear = $month < "04" ? $model->exeYear + 1 : $model->exeYear;
        $where  = " WHERE ";
        $where .= "     SCHOOLCD              = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND       = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
        $where .= "     AND YEAR              = '{$model->exeYear}' ";
        $where .= "     AND SLIP_NO           = '{$slipNo}' ";
        $where .= "     AND REDUCTION_TARGET  = '{$reducTarget}' ";
        $where .= "     AND PLAN_YEAR         = '{$setYear}' ";
        $where .= "     AND PLAN_MONTH        = '{$month}' ";

        $query = Query::updateSQL($data, "REDUCTION_SCHOOL_PLAN_DAT", $where);
        $db->query($query);
    }

    //減免情報取得
    function selectQueryGenmen($model) {
        $query .= " SELECT ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_CD, ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_NAME, ";
        $query .= "     SCHOLAR_H.FROM_DATE, ";
        $query .= "     SCHOLAR_H.TO_DATE, ";
        $query .= "     CASE WHEN REDUC_SCHOOL.REDUCTION_DIV = '1' ";
        $query .= "          THEN '学力' ";
        $query .= "          ELSE '家族' ";
        $query .= "     END AS GENMEN_TYPE ";
        $query .= " FROM ";
        $query .= "     SCHREG_SCHOLARSHIP_HIST_DAT SCHOLAR_H, ";
        $query .= "     REDUCTION_SCHOOL_MST REDUC_SCHOOL ";
        $query .= " WHERE ";
        $query .= "     SCHOLAR_H.SCHREGNO = '{$model->genmenTransfer["SCHREGNO"]}' ";
        $query .= "     AND SCHOLAR_H.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOLAR_H.SCHOOL_KIND = '{$model->genmenTransfer["SCHOOL_KIND"]}' ";
        $query .= "     AND ('".$model->exeYear."-04-01' BETWEEN SCHOLAR_H.FROM_DATE AND SCHOLAR_H.TO_DATE ";
        $query .= "          OR ";
        $query .= "          '".($model->exeYear + 1)."-03-31' BETWEEN SCHOLAR_H.FROM_DATE AND SCHOLAR_H.TO_DATE ";
        $query .= "         ) ";
        $query .= "     AND SCHOLAR_H.SCHOOLCD = REDUC_SCHOOL.SCHOOLCD ";
        $query .= "     AND SCHOLAR_H.SCHOOL_KIND = REDUC_SCHOOL.SCHOOL_KIND ";
        $query .= "     AND REDUC_SCHOOL.YEAR = '{$model->exeYear}' ";
        $query .= "     AND SCHOLAR_H.SCHOLARSHIP = REDUC_SCHOOL.REDUCTION_DIV_CD ";
        $query .= " ORDER BY ";
        $query .= "     REDUC_SCHOOL.REDUCTION_DIV_CD, ";
        $query .= "     SCHOLAR_H.FROM_DATE ";
        return $query;
    }

    //移動情報取得
    function selectQueryTransfer($model) {
        //変数
        $sdate =  $model->exeYear      ."-04-01";
        $edate = ($model->exeYear + 1) ."-03-31";
        //SQL
        $query = "
            WITH TRANS_T AS (
            SELECT
                T1.TRANSFERCD,T1.TRANSFER_SDATE,T1.TRANSFER_EDATE,T2.NAME1
            FROM
                SCHREG_TRANSFER_DAT T1 LEFT OUTER JOIN NAME_MST T2
                                    ON T1.TRANSFERCD = T2.NAMECD2
                                    AND T2.NAMECD1 =  'A004'
            WHERE
                SCHREGNO = '".$model->genmenTransfer["SCHREGNO"]."'
                AND T1.TRANSFERCD IN ('1','2')
                AND ((T1.TRANSFER_SDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}'))
                 OR  (T1.TRANSFER_EDATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')))
            ), GRD_T AS (
            SELECT
                T1.GRD_DIV AS TRANSFERCD,T1.GRD_DATE AS TRANSFER_SDATE,CAST(NULL AS DATE) AS TRANSFER_EDATE,T2.NAME1
            FROM
                SCHREG_BASE_MST T1 LEFT OUTER JOIN NAME_MST T2
                                    ON T1.GRD_DIV = T2.NAMECD2
                                    AND T2.NAMECD1 =  'A003'
            WHERE
                SCHREGNO = '".$model->genmenTransfer["SCHREGNO"]."'
                AND T1.GRD_DIV > '1'
                AND T1.GRD_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')
            ), ENT_T AS (
            SELECT
                T1.ENT_DIV AS TRANSFERCD,T1.ENT_DATE AS TRANSFER_SDATE,CAST(NULL AS DATE) AS TRANSFER_EDATE,T2.NAME1
            FROM
                SCHREG_BASE_MST T1 LEFT OUTER JOIN NAME_MST T2
                                    ON T1.ENT_DIV = T2.NAMECD2
                                    AND T2.NAMECD1 =  'A002'
            WHERE
                SCHREGNO = '".$model->genmenTransfer["SCHREGNO"]."'
                AND T1.ENT_DIV > '3'
                AND T1.ENT_DATE BETWEEN DATE('{$sdate}') AND DATE('{$edate}')
            )
            SELECT
                *
            FROM
                ENT_T
            UNION ALL
            SELECT
                *
            FROM
                TRANS_T
            UNION ALL
            SELECT
                *
            FROM
                GRD_T
        ";
        return $query;
    }

    //軽減特殊コード取得
    function selectQuerySpecailCode($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS LABEL, ";
        $query .= "     NAMECD2 || ':' || NAMESPARE1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'P711' AND ";
        $query .= "     YEAR = '{$model->exeYear}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function updatePlanSum($db, $model, $slipNo, $schregNo) {

        $tableArray = array();

        //学校負担
        $tableArray[0]["TABLE"] = "REDUCTION_BURDEN_CHARGE_PLAN_DAT";
        $tableArray[0]["REDUCTION_TARGET"] = "1";
        $tableArray[0]["MONEY_DIV"] = "";

        //支援基準
        $tableArray[1]["TABLE"] = "REDUCTION_COUNTRY_PLAN_DAT";
        $tableArray[1]["REDUCTION_TARGET"] = "";
        $tableArray[1]["MONEY_DIV"] = "";

        //支援加算
        $tableArray[2]["TABLE"] = "REDUCTION_COUNTRY_PLAN_DAT";
        $tableArray[2]["REDUCTION_TARGET"] = "";
        $tableArray[2]["MONEY_DIV"] = "ADD_";

        //補助
        $tableArray[3]["TABLE"] = "REDUCTION_PLAN_DAT";
        $tableArray[3]["REDUCTION_TARGET"] = "1";
        $tableArray[3]["MONEY_DIV"] = "";

        //減免
        $tableArray[4]["TABLE"] = "REDUCTION_SCHOOL_PLAN_DAT";
        $tableArray[4]["REDUCTION_TARGET"] = "1";
        $tableArray[4]["MONEY_DIV"] = "";

        foreach ($tableArray as $key => $tableInfo) {
            $query  = " SELECT ";
            $query .= "     PLAN_T.SCHOOLCD, ";
            $query .= "     PLAN_T.SCHOOL_KIND, ";
            $query .= "     PLAN_T.YEAR, ";
            $query .= "     PLAN_T.SLIP_NO, ";
            $query .= "     YEAR(MAX(LIMIT_DATE.PAID_LIMIT_DATE)) AS PLAN_YEAR, ";
            $query .= "     LIMIT_DATE.PAID_LIMIT_MONTH AS PLAN_MONTH, ";
            $query .= "     PLAN_T.SCHREGNO, ";
            if ($tableInfo["TABLE"] == "REDUCTION_BURDEN_CHARGE_PLAN_DAT") {
                $query .= "     SUM(PLAN_T.BURDEN_CHARGE) AS BURDEN_CHARGE ";
            } else {
                $query .= "     SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}PLAN_MONEY) AS PLAN_MONEY, ";
                $query .= "     SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}DECISION_MONEY) AS DECISION_MONEY ";
            }
            $query .= " FROM ";
            $query .= "     {$tableInfo["TABLE"]} PLAN_T ";
            $query .= "     INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_DATE ";
            $query .= "          ON PLAN_T.SCHOOLCD     = LIMIT_DATE.SCHOOLCD ";
            $query .= "         AND PLAN_T.SCHOOL_KIND  = LIMIT_DATE.SCHOOL_KIND ";
            $query .= "         AND PLAN_T.YEAR         = LIMIT_DATE.YEAR ";
            $query .= "         AND PLAN_T.SCHREGNO     = LIMIT_DATE.SCHREGNO ";
            $query .= "         AND PLAN_T.SLIP_NO      = LIMIT_DATE.SLIP_NO ";
            $query .= "         AND PLAN_T.PLAN_YEAR    = LIMIT_DATE.PLAN_YEAR ";
            $query .= "         AND PLAN_T.PLAN_MONTH   = LIMIT_DATE.PLAN_MONTH ";
            $query .= " WHERE ";
            $query .= "     PLAN_T.SCHOOLCD         = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND PLAN_T.SCHOOL_KIND  = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $query .= "     AND PLAN_T.YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND PLAN_T.SLIP_NO      = '{$slipNo}' ";
            $query .= "     AND PLAN_T.SCHREGNO     = '{$schregNo}' ";
            if ($tableInfo["REDUCTION_TARGET"]) {
                $query .= "     AND PLAN_T.REDUCTION_TARGET = '{$tableInfo["REDUCTION_TARGET"]}' ";
            }
            $query .= "     AND LIMIT_DATE.PAID_LIMIT_DATE IS NOT NULL ";
            $query .= "     AND LIMIT_DATE.PAID_LIMIT_MONTH IS NOT NULL ";
            $query .= " GROUP BY ";
            $query .= "     PLAN_T.SCHOOLCD, ";
            $query .= "     PLAN_T.SCHOOL_KIND, ";
            $query .= "     PLAN_T.YEAR, ";
            $query .= "     PLAN_T.SCHREGNO, ";
            $query .= "     PLAN_T.SLIP_NO, ";
            $query .= "     LIMIT_DATE.PAID_LIMIT_MONTH ";
            $query .= " HAVING ";
            if ($tableInfo["TABLE"] == "REDUCTION_BURDEN_CHARGE_PLAN_DAT") {
                $query .= "     VALUE(SUM(PLAN_T.BURDEN_CHARGE), 0) > 0 ";
            } else {
                $query .= "     VALUE(SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}PLAN_MONEY), 0) > 0 ";
                $query .= "     OR VALUE(SUM(PLAN_T.{$tableInfo["MONEY_DIV"]}DECISION_MONEY), 0) > 0 ";
            }

            $result = $db->query($query);
            $updSumArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $updSumArray[] = $row;
            }
            $result->free();

            //金額クリア
            $data = array();
            if ($tableInfo["TABLE"] == "REDUCTION_BURDEN_CHARGE_PLAN_DAT") {
                $data["BURDEN_CHARGE"][NUMBER] = 0;
            } else {
                $data[$tableInfo["MONEY_DIV"]."PLAN_MONEY"][NUMBER]     = 0;
                $data[$tableInfo["MONEY_DIV"]."DECISION_MONEY"][NUMBER] = 0;
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     SCHOOLCD            = '".(sprintf("%012d", SCHOOLCD))."' ";
            $where .= "     AND SCHOOL_KIND     = '{$model->schoolKindAry[$model->search["GRADE"]]}' ";
            $where .= "     AND YEAR            = '".CTRL_YEAR ."' ";
            $where .= "     AND SLIP_NO         = '{$slipNo}' ";
            $where .= "     AND SCHREGNO         = '{$schregNo}' ";
            if ($tableInfo["REDUCTION_TARGET"]) {
                $where .= "     AND REDUCTION_TARGET = '{$tableInfo["REDUCTION_TARGET"]}' ";
            }

            $query = Query::updateSQL($data, $tableInfo["TABLE"], $where);
            $db->query($query);

            //更新
            foreach ($updSumArray as $updKey => $updVal) {
                $data = array();
                if ($tableInfo["TABLE"] == "REDUCTION_BURDEN_CHARGE_PLAN_DAT") {
                    $data["BURDEN_CHARGE"][NUMBER] = $updVal["BURDEN_CHARGE"];
                } else {
                    $data[$tableInfo["MONEY_DIV"]."PLAN_MONEY"][NUMBER]     = $updVal["PLAN_MONEY"];
                    $data[$tableInfo["MONEY_DIV"]."DECISION_MONEY"][NUMBER] = $updVal["DECISION_MONEY"];
                }
                $data["REGISTERCD"][TEXT]                              = STAFFCD;
                $data["UPDATED"][FUNC]                                 = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD            = '{$updVal["SCHOOLCD"]}' ";
                $where .= "     AND SCHOOL_KIND     = '{$updVal["SCHOOL_KIND"]}' ";
                $where .= "     AND YEAR            = '{$updVal["YEAR"]}' ";
                $where .= "     AND SLIP_NO         = '{$updVal["SLIP_NO"]}' ";
                $where .= "     AND SCHREGNO        = '{$updVal["SCHREGNO"]}' ";
                if ($tableInfo["REDUCTION_TARGET"]) {
                    $where .= "     AND REDUCTION_TARGET = '{$tableInfo["REDUCTION_TARGET"]}' ";
                }
                $where .= "     AND PLAN_YEAR       = '{$updVal["PLAN_YEAR"]}' ";
                $where .= "     AND PLAN_MONTH      = '{$updVal["PLAN_MONTH"]}' ";

                $query = Query::updateSQL($data, $tableInfo["TABLE"], $where);
                $db->query($query);
            }
        }
    }
}
?>