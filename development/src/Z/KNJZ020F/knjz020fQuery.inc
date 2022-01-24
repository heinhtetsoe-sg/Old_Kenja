<?php

require_once('for_php7.php');

class knjz020fQuery extends Query
{
    //入試年度一覧取得
    public function selectYearQuery()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    public function deleteAtExist($model)
    {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '".$model->year."' AND ";
        $query .= "     TESTDIV      = '1' ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($model, $namecd1, $default = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        if ($default) {
            $query .= "    ,NAMESPARE2 AS DEFAULT ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        if ($namecd1 == "L024" || $namecd1 == "L004") {
            $query .= "     int(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //名称マスタよりNAMESPARE2取得(初期値を取得できていない場合)
    public function getNameSpare2($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     NAMECD1     = '".$namecd1."' AND ";
        $query .= "     NAMESPARE2  = '1' ";

        return $query;
    }

    //名称マスタより取得(初期値を取得できていない場合)
    public function getNameMstSet($model, $namecd1, $namespare2)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namespare2) {
            $query .= " AND NAMESPARE2 = '".$namespare2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //データ取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS TOTALCD, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || L2.COURSENAME || L2.MAJORNAME AS TOTALCD_NAME, ";
        $query .= "     T1.EXAMCOURSECD || ':' || VALUE(L1.EXAMCOURSE_NAME,'') AS EXAMCOURSE_NAME, ";
        if ($model->applicantdiv == "1") {
            $query .= "     L3.NAME1 AS TESTSUBCLASSNAME ";
        } else {
            $query .= "     L3.NAME2 AS TESTSUBCLASSNAME ";
        }
        if ($model->applicantdiv == "1") {
            $query .= "    ,L4.NAME1 AS EXAM_TYPE_NAME ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ON ";
        $query .= "         L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND ";
        $query .= "         L1.APPLICANTDIV = T1.APPLICANTDIV AND ";
        $query .= "         L1.TESTDIV      = '1' AND ";
        $query .= "         L1.COURSECD     = T1.COURSECD AND ";
        $query .= "         L1.MAJORCD      = T1.MAJORCD AND ";
        $query .= "         L1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L2 ON ";
        $query .= "         L2.YEAR         = T1.ENTEXAMYEAR AND ";
        $query .= "         L2.COURSECD     = T1.COURSECD AND ";
        $query .= "         L2.MAJORCD      = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_NAME_MST L3 ON ";
        $query .= "         L3.YEAR         = T1.ENTEXAMYEAR AND ";
        $query .= "         L3.NAMECD1      = 'L009' AND ";
        $query .= "         L3.NAMECD2      = T1.TESTSUBCLASSCD AND ";
        if ($model->applicantdiv == "1") {
            $query .= "         L3.NAME1 IS NOT NULL ";
        } else {
            $query .= "         L3.NAME2 IS NOT NULL ";
        }
        $query .= "     LEFT JOIN V_NAME_MST L4 ON ";
        $query .= "         L4.YEAR         = T1.ENTEXAMYEAR AND ";
        $query .= "         L4.NAMECD1      = 'L005' AND ";
        $query .= "         L4.NAMECD2      = T1.EXAM_TYPE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->testdiv."' ";
        if ($model->applicantdiv == "2") {
            $query .= " AND T1.EXAM_TYPE    = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD, ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T1.EXAM_TYPE ";

        return $query;
    }

    //課程学科・受験コース件数取得
    public function getDataCnt($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS TOTALCD, ";
        if ($flg == "examcoursecd") {
            $query .= "     EXAMCOURSECD, ";
        }
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV      = '".$model->testdiv."' ";
        if ($model->applicantdiv == "2") {
            $query .= " AND EXAM_TYPE    = '1' ";
        }
        $query .= " GROUP BY ";
        $query .= "     COURSECD || MAJORCD ";
        if ($flg == "examcoursecd") {
            $query .= "    ,EXAMCOURSECD ";
        }

        return $query;
    }

    //１レコード取得
    public function getRow($year, $applicantdiv, $testdiv, $totalcd, $examcoursecd, $testsubclasscd, $exam_type)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     COURSECD || MAJORCD AS TOTALCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$year."' AND ";
        $query .= "     APPLICANTDIV    = '".$applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$testdiv."' AND ";
        $query .= "     COURSECD || MAJORCD = '".$totalcd."' AND ";
        $query .= "     EXAMCOURSECD    = '".$examcoursecd."' AND ";
        $query .= "     TESTSUBCLASSCD  = '".$testsubclasscd."' AND ";
        $query .= "     EXAM_TYPE       = '".$exam_type."' ";

        return $query;
    }

    //課程学科取得
    public function getTotalcd($model)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSECD || MAJORCD || ':' || COURSENAME || MAJORNAME AS LABEL, ";
        $query .= "     '1' AS DEFAULT ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験コース取得
    public function getEntExamCourse($model, $totalcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '1' AND ";
        $query .= "     COURSECD || MAJORCD = '{$totalcd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験科目取得
    public function getTestSubclass($model)
    {
        $name = ($model->applicantdiv == "1") ? "NAME1" : "NAME2";

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .=       $name." AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'L009' AND ";
        $query .=       $name." IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($fields)
    {
        $coursecd = substr($fields["TOTALCD"], 0, 1);
        $majorcd  = substr($fields["TOTALCD"], 1, 3);

        $db = Query::dbCheckOut();

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]              = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]             = $coursecd;
        $data["MAJORCD"][TEXT]              = $majorcd;
        $data["EXAMCOURSECD"][TEXT]         = $fields["EXAMCOURSECD"];
        $data["TESTSUBCLASSCD"][TEXT]       = $fields["TESTSUBCLASSCD"];
        $data["EXAM_TYPE"][TEXT]            = $fields["EXAM_TYPE"];
        $data["PERFECT"][NUMBER]            = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_PERFECT_EXAMTYPE_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($fields)
    {
        $coursecd = substr($fields["TOTALCD"], 0, 1);
        $majorcd  = substr($fields["TOTALCD"], 1, 3);

        $db = Query::dbCheckOut();

        $data = array();
        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]              = $fields["TESTDIV"];
        $data["COURSECD"][TEXT]             = $coursecd;
        $data["MAJORCD"][TEXT]              = $majorcd;
        $data["EXAMCOURSECD"][TEXT]         = $fields["EXAMCOURSECD"];
        $data["TESTSUBCLASSCD"][TEXT]       = $fields["TESTSUBCLASSCD"];
        $data["EXAM_TYPE"][TEXT]            = $fields["EXAM_TYPE"];
        $data["PERFECT"][NUMBER]            = $fields["PERFECT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."' ";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."' ";
        $where .= "   AND TESTDIV           = '".$fields["TESTDIV"]."' ";
        $where .= "   AND COURSECD || MAJORCD = '".$fields["TOTALCD"]."' ";
        $where .= "   AND EXAMCOURSECD      = '".$fields["EXAMCOURSECD"]."' ";
        $where .= "   AND TESTSUBCLASSCD    = '".$fields["TESTSUBCLASSCD"]."' ";
        $where .= "   AND EXAM_TYPE         = '".$fields["EXAM_TYPE"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_PERFECT_EXAMTYPE_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."' ";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."' ";
        $query .= "         AND TESTDIV         = '".$fields["TESTDIV"]."' ";
        $query .= "         AND COURSECD || MAJORCD = '".$fields["TOTALCD"]."' ";
        $query .= "         AND EXAMCOURSECD    = '".$fields["EXAMCOURSECD"]."' ";
        $query .= "         AND TESTSUBCLASSCD  = '".$fields["TESTSUBCLASSCD"]."' ";
        $query .= "         AND EXAM_TYPE       = '".$fields["EXAM_TYPE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //ある年度にデータが存在するのかチェック
    public function getCheckPerfectMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";

        return $query;
    }

    //COPY
    public function getCopyQuery($thisYear, $nextYear)
    {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     PERFECT, ";
        $query .= "     RATE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_PERFECT_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";

        return $query;
    }
}
