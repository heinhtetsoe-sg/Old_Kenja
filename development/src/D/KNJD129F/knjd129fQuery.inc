<?php

require_once('for_php7.php');

class knjd129fquery extends Query {

    //学期
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";
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
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= "     LEFT JOIN  SCHREG_REGD_GDAT G1 ON G1.YEAR  = T1.YEAR ";
        $query .= "                                   AND G1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."') ";
        }
        if ($model->callPrgid == "KNJD129J") {
            $query .= "     AND G1.SCHOOL_KIND = 'J' ";
        } else {
            $query .= "     AND G1.SCHOOL_KIND = 'H' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校校種を取得
    function getSchoolKind($model) {
        $setGrade = substr($model->field["GRADE_HR_CLASS"], 0, 2);
    
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$setGrade."' ";
        
        return $query;
    }

    //科目(名称マスタD061)取得
    function getSubclass($model)
    {
        $query  = " SELECT ";
        if ($model->schoolkind === 'J') {
            $query .= "     T1.NAME1 AS VALUE, ";
            $query .= "     T1.NAME1 || ' ' || L1.SUBCLASSNAME AS LABEL ";
        } else if ($model->schoolkind === 'H') {
            $query .= "     T1.NAME2 AS VALUE, ";
            $query .= "     T1.NAME2 || ' ' || L1.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     '' AS VALUE, ";
            $query .= "     '' AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        if ($model->schoolkind === 'J' || $model->schoolkind === 'H') {
            $query .= "     LEFT JOIN SUBCLASS_MST L1 ON ";
            if ($model->schoolkind === 'J') {
                $query .= " L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = T1.NAME1 ";
            } else {
                $query .= " L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = T1.NAME2 ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.NAMECD1 = 'D061' ";
        if ($model->schoolkind === 'J' || $model->schoolkind === 'H') {
            if ($model->schoolkind === 'J') {
                $query .= " AND T1.NAME1 IS NOT NULL ";
            } else {
                $query .= " AND T1.NAME2 IS NOT NULL ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";

        return $query;
    }

    //名称マスタの取得
    function getNameMst($namecd1, $value="")
    {
        $query  = " SELECT ";
        if ($value == "") {
            $query .= "     NAMECD2 AS VALUE, ";
        }
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($value) {
            $query .= " AND NAMECD2 = '".$value."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " WITH SPECIALACT_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         VALUE ";
        $query .= "     FROM ";
        $query .= "         SPECIALACT_SCORE_DAT ";
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
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_HDAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.VALUE";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN SPECIALACT_SCORE T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            $schregno = $model->fields["SCHREGNO"][$i];

            $query  = " DELETE FROM ";
            $query .= "     SPECIALACT_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO   = '".$schregno."' AND ";
            $query .= "     CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD    = '".$subclass_array[3]."' ";
            
            $db->query($query);

            //if (!strlen($model->fields["VALUE"][$i])) {
            //    continue;
            //}

            //項目
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]      = $schregno;
            $data["CLASSCD"][TEXT]       = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
            $data["VALUE"][NUMBER]       = $model->fields["VALUE"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "SPECIALACT_SCORE_DAT");

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
