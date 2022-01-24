<?php

require_once('for_php7.php');

class knjp805aQuery extends Query
{

    //職員
    public function getStaffMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     USER.STAFFCD AS VALUE, ";
        $query .= "     USER.STAFFCD || ' ' || STAF.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     USERGROUP_DAT USER ";
        $query .= "     INNER JOIN V_NAME_MST P005 ON USER.YEAR    = P005.YEAR ";
        $query .= "                               AND P005.NAMECD1 = 'P005' ";
        $query .= "                               AND USER.GROUPCD = P005.NAME1 ";
        $query .= "     LEFT JOIN STAFF_MST STAF ON USER.STAFFCD = STAF.STAFFCD ";
        $query .= " WHERE ";
        $query .= "         USER.YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND USER.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            if ($model->selectSchoolKind) {
                $query .= "     AND USER.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND USER.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND USER.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    public function getGrade($model)
    {
        $query  = " WITH GRADE_ALL (LABEL, VALUE, SORT) AS ( ";
        $query .= "      VALUES('-- 全て --', '99', '00') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "      VALUE, ";
        $query .= "      SORT, ";
        $query .= "      LABEL ";
        $query .= " FROM ";
        $query .= "      GRADE_ALL ";
        $query .= " UNION ALL  ";
        $query .= " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE AS SORT, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

    //年組取得
    public function getGradeHrClass($model)
    {
        $query  = "";
        if (!strlen($model->field["GRADE"]) || $model->field["GRADE"] == '99') {
            $query .= " WITH FRESH (LABEL, VALUE) AS ( ";
            $query .= "      VALUES('新入生', '00-000') ";
            $query .= "      ) ";
            $query .= " SELECT ";
            $query .= "      VALUE, ";
            $query .= "      LABEL ";
            $query .= " FROM ";
            $query .= "      FRESH ";
            $query .= " UNION ALL  ";
        }
        $query .= " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "          ON T1.YEAR         = T2.YEAR ";
        $query .= "         AND T1.GRADE        = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["GRADE"] != '99') {
            $query .= " AND T1.GRADE = '".$model->field["GRADE"]."' ";
        } else {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //文面
    public function getDocumentMst()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     DOCU.DOCUMENTCD AS VALUE, ";
        $query .= "     DOCU.DOCUMENTCD || ' ' || DOCU.TITLE AS LABEL ";
        $query .= " FROM ";
        $query .= "     DOCUMENT_MST DOCU ";
        $query .= "     INNER JOIN V_NAME_MST Z049 ON Z049.YEAR    = '".CTRL_YEAR."' ";
        $query .= "                               AND Z049.NAMECD1 = 'Z049' ";
        $query .= "                               AND Z049.NAMECD2 = DOCU.DOCUMENTCD ";
        $query .= "                               AND Z049.NAMESPARE1 = '2' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getStudentSearch($model)
    {
        $and = "";
        $where = "";

        if ($model->field["GRADE_HR_CLASS"] == "00-000") {
            $where .= "   ".$and."MAIN.ENTERYEAR = '".(CTRL_YEAR + 1)."' ";
            $and = "AND ";
        } else {
            $where .= "   ".$and."VALUE(MAIN.GRD_DIV, '0') NOT IN ('1', '2', '3', '9') ";
            $and = "AND ";
        }
        //学年
        if ($model->field["GRADE"] != '' && $model->field["GRADE"] != '99') {
            $where .= "   ".$and."SCH_R.GRADE = '".$model->field["GRADE"]."' ";
            $and = "AND ";
        }
        //年組
        if ($model->field["GRADE_HR_CLASS"]) {
            $where .= "   ".$and."SCH_R.GRADE || '-' || SCH_R.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
            $and = "AND ";
        }

        return $where;
    }

    //生徒情報取得
    public function getStudentInfoData($model)
    {
        if ($model->field["GRADE_HR_CLASS"] == "00-000") {
            $setYear = (CTRL_YEAR + 1);
        } else {
            $setYear = CTRL_YEAR;
        }

        $tableName = "V_SCHREG_BASE_MST";
        if ($model->field["GRADE_HR_CLASS"] == "00-000") {
            $tableName = "FRESHMAN_DAT";
        }
        if ($model->field["PAID_LIMIT_MONTH"] == "12") {
            $setLmonth = "01";
        } else {
            $setLmonth = sprintf("%02d", ($model->field["PAID_LIMIT_MONTH"] + 1));
        }
        if ($model->field["PAID_LIMIT_MONTH"] < "04" || $model->field["PAID_LIMIT_MONTH"] == "12") {
            $setLimitYear = $setYear + 1;
        } else {
            $setLimitYear = $setYear;
        }
        $setLimitDate = $setLimitYear."-".$setLmonth."-01";

        $where = knjp805aQuery::getStudentSearch($model);
        $and = $where == "" ? "" : "AND ";

        
        $query  = " WITH REGD_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCH_R.YEAR, ";
        $query .= "         SCH_R.SCHREGNO, ";
        $query .= "         SCH_R.GRADE, ";
        $query .= "         SCH_R.HR_CLASS, ";
        $query .= "         SCH_R.ATTENDNO, ";
        $query .= "         SCH_RH.HR_NAME, ";
        $query .= "         MAIN.NAME ";
        $query .= "     FROM ";
        $query .= "         {$tableName} MAIN ";
        $query .= "         LEFT JOIN SCHREG_REGD_DAT SCH_R ON MAIN.SCHREGNO    = SCH_R.SCHREGNO ";
        $query .= "                                        AND SCH_R.YEAR       = '".$setYear."' ";
        $query .= "                                        AND SCH_R.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON SCH_R.YEAR     = SCH_RH.YEAR ";
        $query .= "                                          AND SCH_R.SEMESTER = SCH_RH.SEMESTER ";
        $query .= "                                          AND SCH_R.GRADE    = SCH_RH.GRADE ";
        $query .= "                                          AND SCH_R.HR_CLASS = SCH_RH.HR_CLASS ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = SCH_R.YEAR ";
        $query .= "                                        AND GDAT.GRADE = SCH_R.GRADE ";
        $query .= "     WHERE {$where} ";
        $query .= " ), PRINT_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         PLAN_M.SCHOOLCD, ";
        $query .= "         PLAN_M.SCHOOL_KIND, ";
        $query .= "         PLAN_M.YEAR, ";
        $query .= "         PLAN_M.SLIP_NO, ";
        $query .= "         PLAN_M.SCHREGNO, ";
        $query .= "         SUM(VALUE(PLAN_M.PLAN_MONEY, 0)) - SUM(VALUE(PLAN_M.PAID_MONEY, 0)) AS PLAN_MONEY, ";
        $query .= "         LIMIT_D.PAID_LIMIT_MONTH, ";
        $query .= "         LIMIT_D.PAID_LIMIT_DATE ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "         LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PLAN_M.SCHOOLCD ";
        $query .= "                                        AND SL_D.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ";
        $query .= "                                        AND SL_D.YEAR        = PLAN_M.YEAR ";
        $query .= "                                        AND SL_D.SLIP_NO     = PLAN_M.SLIP_NO ";
        $query .= "         INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_D ON PLAN_M.SCHOOLCD         = LIMIT_D.SCHOOLCD ";
        $query .= "                                                           AND PLAN_M.SCHOOL_KIND      = LIMIT_D.SCHOOL_KIND ";
        $query .= "                                                           AND PLAN_M.YEAR             = LIMIT_D.YEAR ";
        $query .= "                                                           AND PLAN_M.SCHREGNO         = LIMIT_D.SCHREGNO ";
        $query .= "                                                           AND PLAN_M.SLIP_NO          = LIMIT_D.SLIP_NO ";
        $query .= "                                                           AND PLAN_M.PLAN_YEAR        = LIMIT_D.PLAN_YEAR ";
        $query .= "                                                           AND PLAN_M.PLAN_MONTH       = LIMIT_D.PLAN_MONTH ";
        $query .= "                                                           AND LIMIT_D.PAID_LIMIT_DATE < '{$setLimitDate}' ";
        $query .= "         INNER JOIN REGD_DATA REGD ON REGD.YEAR     = PLAN_M.YEAR ";
        $query .= "                                  AND REGD.SCHREGNO = PLAN_M.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             PLAN_M.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND PLAN_M.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         AND PLAN_M.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         AND PLAN_M.YEAR = '{$setYear}' ";
        $query .= "         AND SL_D.CANCEL_DATE IS NULL ";
        $query .= "     GROUP BY ";
        $query .= "         PLAN_M.SCHOOLCD, ";
        $query .= "         PLAN_M.SCHOOL_KIND, ";
        $query .= "         PLAN_M.YEAR, ";
        $query .= "         PLAN_M.SLIP_NO, ";
        $query .= "         PLAN_M.SCHREGNO, ";
        $query .= "         LIMIT_D.PAID_LIMIT_MONTH, ";
        $query .= "         LIMIT_D.PAID_LIMIT_DATE ";
        $query .= "     HAVING ";
        $query .= "         SUM(VALUE(PLAN_M.PLAN_MONEY, 0)) - SUM(VALUE(PLAN_M.PAID_MONEY, 0)) > 0 ";
        //COLLECT_SCHREG_REMINDER_DATデータ
        $query .= " ), REMAIN_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         REMI.YEAR, ";
        $query .= "         REMI.SCHREGNO, ";
        $query .= "         MAX(REMI.REMINDER_COUNT) AS REMINDER_COUNT ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SCHREG_REMINDER_DAT REMI ";
        $query .= "         INNER JOIN REGD_DATA REGD ON REGD.YEAR     = REMI.YEAR ";
        $query .= "                                  AND REGD.SCHREGNO = REMI.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         REMI.YEAR, ";
        $query .= "         REMI.SCHREGNO ";
        $query .= " ), MAIN_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.GRADE || MAIN.HR_CLASS || MAIN.ATTENDNO AS SORT_KEY, ";
        $query .= "     VALUE(REMIN.REMINDER_COUNT, 0) AS REMINDER_COUNT, ";
        $query .= "     PRINT_T.SLIP_NO, ";
        $query .= "     VALUE(PRINT_T.PLAN_MONEY, 0) - ";
        $query .= "     VALUE(CASE WHEN REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0) - ";
        $query .= "     VALUE(CASE WHEN REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0) - ";
        $query .= "     VALUE(CASE WHEN REDUC_D1.OFFSET_FLG = '1' THEN REDUC_D1.DECISION_MONEY ELSE 0 END, 0) - ";
        $query .= "     VALUE(CASE WHEN REDUC_D2.OFFSET_FLG = '1' THEN REDUC_D2.DECISION_MONEY ELSE 0 END, 0) - ";
        $query .= "     VALUE(BURDEN_1.BURDEN_CHARGE, 0) - ";
        $query .= "     VALUE(BURDEN_2.BURDEN_CHARGE, 0) - ";
        $query .= "     VALUE(SCHOOL_1.PLAN_MONEY, 0) - ";
        $query .= "     VALUE(SCHOOL_2.PLAN_MONEY, 0) AS DISP_PLAN_MONEY, ";
        $query .= "     PRINT_T.PAID_LIMIT_DATE, ";
        $query .= "     MAIN.HR_NAME || ' ' || MAIN.ATTENDNO || '番' AS HR_ATTEND_NAME, ";
        $query .= "     MAIN.NAME ";
        $query .= " FROM ";
        $query .= "     REGD_DATA MAIN ";
        $query .= "     INNER JOIN PRINT_T ON MAIN.YEAR     = PRINT_T.YEAR ";
        $query .= "                       AND MAIN.SCHREGNO = PRINT_T.SCHREGNO ";
        $query .= "     LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_C ON PRINT_T.SCHOOLCD         = REDUC_C.SCHOOLCD ";
        $query .= "                                                 AND PRINT_T.SCHOOL_KIND      = REDUC_C.SCHOOL_KIND ";
        $query .= "                                                 AND PRINT_T.YEAR             = REDUC_C.YEAR ";
        $query .= "                                                 AND PRINT_T.SLIP_NO          = REDUC_C.SLIP_NO ";
        $query .= "                                                 AND PRINT_T.PAID_LIMIT_MONTH = REDUC_C.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D1 ON PRINT_T.SCHOOLCD          = REDUC_D1.SCHOOLCD ";
        $query .= "                                          AND PRINT_T.SCHOOL_KIND       = REDUC_D1.SCHOOL_KIND ";
        $query .= "                                          AND PRINT_T.YEAR              = REDUC_D1.YEAR ";
        $query .= "                                          AND REDUC_D1.REDUCTION_TARGET = '1' ";
        $query .= "                                          AND PRINT_T.SLIP_NO           = REDUC_D1.SLIP_NO ";
        $query .= "                                          AND PRINT_T.PAID_LIMIT_MONTH  = REDUC_D1.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D2 ON PRINT_T.SCHOOLCD          = REDUC_D2.SCHOOLCD ";
        $query .= "                                          AND PRINT_T.SCHOOL_KIND       = REDUC_D2.SCHOOL_KIND ";
        $query .= "                                          AND PRINT_T.YEAR              = REDUC_D2.YEAR ";
        $query .= "                                          AND REDUC_D2.REDUCTION_TARGET = '2' ";
        $query .= "                                          AND PRINT_T.SLIP_NO           = REDUC_D2.SLIP_NO ";
        $query .= "                                          AND PRINT_T.PAID_LIMIT_MONTH  = REDUC_D2.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_1 ON PRINT_T.SCHOOLCD        = BURDEN_1.SCHOOLCD ";
        $query .= "                                                      AND PRINT_T.SCHOOL_KIND       = BURDEN_1.SCHOOL_KIND ";
        $query .= "                                                      AND PRINT_T.YEAR              = BURDEN_1.YEAR ";
        $query .= "                                                      AND BURDEN_1.REDUCTION_TARGET = '1' ";
        $query .= "                                                      AND PRINT_T.SLIP_NO           = BURDEN_1.SLIP_NO ";
        $query .= "                                                      AND PRINT_T.PAID_LIMIT_MONTH  = BURDEN_1.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_2 ON PRINT_T.SCHOOLCD        = BURDEN_2.SCHOOLCD ";
        $query .= "                                                      AND PRINT_T.SCHOOL_KIND       = BURDEN_2.SCHOOL_KIND ";
        $query .= "                                                      AND PRINT_T.YEAR              = BURDEN_2.YEAR ";
        $query .= "                                                      AND BURDEN_2.REDUCTION_TARGET = '2' ";
        $query .= "                                                      AND PRINT_T.SLIP_NO           = BURDEN_2.SLIP_NO ";
        $query .= "                                                      AND PRINT_T.PAID_LIMIT_MONTH  = BURDEN_2.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_1 ON PRINT_T.SCHOOLCD          = SCHOOL_1.SCHOOLCD ";
        $query .= "                                                 AND PRINT_T.SCHOOL_KIND       = SCHOOL_1.SCHOOL_KIND ";
        $query .= "                                                 AND PRINT_T.YEAR              = SCHOOL_1.YEAR ";
        $query .= "                                                 AND SCHOOL_1.REDUCTION_TARGET = '1' ";
        $query .= "                                                 AND PRINT_T.SLIP_NO           = SCHOOL_1.SLIP_NO ";
        $query .= "                                                 AND PRINT_T.PAID_LIMIT_MONTH  = SCHOOL_1.PLAN_MONTH ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_2 ON PRINT_T.SCHOOLCD          = SCHOOL_2.SCHOOLCD ";
        $query .= "                                                 AND PRINT_T.SCHOOL_KIND       = SCHOOL_2.SCHOOL_KIND ";
        $query .= "                                                 AND PRINT_T.YEAR              = SCHOOL_2.YEAR ";
        $query .= "                                                 AND SCHOOL_2.REDUCTION_TARGET = '2' ";
        $query .= "                                                 AND PRINT_T.SLIP_NO           = SCHOOL_2.SLIP_NO ";
        $query .= "                                                 AND PRINT_T.PAID_LIMIT_MONTH  = SCHOOL_2.PLAN_MONTH ";
        $query .= "     LEFT JOIN REMAIN_DATA REMIN ON PRINT_T.YEAR     = REMIN.YEAR ";
        $query .= "                                AND PRINT_T.SCHREGNO = REMIN.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SORT_KEY, ";
        $query .= "     REMINDER_COUNT, ";
        $query .= "     SUM(DISP_PLAN_MONEY) AS REMINDER_MONEY, ";
        $query .= "     HR_ATTEND_NAME, ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     DISP_PLAN_MONEY > 0 ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     SORT_KEY, ";
        $query .= "     REMINDER_COUNT, ";
        $query .= "     HR_ATTEND_NAME, ";
        $query .= "     NAME ";
        $query .= " ORDER BY ";
        $query .= "   SORT_KEY ";

        return $query;
    }

    /**
     * 更新処理
     */
    //maxSEQ取得(COLLECT_SCHREG_REMINDER_DAT)
    public function getMaxSeq($setYear, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     max(int(SEQ)) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHREG_REMINDER_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$setYear}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    /**
     * 更新
     */
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["GRADE_HR_CLASS"] == "00-000") {
            $setYear = (CTRL_YEAR + 1);
        } else {
            $setYear = CTRL_YEAR;
        }

        //入金作成
        foreach ($model->arr_schregData as $schregno) {
            if ($model->field["GO_PRINT:".$schregno]) {
                $countUpFlg = ($model->field["NO_COUNT_UP-".$schregno] != "1") ? true : false;

                $maxSeq    = $db->getOne(knjp805aQuery::getMaxSeq($setYear, $schregno));
                $nextSeq   = $maxSeq ?: 1;
                $nextCount = $model->field["REMINDER_COUNT-".$schregno];

                if ($countUpFlg) {
                    $nextSeq++;
                    $nextCount++;
                }

                //DELETE INSERT --COLLECT_SCHREG_REMINDER_DAT
                knjp805aQuery::deleteSchReminderQuery($db, $setYear, $schregno, $nextSeq);
                knjp805aQuery::insertSchReminderQuery($db, $model, $setYear, $schregno, $nextSeq, $nextCount);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    public function deleteSchReminderQuery($db, $setYear, $schregno, $seq)
    {
        $query  = "   DELETE FROM ";
        $query .= "       COLLECT_SCHREG_REMINDER_DAT ";
        $query .= "   WHERE ";
        $query .= "       YEAR         = '{$setYear}' ";
        $query .= "       AND SCHREGNO = '{$schregno}' ";
        $query .= "       AND SEQ      = '{$seq}' ";

        $db->query($query);
    }

    public function insertSchReminderQuery($db, $model, $setYear, $schregno, $seq, $count)
    {
        $data = array();
        $data["YEAR"][TEXT]                 = $setYear;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["SEQ"][TEXT]                  = $seq;
        $data["REMINDER_COUNT"][NUMBER]     = $count;
        $data["DOCUMENTCD"][TEXT]           = $model->field["DOCUMENTCD-".$schregno];
        $data["REMINDER_MONEY"][NUMBER]     = $model->field["REMINDER_MONEY-".$schregno];
        $data["REMINDER_STAFFCD"][TEXT]     = $model->field["REMINDER_STAFFCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_SCHREG_REMINDER_DAT");
        $db->query($query);
    }

    /**
     * テスト更新
     */
    public function &getUpdateTestQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["GRADE_HR_CLASS"] == "00-000") {
            $setYear = (CTRL_YEAR + 1);
        } else {
            $setYear = CTRL_YEAR;
        }

        //入金作成
        foreach ($model->arr_schregData as $schregno) {
            if ($model->field["GO_PRINT:".$schregno]) {
                $query  = " DELETE FROM COLLECT_SCHREG_REMINDER_TESTPRINT_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR        = '{$setYear}' ";
                $query .= "     AND SCHREGNO    = '{$schregno}' ";
                $db->query($query);

                $data = array();
                $data["YEAR"][TEXT]             = $setYear;
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["REMINDER_COUNT"][NUMBER] = $model->field["REMINDER_COUNT-".$schregno] + 1;
                $data["DOCUMENTCD"][TEXT]       = $model->field["DOCUMENTCD-".$schregno];
                $data["REMINDER_MONEY"][NUMBER] = $model->field["REMINDER_MONEY-".$schregno];
                $data["REMINDER_STAFFCD"][TEXT] = $model->field["REMINDER_STAFFCD"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_SCHREG_REMINDER_TESTPRINT_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}