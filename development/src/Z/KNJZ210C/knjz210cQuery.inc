<?php

require_once('for_php7.php');

class knjz210cQuery extends Query {

    //Z010
    function getZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //テーブルチェック
    function hasTableField($db, $tabname, $colname) {

        $query  = " SELECT ";
        $query .= "     COLNAME ";
        $query .= " FROM ( ";
        $query .= "    SELECT ";
        $query .= "        COLNAME ";
        $query .= "    FROM ";
        $query .= "        SYSCAT.COLUMNS ";
        $query .= "    WHERE ";
        $query .= "        TABNAME = '".$tabname."' ";
        $query .= "        AND COLNAME = '".$colname."' ";
        $query .= " ) T1 ";

        return ($db->getOne($query) != ""); 
    }
 
    //学校マスタ
    function getSchoolMstSemesAssesscd($schoolKind = "") {
        $query  = " SELECT ";
        $query .= "     SEMES_ASSESSCD ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($schoolKind) {
            $query .= "     AND SCHOOL_KIND = '".$schoolKind."' ";
        }

        return $query;
    }
 
    //学校マスタ
    function getTestItemMstCountflgNewSdivCount($schoolKind) {
        $query  = " WITH TMP AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV) AS COUNT ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     <> '9' ";
        $query .= "     AND T1.TESTITEMCD   = '01' ";
        $query .= "     AND T1.SCORE_DIV    = '08' ";
        $query .= " GROUP BY ";
        $query .= "       T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAX(COUNT) ";
        $query .= " FROM ";
        $query .= "     TMP ";

        return $query;
    }

    //評定区分
    function getAssesscd() {
        $query  = " WITH HDAT(VALUE, LABEL) AS ( ";
        $query .= " VALUES('5', '5:評定換算マップ') ";
        $query .= " ) ";
        $query .= " SELECT  ";
        $query .= "     VALUE, LABEL ";
        $query .= " FROM ";
        $query .= "     HDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校種別の取得
    function getNameMstA023($model, $field = "") {
        $query  = " SELECT ";
        if ($field) {
            $query .= "     ".$field." AS VALUE ";
        } else {
            $query .= "     NAME1 AS VALUE, ";
            $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($field) {
            $query .= "     AND NAME1 = '".$model->field["SCHOOL_KIND"]."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 IN ('".SCHOOLKIND."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    function getSubclassMst($model, $schoolKindName, $flg="") {
        $query  = " WITH TMP (LABEL, VALUE) AS (";
        $query .= " VALUES (";
        $query .= "     '00' || '-' || '".$model->field["SCHOOL_KIND"]."' || '-' || '00'|| '-' || '000000'|| ':' || '基本設定（".$schoolKindName."）', ";
        $query .= "     '00' || '-' || '".$model->field["SCHOOL_KIND"]."' || '-' || '00'|| '-' || '000000' ";
        $query .= "     ) ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR ='".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     TMP ";
        if ($flg == "COPY") {
            $query .= " WHERE VALUE IN (";
            $query .= "     SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ";
            $query .= "     FROM ASSESS_TESTCOUNT_MST ";
            $query .= "     WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "       AND ASSESSCD = '".$model->field["ASSESSCD"]."' ";
            $query .= " )";
            $query .= " AND VALUE <> '".$model->field["SUBCLASSCD"]."'";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目別評定マスタ情報抽出
    function selectQuery($model, $testCount, $assessLevel)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        
        $query  = "  SELECT ";
        $query .= "      YEAR         ";
        $query .= "    , TESTCOUNT    ";
        $query .= "    , ASSESSLEVEL  ";
        $query .= "    , CLASSCD      ";
        $query .= "    , SCHOOL_KIND  ";
        $query .= "    , CURRICULUM_CD";
        $query .= "    , SUBCLASSCD   ";
        $query .= "    , ASSESSMARK ";
        $query .= "    , INT(ASSESSHIGH) AS ASSESSHIGH ";
        $query .= "    , INT(ASSESSLOW) AS ASSESSLOW ";
        $query .= "  FROM ";
        $query .= "      ASSESS_TESTCOUNT_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '".CTRL_YEAR."' ";
        $query .= "  AND ASSESSCD       = ".$model->field["ASSESSCD"]." ";
        $query .= "  AND TESTCOUNT      = ".$testCount." ";
        $query .= "  AND ASSESSLEVEL    = ".$assessLevel." ";
        $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        
        return $query;
    }
    
    //Insert作成
    function &getInsertQuery($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $query  =  " DELETE FROM ";
        $query .= "      ASSESS_TESTCOUNT_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '".CTRL_YEAR."' ";
        $query .= "  AND ASSESSCD       = '".$model->field["ASSESSCD"]."' ";
        $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        $db->query($query);
        
        for ($ti = 1; $ti <= $model->testCnt; $ti++) {
            for ($i = 1; $i <= $model->assessCnt; $i++) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["ASSESSCD"][TEXT]         = $model->field["ASSESSCD"];
                $data["TESTCOUNT"][NUMBER]      = $ti;
                $data["CLASSCD"][TEXT]          = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
                $data["ASSESSLEVEL"][NUMBER]    = $i;
                $data["ASSESSMARK"][TEXT]       = $model->field["ASSESSMARK".$i];
                $data["ASSESSLOW"][NUMBER]      = $model->field["ASSESSLOW".$i."_".$ti];
                //上限値の更新
                if ($i == $model->assessCnt) {
                    $data["ASSESSHIGH"][NUMBER] = $model->setMax * $ti;
                } else {
                    $data["ASSESSHIGH"][NUMBER] = $model->field["ASSESSLOW".($i + 1)."_".$ti] - 1;
                }
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "ASSESS_TESTCOUNT_MST");
                $db->query($query);
            }
        }
        
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //コピー先年度データカウント
    function cntSubclassOrder($db, $model) {
        $copy_subclass_array = array();
        $copy_subclass_array = explode("-", $model->field["COPY_SUBCLASSCD"]);
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ASSESS_TESTCOUNT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ASSESSCD = '".$model->field["ASSESSCD"]."' ";
        $query .= "     AND CLASSCD =       '".$copy_subclass_array[0]."' ";
        $query .= "     AND SCHOOL_KIND =   '".$copy_subclass_array[1]."' ";
        $query .= "     AND CURRICULUM_CD = '".$copy_subclass_array[2]."' ";
        $query .= "     AND SUBCLASSCD =    '".$copy_subclass_array[3]."' ";

        $data_check = $db->getOne($query);
        
        return $data_check;
    }

    //コピー先年度データカウント
    function cntYearOrder($db, $model, $year) {
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ASSESS_TESTCOUNT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        $data_check = $db->getOne($query);
        
        return $data_check;
    }
    
    //指定年度から対象年度へコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ASSESS_TESTCOUNT_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             ASSESSCD, ";
        $query .= "             TESTCOUNT, ";
        $query .= "             CLASSCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             CURRICULUM_CD, ";
        $query .= "             SUBCLASSCD, ";
        $query .= "             ASSESSLEVEL, ";
        $query .= "             ASSESSMARK, ";
        $query .= "             ASSESSLOW, ";
        $query .= "             ASSESSHIGH, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         ASSESSCD, ";
        $query .= "         TESTCOUNT, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         ASSESSLEVEL, ";
        $query .= "         ASSESSMARK, ";
        $query .= "         ASSESSLOW, ";
        $query .= "         ASSESSHIGH, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         ASSESS_TESTCOUNT_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".(CTRL_YEAR - 1)."' ";
        $query .= "     ) ";
        
        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        
        return true;
    }

    //指定科目からコピー
    function &getCopySubclassQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $copy_subclass_array = array();
        $copy_subclass_array = explode("-", $model->field["COPY_SUBCLASSCD"]);

        $query  =  " DELETE FROM ";
        $query .= "      ASSESS_TESTCOUNT_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '".CTRL_YEAR."' ";
        $query .= "  AND ASSESSCD       = '".$model->field["ASSESSCD"]."' ";
        $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        $db->query($query);
        
        $query  = " INSERT INTO ASSESS_TESTCOUNT_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             ASSESSCD, ";
        $query .= "             TESTCOUNT, ";
        $query .= "             CLASSCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             CURRICULUM_CD, ";
        $query .= "             SUBCLASSCD, ";
        $query .= "             ASSESSLEVEL, ";
        $query .= "             ASSESSMARK, ";
        $query .= "             ASSESSLOW, ";
        $query .= "             ASSESSHIGH, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         ASSESSCD, ";
        $query .= "         TESTCOUNT, ";
        $query .= "         '".$subclass_array[0]."' AS CLASSCD, ";
        $query .= "         '".$subclass_array[1]."' AS SCHOOL_KIND, ";
        $query .= "         '".$subclass_array[2]."' AS CURRICULUM_CD, ";
        $query .= "         '".$subclass_array[3]."' AS SUBCLASSCD, ";
        $query .= "         ASSESSLEVEL, ";
        $query .= "         ASSESSMARK, ";
        $query .= "         ASSESSLOW, ";
        $query .= "         ASSESSHIGH, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         ASSESS_TESTCOUNT_MST ";
        $query .= "     WHERE ";
        $query .= "      YEAR           = '".CTRL_YEAR."' ";
        $query .= "  AND ASSESSCD       = '".$model->field["ASSESSCD"]."' ";
        $query .= "  AND CLASSCD        = '".$copy_subclass_array[0]."' ";
        $query .= "  AND SCHOOL_KIND    = '".$copy_subclass_array[1]."' ";
        $query .= "  AND CURRICULUM_CD  = '".$copy_subclass_array[2]."' ";
        $query .= "  AND SUBCLASSCD     = '".$copy_subclass_array[3]."' ";
        $query .= "     ) ";
    
        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        
        return true;
    }

    //Delete作成
    function &getDeleteQuery($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $query  =  " DELETE FROM ";
        $query .= "      ASSESS_TESTCOUNT_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '".CTRL_YEAR."' ";
        $query .= "  AND ASSESSCD       = '".$model->field["ASSESSCD"]."' ";
        $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }



}
?>
