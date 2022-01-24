<?php

require_once('for_php7.php');

class knjm110mQuery extends Query {

    //講座取得（権限チェック）
    function getChairList($model) {
        $query  = " SELECT ";
        $query .= "     CHAIRCD AS VALUE, ";
        $query .= "     CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->Year."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     CHAIRCD NOT LIKE '92%' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 CHAIR_STF_DAT T2 ";
            $query .= "             WHERE ";
            $query .= "                 T1.YEAR     = T2.YEAR AND ";
            $query .= "                 T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "                 T1.CHAIRCD  = T2.CHAIRCD AND ";
            $query .= "                 T2.STAFFCD  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //担当者名
    function getStaffname($model) {
        $query  = " SELECT ";
        $query .= "     W1.STAFFCD, ";
        $query .= "     W2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT W1 ";
        $query .= "     LEFT JOIN STAFF_MST W2 ON W1.STAFFCD = W2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->Year."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     CHAIRCD     = '".$model->Chair."' ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //実施日付一覧取得
    function getExecutedateList($model) {
        $sdate = $model->Year.'-04-01';
        $edate = ($model->Year+1).'-03-31';

        $query  = " SELECT DISTINCT ";
        $query .= "     EXECUTEDATE AS LABEL, ";
        $query .= "     EXECUTEDATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE BETWEEN '".$sdate."' AND '".$edate."' AND ";
        $query .= "     CHAIRCD NOT LIKE '92%' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 CHAIR_STF_DAT T2 ";
            $query .= "             WHERE ";
            $query .= "                 T1.YEAR     = T2.YEAR AND ";
            $query .= "                 T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "                 T1.CHAIRCD  = T2.CHAIRCD AND ";
            $query .= "                 T2.STAFFCD  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ取得
    function ReadQuery($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->Div == "2") {
            $query .= "     T1.EXECUTEDATE, ";
            $query .= "     T1.PERIODCD, ";
            $query .= "     T2.NAME1 AS KOUJI, ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     T3.CHAIRNAME, ";
            $query .= "     T1.EXECUTED, ";
            $query .= "     T1.SCHOOLING_SEQ ";
        } else {
            $query .= "     T1.EXECUTEDATE, ";
            $query .= "     T1.PERIODCD, ";
            $query .= "     T2.NAME1 AS KOUJI, ";
            $query .= "     T1.EXECUTED, ";
            $query .= "     T1.SCHOOLING_SEQ ";
        }
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT T1, ";
        $query .= "     V_NAME_MST T2, ";
        $query .= "     CHAIR_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND";
        $query .= "     T1.YEAR     = '".$model->Year."' AND ";
        $query .= "     T1.PERIODCD = T2.NAMECD2 AND ";
        $query .= "     T2.NAMECD1  = 'B001' AND ";
        $query .= "     T1.CHAIRCD  = T3.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD NOT LIKE '92%' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        if ($model->Div == "2") {
            if ($model->Exedate) {
                $query .= "     T1.EXECUTEDATE  = '".$model->Exedate."' ";
            } else {
                $query .= "     T1.EXECUTEDATE IS NULL ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.PERIODCD, ";
            $query .= "     T1.CHAIRCD, ";
            $query .= "     T1.SCHOOLING_SEQ ";
        } else {
            $query .= "     T1.CHAIRCD  = '".$model->Chair."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.EXECUTEDATE, ";
            $query .= "     T1.PERIODCD, ";
            $query .= "     T1.SCHOOLING_SEQ ";
        }

        return $query;
    }

    //講座名称取得
    function getChairname($model) {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->Year."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     CHAIRCD     = '".$model->Chair."' ";

        return $query;
    }

    //校時取得
    function selectPeriod($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->Year."' AND ";
        $query .= "     NAMECD1 = 'B001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //備考取得
    function getRemark($model) {
        $query  = " SELECT ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE     = '".$model->Exedate."' AND ";
        $query .= "     PERIODCD        = '".$model->Periodcd."' AND ";
        $query .= "     CHAIRCD         = '".$model->Chair."' AND ";
        $query .= "     SCHOOLING_SEQ   =  ".$model->Schooling_seq."  ";

        return $query;
    }

    //学期マスタ取得
    function getSemesterMst($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->Year."' AND ";
        $query .= "     SEMESTER <> '9' ";

        return $query;
    }

    //データチェック用
    function CheckData($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE     = '".str_replace("/","-",$model->field["EXEDATE2"])."' AND ";
        $query .= "     PERIODCD        = '".$model->field["KOUJI2"] ."' AND ";
        $query .= "     CHAIRCD         = '".$model->field["CHAIRCD2"] ."' AND ";
        $query .= "     SCHOOLING_SEQ   = 1 ";

        return $query;
    }

    //データチェック用
    function CheckData2($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE     = '".str_replace("/","-",$model->Exedate) ."' AND ";
        $query .= "     PERIODCD        = '".$model->Periodcd ."' AND ";
        $query .= "     CHAIRCD         = '".$model->Chair."' AND ";
        $query .= "     SCHOOLING_SEQ   =  ".$model->field["SCHOOLING_SEQ"]." ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]             = $model->Year;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     EXECUTEDATE     = '".str_replace("/","-",$model->Exedate)."' AND ";
        $where .= "     PERIODCD        = '".$model->Periodcd."' AND ";
        $where .= "     CHAIRCD         = '".$model->Chair."' AND ";
        $where .= "     SCHOOLING_SEQ   =  ".$model->field["SCHOOLING_SEQ"]." ";

        $query = Query::updateSQL($data, "SCH_CHR_T_DAT", $where);
        $db->query($query);
        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["EXECUTEDATE"][TEXT]         = str_replace("/","-",$model->field["EXEDATE2"]);
        $data["PERIODCD"][TEXT]            = $model->field["KOUJI2"];
        $data["CHAIRCD"][TEXT]             = $model->field["CHAIRCD2"];
        $data["EXECUTED"][TEXT]            = '';
        $data["DATADIV"][TEXT]             = '';
        $data["YEAR"][TEXT]                = $model->Year;
        $data["SEMESTER"][TEXT]            = $model->semester;
        $data["ATTESTOR"][NUMBER]          = '';
        $data["SCHOOLING_SEQ"][NUMBER]     = 1;
        $data["REMARK"][TEXT]              = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "SYSDATE()";

        $query = Query::insertSQL($data, "SCH_CHR_T_DAT");
        $db->query($query);
        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE     = '".str_replace("/","-",$model->Exedate)."' AND ";
        $query .= "     PERIODCD        = '".$model->Periodcd."' AND ";
        $query .= "     CHAIRCD         = '".$model->Chair."' AND ";
        $query .= "     SCHOOLING_SEQ   = ".$model->field["SCHOOLING_SEQ"]." ";

        $db->query($query);
        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>
