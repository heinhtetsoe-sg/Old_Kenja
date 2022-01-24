<?php

require_once('for_php7.php');

class knjz404Query extends Query {
    //年度一覧取得
    function getYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_GRADE_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }
    
    //学年一覧取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //設定科目一覧取得
    function getSubclassGradeQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || L1.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_GRADE_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.CLASSCD       = T1.CLASSCD ";
        $query .= "                              AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "                              AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                              AND L1.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " AND T1.RECORD_DIV = '".$model->field["RECORD_DIV"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.SCHOOL_KIND IN (SELECT ";
                $query .= "                                SCHOOL_KIND ";
                $query .= "                            FROM ";
                $query .= "                                SCHREG_REGD_GDAT ";
                $query .= "                            WHERE ";
                $query .= "                                    YEAR  = '".$model->year."' ";
                $query .= "                                AND GRADE = '".$model->field["GRADE"]."' ";
                $query .= "                            ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //科目一覧取得
    function getSubclassQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T1.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR  = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.SCHOOL_KIND IN (SELECT ";
                $query .= "                                SCHOOL_KIND ";
                $query .= "                            FROM ";
                $query .= "                                SCHREG_REGD_GDAT ";
                $query .= "                            WHERE ";
                $query .= "                                    YEAR  = '".$model->year."' ";
                $query .= "                                AND GRADE = '".$model->field["GRADE"]."' ";
                $query .= "                            ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " AND NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_GRADE_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR           = '".$model->year."' ";
        $query .= "                 AND S1.GRADE          = '".$model->field["GRADE"]."' ";
        $query .= "                 AND S1.RECORD_DIV     = '".$model->field["RECORD_DIV"]."' ";
        $query .= "                 AND S1.CLASSCD        = T1.CLASSCD ";
        $query .= "                 AND S1.SCHOOL_KIND    = T1.SCHOOL_KIND ";
        $query .= "                 AND S1.CURRICULUM_CD  = T1.CURRICULUM_CD ";
        $query .= "                 AND S1.SUBCLASSCD     = T1.SUBCLASSCD ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //更新データチェック
    function getCountSubclassGradeQuery($model, $classcd, $school_kind, $curriculum_cd, $subclasscd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " AND RECORD_DIV = '".$model->field["RECORD_DIV"]."' ";
        $query .= " AND CLASSCD = '".$classcd."' ";
        $query .= " AND SCHOOL_KIND = '".$school_kind."' ";
        $query .= " AND CURRICULUM_CD = '".$curriculum_cd."' ";
        $query .= " AND SUBCLASSCD = '".$subclasscd."' ";

        return $query;
    }
        
    //UPDATE
    function &getUpdateQuery(&$model) {
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."'  ";
        $query .= " AND RECORD_DIV = '".$model->field["RECORD_DIV"]."' ";
        $db->query($query);

        //追加
        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                list ($classcd, $school_kind, $curriculum_cd, $subclasscd, $sort) = explode('-', $array[$i]);
                
                //データチェック
                $getCount = $db->getOne(knjz404Query::getCountSubclassGradeQuery($model, $classcd, $school_kind, $curriculum_cd, $subclasscd));
                
                //データ更新
                $data = array();
                $data["TEXT_HYOKA_FLG"][TEXT] = "1";
                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][NUMBER]      = "sysdate()";
                if ($getCount == 0) {
                    $data["YEAR"][TEXT]           = $model->year;
                    $data["GRADE"][TEXT]          = $model->field["GRADE"];
                    $data["RECORD_DIV"][TEXT]     = $model->field["RECORD_DIV"];
                    $data["CLASSCD"][TEXT]        = $classcd;
                    $data["SCHOOL_KIND"][TEXT]    = $school_kind;
                    $data["CURRICULUM_CD"][TEXT]  = $curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]     = $subclasscd;

                    $query = Query::insertSQL($data, "SUBCLASS_GRADE_DAT");
                    $db->query($query);
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR = '".$model->year."' ";
                    $where .= " AND GRADE = '".$model->field["GRADE"]."'  ";
                    $where .= " AND RECORD_DIV = '".$model->field["RECORD_DIV"]."'  ";
                    $where .= " AND CLASSCD = '".$classcd."' ";
                    $where .= " AND SCHOOL_KIND = '".$school_kind."' ";
                    $where .= " AND CURRICULUM_CD = '".$curriculum_cd."' ";
                    $where .= " AND SUBCLASSCD = '".$subclasscd."' ";
                
                    $query = Query::updateSQL($data, "SUBCLASS_GRADE_DAT", $where);
                    $db->query($query);
                }
            }
        } else {
            $model->year = "";
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>
