<?php

require_once('for_php7.php');

class knjl080bQuery extends Query {

    //名称
    function GetName($namecd1, $ObjYear, $namecd2 = "")
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
        if ($namecd2) {
            $query .= "     AND namecd2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     namecd2 ";
        return $query;
    }

    //単願
    function GetTangan($model)
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
        $query .= "     AND CHANGE_SINGLE_TEST_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     JUDGMENT_DIV ";
        return $query;
    }

    //対象者・合格者
    function GetLeftList($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     R_DTL.RECEPTNO, ";
        $query .= "     case when R_DAT.PROCEDUREDIV1 = '1' then '1' end as DIV, ";
        $query .= "     case when R_DAT.PROCEDUREDIV1 = '1' then R_DAT.PROCEDUREDATE1 end as PRODATE, ";
        $query .= "     CASE WHEN R_DAT.PROCEDUREDIV1 = '1' AND R_DAT.PROCEDUREDATE1 IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '2' ";
        $query .= "     END AS LEFT_FLG, ";
        $query .= "     JUDG.JUDGMENT_DIV, ";
        $query .= "     JUDG.JUDGMENT_COURSE_ABBV, ";
        $query .= "     APP_B.SUB_ORDER, ";
        $query .= "     APP_B.PROCEDUREDIV, ";
        $query .= "     APP_B.NAME ";
        $query .= "   FROM  ENTEXAM_RECEPT_DETAIL_DAT R_DTL ";
        $query .= "         INNER JOIN ENTEXAM_RECEPT_DAT R_DAT ON R_DTL.ENTEXAMYEAR = R_DAT.ENTEXAMYEAR ";
        $query .= "               AND R_DTL.APPLICANTDIV = R_DAT.APPLICANTDIV ";
        $query .= "               AND R_DTL.TESTDIV = R_DAT.TESTDIV ";
        $query .= "               AND R_DTL.RECEPTNO = R_DAT.RECEPTNO ";
        $query .= "         INNER JOIN ENTEXAM_COURSE_JUDGMENT_MST JUDG ON R_DTL.ENTEXAMYEAR = JUDG.ENTEXAMYEAR ";
        $query .= "               AND JUDG.CHANGE_SINGLE_TEST_FLG = '1' ";
        $query .= "               AND  R_DTL.REMARK1 || R_DTL.REMARK2 || R_DTL.REMARK3 = JUDG.EARLY_PASSCOURSECD || JUDG.EARLY_PASSMAJORCD || JUDG.EARLY_PASSEXAMCOURSECD, ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT APP_B ";
        $query .= "  WHERE ";
        $query .= "     R_DTL.ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $query .= "     AND  R_DTL.APPLICANTDIV  = '".$model->applicantdiv."' ";
        $query .= "     AND  R_DTL.TESTDIV       = '".$model->testdiv."' ";
        $query .= "     AND  R_DTL.SEQ           = '002' ";
        $query .= "     AND  R_DTL.REMARK4       = '1' ";
        $query .= "     AND  R_DTL.ENTEXAMYEAR   = APP_B.ENTEXAMYEAR ";
        $query .= "     AND  R_DAT.EXAMNO        = APP_B.EXAMNO ";
        $query .= "     AND  value(APP_B.PROCEDUREDIV,'0') != '2' ";
        $query .= "     AND  value(APP_B.ENTDIV,'0')       != '2' ";
        $query .= " ORDER BY ";
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
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
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
        $query .= "     AND L1.PROCEDUREDIV1 = '1' ";
        return $query;
    }

    //入学コース取得
    function getEnterCourse($model, $tangan)
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
        $query .= " AND COURSECD     = '".$tangan["EARLY_PASSCOURSECD"]."' ";
        $query .= " AND MAJORCD      = '".$tangan["EARLY_PASSMAJORCD"]."' ";
        $query .= " AND EXAMCOURSECD = '".$tangan["EARLY_PASSEXAMCOURSECD"]."' ";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl080bQuery::GetOrder($model);
        $maxOrder = $db->getOne($query);

        foreach ($array as $select) {
            list($receptno, $proDiv, $changeSingleTestFlg, $order) = preg_split("/:/", $select);
            $tangan = $model->tanganArray[$changeSingleTestFlg];
            if ($proDiv == "1") {
                continue;
            }
            $data = array();
            $data["JUDGEDIV"][TEXT]         = "1";
            $data["PROCEDUREDIV1"][TEXT]    = "1";
            $data["PROCEDUREDATE1"][DATE]   = $model->pro_date;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR               = '".$model->ObjYear."'";
            $where .= "   AND APPLICANTDIV              = '".$model->applicantdiv."'"; 
            $where .= "   AND TESTDIV                   = '".$model->testdiv."'"; 
            $where .= "   AND RECEPTNO                    = '".$receptno."'"; 
            $where .= "   AND VALUE(PROCEDUREDIV1,'0') != '1' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            $data = array();
            //単願手続
            $data["SUC_COURSECD"][TEXT]     = $tangan["EARLY_PASSCOURSECD"];
            $data["SUC_MAJORCD"][TEXT]      = $tangan["EARLY_PASSMAJORCD"];
            $data["SUC_COURSECODE"][TEXT]   = $tangan["EARLY_PASSEXAMCOURSECD"];
            $data["JUDGEMENT"][TEXT]        = "1";
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
            $where .= "   AND EXAMNO                    = '".$receptno."'"; 
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
            
            //入学コースをセット
            $query = knjl080bQuery::getEnterCourse($model, $tangan);
            $getEnter = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
            $query .= "   AND EXAMNO       = '".$receptno."'"; 
            $query .= "   AND SEQ          = '007'";
            $db->query($query);
            
            $data = array();
            $data["ENTEXAMYEAR"][TEXT] = $model->ObjYear;
            $data["EXAMNO"][TEXT]      = $receptno;
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
