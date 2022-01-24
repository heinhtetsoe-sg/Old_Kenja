<?php

require_once('for_php7.php');

class knjd129iquery extends Query {

    //学期
    function getSemester($semester="") {
        $query  = " SELECT ";
        if ($semester == "") {
            $query .= "     SEMESTER AS VALUE, ";
        }
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($semester == "") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query .= " AND SEMESTER = '".$semester."' ";
        }

        return $query;
    }

    //学年取得
    function getGrade() {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //学校校種を取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        
        return $query;
    }

    //コースグループを取得する
    function getCourseGroup($model) {
        $query  = " WITH SCHREG_DATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GROUP_CD AS VALUE, ";
        $query .= "     T1.GROUP_CD || ':' || L2.GROUP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_DATA L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                              AND L1.COURSECD = T1.COURSECD ";
        $query .= "                              AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "                              AND L1.COURSECODE = T1.COURSECODE ";
        $query .= "                              AND L1.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_HDAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                      AND L2.GRADE = T1.GRADE ";
        $query .= "                                      AND L2.GROUP_CD = T1.GROUP_CD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //科目取得
    function getSubclass($model)
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolkind}' ";
        $query .= "     AND EXISTS (SELECT 'X' FROM  ";
        $query .= "              CREDIT_MST L1 ";
        $query .= "              INNER JOIN COURSE_GROUP_CD_DAT L2 ON L1.YEAR = L2.YEAR ";
        $query .= "                AND L1.GRADE = L2.GRADE ";
        $query .= "                AND L2.GROUP_CD = '".$model->field["GROUP_CD"]."' ";
        $query .= "                AND L1.COURSECD = L2.COURSECD ";
        $query .= "                AND L1.MAJORCD = L2.MAJORCD ";
        $query .= "                AND L1.COURSECODE = L2.COURSECODE ";
        $query .= "              WHERE L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                AND L1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "                AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "                AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "            ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //単位認定ラインの算出
    function getPassScoreLine($model) {
        $query  = " SELECT ";
        $query .= "     SCORE_LINE ";
        $query .= " FROM ";
        $query .= "     PASS_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND TESTKINDCD || ':' || TESTITEMCD || ':' || SCORE_DIV = '99:00:08' ";
        $query .= " AND RUISEKI_DIV = '3' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= " AND PASS_DIV = '5' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " AND HR_CLASS = '000' ";
        $query .= " AND COURSECD = '0' ";
        $query .= " AND MAJORCD = '".$model->field["GROUP_CD"]."' ";
        $query .= " AND COURSECODE = '0000' ";

        return $query;
    }

    function getAssessLevelSdivMstAssessLow($model) {
        $query  = " SELECT ";
        $query .= "     ASSESSLOW ";
        $query .= " FROM ";
        $query .= "     ASSESS_LEVEL_SDIV_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND TESTKINDCD || ':' || TESTITEMCD || ':' || SCORE_DIV = '99:00:08' ";
        $query .= " AND RUISEKI_DIV = '3' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= " AND DIV = '5' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " AND HR_CLASS = '000' ";
        $query .= " AND COURSECD = '0' ";
        $query .= " AND MAJORCD = '".$model->field["GROUP_CD"]."' ";
        $query .= " AND COURSECODE = '0000' ";
        $query .= " AND ASSESSLEVEL    = 2 ";

        return $query;
    }

    //補充点の固定表示
    function getScorePass()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     '1' AS VALUE, ";
        $query .= "     '1' || ':' || '合格' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '2' AS VALUE, ";
        $query .= "     '2' || ':' || '不合格' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $getLineScore, $lineScoreDiv) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SUPP_SCHREGNOS AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCORE_PASS, ";
        $query .= "     T1.SCORE_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '99:00:08' "; //固定
        $query .= " AND T1.CLASSCD = '".$subclass_array[0]."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$subclass_array[1]."' ";
        $query .= " AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
        $query .= " AND T1.SUBCLASSCD = '".$subclass_array[3]."' ";
        $query .= " ) , RANK_SCHREGNOS AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     CAST(NULL AS SMALLINT) AS SCORE_PASS, ";
        $query .= "     CAST(NULL AS VARCHAR(1)) AS SCORE_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '99:00:08' "; //固定
        $query .= " AND T1.CLASSCD = '".$subclass_array[0]."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$subclass_array[1]."' ";
        $query .= " AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
        $query .= " AND T1.SUBCLASSCD = '".$subclass_array[3]."' ";
        if ($getLineScore != '' && $lineScoreDiv != '') {
            if ($lineScoreDiv == 'IKA') {
                $query .= " AND T1.SCORE <= ".$getLineScore." ";
            } else if ($lineScoreDiv == 'MIMAN') {
                $query .= " AND T1.SCORE < ".$getLineScore." ";
            }
        } else {
            $query .= " AND T1.SCORE < -1 ";
        }
        $query .= " AND NOT EXISTS (SELECT 'X' ";
        $query .= "   FROM SUPP_SCHREGNOS ";
        $query .= "   WHERE T1.SCHREGNO = SCHREGNO ";
        $query .= "     AND T1.CLASSCD = CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = SUBCLASSCD ";
        $query .= "  )";
        $query .= " ) , SCHREGNOS AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCORE_PASS, ";
        $query .= "     T1.SCORE_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     SUPP_SCHREGNOS T1 ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T1.TESTITEMCD, ";
        $query .= "     T1.SCORE_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCORE_PASS, ";
        $query .= "     T1.SCORE_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     RANK_SCHREGNOS T1 ";

        $query .= " ) , RECORD_RANK_SUPP_EXA_SDIV_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SCORE, ";
        $query .= "     T1.SCORE_PASS, ";
        $query .= "     T1.SCORE_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREGNOS T1 ";
        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                      AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                                      AND L1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                      AND L1.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                      AND L1.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "                                      AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "                                      AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                      AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                      AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                      AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '99:00:08' ";//固定
        $query .= " AND T1.CLASSCD = '".$subclass_array[0]."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$subclass_array[1]."' ";
        $query .= " AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
        $query .= " AND T1.SUBCLASSCD = '".$subclass_array[3]."' ";
        
        $query .= " ), COURSE_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " AND GROUP_CD = '".$model->field["GROUP_CD"]."' ";
        
        $query .= " ), SCHINFO AS (  ";
        $query .= "      SELECT DISTINCT ";
        $query .= "          T1.SCHREGNO, ";
        $query .= "          T1.HR_CLASS,   ";
        $query .= "          T1.ATTENDNO,  ";
        $query .= "          T2.NAME,  ";
        $query .= "          T3.HR_NAME  ";
        $query .= "      FROM  ";
        $query .= "          SCHREG_REGD_DAT T1  ";
        $query .= "          INNER JOIN COURSE_DATA L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                             AND L1.COURSECD = T1.COURSECD ";
        $query .= "                             AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "                             AND L1.COURSECODE = T1.COURSECODE, ";
        $query .= "          SCHREG_BASE_MST T2,  ";
        $query .= "          SCHREG_REGD_HDAT T3  ";
        $query .= "      WHERE  ";
        $query .= "          T1.YEAR = '".CTRL_YEAR."'  ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "      AND T1.GRADE  = '".$model->field["GRADE"]."'  ";
        $query .= "      AND T1.SCHREGNO = T2.SCHREGNO  ";
        $query .= "      AND T1.YEAR = T3.YEAR  ";
        $query .= "      AND T1.GRADE = T3.GRADE  ";
        $query .= "      AND T1.HR_CLASS = T3.HR_CLASS  ";
        $query .= "  )  ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_NAME,  ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.SCORE, ";
        $query .= "     T2.SCORE_PASS, ";
        $query .= "     T2.SCORE_PASS_FLG ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     INNER JOIN RECORD_RANK_SUPP_EXA_SDIV_DATA T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $db = Query::dbCheckOut();

        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            $schregno = $model->fields["SCHREGNO"][$i];

            $query  = " DELETE FROM ";
            $query .= "     SUPP_EXA_SDIV_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' ";
            $query .= " AND SEMESTER   = '".$model->field["SEMESTER"]."' ";
            $query .= " AND TESTKINDCD || ':' || TESTITEMCD || ':' || SCORE_DIV = '99:00:08' ";//固定
            $query .= " AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= " AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= " AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= " AND SUBCLASSCD    = '".$subclass_array[3]."' ";
            $query .= " AND SCHREGNO      = '".$schregno."' ";
            $db->query($query);

            //if (!strlen($model->fields["SCORE_PASS_FLG"][$i])) {
            //    continue;
            //}

            //項目
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]    = '99';
            $data["TESTITEMCD"][TEXT]    = '00';
            $data["SCORE_DIV"][TEXT]     = '08';
            $data["CLASSCD"][TEXT]       = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
            $data["SCHREGNO"][TEXT]      = $schregno;
            $data["SCORE_PASS_FLG"][TEXT]= $model->fields["SCORE_PASS_FLG"][$i];
            $data["SCORE_PASS"][NUMBER]  = $model->fields["SCORE_PASS"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "SUPP_EXA_SDIV_DAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
