<?php

require_once('for_php7.php');

class knjz210aQuery extends Query {

    //コース名コンボ取得
   function getCouseName() {
        $query  = "  SELECT DISTINCT ";
        $query .= "      T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "      T2.GRADE_NAME1 || ' ' || T3.COURSENAME || ' ' || T4.MAJORNAME || ' ' || T5.COURSECODENAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT T1, ";
        $query .= "      SCHREG_REGD_GDAT T2, ";
        $query .= "      COURSE_MST T3, ";
        $query .= "      MAJOR_MST T4, ";
        $query .= "      COURSECODE_MST T5 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "  AND T1.YEAR = T2.YEAR ";
        $query .= "  AND T1.GRADE = T2.GRADE ";
        $query .= "  AND T1.COURSECD = T3.COURSECD ";
        $query .= "  AND T1.MAJORCD = T4.MAJORCD ";
        $query .= "  AND T1.COURSECODE = T5.COURSECODE ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //教科取得
    function getClassMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND  AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || ':' || T1.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_MST T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    function getSubclassMst($classcd, $model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR ='".CTRL_YEAR."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND = '".$classcd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目別評定マスタ情報抽出
    function selectQuery($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        
        $query  = "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      ASSESS_SUBCLASS_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '".CTRL_YEAR."' ";
        $query .= "  AND GRADE          = '".$model->grade."' ";
        $query .= "  AND COURSECD       = '".$model->coursecd."' ";
        $query .= "  AND MAJORCD        = '".$model->majorcd."' ";
        $query .= "  AND COURSECODE     = '".$model->coursecode."' ";
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
        $query .= "      ASSESS_SUBCLASS_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '".CTRL_YEAR."' ";
        $query .= "  AND GRADE          = '".$model->grade."' ";
        $query .= "  AND COURSECD       = '".$model->coursecd."' ";
        $query .= "  AND MAJORCD        = '".$model->majorcd."' ";
        $query .= "  AND COURSECODE     = '".$model->coursecode."' ";
        $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        $db->query($query);
        
        $cnt = "5";
        $data = array();
        for ($i = 1; $i <= $cnt; $i++) {
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["GRADE"][TEXT]            = $model->grade;
            $data["COURSECD"][TEXT]         = $model->coursecd;
            $data["MAJORCD"][TEXT]          = $model->majorcd;
            $data["COURSECODE"][TEXT]       = $model->coursecode;
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
            $data["ASSESSLEVEL"][NUMBER]    = $i;
            $data["ASSESSMARK"][TEXT]       = $model->field["ASSESSMARK".$i];
            $data["ASSESSLOW"][NUMBER]      = $model->field["ASSESSLOW".$i];
            //上限値の更新
            if ($i == $cnt) {
                $data["ASSESSHIGH"][NUMBER] = "100";
            } else {
                $ASSESSHIGH = "ASSESSLOW".($i + 1);
                $data["ASSESSHIGH"][NUMBER] = ($model->field[$ASSESSHIGH] - 1);
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ASSESS_SUBCLASS_MST");
            $db->query($query);
        }
        
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //コピー先年度データカウント
    function cntToYearOrder($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ASSESS_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);
        
        return $data_check;
    }
    
    //コピー元年度データカウント
    function cntFromYearOrder($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ASSESS_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR - 1)."' ";

        $data_check = $db->getOne($query);
        Query::dbCheckIn($db);
        
        return $data_check;
    }
    
    //指定年度から対象年度へコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ASSESS_SUBCLASS_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             GRADE, ";
        $query .= "             COURSECD, ";
        $query .= "             MAJORCD, ";
        $query .= "             COURSECODE, ";
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
        $query .= "         GRADE, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
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
        $query .= "         ASSESS_SUBCLASS_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".(CTRL_YEAR - 1)."' ";
        $query .= "     ) ";
        
        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        
        return true;
    }

}
?>
