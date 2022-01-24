<?php

require_once('for_php7.php');


class knjd649Query extends Query {

    //テスト種別取得
    function getDataDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H308' AND ";
        $query .= "     NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    function getMockName($model)
    {
        $query  = " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(MOCKCD,1,1) = '".$model->field["DATA_DIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     CAST(CAST(GRADE AS SMALLINT) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE <= '10' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース取得（権限チェック）
    function getCourse($model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || '　' || S1.COURSENAME || S2.MAJORNAME || S3.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_MST S1 ON T1.COURSECD = S1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST S2 ON T1.MAJORCD = S2.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST S3 ON T1.COURSECODE = S3.COURSECODE, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T1.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.GRADE = '".$model->field["GRADE"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    function getMockNameCsv($model)
    {
        $query  = " SELECT ";
        $query .= "     MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     MOCKCD = '".$model->field["MOCKCD"]."' ";

        return $query;
    }

    //選択項目名取得
    function getSelectNameCsv($model, $selectdata)
    {
        if($model->field["SELECT_DIV"] == "1"){
            $query  = " SELECT DISTINCT";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     GRADE || HR_CLASS = '".$selectdata."' ";
        } elseif($model->field["SELECT_DIV"] == "2"){
            $query  = " SELECT DISTINCT";
            $query .= "     COURSECODE AS VALUE, ";
            $query .= "     COURSECODENAME AS NAME ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST ";
            $query .= " WHERE ";
            $query .= "     COURSECODE IN(SELECT DISTINCT ";
            $query .= "                     COURSECODE  ";
            $query .= "                   FROM  ";
            $query .= "                     SCHREG_REGD_DAT ";
            $query .= "                   WHERE ";
            $query .= "                     COURSECD || MAJORCD || COURSECODE = '".$selectdata."') ";
        } else {
            $query  = " SELECT DISTINCT";
            $query .= "     GRADE VALUE, ";
            $query .= "     CAST(CAST(GRADE AS SMALLINT) AS CHAR(1)) || '学年' AS NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //科目名称取得
    function getSubclassNameCsv($model, $selectdata)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T2.SUBCLASS_NAME ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_GROUP_DAT T1 ";
        $query .= "     LEFT JOIN MOCK_SUBCLASS_MST T2 ON T1.MOCK_SUBCLASS_CD = T2.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.MOCKCD = '".$model->field["MOCKCD"]."' AND ";
        $query .= "     T1.GRADE = '".$model->field["GRADE"]."' ";
        if($model->field["SELECT_DIV"] == "1"){
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ";
            $query .= "                   ( SELECT DISTINCT ";
            $query .= "                         COURSECD || MAJORCD || COURSECODE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                         SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                         GRADE || HR_CLASS = '".$selectdata."' ";
            $query .= "                   ) ";
        } elseif($model->field["SELECT_DIV"] == "2") {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$selectdata."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";

        return $query;
    }

    //中高一貫判定取得
    function getIkkankou()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //明細表取得
    function getCntStudentCsv1($model, $f_score, $t_score, $subclasscd, $selectdata, $total="")
    {
        $query  = " WITH ";
        $query .= " STUDENT1 AS(" ;
        if($model->field["SELECT_DIV"] == "1"){
            $query .= " SELECT ";
            $query .= "     S1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT S1, ";
            $query .= "     SCHREG_BASE_MST S2 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     S1.SCHREGNO = S2.SCHREGNO AND ";
            $query .= "     S2.SEX = '1' AND ";
            $query .= "     S1.GRADE || S1.HR_CLASS = '".$selectdata."' ";
        } elseif($model->field["SELECT_DIV"] == "2") {
            $query .= " SELECT ";
            $query .= "     S1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT S1, ";
            $query .= "     SCHREG_BASE_MST S2 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     S1.SCHREGNO = S2.SCHREGNO AND ";
            $query .= "     S1.GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "     S2.SEX = '1' AND ";
            $query .= "     S1.COURSECD || S1.MAJORCD || S1.COURSECODE = '".$selectdata."' ";
        } else {
            $query .= " SELECT ";
            $query .= "     S1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT S1, ";
            $query .= "     SCHREG_BASE_MST S2 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     S1.SCHREGNO = S2.SCHREGNO AND ";
            $query .= "     S1.GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "     S2.SEX = '1'";
        }
        $query .= " )," ;
        $query .= " STUDENT2 AS(" ;
        if($model->field["SELECT_DIV"] == "1"){
            $query .= " SELECT ";
            $query .= "     S1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT S1, ";
            $query .= "     SCHREG_BASE_MST S2 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     S1.SCHREGNO = S2.SCHREGNO AND ";
            $query .= "     S2.SEX = '2' AND ";
            $query .= "     S1.GRADE || S1.HR_CLASS = '".$selectdata."' ";
        } elseif($model->field["SELECT_DIV"] == "2") {
            $query .= " SELECT ";
            $query .= "     S1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT S1, ";
            $query .= "     SCHREG_BASE_MST S2 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     S1.SCHREGNO = S2.SCHREGNO AND ";
            $query .= "     S1.GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "     S2.SEX = '2' AND ";
            $query .= "     S1.COURSECD || S1.MAJORCD || S1.COURSECODE = '".$selectdata."' ";
        } else {
            $query .= " SELECT ";
            $query .= "     S1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT S1, ";
            $query .= "     SCHREG_BASE_MST S2 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     S1.SCHREGNO = S2.SCHREGNO AND ";
            $query .= "     S1.GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "     S2.SEX = '2' ";
        }
        $query .= " )," ;
        $query .= " STUDENT3 AS(" ;
        if($model->field["SELECT_DIV"] == "1"){
            $query .= " SELECT ";
            $query .= "     S1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT S1, ";
            $query .= "     SCHREG_BASE_MST S2 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     S1.SCHREGNO = S2.SCHREGNO AND ";
            $query .= "     S1.GRADE || S1.HR_CLASS = '".$selectdata."' ";
        } elseif($model->field["SELECT_DIV"] == "2") {
            $query .= " SELECT ";
            $query .= "     S1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT S1, ";
            $query .= "     SCHREG_BASE_MST S2 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     S1.SCHREGNO = S2.SCHREGNO AND ";
            $query .= "     S1.GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "     S1.COURSECD || S1.MAJORCD || S1.COURSECODE = '".$selectdata."' ";
        } else {
            $query .= " SELECT ";
            $query .= "     S1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT S1, ";
            $query .= "     SCHREG_BASE_MST S2 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     S1.SCHREGNO = S2.SCHREGNO AND ";
            $query .= "     S1.GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= " )" ;

        $query .= " SELECT ";
        $query .= "     '".$f_score."' AS FSCORE, ";
        $query .= "     COUNT(S1.SCHREGNO) AS STUDENT1, ";
        $query .= "     COUNT(S2.SCHREGNO) AS STUDENT2, ";
        $query .= "     COUNT(S3.SCHREGNO) AS STUDENT3 ";
        $query .= " FROM ";
        $query .= "     MOCK_RANK_DAT T1 ";
        $query .= "     LEFT JOIN STUDENT1 S1 ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "     LEFT JOIN STUDENT2 S2 ON T1.SCHREGNO = S2.SCHREGNO ";
        $query .= "     LEFT JOIN STUDENT3 S3 ON T1.SCHREGNO = S3.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.MOCKCD = '".$model->field["MOCKCD"]."' AND ";
        $query .= "     T1.MOCK_SUBCLASS_CD = '".$subclasscd."' AND ";
        $query .= "     T1.MOCKDIV = '1' ";
        if($total == ""){
//            if($f_score == '0'){
//                $query .= "     AND T1.SCORE = ".$f_score." ";
//            } else {
                $query .= "     AND T1.SCORE >= ".$f_score."";
                $query .= "     AND T1.SCORE <= ".$t_score."";
//            }
        }
        $query .= " GROUP BY ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";

        return $query;
    }

    //項目名取得
    function getItemNameCsv($model)
    {
        $selectdata = explode(",", $model->selectdata);

        if($model->field["SELECT_DIV"] == "1"){
            $query  = " SELECT DISTINCT";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     GRADE || HR_CLASS IN('" .implode($selectdata, "','") ."') ";
        } elseif($model->field["SELECT_DIV"] == "2"){
            $query  = " SELECT DISTINCT";
            $query .= "     COURSECODE AS VALUE, ";
            $query .= "     COURSECODENAME AS NAME ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST ";
            $query .= " WHERE ";
            $query .= "     COURSECODE IN(SELECT DISTINCT ";
            $query .= "                     COURSECODE  ";
            $query .= "                   FROM  ";
            $query .= "                     SCHREG_REGD_DAT ";
            $query .= "                   WHERE ";
            $query .= "                     COURSECD || MAJORCD || COURSECODE IN('" .implode($selectdata, "','") ."')) ";
        } else {
            $query  = " SELECT DISTINCT";
            $query .= "     GRADE VALUE, ";
            $query .= "     CAST(CAST(GRADE AS SMALLINT) AS CHAR(1)) || '学年' AS NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //集計表取得
    function getCntStudentCsv2($model, $f_score, $t_score, $subclasscd, $selectdata, $total="")
    {
        $query  = " WITH ";
        $query .= " STUDENT AS(" ;
        if($model->field["SELECT_DIV"] == "1"){
            $query .= " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     HR_CLASS AS ITEM ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     GRADE || HR_CLASS = '".$selectdata."' ";
        } elseif($model->field["SELECT_DIV"] == "2") {
            $query .= " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     COURSECD || MAJORCD || COURSECODE AS ITEM ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "     COURSECD || MAJORCD || COURSECODE = '".$selectdata."' ";
        } else {
            $query .= " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     GRADE AS ITEM ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     GRADE = '".$model->field["GRADE"]."' ";
        }
        $query .= " )" ;

        $query .= " SELECT ";
        $query .= "     '".$f_score."' AS FSCORE, ";
        $query .= "     S1.ITEM, ";
        $query .= "     COUNT(S1.SCHREGNO) AS STUDENT ";
        $query .= " FROM ";
        $query .= "     MOCK_RANK_DAT T1, ";
        $query .= "     STUDENT S1";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.MOCKCD = '".$model->field["MOCKCD"]."' AND ";
        $query .= "     T1.MOCK_SUBCLASS_CD = '".$subclasscd."'  AND ";
        $query .= "     T1.MOCKDIV = '1' AND ";
        $query .= "     T1.SCHREGNO = S1.SCHREGNO ";
        if($total == ""){
//            if($f_score == '0'){
//                $query .= "     AND T1.SCORE = ".$f_score." ";
//            } else {
                $query .= "     AND T1.SCORE >= ".$f_score." ";
                $query .= "     AND T1.SCORE <= ".$t_score." ";
//            }
        }
        $query .= " GROUP BY ";
        $query .= "     S1.ITEM ";

        return $query;
    }
}
?>
