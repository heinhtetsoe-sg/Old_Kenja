<?php

require_once('for_php7.php');

class knjl081bQuery extends Query {

    //名称
    function GetName($namecd1, $ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     name2, ";
        $query .= "     namespare1, ";
        $query .= "     namespare2, ";
        $query .= "     ABBV3, ";
        $query .= "     NAMESPARE3, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$ObjYear."' ";
        $query .= "     AND namecd1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     namecd2 ";
        return $query;
    }

    //正規合否区分
    function GetCourseJudgment($model)
    {
        $query  = " SELECT ";
        $query .= "     JUDGMENT_DIV, ";
        $query .= "     JUDGMENT_COURSE_NAME, ";
        $query .= "     NORMAL_PASSCOURSECD, ";
        $query .= "     NORMAL_PASSMAJORCD, ";
        $query .= "     NORMAL_PASSEXAMCOURSECD, ";
        $query .= "     EARLY_PASSCOURSECD, ";
        $query .= "     EARLY_PASSMAJORCD, ";
        $query .= "     EARLY_PASSEXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        if ($model->testdiv == "1") {
            $query .= "     AND VALUE(TAKE_RECOMMEND_TEST_FLG, '0') = '1' ";
        } else {
            $query .= "     AND VALUE(TAKE_GENERAL_TEST_FLG, '0') = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     JUDGMENT_DIV ";
        return $query;
    }

    //対象者・合格者
    function GetLeftList($model)
    {
        $query  = "";
        $query .= " SELECT  APP_B.EXAMNO, ";
        $query .= "     CASE WHEN APP_B.PROCEDUREDIV = '1' then APP_B.PROCEDUREDATE end as PRODATE, ";
        $query .= "     CASE WHEN APP_B.PROCEDUREDIV = '1' AND APP_B.PROCEDUREDATE IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '2' ";
        $query .= "     END AS LEFT_FLG, ";
        $query .= "     JUDG.JUDGMENT_DIV, ";
        $query .= "     CASE WHEN APP_B.PROCEDUREDIV = '1' AND APP_B.PROCEDUREDATE IS NOT NULL THEN JUDG.JUDGMENT_COURSE_ABBV ";
        $query .= "          WHEN JUDG_TANGAN.JUDGMENT_COURSE_ABBV IS NOT NULL THEN JUDG_TANGAN.JUDGMENT_COURSE_ABBV ";
        $query .= "          WHEN JUDG_SEIKI.JUDGMENT_COURSE_ABBV IS NOT NULL AND JUDG_TANGAN.JUDGMENT_COURSE_ABBV IS NULL THEN JUDG_SEIKI.JUDGMENT_COURSE_ABBV ";
        $query .= "          ELSE JUDG.JUDGMENT_COURSE_ABBV ";
        $query .= "     END AS JUDGMENT_COURSE_ABBV, ";
        $query .= "     CASE WHEN RECE.PROCEDUREDIV1 = '1' THEN 0 ELSE 1 END AS SOUKI_ORDER, ";
        $query .= "     APP_B.SUB_ORDER, ";
        $query .= "     APP_B.PROCEDUREDIV, ";
        //$query .= "     RECE_D002.SEQ AS TANFLG, ";
        $query .= "     APP_B.NAME ";
        $query .= "   FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT APP_B ";
        $query .= "         INNER JOIN ENTEXAM_COURSE_JUDGMENT_MST JUDG ON APP_B.ENTEXAMYEAR = JUDG.ENTEXAMYEAR ";
        if ($model->testdiv == "1") {
            $query .= "           AND  VALUE(JUDG.TAKE_RECOMMEND_TEST_FLG, '0') = '1' ";
            $query .= "           AND  APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = JUDG.NORMAL_PASSCOURSECD || JUDG.NORMAL_PASSMAJORCD || JUDG.NORMAL_PASSEXAMCOURSECD ";
        } else {
            $query .= "           AND (( ";
            $query .= "                VALUE(JUDG.TAKE_GENERAL_TEST_FLG, '0') = '1' AND APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = JUDG.NORMAL_PASSCOURSECD || JUDG.NORMAL_PASSMAJORCD || JUDG.NORMAL_PASSEXAMCOURSECD ";
            $query .= "                ) OR ( ";
            $query .= "                VALUE(JUDG.CHANGE_SINGLE_TEST_FLG, '0') = '1' AND APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = JUDG.EARLY_PASSCOURSECD || JUDG.EARLY_PASSMAJORCD || JUDG.EARLY_PASSEXAMCOURSECD ";
            $query .= "                )) ";
        }
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DAT RECE ON APP_B.ENTEXAMYEAR = RECE.ENTEXAMYEAR ";
        $query .= "               AND  APP_B.APPLICANTDIV = RECE.APPLICANTDIV ";
        $query .= "               AND  APP_B.TESTDIV = RECE.TESTDIV ";
        $query .= "               AND  RECE.EXAM_TYPE = '1' ";
        $query .= "               AND  APP_B.EXAMNO = RECE.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECE_D002 ON RECE.ENTEXAMYEAR = RECE_D002.ENTEXAMYEAR ";
        $query .= "               AND  RECE.APPLICANTDIV = RECE_D002.APPLICANTDIV ";
        $query .= "               AND  RECE.TESTDIV = RECE_D002.TESTDIV ";
        $query .= "               AND  RECE.EXAM_TYPE = RECE_D002.EXAM_TYPE ";
        $query .= "               AND  RECE.RECEPTNO = RECE_D002.RECEPTNO ";
        $query .= "               AND  RECE_D002.SEQ = '002' ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RECE_D001 ON RECE.ENTEXAMYEAR = RECE_D001.ENTEXAMYEAR ";
        $query .= "               AND  RECE.APPLICANTDIV = RECE_D001.APPLICANTDIV ";
        $query .= "               AND  RECE.TESTDIV = RECE_D001.TESTDIV ";
        $query .= "               AND  RECE.EXAM_TYPE = RECE_D001.EXAM_TYPE ";
        $query .= "               AND  RECE.RECEPTNO = RECE_D001.RECEPTNO ";
        $query .= "               AND  RECE_D001.SEQ = '001' ";
        //正規合格コースを取得
        $query .= "         LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST JUDG_SEIKI ON RECE_D001.ENTEXAMYEAR = JUDG_SEIKI.ENTEXAMYEAR ";
        if ($model->testdiv == "1") {
            $query .= "             AND VALUE(JUDG_SEIKI.TAKE_RECOMMEND_TEST_FLG, '0') = '1' ";
        } else {
            $query .= "             AND VALUE(JUDG_SEIKI.TAKE_GENERAL_TEST_FLG, '0') = '1' ";
        }
        $query .= "               AND RECE_D001.REMARK1 || RECE_D001.REMARK2 || RECE_D001.REMARK3 = JUDG_SEIKI.NORMAL_PASSCOURSECD || JUDG_SEIKI.NORMAL_PASSMAJORCD || JUDG_SEIKI.NORMAL_PASSEXAMCOURSECD ";
        //単願切換合格コースを取得
        $query .= "         LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST JUDG_TANGAN ON RECE_D002.ENTEXAMYEAR = JUDG_TANGAN.ENTEXAMYEAR ";
        $query .= "               AND  VALUE(JUDG_TANGAN.CHANGE_SINGLE_TEST_FLG, '0') = '1' ";
        $query .= "               AND  RECE_D002.REMARK1 || RECE_D002.REMARK2 || RECE_D002.REMARK3 = JUDG_TANGAN.EARLY_PASSCOURSECD || JUDG_TANGAN.EARLY_PASSMAJORCD || JUDG_TANGAN.EARLY_PASSEXAMCOURSECD ";
        $query .= "  WHERE ";
        $query .= "     APP_B.ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $query .= "     AND APP_B.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND APP_B.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND APP_B.JUDGEMENT    = '1' ";
        $query .= " ORDER BY ";
        $query .= "     SOUKI_ORDER, ";
        $query .= "     INT(APP_B.SUB_ORDER), ";
        $query .= "     APP_B.PROCEDUREDATE, ";
        $query .= "     APP_B.EXAMNO ";
        return $query;
    }

    //SUB_ORDER取得
    function GetOrder($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(INT(VALUE(T1.SUB_ORDER, '0'))) AS SUB_ORDER ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                    AND L1.TESTDIV = T1.TESTDIV ";
        $query .= "                                    AND L1.EXAM_TYPE = '1' ";
        $query .= "                                    AND L1.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND T1.PROCEDUREDIV = '1' ";
        $query .= "     AND VALUE(L1.PROCEDUREDIV1, '0') != '1' ";
        return $query;
    }

    //入学コース取得
    function getEnterCourse($model, $seiki)
    {
        $query  = " SELECT ";
        $query .= "     ENTER_COURSECD, ";
        $query .= "     ENTER_MAJORCD, ";
        $query .= "     ENTER_COURSECODE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."'";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= " AND TESTDIV      = '".$model->testdiv."'";
        $query .= " AND COURSECD     = '".$seiki["REMARK1"]."' ";
        $query .= " AND MAJORCD      = '".$seiki["REMARK2"]."' ";
        $query .= " AND EXAMCOURSECD = '".$seiki["REMARK3"]."' ";
        return $query;
    }

    //正規合格コースを取得する
    function getSeikiCourse($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND TESTDIV = '".$model->testdiv."' ";
        $query .= " AND EXAM_TYPE = '1' ";
        $query .= " AND RECEPTNO = '".$examno."' ";
        $query .= " AND SEQ = '001' ";
        $query .= " AND REMARK4 = '1' ";
        return $query;
    }
        

    //UPDATE
    function &getUpdateQuery($model)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl081bQuery::GetOrder($model);
        $maxOrder = $db->getOne($query);

        foreach ($array as $select) {
            list($examno, $proDate, $take_testFlg, $order) = preg_split("/:/", $select);
            $judgment = $model->judgmentArray[$take_testFlg];
            if ($proDate != "") {
                continue;
            }
            $data = array();
            //正規合格コースを取得してセット
            $seiki = array();
            $seiki = $db->getRow(knjl081bQuery::getSeikiCourse($model, $examno), DB_FETCHMODE_ASSOC);
            $data["SUC_COURSECD"][TEXT]     = $seiki["REMARK1"];
            $data["SUC_MAJORCD"][TEXT]      = $seiki["REMARK2"];
            $data["SUC_COURSECODE"][TEXT]   = $seiki["REMARK3"];
            //入学
            $data["PROCEDUREDIV"][TEXT]     = "1";
            $data["PROCEDUREDATE"][DATE]    = $model->pro_date;
            $data["ENTDIV"][TEXT]           = "1";
            //ORDERセット
            if ($order == "") {
                $maxOrder++;
                $data["SUB_ORDER"][TEXT]        = $maxOrder;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR               = '".$model->ObjYear."'";
            $where .= "   AND EXAMNO                    = '".$examno."'"; 
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
            
            //入学コースをセット
            $query = knjl081bQuery::getEnterCourse($model, $seiki);
            $getEnter = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
            $query .= "   AND EXAMNO       = '".$examno."'"; 
            $query .= "   AND SEQ          = '007'";
            $db->query($query);
            
            $data = array();
            $data["ENTEXAMYEAR"][TEXT] = $model->ObjYear;
            $data["EXAMNO"][TEXT]      = $examno;
            $data["SEQ"][TEXT]         = '007';
            $data["REMARK1"][TEXT]     = $getEnter["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]     = $getEnter["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]     = $getEnter["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }

}
?>
