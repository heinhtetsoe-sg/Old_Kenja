<?php

require_once('for_php7.php');

class knjp855Query extends Query
{
    //年組取得（権限チェック）
    public function getGradeHrClass($model, $div = "")
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR ";
        $query .= "           AND T1.GRADE = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND T2.SCHOOL_KIND = '{$model->schoolKind}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        if ($div != "GRADE" && $model->field["GRADE"] != "ALL") {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課外費項目一覧
    public function getKagaihiList($model)
    {
        $query .= "  SELECT ";
        $query .= "     MMST.COLLECT_L_CD || MMST.COLLECT_M_CD AS VALUE, ";
        $query .= "     MMST.COLLECT_L_CD || MMST.COLLECT_M_CD || '：' || MMST.COLLECT_M_NAME AS LABEL, ";
        $query .= "     MMST.COLLECT_M_NAME, ";
        $query .= "     MMST.SCHOOL_KIND ";
        $query .= "  FROM ";
        $query .= "      COLLECT_M_MST MMST ";
        $query .= "  WHERE ";
        $query .= "          MMST.SCHOOLCD      = '{$model->schoolCd}' ";
        if ($model->field["GRADE"] != "ALL") {
            $query .= "      AND MMST.SCHOOL_KIND   = '{$model->schoolKind}' ";
        }
        $query .= "      AND MMST.YEAR          = '{$model->year}' ";
        $query .= "      AND EXISTS ( ";
        $query .= "          SELECT ";
        $query .= "              'X' ";
        $query .= "          FROM ";
        $query .= "              V_NAME_MST P013 ";
        $query .= "          WHERE ";
        $query .= "              P013.YEAR        = MMST.YEAR ";
        $query .= "              AND P013.NAMECD1 = 'P013' ";
        $query .= "              AND MMST.COLLECT_L_CD || MMST.COLLECT_M_CD BETWEEN P013.NAMESPARE1 AND P013.NAMESPARE2 ";
        $query .= "      ) ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //入金パターンデータ取得
    public function getPatternList($model, $collect_pattern_cd = "")
    {
        $query  = " SELECT DISTINCT ";
        if ($collect_pattern_cd) {
            $query .= "     * ";
        } else {
            $query .= "     COLLECT_PATTERN_CD AS VALUE, ";
            $query .= "     COLLECT_PATTERN_CD || ':' || COLLECT_PATTERN_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->year}' ";
        $query .= "     AND SCHOOLCD    = '{$model->schoolCd}' ";
        if ($model->field["GRADE"] != "ALL") {
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        }
        if ($collect_pattern_cd) {
            $query .= " AND COLLECT_PATTERN_CD = '{$collect_pattern_cd}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //CSV出力
    public function selectMainQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "      REGD.GRADE, ";
        $query .= "      REGD.HR_CLASS, ";
        $query .= "      REGD.ATTENDNO, ";
        $query .= "      REGD.SCHREGNO, ";
        $query .= "      BASE.NAME ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT REGD ";
        $query .= "      LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "          ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "  WHERE ";
        $query .= "          YEAR     = '{$model->year}' ";
        $query .= "      AND SEMESTER = '{$model->semester}' ";
        if ($model->field["GRADE"] != "ALL") {
            $query .= "      AND GRADE    = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["HR_CLASS"] != "ALL") {
            $query .= "      AND GRADE || '-' || HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      GRADE, ";
        $query .= "      HR_CLASS, ";
        $query .= "      ATTENDNO ";

        return $query;
    }

    //存在チェック(学籍)
    public function getSchregExistCnt($model, $schregno)
    {
        $query  = "  SELECT ";
        $query .= "      COUNT(*) ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO    = '{$schregno}' ";
        $query .= "      AND YEAR    = '{$model->year}' ";
        $query .= "      AND SEMESTER = '{$model->semester}' ";

        return $query;
    }

    //入金項目名取得
    public function getCollectLMName($db, $model, $lmCd)
    {
        $query .= "  SELECT ";
        $query .= "      COLLECT_M_NAME ";
        $query .= "  FROM ";
        $query .= "      COLLECT_M_MST ";
        $query .= "  WHERE ";
        $query .= "      SCHOOLCD         = '{$model->schoolCd}' ";
        $query .= "      AND SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "      AND YEAR         = '{$model->year}' ";
        $query .= "      AND COLLECT_L_CD || COLLECT_M_CD = '{$lmCd}' ";

        $mName = $db->getOne($query);
        return $mName;
    }

    //存在チェック(項目マスタ)
    public function getCollectLMstExistCnt($model, $lmCd)
    {
        $query .= "  SELECT ";
        $query .= "      COUNT(*) ";
        $query .= "  FROM ";
        $query .= "      COLLECT_M_MST ";
        $query .= "  WHERE ";
        $query .= "      SCHOOLCD         = '{$model->schoolCd}' ";
        $query .= "      AND SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "      AND YEAR         = '{$model->year}' ";
        $query .= "      AND COLLECT_L_CD || COLLECT_M_CD = '{$lmCd}' ";

        return $query;
    }

    //MAX伝票番号
    public function getMaxSlipNo($db, $model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SLIP_NO) AS SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR           = '{$model->year}' AND ";
        $query .= "     SCHOOLCD       = '{$model->schoolCd}' AND ";
        $query .= "     SCHOOL_KIND    = '{$model->schoolKind}' AND ";
        $query .= "     SCHREGNO       = '{$schregno}' ";

        $retVal = $db->getOne($query);
        return $retVal;
    }

    //入金パターン取得
    public function getCollectPattern($model, $collect_pattern_cd = "")
    {
        $query  = " SELECT ";
        if ($collect_pattern_cd) {
            $query .= "     * ";
        } else {
            $query .= "     COLLECT_PATTERN_CD AS VALUE, ";
            $query .= "     COLLECT_PATTERN_CD || ' ' || COLLECT_PATTERN_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '{$model->schoolCd}' AND ";
        $query .= "     SCHOOL_KIND = '{$model->schoolKind}' AND ";
        $query .= "     YEAR        = '{$model->year}' ";
        if ($collect_pattern_cd) {
            $query .= " AND COLLECT_PATTERN_CD = '{$collect_pattern_cd}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //名称マスタP012取得(グループコード採番時の番号帯)
    public function getNameMstP012($model)
    {
        $query  = "  SELECT ";
        $query .= "      NAME2, "; //from
        $query .= "      NAME3 ";  //to
        $query .= "  FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '{$model->year}' ";
        $query .= "      AND NAMECD1    = 'P012' ";
        $query .= "      AND NAMESPARE1 = 'KNJP855' ";

        return $query;
    }

    //グループコードの最大値を取得
    public function getMaxCollectGrpCdQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "      MAX(COLLECT_GRP_CD) ";
        $query .= "  FROM ";
        $query .= "      COLLECT_GRP_MST ";
        $query .= "  WHERE ";
        $query .= "          SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "      AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "      AND YEAR        = '{$model->year}' ";
        $query .= "      AND COLLECT_GRP_CD BETWEEN '{$model->grpCdFromNo}' AND '{$model->grpCdToNo}' ";

        return $query;
    }

    public function updateQueryCsv($db, $model, &$data_arr) {

        $cnt = 0;   //処理件数

        //グループコード割当処理
        $nextGrpCd = $model->nextGrpCd;
        $grpCdArray = array();

        //生徒毎の伝票作成処理
        for ($i = 0; $i < get_count($data_arr); $i++) {
            foreach ($model->updateLMCd as $lmKey => $label) {
                $lmCd = explode("-", $lmKey)[1];
                $lCd = substr($lmCd, 0, 2);
                $mCd = substr($lmCd, 2, 2);

                $schregno = $data_arr[$i]["SCHREGNO"];

                //徴収予定額取得 (入力無しであればスキップ)
                $collctMoney = $data_arr[$i][$lmKey];
                if (strlen(trim($collctMoney)) == 0) {
                    continue;
                }

                //入金項目毎にグループコードを割当
                $currentGrpCd = $grpCdArray[$lmCd];
                if ($currentGrpCd == "") {
                    $currentGrpCd = sprintf("%04d", $nextGrpCd);
                    $grpCdArray[$lmCd] = $currentGrpCd;

                    $grpName = mb_strcut("課外費".$model->field["COLLECT_MONTH"].$label, 0, 60);
                    knjp855Query::getUpdateCollectGrpQuery($db, $model, $lmCd, $currentGrpCd, $grpName);
                    $nextGrpCd++;
                }

                //伝票番号取得
                $maxSlipNo  = knjp855Query::getMaxSlipNo($db, $model, $schregno);
                $nextSlipNo = ($maxSlipNo != "") ? $maxSlipNo + 1 : $model->year.$schregno."001";

                //取込データ配列に付加情報追加
                $data_arr[$i]["SLIP_NO"]         = $nextSlipNo;
                $data_arr[$i]["COLLECT_MONEY"]   = $collctMoney;
                $data_arr[$i]["COLLECT_GRP_CD"]  = $currentGrpCd;

                //更新 -- COLLECT_SLIP_DAT
                knjp855Query::updateSlipDatQuery($db, $model, $data_arr[$i]);

                //更新 -- COLLECT_SLIP_M_DAT
                knjp855Query::updateSlipMDatQuery($db, $model, $data_arr[$i], $lCd, $mCd);

                $inputMonth = $model->field["COLLECT_MONTH"];
                $inputYear  = (intval($inputMonth) <= 3) ? $model->year + 1 : $model->year;
                for ($monthVal = 1; $monthVal <= 12; $monthVal++) {
                    $updMonth = sprintf("%02d", $monthVal);
                    $updYear  = ($monthVal <= 3) ? $model->year + 1 : $model->year;

                    //更新 -- COLLECT_SLIP_PLAN_M_DAT
                    $planMoney = ($inputYear.$inputMonth == $updYear.$updMonth) ? $collctMoney : 0; //※指定月以外は0円で計画金をセット
                    knjp855Query::updateSlipPlanMDatQuery($db, $model, $data_arr[$i], $lCd, $mCd, $updYear, $updMonth, $planMoney);

                    //更新 -- COLLECT_SLIP_PLAN_LIMITDATE_DAT
                    $paid_limit_month = ($inputYear.$inputMonth == $updYear.$updMonth) ? $inputMonth : "";
                    $paid_limit_date  = ($inputYear.$inputMonth == $updYear.$updMonth) ? $model->getLimitDate($db, $paid_limit_month) : "";
                    knjp855Query::updateLimitDateQuery($db, $model, $data_arr[$i], $updYear, $updMonth, $paid_limit_date, $paid_limit_month);
                }
            }

            $cnt++;
        }

        return $cnt;
    }

    //取込CSVの入金項目列に何件入力があるかを返す
    public function getLMUpdCnt ($data_arr, $lmCd) {
        $lmCdUpdCnt = get_count(array_filter(array_column($data_arr, $lmCd), function($value) {
            return strlen(trim($value)) > 0;
        }));

        return $lmCdUpdCnt;
    }

    //入金グループ関連登録
    public function getUpdateCollectGrpQuery($db, $model, $lmCd, $grpCd, $grpName)
    {
        $lCd = substr($lmCd, 0, 2);
        $mCd = substr($lmCd, 2, 2);

        //1.COLLECT_GRP_MST
        $query = knjp855Query::insertGrpMstQuery($model, $grpCd, $grpName);
        $db->query($query);

        //2.COLLECT_GRP_DAT
        $query = knjp855Query::insertGrpDatQuery($model, $grpCd, $lCd, $mCd);
        $db->query($query);

        //3.COLLECT_MONTH_GRP_DAT
        $query = knjp855Query::insertMonthGrpDatQuery($model, $grpCd, $lCd, $mCd);
        $db->query($query);
    }

    //追加 -- COLLECT_GRP_MST
    public function insertGrpMstQuery($model, $grpCd, $grpName)
    {
        //COLLECT_SLIP_PLAN_M_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["COLLECT_GRP_CD"][TEXT]   = $grpCd;
        $data["COLLECT_GRP_NAME"][TEXT] = $grpName;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
    
        $query = Query::insertSQL($data, "COLLECT_GRP_MST");
        return $query;
    }
    
    //追加 -- COLLECT_GRP_DAT
    public function insertGrpDatQuery($model, $grpCd, $lCd, $mCd)
    {
        //COLLECT_SLIP_PLAN_M_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["COLLECT_GRP_CD"][TEXT]   = $grpCd;
        $data["COLLECT_L_CD"][TEXT]     = $lCd;
        $data["COLLECT_M_CD"][TEXT]     = $mCd;
        $data["COLLECT_S_CD"][TEXT]     = "00";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
    
        $query = Query::insertSQL($data, "COLLECT_GRP_DAT");
        return $query;
    }
    
    //追加 -- COLLECT_MONTH_GRP_DAT
    public function insertMonthGrpDatQuery($model, $grpCd, $lCd, $mCd)
    {
        $month = $model->field["COLLECT_MONTH"];
        $monthVal = intval($month);
    
        //COLLECT_SLIP_PLAN_M_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]                 = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]              = $model->schoolKind;
        $data["YEAR"][TEXT]                     = $model->year;
        $data["COLLECT_GRP_CD"][TEXT]           = $grpCd;
        $data["COLLECT_L_CD"][TEXT]             = $lCd;
        $data["COLLECT_M_CD"][TEXT]             = $mCd;
        $data["MONTH_CNT"][NUMBER]              = 1;
        $data["COLLECT_MONTH_".$monthVal][TEXT] = "1";
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";
    
        $query = Query::insertSQL($data, "COLLECT_MONTH_GRP_DAT");
        return $query;
    }
    
    //追加 -- COLLECT_SLIP_DAT
    public function updateSlipDatQuery($db, $model, &$data_arr_i)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]             = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SLIP_NO"][TEXT]              = $data_arr_i["SLIP_NO"];
        $data["SCHREGNO"][TEXT]             = $data_arr_i["SCHREGNO"];
        $data["SLIP_STAFFCD"][TEXT]         = STAFFCD;
        $data["PAY_DIV"][TEXT]              = $model->patternInfo["PAY_DIV"];
        $data["SLIP_DATE"][DATE]            = str_replace("/", "-", $model->field["SLIP_DATE"]);
        $data["COLLECT_GRP_CD"][TEXT]       = $data_arr_i["COLLECT_GRP_CD"];
        $data["COLLECT_PATTERN_CD"][TEXT]   = $model->field["COLLECT_PATTERN_CD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_SLIP_DAT");
        $db->query($query);
    }

    //追加 -- COLLECT_SLIP_M_DAT
    public function updateSlipMDatQuery($db, $model, &$data_arr_i, $lCd, $mCd)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["SLIP_NO"][TEXT]          = $data_arr_i["SLIP_NO"];
        $data["SCHREGNO"][TEXT]         = $data_arr_i["SCHREGNO"];
        $data["COLLECT_L_CD"][TEXT]     = $lCd;
        $data["COLLECT_M_CD"][TEXT]     = $mCd;
        $data["COLLECT_MONEY"][NUMBER]  = $data_arr_i["COLLECT_MONEY"];
        $data["COLLECT_CNT"][NUMBER]    = 1;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_SLIP_M_DAT");
        $db->query($query);
}

    //追加 -- COLLECT_SLIP_PLAN_M_DAT
    public function updateSlipPlanMDatQuery($db, $model, &$data_arr_i, $lCd, $mCd, $planYear, $planMonth, $planMoney)
    {
            $data = array();
            $data["SCHOOLCD"][TEXT]         = $model->schoolCd;
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
            $data["YEAR"][TEXT]             = $model->year;
            $data["SLIP_NO"][TEXT]          = $data_arr_i["SLIP_NO"];
            $data["SCHREGNO"][TEXT]         = $data_arr_i["SCHREGNO"];
            $data["COLLECT_L_CD"][TEXT]     = $lCd;
            $data["COLLECT_M_CD"][TEXT]     = $mCd;
            $data["PLAN_YEAR"][TEXT]        = $planYear;
            $data["PLAN_MONTH"][TEXT]       = $planMonth;
            $data["PLAN_MONEY"][NUMBER]     = $planMoney;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_M_DAT");
            $db->query($query);
    }

    //追加 -- COLLECT_SLIP_PLAN_LIMITDATE_DAT
    public function updateLimitDateQuery($db, $model, &$data_arr_i, $planYear, $planMonth, $paid_limit_date, $paid_limit_month)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]              = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]           = $model->schoolKind;
        $data["YEAR"][TEXT]                  = $model->year;
        $data["SLIP_NO"][TEXT]               = $data_arr_i["SLIP_NO"];
        $data["SCHREGNO"][TEXT]              = $data_arr_i["SCHREGNO"];
        $data["PLAN_YEAR"][TEXT]             = $planYear;
        $data["PLAN_MONTH"][TEXT]            = $planMonth;
        $data["PAID_LIMIT_MONTH"][TEXT]      = $paid_limit_month;
        $data["PAID_LIMIT_DATE"][TEXT]       = $paid_limit_date;
        $data["PAID_LIMIT_MONTH_CALC"][TEXT] = $paid_limit_month;
        $data["PAID_LIMIT_DATE_CALC"][TEXT]  = $paid_limit_date;
        $data["REGISTERCD"][TEXT]            = STAFFCD;
        $data["UPDATED"][NUMBER]             = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_LIMITDATE_DAT");
        $db->query($query);
    }

    //銀行休業日取得
    public function getBankHolidayDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BANK_HOLIDAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '{$model->schoolCd}' AND ";
        $query .= "     SCHOOL_KIND = '{$model->schoolKind}' ";

        return $query;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    function selectCsvErrQuery() {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

}
