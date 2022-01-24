<?php

require_once('for_php7.php');

class knjm390Query extends Query {

    //校時取得
    function selectName($cd1)
    {
        $query  = " SELECT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$cd1."' ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //講座取得（権限チェック）
    function getAuth(&$model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_UPDATABLE){
            $model->User = 0;
            $query  = "SELECT DISTINCT CHAIRCD,CHAIRNAME ";
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR ='" .$model->Year ."'";
            $query .= "AND SEMESTER ='" .$model->semester ."'";
            $query .= "AND CHAIRCD not like '92%' ";
            $query .= " ORDER BY CHAIRCD ";
        }else {
        //参照・更新可（制限付き）
            $model->User = 1;
            $query  = "SELECT CHAIRCD,CHAIRNAME ";
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR='" .$model->Year ."'";
            $query .= "AND SEMESTER ='" .$model->semester ."'";
            $query .= "AND CHAIRCD not like '92%' ";
            $query .= "AND CHAIRCD in ";
            $query .= " (SELECT CHAIRCD ";
            $query .= "  FROM CHAIR_STF_DAT ";
            $query .= "WHERE YEAR='" .$model->Year ."'";
            $query .= "AND SEMESTER ='" .$model->semester ."'";
            $query .= "AND STAFFCD ='" .STAFFCD ."')";
            $query .= " ORDER BY CHAIRCD ";
//          $query .= "AND CHAIRCD in ";
//          $query .= " (SELECT CHAIRCD ";
//          $query .= "  FROM CHAIR_STF_DAT ";
//          $query .= "WHERE YEAR='" .$model->Year ."'";
//          $query .= "AND SEMESTER ='" .$model->semester ."'";
//          $query .= "AND STAFFCD ='" .STAFFCD ."' ) ";
//          $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
//          $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
//          $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
//          $query .= " ORDER BY CHAIRCD ";
        }

        return $query;
    }

    //担当者取得
    function selectStaff($model)
    {
        $query  = " SELECT w1.STAFFCD,w1.CHARGEDIV,w2.STAFFNAME ";
        $query .= " FROM CHAIR_STF_DAT w1 left join V_STAFF_MST w2 on w1.STAFFCD = w2.STAFFCD and w2.YEAR = '" .CTRL_YEAR."' ";
        $query .= " WHERE ";
        $query .= "     w1.YEAR = '" .CTRL_YEAR."' ";
        $query .= "     AND w1.SEMESTER = '" .$model->semester ."' ";
        $query .= "     AND w1.CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= " ORDER BY w1.CHARGEDIV DESC, w1.STAFFCD ";

        return $query;
    }

    //回数取得
    function selectSeq($model)
    {
        $query  = " SELECT SCHOOLING_SEQ ";
        $query .= " FROM SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND PERIODCD = '" .$model->field["PERIOD"]."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";

        return $query;
    }

    //出席校
    function getPartnerSchool($model)
    {
        $query  = " SELECT ";
        $query .= "     PARTNER_SCHOOLCD, ";
        $query .= "     PARTNER_SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PARTNER_SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     PARTNER_SCHOOLCD ";

        return $query;
    }

    //登録済み生徒データ
    function getSch($model)
    {
        $query  = " SELECT t1.SCHREGNO,t2.NAME_SHOW,RECEIPT_TIME,t1.REMARK ";
        $query .= " FROM SCH_ATTEND_DAT t1 left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '" .$model->Year."' ";
        $query .= "     AND t1.EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND t1.CHAIRCD = '".$model->field["CHAIR"]."' ";
        if ($model->field["COURSE"] == 1){
            $query .= "     AND t1.PERIODCD = '" .$model->field["PERIOD"]."' ";
            if ($model->field["SCHOOLING_SEQ"]){
                $query .= "     AND t1.SCHOOLING_SEQ = " .$model->field["SCHOOLING_SEQ"]." ";
            }
            $query .= "     AND t1.STAFFCD = '" .$model->field["STAFF"]."' ";
        }
        $query .= "     AND t1.SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";
        $query .= " ORDER BY RECEIPT_DATE DESC, RECEIPT_TIME DESC, t1.SCHREGNO ";

        return $query;
    }

    //講座登録確認
    function getCheckout($model)
    {
        $query  = " SELECT DISTINCT SCHREGNO ";
        $query .= " FROM CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND SEMESTER = '" .$model->semester ."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";

        return $query;
    }

    //サブフォームデータ
    function getsubremark($model)
    {
        $query  = " SELECT t1.SCHREGNO,t2.NAME_SHOW,t1.REMARK ";
        $query .= " FROM SCH_ATTEND_DAT t1 left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND t1.SCHREGNO = '".$model->sch."' ";
        $query .= "     AND t1.EXECUTEDATE = '" .str_replace("/","-",$model->date)."' ";
        $query .= "     AND t1.CHAIRCD = '".$model->chir."' ";
        if ($model->crs == 1){
            $query .= "     AND t1.PERIODCD = '".$model->peri."' ";
            $query .= "     AND t1.STAFFCD = '".$model->stf."' ";
            $query .= "     AND SCHOOLING_SEQ = " .$model->seq." ";
        }
        $query .= "     AND t1.SCHOOLINGKINDCD = '".$model->crs."' ";

        return $query;
    }

    //登録済みチェック（登校スクーリングのみ）
    function getCheckIns($model)
    {
        $query  = " SELECT SCHREGNO ";
        $query .= " FROM SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND PERIODCD = '" .$model->field["PERIOD"]."' ";
        $query .= "     AND SCHOOLING_SEQ = " .$model->field["SCHOOLING_SEQ"]." ";
        $query .= "     AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = " INSERT INTO SCH_ATTEND_DAT";
        $query .= " (YEAR,SCHREGNO,EXECUTEDATE,CHAIRCD,PERIODCD, ";
        if ($model->field["COURSE"] == 1){
            $query .= " SCHOOLING_SEQ, ";
        }
        $query .= " RECEIPT_DATE,RECEIPT_TIME,STAFFCD,TERMINAL_CD,SCHOOLINGKINDCD, ";
        $query .= " REMARK,REGISTERCD,UPDATED) ";
        $query .= " VALUES( ";
        $query .= "     '".$model->Year."', ";
        $query .= "     '".$model->field["SCHREGNO"]."', ";
        $query .= "     '".str_replace("/","-",$model->Date)."', ";
        $query .= "     '".$model->field["CHAIR"]."', ";
        if ($model->field["COURSE"] == 1){
            $query .= "     '".$model->field["PERIOD"]."', ";
            $query .= "     ".$model->field["SCHOOLING_SEQ"].", ";
        }else {
            $query .= "     '', ";
        }
        $query .= "     '".CTRL_DATE."', ";
        $query .= "     current time, ";
        if ($model->field["COURSE"] == 1){
            $query .= "     '".$model->field["STAFF"]."', ";
        }else {
            $query .= "     '', ";
        }
        $query .= "     '', ";
        $query .= "     '".$model->field["COURSE"]."', ";
        if ($model->field["REMARK"] == ''){
            $query .= "     '無', ";
        }else {
            $query .= "     '".$model->field["REMARK"]."', ";
        }
        $query .= "     '".STAFFCD."', ";
        $query .= "     current timestamp ";
        $query .= " ) ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    /**
     * UPDATE
     */
    //実施区分設定
    function getUpdateSchchr($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "UPDATE  ";
        $query .= "  SCH_CHR_T_DAT SET  ";
        $query .= "  EXECUTED = '1' ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND PERIODCD = '" .$model->field["PERIOD"]."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND SCHOOLING_SEQ = ".$model->field["SCHOOLING_SEQ"]." ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //出席校
    function getInsertPartnerSchool($model) {

        $db = Query::dbCheckOut();

        $query  = "DELETE FROM ";
        $query .= "  SCH_ATTEND_DETAIL_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '{$model->Year}' ";
        $query .= "     AND SCHREGNO = '{$model->field["SCHREGNO"]}' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '{$model->field["CHAIR"]}' ";
        $query .= "     AND SEQ = '002' ";

        $db->query($query);

        $data["YEAR"][TEXT]         = $model->Year;
        $data["SCHREGNO"][TEXT]     = $model->field["SCHREGNO"];
        $data["EXECUTEDATE"][DATE]  = $model->Date;
        $data["CHAIRCD"][TEXT]      = $model->field["CHAIR"];
        $data["SEQ"][TEXT]          = "002";
        $data["REMARK1"][TEXT]      = $model->field["PARTNER_SCHOOL"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "SCH_ATTEND_DETAIL_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "UPDATE  ";
        $query .= "  SCH_ATTEND_DAT SET  ";
        if (VARS::post("REMARKSUB") == ''){
            $query .= "  REMARK = '無', ";
        }else {
            $query .= "  REMARK = '".VARS::post("REMARKSUB")."', ";
        }
        $query .= "  REGISTERCD = '".STAFFCD."', ";
        $query .= "  UPDATED = sysdate() ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO = '".$model->sch."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->date)."' ";
        $query .= "     AND CHAIRCD = '".$model->chir."' ";
        if ($model->crs == 1){
            $query .= "     AND PERIODCD = '".$model->peri."' ";
            $query .= "     AND STAFFCD = '".$model->stf."' ";
            $query .= "     AND SCHOOLING_SEQ = ".$model->seq." ";
        }
        $query .= "     AND SCHOOLINGKINDCD = '".$model->crs."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $query;
    }

    /**
     * DELETE
     */
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "DELETE FROM ";
        $query .= "  SCH_ATTEND_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        if ($model->field["COURSE"] == 1){
            $query .= "     AND PERIODCD = '".$model->field["PERIOD"]."' ";
            $query .= "     AND STAFFCD = '".$model->field["STAFF"]."' ";
            $query .= "     AND SCHOOLING_SEQ = ".$model->field["SCHOOLING_SEQ"]." ";
        }
        $query .= "     AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";

        $db->query($query);

        $query  = "DELETE FROM ";
        $query .= "  SCH_ATTEND_DETAIL_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND SEQ = '002' ";

        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getDeleteQuerysitei($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "DELETE FROM ";
        $query .= "  SCH_ATTEND_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO in ".$model->schnodel ;
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        if ($model->field["COURSE"] == 1){
            $query .= "     AND PERIODCD = '".$model->field["PERIOD"]."' ";
            $query .= "     AND STAFFCD = '".$model->field["STAFF"]."' ";
            $query .= "     AND SCHOOLING_SEQ = ".$model->field["SCHOOLING_SEQ"]." ";
        }
        $query .= "     AND SCHOOLINGKINDCD = '".$model->field["COURSE"]."' ";

        $db->query($query);

        $query  = "DELETE FROM ";
        $query .= "  SCH_ATTEND_DETAIL_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO in ".$model->schnodel ;
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND SEQ = '002' ";

        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

}
?>