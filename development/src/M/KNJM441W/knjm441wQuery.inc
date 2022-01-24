<?php

require_once('for_php7.php');
class knjm441wQuery extends Query {
    //学期取得
    function getSemesterMst() {
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

    //テスト種別
    function getTestcd($model)
    {
        $kihonSub = ($model->Properties["useCurriculumcd"] == '1') ? "00-".SCHOOLKIND."-00-000000" : "000000";  //テスト種別は管理者コントロールの基本設定を使用

        $query  = "    SELECT ";
        $query .= "      T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "      T1.TESTITEMNAME AS LABEL ";
        $query .= "    FROM ";
        $query .= "      TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "      INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ";
        $query .= "        ON T1.YEAR = T2.YEAR ";
        $query .= "        AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = T2.SEMESTER || T2.TESTKINDCD || ";
        $query .= "        T2.TESTITEMCD || T2.SCORE_DIV ";
        $query .= "    WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$kihonSub."' ";
        } else {
            $query .= "  AND T2.SUBCLASSCD = '".kihonSub."' ";
        }
        $query .= "    ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //科目取得
    function getSubclassList($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T2.SUBCLASSCD      = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                          AND T2.CLASSCD         = T3.CLASSCD ";
            $query .= "                          AND T2.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                          AND T2.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        if ($model->field["SEMESTER"] == "2") {
            $query .= "     (T1.SEMESTER = '".$model->field["SEMESTER"]."' OR ";
            $query .= "      T1.SEMESTER = '1') AND ";
        } else {
            $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        }
        $query .= "     T1.CHAIRCD = T2.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得
    function getStudentList($model, $list) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.SCHREGNO || '　' || T3.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASS"]."' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASS"]."' AND ";
        }
        $query .= "     T2.SCHREGNO NOT IN ('".implode("','",$list)."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒取得
    function getPassStudentList($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.SCHREGNO || '　' || T3.NAME_SHOW || (CASE WHEN SA.PRINT_FLG = '1' THEN '(発行済み)' ELSE '' END) AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "    LEFT JOIN SUBCLASS_STD_PASS_SDIV_DAT SA ";
        $query .= "   ON SA.YEAR = T1.YEAR ";
        $query .= "  AND SA.SEMESTER = T1.SEMESTER ";
        $query .= "  AND SA.CLASSCD = T1.CLASSCD ";
        $query .= "  AND SA.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "  AND SA.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "  AND SA.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "  AND SA.TESTKINDCD || SA.TESTITEMCD || SA.SCORE_DIV = '".$model->field["TESTCD"]."', ";
        $query .= "     CHAIR_STD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "     SA.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_STD_PASS_SDIV_DAT S1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR         = S1.YEAR AND ";
        $query .= "                 T1.SEMESTER     = S1.SEMESTER AND ";
        $query .= "                 S1.TESTKINDCD || S1.TESTITEMCD || S1.SCORE_DIV = '".$model->field["TESTCD"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 T1.CLASSCD       = S1.CLASSCD AND ";
            $query .= "                 T1.SCHOOL_KIND   = S1.SCHOOL_KIND AND ";
            $query .= "                 T1.CURRICULUM_CD = S1.CURRICULUM_CD AND ";
        }
        $query .= "                 T1.SUBCLASSCD   = S1.SUBCLASSCD AND ";
        $query .= "                 T2.SCHREGNO     = S1.SCHREGNO AND ";
        $query .= "                 S1.SEM_PASS_FLG = '1' ) AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASS"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASS"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新対象生徒取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASS"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASS"]."' ";
        }

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->Properties["useCurriculumcd"] == "1") {
            list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->field["SUBCLASS"]);
        }

        $result = $db->query(knjm441wQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["SEM_PASS_FLG"][TEXT]     = (in_array($row["SCHREGNO"], $model->selectdata)) ? "1" : "";
            if ($model->cmd == "updateprint") {
                $data["PRINT_FLG"][TEXT] = (in_array($row["SCHREGNO"], $model->selectdata)) ? "1" : "";
            } else if (!(in_array($row["SCHREGNO"], $model->selectdata))) {
                $data["PRINT_FLG"][TEXT] = "";
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if ($db->getOne(knjm441wQuery::checkSubclassStdPassDat($model, $row["SCHREGNO"])) > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR            = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER        = '".$model->field["SEMESTER"]."' AND ";
                $where .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTCD"]."' AND ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASS"]."' AND ";
                } else {
                    $where .= "     SUBCLASSCD      = '".$model->field["SUBCLASS"]."' AND ";
                }
                $where .= "     SCHREGNO        = '".$row["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "SUBCLASS_STD_PASS_SDIV_DAT", $where);
            } else {
                $data["YEAR"][TEXT]            = CTRL_YEAR;
                $data["SEMESTER"][TEXT]        = $model->field["SEMESTER"];
                $data["TESTKINDCD"][TEXT]      = substr($model->field["TESTCD"], 0, 2);
                $data["TESTITEMCD"][TEXT]      = substr($model->field["TESTCD"], 2, 2);
                $data["SCORE_DIV"][TEXT]       = substr($model->field["TESTCD"], 4, 2);
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $data["CLASSCD"][TEXT]          = $classcd;
                    $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                    $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                } else {
                    $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASS"];
                }
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];

                $query = Query::insertSQL($data, "SUBCLASS_STD_PASS_SDIV_DAT");
            }
            $db->query($query);
        }
        $result->free();

        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //データの存在チェック
    function checkSubclassStdPassDat($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_PASS_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER        = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$model->field["TESTCD"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASS"]."' AND ";
        } else {
            $query .= "     SUBCLASSCD      = '".$model->field["SUBCLASS"]."' AND ";
        }
        $query .= "     SCHREGNO        = '".$schregno."' ";

        return $query;
    }
}
?>
