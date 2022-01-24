<?php

require_once('for_php7.php');

class knjz220fQuery extends Query {

    //評定マスタ情報抽出
    function selectQuerycnt($model)
    {
        $query  =  " SELECT COUNT(*) AS CNT FROM ASSESS_AVG_MST ";
        $query .=  " WHERE YEAR = '".$model->year."'";

        return $query;
    }

    //評定マスタ情報抽出
    function selectQueryAvg($model)
    {
        $query  =  " SELECT DISTINCT ASSESSAVG FROM ASSESS_AVG_MST ";
        $query .=  " WHERE YEAR = '".$model->year."'";

        return $query;
    }

    //年度取得
    function getYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //評定マスタ情報抽出
    function selectQuery($model)
    {
        $query  =  " SELECT * FROM ASSESS_AVG_MST ";
        $query .=  " WHERE YEAR = '".$model->year."'";
        $query .=  " ORDER BY ";
        $query .=  "    ASSESSLEVEL ";

        return $query;
    }

    //Insert作成
    function &getInsertQuery($model)
    {        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //削除
        $query  =  " DELETE FROM ASSESS_AVG_MST ";
        $query .=  " WHERE YEAR = '".$model->year."' ";
        $db->query($query);
        
        //登録
        $ii = $model->level;
        for($i=1; $i<=$ii; $i++)
        {
            $assesshigh = $model->field["ASSESSHIGH".$i];
            if($i != $ii){
                $ASSESSHIGH = "ASSESSLOW".($i + 1);
                $assesshigh = ($model->field[$ASSESSHIGH] - 1);
            }       

            $data = array();
            $data["YEAR"][TEXT]             = $model->year;
            $data["ASSESSAVG"][NUMBER]      = $model->avg;            
            $data["ASSESSLEVEL"][NUMBER]    = $i;
            $data["ASSESSMARK"][TEXT]       = $model->field["ASSESSMARK".$i];
            $data["ASSESSLOW"][NUMBER]      = $model->field["ASSESSLOW".$i];
            $data["ASSESSHIGH"][NUMBER]     = $assesshigh;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]  = "sysdate()";
            $query = Query::insertSQL($data, "ASSESS_AVG_MST");

            $db->query($query);
        }

        $overBig = 1;
        $db->commit();
        Query::dbCheckIn($db);

        return $overBig;
    }

    //Delete 削除処理
    function &getDeleteQuery($model)
    {        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //削除
        $query  =  " DELETE FROM ASSESS_AVG_MST ";
        $query .=  " WHERE YEAR = '".$model->year."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    //前年度データ取得
    function getCopyData($year, $flg) {
        $query  =  " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        } else {
            $query .=  "    * ";
        }
        $query .=  " FROM ";
        $query .=  "    ASSESS_AVG_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$year."' ";

        return $query;
    }

    //前年度コピー処理
    function &getCopyQuery($model, $pre_year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ";
        $query .=  "    ASSESS_AVG_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$model->year."' ";
        $db->query($query);

        //前年度データ取得
        $query = knjz220fQuery::getCopyData($pre_year, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]          = $model->year;
            $data["ASSESSAVG"][NUMBER]   = $row["ASSESSAVG"];
            $data["ASSESSLEVEL"][NUMBER] = $row["ASSESSLEVEL"];
            $data["ASSESSMARK"][TEXT]    = $row["ASSESSMARK"];
            $data["ASSESSLOW"][NUMBER]   = $row["ASSESSLOW"];
            $data["ASSESSHIGH"][NUMBER]  = $row["ASSESSHIGH"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $query = Query::insertSQL($data, "ASSESS_AVG_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

}
?>
