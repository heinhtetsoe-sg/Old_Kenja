<?php

require_once('for_php7.php');

class knjb1213query extends Query {

    //生徒情報取得
    function getStudentInfo($model) {

        $query  = " WITH MAIN AS ( ";
        if ($model->search_div == "2") {
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.YEAR, ";
            $query .= "         T1.SEMESTER, ";
            $query .= "         T2.ENT_DATE, ";
            $query .= "         T1.ANNUAL, ";
            $query .= "         T3.SEND_ADDR1, ";
            $query .= "         T3.SEND_ADDR2 ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "         LEFT JOIN SCHREG_SEND_ADDRESS_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO AND T3.DIV = '1' ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".$model->year."' AND ";
            $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "         T1.SCHREGNO = '".$model->schregno."' ";
        } else {
            $query .= "     SELECT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.ENTERYEAR AS YEAR, ";
            $query .= "         '".CTRL_SEMESTER."' AS SEMESTER, ";
            $query .= "         T1.ENTERYEAR || '-04-01' AS ENT_DATE, ";
            $query .= "         '新入生' AS ANNUAL, ";
            $query .= "         T3.SEND_ADDR1, ";
            $query .= "         T3.SEND_ADDR2, ";
            $query .= "         T1.ADDR1, ";
            $query .= "         T1.ADDR2 ";
            $query .= "     FROM ";
            $query .= "         FRESHMAN_DAT T1 ";
            $query .= "         LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "         LEFT JOIN SCHREG_SEND_ADDRESS_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO AND T3.DIV = '1' ";
            $query .= "     WHERE ";
            $query .= "         T1.ENTERYEAR    = '".$model->year."' AND ";
            $query .= "         T1.SCHREGNO     = '".$model->schregno."' ";
        }
        $query .= " ), STUDYREC AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         SUM(VALUE(GET_CREDIT,0) + VALUE(ADD_CREDIT,0)) AS GET_CREDIT ";
        $query .= "     FROM ";
        $query .= "         MAIN T1, ";
        $query .= "         SCHREG_STUDYREC_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ), STD_SELECT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         COUNT(T2.SUBCLASSCD) AS SUBCLASS_NUM ";
        $query .= "     FROM ";
        $query .= "         MAIN T1, ";
        $query .= "         SUBCLASS_STD_SELECT_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T2.GROUPCD  = '001' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ), ADDRESS AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.ADDR1, ";
        $query .= "         T2.ADDR2 ";
        $query .= "     FROM ";
        $query .= "         MAIN T1, ";
        $query .= "         SCHREG_ADDRESS_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.ISSUEDATE = (SELECT ";
        $query .= "                             MAX(A1.ISSUEDATE) ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_ADDRESS_DAT A1 ";
        $query .= "                         WHERE ";
        $query .= "                             T1.SCHREGNO = A1.SCHREGNO ";
        $query .= "                         ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.GET_CREDIT, ";
        $query .= "     T3.SUBCLASS_NUM ";
        if ($model->search_div == "2") {
            $query .= "    ,T4.ADDR1 ";
            $query .= "    ,T4.ADDR2 ";
        }
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN STUDYREC T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN STD_SELECT T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN ADDRESS T4 ON T1.SCHREGNO = T4.SCHREGNO ";

        return $query;
    }

    //入学日取得
    function getSchregBaseMst($model) {

        $query  = " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //その他住所取得
    function getSchregSendAddressDat($model) {

        $query  = " SELECT ";
        $query .= "     SEND_ADDR1, ";
        $query .= "     SEND_ADDR2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_SEND_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     DIV = '1' ";

        return $query;
    }

    //学籍住所取得
    function getSchregAddressDat($model) {

        $query  = " SELECT ";
        $query .= "     ADDR1, ";
        $query .= "     ADDR2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     ISSUEDATE   = ( SELECT ";
        $query .= "                         MAX(A1.ISSUEDATE) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_ADDRESS_DAT A1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.SCHREGNO = A1.SCHREGNO ";
        $query .= "                    ) ";

        return $query;
    }

    //登録日取得
    function getRegisterDate($model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     REGISTER_DATE AS VALUE, ";
        $query .= "     REGISTER_DATE LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_APPLY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     YEAR     = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //無償給与対象外教科書取得
    function getSchregTextbookSubclassDat($model, $flg="") {

        $selectdata = explode(',', $model->selectdata);
        for ($i=0; $i< get_count($selectdata); $i++) list($textbookcd[], $div[], $price[], $touroku[]) = explode('-',$selectdata[$i]);

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.TEXTBOOKCD ||'-' || T3.NAMESPARE1 || '-' || CHAR(T2.TEXTBOOKUNITPRICE) AS VALUE, ";
        $query .= "     T1.TEXTBOOKCD || '　' || T2.TEXTBOOKNAME AS LABEL, ";
        $query .= "     T1.TEXTBOOKCD, ";
        $query .= "     T3.NAMESPARE1 AS DIV, ";
        $query .= "     T2.TEXTBOOKUNITPRICE ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_SUBCLASS_DAT T1, ";
        $query .= "     TEXTBOOK_MST T2 ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.NAMECD1 = 'M004' AND T2.TEXTBOOKDIV = T3.NAMECD2 AND T3.NAMESPARE1 IN ('1','2') ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     T1.YEAR         = T3.YEAR AND ";
        $query .= "     T1.YEAR         = '".$model->year."' AND ";
        $query .= "     T1.TEXTBOOKCD   = T2.TEXTBOOKCD ";
        if ($flg) {
            $query .= "     AND T1.TEXTBOOKCD IN ('".implode("','", $textbookcd)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //無償給与対象教科書取得
    function getSchregTextbookFreeDat($model) {
        $register_date = str_replace("/", "-", $model->register_date);

        $query  = " SELECT ";
        $query .= "     T1.TEXTBOOKCD || '-' || T3.NAMESPARE1 || '-' || CHAR(T2.TEXTBOOKUNITPRICE) AS VALUE, ";
        $query .= "     T1.TEXTBOOKCD || '　' || T2.TEXTBOOKNAME AS LABEL, ";
        $query .= "     T1.TEXTBOOKCD, ";
        $query .= "     T3.NAMESPARE1 AS DIV, ";
        $query .= "     T2.TEXTBOOKUNITPRICE ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_DAT T1, ";
        $query .= "     TEXTBOOK_MST T2 ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.NAMECD1= 'M004' AND T2.TEXTBOOKDIV = T3.NAMECD2 AND T3.NAMESPARE1 IN ('1','2') ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO         = '".$model->schregno."' AND ";
        $query .= "     T1.YEAR             = T3.YEAR AND ";
        $query .= "     T1.YEAR             = '".$model->year."' AND ";
        $query .= "     T1.REGISTER_DATE    = '".$register_date."' AND ";
        $query .= "     T1.TEXTBOOKCD       = T2.TEXTBOOKCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //SCHREG_TEXTBOOK_FREE_DATに登録されているか
    function checkTextbook($model, $textbookcd, $flg="") {
        $register_date = ($model->register_date) ? str_replace("/", "-", $model->register_date) : "";

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        if ($flg == 'past') {
            $query .= "     YEAR        < '".$model->year."' AND ";
        } else if ($flg == 'this') {
            $query .= "     YEAR            = '".$model->year."' AND ";
            $query .= "     REGISTER_DATE   = '".$register_date."' AND ";
        }
        $query .= "     TEXTBOOKCD  = '".$textbookcd."' ";

        return $query;
    }

    //SCHREG_TEXTBOOK_FREE_APPLY_DAT取得
    function getSchregTextbookFreeApplyDat($model) {
        $register_date = str_replace("/", "-", $model->register_date);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_APPLY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO         = '".$model->schregno."' AND ";
        $query .= "     YEAR             = '".$model->year."' AND ";
        $query .= "     REGISTER_DATE    = '".$register_date."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $namecd2="") {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //存在チェック（SCHREG_TEXTBOOK_FREE_DAT）
    function checkSchregTextbookFreeDat($model) {

        $register_date = str_replace("/", "-", $model->field["REGISTER_DATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     REGISTER_DATE   = '".$register_date."' ";

        return $query;
    }

    //存在チェック（SCHREG_TEXTBOOK_FREE_APPLY_DAT）
    function checkSchregTextbookFreeApplyDat($model) {

        $register_date = str_replace("/", "-", $model->field["REGISTER_DATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_APPLY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     REGISTER_DATE   = '".$register_date."' ";

        return $query;
    }

    //教科書存在チェック
    function checkTextbookExist($model) {

        $selectdata = explode(',', $model->selectdata);
        for ($i=0; $i< get_count($selectdata); $i++) list($textbookcd[], $bookdiv[], $price[], $touroku[]) = explode('-',$selectdata[$i]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     TEXTBOOKCD IN ('".implode("','", $textbookcd)."') ";

        return $query;
    }

    //教科書情報取得
    function getTextbookSubclassDat($model, $textbookcd) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     TEXTBOOKCD      = '".$textbookcd."' ";
        $query .= " ORDER BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //追加
    function &getInsertQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $selectdata = explode(',', $model->selectdata);

        //SCHREG_TEXTBOOK_FREE_DAT
        if ($selectdata) {
            for ($i = 0; $i < get_count($selectdata); $i++){
                list($textbookcd, $bookdiv, $price, $touroku) = explode('-', $selectdata[$i]);
                $info = $db->getRow(knjb1213Query::getTextbookSubclassDat($model, $textbookcd), DB_FETCHMODE_ASSOC);

                $data = array();
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["YEAR"][TEXT]             = $model->year;
                $data["REGISTER_DATE"][TEXT]    = str_replace("/", "-", $model->field["REGISTER_DATE"]);
                $data["CLASSCD"][TEXT]          = $info["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $info["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $info["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]       = $info["SUBCLASSCD"];
                $data["TEXTBOOKCD"][TEXT]       = $textbookcd;
                $data["FREE_FLG"][TEXT]         = 1;

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "SCHREG_TEXTBOOK_FREE_DAT");
                $db->query($query);
            }
        }

        //SCHREG_TEXTBOOK_FREE_APPLY_DAT
        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["YEAR"][TEXT]                 = $model->year;
        $data["REGISTER_DATE"][TEXT]        = str_replace("/", "-", $model->field["REGISTER_DATE"]);
        $data["TOTAL_GK"][NUMBER]           = $model->field["BOOKDIV1_GK"] + $model->field["BOOKDIV2_GK"];
        $data["TOTAL_COUNT"][NUMBER]        = $model->field["TOTAL_COUNT"];
        $data["BOOKDIV1_GK"][NUMBER]        = $model->field["BOOKDIV1_GK"];
        $data["BOOKDIV2_GK"][NUMBER]        = $model->field["BOOKDIV2_GK"];
        $data["PROVIDE_REASON"][TEXT]       = $model->field["PROVIDE_REASON"];
        $data["ATTACH_DOCUMENTS"][TEXT]     = $model->field["ATTACH_DOCUMENTS"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["JUDGE_RESULT"][TEXT]         = $model->field["JUDGE_RESULT"];
        $data["DECISION_DATE"][TEXT]        = str_replace("/", "-", $model->field["DECISION_DATE"]);

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_TEXTBOOK_FREE_APPLY_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //更新
    function &getUpdateQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $selectdata = explode(',', $model->selectdata);
        $register_date = str_replace("/", "-", $model->register_date);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     REGISTER_DATE   = '".$register_date."' ";

        $db->query($query);

        //SCHREG_TEXTBOOK_FREE_DAT
        if ($selectdata) {
            for ($i = 0; $i < get_count($selectdata); $i++){
                list($textbookcd, $bookdiv, $price, $touroku) = explode('-', $selectdata[$i]);
                $info = $db->getRow(knjb1213Query::getTextbookSubclassDat($model, $textbookcd), DB_FETCHMODE_ASSOC);

                $data = array();
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["YEAR"][TEXT]             = $model->year;
                $data["REGISTER_DATE"][TEXT]    = $register_date;
                $data["CLASSCD"][TEXT]          = $info["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $info["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $info["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]       = $info["SUBCLASSCD"];
                $data["TEXTBOOKCD"][TEXT]       = $textbookcd;
                $data["FREE_FLG"][TEXT]         = 1;

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "SCHREG_TEXTBOOK_FREE_DAT");
                $db->query($query);
            }
        }

        //SCHREG_TEXTBOOK_FREE_APPLY_DAT
        $data = array();
        $data["TOTAL_GK"][NUMBER]           = $model->field["BOOKDIV1_GK"] + $model->field["BOOKDIV2_GK"];
        $data["TOTAL_COUNT"][NUMBER]        = $model->field["TOTAL_COUNT"];
        $data["BOOKDIV1_GK"][NUMBER]        = $model->field["BOOKDIV1_GK"];
        $data["BOOKDIV2_GK"][NUMBER]        = $model->field["BOOKDIV2_GK"];
        $data["PROVIDE_REASON"][TEXT]       = $model->field["PROVIDE_REASON"];
        $data["ATTACH_DOCUMENTS"][TEXT]     = $model->field["ATTACH_DOCUMENTS"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["JUDGE_RESULT"][TEXT]         = $model->field["JUDGE_RESULT"];
        $data["DECISION_DATE"][TEXT]        = str_replace("/", "-", $model->field["DECISION_DATE"]);

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $where .= "     YEAR            = '".$model->year."' AND ";
        $where .= "     REGISTER_DATE   = '".$register_date."' ";

        $query = Query::updateSQL($data, "SCHREG_TEXTBOOK_FREE_APPLY_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //削除
    function &getDeleteQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $register_date = str_replace("/", "-", $model->register_date);

        //SCHREG_TEXTBOOK_FREE_DAT
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     REGISTER_DATE   = '".$register_date."' ";

        $db->query($query);

        //SCHREG_TEXTBOOK_FREE_APPLY_DAT
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_TEXTBOOK_FREE_APPLY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     YEAR            = '".$model->year."' AND ";
        $query .= "     REGISTER_DATE   = '".$register_date."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
