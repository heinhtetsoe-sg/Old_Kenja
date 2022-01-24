<?php

require_once('for_php7.php');

class knjl082bQuery extends Query {

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
    function GetLeftList($model, $flg)
    {
        //対象者を取得(左のリストに選択している人）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
    
        $tangan = $model->tanganArray[$model->changeSingleTestFlg];
        $course = $tangan["NORMAL_PASSCOURSECD"].$tangan["NORMAL_PASSMAJORCD"].$tangan["NORMAL_PASSEXAMCOURSECD"];
        $query  = "";
        $query .= " SELECT  APP_B.EXAMNO, ";
        $query .= "     CASE WHEN APP_B.PROCEDUREDIV = '1' then APP_B.PROCEDUREDATE end as PRODATE, ";
        $query .= "     CASE WHEN APP_B.PROCEDUREDIV = '1' AND APP_B.PROCEDUREDATE IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '2' ";
        $query .= "     END AS LEFT_FLG, ";
        $query .= "     APP_B.PROCEDUREDIV, ";
        $query .= "     RECE.PROCEDUREDIV1, ";
        $query .= "     APP_B.NAME ";
        $query .= "   FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT APP_B ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DAT RECE ON APP_B.ENTEXAMYEAR = RECE.ENTEXAMYEAR ";
        $query .= "               AND  APP_B.APPLICANTDIV = RECE.APPLICANTDIV ";
        $query .= "               AND  APP_B.TESTDIV = RECE.TESTDIV ";
        $query .= "               AND  RECE.EXAM_TYPE = '1' ";
        $query .= "               AND  APP_B.EXAMNO = RECE.EXAMNO ";
        $query .= "  WHERE ";
        $query .= "     APP_B.ENTEXAMYEAR       = '".$model->ObjYear."' ";
        $query .= "     AND  APP_B.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND  APP_B.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND  APP_B.JUDGEMENT    = '1' ";
        if ($flg == "right") {
            $query .= "     AND  APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE = '{$course}' ";
            $query .= "     AND ( APP_B.PROCEDUREDATE <> '".str_replace("/","-",$model->pro_date)."' ";
            $query .= "           OR APP_B.PROCEDUREDATE IS NULL ) ";
            foreach ($array as $select) {
                list($examno, $proDate) = preg_split("/:/", $select);
                $query .= "     AND  APP_B.EXAMNO    <> '".$examno."' ";
            }  
        } else {
            $query .= "     AND  APP_B.PROCEDUREDATE = '".str_replace("/","-",$model->pro_date)."' ";
        }
        $query .= " ORDER BY ";
        if ($flg == "right") {
            $query .= "     APP_B.EXAMNO ";
        } else {
            $query .= "     INT(APP_B.SUB_ORDER) ";
        }

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $tangan = $model->tanganArray[$model->changeSingleTestFlg];
        //更新する手続日付の中の受付番号の最大値を取得
        $count = $db->getOne(knjl082bQuery::GetSubOrderList($model, "COUNT"));
        if (!$count) {
            $count = 1;
        } else {
            $count++;
        }
        foreach ($array as $select) {
            list($examno, $proDate) = preg_split("/:/", $select);
            if ($proDate != "") {
                continue;
            }
            $data = array();
            $data["SUB_ORDER"][TEXT]        = $count;
            $data["PROCEDUREDIV"][TEXT]     = "1";
            $data["PROCEDUREDATE"][DATE]    = $model->pro_date;
            $data["ENTDIV"][TEXT]           = "1";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR               = '".$model->ObjYear."'";
            $where .= "   AND EXAMNO                    = '".$examno."'"; 
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
            $count++;
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }

    //番号振りなおし対象者取得
    function GetSubOrderList($model, $flg)
    {
        $query .= " SELECT ";
        if (!$flg) {
            $query .= "     APP_B.EXAMNO ";
        } else {
            $query .= "     MAX(INT(APP_B.SUB_ORDER)) AS MAX_SUB_ORDER ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT APP_B ";
        $query .= "  WHERE ";
        $query .= "          APP_B.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND  APP_B.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND  APP_B.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND  APP_B.JUDGEMENT    = '1' ";
        if (!$flg) {
            $query .= "     AND  APP_B.PROCEDUREDATE IS NOT NULL  ";
            $query .= "     AND  APP_B.SUB_ORDER IS NOT NULL ";
            $query .= " ORDER BY ";
            $query .= "     APP_B.PROCEDUREDATE, ";
            $query .= "     INT(APP_B.SUB_ORDER), ";
            $query .= "     APP_B.EXAMNO ";
        } else {
            $query .= "     AND  APP_B.PROCEDUREDATE = '".str_replace("/","-",$model->pro_date)."' ";
            $query .= "     AND  APP_B.SUB_ORDER IS NOT NULL ";
        }
        return $query;
    }
        
    //UPDATE(受付番号振りなおし)
    function &getUpdateRenbanQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $count = 1;
        $result = $db->query(knjl082bQuery::GetSubOrderList($model, ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["SUB_ORDER"][TEXT]        = $count;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."'";
            $where .= "   AND EXAMNO        = '".$row["EXAMNO"]."'"; 
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
            $count++;
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }

}
?>
