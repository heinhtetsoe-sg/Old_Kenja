<?php

require_once('for_php7.php');

class knjm110Query extends Query {

    //講座取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT CHAIRCD,CHAIRNAME ";
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR='" .$model->Year ."'";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."'";
            $query .= "AND CHAIRCD not like '92%' ";
            $query .= " ORDER BY CHAIRCD ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT CHAIRCD,CHAIRNAME ";
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR='" .$model->Year ."'";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."'";
            $query .= "AND CHAIRCD not like '92%' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " ORDER BY CHAIRCD ";
        }

        return $query;
    }

    //担当者名
    function getStaffname($model)
    {
        $query  = " SELECT ";
        $query .= "     w1.STAFFCD, ";
        $query .= "     w2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT w1 left join STAFF_MST w2 on w1.STAFFCD = w2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year ."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER ."' AND ";
        $query .= "     CHAIRCD = '".$model->Chair ."' ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //データ取得
    function ReadQuery($model)
    {

        $query  = " SELECT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     SCHOOLING_SEQ, ";
        $query .= "     EXECUTED, ";
        $query .= "     REMARK, ";
        $query .= "     CHAIRNAME, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT w1 left join V_NAME_MST w2 on w1.PERIODCD = w2.NAMECD2 and w2.YEAR = '" .$model->Year ."' and NAMECD1 = 'B001' left join CHAIR_DAT w3 on w1.CHAIRCD = w3.CHAIRCD and w3.YEAR = '" .$model->Year ."' and w3.SEMESTER = '" .CTRL_SEMESTER ."' ";
        $query .= " WHERE ";
//        $query .= "     (EXECUTED <> '1' OR EXECUTED is null) AND ";
        $query .= "     w1.YEAR = '" .$model->Year ."' AND ";
        $query .= "     w1.CHAIRCD = '".$model->Chair ."' ";
        $query .= " ORDER BY EXECUTEDATE ";

        return $query;
    }

    //校時取得
    function selectPeriod($model)
    {
        $query  = " SELECT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year ."' ";
        $query .= "     AND NAMECD1 = 'B001' ";

        return $query;
    }

    //講座名称取得
    function getChairname($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year ."' AND ";
        $query .= "     SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "     CHAIRCD = '".$model->Chair."' ";

        return $query;
    }

    //最大回数
    function selectQuery($model)
    {
        $query  = "SELECT ";
        $query .= "  SCH_SEQ_ALL, ";
        $query .= "  SCH_SEQ_MIN ";
        $query .= "FROM ";
        $query .= "  CHAIR_CORRES_DAT ";
        $query .= "WHERE ";
        $query .= "  YEAR = '".$model->Year."' AND ";
        $query .= "  CHAIRCD = '".$model->Chair."' ";

        return $query;
    }

    function getAttendDate()
    {
        return "SELECT ATTEND_CTRL_DATE FROM CONTROL_MST";
    }

    //データチェック用
    function CheckData($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '" .str_replace("/","-",$model->field["EXEDATE2"]) ."' AND ";
        $query .= "     PERIODCD = '" .$model->field["KOUJI2"] ."' AND ";
        $query .= "     CHAIRCD = '".$model->Chair."' AND ";
        $query .= "     SCHOOLING_SEQ = ".$model->field["KAISUU2"]." ";

        return $query;
    }


    //データチェック用
    function CheckData2($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '" .str_replace("/","-",$model->Exedate) ."' AND ";
        $query .= "     PERIODCD = '" .$model->Periodcd ."' AND ";
        $query .= "     CHAIRCD = '".$model->Chair."' AND ";
        $query .= "     SCHOOLING_SEQ = ".$model->Schooling_seq." ";

        return $query;
    }


    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["EXECUTEDATE"][TEXT]      = str_replace("/","-",$model->field["EXEDATE2"]);
        $data["PERIODCD"][TEXT]         = $model->field["KOUJI2"];
        $data["CHAIRCD"][TEXT]          = $model->Chair;
        $data["SCHOOLING_SEQ"][NUMBER]  = $model->field["KAISUU2"];
        $data["YEAR"][TEXT]             = $model->Year;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $where  = " WHERE EXECUTEDATE   = '".str_replace("/","-",$model->Exedate)."'";
        $where .= " AND   PERIODCD      = '" .$model->Periodcd ."' ";
        $where .= " AND   CHAIRCD       = '".$model->Chair."' ";
        $where .= " AND   SCHOOLING_SEQ = ".$model->Schooling_seq." ";

        $query = Query::updateSQL($data, "SCH_CHR_T_DAT", $where);
        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["EXECUTEDATE"][TEXT]         = str_replace("/","-",$model->field["EXEDATE2"]);
        $data["PERIODCD"][TEXT]            = $model->field["KOUJI2"];
        $data["CHAIRCD"][TEXT]             = $model->Chair;
        $data["EXECUTED"][TEXT]            = '';
        $data["DATADIV"][TEXT]             = '';
        $data["YEAR"][TEXT]                = $model->Year;
        $data["SEMESTER"][TEXT]            = $model->semester;
        $data["ATTESTOR"][NUMBER]          = '';
        $data["SCHOOLING_SEQ"][NUMBER]     = $model->field["KAISUU2"];
        $data["REMARK"][TEXT]              = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "SYSDATE()";

        $query = Query::insertSQL($data, "SCH_CHR_T_DAT");

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    /**
     * DELETE
     */
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "DELETE FROM ";
        $query .= "  SCH_CHR_T_DAT ";
        $query .= " WHERE  ";
        $query .= "     EXECUTEDATE = '" .str_replace("/","-",$model->Exedate) ."' AND ";
        $query .= "     PERIODCD = '" .$model->Periodcd ."' AND ";
        $query .= "     CHAIRCD = '".$model->Chair."' AND ";
        $query .= "     SCHOOLING_SEQ = ".$model->Schooling_seq." ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>
