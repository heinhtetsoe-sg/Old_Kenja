<?php

require_once('for_php7.php');
class knjx_e061studyrecQuery extends Query
{

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年一覧
    public function getSelectFieldSQL1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }

    //生徒一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.ATTENDNO || '番 ' || T2.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER  = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //SCHREG_STUDYREC_DAT
            knjx_e061studyrecQuery::getDeleteQueryStudyrec($db, $data_arr, $i, $model);
            knjx_e061studyrecQuery::getInsertQueryStudyrec($db, $data_arr, $i, $model);
            //STUDYRECREMARK_DAT
            knjx_e061studyrecQuery::getDeleteQueryRemark($db, $data_arr, $i, $model);
            knjx_e061studyrecQuery::getInsertQueryRemark($db, $data_arr, $i, $model);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //SCHREG_STUDYREC_DAT
            knjx_e061studyrecQuery::getDeleteQueryStudyrec($db, $data_arr, $i, $model);
            //STUDYRECREMARK_DAT
            knjx_e061studyrecQuery::getDeleteQueryRemark($db, $data_arr, $i, $model);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //科目数の取得
    public function getSubclasses($model, $classcd)
    {
        $query  = " SELECT SUBCLASSES ";
        $query .= "   FROM CLASS_MST ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND  = '{$classcd}'";
        } else {
            $query .= "  WHERE CLASSCD = '{$classcd}'";
        }

        return $query;
    }

    //追加SQL
    public function getInsertQueryStudyrec($db, $data_arr, $i, $model)
    {
        $data = array();
        $classcd = ($model->Properties["useCurriculumcd"] == '1') ? $data_arr[$i]["CLASSCD"].'-'.$data_arr[$i]["SCHOOL_KIND"] : $data_arr[$i]["CLASSCD"];

        $data["SCHOOLCD"][TEXT]         = $data_arr[$i]["SCHOOLCD"];
        $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
        $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
        $data["ANNUAL"][TEXT]           = $data_arr[$i]["ANNUAL"];
        $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
        $data["CLASSNAME"][TEXT]        = $data_arr[$i]["CLASSNAME"];
        $data["CLASSABBV"][TEXT]        = $data_arr[$i]["CLASSABBV"];
        $data["CLASSNAME_ENG"][TEXT]    = $data_arr[$i]["CLASSNAME_ENG"];
        $data["CLASSABBV_ENG"][TEXT]    = $data_arr[$i]["CLASSABBV_ENG"];
        $data["SUBCLASSES"][NUMBER]     = $db->getOne(knjx_e061studyrecQuery::getSubclasses($model, $classcd));
        $data["SUBCLASSNAME"][TEXT]     = $data_arr[$i]["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT]     = $data_arr[$i]["SUBCLASSABBV"];
        $data["SUBCLASSNAME_ENG"][TEXT] = $data_arr[$i]["SUBCLASSNAME_ENG"];
        $data["SUBCLASSABBV_ENG"][TEXT] = $data_arr[$i]["SUBCLASSABBV_ENG"];
        $data["VALUATION"][NUMBER]      = $data_arr[$i]["VALUATION"];
        $data["GET_CREDIT"][NUMBER]     = $data_arr[$i]["GET_CREDIT"];
        $data["ADD_CREDIT"][NUMBER]     = $data_arr[$i]["ADD_CREDIT"];
        $data["COMP_CREDIT"][NUMBER]    = $data_arr[$i]["COMP_CREDIT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHREG_STUDYREC_DAT");
        $db->query($query);
    }

    //追加SQL
    public function getInsertQueryRemark($db, $data_arr, $i, $model)
    {
        $data = array();

        $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
        $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
        $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
        $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "STUDYRECREMARK_DAT");
        $db->query($query);
    }

    //削除SQL
    public function getDeleteQueryStudyrec($db, $data_arr, $i, $model)
    {
        $query  = " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".$data_arr[$i]["SCHOOLCD"]."' ";
        $query .= "     AND YEAR        = '".$data_arr[$i]["YEAR"]."' ";
        $query .= "     AND SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
        $query .= "     AND CLASSCD     = '".$data_arr[$i]["CLASSCD"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
            $query .= "     AND CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
        }
        $query .= "     AND SUBCLASSCD  = '".$data_arr[$i]["SUBCLASSCD"]."' ";
        $db->query($query);
    }

    //削除SQL
    public function getDeleteQueryRemark($db, $data_arr, $i, $model)
    {
        $query  = " DELETE FROM STUDYRECREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".$data_arr[$i]["YEAR"]."' ";
        $query .= "     AND SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
        $query .= "     AND CLASSCD     = '".$data_arr[$i]["CLASSCD"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
            $query .= "     AND CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
        }
        $query .= "     AND SUBCLASSCD  = '".$data_arr[$i]["SUBCLASSCD"]."' ";
        $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = 'KNJX_E061M';
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T3.SCHOOLCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.SEX, ";
        $query .= "     N1.NAME1 AS SEX_NAME, ";
        $query .= "     T3.YEAR, ";
        $query .= "     T3.ANNUAL, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     C1.COURSECODENAME, ";
        $query .= "     T3.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.CLASSNAME, ";
        $query .= "     T3.CLASSABBV, ";
        $query .= "     T3.CLASSNAME_ENG, ";
        $query .= "     T3.CLASSABBV_ENG, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T3.SUBCLASSABBV, ";
        $query .= "     T3.SUBCLASSNAME_ENG, ";
        $query .= "     T3.SUBCLASSABBV_ENG, ";
        $query .= "     T3.VALUATION, ";
        $query .= "     T3.GET_CREDIT, ";
        $query .= "     T3.ADD_CREDIT, ";
        $query .= "     T3.COMP_CREDIT, ";
        $query .= "     T4.REMARK, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_STUDYREC_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                                      AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN STUDYRECREMARK_DAT T4 ON T4.YEAR = T3.YEAR ";
        $query .= "                                    AND T4.SCHREGNO = T3.SCHREGNO ";
        $query .= "                                    AND T4.CLASSCD = T3.CLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                    AND T4.SCHOOL_KIND   = T3.SCHOOL_KIND ";
            $query .= "                                    AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "                                    AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T2.SEX ";
        $query .= "     LEFT JOIN COURSECODE_MST C1 ON C1.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["STUDENT"] != "") {
            $query .= "     AND T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //教育課程コード取得
    public function getCurriculumCd()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z018' ";

        return $query;
    }
}
