<?php

require_once('for_php7.php');

class knjg045bQuery extends Query {

    //年度取得
   function getYear($model, $year="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }
    
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //学期開始、終了日取得
    function getSEdate($semesName) {
        $query  = " SELECT DISTINCT ";
        if ($semesName == "SDATE") {
            $query .= "     SDATE ";
        } else {
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER = '9' ";

        return $query;
    }

    //左側のlistを作るためのデータを取得
    function getList($model) {
        $query .= " SELECT ";
        $query .= "     T1.SCHOOLCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.DIARY_DATE, ";
        $query .= "     COUNT(T2.SCHOOLCD) as CNT ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT T2 ";
        $query .= "     ON T1.SCHOOLCD=T2.SCHOOLCD ";
        $query .= "     AND T1.SCHOOL_KIND=T2.SCHOOL_KIND ";
        $query .= "     AND T1.DIARY_DATE=T2.DIARY_DATE ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND='".$model->schoolkind."' ";
        $query .= "     AND T1.DIARY_DATE>='".$model->sDate."' ";
        $query .= "     AND T1.DIARY_DATE<='".$model->eDate."' ";
        $query .= "     AND T1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."'";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHOOLCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.DIARY_DATE ";
        $query .= " ORDER BY ";
        $query .= "     T1.DIARY_DATE ".$model->sort." ";

        return $query;
    }

    function getSEQDat($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolkind2."' ";
        if($model->diarydate !=''){
            $query .= "    AND DIARY_DATE  = '".$model->diarydate."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }
    function getSEQDatDetail($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".$model->schoolcd."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolkind2."' ";
        if($model->diarydate !=''){
            $query .= "    AND DIARY_DATE  = '".$model->diarydate."' ";
        }
        $query .= "     AND SEQ    = '".$model->seq."' ";

        return $query;
    }

    function getSEQMax($model, $schoolkind, $diarydate)
    {
        $query .= " SELECT ";
        $query .= "     max(SEQ) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$schoolkind."' ";
        $query .= "     AND T1.DIARY_DATE= '".$diarydate."' ";
        
        return $query;
    }

    function getCntDiary($model, $schoolkind, $diarydate) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '".$schoolkind."' ";
        $query .= "     AND DIARY_DATE  = '".$diarydate."' ";
        
        return $query;
    }

    function getCntDiarySql($model, $schoolkind, $diarydate) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND = '".$schoolkind."' ";
        $query .= "     AND DIARY_DATE  = '".$diarydate."' ";
        
        return $query;
    }

        
    //追加
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SCHOOLCD"][TEXT]      = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]   = $model->field["SCHOOL_KIND"];
        $data["DIARY_DATE"][TEXT]    = str_replace("/","-",$model->field["DIARY_DATE"]);
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $query = knjg045bQuery::getCntDiary($model,$model->field["SCHOOL_KIND"],str_replace("/","-",$model->field["DIARY_DATE"]));
        $diaryCnt = $db->getOne($query);
        if ($diaryCnt < 1) {
            $query = Query::insertSQL($data, "SCHOOL_DIARY_DAT");
        } else {
            $where  = " WHERE SCHOOLCD    = '" .sprintf("%012d", SCHOOLCD)."' AND ";
            $where .= "       SCHOOL_KIND = '" .$model->field["SCHOOL_KIND"]."' AND ";
            $where .= "       DIARY_DATE = '" .str_replace('/','-',$this->field['DIARY_DATE'])."' ";

            $query = Query::updateSQL($data, "SCHOOL_DIARY_DAT", $where);
        }
        $db->query($query);

        $query = knjg045bQuery::getSEQMax($model,$model->field["SCHOOL_KIND"],str_replace("/","-",$model->field["DIARY_DATE"]));
        $seq = $db->getOne($query);
        if(empty($seq)){
            $seq = '001';
        } else {
            $seq = $seq+1;
        }

        $data = array();
        $data["SCHOOLCD"][TEXT]      = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]   = $model->field["SCHOOL_KIND"];
        $data["DIARY_DATE"][TEXT]    = str_replace("/","-",$model->field["DIARY_DATE"]);
        $data["SEQ"][TEXT]           = sprintf('%03d', $seq);
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DIARY_DETAIL_SEQ_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //更新
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $where  = " WHERE SCHOOLCD    = '" .sprintf("%012d", SCHOOLCD)."' AND ";
        $where .= "       SCHOOL_KIND = '" .$model->field["SCHOOL_KIND"]."' AND ";
        $where .= "       DIARY_DATE = '" .str_replace('/','-',$this->field['DIARY_DATE'])."' ";

        $query = Query::updateSQL($data, "SCHOOL_DIARY_DAT", $where);
        $db->query($query);

        $data = array();
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $where  = " WHERE SCHOOLCD    = '" .sprintf("%012d", SCHOOLCD)."' AND ";
        $where .= "       SCHOOL_KIND = '" .$model->field["SCHOOL_KIND"]."' AND ";
        $where .= "       DIARY_DATE = '" .str_replace('/','-',$this->field['DIARY_DATE'])."' AND ";
        $where .= "       SEQ         = '" .$model->field['SEQ']."'";

        $query = Query::updateSQL($data, "SCHOOL_DIARY_DETAIL_SEQ_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
    
    //削除
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query = knjg045bQuery::getCntDiarySql($model,$model->field["SCHOOL_KIND"],str_replace("/","-",$model->field["DIARY_DATE"]));
        $diaryCnt = $db->getOne($query);
        if ($diaryCnt == 1) {
            $query  = "  DELETE FROM ";
            $query .= "     SCHOOL_DIARY_DAT ";
            $query .= "  WHERE";
            $query .= "       SCHOOLCD    = '" .sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "       SCHOOL_KIND = '" .$model->field["SCHOOL_KIND"]."' AND ";
            $query .= "       DIARY_DATE  = '" .str_replace('/','-',$this->field['DIARY_DATE'])."' ";
            $db->query($query);
        }

        $query  = "  DELETE FROM ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT ";
        $query .= "  WHERE";
        $query .= "       SCHOOLCD    = '" .sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "       SCHOOL_KIND = '" .$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "       DIARY_DATE  = '" .str_replace('/','-',$this->field['DIARY_DATE'])."' AND ";
        $query .= "       SEQ         = '" .$model->field['SEQ']."'";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

}
?>
