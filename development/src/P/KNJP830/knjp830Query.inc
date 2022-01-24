<?php

require_once('for_php7.php');

class knjp830Query extends Query {
    /**********/
    /* 左画面 */
    /**********/
    //年度取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト一覧取得
    function selectQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.GRADE || '-' || REGD.HR_CLASS AS GRADE_HR_CLASS, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     MAPP.SGL_SCHOOLKIND || '-' || MAPP.SGL_MAJORCD || '-' || MAPP.SGL_COURSECODE || ':' || SGLM.SGL_COURSECODE_NAME AS SGL_COURSE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                                    AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "                                    AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN COLLECT_SGL_COURSE_MAPPING_DAT MAPP ON REGD.YEAR     = MAPP.YEAR ";
        $query .= "                                                  AND REGD.GRADE    = MAPP.GRADE ";
        $query .= "                                                  AND REGD.HR_CLASS = MAPP.HR_CLASS ";
        $query .= "     LEFT JOIN COLLECT_SGL_COURSECODE_MST SGLM ON MAPP.SGL_SCHOOLKIND = SGLM.SGL_SCHOOLKIND ";
        $query .= "                                              AND MAPP.SGL_MAJORCD    = SGLM.SGL_MAJORCD ";
        $query .= "                                              AND MAPP.SGL_COURSECODE = SGLM.SGL_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR  = '{$model->year}' ";
        $query .= "     AND REGD.GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE_HR_CLASS ";

        return $query;
    }

    /**********/
    /* 右画面 */
    /**********/
    //設置取得
    function getSglSchoolKind() {
        $query  = " SELECT ";
        $query .= "     SGL_SCHOOLKIND AS VALUE, ";
        $query .= "     SGL_SCHOOLKIND || ':' || SGL_SCHOOLKIND_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_SCHOOLKIND_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学部取得
    function getSglMajorCd($sglSchoolKind) {
        $query  = " SELECT ";
        $query .= "     SGL_MAJORCD AS VALUE, ";
        $query .= "     SGL_MAJORCD || ':' || SGL_MAJORCD_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_MAJORCD_MST ";
        $query .= " WHERE ";
        $query .= "     SGL_SCHOOLKIND = '{$sglSchoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学科取得
    function getSglCourseCd($sglSchoolKind, $sglMajorCd) {
        $query  = " SELECT ";
        $query .= "     SGL_COURSECODE AS VALUE, ";
        $query .= "     SGL_COURSECODE || ':' || SGL_COURSECODE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "         SGL_SCHOOLKIND = '{$sglSchoolKind}' ";
        $query .= "     AND SGL_MAJORCD    = '{$sglMajorCd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $gradeHrClass) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_COURSE_MAPPING_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     AND GRADE || '-' || HR_CLASS = '{$gradeHrClass}' ";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    /************/
    /* 更新処理 */
    /************/
    //カウント取得
    function getCntQuery($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_COURSE_MAPPING_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     AND GRADE || '-' || HR_CLASS = '{$model->gradeHrClass}' ";

        return $query;
    }

    //INSERT or UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = $db->getOne(knjp830Query::getCntQuery($model));

        list($grade, $hrClass) = explode('-', $model->gradeHrClass);

        $data["YEAR"][TEXT]             = $model->year;
        $data["GRADE"][TEXT]            = $grade;
        $data["HR_CLASS"][TEXT]         = $hrClass;
        $data["SGL_SCHOOLKIND"][TEXT]   = $fields["SGL_SCHOOLKIND"];
        $data["SGL_MAJORCD"][TEXT]      = $fields["SGL_MAJORCD"];
        $data["SGL_COURSECODE"][TEXT]   = $fields["SGL_COURSECODE"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($cnt > 0) {
            $where  = " WHERE ";
            $where .= "         YEAR     = '{$model->year}' ";
            $where .= "     AND GRADE    = '{$grade}' ";
            $where .= "     AND HR_CLASS = '{$hrClass}' ";

            $query = Query::updateSQL($data, "COLLECT_SGL_COURSE_MAPPING_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "COLLECT_SGL_COURSE_MAPPING_DAT");
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($grade, $hrClass) = explode('-', $model->gradeHrClass);

        $query  = " DELETE FROM COLLECT_SGL_COURSE_MAPPING_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->year}' ";
        $query .= "     AND GRADE    = '{$grade}' ";
        $query .= "     AND HR_CLASS = '{$hrClass}' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    /**************/
    /* コピー処理 */
    /**************/
    //今年度のデータが存在するかチェック
    function CheckData($model, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM COLLECT_SGL_COURSE_MAPPING_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO COLLECT_SGL_COURSE_MAPPING_DAT ";
        $query .= "           ( YEAR, ";
        $query .= "             GRADE, ";
        $query .= "             HR_CLASS, ";
        $query .= "             SGL_SCHOOLKIND, ";
        $query .= "             SGL_MAJORCD, ";
        $query .= "             SGL_COURSECODE, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->year."', ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         SGL_SCHOOLKIND, ";
        $query .= "         SGL_MAJORCD, ";
        $query .= "         SGL_COURSECODE, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SGL_COURSE_MAPPING_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".($model->year - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
