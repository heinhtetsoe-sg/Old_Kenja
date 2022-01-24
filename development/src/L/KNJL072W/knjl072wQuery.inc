<?php

require_once('for_php7.php');

class knjl072wQuery extends Query {

    //権限チェック（入試管理者）
    function getAdminFlg() {
        $query  = " SELECT ";
        $query .= "     FIELD1 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND STAFF_SEQ = '009' ";

        $db = Query::dbCheckOut();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }
    
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース取得
    function getCourse($model) {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD || ':' || M1.MAJORNAME || T1.EXAMCOURSE_NAME AS LABEL, ";
        $query .= "     T1.COURSECD || '_' || T1.MAJORCD || '_' || T1.EXAMCOURSECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN MAJOR_MST M1 ";
        $query .= "          ON M1.COURSECD = T1.COURSECD ";
        $query .= "         AND M1.MAJORCD = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV         = '".$model->testdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格コンボ取得
    function getJudge($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".$model->ObjYear."' ";
        $query .= "     AND NAMECD1     = 'L013' ";
        $query .= "     AND NAMESPARE1  = '1' ";
        //入試区分が「4:スポーツ特枠選抜」の時、「3:前期選抜合格」選択可能
        if ($model->testdiv !== "4") {
            $query .= "     AND NAMECD2     <> '3' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格者一覧取得
    function getLeftList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EXAMNO AS VALUE, ";
        $query .= "     T1.EXAMNO || '　' || T1.NAME AS LABEL, ";
        $query .= "     T1.JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     INNER JOIN V_NAME_MST T3 ";
        $query .= "              ON T1.ENTEXAMYEAR  = T3.YEAR ";
        $query .= "             AND T3.NAMECD1      = 'L013' ";
        $query .= "             AND T3.NAMECD2      = T1.JUDGEMENT ";
        $query .= "             AND T3.NAMESPARE1   = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->testdiv."' AND ";
        if ($model->wishdiv == "1") {
            $query .= "     T1.LAST_DAI1_COURSECD || '_' || T1.LAST_DAI1_MAJORCD || '_' || T1.LAST_DAI1_COURSECODE = '".$model->examcoursecd."' AND ";
        } else {
            $query .= "     T1.LAST_DAI2_COURSECD || '_' || T1.LAST_DAI2_MAJORCD || '_' || T1.LAST_DAI2_COURSECODE = '".$model->examcoursecd."' AND ";
        }
        $query .= "     T1.SUC_COURSECD || '_' || T1.SUC_MAJORCD || '_' || T1.SUC_COURSECODE = '".$model->examcoursecd."' ";
        if ($model->testdiv2 == "1") {
            $query .= "     AND T1.TESTDIV2 = '".$model->testdiv2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受検者一覧取得
    function getRightList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EXAMNO AS VALUE, ";
        $query .= "     T1.EXAMNO || '　' || T1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->testdiv."' AND ";
        if ($model->wishdiv == "1") {
            $query .= "     T1.LAST_DAI1_COURSECD || '_' || T1.LAST_DAI1_MAJORCD || '_' || T1.LAST_DAI1_COURSECODE = '".$model->examcoursecd."' AND ";
        } else {
            $query .= "     T1.LAST_DAI2_COURSECD || '_' || T1.LAST_DAI2_MAJORCD || '_' || T1.LAST_DAI2_COURSECODE = '".$model->examcoursecd."' AND ";
        }
        $query .= "    (T1.JUDGEMENT IS NULL OR ";
        $query .= "     T1.JUDGEMENT NOT IN (SELECT ";
        $query .= "                             N1.NAMECD2 ";
        $query .= "                         FROM ";
        $query .= "                             V_NAME_MST N1 ";
        $query .= "                         WHERE ";
        $query .= "                             N1.YEAR     = T1.ENTEXAMYEAR AND ";
        $query .= "                             N1.NAMECD1  = 'L013' AND ";
        $query .= "                             (N1.NAMESPARE1 = '1' OR N1.NAMECD2 = '4' OR N1.NAMECD2 = '5') ";
        $query .= "                         )) ";
        if ($model->testdiv2 == "1") {
            $query .= "     AND T1.TESTDIV2 = '".$model->testdiv2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "     TESTDIV      = '".$model->testdiv."' AND ";
        $query .= "     COURSECD || '_' || MAJORCD || '_' || EXAMCOURSECD = '".$model->examcoursecd."' ";

        return $query;
    }

    //更新
    function &getUpdateQuery($model) {
        //合格者一覧
        $array  = strlen($model->selectdata) ? explode(",", $model->selectdata) : array();
        //受検者一覧
        $array2 = strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array();

        list ($coursecd, $majorcd, $coursecode) = explode("_", $model->examcoursecd);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入学コースを取得
        $getEnter = array();
        $getEnter = $db->getRow(knjl072wQuery::getEnterCourse($model), DB_FETCHMODE_ASSOC);

        //UPDATE -- 合格者
        foreach ($array as $examno) {
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $model->judge;
            $data["SUC_COURSECD"][TEXT]     = $coursecd;
            $data["SUC_MAJORCD"][TEXT]      = $majorcd;
            $data["SUC_COURSECODE"][TEXT]   = $coursecode;
            $data["PROCEDUREDIV"][TEXT]     = "1";
            $data["ENTDIV"][TEXT]           = "1";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR = '".$model->ObjYear."' AND ";
            $where .= "     APPLICANTDIV= '".$model->applicantdiv."' AND ";
            $where .= "     EXAMNO      = '".$examno."' ";

            $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where));

            //入学コース
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' AND ";
            $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
            $query .= "       EXAMNO        = '".$examno."' AND ";
            $query .= "       SEQ           = '007' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $examno;
            $data["SEQ"][TEXT]          = "007";
            $data["REMARK1"][TEXT]      = $getEnter["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]      = $getEnter["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]      = $getEnter["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        //UPDATE -- 受検者
        foreach ($array2 as $examno) {
            $data = array();
            $data["JUDGEMENT"][TEXT]        = "2";
            $data["SUC_COURSECD"][TEXT]     = NULL;
            $data["SUC_MAJORCD"][TEXT]      = NULL;
            $data["SUC_COURSECODE"][TEXT]   = NULL;
            //不合格更新時に(JUDGEMENT=2)、辞退(ENTDIV=2)をNULLにしない
            $query  = " SELECT ENTDIV FROM ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' AND ";
            $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
            $query .= "       EXAMNO        = '".$examno."' ";
            $entdiv = $db->getOne($query);
            if ($entdiv != "2") {
                $data["PROCEDUREDIV"][TEXT]     = NULL;
                $data["ENTDIV"][TEXT]           = NULL;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR = '".$model->ObjYear."' AND ";
            $where .= "     APPLICANTDIV= '".$model->applicantdiv."' AND ";
            $where .= "     EXAMNO      = '".$examno."' ";

            $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where));

            //入学コース
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' AND ";
            $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
            $query .= "       EXAMNO        = '".$examno."' AND ";
            $query .= "       SEQ           = '007' ";
            $db->query($query);
        }
        $db->commit();

        Query::dbCheckIn($db);
    }
}
?>
