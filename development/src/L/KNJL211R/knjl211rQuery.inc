<?php

require_once('for_php7.php');

class knjl211rQuery extends Query 
{

    //名称マスタよりデータを取得
    function get_name_cd($model, $year, $cd, $nameField = "NAME1")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || {$nameField} AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($cd == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分マスタ
    function getCourseMst($ObjYear)
    {
        $query .= " SELECT ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     MAX(EXAMCOURSE_NAME) AS EXAMCOURSE_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " GROUP BY ";
        $query .= "     EXAMCOURSECD ";
        $query .= " ORDER BY ";
        $query .= "     EXAMCOURSECD ";
        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.BEFORE_PAGE, ";
        $query .= "     T1.BEFORE_SEQ, ";
        $query .= "     T1.BEFORE_COURSECD || '-' || T1.BEFORE_MAJORCD || '-' || T1.BEFORE_EXAMCOURSECD AS EXAMCOURSECD, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.NAISIN1, ";
        $query .= "     T1.NAISIN2, ";
        $query .= "     T1.NAISIN3, ";
        $query .= "     T1.ATTEND1, ";
        $query .= "     T1.ATTEND2, ";
        $query .= "     T1.ATTEND3, ";
        $query .= "     T1.ATTEND_TOTAL, ";
        $query .= "     T1.SENBATU1_SCHOOL, ";
        $query .= "     T1.SENBATU1_MAJOR, ";
        $query .= "     T1.SENBATU2_SCHOOL, ";
        $query .= "     T1.SENBATU2_MAJOR, ";
        $query .= "     T1.SCHOLARSHIP, ";
        $query .= "     T1.RECOM_FLG, ";
        $query .= "     T1.RECOM_REMARK, ";
        $query .= "     T1.REMARK, ";
        $query .= "     T1.NANKAN_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT T1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.NAMECD1 = 'L004' ";
        $query .= "                            AND L1.NAMECD2 = T1.TESTDIV ";
        $query .= "                            AND L1.YEAR    = T1.ENTEXAMYEAR ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR   = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV       = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND T1.BEFORE_PAGE   = '".$model->field["BEFORE_PAGE"]."' ";
        $query .= "     AND T1.BEFORE_SEQ    = '".$model->field["BEFORE_SEQ"]."' ";

        return $query;
    }

    //受験番号取得
    function getExamno($model, $Row)
    {
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT L1 ON L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                            AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                            AND L1.EXAMNO       = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         L1.ENTEXAMYEAR   = '".$model->year."' ";
        $query .= "     AND L1.APPLICANTDIV  = '".$Row["APPLICANTDIV"]."' ";
        $query .= "     AND L1.TESTDIV       = '".$Row["TESTDIV"]."' ";
        $query .= "     AND T1.SEQ           = '002' ";
        $query .= "     AND T1.REMARK1       = '".$Row["BEFORE_PAGE"]."' ";
        $query .= "     AND T1.REMARK2       = '".$Row["BEFORE_SEQ"]."' ";

        return $query;
    }


    //内諾取得
    function getNaidaku($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSE_MARK AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR   = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV       = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //事前データ取得
    function getEntexamApplicantBeforeDat($model, $year, $applicantdiv, $testdiv, $jizen_page, $jizen_seq)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     L1.ABBV3 AS JIZEN_TEST_CD, ";
        $query .= "     L2.EXAMCOURSE_MARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.NAMECD1 = 'L004' ";
        $query .= "                            AND L1.NAMECD2 = T1.TESTDIV ";
        $query .= "                            AND L1.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L2 ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                    AND L2.TESTDIV = T1.TESTDIV ";
        $query .= "                                    AND L2.COURSECD = T1.BEFORE_COURSECD ";
        $query .= "                                    AND L2.MAJORCD = T1.BEFORE_MAJORCD ";
        $query .= "                                    AND L2.EXAMCOURSECD = T1.BEFORE_EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR   = '".$year."' ";
        $query .= " AND T1.APPLICANTDIV  = '".$applicantdiv."' ";
        $query .= " AND T1.TESTDIV       = '".$testdiv."' ";
        $query .= " AND T1.BEFORE_PAGE   = '".$jizen_page."' ";
        $query .= " AND T1.BEFORE_SEQ    = '".$jizen_seq."' ";

        return $query;
    }

    //出身学校マスタチェック
    function checkSchool($cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($courseCd, $major, $courseCode) = preg_split("/-/", $model->field["EXAMCOURSECD"]);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $model->year;
        $data["APPLICANTDIV"][TEXT]         = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]              = $model->field["TESTDIV"];
        $data["BEFORE_COURSECD"][TEXT]      = $courseCd;
        $data["BEFORE_MAJORCD"][TEXT]       = $major;
        $data["BEFORE_EXAMCOURSECD"][TEXT]  = $courseCode;
        $data["FS_CD"][TEXT]                = (strlen($model->field["FS_CD"]) ? sprintf("%07d", $model->field["FS_CD"]) : "");
        $data["NAME"][TEXT]                 = $model->field["NAME"];
        //半角を全角変換
        $model->field["NAME_KANA"] = mb_convert_kana($model->field["NAME_KANA"], "K", "UTF-8");
        $data["NAME_KANA"][TEXT]            = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]                  = $model->field["SEX"];
        $data["NAISIN1"][NUMBER]            = $model->field["NAISIN1"];
        $data["NAISIN2"][NUMBER]            = $model->field["NAISIN2"];
        $data["NAISIN3"][NUMBER]            = $model->field["NAISIN3"];
        $data["ATTEND1"][NUMBER]            = $model->field["ATTEND1"];
        $data["ATTEND2"][NUMBER]            = $model->field["ATTEND2"];
        $data["ATTEND3"][NUMBER]            = $model->field["ATTEND3"];
        $data["ATTEND_TOTAL"][NUMBER]       = $model->field["ATTEND_TOTAL"];
        $data["SENBATU1_SCHOOL"][TEXT]      = $model->field["SENBATU1_SCHOOL"];
        $data["SENBATU1_MAJOR"][TEXT]       = $model->field["SENBATU1_MAJOR"];
        $data["SENBATU2_SCHOOL"][TEXT]      = $model->field["SENBATU2_SCHOOL"];
        $data["SENBATU2_MAJOR"][TEXT]       = $model->field["SENBATU2_MAJOR"];
        $data["SCHOLARSHIP"][TEXT]          = $model->field["SCHOLARSHIP"];
        $data["RECOM_FLG"][TEXT]            = $model->field["RECOM_FLG"];
        $data["RECOM_REMARK"][TEXT]         = $model->field["RECOM_REMARK"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["NANKAN_FLG"][TEXT]           = $model->field["NANKAN_FLG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR       = '".$model->year."' ";
        $where .= "     AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "     AND TESTDIV       = '".$model->field["TESTDIV"]."' ";
        $where .= "     AND BEFORE_PAGE   = '".$model->field["BEFORE_PAGE"]."' ";
        $where .= "     AND BEFORE_SEQ    = '".$model->field["BEFORE_SEQ"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANT_BEFORE_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANT_BEFORE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANT_BEFORE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR       = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV       = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND BEFORE_PAGE   = '".$model->field["BEFORE_PAGE"]."' ";
        $query .= "     AND BEFORE_SEQ    = '".$model->field["BEFORE_SEQ"]."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $data["REMARK1"][TEXT]          = "";
        $data["REMARK2"][TEXT]          = "";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR      = '".$model->year."' ";
        $where .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "     AND SEQ          = '002'";
        $where .= "     AND REMARK1      = '".$model->field["BEFORE_PAGE"]."'";
        $where .= "     AND REMARK2      = '".$model->field["BEFORE_SEQ"]."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>