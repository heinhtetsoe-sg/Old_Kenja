<?php

require_once('for_php7.php');

class knje012qQuery extends Query {
    //学年判定(３学年)
    function getGradeCd($model) {
        $query  = " SELECT ";
        $query .= "     L1.GRADE_CD, ";
        $query .= "     L1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = T1.YEAR ";
        $query .= "                                  AND L1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$model->exp_year}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.SEMESTER = '{$model->exp_semester}' ";

        return $query;
    }

    function selectQuery($model) {
        $query  =" SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_HDAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //フォーム2の値を取得
    function selectQueryForm2($model, $year) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     ATTENDREC_REMARK ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_DAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_DAT ";
        }
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$year}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //対象学年取得
    function getGdat($model) {
        if ($model->mode == "grd") {
            $year = $model->grd_year;
        } else {
            $year = CTRL_YEAR;
        }

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";

        return $query;
    }

    //学年（年次）コンボボックス用
    function selectQueryAnnual($model) {
        if ($model->mode == "grd") {     //卒業
            $year      = $model->grd_year;
            $semester  = $model->grd_semester;
            $tableName = "V_GRD_REGDYEAR_GRADE_DAT";
        } else {
            $year      = CTRL_YEAR;
            $semester  = CTRL_SEMESTER;
            $tableName = "V_REGDYEAR_GRADE_DAT";
        }
        $query  = " WITH GRADE_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.SCHOOL_KIND ";
        $query .= "         , T1.GRADE_NAME1 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.ANNUAL ";
        $query .= "     , T2.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     {$tableName} T1, ";
        $query .= "     GRADE_T T2 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO  = '{$model->schregno}' ";
        $query .= "     AND T1.YEAR      = T2.YEAR ";
        $query .= "     AND T1.GRADE     = T2.GRADE ";

        return $query;
    }

    /* 更新処理 */
    function update(&$db, $model) {
        //レコードを一旦削除する
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_HDAT";
        } else {
            $tableName = "HEXAM_ENTREMARK_HDAT";
        }

        $query  = " DELETE FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $db->query($query);

        $data = array();
        $data["SCHREGNO"][TEXT]       = $model->schregno;
        $data["REMARK"][TEXT]         = isset($model->no_comments) ? $model->no_comments_label : $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "sysdate()";

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
    }

    /* 更新処理 */
    function update2(&$db, $model, $val) {
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_DAT";
        } else {
            $tableName = "HEXAM_ENTREMARK_DAT";
        }

        //レコードを一旦削除する
        $query  = " DELETE FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "       YEAR      = '{$val["YEAR"]}' ";
        $query .= "   AND SCHREGNO  = '{$model->schregno}' ";

        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]             = $val["YEAR"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["ANNUAL"][TEXT]           = $val["ANNUAL"];
        $data["ATTENDREC_REMARK"][TEXT] = $model->field2[$val["YEAR"]]["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
    }
/*********************************まとめ出欠備考取込ボタン******************************************/
    //セット日々出欠備考
    function getSemesRemark($model, $db, $year) {
        $query  = "  SELECT ";
        $query .= "      REMARK1, ";
        $query .= "      CASE ";
        $query .= "          WHEN MONTH = '01' THEN '13' ";
        $query .= "          WHEN MONTH = '02' THEN '14' ";
        $query .= "          WHEN MONTH = '03' THEN '15' ";
        $query .= "          ELSE MONTH ";
        $query .= "      END AS SORT_MONTH  ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "          COPYCD   = '0' ";
        $query .= "      AND YEAR     = '".$year."' ";
        $query .= "      AND SCHREGNO = '{$model->schregno}' ";
        $query .= "  ORDER BY ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      SORT_MONTH ";

        $result = $db->query($query);
        $set_remark = "";
        $count = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($count == 0) {
                $set_remark .= $row["REMARK1"];
            } else {
                if ($row["REMARK1"] != "") {
                    $set_remark .= "／".$row["REMARK1"];
                }
            }
            $count++;
        }

        return $set_remark;
    }
/*********************************出欠の記録参照ボタン******************************************/
    //学校マスタの校種有無チェック
    function checkSchoolMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //校種取得
    function getSchoolKind2($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".VARS::get("YEAR")."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.SEMESTER IN (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = S1.YEAR AND ";
        $query .= "                         T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                     ) ";

        return $query;
    }

    //出欠の記録参照
    function getAttendSemesDat($model, $knjSchoolMst) {
        $query  = " WITH ATTEND_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(OFFDAYS,0))  AS SICK, ";
        } else {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) - VALUE(OFFDAYS,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0))  AS SICK, ";
        }
        $query .= "         SUM(VALUE(SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "             + VALUE(VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "             + VALUE(KOUDOME,0) ";
        }
        $query .= "             )  AS SUSPEND, ";
        $query .= "         SUM(VALUE(MOURNING,0)) AS MOURNING, ";
        $query .= "         SUM(VALUE(LATE,0)) AS LATE, ";
        $query .= "         SUM(VALUE(EARLY,0)) AS EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".VARS::get("YEAR")."' AND ";
        $query .= "         SCHREGNO    = '".$model->schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         LESSON, ";
        $query .= "         SUSPEND, ";
        $query .= "         MOURNING, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) AS ATTEND1, ";
        $query .= "         SICK, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) - VALUE(SICK,0) AS ATTEND2, ";
        $query .= "         LATE, ";
        $query .= "         EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEM ";
        $query .= " ) ";

        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SEMESTERNAME, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.LESSON, ";
        $query .= "         T2.SUSPEND, ";
        $query .= "         T2.MOURNING, ";
        $query .= "         T2.ATTEND1, ";
        $query .= "         T2.SICK, ";
        $query .= "         T2.ATTEND2, ";
        $query .= "         T2.LATE, ";
        $query .= "         T2.EARLY ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         LEFT JOIN MAIN T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".VARS::get("YEAR")."' AND ";
        $query .= "         T1.SEMESTER <> '9' ";
        $query .= "     ORDER BY ";
        $query .= "         T1.SEMESTER ";

        return $query;
    }
}
?>
