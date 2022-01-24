<?php

require_once('for_php7.php');

//生徒授業料軽減
class knjp727Model extends model {
    var $cmd;                   //一覧表示時のcmd
    var $search = array();
    var $income     = array();
    var $schregno   = array();
    var $field      = array();

    function knjp727Model() {}
    function init() {
        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->search);
            unset($this->income);
            unset($this->field);
            unset($this->config);
            unset($this->changePrg);
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd = VARS::request("cmd");
        $this->windowWidth = VARS::post("windowWidth");
        $this->windowHeight = VARS::post("windowHeight");
        //検索条件
        $this->search = array("STD_DIV"  => VARS::post("STD_DIV"),
                              "GRADE"    => VARS::post("GRADE_SEND"),
                              "HR_CLASS" => VARS::post("HR_CLASS_SEND"),
                              "PREF"     => VARS::post("PREF"),
                              "RAD_PREF" => VARS::post("RAD_PREF")
                             );
        //確定月
        $this->decMonth = array("COUNTRY_DEC_MONTH1"        => VARS::post("COUNTRY_DEC_MONTH1"),
                                "COUNTRY_DEC_MONTH2"        => VARS::post("COUNTRY_DEC_MONTH2"),
                                "PREF_DEC_MONTH1"           => VARS::post("PREF_DEC_MONTH1"),
                                "PREF_DEC_MONTH2"           => VARS::post("PREF_DEC_MONTH2")
                             );
        if(VARS::get("SENDPRGID")){
            unset($this->search);
            unset($this->income);
            unset($this->field);
            unset($this->config);
            unset($this->changePrg);
            $this->search["GRADE"] = VARS::get("S_GRADE");
            $this->search["HR_CLASS"] = VARS::get("S_HR_CLASS");
            $this->search["PREF"] = VARS::get("S_PREF");
            $this->search["RAD_PREF"] = VARS::get("S_RAD_PREF");
        }
        //軽減額検索
        $this->income  = array("SLIP_NO"        => VARS::post("SLIP_NO"),
                               "SCHREGNO"       => $this->slipSchreg[VARS::post("SLIP_NO")],
                               "GRADE"          => VARS::post("GRADE")
                              );

        $checkGrade = $this->income["GRADE"] ? $this->income["GRADE"] : $this->search["GRADE"];
        $this->exeYear = $this->exeYear? $this->exeYear : CTRL_YEAR;
        $this->mstLastYear = $checkGrade == "01" ? $this->exeYear : $this->exeYear - 1;

        //画面のコンテンツ
        $this->field  = array("REDUCTION_SEQ_1"         => VARS::post("REDUCTION_SEQ_1"),
                              "COUNTRY_RARE_CASE_CD"    => VARS::post("COUNTRY_RARE_CASE_CD"),
                              "COUNTRY_RANK_1"          => VARS::post("COUNTRY_RANK_1"),
                              "COUNTRY_MONEY_1"         => VARS::post("COUNTRY_MONEY_1"),
                              "COUNTRY_BASE_FLG_1"      => VARS::post("COUNTRY_BASE_FLG_1"),
                              "COUNTRY_RANK_2"          => VARS::post("COUNTRY_RANK_2"),
                              "COUNTRY_MONEY_2"         => VARS::post("COUNTRY_MONEY_2"),
                              "COUNTRY_BASE_FLG_2"      => VARS::post("COUNTRY_BASE_FLG_2"),
                              "REDUCTION_ADD_MONEY_1"   => VARS::post("REDUCTION_ADD_MONEY_1"),
                              "COUNTRY_ADD_MONEY_1"     => VARS::post("COUNTRY_ADD_MONEY_1"),
                              "COUNTRY_ADD_FLG_1"       => VARS::post("COUNTRY_ADD_FLG_1"),
                              "REDUCTION_SEQ_2"         => VARS::post("REDUCTION_SEQ_2"),
                              "COUNTRY_ADD_MONEY_2"     => VARS::post("COUNTRY_ADD_MONEY_2"),
                              "COUNTRY_ADD_FLG_2"       => VARS::post("COUNTRY_ADD_FLG_2"),
                              "SCOOL_DIV_FIRST_1"       => VARS::post("SCOOL_DIV_FIRST_1"),
                              "REDUC_SCHOOL_FLG_1"      => VARS::post("REDUC_SCHOOL_FLG_1"),
                              "SCOOL_DIV_LAST_1"        => VARS::post("SCOOL_DIV_LAST_1"),
                              "REDUC_SCHOOL_FLG_2"      => VARS::post("REDUC_SCHOOL_FLG_2"),
                              "REDUC_REMARK"            => VARS::post("REDUC_REMARK")
                            );

        //異動情報,奨学金情報の参照
        $this->genmenTransfer = array("SCHREGNO"  =>  VARS::post("SCHREGNO"),
                                      "SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND")
                                     );
    }

    function &getMainModel() {}

    //入力項目チェック
    function validateInput() {
        for($i = 0; $i < get_count($this->slipno); $i++){
            if (strlen($this->field["REDUC_REMARK"][$i]) > 30){
                $this->setWarning('MSG901',"備考　伝票番号：" .$this->slipno[$i]);
                return false;
            }
            if ($this->field["REDUC_INCOME_1"][$i] != "" && $this->field["REDUCTION_ADD_MONEY_1"][$i] == ""){
                $this->field["REDUCTION_ADD_MONEY_1"][$i] = 0;
            }
            if ($this->field["REDUC_INCOME_2"][$i] != "" && $this->field["COUNTRY_ADD_MONEY_2"][$i] == ""){
                $this->field["COUNTRY_ADD_MONEY_2"][$i] = 0;
            }
        }
        return true;
    }

    /**
     * 更新用のモデルを返します
    */
    function &getUpdateModel() {
        //セット
        for ($i = 0; $i < get_count($this->slipno); $i++) {
            $this->slipSchoolStd[$this->slipno[$i]]["SAKI1"]    = VARS::post("SCOOL_DIV_FIRST_1_".$this->slipno[$i]);
            $this->slipSchoolStd[$this->slipno[$i]]["ATO1"]     = VARS::post("SCOOL_DIV_LAST_1_".$this->slipno[$i]);
        }

        if ($this->validateInput()){
            knjp727Query::update($this);
            $this->setMessage("MSG201");
        }
    }

    // 課税総取得変更のモデルを返します
    function &getSendModel() {
        //金額入力
        $db = Query::dbCheckOut();

        //減免先
        list($jugyouryou, $genmenJugyou) = $this->setReductionStd($this->reducStdSaki[$this->income["SCHREGNO"]], $this->slipPlan[$this->income["SLIP_NO"]]["TOTAL_JUGYOU"], "saki");

        $credits = $this->slipInfo[$this->income["SLIP_NO"]]["CREDITS"];

        //基準額
        $query = knjp727Query::selectQueryReductionMonty($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($this->slipInfo[$this->income["SLIP_NO"]]["IS_CREDITCNT"]) {
            $row["COUNTRY_MONEY1"] = $row["COUNTRY_MONEY1"] * $credits;
            $row["COUNTRY_MONEY2"] = $row["COUNTRY_MONEY2"] * $credits;
        }

        $json_array = array();
        $countryMoney  = 0;
        $countryMoney1 = 0;
        $countryMoney2 = 0;
        if (is_array($row)) {
            foreach ($row as $key => $val) {
                $json_array[] = "'{$key}':'{$val}'";
            }
            $countryMoney  = $row["COUNTRY_MONEY1"] + $row["COUNTRY_MONEY2"];
            $countryMoney1 = $row["COUNTRY_MONEY1"];
            $countryMoney2 = $row["COUNTRY_MONEY2"];
        } else {
            $json_array[] = "'COUNTRY_RANK1':''";
            $json_array[] = "'COUNTRY_RANK2':''";
        }

        //加算額
        $query = knjp727Query::selectQueryReductionAddMonty($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($this->slipInfo[$this->income["SLIP_NO"]]["IS_CREDITCNT"]) {
            $row["COUNTRY_ADD_MONEY1"] = $row["COUNTRY_ADD_MONEY1"] * $credits;
            $row["COUNTRY_ADD_MONEY2"] = $row["COUNTRY_ADD_MONEY2"] * $credits;
        }

        $countryAddMoney  = 0;
        $countryAddMoney1 = 0;
        $countryAddMoney2 = 0;
        if (is_array($row)) {
            foreach ($row as $key => $val) {
                $json_array[] = "'{$key}':'{$val}'";
            }
            $countryAddMoney  = $row["COUNTRY_ADD_MONEY1"] + $row["COUNTRY_ADD_MONEY2"];
            $countryAddMoney1 = $row["COUNTRY_ADD_MONEY1"];
            $countryAddMoney2 = $row["COUNTRY_ADD_MONEY2"];
        }
        $json_array[] = "'JUGYOURYOU':'{$jugyouryou}'";

        $calcAdjustmentMoney = 0;
        $setSakiGenmen = $genmenJugyou;

        $calcAdjustmentMoney = $jugyouryou - (($countryMoney1 + $countryAddMoney1) * 3 + ($countryMoney2 + $countryAddMoney2) * 9);

        $setAtoGenmen = $this->slipInfo[$this->income["SLIP_NO"]]["SCOOL_DIV_LAST_1"] ? $this->slipInfo[$this->income["SLIP_NO"]]["SCOOL_DIV_LAST_1"] : 0;
        if ($calcAdjustmentMoney > 0) {
            if ($calcAdjustmentMoney < $setAtoGenmen) {
                $setAtoGenmen = $calcAdjustmentMoney;
            }
            $calcAdjustmentMoney = 0;
        } else {
            $calcAdjustmentMoney = -1 * $calcAdjustmentMoney;
            $setSakiGenmen = $this->slipPlan[$row["SLIP_NO"]]["TOTAL_JUGYOU"];
            $setAtoGenmen = 0;
        }

        $json_array[] = "'ADJUSTMENT_MONEY':'{$calcAdjustmentMoney}'";

        $json_array[] = "'GA1':'{$jugyouryou}'";
        $json_array[] = "'GA2':'{$countryMoney1}'";
        $json_array[] = "'GA3':'{$countryMoney2}'";
        $json_array[] = "'GA4':'{$countryAddMoney1}'";
        $json_array[] = "'GA5':'{$countryAddMoney2}'";

        if (get_count($json_array) > 0) {
            $json = implode(',', $json_array);
            echo "{ 'result':true,{$json} }";
        } else {
            echo "{ 'result':false }";
        }
        Query::dbCheckIn($db);
    }

    // 減免情報のモデルを返します
    function &getSendModelGenmen() {
        $db = Query::dbCheckOut();
        $returnValue = "";

        //移動情報取得
        $query = knjp727Query::selectQueryGenmen($this);
        $result2 = $db->query($query);
        while ($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            //T1.TRANSFERCD,T1.TRANSFER_SDATE,T1.TRANSFER_EDATE,T2.NAME1
            list($fy, $fm, $fd) = explode("-", $row["FROM_DATE"]);
            list($ty, $tm, $td) = explode("-", $row["TO_DATE"]);
            $row["S_YEAR_MONTH"] = ($row["FROM_DATE"]) ? $fy.'-'.$fm : "";
            $row["E_YEAR_MONTH"] = ($row["TO_DATE"]) ? $ty.'-'.$tm : "";
            $returnValue .= "&nbsp;{$row["S_YEAR_MONTH"]}";
            $returnValue .= "～{$row["E_YEAR_MONTH"]}";
            $returnValue .= " {$row["REDUCTION_DIV_NAME"]}({$row["GENMEN_TYPE"]})<BR>";
        }

        if($returnValue != ""){
            echo $returnValue;
        }else{
            echo "NOTFOUND";
        }
        Query::dbCheckIn($db);
    }

    // 移動情報のモデルを返します
    function &getSendModelTransfer() {
        $db = Query::dbCheckOut();
        $returnValue = "";

        //移動情報取得
        $query = knjp727Query::selectQueryTransfer($this);
        $result2 = $db->query($query);
        while ($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            //T1.TRANSFERCD,T1.TRANSFER_SDATE,T1.TRANSFER_EDATE,T2.NAME1
            $returnValue .= "&nbsp;".$row["TRANSFER_SDATE"];
            $returnValue .= ($row["TRANSFER_EDATE"] == "")? "" : "～".$row["TRANSFER_EDATE"];
            $returnValue .= " ".$row["NAME1"]."<BR>";
        }

        if($returnValue != ""){
            echo $returnValue;
        }else{
            echo "NOTFOUND";
        }
        Query::dbCheckIn($db);
    }

    function setReductionPref($jugyouryou, $jugyouryouTou, $countryMoney, $countryAddMoney, $prefMoney, $moneyDiv, $hyojunJugyouryou, $soeji) {
        //授業料に標準授業料をセット
        if ($hyojunJugyouryou > 0 && $jugyouryou > $hyojunJugyouryou) {
            $jugyouryou =  $hyojunJugyouryou;
        }

        //授業料ベースの計算
        $numerator = ($soeji == "1") ? "1" : "3";
        $calJugyou = ($jugyouryou + $jugyouryouTou - (($countryMoney + $countryAddMoney) * 12)) / 4 * $numerator;

        //補助金ベースの計算
        if ($moneyDiv == "1") {
            //割合
            $calPref = 0;
            $calPref = $prefMoney - $countryMoney - $countryAddMoney;
            $calPref = $calPref > 0 ? $calPref : 0;
            $calPref = $calPref > $prefMoney ? $prefMoney : $calPref;
            $calPref = $calPref * 12 / 4 * $numerator;
            $retMoney = $calJugyou > $calPref ? $calPref : $calJugyou;
        } else if ($moneyDiv == "2") {
            //絶対額
            $calPref = $prefMoney * 12 / 4 * $numerator;
            $retMoney = $calJugyou > $calPref ? $calPref : $calJugyou;
        } else if ($moneyDiv == "3") {
            //保護者負担額
            $calPref = $prefMoney / 4 * $numerator;
            $retMoney = ($calJugyou - $prefMoney > 0) ? $calJugyou - $prefMoney : 0;
        }

        return array($retMoney, $calJugyou);
    }

    //府県
    function setReductionMst($reductionMst, $income, $div, $saki) {
        $retMoney = 0;
        $retRank = "";
        if (!is_array($reductionMst)) {
            return array($retMoney, $retRank, "", "");
        }

        foreach ($reductionMst as $key => $val) {
            if ($val["INCOME_LOW".$div] <= $income && $val["INCOME_HIGH".$div] >= $income) {
                if ($val["MONEY_DIV".$div] == "1") {
                    $jugyouRyou = $saki["MONTH_MONEY"] + $saki["MONTH_MONEY_TOU"];
                    $jugyou = $jugyouRyou / $val["DENOMINATOR".$div];
                    $retMoney = $jugyou * $val["NUMERATOR".$div];
                    $zengakuFlg = ($val["DENOMINATOR".$div] > 0 && $val["DENOMINATOR".$div] == $val["NUMERATOR".$div]) ? 1 : "";
                    return array($retMoney, $val["INCOME_RANK".$div], $val["MONEY_DIV".$div], $zengakuFlg);
                } else if ($val["MONEY_DIV".$div] == "2" || $val["MONEY_DIV".$div] == "3") {
                    $retMoney = $val["REDUCTIONMONEY_".$div];
                    return array($retMoney, $val["INCOME_RANK".$div], $val["MONEY_DIV".$div], "");
                }
            }
        }
        return array($retMoney, $retRank, "", "");
    }

    //減免
    function setReductionStd($reducStd, $jugyouRyou, $div) {
        $retMoney = 0;
        if (!is_array($reducStd)) {
            return array($jugyouRyou, $retMoney);
        }
        if ($reducStd["MONEY_DIV"] == "1") {
            $jugyou = $jugyouRyou / $reducStd["DENOMINATOR"];
            $retMoney = $jugyou * $reducStd["NUMERATOR"];
        } else if ($reducStd["MONEY_DIV"] == "2") {
            $retMoney = $reducStd["MONEY"];
        }
        if ($div == "saki") {
            $retJugyouRyou = $jugyouRyou - $retMoney;
        } else {
            $retJugyouRyou = $jugyouRyou;
        }
        return array($retJugyouRyou, $retMoney);
    }

    function getReductionMoneyCal($reducStd, $jugyouRyou) {
        if ($jugyouRyou <= 0) {
            return $jugyouRyou;
        }
        list($sYear, $sMonth, $sDay) = explode("-", $reducStd["S_YEAR_MONTH"]);
        list($eYear, $eMonth, $eDay) = explode("-", $reducStd["E_YEAR_MONTH"]);
        $sMonth = $sMonth < "04" ? $sMonth + 12 : $sMonth;
        $eMonth = $eMonth < "04" ? $eMonth + 12 : $eMonth;
        return array($jugyouRyou, $sMonth, $eMonth);
    }

}
?>
