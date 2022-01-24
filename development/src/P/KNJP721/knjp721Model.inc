<?php

require_once('for_php7.php');
//生徒授業料軽減
class knjp721Model extends model
{
    public $cmd;                   //一覧表示時のcmd
    public $search = array();
    public $income     = array();
    public $schregno   = array();
    public $field      = array();
    public $lastColumn;//CSVの最後の列の値

    public function knjp721Model()
    {
    }
    public function init()
    {
        if (VARS::request("PROGRAMID")) {
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
        $this->decMonth = array("COUNTRY_DEC_MONTH1"        => VARS::post("COUNTRY_DEC_MONTH1"),
                                "COUNTRY_DEC_MONTH2"        => VARS::post("COUNTRY_DEC_MONTH2"),
                                "PREF_DEC_MONTH1"           => VARS::post("PREF_DEC_MONTH1"),
                                "PREF_DEC_MONTH2"           => VARS::post("PREF_DEC_MONTH2")
                             );
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
                               "REDUC_INCOME_1"         => VARS::post("REDUC_INCOME_1"),
                               "REDUC_INCOME_2"         => VARS::post("REDUC_INCOME_2"),
                               "G_PREF_CD"              => VARS::post("G_PREF_CD"),
                               "COUNTRY_RANK_1"         => VARS::post("COUNTRY_RANK_1"),
                               "COUNTRY_RANK_2"         => VARS::post("COUNTRY_RANK_2"),
                               "COUNTRY_MONEY_1"        => VARS::post("COUNTRY_MONEY_1"),
                               "COUNTRY_ADD_MONEY_1"    => VARS::post("COUNTRY_ADD_MONEY_1"),
                               "COUNTRY_MONEY_2"        => VARS::post("COUNTRY_MONEY_2"),
                               "COUNTRY_ADD_MONEY_2"    => VARS::post("COUNTRY_ADD_MONEY_2"),
                               "PREF_RANK_1"            => VARS::post("PREF_RANK_1"),
                               "PREF_RANK_2"            => VARS::post("PREF_RANK_2"),
                               "PREF_MONEY_1"           => VARS::post("PREF_MONEY_1"),
                               "PREF_MONEY_2"           => VARS::post("PREF_MONEY_2"),
                               "BURDEN_CHARGE_1"        => VARS::post("BURDEN_CHARGE_1"),
                               "BURDEN_CHARGE_2"        => VARS::post("BURDEN_CHARGE_2"),
                               "OBJ_NAME"               => VARS::post("OBJ_NAME"),
                               "SOEJI"                  => VARS::post("SOEJI")
                              );

        $checkGrade = $this->income["GRADE"] ? $this->income["GRADE"] : $this->search["GRADE"];
        $this->mstLastYear = $checkGrade == "01" ? CTRL_YEAR : CTRL_YEAR - 1;

        //画面のコンテンツ
        $this->field  = array("REDUCTION_SEQ_1"         => VARS::post("REDUCTION_SEQ_1"),
                              "REDUC_INCOME_1"          => VARS::post("REDUC_INCOME_1"),
                              "REDUC_INCOME_1_1"        => VARS::post("REDUC_INCOME_1_1"),
                              "REDUC_INCOME_1_2"        => VARS::post("REDUC_INCOME_1_2"),
                              "REDUC_INCOME_1_3"        => VARS::post("REDUC_INCOME_1_3"),
                              "REDUC_INCOME_1_4"        => VARS::post("REDUC_INCOME_1_4"),
                              "REDUC_INCOME_2"          => VARS::post("REDUC_INCOME_2"),
                              "REDUC_INCOME_2_1"        => VARS::post("REDUC_INCOME_2_1"),
                              "REDUC_INCOME_2_2"        => VARS::post("REDUC_INCOME_2_2"),
                              "REDUC_INCOME_2_3"        => VARS::post("REDUC_INCOME_2_3"),
                              "REDUC_INCOME_2_4"        => VARS::post("REDUC_INCOME_2_4"),
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
                              "PREF_RANK_1"             => VARS::post("PREF_RANK_1"),
                              "PREF_MONEY_1"            => VARS::post("PREF_MONEY_1"),
                              "PREF_FLG_1"              => VARS::post("PREF_FLG_1"),
                              "PREF_RANK_2"             => VARS::post("PREF_RANK_2"),
                              "PREF_MONEY_2"            => VARS::post("PREF_MONEY_2"),
                              "PREF_FLG_2"              => VARS::post("PREF_FLG_2"),
                              "REDUCTION_JUGYOURYOU1"   => VARS::post("REDUCTION_JUGYOURYOU1"),
                              "REDUC_SCHOOL_FLG_1"      => VARS::post("REDUC_SCHOOL_FLG_1"),
                              "REDUCTION_JUGYOURYOU2"   => VARS::post("REDUCTION_JUGYOURYOU2"),
                              "REDUC_SCHOOL_FLG_2"      => VARS::post("REDUC_SCHOOL_FLG_2"),
                              "REDUC_REMARK"            => VARS::post("REDUC_REMARK")
                            );

        //異動情報,奨学金情報の参照
        $this->genmenTransfer = array("SCHREGNO"  =>  VARS::post("SCHREGNO"),
                                      "SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND")
                                     );

        //軽減対象都道府県設定値
        $this->config  = array("REDUC_PREF"      =>  VARS::post("REDUC_PREF"),
                               "RAD_REDUC_PREF"  =>  VARS::post("RAD_REDUC_PREF")
                              );

        //検索対象都道府県
        if (!isset($this->search["RAD_PREF"])) {
            $this->search["RAD_PREF"] = 1;
        }
        //軽減対象都道府県設定値
        if (!isset($this->config["RAD_REDUC_PREF"])) {
            $this->config["RAD_REDUC_PREF"] = 1;
        }

        //確定済み
        $this->zumi  = array("COUNTRY_ZUMI"     =>  VARS::post("COUNTRY_ZUMI"),
                               "PREF_ZUMI"      =>  VARS::post("PREF_ZUMI")
                              );

        $this->calcFlg = array();
        if ($this->cmd == 'calc') {
            $this->calcFlg = VARS::post("CALC_FLG");
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    //入力項目チェック
    public function validateInput()
    {
        for ($i = 0; $i < get_count($this->slipno); $i++) {
            $this->field["REDUC_INCOME_1"][$i] = VARS::post("SET_REDUC_INCOME1_".$this->slipno[$i]);
            if (is_numeric($this->field["REDUC_INCOME_1"][$i]) && strlen($this->field["REDUC_INCOME_1"][$i]) > 8) {
                $this->setWarning('MSG901', "課税総取得額　伝票番号：" .$this->slipno[$i]);
                return false;
            }
            $this->field["REDUC_INCOME_2"][$i] = VARS::post("SET_REDUC_INCOME2_".$this->slipno[$i]);
            if (is_numeric($this->field["REDUC_INCOME_2"][$i]) && strlen($this->field["REDUC_INCOME_2"][$i]) > 8) {
                $this->setWarning('MSG901', "課税総取得額　伝票番号：" .$this->slipno[$i]);
                return false;
            }
            if (strlen($this->field["REDUC_REMARK"][$i]) > 30) {
                $this->setWarning('MSG901', "備考　伝票番号：" .$this->slipno[$i]);
                return false;
            }
        }
        return true;
    }

    /**
     * 更新用のモデルを返します
    */
    public function &getUpdateModel()
    {
        //セット
        for ($i = 0; $i < get_count($this->slipno); $i++) {
            $this->slipSchoolStd[$this->slipno[$i]]["REDUCTION_JUGYOURYOU1"]    = VARS::post("REDUCTION_JUGYOURYOU1_".$this->slipno[$i]);
            $this->slipSchoolStd[$this->slipno[$i]]["REDUCTION_JUGYOURYOU2"]    = VARS::post("REDUCTION_JUGYOURYOU2_".$this->slipno[$i]);

            $this->field["PAID_FLG1"][$this->slipno[$i]]        = VARS::post("PAID_FLG1_".$this->slipno[$i]);
            $this->field["PAID_FLG2"][$this->slipno[$i]]        = VARS::post("PAID_FLG2_".$this->slipno[$i]);

            $this->field["SCH_PAID_FLG1"][$this->slipno[$i]]    = VARS::post("SCH_PAID_FLG1_".$this->slipno[$i]);
            $this->field["SCH_PAID_FLG2"][$this->slipno[$i]]    = VARS::post("SCH_PAID_FLG2_".$this->slipno[$i]);

            $this->field["BURDEN_CHARGE1"][$this->slipno[$i]]   = VARS::post("BURDEN_CHARGE1_".$this->slipno[$i]);
            $this->field["BURDEN_CHARGE2"][$this->slipno[$i]]   = VARS::post("BURDEN_CHARGE2_".$this->slipno[$i]);
            $this->field["TOTAL_BURDEN_CHARGE"][$this->slipno[$i]]   = VARS::post("TOTAL_BURDEN_CHARGE_".$this->slipno[$i]);

            $this->field["MAX_COUNTRY_JUGYOURYOU1"][$this->slipno[$i]]   = VARS::post("MAX_COUNTRY_JUGYOURYOU1_".$this->slipno[$i]);
            $this->field["MAX_COUNTRY_JUGYOURYOU2"][$this->slipno[$i]]   = VARS::post("MAX_COUNTRY_JUGYOURYOU2_".$this->slipno[$i]);
        }

        if ($this->validateInput()) {
            knjp721Query::update($this);
            $this->setMessage("MSG201");
        }
    }

    //再計算処理
    public function &getCalcModel(&$db, $getRow)
    {
        $contryJugyouryou1 = $contryJugyouryou2 = 0;
        $prefOnlyJugyouryou1 = $prefOnlyJugyouryou2 = 0;
        $setGenmenItemAto = array();
        if (is_array($this->slipPlan[$getRow["SLIP_NO"]])) {
            foreach ($this->slipPlan[$getRow["SLIP_NO"]] as $slipPlanKey => $slipPlanVal) {
                list($gakunokin_div, $reduction_div, $is_reduction_school) = explode(':', $slipPlanVal["DIV"]);
                $flg = ($gakunokin_div == '1') ? 1 : 2;

                if ($reduction_div == '1') {
                    ${"contryJugyouryou".$flg} += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                } elseif ($reduction_div == '2') {
                    ${"prefOnlyJugyouryou".$flg} += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                }

                if ($is_reduction_school == '1') {
                    if (is_array($this->reducStdAto[$getRow["SCHREGNO"]])) {
                        foreach ($this->reducStdAto[$getRow["SCHREGNO"]] as $reducKey => $reducVal) {
                            list($jugyou, $genmenJugyou) = $this->setReductionStd($reducVal, $slipPlanVal["MONEY"], "ato");
                            $slipPlanVal["MONEY"] = $jugyou;
                            $setGenmenItemAto[] = $slipPlanKey;
                        }
                    }
                }
            }
        }

        $setRow = array();
        $moneyDiv1 = $moneyDiv2 = "";
        $redMoney1 = $redMoney2 = 0;
        for ($soeji = 1; $soeji <= 2; $soeji++) {
            $setRow["BURDEN_CHARGE".$soeji] = $getRow["BURDEN_CHARGE".$soeji];

            //入金済みは処理しない
            if ($getRow["PAID_FLG".$soeji]) {
                continue;
            }

            $numerator = ($soeji == "1") ? "1" : "3";
            $reducflg = false;

            if (!$getRow["REDUCTION_SCHREGNO"]) {
                if (!$getRow["COUNTRY_RANK_".$soeji]) {
                    $getRow["COUNTRY_RANK_".$soeji] = 'XX';
                }
                if (!$getRow["PREF_RANK_".$soeji]) {
                    $getRow["PREF_RANK_".$soeji] = 'XX';
                }
            }

            //支援金
            $countryFlg = '';
            if ($getRow["COUNTRY_BASE_FLG_".$soeji] == "1" || $getRow["COUNTRY_ADD_FLG_".$soeji] == "1") {
                $countryFlg = 'kakutei';
            } elseif (strlen($getRow["REDUC_INCOME_".$soeji]) > 0) {
                $countryFlg = 'income';
                $reducflg = true;
            } elseif ($getRow["COUNTRY_RANK_".$soeji]) {
                $countryFlg = 'rank';
                $reducflg = true;
            }

            $countryMoney = $countryAddMoney = $countryMoneyTotal = 0;
            if ($countryFlg && $countryFlg != 'kakutei') {
                //基準額
                if ($countryFlg == 'rank' && $getRow["COUNTRY_RANK_".$soeji] == 'XX') {
                    list($row["REDUCTION_SEQ"], $row["COUNTRY_MONEY"], $row["COUNTRY_RANK"]) = array(null, 0, $getRow["COUNTRY_RANK_".$soeji]);
                } else {
                    $query = knjp721Query::selectQueryReductionMonty($this, $getRow, $soeji, $countryFlg);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                }
                if (is_array($row)) {
                    foreach ($row as $key => $val) {
                        $setRow[$key."_".$soeji] = $row[$key];
                    }
                    if ($row["COUNTRY_MONEY"] > 0) {
                        $countryMoney = $row["COUNTRY_MONEY"];
                    }
                } else {
                    $setRow["COUNTRY_RANK_".$soeji] = "";
                }

                //加算額
                if ($countryFlg == 'rank' && $getRow["COUNTRY_RANK_".$soeji] == 'XX') {
                    list($row["REDUCTION_SEQ"], $row["COUNTRY_ADD_MONEY"]) = array(null, 0);
                } else {
                    $query = knjp721Query::selectQueryReductionAddMonty($this, $getRow, $soeji, $countryFlg);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                }
                if (is_array($row)) {
                    foreach ($row as $key => $val) {
                        $setRow[$key."_".$soeji] = $row[$key];
                        if ($key == "COUNTRY_ADD_MONEY" && $val > 0) {
                            $countryAddMoney += $val;
                        }
                    }
                }
            } elseif ($countryFlg == 'kakutei') {
                $countryMoney       = max($getRow["COUNTRY_MONEY_".$soeji], 0);
                $countryAddMoney    = max($getRow["COUNTRY_ADD_MONEY_".$soeji], 0);
            }
            $monthCnt = ($soeji == "1") ? 3 : 9;
            $countryMoneyTotal = ($countryMoney + $countryAddMoney) * $monthCnt;

            //補助金用授業料、授業料等
            for ($flg = 1; $flg <= 2; $flg++) {
                $tmpPrefJugyouryou = ${"contryJugyouryou".$flg} + ${"prefOnlyJugyouryou".$flg};
                ${"prefJugyouryou".$flg} = ($tmpPrefJugyouryou > 0) ? $tmpPrefJugyouryou / 4 * $numerator : 0;
            }

            $refYearDiv = $this->prefGradeMst[$getRow["G_PREF_CD"]]["REFER_YEAR_DIV".$soeji];
            $refYearDiv1 = $this->prefGradeMst[$getRow["G_PREF_CD"]]["REFER_YEAR_DIV1"];
            $refYearDiv2 = $this->prefGradeMst[$getRow["G_PREF_CD"]]["REFER_YEAR_DIV2"];

            //補助金
            $prefFlg = '';
            if ($getRow["PREF_FLG_".$soeji] == "1") {
                $prefFlg = 'kakutei';
            } elseif (strlen($getRow["REDUC_INCOME_".$refYearDiv]) > 0) {
                $prefFlg = 'income';
                $reducflg = true;
            } elseif ($getRow["PREF_RANK_".$soeji]) {
                $prefFlg = 'rank';
                $reducflg = true;
            }

            //標準授業料
            $hyojunJugyouryou = 0;
            if ($this->prefGradeMst[$getRow["G_PREF_CD"]]["STANDARD_SCHOOL_FEE"] > 0) {
                $hyojunJugyouryou = $this->prefGradeMst[$getRow["G_PREF_CD"]]["STANDARD_SCHOOL_FEE"] / 4 * $numerator;
            }

            //支援金
            $contryJugyouryou = $contryJugyouryou1 + $contryJugyouryou2;
            $contryJugyouryou = ($contryJugyouryou > 0) ? $contryJugyouryou / 4 * $numerator : 0;
            $tmpCountryMoney = ($countryMoney + $countryAddMoney) * 12 / 4 * $numerator;
            $tmpCountryMoney = ($contryJugyouryou - $tmpCountryMoney > 0) ? $tmpCountryMoney : $contryJugyouryou;

            //減免後・学校負担金算出(1)
            $tmpReductMoney = $prefJugyouryou1;
            $setBurdenCharge = 0;

            $setMoneyArray = array("PREF_MONEY"         => $prefJugyouryou1,
                                   "PREF_MONEY_TOU"     => $prefJugyouryou2,
                                   "SCHOOL_FEE"         => $hyojunJugyouryou,
                                   "COUNTRY_MONEY"      => ($contryJugyouryou1 > 0) ? $contryJugyouryou1 / 4 * $numerator : 0,
                                   "COUNTRY_MONEY_TOU"  => ($contryJugyouryou2 > 0) ? $contryJugyouryou2 / 4 * $numerator : 0,
                                   "PREFONLY_MONEY"     => ($prefOnlyJugyouryou1 > 0) ? $prefOnlyJugyouryou1 / 4 * $numerator : 0,
                                   "PREFONLY_MONEY_TOU" => ($prefOnlyJugyouryou2 > 0) ? $prefOnlyJugyouryou2 / 4 * $numerator : 0);

            list($calcPrefMoney, $prefRank, $moneyDiv, $burdenChargeFlg, $calcPrefMoneyOnly) = $this->setReductionMst($this->reductionMst[$getRow["G_PREF_CD"]], $getRow["REDUC_INCOME_".$refYearDiv], $soeji, $setMoneyArray, $prefFlg, $getRow["PREF_RANK_".$soeji]);
            list($prefMoney, $burdenCharge) = $this->setReductionPref($countryMoney, $countryAddMoney, $calcPrefMoney, $calcPrefMoneyOnly, $setMoneyArray, $moneyDiv, $soeji, $burdenChargeFlg);

            $setRow["PREF_RANK_".$soeji] = $prefRank;
            if ($prefFlg == 'kakutei') {
                $setRow["PREF_MONEY_".$soeji] = $getRow["PREF_MONEY_".$soeji];
                $burdenCharge = $getRow["BURDEN_CHARGE".$soeji];
            } else {
                $setRow["PREF_MONEY_".$soeji] = (!strlen($prefRank) && !$prefMoney && $prefRank != 'XX') ? "" : $prefMoney;
            }

            ${"moneyDiv".$soeji} = $moneyDiv;

            ${"redMoney".$soeji} = $calcPrefMoney;

            //減免後・学校負担金算出(2)
            if ($hyojunJugyouryou > 0 && $burdenChargeFlg && $prefJugyouryou1 > $hyojunJugyouryou && $moneyDiv) {
                //授業料に標準授業料をセット
                $tmpReductMoney = $hyojunJugyouryou;
            }
            $tmpReductMoney -= $tmpCountryMoney;
            $tmpReductMoney = max($tmpReductMoney, 0);
            $tmpReductMoney += $prefJugyouryou2;
            $tmpReductMoney -= $prefMoney;
            $tmpReductMoney = max($tmpReductMoney, 0);
            if ($moneyDiv == '3') {
                $tmpReductMoney -= $calcPrefMoney;
            }
            $setBurdenCharge = max($burdenCharge, 0);

            //減免対象者（後）
            if (!get_count($setGenmenItemAto)) {
                $reducflg = false;
            }

            //減免後・学校負担金算出(3)
            $tmpReductMoney = max($tmpReductMoney, 0);
            if (is_array($this->reducStdAto[$getRow["SCHREGNO"]]) && $tmpReductMoney > 0) {
                foreach ($this->reducStdAto[$getRow["SCHREGNO"]] as $reducKey => $reducVal) {
                    list($jugyou, $genmenJugyou) = $this->setReductionStd($reducVal, $tmpReductMoney, "ato", $soeji);
                    $tmpReductMoney = $genmenJugyou;
                }
            }
            if ($getRow["REDUC_SCHOOL_FLG_".$soeji] == "1") {
                $setRow["REDUCTION_JUGYOURYOU".$soeji] = $getRow["REDUCTION_JUGYOURYOU".$soeji];
            } else {
                $setRow["REDUCTION_JUGYOURYOU".$soeji] = ($reducflg) ? intval($tmpReductMoney) : 0;
            }

            $setRow["BURDEN_CHARGE".$soeji] = $setBurdenCharge;
        }
        $setRow["TOTAL_BURDEN_CHARGE"] = $setRow["BURDEN_CHARGE1"] + $setRow["BURDEN_CHARGE2"];

        $this->setMessage("MSG202");
        return $setRow;
    }

    // 課税総取得変更のモデルを返します
    public function &getSendModel()
    {
        //金額入力
        $db = Query::dbCheckOut();

        if ($this->income["SOEJI"] == '1') {
            $setSoeji = "1";
            $setNumerator = "1";
        } else {
            $setSoeji = "2";
            $setNumerator = "3";
        }

        $json_array = array();

        $jugyouSaki = array();
        $contryJugyouryou1 = $contryJugyouryou2 = 0;
        $prefOnlyJugyouryou1 = $prefOnlyJugyouryou2 = 0;
        $setGenmenSaki = 0;
        $setGenmenItemAto = array();
        if (is_array($this->slipPlan[$this->income["SLIP_NO"]])) {
            foreach ($this->slipPlan[$this->income["SLIP_NO"]] as $slipPlanKey => $slipPlanVal) {
                list($gakunokin_div, $reduction_div, $is_reduction_school) = explode(':', $slipPlanVal["DIV"]);
                $flg = ($gakunokin_div == '1') ? "1" : "2";

                if ($reduction_div == '1') {
                    ${"contryJugyouryou".$flg} += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                } elseif ($reduction_div == '2') {
                    ${"prefOnlyJugyouryou".$flg} += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                }

                //減免先
                if ($is_reduction_school == '1' && is_array($this->reducStdSaki[$this->income["SCHREGNO"]])) {
                    foreach ($this->reducStdSaki[$this->income["SCHREGNO"]] as $reducKey => $reducVal) {
                        list($jugyou, $genmenJugyou) = $this->setReductionStd($reducVal, $slipPlanVal["MONEY"], "saki");
                        $slipPlanVal["MONEY"] = $jugyou;
                        $this->slipPlan[$this->income["SLIP_NO"]][$slipPlanKey]["KANSAN"] = $jugyou;
                        $setGenmenSaki += $genmenJugyou;
                    }
                }
                $jugyouSaki[$flg] += $slipPlanVal["MONEY"];

                //減免後
                if ($is_reduction_school == '1' && is_array($this->reducStdAto[$this->income["SCHREGNO"]])) {
                    foreach ($this->reducStdAto[$this->income["SCHREGNO"]] as $reducKey => $reducVal) {
                        list($jugyou, $genmenJugyou) = $this->setReductionStd($reducVal, $slipPlanVal["MONEY"], "ato");
                        $slipPlanVal["MONEY"] = $jugyou;
                        $setGenmenItemAto[] = $slipPlanKey;
                    }
                }
            }
        }

        $setRow = array();
        $moneyDiv1 = $moneyDiv2 = "";
        $redMoney1 = $redMoney2 = 0;
        for ($soeji = 1; $soeji <= 2; $soeji++) {
            $numerator = ($soeji == "1") ? "1" : "3";
            $reducflg = false;

            $reduc_flg = '';
            if ($this->income["OBJ_NAME"] == 'REDUC_INCOME_'.$soeji.'[]' && strlen($this->income["REDUC_INCOME_".$soeji]) > 0) {
                $reduc_flg = 'income';
                $reducflg = true;
            } elseif ($this->income["COUNTRY_RANK_".$soeji]) {
                $reduc_flg = 'rank';
                $reducflg = true;
            } elseif ($this->income["PREF_RANK_".$soeji]) {
                $reduc_flg = 'rank';
                $reducflg = true;
            }

            $countryMoney = $countryAddMoney = $countryMoneyTotal = 0;
            if ($reduc_flg) {
                //基準額
                if ($reduc_flg == 'rank' && $this->income["COUNTRY_RANK_".$soeji] == 'XX') {
                    list($row["REDUCTION_SEQ"], $row["COUNTRY_MONEY"], $row["COUNTRY_RANK"]) = array(null, 0, $this->income["COUNTRY_RANK_".$soeji]);
                } else {
                    $query = knjp721Query::selectQueryReductionMonty($this, $this->income, $soeji, $reduc_flg);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                }

                $setBaseMoney = 0;
                if (is_array($row)) {
                    foreach ($row as $key => $val) {
                        if ($soeji == $setSoeji) {
                            $json_array[] = "'{$key}':'{$val}'";
                        }
                        if ($key == "COUNTRY_MONEY") {
                            $setBaseMoney = $val;
                        }
                    }
                    $countryMoney = $row["COUNTRY_MONEY"];
                } else {
                    if ($soeji == $setSoeji) {
                        $json_array[] = "'COUNTRY_RANK':''";
                    }
                }

                //加算額
                if ($this->income["COUNTRY_RANK_".$soeji] == 'XX') {
                    list($row["REDUCTION_SEQ"], $row["COUNTRY_ADD_MONEY"]) = array(null, 0);
                } else {
                    $query = knjp721Query::selectQueryReductionAddMonty($this, $this->income, $soeji, $reduc_flg);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                }

                if (is_array($row)) {
                    foreach ($row as $key => $val) {
                        if ($soeji == $setSoeji) {
                            $json_array[] = "'{$key}':'{$val}'";
                        }
                        if ($key == "COUNTRY_ADD_MONEY") {
                            $setMoney = $val;
                        }
                    }
                    $countryAddMoney = $setMoney;
                }
            }

            $monthCnt = ($soeji == "1") ? 3 : 9;
            $countryMoneyTotal = ($countryMoney + $countryAddMoney) * $monthCnt;

            //補助金用授業料、授業料等
            for ($flg = 1; $flg <= 2; $flg++) {
                $tmpPrefJugyouryou = ${"contryJugyouryou".$flg} + ${"prefOnlyJugyouryou".$flg};
                ${"prefJugyouryou".$flg} = ($tmpPrefJugyouryou > 0) ? $tmpPrefJugyouryou / 4 * $numerator : 0;
            }

            //標準授業料
            $hyojunJugyouryou = 0;
            if ($this->prefGradeMst[$this->income["G_PREF_CD"]]["STANDARD_SCHOOL_FEE"] > 0) {
                $hyojunJugyouryou = $this->prefGradeMst[$this->income["G_PREF_CD"]]["STANDARD_SCHOOL_FEE"] / 4 * $numerator;
            }

            //支援金
            $contryJugyouryou = $contryJugyouryou1 + $contryJugyouryou2;
            $contryJugyouryou = ($contryJugyouryou > 0) ? $contryJugyouryou / 4 * $numerator : 0;
            $tmpCountryMoney = ($countryMoney + $countryAddMoney) * 12 / 4 * $numerator;
            $tmpCountryMoney = ($contryJugyouryou - $tmpCountryMoney > 0) ? $tmpCountryMoney : $contryJugyouryou;

            //減免後・学校負担金算出(1)
            $tmpReductMoney = $prefJugyouryou1;
            $setBurdenCharge = 0;

            //補助金
            $prefFlg = '';

            //確定済み
            $countryZumiArray   = explode(',', $this->zumi["COUNTRY_ZUMI"]);
            $prefZumiArray      = explode(',', $this->zumi["PREF_ZUMI"]);
            $zumiArray          = array_merge($countryZumiArray, $prefZumiArray);

            //府県補助金
            $setMoneyArray = array("PREF_MONEY"         => $prefJugyouryou1,
                                   "PREF_MONEY_TOU"     => $prefJugyouryou2,
                                   "SCHOOL_FEE"         => $hyojunJugyouryou,
                                   "COUNTRY_MONEY"      => ($contryJugyouryou1 > 0) ? $contryJugyouryou1 / 4 * $numerator : 0,
                                   "COUNTRY_MONEY_TOU"  => ($contryJugyouryou2 > 0) ? $contryJugyouryou2 / 4 * $numerator : 0,
                                   "PREFONLY_MONEY"     => ($prefOnlyJugyouryou1 > 0) ? $prefOnlyJugyouryou1 / 4 * $numerator : 0,
                                   "PREFONLY_MONEY_TOU" => ($prefOnlyJugyouryou2 > 0) ? $prefOnlyJugyouryou2 / 4 * $numerator : 0);

            $refYearDiv = $this->prefGradeMst[$this->income["G_PREF_CD"]]["REFER_YEAR_DIV".$soeji];
            if ($refYearDiv) {
                $kirikae = ($this->income["OBJ_NAME"] == 'REDUC_INCOME_'.$soeji.'[]') ? 'income' : 'rank';
            } else {
                $kirikae = 'rank';
            }

            list($calcPrefMoney, $prefRank, $moneyDiv, $burdenChargeFlg, $calcPrefMoneyOnly) = $this->setReductionMst($this->reductionMst[$this->income["G_PREF_CD"]], $this->income["REDUC_INCOME_".$refYearDiv], $soeji, $setMoneyArray, $kirikae, $this->income["PREF_RANK_".$soeji]);
            list($prefMoney, $burdenCharge) = $this->setReductionPref($countryMoney, $countryAddMoney, $calcPrefMoney, $calcPrefMoneyOnly, $setMoneyArray, $moneyDiv, $soeji, $burdenChargeFlg);
            $prefMoney = max($prefMoney, 0);
            if ($soeji == $setSoeji) {
                if (in_array("PREF_FLG_".$soeji, $zumiArray)) {
                    $json_array[] = "'PREF_MONEY':'{$this->income["PREF_MONEY_".$soeji]}'";
                    $json_array[] = "'PREF_RANK':'{$this->income["PREF_RANK_".$soeji]}'";
                } else {
                    $json_array[] = "'PREF_MONEY':'{$prefMoney}'";
                    $json_array[] = "'PREF_RANK':'{$prefRank}'";
                }
                $json_array[] = "'JUGYOURYOU':'{$jugyouSaki[1]}'";
                $json_array[] = "'JUGYOURYOUTOU':'{$jugyouSaki[2]}'";
                $json_array[] = "'MONEY_DIV{$soeji}':'{$moneyDiv}'";
                $json_array[] = "'REDUCTIONMONEY{$soeji}':'{$calcPrefMoney}'";
            }

            //減免後・学校負担金算出(2)
            if ($hyojunJugyouryou > 0 && $burdenChargeFlg && $prefJugyouryou1 > $hyojunJugyouryou && $moneyDiv) {
                //授業料に標準授業料をセット
                $tmpReductMoney = $hyojunJugyouryou;
            }
            $tmpReductMoney -= $tmpCountryMoney;
            $tmpReductMoney = max($tmpReductMoney, 0);
            $tmpReductMoney += $prefJugyouryou2;
            $tmpReductMoney -= $prefMoney;
            $tmpReductMoney = max($tmpReductMoney, 0);
            if ($moneyDiv == '3') {
                $tmpReductMoney -= $calcPrefMoney;
            }
            $setBurdenCharge = max($burdenCharge, 0);

            //減免対象者（後）
            if (!get_count($setGenmenItemAto)) {
                $reducflg = false;
            }

            //減免後・学校負担金算出(3)
            $tmpReductMoney = max($tmpReductMoney, 0);
            if (is_array($this->reducStdAto[$this->income["SCHREGNO"]]) && $tmpReductMoney > 0) {
                foreach ($this->reducStdAto[$this->income["SCHREGNO"]] as $reducKey => $reducVal) {
                    list($jugyou, $genmenJugyou) = $this->setReductionStd($reducVal, $tmpReductMoney, "ato", $soeji);
                    $tmpReductMoney = $genmenJugyou;
                }
            }
            ${"reductJugyouryou".$soeji} = ($reducflg) ? intval($tmpReductMoney) : 0;

            $json_array[] = "'BURDEN_CHARGE{$soeji}':'{$setBurdenCharge}'";
        }
        $reductJugyouryou = $reductJugyouryou1 + $reductJugyouryou2;

        $json_array[] = "'REDUCTION_JUGYOURYOU_SAKI':'{$setGenmenSaki}'";
        $json_array[] = "'REDUCTION_JUGYOURYOU_ATO1':'{$reductJugyouryou1}'";
        $json_array[] = "'REDUCTION_JUGYOURYOU_ATO2':'{$reductJugyouryou2}'";

        if (get_count($json_array) > 0) {
            $json = implode(',', $json_array);
            echo "{ 'result':true,{$json} }";
        } else {
            echo "{ 'result':false }";
        }
        Query::dbCheckIn($db);
    }

    // 減免情報のモデルを返します
    public function &getSendModelGenmen()
    {
        $db = Query::dbCheckOut();
        $returnValue = "";

        //減免情報取得
        $query = knjp721Query::selectQueryGenmen($this, $this->genmenTransfer["SCHREGNO"], $this->genmenTransfer["SCHOOL_KIND"]);
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
    public function &getSendModelTransfer()
    {
        $db = Query::dbCheckOut();
        $returnValue = "";

        //異動情報取得
        $query = knjp721Query::selectQueryTransfer($this, $this->genmenTransfer["SCHREGNO"]);
        $result2 = $db->query($query);
        while ($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            //T1.TRANSFERCD,T1.TRANSFER_SDATE,T1.TRANSFER_EDATE,T2.NAME1
            $returnValue .= "&nbsp;".$row["TRANSFER_SDATE"];
            $returnValue .= ($row["TRANSFER_EDATE"] == "")? "" : "～".$row["TRANSFER_EDATE"];
            $returnValue .= " ".$row["NAME1"]."<BR>";
        }

        if ($returnValue != "") {
            echo $returnValue;
        } else {
            echo "NOTFOUND";
        }
        Query::dbCheckIn($db);
    }

    public function setReductionPref($countryMoney, $countryAddMoney, $prefMoney, $prefMoneyOnly, $setMoneyArray, $moneyDiv, $soeji, $burdenChargeFlg)
    {
        $numerator = ($soeji == "1") ? "1" : "3";

        //支援金計
        $contryJugyouryou = $setMoneyArray["COUNTRY_MONEY"] + $setMoneyArray["COUNTRY_MONEY_TOU"];
        //差分
        $diff = 0;
        if ($setMoneyArray["SCHOOL_FEE"] > 0 && $setMoneyArray["COUNTRY_MONEY"] + $setMoneyArray["PREFONLY_MONEY"] > $setMoneyArray["SCHOOL_FEE"] && $moneyDiv) {
            $diff = $setMoneyArray["COUNTRY_MONEY"] + $setMoneyArray["PREFONLY_MONEY"] - $setMoneyArray["SCHOOL_FEE"];
        }

        //授業料ベースの計算
        $countryMoneyTotal = ($countryMoney + $countryAddMoney) * 12 / 4 * $numerator;
        $calJugyou = $contryJugyouryou;
        $calJugyou = $calJugyou - $countryMoneyTotal;
        $calJugyou = max($calJugyou, 0);
        //補助金用授業料から標準授業料との差分を引く
        $calJugyou = $calJugyou + $setMoneyArray["PREFONLY_MONEY"]- $diff;
        $calJugyou = max($calJugyou, 0);
        $calJugyou = $calJugyou + $setMoneyArray["PREFONLY_MONEY_TOU"];

        //補助金ベースの計算
        $retMoney = 0;
        if ($moneyDiv == "1") {
            if ($prefMoneyOnly - $countryMoneyTotal > 0) {
                $prefMoney = $prefMoney - $countryMoneyTotal;
            } else {
                $prefMoney = $prefMoney - $prefMoneyOnly;
            }
            $retMoney = min($prefMoney, $calJugyou);
        } elseif ($moneyDiv == "2") {
            //絶対額
            $retMoney = min($prefMoney, $calJugyou);
        } elseif ($moneyDiv == "3") {
            //保護者負担額
            $retMoney = ($calJugyou - $prefMoney > 0) ? $calJugyou - $prefMoney : 0;
        }
        $retMoney = ($retMoney > 0) ? $retMoney : 0;

        //負担金
        $burdenCharge = 0;
        if ($burdenChargeFlg) {
            $burdenCharge = $diff;
        }

        return array($retMoney, $burdenCharge);
    }

    //府県補助金
    public function setReductionMst($reductionMst, $income, $div, $setMoneyArray, $kirikae, $rank)
    {
        $retMoney = 0;
        $retRank = "";
        if (!is_array($reductionMst)) {
            return array($retMoney, $retRank, "", "", "");
        } elseif (($kirikae == 'rank' && $rank == 'XX') || $kirikae == 'kakutei') {
            return array($retMoney, $rank, "", "", "");
        }

        $jugyouryou = $jugyouryouOnly = $setMoneyArray["PREF_MONEY"];
        if ($setMoneyArray["SCHOOL_FEE"] > 0 && $setMoneyArray["PREF_MONEY"] > $setMoneyArray["SCHOOL_FEE"]) {
            $jugyouryou = $jugyouryouOnly = $setMoneyArray["SCHOOL_FEE"];
        }
        $jugyouryou += $setMoneyArray["PREF_MONEY_TOU"];
        foreach ($reductionMst as $key => $val) {
            if ($kirikae == 'rank') {
                $flg = ($rank == $val["INCOME_RANK".$div]) ? true : false;
            } else {
                $flg = ($val["INCOME_LOW".$div] <= $income && $val["INCOME_HIGH".$div] >= $income) ? true : false;
            }

            if ($flg) {
                if ($val["MONEY_DIV".$div] == "1") {
                    $retMoney = max($jugyouryou, 0);
                    $retMoneyOnly = max($jugyouryouOnly, 0);
                    if ($retMoney > 0) {
                        $retMoney = $retMoney / $val["DENOMINATOR".$div];
                        $retMoney = $retMoney * $val["NUMERATOR".$div];
                        $retMoneyOnly = $retMoneyOnly / $val["DENOMINATOR".$div];
                        $retMoneyOnly = $retMoneyOnly * $val["NUMERATOR".$div];
                    }
                    return array($retMoney, $val["INCOME_RANK".$div], $val["MONEY_DIV".$div], $val["BURDEN_CHARGE_FLG".$div], $retMoneyOnly);
                } elseif ($val["MONEY_DIV".$div] == "2" || $val["MONEY_DIV".$div] == "3") {
                    $retMoney = min($val["REDUCTIONMONEY_".$div], $jugyouryou);
                    $retMoney = max($retMoney, 0);
                    return array($retMoney, $val["INCOME_RANK".$div], $val["MONEY_DIV".$div], $val["BURDEN_CHARGE_FLG".$div], "");
                }
            }
        }
        return array($retMoney, $retRank, "", "", "");
    }

    //減免
    public function setReductionStd($reducStd, $jugyouRyou, $div, $soeji = "")
    {
        $retMoney = 0;
        if (!is_array($reducStd)) {
            return array($jugyouRyou, $retMoney);
        }
        if ($reducStd["MONEY_DIV"] == "1") {
            $jugyou = $jugyouRyou / $reducStd["DENOMINATOR"];
            $retMoney = $jugyou * $reducStd["NUMERATOR"];
        } elseif ($reducStd["MONEY_DIV"] == "2") {
            $money = $reducStd["MONEY"];
            if ($soeji) {
                $numerator = ($soeji == "1") ? 1 : 3;
                $money = ($reducStd["MONEY"] > 0) ? $reducStd["MONEY"] / 4 * $numerator : 0;
            }
            $retMoney = min($jugyouRyou, $money);
        }
        if ($div == "saki") {
            $retJugyouRyou = $jugyouRyou - $retMoney;
        } else {
            $retJugyouRyou = $jugyouRyou;
        }
        return array($retJugyouRyou, $retMoney);
    }

    public function getReductionMoneyCal($reducStd, $jugyouRyou)
    {
        if ($jugyouRyou <= 0) {
            return $jugyouRyou;
        }
        list($sYear, $sMonth, $sDay) = explode("-", $reducStd["S_YEAR_MONTH"]);
        list($eYear, $eMonth, $eDay) = explode("-", $reducStd["E_YEAR_MONTH"]);
        $sMonth = $sMonth < "04" ? $sMonth + 12 : $sMonth;
        $eMonth = $eMonth < "04" ? $eMonth + 12 : $eMonth;
        return array($jugyouRyou, $sMonth, $eMonth);
    }

    //ＣＳＶ出力
    public function getCsvModel()
    {
        //ヘッダー
        $header = array();
        $header["GHA"]                      = array("", "年組番");
        $header["NAME_SHOW"]                = array("", "氏名");
        $header["SCHREGNO"]                 = array("", "学籍番号");
        $header["SLIP_NO"]                  = array("", "伝票番号");
        $header["REDUC_INCOME_1"]           = array("前年度", "所得割額1");
        $header["REDUC_INCOME_1_1"]         = array("前年度", "市町村所得割1");
        $header["REDUC_INCOME_1_2"]         = array("前年度", "都道府県所得割1");
        $header["REDUC_INCOME_1_3"]         = array("前年度", "市町村所得割2");
        $header["REDUC_INCOME_1_4"]         = array("前年度", "都道府県所得割2");
        $header["REDUC_INCOME_2"]           = array("今年度", "所得割額1");
        $header["REDUC_INCOME_2_1"]         = array("今年度", "市町村所得割1");
        $header["REDUC_INCOME_2_2"]         = array("今年度", "都道府県所得割1");
        $header["REDUC_INCOME_2_3"]         = array("今年度", "市町村所得割2");
        $header["REDUC_INCOME_2_4"]         = array("今年度", "都道府県所得割2");

        $header["JUGYOURYOU"]               = array("", "授業料");
        $header["JUGYOURYOUTOU"]            = array("", "授業料等");

        $header["COUNTRY_RANK_1"]           = array("前期支援金", "ランク");
        $header["COUNTRY_MONEY_1"]          = array("前期支援金", "基準額");
        $header["COUNTRY_ADD_MONEY_1"]      = array("前期支援金", "加算額");
        $header["COUNTRY_RANK_2"]           = array("後期支援金", "ランク");
        $header["COUNTRY_MONEY_2"]          = array("後期支援金", "基準額");
        $header["COUNTRY_ADD_MONEY_2"]      = array("後期支援金", "加算額");
        $header["COUNTRY_TOTAL_MONEY"]      = array("", "支援額合計");

        $header["G_PREF"]                   = array("", "補助対象都道府県");
        $header["PREF_RANK_1"]              = array("補助額1", "ランク");
        $header["PREF_MONEY_1"]             = array("補助額1", "補助額");
        $header["PREF_RANK_2"]              = array("補助額2", "ランク");
        $header["PREF_MONEY_2"]             = array("補助額2", "補助額");
        $header["PREF_TOTAL_MONEY"]         = array("", "補助額合計");

        $header["TOTAL_BURDEN_CHARGE"]      = array("", "負担金");
        $header["ADJUSTMENT_MONEY"]         = array("", "調整金");

        $header["REDUCTION_JUGYOURYOU1"]    = array("", "減免額1");
        $header["REDUCTION_JUGYOURYOU2"]    = array("", "減免額2");
        $header["GENMEN_TRANSFER_MARK"]     = array("", "減免情報");
        $header["REMARK"]                   = array("", "備考");
        $header[$this->lastColumn]          = array("", $this->lastColumn);

        $contents  = "";        //クリア
        $dataFlg = false;       //出力フラグ

        $header1 = $header2 = array();
        foreach ($header as $key => $val) {
            list($header1[], $header2[]) = $val;
        }
        $contents .= implode($header1, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjp721Query::selectQuery($this, 'csv');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $row["GHA"] = $row["HR_NAMEABBV"].'-'.$row["ATTENDNO"];

            //授業料・授業料等
            $jugyouSaki = array();
            $setGenmenSaki = 0;
            if (is_array($this->slipPlan[$row["SLIP_NO"]])) {
                foreach ($this->slipPlan[$row["SLIP_NO"]] as $slipPlanKey => $slipPlanVal) {
                    list($gakunokin_div, $reduction_div, $is_reduction_school) = explode(':', $slipPlanVal["DIV"]);
                    $flg = ($gakunokin_div == '1') ? 1 : 2;
                    if ($is_reduction_school == '1') {
                        if (is_array($this->reducStdSaki[$row["SCHREGNO"]])) {
                            foreach ($this->reducStdSaki[$row["SCHREGNO"]] as $reducKey => $reducVal) {
                                list($jugyou, $genmenJugyou) = $this->setReductionStd($reducVal, $slipPlanVal["MONEY"], "saki");
                                $slipPlanVal["MONEY"] = $jugyou;
                                $setGenmenSaki += $genmenJugyou;
                            }
                        }
                    }
                    //授業料、授業料等をそれぞれ合算
                    $jugyouSaki[$flg] += $slipPlanVal["MONEY"];
                }
            }

            $row["JUGYOURYOU"]      = $jugyouSaki[1];
            $row["JUGYOURYOUTOU"]   = $jugyouSaki[2];

            $row["COUNTRY_RANK_1"] = ($row["COUNTRY_RANK_1"]) ? $this->countryRank[$row["COUNTRY_RANK_1"]] : "";
            $row["COUNTRY_RANK_2"] = ($row["COUNTRY_RANK_2"]) ? $this->countryRank[$row["COUNTRY_RANK_2"]] : "";

            $row["PREF_RANK_1"] = ($row["PREF_RANK_1"]) ? $this->prefRank[$row["PREF_RANK_1"]] : "";
            $row["PREF_RANK_2"] = ($row["PREF_RANK_2"]) ? $this->prefRank[$row["PREF_RANK_2"]] : "";

            $row["COUNTRY_TOTAL_MONEY"] = ($row["COUNTRY_MONEY_1"] + $row["COUNTRY_ADD_MONEY_1"]) * 3 + ($row["COUNTRY_MONEY_2"] + $row["COUNTRY_ADD_MONEY_2"]) * 9;

            $row["PREF_TOTAL_MONEY"] = $row["PREF_MONEY_1"] + $row["PREF_MONEY_2"];

            $array = array("COUNTRY_TOTAL_MONEY", "COUNTRY_TOTAL_MONEY", "TOTAL_BURDEN_CHARGE", "ADJUSTMENT_MONEY", "REDUCTION_JUGYOURYOU");
            foreach ($array as $akey) {
                if (!$row[$akey]) {
                    $row[$akey] = 0;
                }
            }

            if ($row["REDUCTION_JUGYOURYOU"] == 0 && $setGenmenSaki > 0) {
                $row["REDUCTION_JUGYOURYOU1"] = $setGenmenSaki / 4 * 1;
                $row["REDUCTION_JUGYOURYOU2"] = $setGenmenSaki / 4 * 3;
            }

            //減免情報取得
            $genmenInfo = $sep = "";
            $query2 = knjp721Query::selectQueryGenmen($this, $row["SCHREGNO"], $this->schoolKindAry[$this->search["GRADE"]]);
            $result2 = $db->query($query2);
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                list($fy, $fm, $fd) = explode("-", $row2["FROM_DATE"]);
                list($ty, $tm, $td) = explode("-", $row2["TO_DATE"]);
                $s_year_month = ($row2["FROM_DATE"]) ? $fy.'-'.$fm : "";
                $e_year_month = ($row2["TO_DATE"]) ? $ty.'-'.$tm : "";
                $genmenInfo .= "{$sep}減：";
                $genmenInfo .= "{$s_year_month}";
                $genmenInfo .= "～{$e_year_month}";
                $genmenInfo .= " {$row2["REDUCTION_DIV_NAME"]}({$row2["GENMEN_TYPE"]})";
                $sep = "　";
            }
            //異動情報取得
            $transferInfo = $sep = "";
            $query2 = knjp721Query::selectQueryTransfer($this, $row["SCHREGNO"]);
            $result2 = $db->query($query2);
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $transferInfo .= "{$sep}異：";
                $transferInfo .= $row2["TRANSFER_SDATE"];
                $transferInfo .= ($row2["TRANSFER_EDATE"] == "") ? "" : "～".$row2["TRANSFER_EDATE"];
                $transferInfo .= " ".$row2["NAME1"];
                $sep = "　";
            }
            //減免情報
            $row["GENMEN_TRANSFER_MARK"] = (strlen($genmenInfo) && strlen($transferInfo)) ? $genmenInfo."　".$transferInfo : $genmenInfo.$transferInfo;

            $row[$this->lastColumn] = $this->lastColumn;

            $sep = "";
            foreach ($header as $key => $val) {
                $contents .= $sep.$row[$key];
                $sep = ",";
            }
            $contents .= "\n";

            $dataFlg = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($dataFlg) {
            $couse = CTRL_YEAR."年度　就学支援金・補助金算定(".$this->search["GRADE"].")";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
