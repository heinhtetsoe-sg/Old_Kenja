<?php

require_once('for_php7.php');

class knjz418bQuery extends Query
{
    //大分類コンボ
    public function getJobtypeLcd()
    {
        $query  = " SELECT ";
        $query .= "     JOBTYPE_LCD || ' ' || JOBTYPE_LNAME AS LABEL, ";
        $query .= "     JOBTYPE_LCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_L_MST ";
        $query .= " ORDER BY ";
        $query .= "     JOBTYPE_LCD ";

        return $query;
    }

    //左フレームのリスト
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.JOBTYPE_LCD, ";
        $query .= "     T1.JOBTYPE_MCD, ";
        $query .= "     T1.JOBTYPE_MNAME, ";
        $query .= "     L1.JOBTYPE_SCD, ";
        $query .= "     L1.JOBTYPE_SSCD, ";
        $query .= "     L1.JOBTYPE_SNAME ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_M_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     JOBTYPE_SS_MST L1 ON  L1.JOBTYPE_LCD = T1.JOBTYPE_LCD ";
        $query .= "                      AND L1.JOBTYPE_MCD = T1.JOBTYPE_MCD ";
        $query .= " WHERE ";
        $query .= "     T1.JOBTYPE_LCD = '{$model->field["JOBTYPE_LCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.JOBTYPE_MCD, ";
        $query .= "     L1.JOBTYPE_SCD, ";
        $query .= "     L1.JOBTYPE_SSCD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    public function getJobtypeSCnt($jobtype_lcd, $jobtype_mcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_SS_MST ";
        $query .= " WHERE ";
        $query .= "         JOBTYPE_LCD = '{$jobtype_lcd}' ";
        $query .= "     AND JOBTYPE_MCD = '{$jobtype_mcd}' ";

        return $query;
    }

    public function getJobtypeMcd($model)
    {
        $query .= " SELECT ";
        $query .= "     JOBTYPE_LCD || ' ' || JOBTYPE_MCD || ' ' || JOBTYPE_MNAME AS LABEL, ";
        $query .= "     JOBTYPE_LCD || '_' || JOBTYPE_MCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_M_MST ";

        return $query;
    }

    //職業(小)マスタより1レコード取得(右フレーム用)
    public function getJobtypeMsMst($jobtype_lcd, $jobtype_mcd, $jobtype_scd, $jobtype_sscd)
    {
        $query .= " SELECT ";
        $query .= "     JOBTYPE_LCD, ";
        $query .= "     JOBTYPE_MCD, ";
        $query .= "     JOBTYPE_SCD, ";
        $query .= "     JOBTYPE_SSCD, ";
        $query .= "     JOBTYPE_SNAME, ";
        $query .= "     JOBTYPE_SNAME_KANA ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_SS_MST ";
        $query .= " WHERE ";
        $query .= "         JOBTYPE_LCD  = '{$jobtype_lcd}' ";
        $query .= "     AND JOBTYPE_MCD  = '{$jobtype_mcd}' ";
        $query .= "     AND JOBTYPE_SCD  = '{$jobtype_scd}' ";
        $query .= "     AND JOBTYPE_SSCD = '{$jobtype_sscd}' ";

        return $query;
    }

    /**********/
    /* UPDATE */
    /**********/
    //職業(小)更新
    public function getUpdateSQuery($model)
    {
        $db = Query::dbCheckOut();
        list($jobtype_lcd, $jobtype_mcd) = explode("_", $model->field["JOBTYPE_L_MCD"]);

        $query  = " DELETE FROM ";
        $query .= "     JOBTYPE_SS_MST ";
        $query .= " WHERE ";
        $query .= "         JOBTYPE_LCD  = '{$jobtype_lcd}' ";
        $query .= "     AND JOBTYPE_MCD  = '{$jobtype_mcd}' ";
        $query .= "     AND JOBTYPE_SCD  = '{$model->field["JOBTYPE_SCD"]}' ";
        $query .= "     AND JOBTYPE_SSCD = '{$model->field["JOBTYPE_SSCD"]}' ";

        $db->query($query);

        $data["JOBTYPE_LCD"][TEXT]        = $jobtype_lcd;
        $data["JOBTYPE_MCD"][TEXT]        = $jobtype_mcd;
        $data["JOBTYPE_SCD"][TEXT]        = $model->field["JOBTYPE_SCD"];
        $data["JOBTYPE_SSCD"][TEXT]       = $model->field["JOBTYPE_SSCD"];
        $data["JOBTYPE_SNAME"][TEXT]      = $model->field["JOBTYPE_SNAME"];
        $data["JOBTYPE_SNAME_KANA"][TEXT] = $model->field["JOBTYPE_SNAME_KANA"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "JOBTYPE_SS_MST");
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        list($jobtype_lcd, $jobtype_mcd) = explode("_", $model->field["JOBTYPE_L_MCD"]);

        //JOBTYPE_M_MSTは無条件に削除
        $query  = " DELETE FROM ";
        $query .= "     JOBTYPE_SS_MST ";
        $query .= " WHERE ";
        $query .= "         JOBTYPE_LCD  = '{$jobtype_lcd}' ";
        $query .= "     AND JOBTYPE_MCD  = '{$jobtype_mcd}' ";
        $query .= "     AND JOBTYPE_SCD  = '{$model->field["JOBTYPE_SCD"]}' ";
        $query .= "     AND JOBTYPE_SSCD = '{$model->field["JOBTYPE_SSCD"]}' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }
}
