<?php

require_once('for_php7.php');

class knjd132vquery extends Query
{

    //校種取得
    public function getSchoolKind($grade)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //学校名取得
    public function getNameMstZ010($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学期一覧取得
    public function getSemesterList($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->schoolName == 'miyagiken' || $model->schoolName == 'rakunan' || $model->schoolName == 'seijyo' || $model->Properties["knjd132vSemes9NotHyoji"] == "1") {
            $query .= "     AND SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    public function getGradeHrclass($model, $semester)
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
        $query .= "     T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
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
    public function selectQuery($model, $semester)
    {
        $query  = " WITH COMMUNICATION AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMMUNICATION ";
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$model->field["SEMESTER"]."' ";
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
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN COMMUNICATION T2 ON T1.SCHREGNO = T2.SCHREGNO ";
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

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["COMMUNICATION"][TEXT]    = $model->fields["COMMUNICATION"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //存在チェック
            $query  = " SELECT COUNT(*) FROM HREPORTREMARK_DAT";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO = '".$schregno."' ";

            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE ";
                $where .= "     YEAR = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
                $where .= "     SCHREGNO = '".$schregno."' ";
                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            }
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }

    /*===================================定型文選択===================================*/

    //学年取得
    public function getGrade($model, $schregno, $semester)
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "    T2.GRADE AS VALUE ";
        $query .= "    , T2.GRADE_NAME1 AS LABEL ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN ";
        $query .= "    SCHREG_REGD_GDAT T2 ";
        $query .= "    ON ";
        $query .= "    T1.YEAR = T2.YEAR ";
        $query .= "    AND ";
        $query .= "    T1.GRADE = T2.GRADE ";
        $query .= "  WHERE ";
        $query .= "    T1.SCHREGNO = '".$schregno."' ";
        $query .= "    AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$semester."' ";

        return $query;
    }

    //定型文マスタ一覧取得
    public function getHtrainRemarkTempDat($model, $semester, $grade)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '".$semester."' ";
        $query .= "     AND GRADE       = '".$grade."'  ";
        $query .= "     AND DATA_DIV    = '05' ";
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }

    //タイトルの取得
    public function getNameMstTitle()
    {
        $query  = " SELECT ";
        $query .= "     Name1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     namecd1 = 'A042' ";
        $query .= "     AND namecd2 = '05' ";
        $query .= "  ";

        return $query;
    }
}
