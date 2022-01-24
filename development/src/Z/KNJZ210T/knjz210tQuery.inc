<?php

require_once('for_php7.php');

class knjz210tQuery extends Query {

    //学期取得
    function getSemesterMst()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    function getTestcd($model, $semester)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || ' ' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $model->testTable;
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' AND ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     SEMESTER = '".$semester."' AND ";
        }
        $query .= "     TESTKINDCD IN ('01','02','99') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入力チェック対象講座リスト
    function SelectQuery($model, $record_div)
    {
        if($model->semester == "9"){
            $query  = " WITH CHAIRCD AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     MAX(T1.SEMESTER) AS SEMESTER, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT T1, ";
            $query .= "     CHAIR_STD_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
            $query .= "     T1.SUBCLASSCD IS NOT NULL ";
            $query .= " GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD ";
            $query .= " ), CHAIRNAME AS (";
            $query .= " SELECT ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     T2.CHAIRNAME ";
            $query .= " FROM ";
            $query .= "     CHAIRCD T1 ";
            $query .= "     LEFT JOIN CHAIR_DAT T2 ON ";
            $query .= "         T2.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
            $query .= "     AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD = T2.CLASSCD ";
                $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
            $query .= " ) ";
            $query .= " SELECT DISTINCT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T2.SUBCLASSNAME, ";
            $query .= "     T1.RECORD_DIV, ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     T3.CHAIRNAME ";
            $query .= " FROM ";
            $query .= "     RECORD_CHKFIN_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON  ";
            $query .= "         T1.SUBCLASSCD = T2.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD = T2.CLASSCD ";
                $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
            $query .= "     LEFT JOIN CHAIRNAME T3 ON ";
            $query .= "         T1.CHAIRCD = T3.CHAIRCD ";
        } else {
            $query  = " SELECT DISTINCT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T2.SUBCLASSNAME, ";
            $query .= "     T1.RECORD_DIV, ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     T3.CHAIRNAME ";
            $query .= " FROM ";
            $query .= "     RECORD_CHKFIN_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON  ";
            $query .= "         T1.SUBCLASSCD = T2.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD = T2.CLASSCD ";
                $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
            $query .= "     LEFT JOIN CHAIR_DAT T3 ON ";
            $query .= "         T1.YEAR = T3.YEAR AND ";
            $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
            $query .= "         T1.CHAIRCD = T3.CHAIRCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '".$model->testcd."' AND ";
        $query .= "     T1.RECORD_DIV =  '".$record_div."' ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;

    }

    //科目取得
    function getSubclass($model)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.COMBINED_CLASSCD || '-' || L1.COMBINED_SCHOOL_KIND || '-' || L1.COMBINED_CURRICULUM_CD || '-' || L1.COMBINED_SUBCLASSCD AS COMBINED_VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || S1.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     L1.COMBINED_SUBCLASSCD AS COMBINED_VALUE, ";
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSCD || ' ' || S1.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     LEFT JOIN SUBCLASS_MST S1 ON T1.SUBCLASSCD = S1.SUBCLASSCD ";
            $query .= "                              AND T1.CLASSCD       = S1.CLASSCD ";
            $query .= "                              AND T1.SCHOOL_KIND   = S1.SCHOOL_KIND ";
            $query .= "                              AND T1.CURRICULUM_CD = S1.CURRICULUM_CD ";
            $query .= "     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT L1 ON L1.COMBINED_SUBCLASSCD    = S1.SUBCLASSCD ";
            $query .= "                                               AND L1.COMBINED_CLASSCD       = S1.CLASSCD ";
            $query .= "                                               AND L1.COMBINED_SCHOOL_KIND   = S1.SCHOOL_KIND ";
            $query .= "                                               AND L1.COMBINED_CURRICULUM_CD = S1.CURRICULUM_CD ";
            $query .= "                                               AND L1.YEAR = '".CTRL_YEAR."', ";
        } else {
            $query .= "     LEFT JOIN SUBCLASS_MST S1 ON T1.SUBCLASSCD = S1.SUBCLASSCD ";
            $query .= "     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT L1 ON L1.COMBINED_SUBCLASSCD = S1.SUBCLASSCD ";
            $query .= "                                               AND L1.YEAR = '".CTRL_YEAR."', ";
        }
        $query .= "     CHAIR_STD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        if($model->semester != "9"){
            $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        }
        $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= "     T1.SUBCLASSCD IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //対象講座一覧
    function getChkChrList($model) {
        $record_div = $model->record_div;
        if($model->semester == "9"){
            $query  = " WITH CHAIRCD AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     MAX(T1.SEMESTER) AS SEMESTER, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT T1, ";
            $query .= "     CHAIR_STD_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
            $query .= "     T1.SUBCLASSCD IS NOT NULL ";
            $query .= " GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD ";
            $query .= " ) ";
            $query .= " SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.RECORD_DIV AS RECORD_DIV2, ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     VALUE(T1.EXECUTED, '0') AS EXECUTED, ";
            $query .= "     T3.CHAIRNAME ";
            $query .= " FROM ";
            $query .= "     RECORD_CHKFIN_DAT T1, ";
            $query .= "     CHAIRCD T2 ";
            $query .= "     LEFT JOIN CHAIR_DAT T3 ON ";
            $query .= "         T3.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T2.SEMESTER = T3.SEMESTER ";
            $query .= "     AND T2.CHAIRCD = T3.CHAIRCD ";
            $query .= "     AND T2.SUBCLASSCD = T3.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T2.CLASSCD = T3.CLASSCD ";
                $query .= "     AND T2.SCHOOL_KIND = T3.SCHOOL_KIND ";
                $query .= "     AND T2.CURRICULUM_CD = T3.CURRICULUM_CD ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= " AND T1.SEMESTER = '".$model->semester."' ";
            $query .= " AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD = T2.CLASSCD ";
                $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
            if ($model->subclasscd != 'XXXXXX') {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "  AND T1.SUBCLASSCD = '".substr($model->subclasscd, 7, 6)."' ";
                } else {
                    $query .= "  AND T1.SUBCLASSCD = '".$model->subclasscd."' ";
                }
            }
            $query .= " AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->testcd."' ";
            $query .= " AND T1.CHAIRCD = T2.CHAIRCD ";
            $query .= " AND T1.RECORD_DIV = '".$record_div."' ";
        } else {
            $query  = " SELECT DISTINCT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.RECORD_DIV AS RECORD_DIV2, ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     VALUE(T1.EXECUTED, '0') AS EXECUTED, ";
            $query .= "     T2.CHAIRNAME ";
            $query .= " FROM ";
            $query .= "     RECORD_CHKFIN_DAT T1,";
            $query .= "     CHAIR_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR ";
            $query .= " AND T1.YEAR = '".CTRL_YEAR."' ";
            $query .= " AND T1.SEMESTER = T2.SEMESTER ";
            $query .= " AND T1.SEMESTER = '".$model->semester."' ";
            $query .= " AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD = T2.CLASSCD ";
                $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
            if ($model->subclasscd != 'XXXXXX') {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "  AND T1.SUBCLASSCD = '".substr($model->subclasscd, 7, 6)."' ";
                } else {
                    $query .= "  AND T1.SUBCLASSCD = '".$model->subclasscd."' ";
                }
            }
            $query .= " AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->testcd."' ";
            $query .= " AND T1.CHAIRCD = T2.CHAIRCD ";
            $query .= " AND T1.RECORD_DIV = '".$record_div."' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     RECORD_DIV2, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //講座一覧
    function getChairList($model, $chaircd)
    {
        if($model->semester == "9"){
            $query  = " WITH CHAIRCD AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     MAX(T1.SEMESTER) AS SEMESTER, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT T1, ";
            $query .= "     CHAIR_STD_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
            $query .= "     T1.SUBCLASSCD IS NOT NULL ";
            $query .= " GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD ";
            $query .= " ) ";
            $query .= " SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     T2.CHAIRNAME ";
            $query .= " FROM ";
            $query .= "     CHAIRCD T1 ";
            $query .= "     LEFT JOIN CHAIR_DAT T2 ON ";
            $query .= "         T2.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
            $query .= "     AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD = T2.CLASSCD ";
                $query .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                $query .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
            $query .= " WHERE ";
            if ($model->subclasscd != 'XXXXXX') {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "  T1.SUBCLASSCD = '".substr($model->subclasscd, 7, 6)."' AND ";
                } else {
                    $query .= "  T1.SUBCLASSCD = '".$model->subclasscd."' AND ";
                }
            }
            $query .= "     T1.CHAIRCD NOT IN ('".implode("','",$chaircd)."') ";
        } else {
            $query  = " SELECT DISTINCT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     T1.CHAIRNAME ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT T1, ";
            $query .= "     CHAIR_STD_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = T2.YEAR ";
            $query .= " AND T1.YEAR = '".CTRL_YEAR."' ";
            $query .= " AND T1.SEMESTER = T2.SEMESTER ";
            $query .= " AND T1.SEMESTER = '".$model->semester."' ";
            if ($model->subclasscd != 'XXXXXX') {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "  AND T1.SUBCLASSCD = '".substr($model->subclasscd, 7, 6)."' ";
                } else {
                    $query .= "  AND T1.SUBCLASSCD = '".$model->subclasscd."' ";
                }
            }
            $query .= " AND T1.CHAIRCD = T2.CHAIRCD ";
            $query .= " AND T1.CHAIRCD NOT IN ('".implode("','",$chaircd)."') ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    function copy_check($semester, $testcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$testcd}' AND ";
        $query .= "     RECORD_DIV IN ('1','2') ";

        return $query;
    }

    //更新
    function &UpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $record_div = $model->record_div;

        //削除
        $query  = " DELETE FROM  ";
        $query .= "     RECORD_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".$model->semester."' ";
        if ($model->subclasscd != 'XXXXXX') {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "  AND SUBCLASSCD = '".substr($model->subclasscd, 7, 6)."' ";
            } else {
                $query .= "  AND SUBCLASSCD = '".$model->subclasscd."' ";
            }
        }
        $query .= " AND TESTKINDCD || TESTITEMCD = '".$model->testcd."' ";
        $query .= " AND RECORD_DIV = '".$record_div."' ";

        $db->query($query);

        //追加
        $chaircdArray = explode(',',$model->field["CHAIRCD"]);
        if($model->field["CHAIRCD"]){
            for ($i = 0; $i < get_count($chaircdArray); $i++){
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    list($classcd, $school_kind, $curriculum_cd, $subclasscd, $chaircd, $executed) = explode("_", $chaircdArray[$i]);
                } else {
                    list($subclasscd, $chaircd, $executed) = explode("_", $chaircdArray[$i]);
                }
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->semester;
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]       = $classcd;
                    $data["SCHOOL_KIND"][TEXT]   = $school_kind;
                    $data["CURRICULUM_CD"][TEXT] = $curriculum_cd;
                }
                $data["SUBCLASSCD"][TEXT]   = $subclasscd;
                $data["CHAIRCD"][TEXT]      = $chaircd;
                $data["TESTKINDCD"][TEXT]   = substr($model->testcd,0,2);
                $data["TESTITEMCD"][TEXT]   = substr($model->testcd,2);
                $data["RECORD_DIV"][TEXT]   = $record_div;
                $data["EXECUTEDATE"][DATE]  = "";
                $data["EXECUTED"][TEXT]     = $executed;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "RECORD_CHKFIN_DAT"));
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }

    //コピー元データ取得
    function getCopyData($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->semester."' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '".$model->testcd."' AND ";
        $query .= "     RECORD_DIV = '".$model->record_div."' ";

        return $query;

    }

    //コピー元のデータをコピー先にINSERT
    function &copyInsert($Row, $model) {

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SEMESTER"][TEXT]     = $Row["SEMESTER"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $Row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = $Row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT] = $Row["CURRICULUM_CD"];
        } 
        $data["SUBCLASSCD"][TEXT]   = $Row["SUBCLASSCD"];
        $data["CHAIRCD"][TEXT]      = $Row["CHAIRCD"];
        $data["TESTKINDCD"][TEXT]   = $Row["TESTKINDCD"];
        $data["TESTITEMCD"][TEXT]   = $Row["TESTITEMCD"];
        $data["RECORD_DIV"][TEXT]   = ($Row["RECORD_DIV"] == "2") ? "1" : "2";
        $data["EXECUTED"][TEXT]     = '0';
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "RECORD_CHKFIN_DAT");
        return $query;
    }

    //コピー元データ取得
    function getCopyData_2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     CHAIRCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        if ($model->semester_2 == 9) {
            $query .= "     2 AS RECORD_DIV ";
        } else {
            $query .= "     RECORD_DIV ";
        }
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$model->semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD = '{$model->testcd}' AND ";
        $query .= "     RECORD_DIV IN ('1','2') ";

        return $query;
    }

    //コピー元のデータをコピー先にINSERT
    function &copyInsert_2($Row, $model) {
        $testkindcd = substr($model->testcd_2, 0, 2);
        $testitemcd = substr($model->testcd_2, 2, 2);

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SEMESTER"][TEXT]     = $model->semester_2;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $Row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = $Row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT] = $Row["CURRICULUM_CD"];
        } 
        $data["SUBCLASSCD"][TEXT]   = $Row["SUBCLASSCD"];
        $data["CHAIRCD"][TEXT]      = $Row["CHAIRCD"];
        $data["TESTKINDCD"][TEXT]   = $testkindcd;
        $data["TESTITEMCD"][TEXT]   = $testitemcd;
        $data["RECORD_DIV"][TEXT]   = $Row["RECORD_DIV"];
        $data["EXECUTED"][TEXT]     = '0';
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "RECORD_CHKFIN_DAT");
        return $query;
    }

    //学期名取得
    function getSemesterName($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$model->semester}' ";

        return $query;
    }

    //テスト名取得
    function getTestName($model) {
        $query  = " SELECT ";
        $query .= "     TESTITEMNAME ";
        $query .= " FROM ";
        $query .=       $model->testTable;
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' AND ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     SEMESTER = '{$model->semester}' AND ";
        }
        $query .= "     TESTKINDCD || TESTITEMCD = '{$model->testcd}' ";

        return $query;
    }
}
?>