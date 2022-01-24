<?php

require_once('for_php7.php');

class knje400aQuery extends Query {
    //教育委員会チェック
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }

    //データのリストを取得
    function getList($model) {
        $query  = " WITH SCHOOL_COUNT_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     DOC_NUMBER, ";
        $query .= "     COUNT(*) AS SCHOOL_COUNT ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR= '{$model->year}' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     DOC_NUMBER ";
        $query .= " ) ";    
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.DOC_NUMBER, ";
        $query .= "     CASE WHEN T1.TRANSMISSION_DATE IS NOT NULL THEN '済'  ";
        $query .= "     ELSE NULL END AS TRANSMISSION_STATUS, ";
        $query .= "     T1.SUBMISSION_DATE, ";
        $query .= "     T1.NOTICE_TITLE, ";
        $query .= "     T1.WRITING_DATE, ";
        $query .= "     VALUE(L1.SCHOOL_COUNT, 0) AS SCHOOL_COUNT ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT T1 ";
        $query .= "     LEFT JOIN SCHOOL_COUNT_DATA L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                   AND L1.DOC_NUMBER = T1.DOC_NUMBER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //対象データを取得
    function getRow($model) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT T1";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND DOC_NUMBER = ".$model->docNumber." ";

        return $query;
    }

    //対象PRG
    function getPrg($model) {
        $query  = " SELECT ";
        $query .= "     NAME3 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E045' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //年度
    function getYear() {
        $query  = " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS VALUE, ";
        $query .= "     '".CTRL_YEAR."' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC";

        return $query;
    }
    
    //通達に選択した学校データ
    function getSelectSchoolData($model)
    {
        $query  = " SELECT ";
        $query .= "     CASE WHEN T1.ANSWER_FLG IS NOT NULL THEN '【済】'  ";
        $query .= "     ELSE '　　　' END AS FLG_NAME, ";
        $query .= "     T1.EDBOARD_SCHOOLCD, ";
        $query .= "     T2.EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     LEFT JOIN EDBOARD_SCHOOL_MST T2 ON T2.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR= '{$model->year}' ";
        $query .= " AND T1.DOC_NUMBER = ".$model->docNumber." ";    
        $query .= " ORDER BY ";
        $query .= "     T1.EDBOARD_SCHOOLCD ";

        return $query;
    }

    //対象学校データ
    function getSchoolData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     '　　　' AS FLG_NAME, ";
        $query .= "     T1.EDBOARD_SCHOOLCD, ";
        $query .= "     T1.EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST T1  ";
        if ($model->docNumber) {
            $query .= " WHERE ";
            $query .= "     T1.EDBOARD_SCHOOLCD NOT IN (";
            $query .= "                 SELECT ";
            $query .= "                     W1.EDBOARD_SCHOOLCD ";
            $query .= "                 FROM ";
            $query .= "                     AFT_SEARCH_REPORT_SCHOOL_DAT W1 ";
            $query .= "                 WHERE ";
            $query .= "                     W1.YEAR= '{$model->year}' ";
            $query .= "                 AND W1.DOC_NUMBER = ".$model->docNumber." ";    
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EDBOARD_SCHOOLCD ";

        return $query;
    }

    //指定年度の文書番号の最大値を取得
    function getMaxNumber($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(DOC_NUMBER) AS NUMBER ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR= '{$model->year}' ";

        return $query;
    }
    
    //AFT_SEARCH_REPORT_DATの更新処理(更新、学校へ通達)
    function &getUpdateMainQuery(&$db, $model) {
        $data = array();
        //更新時
        if ($model->cmd === 'update') {
            $data["SUBMISSION_DATE"][DATE]      = str_replace("/","-",$model->field["SUBMISSION_DATE"]);
            $data["NOTICE_TITLE"][TEXT]         = $model->field["NOTICE_TITLE"];
            $data["NOTICE_MESSAGE"][TEXT]       = $model->field["NOTICE_MESSAGE"];
            $data["VIEWING_PERIOD_FROM"][DATE]  = str_replace("/","-",$model->field["VIEWING_PERIOD_FROM"]);
            $data["VIEWING_PERIOD_TO"][DATE]    = str_replace("/","-",$model->field["VIEWING_PERIOD_TO"]);
            $data["WRITING_DATE"][DATE]         = str_replace("/","-",$model->field["WRITING_DATE"]);
            $data["REQUEST_ANSWER_FLG"][TEXT]   = $model->field["REQUEST_ANSWER_FLG"];
            $data["REQUEST_ANSWER_PRG"][TEXT]   = $model->field["REQUEST_ANSWER_PRG"];
        //通達時のみ
        } else {
            $data["TRANSMISSION_DATE"][DATE]    = date("Y-m-d");
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";
        $where  = " WHERE ";
        $where .= "     YEAR= '{$model->year}' ";
        $where .= " AND DOC_NUMBER = ".$model->docNumber." ";    
        
        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_DAT", $where);
        $db->query($query);
        
        return;
    }

    //AFT_SEARCH_REPORT_DATの更新処理(追加)
    function &getInsertMainQuery(&$db, $model) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["DOC_NUMBER"][NUMBER]         = $model->maxNumber;
        $data["SUBMISSION_DATE"][DATE]      = str_replace("/","-",$model->field["SUBMISSION_DATE"]);
        $data["NOTICE_TITLE"][TEXT]         = $model->field["NOTICE_TITLE"];
        $data["NOTICE_MESSAGE"][TEXT]       = $model->field["NOTICE_MESSAGE"];
        $data["VIEWING_PERIOD_FROM"][DATE]  = str_replace("/","-",$model->field["VIEWING_PERIOD_FROM"]);
        $data["VIEWING_PERIOD_TO"][DATE]    = str_replace("/","-",$model->field["VIEWING_PERIOD_TO"]);
        $data["WRITING_DATE"][DATE]         = str_replace("/","-",$model->field["WRITING_DATE"]);
        $data["REQUEST_ANSWER_FLG"][TEXT]   = $model->field["REQUEST_ANSWER_FLG"];
        $data["REQUEST_ANSWER_PRG"][TEXT]   = $model->field["REQUEST_ANSWER_PRG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";
        
        $query = Query::insertSQL($data, "AFT_SEARCH_REPORT_DAT");
        $db->query($query);
        
        return;
    }

    //AFT_SEARCH_REPORT_SCHOOL_DATの更新処理(Delete Insert)
    function &getUpdateSchoolQuery(&$db, $selectdata, $model) {
        //学校データの削除
        knje400aQuery::getDeleteSchoolQuery($db, $model);
        //選択した学校データの更新
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            //更新
            knje400aQuery::updateAftSearchReportSchool($db, $model, $selectdata[$i]);
        }
        return;
    }

    //選択したデータの更新処理(更新、追加兼用)
    function updateAftSearchReportSchool($db, $model, $selectdata) {
        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        if ($model->cmd === 'update') {
            $data["DOC_NUMBER"][NUMBER]     = $model->docNumber;
        } else {
            $data["DOC_NUMBER"][NUMBER]     = $model->maxNumber;
        }
        $data["EDBOARD_SCHOOLCD"][TEXT] = $selectdata;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT");
        $db->query($query);
        
        return;
    }

    //削除処理(AFT_SEARCH_REPORT_DAT)
    function &getDeleteMainQuery(&$db, $model) {
        $query  = " DELETE FROM ";
        $query .= "     AFT_SEARCH_REPORT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR= '{$model->year}' ";
        $query .= " AND DOC_NUMBER= ".$model->docNumber." ";
        $db->query($query);
        
        return;
    }

    //削除処理(AFT_SEARCH_REPORT_SCHOOL_DAT)
    function &getDeleteSchoolQuery(&$db, $model) {
        $query  = " DELETE FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR= '{$model->year}' ";
        if ($model->cmd === 'add') {
            $query .= " AND DOC_NUMBER= ".$model->maxNumber." ";
        } else {
            $query .= " AND DOC_NUMBER= ".$model->docNumber." ";
        }
        $db->query($query);

        return;
    }
    
}
?>
