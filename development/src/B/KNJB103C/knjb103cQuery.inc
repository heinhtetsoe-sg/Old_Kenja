<?php

require_once('for_php7.php');

class knjb103cQuery extends Query
{
    //学年取得
    public function getSchregRegdGdat($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode("','", explode(':', $model->selectSchoolKind))."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    public function getSemester($model, $semester = "")
    {
        $query  = "  SELECT ";
        $query .= "      SEMESTER AS VALUE, ";
        $query .= "      SEMESTERNAME AS LABEL, ";
        $query .= "      SEMESTERNAME ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER <> '9' ";
        if ($semester) {
            $query .= "      AND SEMESTER = '".$model->semester."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //考査取得
    public function getTest($model, $testcd = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR          = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER  = '".$model->semester."' ";
        $query .= "     AND SCORE_DIV = '01' ";
        if ($testcd) {
            $query .= "      AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$testcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科取得
    public function getClass($model)
    {
        $query  = " WITH CLASSCD_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CLASSCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN SCH_CHR_TEST T2 ";
        $query .= "              ON T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "              ON T3.YEAR        = T1.YEAR ";
        $query .= "             AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR            = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER    = '".$model->semester."' ";
        $query .= "         AND T3.GRADE       = '".$model->grade."' ";
        $query .= "         AND T2.TESTKINDCD  = '".SUBSTR($model->testcd, 0, 2)."' ";
        $query .= "         AND T2.TESTITEMCD  = '".SUBSTR($model->testcd, 2, 2)."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CD.CLASSCD AS VALUE, ";
        $query .= "     MST.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASSCD_DAT CD ";
        $query .= "     LEFT JOIN V_CLASS_MST MST ";
        $query .= "          ON MST.YEAR        = CD.YEAR ";
        $query .= "         AND MST.CLASSCD     = CD.CLASSCD ";
        $query .= "         AND MST.SCHOOL_KIND = CD.SCHOOL_KIND ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    public function getSubclass($model, $subclasscd = "")
    {
        $query  = " WITH SUBCLASSCD_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.CURRICULUM_CD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN SCH_CHR_TEST T2 ";
        $query .= "              ON T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "              ON T3.YEAR        = T1.YEAR ";
        $query .= "             AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR            = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER    = '".$model->semester."' ";
        $query .= "         AND T3.GRADE       = '".$model->grade."' ";
        $query .= "         AND T1.CLASSCD     = '".$model->classcd."' ";
        $query .= "         AND T2.TESTKINDCD  = '".SUBSTR($model->testcd, 0, 2)."' ";
        $query .= "         AND T2.TESTITEMCD  = '".SUBSTR($model->testcd, 2, 2)."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.CURRICULUM_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CD.CLASSCD || '-' || CD.SCHOOL_KIND || '-' || CD.CURRICULUM_CD || '-' || CD.SUBCLASSCD AS VALUE, ";
        $query .= "     MST.SUBCLASSNAME AS LABEL, ";
        $query .= "     MST.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASSCD_DAT CD ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST MST ";
        $query .= "          ON MST.YEAR          = CD.YEAR ";
        $query .= "         AND MST.CLASSCD       = CD.CLASSCD ";
        $query .= "         AND MST.SCHOOL_KIND   = CD.SCHOOL_KIND ";
        $query .= "         AND MST.CURRICULUM_CD = CD.CURRICULUM_CD ";
        $query .= "         AND MST.SUBCLASSCD    = CD.SUBCLASSCD ";
        if ($subclasscd) {
            $query .= "     WHERE ";
            $query .= "         CD.CLASSCD || '-' || CD.SCHOOL_KIND || '-' || CD.CURRICULUM_CD || '-' || CD.SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //職員一覧取得
    public function getStaffMst($model, $staffcd = "")
    {
        $query  = " SELECT ";
        $query .= "     STAFFCD, ";
        $query .= "     STAFFNAME ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($staffcd) {
            $query .= "     AND STAFFCD = '".$staffcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //職員一覧取得 (監督者)
    public function getStaffMstProctor($model)
    {
        $query  = " SELECT ";
        $query .= "     STAFF.STAFFCD, ";
        $query .= "     STAFF.STAFFNAME, ";
        $query .= "     CHAIR.CHARGEDIV ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST STAFF ";
        $query .= "     LEFT JOIN CHAIR_STF_DAT CHAIR ";
        $query .= "          ON CHAIR.YEAR      = STAFF.YEAR ";
        $query .= "         AND CHAIR.SEMESTER  = '".$model->semester."' ";
        $query .= "         AND CHAIR.STAFFCD   = STAFF.STAFFCD ";
        $query .= "         AND CHAIR.CHAIRCD   = '".$model->staffChaircd."' ";
        $query .= " WHERE ";
        $query .= "     STAFF.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     CASE WHEN CHAIR.CHARGEDIV IS NULL THEN 0 ELSE 1 END DESC, ";
        $query .= "     CHAIR.CHARGEDIV DESC, ";
        $query .= "     STAFF.STAFFCD ";

        return $query;
    }

    //講座 正担任取得
    public function getChairStaffList($model, $chaircd)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T2.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST T2 ";
        $query .= "          ON T2.STAFFCD = T1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR          = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER  = '".$model->semester."' ";
        $query .= "     AND T1.CHAIRCD   = '".$chaircd."' ";
        $query .= "     AND T1.CHARGEDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD ";

        return $query;
    }

    //講座一覧
    public function getChairList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T1.CHAIRABBV, ";
        $query .= "     T5.PROCTOR_STAFFCD, ";
        $query .= "     T5.RETURN_STAFFCD, ";
        $query .= "     T5.Q_PAPERS, ";
        $query .= "     T5.A_PAPERS, ";
        $query .= "     T5.Q_BOTH_DIV, ";
        $query .= "     T5.A_BOTH_DIV, ";
        $query .= "     T5.DUE_DATE, ";
        $query .= "     T5.DUE_TIME, ";
        $query .= "     T5.REMARK, ";
        $query .= "     T7.STAFFNAME AS PROCTOR_STAFF_NAME, ";
        $query .= "     T6.STAFFNAME AS RETURN_STAFF_NAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN TESTITEM_LABEL_MST T5 ";
        $query .= "          ON T5.YEAR          = T1.YEAR ";
        $query .= "         AND T5.SEMESTER      = T1.SEMESTER ";
        $query .= "         AND T5.GRADE         = '".$model->grade."' ";
        $query .= "         AND T5.CLASSCD       = T1.CLASSCD ";
        $query .= "         AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND T5.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "         AND T5.CHAIRCD       = T1.CHAIRCD ";
        $query .= "         AND T5.TESTKINDCD || T5.TESTITEMCD || T5.SCORE_DIV = '".$model->testcd."' ";
        $query .= "     LEFT JOIN STAFF_MST T6 ";
        $query .= "          ON T6.STAFFCD = T5.RETURN_STAFFCD ";
        $query .= "     LEFT JOIN STAFF_MST T7 ";
        $query .= "          ON T7.STAFFCD = T5.PROCTOR_STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->subclasscd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //--- CHEACK（試験封筒ラベルマスタ）
    public function countTestitemLabelMst($model, $chaircd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     TESTITEM_LABEL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND GRADE    = '".$model->grade."' ";
        $query .= "     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->testcd."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        $query .= "     AND CHAIRCD  = '".$chaircd."' ";

        return $query;
    }

    //「更新ボタン」押し時の処理（メイン）・・・データがない場合、追加。ある場合、更新。
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->fields as $counter => $value) {
            $chaircd = $value["CHAIRCD"];
            $query = knjb103cQuery::countTestitemLabelMst($model, $chaircd);
            $dataCnt = $db->getOne($query);

            //データセット
            $data = array();

            $data["PROCTOR_STAFFCD"][TEXT]  = $value["PROCTOR_STAFFCD"];
            $data["RETURN_STAFFCD"][TEXT]   = $value["RETURN_STAFFCD"];
            $data["Q_PAPERS"][NUMBER]       = $value["Q_PAPERS"];
            $data["A_PAPERS"][NUMBER]       = $value["A_PAPERS"];
            $data["Q_BOTH_DIV"][TEXT]       = !strlen($value["Q_BOTH_DIV"]) ? "0" : $value["Q_BOTH_DIV"];
            $data["A_BOTH_DIV"][TEXT]       = !strlen($value["A_BOTH_DIV"]) ? "0" : $value["A_BOTH_DIV"];
            $data["DUE_DATE"][DATE]         = $value["DUE_DATE"];
            $data["DUE_TIME"][TEXT]         = $value["DUE_TIME"];
            $data["REMARK"][TEXT]           = $value["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR         = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER = '".$model->semester."' ";
                $where .= "     AND GRADE    = '".$model->grade."' ";
                $where .= "     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->testcd."' ";
                $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
                $where .= "     AND CHAIRCD  = '".$chaircd."' ";
        
                //更新
                $query = Query::updateSQL($data, "TESTITEM_LABEL_MST", $where);
            } else {
                list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $model->subclasscd);

                $data["YEAR"][TEXT]           = CTRL_YEAR;
                $data["SEMESTER"][TEXT]       = $model->semester;
                $data["GRADE"][TEXT]          = $model->grade;
                $data["TESTKINDCD"][TEXT]     = substr($model->testcd, 0, 2);
                $data["TESTITEMCD"][TEXT]     = substr($model->testcd, 2, 2);
                $data["SCORE_DIV"][TEXT]      = substr($model->testcd, 4, 2);
                $data["CLASSCD"][TEXT]        = $classcd;
                $data["SCHOOL_KIND"][TEXT]    = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]  = $curriculumCd;
                $data["SUBCLASSCD"][TEXT]     = $subclasscd;
                $data["CHAIRCD"][TEXT]        = $chaircd;

                //追加
                $query = Query::insertSQL($data, "TESTITEM_LABEL_MST");
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    public function &replaceUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $array = explode(",", $model->replace_data["selectdata"]);
        for ($idx = 0; $idx < get_count($array); $idx++) {
            $chaircd = $array[$idx];
            $query = knjb103cQuery::countTestitemLabelMst($model, $chaircd);
            $dataCnt = $db->getOne($query);

            //データセット
            $data = array();
            foreach ($model->replace_data["field"] as $key => $val) {
                if (in_array($key, array("Q_PAPERS", "A_PAPERS"))) {
                    $data[$key][NUMBER]  = $val;
                } elseif (in_array($key, array("Q_BOTH_DIV", "A_PAPERS"))) {
                    $data[$key][TEXT]    = !strlen($val) ? "0" : $val;
                } elseif (in_array($key, array("DUE_DATE"))) {
                    $data[$key][DATE]    = $val;
                } else {
                    $data[$key][TEXT]    = $val;
                }
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";

            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR         = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER = '".$model->semester."' ";
                $where .= "     AND GRADE    = '".$model->grade."' ";
                $where .= "     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->testcd."' ";
                $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
                $where .= "     AND CHAIRCD  = '".$chaircd."' ";
        
                //更新
                $query = Query::updateSQL($data, "TESTITEM_LABEL_MST", $where);
            } else {
                list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $model->subclasscd);

                $data["YEAR"][TEXT]           = CTRL_YEAR;
                $data["SEMESTER"][TEXT]       = $model->semester;
                $data["GRADE"][TEXT]          = $model->grade;
                $data["TESTKINDCD"][TEXT]     = substr($model->testcd, 0, 2);
                $data["TESTITEMCD"][TEXT]     = substr($model->testcd, 2, 2);
                $data["SCORE_DIV"][TEXT]      = substr($model->testcd, 4, 2);
                $data["CLASSCD"][TEXT]        = $classcd;
                $data["SCHOOL_KIND"][TEXT]    = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]  = $curriculumCd;
                $data["SUBCLASSCD"][TEXT]     = $subclasscd;
                $data["CHAIRCD"][TEXT]        = $chaircd;

                //追加
                $query = Query::insertSQL($data, "TESTITEM_LABEL_MST");
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
