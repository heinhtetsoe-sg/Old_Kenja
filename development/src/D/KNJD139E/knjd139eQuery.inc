<?php

require_once('for_php7.php');

class knjd139equery extends Query {

    //名称マスタの取得
    function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1";
        $query .= "     INNER JOIN SUBCLASS_GRADE_DAT L1 ON L1.YEAR  = T1.YEAR ";
        $query .= "                                     AND L1.GRADE = T1.GRADE ";
        $query .= "                                     AND L1.RECORD_DIV = T1.RECORD_DIV ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER2"]."' ";
        $query .= "     AND VALUE(L1.TEXT_HYOKA_FLG, '0') = '1' ";
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

    //クラス形態取得
    function getClassKeitai($model)
    {
        $query  = " SELECT ";
        $query .= "     HDAT.RECORD_DIV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT AS HDAT ";
        $query .= " WHERE ";
        $query .= "     HDAT.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND HDAT.SEMESTER  = '".$model->field["SEMESTER2"]."' ";
        $query .= " AND HDAT.GRADE || HDAT.HR_CLASS  = '".$model->field["GRADE_HR_CLASS"]."' ";

        return $query;
    }

    //科目取得
    function getSubclassMst($grade_hr_class, $model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_GRADE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_FI_HDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                      AND L1.SEMESTER = '".$model->field["SEMESTER2"]."' ";
        $query .= "                                      AND L1.GRADE = T1.GRADE ";
        $query .= "                                      AND L1.RECORD_DIV = T1.RECORD_DIV ";
        $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD";
        $query .= "                              AND T2.CLASSCD = T1.CLASSCD ";
        $query .= "                              AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                              AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND L1.GRADE || L1.HR_CLASS = '".$grade_hr_class."' ";
        $query .= " AND VALUE(T1.TEXT_HYOKA_FLG, '0') = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //観点取得
    function selectViewcdQuery($model, $schregno){
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $class_array = array();
        $class_array = explode("-", $model->field["CLASSCD"]);
        $query  = " SELECT DISTINCT";
        $query .= "     R1.VIEWCD, ";
        $query .= "     R1.STATUS, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     T2.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DAT R1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_YDAT T1 ON T1.YEAR        = R1.YEAR";
        $query .= "                                      AND T1.SUBCLASSCD  = R1.SUBCLASSCD ";
        $query .= "                                  AND  T1.CLASSCD = R1.CLASSCD ";
        $query .= "                                  AND  T1.SCHOOL_KIND = R1.SCHOOL_KIND ";
        $query .= "                                  AND  T1.CURRICULUM_CD = R1.CURRICULUM_CD ";
        $query .= "                                      AND T1.VIEWCD      = R1.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2  ON T2.SUBCLASSCD  = R1.SUBCLASSCD ";
        $query .= "                                 AND  T1.CLASSCD = R1.CLASSCD ";
        $query .= "                                 AND  T1.SCHOOL_KIND = R1.SCHOOL_KIND ";
        $query .= "                                 AND  T1.CURRICULUM_CD = R1.CURRICULUM_CD ";
        $query .= "                                     AND T2.VIEWCD      = R1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    R1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    R1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "    R1.SCHREGNO = '".$schregno."' AND ";
        $query .= "    T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"],0,2)."' AND ";
        $query .= "    T1.GRADE = T2.GRADE AND ";
        $query .= "    R1.CLASSCD       = '".$subclass_array[0]."' AND ";
        $query .= "    R1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
        $query .= "    R1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
        $query .= "    R1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        $query .= "  T1.CLASSCD = T2.CLASSCD AND ";
        $query .= "  T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
        $query .= "  T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "    T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        $query .= "    SUBSTR(R1.VIEWCD,1,2) = '".$subclass_array[0]."' ";
        $query .= " ORDER BY ";
        $query .= "    T2.SHOWORDER, ";
        $query .= "    R1.VIEWCD ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH REPORTREMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_REPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '". CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER    = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
        $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
        $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
        $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";

        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_FI_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_FI_HDAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER2"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.REMARK1";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN REPORTREMARK T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            $query  = " DELETE FROM ";
            $query .= "     JVIEWSTAT_REPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO   = '".$schregno."' AND ";
            $query .= "     CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD    = '".$subclass_array[3]."' ";
            
            $db->query($query);

            if (!strlen($model->fields["REMARK1"][$i])) {
                continue;
            }

            //項目
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]      = $schregno;
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
            $data["REMARK1"][TEXT]       = $model->fields["REMARK1"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "JVIEWSTAT_REPORTREMARK_DAT");

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
