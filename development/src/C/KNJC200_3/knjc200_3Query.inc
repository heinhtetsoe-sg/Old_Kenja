<?php

require_once('for_php7.php');


class knjc200_3Query extends Query {

    //クラス／担任
    function getHrName($model)
    {
        $query  = "SELECT GRADE, HR_CLASS, HR_NAME, ";
        $query .= "       TR_CD1, STAFFNAME ";
        $query .= "FROM   SCHREG_REGD_HDAT T1 ";
        $query .= "       LEFT JOIN STAFF_MST S1 ON S1.STAFFCD=T1.TR_CD1 ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".CTRL_SEMESTER."' AND ";
        $query .= "       GRADE||HR_CLASS = '".$model->hr_name."' ";
        return $query;
    }

    //生徒
    function getSchno($model)
    {
        $query  = "SELECT T1.SCHREGNO, ATTENDNO, S1.NAME ";
        $query .= "FROM   SCHREG_REGD_DAT T1 ";
        $query .= "       LEFT JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO=T1.SCHREGNO ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".CTRL_SEMESTER."' AND ";
        $query .= "       GRADE||HR_CLASS = '".$model->hr_name."' ";
        $query .= "ORDER BY ATTENDNO ";
        return $query;
    }

    //直近データ
    function getList($model)
    {
        $query  = "SELECT SEQNO, DATE(UPDATED) AS UPDATED1, TIME(UPDATED) AS UPDATED2 ";
        $query .= "FROM   ATTEND_PETITION_HDAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        $query .= "ORDER BY UPDATED DESC ";
        return $query;
    }

    //１レコード取得
    function getListRow($model)
    {
        $query  = "SELECT SEQNO, DI_REMARK AS SONOTA,";
        $query .= "       CONTACTER, CALLBACK, FROMDATE, FROMPERIOD, TODATE, TOPERIOD, DI_CD, DI_REMARK ";
        $query .= "FROM   ATTEND_PETITION_HDAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  SEQNO = ".$model->field["SEQNO"]." ";
        $query .= "  AND  SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        return $query;
    }

    //校時
    function getPeriodcd()
    {
        $query  = "SELECT namecd2, name1 ";
        $query .= "FROM   v_name_mst ";
        $query .= "WHERE  year='".CTRL_YEAR."' and namecd1='B001' ";
        $query .= "ORDER BY namecd2 ";
        return $query;
    }

    //勤怠
    function getDicd()
    {
        $query  = "SELECT namecd2, name1 ";
        $query .= "FROM   name_mst ";
        $query .= "WHERE  namecd1='C001' AND namespare3 is not null ";
        $query .= "ORDER BY namespare3, smallint(namecd2) ";
        return $query;
    }

    //理由(備考)
    function getDiremark()
    {
        $query  = "SELECT min(namecd2) as namecd2, name1, rtrim(substr(name1,1,30)) as di_remark ";
        $query .= "FROM   name_mst ";
        $query .= "WHERE  namecd1='C999' and name1<>'' ";
        $query .= "GROUP BY name1 ";
        $query .= "ORDER BY 1 ";
        return $query;
    }

    //受付番号Max値の取得
    function getMaxSeqno()
    {
        $db = Query::dbCheckOut();
        $query  = "SELECT VALUE(MAX(SEQNO),0) + 1 AS SEQNO ";
        $query .= "FROM   ATTEND_PETITION_HDAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $max_seqno = $db->getOne($query);
        Query::dbCheckIn($db);
        return $max_seqno;
    }

    //追加
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /*** 出欠届けヘッダデータ ***/

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQNO"][NUMBER]          = $model->field["SEQNO"];
        $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];
        $data["CONTACTER"][TEXT]     	= str_replace("9","0",$model->field["CONTACTER"]);
        $data["CALLBACK"][TEXT]     	= str_replace("9","0",$model->field["CALLBACK"]);
        $data["FIRSTREGISTER"][TEXT]    = STAFFCD;
        $data["FIRSTDATE"][NUMBER]      = "SYSDATE()";
        $data["FROMDATE"][TEXT]         = str_replace("/","-",$model->field["FROMDATE"]);
        $data["TODATE"][TEXT]           = str_replace("/","-",$model->field["TODATE"]);
        $data["FROMPERIOD"][TEXT]       = $model->field["FROMPERIOD"];
        $data["TOPERIOD"][TEXT]         = $model->field["TOPERIOD"];
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];
        $data["DI_REMARK"][TEXT]        = ($model->field["SONOTA"]!="")?$model->field["SONOTA"]:$model->field["DI_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "attend_petition_hdat");
        $db->query($query);

        /*** 名称マスタ（備考データ） ***/

        if ($model->field["SONOTA"] != "") knjc200_3Query::getInsertNameMst($model, $db);

        /*** 出欠届けデータ ***/

        knjc200_3Query::getInsertAttendPDat($model, $db);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //更新
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /*** 出欠届けヘッダデータ ***/

        $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];
        $data["CONTACTER"][TEXT]     	= str_replace("9","0",$model->field["CONTACTER"]);
        $data["CALLBACK"][TEXT]     	= str_replace("9","0",$model->field["CALLBACK"]);
        $data["FROMDATE"][TEXT]         = str_replace("/","-",$model->field["FROMDATE"]);
        $data["TODATE"][TEXT]           = str_replace("/","-",$model->field["TODATE"]);
        $data["FROMPERIOD"][TEXT]       = $model->field["FROMPERIOD"];
        $data["TOPERIOD"][TEXT]         = $model->field["TOPERIOD"];
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];
        $data["DI_REMARK"][TEXT]        = ($model->field["SONOTA"]!="")?$model->field["SONOTA"]:$model->field["DI_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $where  = "WHERE  YEAR = '".CTRL_YEAR."' ";
        $where .= "  AND  SEQNO = ".$model->field["SEQNO"]." ";

        $query = Query::updateSQL($data, "attend_petition_hdat", $where);
        $db->query($query);

        /*** 名称マスタ（備考データ） ***/

        if ($model->field["SONOTA"] != "") knjc200_3Query::getInsertNameMst($model, $db);

        /*** 出欠届けデータ ***/

        knjc200_3Query::getDeleteAttendPDat($model, $db);
        knjc200_3Query::getInsertAttendPDat($model, $db);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //削除
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM attend_petition_hdat ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  SEQNO = ".$model->field["SEQNO"]." ";
        $db->query($query);

        knjc200_3Query::getDeleteAttendPDat($model, $db);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //名称マスタ(備考)の存在チェック用
    function getExistsDiremark($model)
    {
        $query  = "SELECT COUNT(*) FROM name_mst ";
        $query .= "WHERE  namecd1='C999' and rtrim(substr(name1,1,30)) = '".$model->field["SONOTA"]."' ";
        return $query;
    }

    //名称マスタ(備考)のMax値の取得
    function getMaxNamecd2()
    {
        return "SELECT value(max(smallint(namecd2)),0) + 1 FROM name_mst WHERE namecd1='C999' ";
    }

    //名称マスタ(備考)の追加
    function getInsertNameMst($model, $db)
    {
        if ( $db->getOne(knjc200_3Query::getExistsDiremark($model)) == 0 ) {
                $max_namecd2 = $db->getOne(knjc200_3Query::getMaxNamecd2());
                $data["NAMECD1"][TEXT]          = "C999";
                $data["NAMECD2"][TEXT]          = sprintf("%04d", $max_namecd2);
                $data["NAME1"][TEXT]            = $model->field["SONOTA"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                $query = Query::insertSQL($data, "name_mst");
                $db->query($query);
        }
        return true;
    }

    //出欠届けデータの追加
    function getInsertAttendPDat($model, $db)
    {
        $attenddate = str_replace("/","-",$model->field["FROMDATE"]);//初期値：開始日付
        //開始日付から終了日付までのループ
        for ($i = 1; $attenddate <= str_replace("/","-",$model->field["TODATE"]); $i++) {
            //開始校時から終了校時までのループ
            for ($ip = 0; $ip < get_count($model->arr_period); $ip++) {
                //開始校時＜終了校時の場合
                if ($model->field["FROMDATE"] < $model->field["TODATE"]) {
                    if ($attenddate == str_replace("/","-",$model->field["FROMDATE"])) {
                        if (($model->field["FROMPERIOD"] <= $model->arr_period[$ip]) && 
                            ($model->arr_period[$ip] <= $model->e_period)) {
//echo $attenddate ." " .$model->arr_period[$ip] ."<br>";
                            knjc200_3Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip]);
                        }
                    } else if ($attenddate == str_replace("/","-",$model->field["TODATE"])) {
                        if (($model->s_period <= $model->arr_period[$ip]) && 
                            ($model->arr_period[$ip] <= $model->field["TOPERIOD"])) {
//echo $attenddate ." " .$model->arr_period[$ip] ."<br>";
                            knjc200_3Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip]);
                        }
                    } else {
                        if (($model->s_period <= $model->arr_period[$ip]) && 
                            ($model->arr_period[$ip] <= $model->e_period)) {
//echo $attenddate ." " .$model->arr_period[$ip] ."<br>";
                            knjc200_3Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip]);
                        }
                    }
                //開始校時＝終了校時の場合
                } else {
                    if (($model->field["FROMPERIOD"] <= $model->arr_period[$ip]) && 
                        ($model->arr_period[$ip] <= $model->field["TOPERIOD"])) {
//echo $attenddate ." " .$model->arr_period[$ip] ."<br>";
                            knjc200_3Query::getInsertAttendPDatQuery($model, $db, $attenddate, $model->arr_period[$ip]);
                    }
                }
            }
            $query2 = "VALUES Add_days(date('".str_replace("/","-",$model->field["FROMDATE"])."'), ".$i.")";
            $attenddate = $db->getOne($query2);//次の日
        }

        return true;
    }

    //削除(出欠届けデータ)
    function getDeleteAttendPDat($model, $db)
    {
        $query  = "DELETE FROM attend_petition_dat ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  SEQNO = ".$model->field["SEQNO"]." ";
        $db->query($query);

        return true;
    }

    //追加(出欠届けデータ)
    function getInsertAttendPDatQuery($model, $db, $attenddate, $period)
    {
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEQNO"][NUMBER]          = $model->field["SEQNO"];
        $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];

        $data["ATTENDDATE"][TEXT]       = $attenddate;
        $data["PERIODCD"][TEXT]         = $period;
        $data["DI_CD"][TEXT]            = $model->field["DI_CD"];
        $data["DI_REMARK"][TEXT]        = ($model->field["SONOTA"]!="")?$model->field["SONOTA"]:$model->field["DI_REMARK"];

        $data["EXECUTED"][TEXT]         = "0";

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "attend_petition_dat");
        $db->query($query);

        return true;
    }

}
?>
