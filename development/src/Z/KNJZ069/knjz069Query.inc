<?php

require_once('for_php7.php');

class knjz069Query extends Query {

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= "     , SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.GRADE_NAME1 AS LABEL ";
        $query .= "     , CH_CLS.TRGTGRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CH_CLS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON GDAT.YEAR = CH_CLS.YEAR ";
        $query .= "         AND GDAT.GRADE = CH_CLS.TRGTGRADE ";
        $query .= " WHERE ";
        $query .= "     CH_CLS.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CH_CLS.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclass($model) {
        $query  = " SELECT ";
        $query .= "     V_SUB.CLASSCD || '-' || V_SUB.SCHOOL_KIND || '-' || V_SUB.CURRICULUM_CD || '-' || V_SUB.SUBCLASSCD || ':' || V_SUB.SUBCLASSNAME AS LABEL ";
        $query .= "     , V_SUB.CLASSCD || '-' || V_SUB.SCHOOL_KIND || '-' || V_SUB.CURRICULUM_CD || '-' || V_SUB.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST V_SUB ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON GDAT.YEAR = V_SUB.YEAR ";
        $query .= "         AND GDAT.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "         AND GDAT.SCHOOL_KIND = V_SUB.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     V_SUB.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //対象一覧取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     DAT.ELEMENT_CD AS VALUE ";
        $query .= "     , DAT.ELEMENT_CD || ' ' || MST.ELEMENT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PYP_ELEMENT_DAT DAT ";
        $query .= "     INNER JOIN PYP_ELEMENT_MST MST ";
        $query .= "         ON MST.ELEMENT_DIV = DAT.ELEMENT_DIV ";
        $query .= "         AND MST.ELEMENT_CD = DAT.ELEMENT_CD ";
        $query .= " WHERE ";
        $query .= "     DAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DAT.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND DAT.ELEMENT_DIV = '".$model->field["ELEMENT_DIV"]."' ";
        $query .= "     AND DAT.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND DAT.CLASSCD || '-' || DAT.SCHOOL_KIND || '-' || DAT.CURRICULUM_CD || '-' || DAT.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //評価の要素一覧取得
    function selectSubclassQuery($elementCdArray, $model)
    {
        $query  = " SELECT ";
        $query .= "     ELEMENT_CD AS VALUE ";
        $query .= "     , ELEMENT_CD || ' ' || ELEMENT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PYP_ELEMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ELEMENT_DIV = '".$model->field["ELEMENT_DIV"]."' ";
        $query .= "     AND ELEMENT_CD NOT IN ('".implode("','", $elementCdArray)."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $all_light = false;
        list($classcd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $model->field["SUBCLASSCD"]);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     PYP_ELEMENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND CLASSCD = '".$classcd."' ";
        $query .= "     AND SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "     AND CURRICULUM_CD = '".$curriculumCd."' ";
        $query .= "     AND SUBCLASSCD = '".$subclassCd."' ";
        $query .= "     AND ELEMENT_DIV = '".$model->field["ELEMENT_DIV"]."' ";

        $db->query($query);

        if ($model->selectdata) {
            $selectdata = explode (",", $model->selectdata);
            sort($selectdata);
            $sort = 0;
            foreach ($selectdata as $elementCd) {
                $sort++;

                $data = array();

                $data["YEAR"][TEXT] = CTRL_YEAR;
                $data["SEMESTER"][TEXT] = $model->field["SEMESTER"];
                $data["GRADE"][TEXT] = $model->field["GRADE"];
                $data["CLASSCD"][TEXT] = $classcd;
                $data["SCHOOL_KIND"][TEXT] = $schoolKind;
                $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
                $data["SUBCLASSCD"][TEXT] = $subclassCd;
                $data["ELEMENT_DIV"][TEXT] = $model->field["ELEMENT_DIV"];
                $data["ELEMENT_CD"][TEXT] = $elementCd;
                $data["SORT"][TEXT] = $sort;
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][NUMBER] = "sysdate()";

                $query = Query::insertSQL($data, "PYP_ELEMENT_DAT");

                $db->query($query);
            }
        }

        $db->commit(); //トランザクションをコミットする。

        Query::dbCheckIn($db);
    }
}
?>
