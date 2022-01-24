<?php

require_once('for_php7.php');

class knjd428Query extends Query {

    //グループ、履修情報取得
    function getViewGradeKindSchreg($model, $flg="") {
        $query  = " SELECT ";
        if ($flg == 'set') {
            $query .= " DISTINCT ";
            $query .= "     T1.GAKUBU_SCHOOL_KIND, ";
            $query .= "     T1.GHR_CD, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD ";
        } else {
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER = '9' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        if ($flg != 'set') {
            $query .= " ORDER BY ";
            $query .= "     T1.GROUPCD,  ";
            $query .= "     T1.CLASSCD,  ";
            $query .= "     T1.SCHOOL_KIND,  ";
            $query .= "     T1.CURRICULUM_CD,  ";
            $query .= "     T1.SUBCLASSCD,  ";
            $query .= "     T1.UNITCD  ";
        }

        return $query;
    }

    //グループコード取得
    function getGroupcd($model, $row) {
        $query  = " SELECT ";
        $query .= "     GROUPNAME ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND SEMESTER    = '9' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$row["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND GHR_CD      = '".$row["GHR_CD"]."' ";
        $query .= " AND GRADE       = '".$row["GRADE"]."' ";
        $query .= " AND HR_CLASS    = '".$row["HR_CLASS"]."' ";
        $query .= " AND CONDITION   = '".$row["CONDITION"]."' ";
        $query .= " AND GROUPCD     = '".$row["GROUPCD"]."' ";

        return $query;
    }

    //状態区分名称取得
    function getConditionName($model, $condition) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND NAMECD1     = 'A033' ";
        $query .= " AND NAMECD2     = '".$condition."' ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // GRADE_KIND_SCHREG_GROUP_DAT 取得
    function getGradeKindSchregGroupDat($model) {
        if ($model->Properties["useGradeKindCompGroupSemester"] == '1') {
            $semester = $model->semester;
        } else {
            $semester = "9";
        }

        $query  = " SELECT ";
        $query .= "     GAKUBU_SCHOOL_KIND, ";
        $query .= "     GHR_CD, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     CONDITION, ";
        $query .= "     GROUPCD ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //帳票パターン取得
    function getPrintPattern($model, $row) {
        if ($model->Properties["useGradeKindCompGroupSemester"] == '1') {
            $semester = $model->semester;
        } else {
            $semester = "9";
        }

        $query  = " SELECT ";
        $query .= "     A035.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST PATTERN ";
        $query .= "     LEFT JOIN NAME_MST A035 ON A035.NAMECD1 = 'A035' ";
        $query .= "          AND PATTERN.GUIDANCE_PATTERN = A035.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "         PATTERN.YEAR               = '{$model->exp_year}' ";
        $query .= "     AND PATTERN.SEMESTER           = '{$semester}' ";
        $query .= "     AND PATTERN.GAKUBU_SCHOOL_KIND = '{$row["GAKUBU_SCHOOL_KIND"]}' ";
        $query .= "     AND PATTERN.GHR_CD             = '{$row["GHR_CD"]}' ";
        $query .= "     AND PATTERN.GRADE              = '{$row["GRADE"]}' ";
        $query .= "     AND PATTERN.HR_CLASS           = '{$row["HR_CLASS"]}' ";
        $query .= "     AND PATTERN.CONDITION          = '{$row["CONDITION"]}' ";
        $query .= "     AND PATTERN.GROUPCD            = '{$row["GROUPCD"]}' ";

        return $query;
    }

    //所見取得(通年タイプ)
    function getHreportremarkDetailDat($model) {
        $query  = " SELECT ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->semester9}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND DIV      = '{$model->detailDiv}' ";
        $query .= "     AND CODE     = '{$model->detailCode}' ";

        return $query;
    }

    //所見取得
    function getHreportremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     COMMUNICATION, ";
        $query .= "     ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //校種 取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "       ON GDAT.YEAR     = REGD.YEAR ";
        $query .= "      AND GDAT.GRADE    = REGD.GRADE ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "       ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "      AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "      AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "      AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR     = '{$model->exp_year}' ";
        $query .= " AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= " AND REGD.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND HDAT.GRADE    = '{$model->grade}' ";
        $query .= " AND HDAT.HR_CLASS = '{$model->hrClass}' ";

        return $query;
    }

    //HREPORT_CONDITION_DAT 取得
    function getReportCondition($model,$schoolKind,$seq) {
        $query  = " SELECT ";
        $query .= "   T1.REMARK1 ";
        $query .= " FROM ";
        $query .= "   HREPORT_CONDITION_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "   AND T1.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "   AND T1.GRADE = '00' ";
        $query .= "   AND T1.COURSECD = '0' ";
        $query .= "   AND T1.MAJORCD = '000' ";
        $query .= "   AND T1.COURSECODE = '0000' ";
        $query .= "   AND T1.SEQ = '".$seq."' ";

        return $query;
    }

    //INSERT -- HREPORTREMARK_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $checkD = $db->getCol(knjd428Query::getHreportremarkDetailDat($model));
        $checkH = $db->getCol(knjd428Query::getHreportremarkDat($model));

        // HREPORTREMARK_DETAIL_DAT
        $data = array();
        $data["REMARK1"][TEXT]          = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]          = $model->field["REMARK2"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if (get_count($checkD) == 0) {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->semester9;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = $model->detailDiv;
            $data["CODE"][TEXT]         = $model->detailCode;

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "         YEAR     = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '{$model->semester9}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";
            $where .= "     AND DIV      = '{$model->detailDiv}' ";
            $where .= "     AND CODE     = '{$model->detailCode}' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        // HREPORTREMARK_DAT
        $data = array();
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if (get_count($checkH) == 0) {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["SCHREGNO"][TEXT]     = $model->schregno;

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "         YEAR     = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '{$model->semester}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
