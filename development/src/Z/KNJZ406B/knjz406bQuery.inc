<?php

require_once('for_php7.php');

class knjz406bQuery extends Query {

    function getD084() {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D084' ";
        $query .= "     AND NAMECD2 = '1' ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($model) {

        $tmpSql = knjz406bQuery::kyoukaTantouSql();

        $query  = " SELECT ";
        $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS AS VALUE, ";
        $query .= "     REGD_H.HR_NAME AS LABEL ";
        $query  .= "    , CASE ";
        $query  .= "      WHEN EXISTS ( ".$tmpSql." ) THEN 1 ELSE 0 END AS KYOUKA_TANTOU_FLG";

        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR        = '".CTRL_YEAR."' AND";
        $query .= "     REGD_H.SEMESTER    = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ( ";
            $query .= "             ( REGD_H.TR_CD1      = '".STAFFCD."' ";
            $query .= "              OR REGD_H.TR_CD2      = '".STAFFCD."' ";
            $query .= "              OR REGD_H.TR_CD3      = '".STAFFCD."' ";
            $query .= "              OR REGD_H.SUBTR_CD1   = '".STAFFCD."' ";
            $query .= "              OR REGD_H.SUBTR_CD2   = '".STAFFCD."' ";
            $query .= "              OR REGD_H.SUBTR_CD3   = '".STAFFCD."') ";

            $query  .= "        OR ";

            //教科担当の場合、担当している講座に対応する年組を絞る
            $query  .= "            EXISTS ( ".$tmpSql." ) ";
            $query  .= "    ) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND REGD_H.GRADE IN (  SELECT ";
                $query .= "                             GRADE ";
                $query .= "                         FROM ";
                $query .= "                             SCHREG_REGD_GDAT ";
                $query .= "                         WHERE ";
                $query .= "                             YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                             SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND REGD_H.GRADE IN (  SELECT ";
            $query .= "                             GRADE ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_GDAT ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                             SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";

        return $query;
    }

    function kyoukaTantouSql() {
            $query   = "        SELECT ";
            $query  .= "          T1.YEAR ";
            $query  .= "          , T1.SEMESTER ";
            $query  .= "          , T2.STAFFCD ";
            $query  .= "          , T3.TRGTGRADE ";
            $query  .= "          , T3.TRGTCLASS ";
            $query  .= "        FROM ";
            $query  .= "          CHAIR_DAT T1 ";
            $query  .= "          INNER JOIN CHAIR_STF_DAT T2 ";
            $query  .= "            ON T2.YEAR = T1.YEAR ";
            $query  .= "            AND T2.SEMESTER = T1.SEMESTER ";
            $query  .= "            AND T2.CHAIRCD = T1.CHAIRCD ";
            $query  .= "          INNER JOIN CHAIR_CLS_DAT T3 ";
            $query  .= "            ON T3.YEAR = T1.YEAR ";
            $query  .= "            AND T3.SEMESTER = T1.SEMESTER ";
            $query  .= "            AND T3.CHAIRCD = T1.CHAIRCD ";
            $query  .= "            AND T3.GROUPCD = T1.GROUPCD ";
            $query  .= "        WHERE ";
            $query  .= "          T1.YEAR = REGD_H.YEAR ";
            $query  .= "          AND T1.SEMESTER = REGD_H.SEMESTER ";
            $query  .= "          AND T3.TRGTGRADE = REGD_H.GRADE ";
            $query  .= "          AND T3.TRGTCLASS = REGD_H.HR_CLASS ";
            $query  .= "          AND T2.STAFFCD = '".STAFFCD."' ";
            $query  .= "        GROUP BY ";
            $query  .= "          T1.YEAR ";
            $query  .= "          , T1.SEMESTER ";
            $query  .= "          , T2.STAFFCD ";
            $query  .= "          , T3.TRGTGRADE ";
            $query  .= "          , T3.TRGTCLASS ";

            return $query;
    }

    function selectSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     REGD_G.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT REGD_G ";
        $query .= " WHERE ";
        $query .= "     REGD_G.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_G.GRADE = '{$grade}' ";

        return $query;
    }

    //科目取得
    function getSubclass($model) {
        list($grade, $hr_class) = explode('-', $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSCD || ' ' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= " INNER JOIN ";
        $query .= "     JVIEWNAME_GRADE_YDAT T2 ";
        $query .= " ON ";
        $query .= "         T1.GRADE        = T2.GRADE ";
        $query .= "     AND T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND T1.CLASSCD          = T2.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        }
        
        $query .= " INNER JOIN ";
        $query .= "     V_SUBCLASS_MST T3 ";
        $query .= " ON ";
        $query .= "         T2.YEAR         = T3.YEAR ";
        $query .= "     AND T1.SUBCLASSCD   = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND T1.CLASSCD          = T3.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND      = T3.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD    = T3.CURRICULUM_CD ";
        }

        if (in_array($model->field["GRADE_HR_CLASS"], $model->kyoukaTantouList)) {
            $query .= "     INNER JOIN CHAIR_DAT CD ";
            $query .= "              ON CD.YEAR       = T2.YEAR ";
            $query .= "             AND CD.SEMESTER   = '".CTRL_SEMESTER."' ";
            $query .= "             AND CD.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "             AND CD.CLASSCD = T2.CLASSCD ";
                $query .= "             AND CD.SCHOOL_KIND = T2.SCHOOL_KIND ";
                $query .= "             AND CD.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
            $query .= "     INNER JOIN CHAIR_CLS_DAT CCD ";
            $query .= "              ON CCD.YEAR     = CD.YEAR ";
            $query .= "             AND CCD.SEMESTER = CD.SEMESTER ";
            $query .= "             AND CCD.CHAIRCD =  CD.CHAIRCD ";
            $query .= "             AND CCD.GROUPCD =  CD.GROUPCD ";
            $query .= "             AND CCD.TRGTGRADE = '".$grade."' ";
            $query .= "             AND CCD.TRGTCLASS = '".$hr_class."' ";
            $query  .= "    INNER JOIN CHAIR_STF_DAT CSD ";
            $query  .= "      ON CSD.YEAR = CD.YEAR ";
            $query  .= "      AND CSD.SEMESTER = CD.SEMESTER ";
            $query  .= "      AND CSD.CHAIRCD = CD.CHAIRCD ";
            $query  .= "      AND CSD.STAFFCD = '".STAFFCD."' ";
        }

        $query .= " WHERE ";
        $query .= "     T2.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE        = '".$grade."' AND ";
        $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) <= '91' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //観点一覧取得
    function getViewList($model) {
        list($grade, $hr_class) = explode('-', $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     T1.VIEWCD AS VALUE, ";
        $query .= "     T1.VIEWCD || ':' || VALUE(T1.VIEWABBV,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT S1 ";
        $query .= "              ON S1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "             AND S1.GRADE        = T1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND S1.CLASSCD      = T1.CLASSCD ";
            $query .= "             AND S1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "             AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND S1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "             AND S1.VIEWCD       = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト単元データ取得
    function getUnitTestDat($model,$flg="") {
        $query  = " SELECT ";
        if ($flg == "count"){
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     SEQ, ";
            $query .= "     SORT, ";
            $query .= "     UNIT_L_NAME, ";
            $query .= "     UNIT_TEST_DATE ";
        }
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     DATA_DIV    = '2' AND ";
        $query .= "     GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($flg != "count"){
            $query .= " ORDER BY ";
            $query .= "     SORT, ";
            $query .= "     SEQ ";
        }

        return $query;
    }

    //データ取得
    function getUnitTestInputseqDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }

        return $query;
    }

    //更新処理
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list ($grade, $hr_class) = explode('-', $model->field["GRADE_HR_CLASS"]);

        if ($model->Properties["useCurriculumcd"] == '1') {
            list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->field["SUBCLASSCD"]);
        } else {
            $subclasscd = $model->field["SUBCLASSCD"];
        }

        foreach ($model->field["SEQ"] as $key => $seq) {
            //更新対象の単元のみ処理
            if ($model->setData[$seq]["CHECK"]) {

                //追加・更新
                foreach ($model->viewList as $viewcd) {

                    //存在チェック
                    $query  = " SELECT ";
                    $query .= "     COUNT(*) ";
                    $query .= " FROM ";
                    $query .= "     UNIT_TEST_INPUTSEQ_DAT ";
                    $query .= " WHERE ";
                    $query .= "     YEAR            = '".CTRL_YEAR."' AND ";
                    $query .= "     GRADE           = '".$grade."' AND ";
                    $query .= "     HR_CLASS        = '".$hr_class."' AND ";
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "     CLASSCD         = '".$classcd."' AND ";
                        $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
                        $query .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
                    }
                    $query .= "     SUBCLASSCD      = '".$subclasscd."' AND ";
                    $query .= "     SEQ             =  ".$seq." AND ";
                    $query .= "     VIEWCD          =  '".$viewcd."' ";
                    $cnt = $db->getOne($query);


                    $data = array();
                    if($cnt == 0){
                        //追加
                        $data["YEAR"][TEXT]                 = CTRL_YEAR;
                        $data["GRADE"][TEXT]                = $grade;
                        $data["HR_CLASS"][TEXT]             = $hr_class;
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $data["CLASSCD"][TEXT]              = $classcd;
                            $data["SCHOOL_KIND"][TEXT]          = $school_kind;
                            $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
                        }
                        $data["SUBCLASSCD"][TEXT]           = $subclasscd;
                        $data["SEQ"][NUMBER]                = $seq;
                        $data["VIEWCD"][TEXT]               = $viewcd;
                        $data["VIEWFLG"][TEXT]              = ($model->setData[$seq]["VIEWFLG_".$viewcd]) ? "1" : "";
                        $data["UNIT_ASSESSHIGH"][NUMBER]    = $model->setData[$seq]["UNIT_ASSESSHIGH_".$viewcd];
                        $data["WEIGHTING"][NUMBER]          = $model->setData[$seq]["WEIGHTING_".$viewcd];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][FUNC]              = "sysdate()";
                        $query = Query::insertSQL($data, "UNIT_TEST_INPUTSEQ_DAT");
                    } else {
                        //更新
                        $data["VIEWFLG"][TEXT]              = ($model->setData[$seq]["VIEWFLG_".$viewcd]) ? "1" : "";
                        $data["UNIT_ASSESSHIGH"][NUMBER]    = $model->setData[$seq]["UNIT_ASSESSHIGH_".$viewcd];
                        $data["WEIGHTING"][NUMBER]          = $model->setData[$seq]["WEIGHTING_".$viewcd];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][FUNC]              = "sysdate()";

                        $where  = " WHERE ";
                        $where .= "     YEAR            = '".CTRL_YEAR."' AND ";
                        $where .= "     GRADE           = '".$grade."' AND ";
                        $where .= "     HR_CLASS        = '".$hr_class."' AND ";
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $where .= "     CLASSCD         = '".$classcd."' AND ";
                            $where .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
                            $where .= "     CURRICULUM_CD   = '".$curriculum_cd."' AND ";
                        }
                        $where .= "     SUBCLASSCD      = '".$subclasscd."' AND ";
                        $where .= "     SEQ             =  ".$seq." AND ";
                        $where .= "     VIEWCD          =  '".$viewcd."' ";

                        $query = Query::updateSQL($data, "UNIT_TEST_INPUTSEQ_DAT", $where);
                    }
                    
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //データ存在チェック -- UNIT_TEST_DAT
    function checkUnitTestDat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     DATA_DIV    = '2' ";

        return $query;
    }

    //データ存在チェック -- JVIEWNAME_GRADE_YDAT
    function checkJviewnameGradeYdat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //データ存在チェック -- UNIT_TEST_INPUTSEQ_DAT
    function checkUnitTestInpuseqDat($model, $year) {
        $query  = " SELECT ";
        $query .= "     COUNT(T1.VIEWCD) AS CNT ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT S1 ";
        $query .= "              ON T1.YEAR         = S1.YEAR ";
        $query .= "             AND T1.GRADE        = S1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T1.CLASSCD      = S1.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND  = S1.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD = S1.CURRICULUM_CD ";
        }
        $query .= "             AND T1.SUBCLASSCD   = S1.SUBCLASSCD ";
        $query .= "             AND T1.VIEWCD       = S1.VIEWCD ";
        $query .= "     INNER JOIN UNIT_TEST_DAT S2 ";
        $query .= "              ON T1.YEAR         = S2.YEAR ";
        $query .= "             AND S2.DATA_DIV     = '2' ";
        $query .= "             AND T1.GRADE        = S2.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T1.CLASSCD      = S2.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND  = S2.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD = S2.CURRICULUM_CD ";
        }
        $query .= "             AND T1.SUBCLASSCD   = S2.SUBCLASSCD ";
        $query .= "             AND T1.SEQ          = S2.SEQ ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' AND ";
//        $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) = '91' ";
        $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) <= '91' ";

        return $query;
    }

    //コピー
    function &getCopyQuery($db, $model) {
        $pre_year   = CTRL_YEAR - 1;
        $this_year  = CTRL_YEAR;

        $query  = " INSERT INTO UNIT_TEST_INPUTSEQ_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$this_year."' AS YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWFLG, ";
        $query .= "     T1.UNIT_ASSESSHIGH, ";
        $query .= "     T1.WEIGHTING, ";
        $query .= "     T1.WEIGHTING_EXE, ";
        $query .= "     T1.WEIGHTING_CALC, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT S1 ";
        $query .= "              ON S1.YEAR         = '".$this_year."' ";
        $query .= "             AND T1.GRADE        = S1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T1.CLASSCD      = S1.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND  = S1.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD = S1.CURRICULUM_CD ";
        }
        $query .= "             AND T1.SUBCLASSCD   = S1.SUBCLASSCD ";
        $query .= "             AND T1.VIEWCD       = S1.VIEWCD ";
        $query .= "     INNER JOIN UNIT_TEST_DAT S2 ";
        $query .= "              ON S2.YEAR         = '".$this_year."' ";
        $query .= "             AND S2.DATA_DIV     = '2' ";
        $query .= "             AND T1.GRADE        = S2.GRADE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T1.CLASSCD      = S2.CLASSCD ";
            $query .= "             AND T1.SCHOOL_KIND  = S2.SCHOOL_KIND ";
            $query .= "             AND T1.CURRICULUM_CD = S2.CURRICULUM_CD ";
        }
        $query .= "             AND T1.SUBCLASSCD   = S2.SUBCLASSCD ";
        $query .= "             AND T1.SEQ          = S2.SEQ ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$pre_year."' AND ";
//        $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) = '91' ";
        $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) <= '91' ";

        $db->query($query);

        return;
    }
}
?>
