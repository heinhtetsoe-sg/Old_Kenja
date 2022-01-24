<?php

require_once('for_php7.php');

class knjz064Query extends Query {
    //学部取得
    function getSchoolKind($school_kind="") {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($school_kind) {
            $query .= "     AND NAME1 = '".$school_kind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得（左フレーム）
    function getList($model, $school_kind, $condition="") {
        $query  = " SELECT ";
        if ($condition) {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.CONDITION || ':' || L1.NAME1 AS CONDITION_SHOW, ";
            $query .= "     VALUE(L2.CLASS_REMARK1,'&nbsp;') || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS SUBCLASS ";
        }
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_SUBCLASS_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1     = 'A033' ";
        $query .= "                          AND T1.CONDITION   = L1.NAMECD2, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= "     LEFT JOIN CLASS_DETAIL_MST L2 ON L2.CLASSCD     = T2.CLASSCD ";
        $query .= "                                  AND L2.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "                                  AND L2.CLASS_SEQ   = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = T2.YEAR AND ";
        $query .= "     T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "     T1.CLASSCD          = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
        $query .= "     T1.SCHOOL_KIND      = '".$school_kind."' AND ";
        $query .= "     T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        if ($condition) {
            $query .= "     AND T1.CONDITION   = '".$condition."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD ";
        }

        return $query;
    }

    //状態区分取得
    function getCondition() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A033' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //GRADE_KIND_COMP_SUBCLASS_DAT取得
    function getGradeKindCompSubclassDat($model) {
        $query  = " SELECT ";
        $query .= "     VALUE(L1.CLASS_REMARK1,'0') || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     VALUE(L1.CLASS_REMARK1,'&nbsp;') || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_SUBCLASS_DAT T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= "     LEFT JOIN CLASS_DETAIL_MST L1 ON L1.CLASSCD     = T2.CLASSCD ";
        $query .= "                                  AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "                                  AND L1.CLASS_SEQ   = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = T2.YEAR AND ";
        $query .= "     T1.YEAR             = '".CTRL_YEAR."' AND ";
        $query .= "     T1.CONDITION        = '".$model->condition."' AND ";
        $query .= "     T1.CLASSCD          = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
        $query .= "     T1.SCHOOL_KIND      = '".$model->school_kind."' AND ";
        $query .= "     T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目マスタ取得
    function getSubclassMst($model) {
        $query  = " SELECT ";
        $query .= "     VALUE(L1.CLASS_REMARK1,'0') || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     VALUE(L1.CLASS_REMARK1,'&nbsp;') || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN CLASS_DETAIL_MST L1 ON L1.CLASSCD     = T1.CLASSCD ";
        $query .= "                                  AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                  AND L1.CLASS_SEQ   = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->school_kind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新
    function &UpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM  ";
        $query .= "     GRADE_KIND_COMP_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     CONDITION   = '".$model->condition."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->school_kind."' ";

        $db->query($query);

        //追加
        $selectdata = explode(',', $model->selectdata);
        if ($model->selectdata) {
            for ($i=0; $i < get_count($selectdata); $i++) {
                list($class_div, $classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $selectdata[$i]);

                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CONDITION"][TEXT]        = $model->condition;
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $db->query(Query::insertSQL($data, "GRADE_KIND_COMP_SUBCLASS_DAT"));
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //存在チェック（コピー）
    function checkGradeKindCompSubclassDat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //コピー用データ取得
    function copyGradeKindCompSubclassDat() {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_SUBCLASS_DAT T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR          = T2.YEAR AND ";
        $query .= "     T1.YEAR          = '".(CTRL_YEAR-1)."' AND ";
        $query .= "     T1.CLASSCD       = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD    = T2.SUBCLASSCD  ";

        return $query;
    }

    //コピー
    function &copyInsertQuery($Row) {
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["CONDITION"][TEXT]        = $Row["CONDITION"];
        $data["CLASSCD"][TEXT]          = $Row["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]      = $Row["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]    = $Row["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]       = $Row["SUBCLASSCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "GRADE_KIND_COMP_SUBCLASS_DAT");
        return $query;
    }
}
?>
