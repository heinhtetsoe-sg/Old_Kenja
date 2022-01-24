<?php

class knjm437wQuery extends Query {

    //テスト種別
    function getTestcd($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS VALUE ";
        $query .= "   , T2.TESTITEMNAME AS LABEL ";
        $query .= " FROM NAME_MST T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = T1.NAMESPARE1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'M002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //科目（講座）リスト
    function ReadQuery($model) {
        //参照・更新可
        if (AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT DISTINCT CHAIRCD,CHAIRNAME AS LABEL, ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= " CHAIRCD || CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS VALUE ";
            } else {
                $query .= " CHAIRCD || SUBCLASSCD AS VALUE ";
            }
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR ='" .CTRL_YEAR ."'";
            $query .= "AND SUBSTR(SUBCLASSCD,1,2) < '92' ";
            if (CTRL_SEMESTER == '1') {
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $query .= " AND CLASSCD || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT NAME1 || NAME2 || NAME3 FROM NAME_MST WHERE NAMECD1 = 'M016') ";
                } else {
                    $query .= " AND SUBCLASSCD NOT IN (SELECT NAME3 FROM NAME_MST WHERE NAMECD1 = 'M016') ";
                }
            } else if (CTRL_SEMESTER == '2') {
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $query .= " AND CLASSCD || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT NAME1 || NAME2 || NAME3 FROM NAME_MST WHERE NAMECD1 = 'M015') ";
                } else {
                    $query .= " AND SUBCLASSCD NOT IN (SELECT NAME3 FROM NAME_MST WHERE NAMECD1 = 'M015') ";
                }
            }
            $query .= " ORDER BY CHAIRCD ";
        //参照・更新可（制限付き）
        } else {
            $query  = "SELECT DISTINCT CHAIRCD,CHAIRNAME AS LABEL, ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= " CHAIRCD || CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS VALUE ";
            } else {
                $query .= " CHAIRCD || SUBCLASSCD AS VALUE ";
            }
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SUBSTR(SUBCLASSCD,1,2) < '92' ";
            if (CTRL_SEMESTER == '1') {
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $query .= " AND CLASSCD || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT NAME1 || NAME2 || NAME3 FROM NAME_MST WHERE NAMECD1 = 'M016') ";
                } else {
                    $query .= " AND SUBCLASSCD NOT IN (SELECT NAME3 FROM NAME_MST WHERE NAMECD1 = 'M016') ";
                }
            } else if (CTRL_SEMESTER == '2') {
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $query .= " AND CLASSCD || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT NAME1 || NAME2 || NAME3 FROM NAME_MST WHERE NAMECD1 = 'M015') ";
                } else {
                    $query .= " AND SUBCLASSCD NOT IN (SELECT NAME3 FROM NAME_MST WHERE NAMECD1 = 'M015') ";
                }
            }
            $query .= "AND CHAIRCD in ";
            $query .= " (SELECT CHAIRCD ";
            $query .= "  FROM CHAIR_STF_DAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
            $query .= "AND STAFFCD ='" .STAFFCD ."')";
            $query .= " ORDER BY CHAIRCD ";
        }

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " WITH SUBCLASS_STD_SELECT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         TSL.YEAR, ";
        $query .= "         TSL.SEMESTER, ";
        $query .= "         TSL.CLASSCD, ";
        $query .= "         TSL.SCHOOL_KIND, ";
        $query .= "         TSL.CURRICULUM_CD, ";
        $query .= "         TSL.SUBCLASSCD, ";
        $query .= "         TSL.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_SELECT_DAT TSL ";
        $query .= "     WHERE ";
        $query .= "         TSL.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND TSL.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND TSL.CLASSCD || TSL.SCHOOL_KIND || TSL.CURRICULUM_CD || TSL.SUBCLASSCD = '".substr($model->sub,7)."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     TSL.YEAR, ";
        $query .= "     TSL.SEMESTER, ";
        $query .= "     TSL.CLASSCD, ";
        $query .= "     TSL.SCHOOL_KIND, ";
        $query .= "     TSL.CURRICULUM_CD, ";
        $query .= "     TSL.SUBCLASSCD, ";
        $query .= "     TSL.SCHREGNO, ";
        $query .= "     BASE.NAME_SHOW, ";
        $query .= "     CASE WHEN TPS.SCHREGNO IS NULL THEN '' ";
        $query .= "          WHEN TPS.SEM_PASS_FLG = '1' THEN '許可' ";
        $query .= "          ELSE '／' ";
        $query .= "     END AS JUKEN_KYOKA, ";
        $query .= "     TPS.SEM_PASS_FLG, ";
        $query .= "     MST.TESTITEMNAME AS TEST_NAME, ";
        $query .= "     TAT.TESTKINDCD || TAT.TESTITEMCD || TAT.SCORE_DIV AS TESTCD, ";
        $query .= "     TAT.ATTEND, ";
        $query .= "     TAT.TEST_DATE ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT TSL ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = TSL.SCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_STD_PASS_SDIV_DAT TPS ON TSL.YEAR = TPS.YEAR ";
        $query .= "         AND TSL.SEMESTER = TPS.SEMESTER ";
        $query .= "         AND TPS.TESTKINDCD || TPS.TESTITEMCD || TPS.SCORE_DIV = '".$model->testcd."' ";
        $query .= "         AND TSL.CLASSCD = TPS.CLASSCD ";
        $query .= "         AND TSL.SCHOOL_KIND = TPS.SCHOOL_KIND ";
        $query .= "         AND TSL.CURRICULUM_CD = TPS.CURRICULUM_CD ";
        $query .= "         AND TSL.SUBCLASSCD = TPS.SUBCLASSCD ";
        $query .= "         AND TSL.SCHREGNO = TPS.SCHREGNO ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV MST ON MST.YEAR = TPS.YEAR ";
        $query .= "         AND MST.SEMESTER = TPS.SEMESTER ";
        $query .= "         AND MST.TESTKINDCD = TPS.TESTKINDCD ";
        $query .= "         AND MST.TESTITEMCD = TPS.TESTITEMCD ";
        $query .= "         AND MST.SCORE_DIV = TPS.SCORE_DIV ";
        $query .= "     LEFT JOIN TEST_ATTEND_SDIV_HIST_DAT TAT ON TSL.YEAR = TAT.YEAR ";
        $query .= "         AND TSL.SEMESTER = TAT.SEMESTER ";
        $query .= "         AND TAT.TESTKINDCD || TAT.TESTITEMCD || TAT.SCORE_DIV = '".$model->testcd."' ";
        $query .= "         AND TSL.CLASSCD = TAT.CLASSCD ";
        $query .= "         AND TSL.SCHOOL_KIND = TAT.SCHOOL_KIND ";
        $query .= "         AND TSL.CURRICULUM_CD = TAT.CURRICULUM_CD ";
        $query .= "         AND TSL.SUBCLASSCD = TAT.SUBCLASSCD ";
        $query .= "         AND TSL.SCHREGNO = TAT.SCHREGNO ";
        $query .= "         AND TAT.SEQ = 1 ";
        $query .= " WHERE ";
        $query .= "     VALUE(BASE.INOUTCD, '') <> '8' ";
        $query .= " ORDER BY ";
        $query .= "     SUBSTR(TSL.SCHREGNO, 1, 4) DESC, SUBSTR(TSL.SCHREGNO, 5, 4) ASC ";
        return $query;
    }

    /* データ更新処理 */
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        //変数
        $year = CTRL_YEAR;
        $semester = CTRL_SEMESTER;
        $subcd = substr($model->sub,7);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($subcd, 0, 2);
            $substr_school_kind      = substr($subcd, 2, 1);
            $substr_curriculum_cd    = substr($subcd, 3, 1);
            $substr_subclasscd       = substr($subcd, 4, 6);
        }

        $cnt = count($model->setdata["SCHREGNO"]);
        for ($i = 1; $i <= $cnt; $i++) {
            //変数
            $schno = $model->setdata["SCHREGNO"][$i];

            //削除
            //一旦、選択されたテスト種別と同じテスト種別のレコードを削除する。
            $query  = "DELETE FROM TEST_ATTEND_SDIV_HIST_DAT ";
            $query .= " WHERE YEAR = '{$year}'";
            $query .= "   AND SEMESTER = '{$semester}'";
            $query .= "   AND TESTKINDCD = '".substr($model->testcd, 0, 2)."'";
            $query .= "   AND TESTITEMCD = '".substr($model->testcd, 2, 2)."'";
            $query .= "   AND SCORE_DIV  = '".substr($model->testcd, 4, 2)."'";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND CLASSCD          = '".$substr_classcd."' ";
                $query .= "   AND SCHOOL_KIND      = '".$substr_school_kind."' ";
                $query .= "   AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                $query .= "   AND SUBCLASSCD       = '".$substr_subclasscd."' ";
            } else {
                $query .= "   AND SUBCLASSCD = '{$subcd}'";
            }
            $query .= "   AND SCHREGNO = '{$schno}'";
            $query .= "   AND SEQ = 1 ";
            $db->query($query);

            if ($model->setdata["CHK_DIS"][$i] == "1") {
                continue;
            }

            //追加
            $data = array();
            $data["YEAR"][TEXT] = $year;
            $data["SEMESTER"][TEXT] = $semester;
            $data["TESTKINDCD"][TEXT] = substr($model->testcd, 0, 2);
            $data["TESTITEMCD"][TEXT] = substr($model->testcd, 2, 2);
            $data["SCORE_DIV"][TEXT] = substr($model->testcd, 4, 2);
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]       = $substr_classcd;
                $data["SCHOOL_KIND"][TEXT]   = $substr_school_kind;
                $data["CURRICULUM_CD"][TEXT] = $substr_curriculum_cd;
                $data["SUBCLASSCD"][TEXT]    = $substr_subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT] = $subcd;
            }
            $data["SCHREGNO"][TEXT] = $schno;
            $data["SEQ"][NUMBER] = 1;
            $data["TEST_DATE"][TEXT] = str_replace("/", "-", $model->test_date);
            $data["ATTEND"][TEXT] = ($model->setdata["CHK_JUKEN"][$i] == "1") ? "1" : "";
            $data["REGISTERCD"][TEXT] = STAFFCD ;
            $data["UPDATED"][FUNC] = "sysdate()";
            $query = Query::insertSQL($data, "TEST_ATTEND_SDIV_HIST_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

}
?>
