<?php

require_once('for_php7.php');

class knjl083qQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     ABBV3, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //入学者・合格者取得
    function GetLeftList($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     N2.NAME1 AS SHDIV, ";
        $query .= "     CASE WHEN T1.PROCEDUREDIV = '1' THEN T1.PROCEDUREDATE END AS PRO_DATE, ";
        $query .= "     CASE WHEN T1.ENTDIV = '1' THEN '1' ELSE '0' END AS LEFT_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1      = 'L013' ";
        $query .= "         AND N1.NAMECD2      = T1.JUDGEMENT ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ";
        $query .= "          ON N2.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "         AND N2.NAMECD1      = 'L006' ";
        $query .= "         AND N2.NAMECD2      = T1.SHDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR              = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV             = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV                  = '".$model->testdiv."' AND ";
        //合格者が対象
        $query .= "     N1.NAMESPARE1               = '1' AND ";
        //辞退者は除く
        $query .= "     VALUE(T1.PROCEDUREDIV,'')  != '2' AND ";    //手続区分 1:済み 2:無し（辞退）
        $query .= "     VALUE(T1.ENTDIV,'')        != '2' ";        //入学区分 1:済み 2:辞退
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //入学コース取得
    function getEnterCourse($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     L1.ENTER_COURSECD, ";
        $query .= "     L1.ENTER_MAJORCD, ";
        $query .= "     L1.ENTER_COURSECODE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "             ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND L1.COURSECD     = T1.SUC_COURSECD ";
        $query .= "             AND L1.MAJORCD      = T1.SUC_MAJORCD ";
        $query .= "             AND L1.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入学者リスト
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //合格者リスト
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());

        if ($model->testdiv0 == "1") {
            $array_examno = array_merge($array, $array2);

            //データ有無チェック
            $chkData = array();
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
            $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
            $query .= "     SEQ             = '023' AND ";
            $query .= "     EXAMNO IN ('".implode("','", $array_examno)."') ";
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $chkData[$row["EXAMNO"]] = array("datadiv"  => "1",
                                                 "shdiv"    => $row["REMARK5"]);
            }
            $result->free();
        }

        //入学コース取得
        $EntCourse = array();
        $result = $db->query(knjl083qQuery::getEnterCourse($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $EntCourse[$row["EXAMNO"]][]    = $row["ENTER_COURSECD"];
            $EntCourse[$row["EXAMNO"]][]    = $row["ENTER_MAJORCD"];
            $EntCourse[$row["EXAMNO"]][]    = $row["ENTER_COURSECODE"];
        }
        $result->free();

        /************************/
        /*  入学者（左リスト）  */
        /************************/
        foreach ($array as $examno) {
            //ENTEXAM_APPLICANTBASE_DAT
            //手続区分・日付をセット
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $baseRow = $db->getRow(" SELECT * FROM ENTEXAM_APPLICANTBASE_DAT " .$where, DB_FETCHMODE_ASSOC);
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = "1";      //手続区分 1:済み 2:無し
            if ($baseRow["PROCEDUREDATE"] == "") {
                $data["PROCEDUREDATE"][DATE]    = $model->pro_date;
            }
            //専併区分（1:海外入試の場合）
            if ($model->testdiv0 == "1" && $baseRow["SHDIV"] == "0") {
                $data["SHDIV"][TEXT]            = $model->shdiv;
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

            //入学コース
            list ($coursecd, $majorcd, $coursecode) = $EntCourse[$examno];

            //入学コースをセット
            list ($coursecd, $majorcd, $coursecode) = $EntCourse[$examno];
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SEQ"][TEXT]              = '007';
            $data["REMARK1"][TEXT]          = $coursecd;
            $data["REMARK2"][TEXT]          = $majorcd;
            $data["REMARK3"][TEXT]          = $coursecode;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        /************************/
        /*  合格者（右リスト）  */
        /************************/
        foreach ($array2 as $examno) {
            //ENTEXAM_APPLICANTBASE_DAT
            //手続区分・日付をクリア
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = "";       //手続区分 1:済み 2:無し
            $data["PROCEDUREDATE"][DATE]    = "";
            //専併区分をクリア（1:海外入試の場合）
            if ($model->testdiv0 == "1") {
                $data["SHDIV"][TEXT]            = "0";
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
