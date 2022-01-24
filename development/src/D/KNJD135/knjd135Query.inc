<?php

require_once('for_php7.php');

class knjd135query extends Query {
    //学校名
    function getZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

        return $query;
    }

    //学期一覧取得
    function getSemesterList() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
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
        if ($model->schoolName == 'sapporo') {
            $query .= "     INNER JOIN (SELECT DISTINCT YEAR, SEMESTER, GRADE, HR_CLASS FROM SCHREG_REGD_DAT T1 WHERE COURSECODE IN ('1851', '1852', '1853') ";
            $query .= "     ) T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                $query .= "           AND T1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期詳細データより日付取得
    function getSemesDate($semester, $detail="") {
        $query  = " SELECT ";
        if(!$detail){
            $query .= "     MIN(SDATE) AS SDATE, ";
            $query .= "     MAX(EDATE) AS EDATE ";
        } else {
            $query .= "     SDATE, ";
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if($semester != "9"){
            $query .= "     AND SEMESTER = '".$semester."' ";
        }
        if($detail){
            $query .= "     AND SEMESTER_DETAIL = '".$detail."' ";
        }

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        $query  = " WITH COMMUNICATION AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMMUNICATION ";
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '". CTRL_YEAR ."' AND ";
        $query .= "         SEMESTER = '{$model->field["SEMESTER"]}' ";

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
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        if ($model->schoolName == 'sapporo') {
            $query .= "     AND T1.COURSECODE IN ('1851', '1852', '1853') ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.COMMUNICATION AS REMARK";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN COMMUNICATION T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO   = '".$schregno."' ";

            $db->query($query);

            if (!strlen($model->fields["REMARK"][$i])) {
                continue;
            }

            //項目
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]      = $schregno;
            $data["COMMUNICATION"][TEXT] = $model->fields["REMARK"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
