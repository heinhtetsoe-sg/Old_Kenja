<?php

require_once('for_php7.php');

class knjm410Query extends Query {

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
            $query .= "AND CHAIRCD like '92%' ";
            $query .= " ORDER BY CHAIRCD ";
        }else {
        //参照・更新可（制限付き）
            $model->User = 1;
            $query  = "SELECT CHAIRCD,CHAIRNAME ";
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR='" .$model->Year ."'";
            $query .= "AND SEMESTER ='" .$model->semester ."'";
            $query .= "AND CHAIRCD like '92%' ";
            $query .= "AND CHAIRCD in ";
            $query .= " (SELECT CHAIRCD ";
            $query .= "  FROM CHAIR_STF_DAT ";
            $query .= "WHERE YEAR='" .$model->Year ."'";
            $query .= "AND SEMESTER ='" .$model->semester ."'";
            $query .= "AND STAFFCD ='" .STAFFCD ."')";
            $query .= " ORDER BY CHAIRCD ";
//          $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
//          $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
//          $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
//          $query .= " ORDER BY CHAIRCD ";
        }
        return $query;
    }

    //登録済み生徒データ
    function getSch($model)
    {
        $query  = " SELECT t1.SCHREGNO,t2.NAME_SHOW,RECEIPT_TIME ";
        $query .= " FROM HR_ATTEND_DAT t1 left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '" .$model->Year."' ";
        $query .= "     AND t1.EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND t1.CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND t1.PERIODCD = '" .$model->field["PERIOD"]."' ";
        $query .= " ORDER BY RECEIPT_DATE DESC, t1.RECEIPT_TIME DESC,t1.SCHREGNO ";

        return $query;
    }
    //生徒登録確認
    function getCheckoutsc($model)
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

    //講座登録確認
    function getCheckoutch($model)
    {
        $query  = " SELECT DISTINCT CHAIRCD ";
        $query .= " FROM SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND PERIODCD = '" .$model->field["PERIOD"]."' ";
        $query .= "     AND SCHOOLING_SEQ = 0 ";

        return $query;
    }

    //重複登録確認
    function getCheckout($model)
    {
        $query  = " SELECT DISTINCT SCHREGNO ";
        $query .= " FROM HR_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND PERIODCD = '" .$model->field["PERIOD"]."' ";

        return $query;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = " INSERT INTO HR_ATTEND_DAT";
        $query .= " VALUES( ";
        $query .= "     '".$model->Year."', ";
        $query .= "     '".$model->field["SCHREGNO"]."', ";
        $query .= "     '".str_replace("/","-",$model->Date)."', ";
        $query .= "     '".$model->field["CHAIR"]."', ";
        $query .= "     '".$model->field["PERIOD"]."', ";
        $query .= "     '".CTRL_DATE."', ";
        $query .= "     time(sysdate()), ";
        $query .= "     '', ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
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
    function getUpdateQuery($model)
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
        $query .= "     AND SCHOOLING_SEQ = 0 ";

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
        $query .= "  HR_ATTEND_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND PERIODCD = '".$model->field["PERIOD"]."' ";

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
        $query .= "  HR_ATTEND_DAT ";
        $query .= " WHERE  ";
        $query .= "     YEAR = '" .$model->Year."' ";
        $query .= "     AND SCHREGNO in ".$model->schnodel ;
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$model->Date)."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIR"]."' ";
        $query .= "     AND PERIODCD = '".$model->field["PERIOD"]."' ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

}
?>