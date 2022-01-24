<?php

require_once('for_php7.php');

class knjg045cQuery extends Query {

    //校種
    function getA023($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //日誌取得
    function getDiary($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        return $query;
    }

    //天気取得
    function getWeather($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A006' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    function getMainData($model){
        $query .= " SELECT ";
        $query .= "     T1.SCHOOLCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.DIARY_DATE, ";
        $query .= "     T1.WEATHER, ";
        $query .= "     T1.WEATHER2, ";
        $query .= "     SEQ1.REMARK1 AS SEQ001_REMARK1, ";
        $query .= "     SEQ1.REMARK2 AS SEQ001_REMARK2, ";
        $query .= "     SEQ1.REMARK3 AS SEQ001_REMARK3, ";
        $query .= "     SEQ1.REMARK4 AS SEQ001_REMARK4, ";
        $query .= "     SEQ1.REMARK5 AS SEQ001_REMARK5, ";
        $query .= "     SEQ2.REMARK1 AS SEQ002_REMARK1, ";
        $query .= "     SEQ2.REMARK2 AS SEQ002_REMARK2, ";
        $query .= "     SEQ2.REMARK3 AS SEQ002_REMARK3, ";
        $query .= "     SEQ2.REMARK4 AS SEQ002_REMARK4, ";
        $query .= "     SEQ2.REMARK5 AS SEQ002_REMARK5, ";
        $query .= "     SEQ2.REMARK6 AS SEQ002_REMARK6, ";
        $query .= "     SEQ2.REMARK7 AS SEQ002_REMARK7, ";
        $query .= "     SEQ2.REMARK8 AS SEQ002_REMARK8, ";
        $query .= "     SEQ3.REMARK1 AS SEQ003_REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT SEQ1 ";
        $query .= "     ON T1.SCHOOLCD = SEQ1.SCHOOLCD ";
        $query .= "     AND T1.SCHOOL_KIND = SEQ1.SCHOOL_KIND ";
        $query .= "     AND T1.DIARY_DATE = SEQ1.DIARY_DATE ";
        $query .= "     AND SEQ1.SEQ='001' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT SEQ2 ";
        $query .= "     ON T1.SCHOOLCD = SEQ2.SCHOOLCD ";
        $query .= "     AND T1.SCHOOL_KIND = SEQ2.SCHOOL_KIND ";
        $query .= "     AND T1.DIARY_DATE = SEQ2.DIARY_DATE ";
        $query .= "     AND SEQ2.SEQ='002' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT SEQ3 ";
        $query .= "     ON T1.SCHOOLCD = SEQ3.SCHOOLCD ";
        $query .= "     AND T1.SCHOOL_KIND = SEQ3.SCHOOL_KIND ";
        $query .= "     AND T1.DIARY_DATE = SEQ3.DIARY_DATE ";
        $query .= "     AND SEQ3.SEQ='003' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD='".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND T1.SCHOOL_KIND='".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND T1.DIARY_DATE='".str_replace("/","-",$model->diaryDate)."' ";
        
        return $query;
    }

    function &getDeleteQuery($model, $flg="")
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        //SCHOOL_DIARY_DAT
        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $db->query($query);
        
        //SCHOOL_DIARY_DETAIL_SEQ_DAT
        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DETAIL_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND SEQ IN('001','002','003') ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //メインデータのデータ有無
        $query = knjg045cQuery::getDiary($model);
        $diaryData = $db->getOne($query);
        $data = array();
        $data["WEATHER"][TEXT] = $model->field['WEATHER'];
        $data["WEATHER2"][TEXT] = $model->field['WEATHER2'];
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";
        if ($diaryData == 0) {
            $data["SCHOOLCD"][DATE]                 = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][DATE]              = $model->field["SCHOOL_KIND"];
            $data["DIARY_DATE"][DATE]               = str_replace('/', '-', $model->diaryDate);
            $db->query(Query::insertSQL($data, "SCHOOL_DIARY_DAT"));
        } else {
            
            $where  = " WHERE ";
            $where .= "     SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $where .= "     AND SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."' ";
            $where .= "     AND DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
            
            $db->query(Query::updateSQL($data, "SCHOOL_DIARY_DAT", $where));
        }
        
        //SCHOOL_DIARY_DETAIL_SEQ_DAT
        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DETAIL_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND SEQ IN('001','002','003') ";
        $db->query($query);

        $data = array();
        $data["SEQ"][TEXT] = '001';
        $data["REMARK1"][TEXT] = $model->field['SEQ001_REMARK1'];
        $data["REMARK2"][TEXT] = $model->field['SEQ001_REMARK2'];
        $data["REMARK3"][TEXT] = $model->field['SEQ001_REMARK3'];
        $data["REMARK4"][TEXT] = $model->field['SEQ001_REMARK4'];
        $data["REMARK5"][TEXT] = $model->field['SEQ001_REMARK5'];
        $data["SCHOOLCD"][DATE]                     = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][DATE]                  = $model->field["SCHOOL_KIND"];
        $data["DIARY_DATE"][DATE]                   = str_replace('/', '-', $model->diaryDate);
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";
        
        $db->query(Query::insertSQL($data, "SCHOOL_DIARY_DETAIL_SEQ_DAT"));
        
        $data = array();
        $data["SEQ"][TEXT] = '002';
        $data["REMARK1"][TEXT] = $model->field['SEQ002_REMARK1'];
        $data["REMARK2"][TEXT] = $model->field['SEQ002_REMARK2'];
        $data["REMARK3"][TEXT] = $model->field['SEQ002_REMARK3'];
        $data["REMARK4"][TEXT] = $model->field['SEQ002_REMARK4'];
        $data["REMARK5"][TEXT] = $model->field['SEQ002_REMARK5'];
        $data["REMARK6"][TEXT] = $model->field['SEQ002_REMARK6'];
        $data["REMARK7"][TEXT] = $model->field['SEQ002_REMARK7'];
        $data["REMARK8"][TEXT] = $model->field['SEQ002_REMARK8'];
        $data["SCHOOLCD"][DATE]                     = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][DATE]                  = $model->field["SCHOOL_KIND"];
        $data["DIARY_DATE"][DATE]                   = str_replace('/', '-', $model->diaryDate);
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";
        
        $db->query(Query::insertSQL($data, "SCHOOL_DIARY_DETAIL_SEQ_DAT"));
        
        $data = array();
        $data["SEQ"][TEXT] = '003';
        $data["REMARK1"][TEXT] = $model->field['SEQ003_REMARK1'];
        $data["SCHOOLCD"][DATE]                     = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][DATE]                  = $model->field["SCHOOL_KIND"];
        $data["DIARY_DATE"][DATE]                   = str_replace('/', '-', $model->diaryDate);
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";
        
        $db->query(Query::insertSQL($data, "SCHOOL_DIARY_DETAIL_SEQ_DAT"));
        
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>