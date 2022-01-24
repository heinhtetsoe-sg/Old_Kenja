<?php

require_once('for_php7.php');

class knjg045dQuery extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //日誌取得
    function getDiary($model, $flg="") {
        $query  = " SELECT ";
        if ($flg == "count") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.*, ";
            $query .= "     L001.REMARK1 AS RAIKOU, ";
            $query .= "     L001.REMARK2 AS MATTER, ";
            $query .= "     L002.REMARK3 AS REMARK ";
        }
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DAT T1 ";
        if ($flg == "") {
            $query .= "     LEFT JOIN SCHOOL_DIARY_DETAIL_SEQ_DAT L001 ";
            $query .= "            ON L001.DIARY_DATE  = T1.DIARY_DATE ";
            $query .= "           AND L001.SEQ         = '001' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "       AND L001.SCHOOLCD    = T1.SCHOOLCD ";
                $query .= "       AND L001.SCHOOL_KIND = T1.SCHOOL_KIND ";
            }
            $query .= "     LEFT JOIN SCHOOL_DIARY_DETAIL_SEQ_DAT L002 ";
            $query .= "            ON L002.DIARY_DATE  = T1.DIARY_DATE ";
            $query .= "           AND L002.SEQ         = '002' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "       AND L002.SCHOOLCD    = T1.SCHOOLCD ";
                $query .= "       AND L002.SCHOOL_KIND = T1.SCHOOL_KIND ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }

    //天気取得
    function getWeather($model) {
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

    //行事取得
    function getEventMst($model) {
        $query  = " SELECT DISTINCT";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "         DATA_DIV     = '1' ";
        $query .= "     AND GRADE        = '00' ";
        $query .= "     AND COURSECD     = '0' ";
        $query .= "     AND MAJORCD      = '000' ";
        $query .= "     AND EXECUTEDATE  = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= "     AND HR_CLASS     = '000' ";
        $query .= "     AND HR_CLASS_DIV = '".$model->hr_class_div."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出張者、休暇等情報取得
    function getStaffData($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L2.STAFFNAME_SHOW, ";
        $query .= "     CASE WHEN T1.SEQ BETWEEN '101' AND '120' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE CASE WHEN T1.SEQ BETWEEN '201' AND '220' ";
        $query .= "                    THEN '2' ";
        $query .= "                    ELSE '0' ";
        $query .= "               END ";
        $query .= "     END AS STAFF_DIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DETAIL_SEQ_DAT T1 ";
        $query .= "     INNER JOIN V_STAFF_MST L2 ON L2.STAFFCD = T1.REMARK1 ";
        $query .= "                              AND L2.YEAR = '".CTRL_YEAR."' ";
        $query .= " WHERE ";
        $query .= "     T1.DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    STAFF_DIV, ";
        $query .= "    T1.REMARK1, ";
        $query .= "    T1.SEQ ";

        return $query;
    }

    //職員取得(選択済み)
    function getSelectSubQuery($model, $setDiaryDate)
    {
        $query  = "SELECT ";
        $query .= "    T1.STAFFCD AS VALUE, ";
        $query .= "    T1.STAFFCD || ':' || T1.STAFFNAME_SHOW AS LABEL ";
        $query .= "FROM ";
        $query .= "    V_STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.SECTIONCD ";
        return $query;
    }

    //SCHOOL_DIARY_DETAIL_SEQ_DAT 取得
    function getSchoolDiaryDetailSeqDat($model, $field, $setDiaryDate, $setStaffDiv, $staffCd, $seq="") {
        $query  = "SELECT ";
        $query .= "    ".$field." ";
        $query .= "FROM ";
        $query .= "    SCHOOL_DIARY_DETAIL_SEQ_DAT T1 ";
        $query .= " WHERE ";
        $query .= "    T1.DIARY_DATE  = '".str_replace('/', '-', $setDiaryDate)."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if($seq == ""){
            $query .= "    AND T1.REMARK1 = '".$staffCd."' ";
            if ($setStaffDiv == '1') {
                $query .= " AND T1.SEQ BETWEEN '101' AND '120' ";
            } else if ($setStaffDiv == '2') {
                $query .= " AND T1.SEQ BETWEEN '201' AND '220' ";
            }
        } else {
            $query .= "    AND T1.SEQ = '".$seq."' ";
        }

        $query .= " ORDER BY ";
        $query .= "    T1.REMARK1, ";
        $query .= "    T1.SEQ ";
        return $query;
    }


    /**
     * INSERT
     */
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        //SCHOOL_DIARY_DAT
        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]                  = SCHOOLKIND;
        }
        $data["DIARY_DATE"][DATE]                   = $model->diaryDate;
        $data["WEATHER"][TEXT]                      = $model->field["WEATHER"];
        $data["WEATHER2"][TEXT]                     = $model->field["WEATHER2"];
        $data["NEWS"][TEXT]                         = $model->field["NEWS"];
        $data["STAFFNEWS"][TEXT]                    = $model->field["STAFFNEWS"];
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";
        $query = Query::insertSQL($data, "SCHOOL_DIARY_DAT");
        $db->query($query);

        //SCHOOL_DIARY_DETAIL_SEQ_DAT
        $seq = array("001","002");
        for ($i = 0; $i < sizeof($seq); $i++) {

            if($seq[$i] == "001"){
                $remark1 = $model->field["RAIKOU"]; //来校者
                $remark2 = $model->field["MATTER"]; //用件
                $remark3 = "";
            } else {
                //その他
                $remark1 = "";
                $remark2 = "";
                $remark3 = $model->field["REMARK"]; //その他
            }
        
            $data = array();
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]                  = SCHOOLKIND;
            }
            $data["DIARY_DATE"][DATE]                   = $model->diaryDate;
            $data["SEQ"][TEXT]                          = $seq[$i];
            $data["REMARK1"][TEXT]                      = $remark1;
            $data["REMARK2"][TEXT]                      = $remark2;
            $data["REMARK3"][TEXT]                      = $remark3;
            $data["REGISTERCD"][TEXT]                   = STAFFCD;
            $data["UPDATED"][FUNC]                      = "SYSDATE()";
            $query = Query::insertSQL($data, "SCHOOL_DIARY_DETAIL_SEQ_DAT");
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getDeleteQuery($model, $flg="") {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        //SCHOOL_DIARY_DAT
        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query($query);

        //SCHOOL_DIARY_DETAIL_SEQ_DAT
        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DETAIL_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        if($flg != "delete"){
            $query .= "     AND SEQ IN ('001','002') ";
        }
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //更新
    function getUpdateDetailSeq($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //メインデータのデータ有無
        $query = knjg045dQuery::getDiary($model, "count");
        $diaryData = $db->getOne($query);
        if ($diaryData == 0) {
            //SCHOOL_DIARY_DAT
            $data = array();
            if ($model->Properties["useSchool_KindField"] == "1") {
                $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]                  = SCHOOLKIND;
            }
            $data["DIARY_DATE"][DATE]                   = $model->setDiaryDate;
            $data["REGISTERCD"][TEXT]                   = STAFFCD;
            $data["UPDATED"][FUNC]                      = "SYSDATE()";
            $query = Query::insertSQL($data, "SCHOOL_DIARY_DAT");
            $db->query($query);
        }

        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DETAIL_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->setDiaryDate)."' ";
        if ($model->setStaffDiv === '1'){
            $query .= "     AND SEQ BETWEEN '101' AND '120' ";
        } else if ($model->setStaffDiv === '2'){
            $query .= "     AND SEQ BETWEEN '201' AND '220' ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        if ($model->setStaffDiv === '1' || $model->setStaffDiv === '2') {
            for ($i = 1; $i <= 20; $i++) {

                //入力された行のみ処理する
                if ($model->fields["STAFF"][$i] == "") {
                    continue;
                }

                $seq = sprintf("%03d", $i);

                if ($model->setStaffDiv === '1'){
                    //出張先
                    $seq = sprintf("%03d", intval($seq) + 100); //SEQ 101～120
                    $remark1 = $model->fields["STAFF"][$i];     //STAFFCD
                    $remark2 = $model->fields["REMARK2"][$i];   //出張先
                    $remark3 = $model->fields["REMARK3"][$i];   //用件
                    $remark4 = "";                              //
                } else if ($model->setStaffDiv === '2'){
                    //休暇等
                    $seq = sprintf("%03d", intval($seq) + 200); //SEQ 201～220
                    $remark1 = $model->fields["STAFF"][$i];     //STAFFCD
                    $remark2 = $model->fields["REMARK2"][$i];   //休暇区分
                    $remark3 = $model->fields["REMARK3"][$i];   //事由
                    $remark4 = $model->fields["REMARK4"][$i];   //時間
                }

                $data = array();
                if ($model->Properties["useSchool_KindField"] == "1") {
                    $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]                  = SCHOOLKIND;
                }
                $data["DIARY_DATE"][DATE]                   = $model->setDiaryDate;
                $data["SEQ"][TEXT]                          = $seq;
                $data["REMARK1"][TEXT]                      = $remark1;
                $data["REMARK2"][TEXT]                      = $remark2;
                $data["REMARK3"][TEXT]                      = $remark3;
                $data["REMARK4"][TEXT]                      = $remark4;
                $data["REGISTERCD"][TEXT]                   = STAFFCD;
                $data["UPDATED"][FUNC]                      = "SYSDATE()";

                $query = Query::insertSQL($data, "SCHOOL_DIARY_DETAIL_SEQ_DAT");
                $db->query($query);
            }//for
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>