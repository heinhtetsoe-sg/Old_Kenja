<?php

require_once('for_php7.php');


class knjd155aquery extends Query
{

    //学期一覧取得
    public function getSemesterList()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト一覧取得
    public function getTestList($model, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ' ' || TESTITEMNAME AS LABEL, ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($flg) {
            $query .= "    AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
        }
        $query .= "    AND SCORE_DIV = '08' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    public function getGradeHrclass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
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

    //学期詳細データより日付取得
    public function getSemesDate($model, $detail = "")
    {
        $query  = " SELECT ";
        if (!$detail) {
            $query .= "     MIN(SDATE) AS SDATE, ";
            $query .= "     MAX(EDATE) AS EDATE ";
        } else {
            $query .= "     SDATE, ";
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($detail) {
            $query .= "     AND SEMESTER_DETAIL = '".$detail."' ";
        }

        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model)
    {
        $query  = " WITH REMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 ";
        $query .= "     FROM ";
        $query .= "         HEXAM_RECORD_REMARK_SDIV_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTKINDCD"]."' AND ";
        $query .= "         REMARK_DIV = '4' ";
        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == "9") {
            $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        } else {
            $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        }
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.REMARK1 AS REMARK";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN REMARK T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            $query  = " DELETE FROM ";
            $query .= "     HEXAM_RECORD_REMARK_SDIV_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTKINDCD"]."' AND ";
            $query .= "     SCHREGNO   = '".$schregno."' AND ";
            $query .= "     REMARK_DIV = '4' ";

            $db->query($query);

            if (!strlen($model->fields["REMARK"][$i])) {
                continue;
            }

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["TESTKINDCD"][TEXT]       = substr($model->field["TESTKINDCD"], 0, 2);
            $data["TESTITEMCD"][TEXT]       = substr($model->field["TESTKINDCD"], 2, 2);
            $data["SCORE_DIV"][TEXT]        = substr($model->field["TESTKINDCD"], 4, 2);
            $data["REMARK_DIV"][TEXT]       = "4";
            $data["REMARK1"][TEXT]          = $model->fields["REMARK"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HEXAM_RECORD_REMARK_SDIV_DAT");

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }

    /*===================================一括更新画面===================================*/

    //年組名取得
    public function getHrName($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     GRADE || HR_CLASS = '".$model->grade_hr_class."' ";

        return $query;
    }

    //テスト名取得
    public function getTestName($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->testkindcd."' ";

        return $query;
    }

    //置換画面用の生徒一覧取得
    public function getStudent($model, $flg = "")
    {
        $selectdata = str_replace(",", "','", $model->replace_data["selectdata"]);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        if ($model->semester == "9") {
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        } else {
            $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        }
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->grade_hr_class."' AND ";
        if ($flg == "1") {
            $query .= "     T1.SCHREGNO IN ('".$selectdata."') ";
        } else {
            $query .= "     T1.SCHREGNO NOT IN ('".$selectdata."') ";
        }
        $query .= " ORDER BY";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //一括更新
    public function ReplaceUpdate($model)
    {
        $db = Query::dbCheckOut();
        $model->replace_data["selectdata"] = explode(",", $model->replace_data["selectdata"]);

        for ($i = 0; $i < get_count($model->replace_data["selectdata"]); $i++) {
            $schregno = $model->replace_data["selectdata"][$i];

            $query  = " DELETE FROM ";
            $query .= "     HEXAM_RECORD_REMARK_SDIV_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER   = '".$model->semester."' AND ";
            $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->testkindcd."' AND ";
            $query .= "     SCHREGNO   = '".$schregno."' AND ";
            $query .= "     REMARK_DIV = '4' ";

            $db->query($query);

            if (!strlen($model->replace_data["field"]["REMARK"])) {
                continue;
            }

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->semester;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["TESTKINDCD"][TEXT]       = substr($model->testkindcd, 0, 2);
            $data["TESTITEMCD"][TEXT]       = substr($model->testkindcd, 2, 2);
            $data["SCORE_DIV"][TEXT]        = substr($model->testkindcd, 4);
            $data["REMARK_DIV"][TEXT]       = "4";
            $data["REMARK1"][TEXT]          = $model->replace_data["field"]["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HEXAM_RECORD_REMARK_SDIV_DAT");

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
