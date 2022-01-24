<?php

require_once('for_php7.php');

class knjp906query extends Query {
    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //学期一覧取得
    function getSemesterList($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         GDAT.SCHOOL_KIND, ";
        $query .= "         BASE.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
        $query .= "              AND REGD.GRADE = GDAT.GRADE, ";
        $query .= "         SCHREG_BASE_MST BASE ";
        $query .= "     WHERE ";
        $query .= "             REGD.YEAR                   = '".CTRL_YEAR."' ";
        $query .= "         AND REGD.SEMESTER               = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND REGD.GRADE || REGD.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        $query .= "         AND REGD.SCHREGNO               = BASE.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SCHI.SCHREGNO, ";
        $query .= "     SCHI.ATTENDNO, ";
        $query .= "     SCHI.NAME, ";
        $query .= "     BENEFIT.BENEFIT_MONEY ";
        $query .= " FROM ";
        $query .= "     SCHINFO SCHI ";
        $query .= "     LEFT JOIN LEVY_REQUEST_BENEFIT_SCHREG_DAT BENEFIT ON BENEFIT.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "          AND SCHI.SCHOOL_KIND = BENEFIT.SCHOOL_KIND ";
        $query .= "          AND BENEFIT.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND SCHI.SCHREGNO = BENEFIT.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     SCHI.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM LEVY_REQUEST_BENEFIT_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO IN (SELECT ";
        $query .= "                         SCHREGNO ";
        $query .= "                      FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                      WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' ";
        $query .= "                         AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "                         AND GRADE || HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        $query .= "                      ) ";
        $db->query($query);

        foreach ($model->updFields as $schregno => $setMoney) {

            //更新前データから値が変更されていた場合、給付伝票を削除
            if ($model->preMoney[$schregno] != $setMoney) {
                //給付伝票削除
                //①--削除 LEVY_REQUEST_OUTGO_SCHREG_DAT
                $query  = " DELETE FROM LEVY_REQUEST_OUTGO_SCHREG_DAT ";
                $query .= " WHERE ";
                $query .= "     SCHOOLCD        = '".SCHOOLCD."' ";
                $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
                $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
                $query .= "     AND LINE_NO     = '98' ";
                $query .= "     AND SCHREGNO    = '{$schregno}' ";
                $db->query($query);

                //②--削除 LEVY_REQUEST_OUTGO_MEISAI_DAT
                $query  = " DELETE FROM LEVY_REQUEST_OUTGO_MEISAI_DAT ";
                $query .= " WHERE ";
                $query .= "     SCHOOLCD        = '".SCHOOLCD."' ";
                $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
                $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
                $query .= "     AND LINE_NO     = '98' ";
                $db->query($query);

                //③--削除 LEVY_REQUEST_OUTGO_DAT
                $query  = " DELETE FROM LEVY_REQUEST_OUTGO_DAT ";
                $query .= " WHERE ";
                $query .= "     SCHOOLCD        = '".SCHOOLCD."' ";
                $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
                $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
                $query .= "     AND INCOME_L_CD = '98' ";
                $query .= "     AND INCOME_M_CD = '98' ";
                $db->query($query);
            }

            $setMoney = str_replace(array(" ", "　"), "", $setMoney);
            if (!$setMoney) {
                continue;
            }

            $data = array();
            $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["BENEFIT_MONEY"][NUMBER]  = $setMoney;
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "LEVY_REQUEST_BENEFIT_SCHREG_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
