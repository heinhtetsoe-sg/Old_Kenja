<?php
class knjj521query extends Query
{

    //年組
    public function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.GRADE IN (SELECT ";
                $query .= "                             REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                             AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.GRADE IN (SELECT ";
            $query .= "                             REGD_G.GRADE ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //日付取得
    public function getScoreDate($model)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR                = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER            = '".CTRL_SEMESTER."' AND ";
        $query .= "         GRADE || HR_CLASS   = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     MIN(T1.DATE) AS DATE ";
        $query .= " FROM ";
        $query .= "     SPORTS_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO IN (SELECT W1.SCHREGNO FROM SCHNO W1) ";

        return $query;
    }

    //体力種目マスタ
    public function getSportsItemMst()
    {
        $query  = " SELECT ";
        $query .= "     ITEMCD, ";
        $query .= "     ITEMABBV, ";
        $query .= "     UNIT ";
        $query .= " FROM ";
        $query .= "     SPORTS_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD < '900' ";
        $query .= " ORDER BY ";
        $query .= "     ITEMCD ";

        return $query;
    }

    //リスト
    public function getList($model, $item_key)
    {
        $query  = " WITH SPORTS_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        // "1" => "001"
        foreach ($item_key as $lenNo => $itemCd) {
            $query .= "    ,MAX(CASE WHEN ITEMCD = '{$itemCd}' THEN RECORD ELSE NULL END) AS RECORD".$lenNo;
        }
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN TOTAL ELSE NULL END) AS TOTAL ";
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN VALUE ELSE NULL END) AS VALUE ";
        $query .= "     FROM ";
        $query .= "         SPORTS_SCORE_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), SPORTS_INQUIRY_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $inquiryCdArray = range(1, $model->maxInquiryNum);
        foreach ($inquiryCdArray as $inquiryCd) {
            $query .= "    ,MAX(CASE WHEN INQUIRYCD = '{$inquiryCd}' THEN VALUE ELSE NULL END) AS INQUIRY".$inquiryCd;
        }
        $query .= "     FROM ";
        $query .= "         SPORTS_SCORE_INQUIRY_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "    ,T1.ATTENDNO ";
        $query .= "    ,L1.SEX ";
        $query .= "    ,L1.BIRTHDAY ";
        $query .= "    ,L1.NAME_SHOW ";
        $query .= "    ,L1.GRD_DATE ";
        $date = str_replace("/", "-", CTRL_DATE);
        $query .= "    ,CASE WHEN VALUE(L1.GRD_DATE,'9999-12-31') > '{$date}' THEN 'no' ELSE 'yes' END AS JOSEKI ";
        // "1" => "001"
        foreach ($item_key as $lenNo => $itemCd) {
            $query .= ",L2.RECORD{$lenNo}";
        }
        $query .= "    ,L2.TOTAL ";
        $query .= "    ,L2.VALUE ";
        foreach ($inquiryCdArray as $inquiryCd) {
            $query .= ",L3.INQUIRY".$inquiryCd;
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SPORTS_SCORE L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SPORTS_INQUIRY_SCORE L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR                 = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER             = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //項目別得点表マスタ取得
    public function getSportsItemScoreBaseMst()
    {
        $query .= "     SELECT ";
        $query .= "         ITEMCD, ";
        $query .= "         SEX, ";
        $query .= "         SCORE_LEVEL, ";
        $query .= "         RECORD_LOW, ";
        $query .= "         RECORD_HIGH ";
        $query .= "     FROM ";
        $query .= "         SPORTS_ITEM_SCORE_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //得点取得
    public function getSportsItemScore($record, $scoreBaseMst)
    {
        foreach ($scoreBaseMst as $row) {
            $recordLow = ($row["RECORD_LOW"] != "") ? floatval($row["RECORD_LOW"]) : 0.0;
            $recordHigh = ($row["RECORD_HIGH"] != "") ? floatval($row["RECORD_HIGH"]) : 1000.0;
            if ($recordLow <= $record && $record <= $recordHigh) {
                return $row["SCORE_LEVEL"];
            }
        }
        return null;
    }

    //総合評価基準マスタ取得
    public function getSportsTotalValueBaseMst()
    {
        $query .= "     SELECT ";
        $query .= "         AGE, ";
        $query .= "         SEX, ";
        $query .= "         TOTAL_LEVEL, ";
        $query .= "         TOTAL_MARK, ";
        $query .= "         TOTAL_SCORE_LOW, ";
        $query .= "         TOTAL_SCORE_HIGH ";
        $query .= "     FROM ";
        $query .= "         SPORTS_TOTAL_VALUE_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //総合評価取得
    public function getSportsTotalValueScore($totalScore, $totalValueBaseMst)
    {
        foreach ($totalValueBaseMst as $row) {
            $totalLow = ($row["TOTAL_SCORE_LOW"] != "") ? $row["TOTAL_SCORE_LOW"]: "0";
            $totalHigh = ($row["TOTAL_SCORE_HIGH"] != "") ? $row["TOTAL_SCORE_HIGH"]: "1000";
            if ($totalLow <= $totalScore && $totalScore <= $totalHigh) {
                return $row["TOTAL_MARK"];
            }
        }
    }

    /* データ更新処理 */
    public function update($model)
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //変数
        $year = CTRL_YEAR;
        $date = ($model->field["DATE"]) ? str_replace("/", "-", $model->field["DATE"]) : "";

        //種目得点マスタ取得
        $tmpArray1 = array();
        $query = knjj521Query::getSportsItemScoreBaseMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmpArray1[$row["SEX"]."-".$row["ITEMCD"]][] = $row;
        }
        //総合評価基準マスタ取得
        $tmpArray2 = array();
        $query = knjj521Query::getSportsTotalValueBaseMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmpArray2[$row["AGE"]][] = $row;
        }

        //生徒でループ
        for ($gyoNo = 0; $gyoNo < count($model->data["SCHREGNO"]); $gyoNo++) {
            //変数
            $schNo = $model->data["SCHREGNO"][$gyoNo];
            //削除
            $query  = " DELETE FROM ";
            $query .= "     SPORTS_SCORE_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$year}' AND ";
            $query .= "     SCHREGNO = '{$schNo}' ";
            $db->query($query);

            //種目でループ
            $calcSumScore = 0;
            $calcCnt = 0;
            foreach ($model->data["RECORD"] as $lenNo => $itemCd) {
                $scoreBaseMst = $tmpArray1[$model->fields["SEX"][$gyoNo]."-".$itemCd];
                $calcScore = 0;
                if ($model->fields["RECORD".$lenNo][$gyoNo] != "" && $scoreBaseMst) {
                    $calcScore = knjj521Query::getSportsItemScore($model->fields["RECORD".$lenNo][$gyoNo], $scoreBaseMst);
                    $calcCnt++;
                }

                $data = array();
                $data["YEAR"][TEXT]         = $year;
                $data["SCHREGNO"][TEXT]     = $schNo;
                $data["ITEMCD"][TEXT]       = $itemCd;
                $data["DATE"][TEXT]         = $date;
                $data["RECORD"][NUMBER]     = $model->fields["RECORD".$lenNo][$gyoNo];
                if ($model->cmd == "calc") {
                    $data["SCORE"][NUMBER]      = $calcScore;
                }
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][NUMBER]    = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "SPORTS_SCORE_DAT");
                $db->query($query);

                $calcSumScore += $calcScore;
            }

            $age = intval((str_replace("-", "", $date) - str_replace("-", "", $model->fields["BIRTHDAY"][$gyoNo])) / 10000);
            $totalValueBaseMst = $tmpArray2[$age];

            $calcTotalValueMark = "";
            if ($totalValueBaseMst) {
                $calcTotalValueMark = knjj521Query::getSportsTotalValueScore($calcSumScore, $totalValueBaseMst);
            }

            //総合判定
            $data = array();
            $data["YEAR"][TEXT]         = $year;
            $data["SCHREGNO"][TEXT]     = $schNo;
            $data["ITEMCD"][TEXT]       = "999";
            $data["DATE"][TEXT]         = $date;
            $data["VALUE"][TEXT]        = ($model->cmd != "calc") ? $model->fields["VALUE"][$gyoNo] : ($calcCnt >= 1 ? $calcTotalValueMark : "");
            $data["TOTAL"][TEXT]        = ($model->cmd != "calc") ?  $model->fields["TOTAL"][$gyoNo] : ($calcCnt >= 1 ? $calcSumScore : "");
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "SPORTS_SCORE_DAT");
            $db->query($query);

            //削除
            $query  = " DELETE FROM ";
            $query .= "     SPORTS_SCORE_INQUIRY_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$year}' AND ";
            $query .= "     SCHREGNO = '{$schNo}' ";
            $db->query($query);
            //問診項目でループ
            $inquiryCdArray = range(1, $model->maxInquiryNum);
            foreach ($inquiryCdArray as $inquiryCd) {
                $data = array();
                $data["YEAR"][TEXT]         = $year;
                $data["SCHREGNO"][TEXT]     = $schNo;
                $data["INQUIRYCD"][NUMBER]  = $inquiryCd;
                $data["VALUE"][NUMBER]      = $model->fields["INQUIRY".$inquiryCd][$gyoNo];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][NUMBER]    = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "SPORTS_SCORE_INQUIRY_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
