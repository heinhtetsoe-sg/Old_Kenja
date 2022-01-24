<?php

require_once('for_php7.php');

class knjl083rQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV3, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //手続者・合格者取得
    function getLeftRightList($model) {
        $query  = " SELECT ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     CASE WHEN R1.PROCEDUREDIV1 = '1' THEN R1.PROCEDUREDATE1 END AS PRO_DATE, ";
        $query .= "     CASE WHEN R1.PROCEDUREDIV1 = '1' THEN '1' ELSE '0' END AS LEFT_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "       ON T1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "      AND T1.APPLICANTDIV = R1.APPLICANTDIV";
        $query .= "      AND T1.EXAMNO       = R1.EXAMNO     ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR         = R1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1      = 'L013' ";
        $query .= "         AND N1.NAMECD2      = R1.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR              = '".$model->ObjYear."' AND ";
        $query .= "     R1.APPLICANTDIV             = '".$model->applicantdiv."' AND ";
        $query .= "     R1.TESTDIV                  = '".$model->testdiv."' AND ";
        //合格者が対象
        $query .= "     N1.NAMESPARE1               = '1' AND ";
        //辞退者は除く
        $query .= "     VALUE(T1.PROCEDUREDIV,'')  != '2' AND ";    //手続区分 1:済み 2:無し
        $query .= "     VALUE(T1.ENTDIV,'')        != '2' ";        //入学区分 1:済み 2:辞退
        $query .= " ORDER BY ";
        $query .= "     R1.RECEPTNO ";

        return $query;
    }

    //入学コース取得
    function getEnterCourse($model) {
        $query  = " SELECT ";
        $query .= "     ENTER_COURSECD, ";
        $query .= "     ENTER_MAJORCD, ";
        $query .= "     ENTER_COURSECODE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV      = '".$model->testdiv."' ";

        return $query;
    }

    //他入試区分の手続年月日を取得
    function getProcedureOtherDate($model, $examno) {
        $query = " SELECT ";
        $query .= "     PROCEDUREDATE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV != '{$model->testdiv}' AND ";
        $query .= "     EXAMNO = '{$examno}' ";
    
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //手続者リスト
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //合格者リスト
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());

        //入学コースを取得
        $getEnter = array();
        $getEnter = $db->getRow(knjl083rQuery::getEnterCourse($model), DB_FETCHMODE_ASSOC);

        /************************/
        /*  手続者（左リスト）  */
        /************************/
        foreach ($array as $valwk) {
            $receptno_wk = array();
            $receptno_wk = explode("-", $valwk);
            $receptno = $receptno_wk[0];
            $examno = $receptno_wk[1];
            //手続き日付を更新
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
            $where .= "   AND RECEPTNO      = '{$receptno}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $baseRow = $db->getRow(" SELECT * FROM ENTEXAM_RECEPT_DAT " .$where, DB_FETCHMODE_ASSOC);
            $data = array();
            $data["PROCEDUREDIV1"][TEXT]     = "1";
            if ($baseRow["PROCEDUREDATE1"] == "") {
                $data["PROCEDUREDATE1"][DATE]   = $model->pro_date;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //手続区分・日付、入学区分をセット
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $baseRow = $db->getRow(" SELECT * FROM ENTEXAM_APPLICANTBASE_DAT " .$where, DB_FETCHMODE_ASSOC);
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = "1";      //手続区分 1:済み 2:無し
            if ($baseRow["PROCEDUREDATE"] < str_replace("/", "-", $model->pro_date)) {
                $data["PROCEDUREDATE"][DATE]    = $model->pro_date;
            }
            $data["ENTDIV"][TEXT]           = "1";      //入学区分 1:済み 2:辞退
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //入学コースをクリア
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' "; 
            $query .= "   AND EXAMNO        = '{$examno}' "; 
            $query .= "   AND SEQ           = '007' ";
            $db->query($query);
            //入学コースをセット
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SEQ"][TEXT]              = '007';
            $data["REMARK1"][TEXT]          = $getEnter["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]          = $getEnter["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]          = $getEnter["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        /************************/
        /*  合格者（右リスト）  */
        /************************/
        foreach ($array2 as $valwk) {
            $receptno_wk = array();
            $receptno_wk = explode("-", $valwk);
            $receptno = $receptno_wk[0];
            $examno = $receptno_wk[1];
            //更新日付をクリア
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV       = '{$model->testdiv}' ";
            $where .= "   AND RECEPTNO      = '{$receptno}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $data = array();
            $data["PROCEDUREDIV1"][TEXT]     = "";
            $data["PROCEDUREDATE1"][DATE]    = "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //右リストの更新SQL(クリア処理)で、他試験区分で手続済がある人は(ENTEXAM_RECEPT_DAT.PROCEDUREDIV1=1)、下記テーブルのクリア処理をしない
            //手続区分・日付、入学区分(ENTEXAM_APPLICANTBASE_DAT.PROCEDUREDIV,PROCEDUREDATE,ENTDIV)
            //入学コース(ENTEXAM_APPLICANTBASE_DETAIL_DAT.SEQ=007)
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV      != '{$model->testdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $where .= "   AND PROCEDUREDIV1 = '1' ";
            $proCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_RECEPT_DAT " .$where);
            if ($proCnt > 0) continue;

            //手続区分・日付、入学区分をクリア
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = "";       //手続区分 1:済み 2:無し
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $baseRow = $db->getRow(" SELECT * FROM ENTEXAM_APPLICANTBASE_DAT " .$where, DB_FETCHMODE_ASSOC);
            if ($baseRow["PROCEDUREDATE"] != "") {
                $otherProcedureDate = $db->getOne(knjl083rQuery::getProcedureOtherDate($model, $examno));
                if ($otherProcedureDate != $baseRow["PROCEDUREDATE"]) {
                    //他試験区分で登録されたPROCEDUREDATE1で登録
                    $data["PROCEDUREDATE"][DATE] = str_replace("-", "/", $otherProcedureDate);
                }
            }
            $data["ENTDIV"][TEXT]           = "";       //入学区分 1:済み 2:辞退
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //入学コースをクリア
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' "; 
            $query .= "   AND EXAMNO        = '{$examno}' "; 
            $query .= "   AND SEQ           = '007' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
