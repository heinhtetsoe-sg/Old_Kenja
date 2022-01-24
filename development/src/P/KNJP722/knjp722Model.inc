<?php

require_once('for_php7.php');

//生徒入学金軽減
class knjp722Model extends model {
    var $cmd;                   //一覧表示時のcmd
    var $search = array();
    var $income     = array();
    var $schregno   = array();
    var $field      = array();

    function knjp722Model() {}
    function init() {
        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->search);
            unset($this->income);
            unset($this->field);
            unset($this->config);
            unset($this->changePrg);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd = VARS::request("cmd");
        $this->windowWidth = VARS::post("windowWidth");
        $this->windowHeight = VARS::post("windowHeight");
        //検索条件
        $this->search = array("GRADE"    => VARS::post("GRADE_SEND"),
                              "HR_CLASS" => VARS::post("HR_CLASS_SEND"),
                              "PREF"     => VARS::post("PREF"),
                              "RAD_PREF" => VARS::post("RAD_PREF")
                             );
        //確定月
        $this->decMonth = array("PREF_DEC_MONTH"    => VARS::post("PREF_DEC_MONTH"));

        if (VARS::get("SENDPRGID")) {
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
        $this->income  = array("SLIP_NO"                => VARS::post("SLIP_NO"),
                               "SCHREGNO"               => $this->slipSchreg[VARS::post("SLIP_NO")],
                               "GRADE"                  => VARS::post("GRADE"),
                               "REDUC_INCOME"           => VARS::post("REDUC_INCOME"),
                               "G_PREF_CD"              => VARS::post("G_PREF_CD"),
                               "PREF_RANK"              => VARS::post("PREF_RANK"),
                               "PREF_RARE_CASE_CD"      => VARS::post("PREF_RARE_CASE_CD"),
                               "PREF_MONEY"             => VARS::post("PREF_MONEY"),
                               "OBJ_NAME"               => VARS::post("OBJ_NAME"),
                               "SOEJI"                  => VARS::post("SOEJI")
                              );

        $checkGrade = $this->income["GRADE"] ? $this->income["GRADE"] : $this->search["GRADE"];
        $this->mstLastYear = $checkGrade == "01" ? CTRL_YEAR : CTRL_YEAR - 1;

        //画面のコンテンツ
        $this->field  = array("REDUCTION_SEQ"           => VARS::post("REDUCTION_SEQ"),
                              "REDUC_INCOME"            => VARS::post("REDUC_INCOME"),
                              "PREF_RARE_CASE_CD"       => VARS::post("PREF_RARE_CASE_CD"),
                              "PREF_RANK"               => VARS::post("PREF_RANK"),
                              "PREF_MONEY"              => VARS::post("PREF_MONEY"),
                              "PREF_FLG"                => VARS::post("PREF_FLG"),
                              "REDUCTION_NYUGAKUKIN"    => VARS::post("REDUCTION_NYUGAKUKIN"),
                              "REDUC_SCHOOL_FLG"        => VARS::post("REDUC_SCHOOL_FLG"),
                              "REDUC_REMARK"            => VARS::post("REDUC_REMARK")
                            );

        //異動情報,奨学金情報の参照
        $this->genmenTransfer = array("SCHREGNO"    =>  VARS::post("SCHREGNO"),
                                      "SCHOOL_KIND" =>  VARS::post("SCHOOL_KIND")
                                     );

        //軽減対象都道府県設定値
        $this->config  = array("REDUC_PREF"     =>  VARS::post("REDUC_PREF"),
                               "RAD_REDUC_PREF" =>  VARS::post("RAD_REDUC_PREF")
                              );

        //検索対象都道府県
        if (!isset($this->search["RAD_PREF"])){
            $this->search["RAD_PREF"] = 1;
        }
        //軽減対象都道府県設定値
        if (!isset($this->config["RAD_REDUC_PREF"])){
            $this->config["RAD_REDUC_PREF"] = 1;
        }

        $this->calcFlg = array();
        if ($this->cmd == 'calc') {
            $this->calcFlg = VARS::post("CALC_FLG");
        }
    }

    function &getMainModel() {}

    //入力項目チェック
    function validateInput() {
        for ($i = 0; $i < get_count($this->slipno); $i++) {
            $this->field["REDUC_INCOME"][$i] = VARS::post("SET_REDUC_INCOME_".$this->slipno[$i]);
            if (is_numeric($this->field["REDUC_INCOME"][$i]) && strlen($this->field["REDUC_INCOME"][$i]) > 8) {
                $this->setWarning('MSG901',"課税総取得額　伝票番号：" .$this->slipno[$i]);
                return false;
            }
            if (strlen($this->field["REDUC_REMARK"][$i]) > 30) {
                $this->setWarning('MSG901',"備考　伝票番号：" .$this->slipno[$i]);
                return false;
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
            $this->slipSchoolStd[$this->slipno[$i]]["SAKI1"]    = VARS::post("REDUCTION_NYUGAKUKIN_".$this->slipno[$i]);

            $this->field["PAID_FLG"][$this->slipno[$i]]         = VARS::post("PAID_FLG_".$this->slipno[$i]);
        }

        if ($this->validateInput()){
            knjp722Query::update($this);
            $this->setMessage("MSG201");
        }
    }

    //再計算処理
    function &getCalcModel(&$db, $getRow) {

        $prefOnlyNyugakukin = 0;
        $setGenmenItemAto = array();
        if (is_array($this->slipPlan[$getRow["SLIP_NO"]])) {
            foreach ($this->slipPlan[$getRow["SLIP_NO"]] as $slipPlanKey => $slipPlanVal) {
                list ($reduction_div, $is_reduction_school) = explode(':', $slipPlanVal["DIV"]);

                if ($reduction_div == '3') {
                    $prefOnlyNyugakukin += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                }

                if ($is_reduction_school == '1') {
                    if (is_array($this->reducStdAto[$getRow["SCHREGNO"]])) {
                        foreach ($this->reducStdAto[$getRow["SCHREGNO"]] as $reducKey => $reducVal) {
                            list($nyugakukin, $genmenNyugakukin) = $this->setReductionStd($reducVal, $slipPlanVal["MONEY"], "ato");
                            $slipPlanVal["MONEY"] = $nyugakukin;
                            $setGenmenItemAto[] = $slipPlanKey;
                        }
                    }
                }
            }
        }

        $countryRare = explode(':', $getRow["COUNTRY_RARE_CASE_CD"]);
        $prefRare = explode(':', $getRow["PREF_RARE_CASE_CD"]);

        $setRow = array();
        $reducflg = false;

        //初期値
        if (!$getRow["REDUCTION_SCHREGNO"]) {
            if (!$getRow["PREF_RANK"]) $getRow["PREF_RANK"] = 'XX';
        }

        //補助金用入学金
        $prefNyugakukin = ($prefOnlyNyugakukin > 0) ? $prefOnlyNyugakukin : 0;

        //補助金
        $prefFlg = '';
        if ($prefRare[1] == '1' && strlen($getRow["REDUC_INCOME"]) == 0) {
            $prefFlg = 'rare';
            $reducflg = true;
        } else if (strlen($getRow["REDUC_INCOME"]) > 0) {
            $prefFlg = 'income';
            $setRow["PREF_RARE_CASE_CD"] = "";
            $setRow["PREF_TEXT_DISP"] = "";
            $reducflg = true;
        } else if ($getRow["PREF_RANK"]) {
            $prefFlg = 'rank';
            $reducflg = true;
        }

        //減免後算出(1)
        $tmpReductMoney = $prefNyugakukin;

        if ($prefFlg == 'rare') {
            $setRow["REDUCTION_SEQ"] = '';
            $setRow["PREF_MONEY"] = $getRow["PREF_MONEY"];

            //減免後算出(2)
            $total1 += $tmpReductMoney;
            $tmpReductMoney -= $getRow["PREF_MONEY"];
        } else {
            $setMoney = $prefNyugakukin;
            list($calcPrefMoney, $prefRank, $moneyDiv, $burdenChargeFlg, $prefSeq) = $this->setReductionMst($this->reductionMst[$getRow["G_PREF_CD"]], $getRow["REDUC_INCOME"], $setMoney, $prefFlg, $getRow["PREF_RANK"]);
            list($prefMoney, $burdenCharge) = $this->setReductionPref($calcPrefMoney, $setMoney, $moneyDiv, $countryRare[1], $burdenChargeFlg);
            $setRow["REDUCTION_SEQ"] = $prefSeq;
            $setRow["PREF_RANK"] = $prefRank;
            $setRow["PREF_MONEY"] = (!strlen($prefRank) && !$prefMoney && $prefRank != 'XX') ? "" : $prefMoney;

            //減免後算出(2)
            $tmpReductMoney -= $prefMoney;
        }

        //減免対象者（後）
        if (!get_count($setGenmenItemAto)) {
            $reducflg = false;
        }

        //減免後算出(3)
        $tmpReductMoney = max($tmpReductMoney, 0);
        if (is_array($this->reducStdAto[$getRow["SCHREGNO"]]) && $tmpReductMoney > 0) {
            foreach ($this->reducStdAto[$getRow["SCHREGNO"]] as $reducKey => $reducVal) {
                list($nyugakukin, $genmenNyugakukin) = $this->setReductionStd($reducVal, $tmpReductMoney, "ato");
                $tmpReductMoney = $genmenNyugakukin;
            }
        }
        $setRow["REDUCTION_NYUGAKUKIN"] = ($reducflg) ? intval($tmpReductMoney) : 0;

        $this->setMessage("MSG202");
        return $setRow;
    }

    // 課税総取得変更のモデルを返します
    function &getSendModel() {
        //金額入力
        $db = Query::dbCheckOut();

        $json_array = array();

        $nyugakukinSaki = 0;
        $prefOnlyNyugakukin = 0;
        $setGenmenSaki = $setGenmenAto = 0;
        $setGenmenItemAto = array();
        if (is_array($this->slipPlan[$this->income["SLIP_NO"]])) {
            foreach ($this->slipPlan[$this->income["SLIP_NO"]] as $slipPlanKey => $slipPlanVal) {
                list ($reduction_div, $is_reduction_school) = explode(':', $slipPlanVal["DIV"]);

                if ($reduction_div == '3') {
                    $prefOnlyNyugakukin += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                }

                //減免先
                if ($is_reduction_school == '1' && is_array($this->reducStdSaki[$this->income["SCHREGNO"]])) {
                    foreach ($this->reducStdSaki[$this->income["SCHREGNO"]] as $reducKey => $reducVal) {
                        list($nyugakukin, $genmenNyugakukin) = $this->setReductionStd($reducVal, $slipPlanVal["MONEY"], "saki");
                        $slipPlanVal["MONEY"] = $nyugakukin;
                        $this->slipPlan[$this->income["SLIP_NO"]][$slipPlanKey]["KANSAN"] = $nyugakukin;
                        $setGenmenSaki += $genmenNyugakukin;
                    }
                }
                $nyugakukinSaki += $slipPlanVal["MONEY"];

                //減免後
                if ($is_reduction_school == '1' && is_array($this->reducStdAto[$this->income["SCHREGNO"]])) {
                    foreach ($this->reducStdAto[$this->income["SCHREGNO"]] as $reducKey => $reducVal) {
                        list($nyugakukin, $genmenNyugakukin) = $this->setReductionStd($reducVal, $slipPlanVal["MONEY"], "ato");
                        $slipPlanVal["MONEY"] = $nyugakukin;
                        $setGenmenItemAto[] = $slipPlanKey;
                    }
                }
            }
        }

        $setRow = array();
        $reduc_flg = '';
        $reducflg = false;
        if ($this->income["OBJ_NAME"] == 'REDUC_INCOME[]' && strlen($this->income["REDUC_INCOME"]) > 0) {
            $reduc_flg = 'income';
            $reducflg = true;
        } else if ($this->income["PREF_RANK"]) {
            $reduc_flg = 'rank';
            $reducflg = true;
        }

        //補助金用入学金
        $prefNyugakukin = ($prefOnlyNyugakukin > 0) ? $prefOnlyNyugakukin : 0;

        //減免後・学校負担金算出(1)
        $tmpReductMoney = ($prefNyugakukin > 0) ? $prefNyugakukin : 0;

        $prefRare = explode(':', $this->income["PREF_RARE_CASE_CD"]);

        //補助金
        $prefFlg = '';
        if ($prefRare[1] == '1') {
            $prefFlg = 'rare';
        }

        if ($prefFlg == 'rare') {
            $prefMoney = $this->income["PREF_MONEY"];

            $json_array[] = "'REDUCTION_SEQ':''";
            $json_array[] = "'PREF_MONEY':'{$prefMoney}'";
            $json_array[] = "'PREF_RANK':''";
            $json_array[] = "'MONEY_DIV':''";
            $json_array[] = "'NYUGAKUKIN':'{$nyugakukinSaki}'";

            //減免後算出(2)
            $tmpReductMoney -= $prefMoney;
        } else {
            //府県補助金
            $setMoney = ($prefNyugakukin > 0) ? $prefNyugakukin : 0;
            $kirikae = ($this->income["OBJ_NAME"] == 'REDUC_INCOME[]') ? 'income' : 'rank';
            list($calcPrefMoney, $prefRank, $moneyDiv, $burdenChargeFlg, $prefSeq) = $this->setReductionMst($this->reductionMst[$this->income["G_PREF_CD"]], $this->income["REDUC_INCOME"], $setMoney, $kirikae, $this->income["PREF_RANK"]);
            list($prefMoney, $burdenCharge) = $this->setReductionPref($calcPrefMoney, $setMoney, $moneyDiv, $rareFlg[1], $burdenChargeFlg);
            $json_array[] = "'REDUCTION_SEQ':'{$prefSeq}'";
            $prefMoney = max($prefMoney, 0);
            $json_array[] = "'PREF_MONEY':'{$prefMoney}'";
            $json_array[] = "'PREF_RANK':'{$prefRank}'";
            $json_array[] = "'NYUGAKUKIN':'{$nyugakukinSaki}'";
            $json_array[] = "'MONEY_DIV':'{$moneyDiv}'";
            $json_array[] = "'REDUCTIONMONEY':'{$calcPrefMoney}'";

            //減免後算出(2)
            $tmpReductMoney -= $prefMoney;
        }

        //減免対象者（後）
        if (!get_count($setGenmenItemAto)) {
            $reducflg = false;
        }

        //減免後算出(3)
        $tmpReductMoney = max($tmpReductMoney, 0);
        if (is_array($this->reducStdAto[$this->income["SCHREGNO"]]) && $tmpReductMoney > 0) {
            foreach ($this->reducStdAto[$this->income["SCHREGNO"]] as $reducKey => $reducVal) {
                list($nyugakukin, $genmenNyugakukin) = $this->setReductionStd($reducVal, $tmpReductMoney, "ato");
                $tmpReductMoney = $genmenNyugakukin;
            }
        }
        $reductNyugakukin = ($reducflg) ? intval($tmpReductMoney) : 0;
        $json_array[] = "'REDUCTION_NYUGAKUKIN':'{$reductNyugakukin}'";

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

        //減免情報取得
        $query = knjp722Query::selectQueryGenmen($this);
        $result2 = $db->query($query);
        while ($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            list($fy, $fm, $fd) = explode("-", $row["FROM_DATE"]);
            list($ty, $tm, $td) = explode("-", $row["TO_DATE"]);
            $row["S_YEAR_MONTH"] = ($row["FROM_DATE"]) ? $fy.'-'.$fm : "";
            $row["E_YEAR_MONTH"] = ($row["TO_DATE"]) ? $ty.'-'.$tm : "";
            $returnValue .= "&nbsp;{$row["S_YEAR_MONTH"]}";
            $returnValue .= "～{$row["E_YEAR_MONTH"]}";
            $returnValue .= " {$row["REDUCTION_DIV_NAME"]}({$row["GENMEN_TYPE"]})<BR>";
        }

        if ($returnValue != "") {
            echo $returnValue;
        } else {
            echo "NOTFOUND";
        }
        Query::dbCheckIn($db);
    }

    // 異動情報のモデルを返します
    function &getSendModelTransfer() {
        $db = Query::dbCheckOut();
        $returnValue = "";

        //異動情報取得
        $query = knjp722Query::selectQueryTransfer($this);
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

    function setReductionPref($prefMoney, $setMoney, $moneyDiv, $countryRareFlg, $burdenChargeFlg) {
        //入学金ベースの計算
        $calMoney = $setMoney;

        //補助金ベースの計算
        $retMoney = 0;
        if ($moneyDiv == "1") {
            $retMoney = min($prefMoney, $calMoney);
        } else if ($moneyDiv == "2") {
            //絶対額
            $retMoney = min($prefMoney, $calMoney);
        }
        $retMoney = ($retMoney > 0) ? $retMoney : 0;

        //負担金
        $burdenCharge = 0;

        return array($retMoney, $burdenCharge);
    }

    //府県補助金
    function setReductionMst($reductionMst, $income, $setMoney, $kirikae, $rank) {
        $retMoney = 0;
        $retRank = "";
        if (!is_array($reductionMst)) {
            return array($retMoney, $retRank, "", "", "");
        } else if ($kirikae == 'rank' && $rank == 'XX') {
            return array($retMoney, $rank, "", "", "");
        }

        $nyugakukin = $setMoney;
        foreach ($reductionMst as $key => $val) {
            if ($kirikae == 'rank') {
                $flg = ($rank == $val["INCOME_RANK"]) ? true : false;
            } else {
                $flg = ($val["INCOME_LOW"] <= $income && $val["INCOME_HIGH"] >= $income) ? true : false;
            }

            if ($flg) {
                if ($val["MONEY_DIV"] == "1") {
                    $retMoney = max($nyugakukin, 0);
                    if ($retMoney > 0) {
                        $retMoney = $retMoney * $val["NUMERATOR"];
                        $retMoney = $retMoney / $val["DENOMINATOR"];
                    }
                    return array($retMoney, $val["INCOME_RANK"], $val["MONEY_DIV"], "", $val["REDUCTION_SEQ"]);
                } else if ($val["MONEY_DIV"] == "2") {
                    $retMoney = min($val["REDUCTIONMONEY"], $nyugakukin);
                    $retMoney = max($retMoney, 0);
                    return array($retMoney, $val["INCOME_RANK"], $val["MONEY_DIV"], "", $val["REDUCTION_SEQ"]);
                }
            }
        }
        return array($retMoney, $retRank, "", "", "");
    }

    //減免
    function setReductionStd($reducStd, $nyugakukin, $div) {
        $retMoney = 0;
        if (!is_array($reducStd)) {
            return array($nyugakukin, $retMoney);
        }
        if ($reducStd["MONEY_DIV"] == "1") {
            $tmp = $nyugakukin * $reducStd["NUMERATOR"];
            $retMoney = $tmp / $reducStd["DENOMINATOR"];
        } else if ($reducStd["MONEY_DIV"] == "2") {
            $retMoney = min($nyugakukin, $reducStd["MONEY"]);
        }
        if ($div == "saki") {
            $retNyugakukin = $nyugakukin - $retMoney;
        } else {
            $retNyugakukin = $nyugakukin;
        }
        return array($retNyugakukin, $retMoney);
    }

    function getReductionMoneyCal($reducStd, $nyugakukin) {
        if ($nyugakukin <= 0) {
            return $nyugakukin;
        }
        list($sYear, $sMonth, $sDay) = explode("-", $reducStd["S_YEAR_MONTH"]);
        list($eYear, $eMonth, $eDay) = explode("-", $reducStd["E_YEAR_MONTH"]);
        $sMonth = $sMonth < "04" ? $sMonth + 12 : $sMonth;
        $eMonth = $eMonth < "04" ? $eMonth + 12 : $eMonth;
        return array($nyugakukin, $sMonth, $eMonth);
    }

}
?>
