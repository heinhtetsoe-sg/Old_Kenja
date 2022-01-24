<?php

require_once('for_php7.php');

class knjd139fquery extends Query {
    //名称マスタ取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclassMst($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_GRADE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_FI_HDAT L1 ON L1.YEAR        = T1.YEAR ";
        $query .= "                                      AND L1.SEMESTER    = '".$model->field["SEMESTER2"]."' ";
        $query .= "                                      AND L1.GRADE       = T1.GRADE ";
        $query .= "                                      AND L1.RECORD_DIV  = T1.RECORD_DIV ";
        $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD      = T1.SUBCLASSCD ";
        $query .= "                              AND T2.CLASSCD         = T1.CLASSCD ";
        $query .= "                              AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
        $query .= "                              AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' AND ";
        $query .= "     VALUE(T1.TEXT_HYOKA_FLG, '0') = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座取得
    function getChairList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIRCD AS VALUE, ";
        $query .= "     CHAIRCD || ' ' || VALUE(CHAIRNAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //メインデータの取得
    function selectQuery($model) {
        $subclass_array = array();
        list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_FI_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_FI_HDAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.GRADE    = T3.GRADE AND ";
        $query .= "         T1.GRADE    = '".$model->field["GRADE"]."' AND ";
        $query .= "         T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ), CHAIR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1, ";
        $query .= "         CHAIR_STD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "         T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        $query .= " ), REPORTREMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_REPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR            = '". CTRL_YEAR ."' AND ";
        $query .= "         SEMESTER        = '{$model->field["SEMESTER"]}' AND ";
        $query .= "         CLASSCD         = '".$classcd."' AND ";
        $query .= "         SCHOOL_KIND     = '".$school_kind."' AND ";
        $query .= "         CURRICULUM_CD   = '".$curriculum_cd."' AND ";
        $query .= "         SUBCLASSCD      = '".$subclasscd."' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T3.REMARK1";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     INNER JOIN CHAIR T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN REPORTREMARK T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //更新処理
    function update($model) {
        list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->field["SUBCLASSCD"]);

        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            //削除
            $query  = " DELETE FROM ";
            $query .= "     JVIEWSTAT_REPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER        = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO        = '".$schregno."' AND ";
            $query .= "     CLASSCD         = '".$classcd."' AND ";
            $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
            $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
            $query .= "     SUBCLASSCD      = '".$subclasscd."' ";

            $db->query($query);

            if (!strlen($model->fields["REMARK1"][$i])) {
                continue;
            }

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            $data["REMARK1"][TEXT]          = $model->fields["REMARK1"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "JVIEWSTAT_REPORTREMARK_DAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
