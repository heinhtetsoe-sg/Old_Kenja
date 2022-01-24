<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp332Model extends Model {
    var $field      = array();
    var $output;
    var $todoufuken;
    var $hrclass;
    var $selectdata = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp332Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "DATE1"            =>  VARS::post("DATE1"),            //日付FROM
                             "DATE2"            =>  VARS::post("DATE2"),            //日付TO
                             "YOUSIKI"          =>  VARS::post("YOUSIKI"),          //出力様式
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->output     = VARS::post("OUTPUT");
        $this->todoufuken = VARS::post("TODOUFUKEN");
        $this->hrclass    = VARS::post("HRCLASS");
        $this->cmd        = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
        $this->grdDate    = VARS::post("GRD_DATE");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * CSV出力
     */
    function getDownloadModel()
    {
        $data = false;

        $db = Query::dbCheckOut();

        $query = knjp332Query::getG212Sql();
        $result = $db->query($query);
        $this->g212 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->g212[]["GRANTCD"] = $row["NAMECD2"];
        }

        $inState = "('".str_replace(",", "','", VARS::post("selectdata"))."')";
        //SQL文発行
        if ($this->output == "1") {
            $query = knjp332Query::todoufukenMeisai($this, $inState);
        } else if ($this->output == "2") {
            $query = knjp332Query::classMeisai($this, $inState);
        } else {
            $query = knjp332Query::kojinMeisai($this, $inState);
        }
        $result = $db->query($query);

        list($date1Y, $date1M, $date1D) = preg_split("/\//", $this->field["DATE1"]);
        $nendo1Y = $date1M < "04" ? $date1Y - 1 : $date1Y;
        $date1WarekiNendo = common::Calc_Wareki($nendo1Y, $date1M, $date1D);
        $date1WarekiNen = common::Calc_Wareki($date1Y, $date1M, $date1D);
        $date1WarekiDate = $date1WarekiNen.$date1M."月".$date1D."日";

        list($date2Y, $date2M, $date2D) = preg_split("/\//", $this->field["DATE2"]);
        $date2WarekiNen = common::Calc_Wareki($date2Y, $date2M, $date2D);
        $date2WarekiDate = $date2WarekiNen.$date2M."月".$date2D."日";
        $date2WarekiYoubi = common::DateConv1($this->field["DATE2"], 3);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $keigenMoney = $row["REDUCTIONMONEY"] + $row["B_PAID_MONEY"] + $row["A_PAID_MONEY"];
            $grantTotal = 0;
            $grantMap = array();
            $grantCnt2 = 0;
            foreach ($this->g212 as $key => $val) {
                if ($row["GRANTNAME{$val["GRANTCD"]}"] == "") {
                    continue;
                }
                $grantTotal += $row["GRANT_MONEY{$val["GRANTCD"]}"];
                $grantVal = array();
                $grantVal["GRANT_NAME"] = $row["GRANTNAME{$val["GRANTCD"]}"];
                $grantVal["GRANT_MONEY"] = $row["GRANT_MONEY{$val["GRANTCD"]}"];
                $grantMap[$val["GRANTCD"]] = $grantVal;
                $grantCnt2++;
            }
            $grantCnt2 = $grantCnt2 > 2 ? 2 : $grantCnt2;
            $paternCd = "";
            if ($keigenMoney > 0) {
                if ($grantTotal > 0) {
                    $paternCd = "B";
                } else if ($row["MONEY_DUE"] < 600000) {
                    $paternCd = "F";
                } else {
                    $paternCd = "A";
                }
            } else {
                if ($grantTotal > 0) {
                    if ($grantTotal >= $row["MONEY_DUE"]) {
                        if ($grantCnt2 == 1) {
                            $paternCd = "E";
                        } else {
                            $paternCd = "E";
                        }
                    } else {
                        if ($grantCnt2 == 1) {
                            $paternCd = "D";
                        } else {
                            $paternCd = "D";
                        }
                    }
                } else {
                    $paternCd = "C";
                }
            }

            $contents .= ",授業料軽減決定通知書";
            $contents .= "\n\n";
            $contents .= ",,,,,".$date1WarekiDate;
            $contents .= "\n\n";
            $contents .= "申請者（学資負担者）";
            $contents .= "\n\n";
            $contents .= $row["GUARANTOR_NAME"]."殿";
            $contents .= "\n\n";
            $contents .= "（".$row["HR_NAME"]." ".$row["ATTENDNO"]."番  ".$row["NAME"]." ）\n\n";
            $contents .= "\n\n\n";
            $contents .= ",,,,,近 畿 大 学 附 属 高 等 学 校";
            $contents .= "\n\n\n";
            $contents .= ",,,,,事　務　部";
            $contents .= "\n\n\n\n";
            if ($paternCd == "A" || $paternCd == "B" || $paternCd == "F") {
                $contents .= ",,　このたび、国より{$date1WarekiNendo}高等学校等就学支援金、府県より授業";
                $contents .= "\n\n";
                $contents .= ",,料支援（軽減）補助金の交付が下記のとおり決定されました。就学支援";
                $contents .= "\n\n";
                $contents .= ",,金及び授業料支援（軽減）補助金は、授業料と相殺して交付いたします。";
                $contents .= "\n\n";
                $contents .= ",,　つきましては、{$date1WarekiNendo}の授業料引落し額を下記のとおり通知い";
                $contents .= "\n\n";
                $contents .= ",,たします。";
            } else {
                $contents .= ",,　{$date1WarekiNendo}の授業料引落し額を下記のとおり通知いたします。";
            }
            $contents .= "\n\n\n\n";
            $contents .= ",,,　　　　　　　　　　　　　記";
            $contents .= "\n\n\n\n";

            $totalReducMoney = $row["REDUCTIONMONEY"] + $row["B_PAID_MONEY"] + $row["A_PAID_MONEY"] + $row["ADJUSTMENT_MONEY"];
            $totalReducCountryMoney = $row["B_PAID_MONEY"] + $row["A_PAID_MONEY"];
            if ($paternCd == "A") {
                $contents .= ",,1 . 授業料軽減決定額,\"". number_format($totalReducMoney) ."円\"";
                $contents .= "\n\n\n";

                $totalMoney = $row["MONEY_DUE"] - ($totalReducCountryMoney + $row["REDUCTIONMONEY"] + $row["ADJUSTMENT_MONEY"]);
                $patern = "1";
                if ($totalMoney > 0) {
                    $totalMoney = $totalMoney + $row["FEE"];
                    $patern = "2";
                }
                $contents .= ",,2 . 授業料引落し額,\"". number_format($totalMoney) ."円\"";
                $contents .= "\n\n\n";

                $contents .= ",,3 . 内　　訳,授業料,①,\"". number_format($row["MONEY_DUE"]) ."円\"";
                $contents .= "\n\n\n";
                $contents .= ",,,就学支援金(国),②,\"". number_format($totalReducCountryMoney) ."円\"";
                $contents .= "\n\n\n";
                $contents .= ",,,授業料支援(軽減)補助金(府県),③,\"". number_format($row["REDUCTIONMONEY"]) ."円\"";
                if ($row["ADJUSTMENT_MONEY"] > 0) {
                    $contents .= "\n\n\n";
                    $contents .= ",,,大阪府授業料支援補助金(学校負担分),④,\"". number_format($row["ADJUSTMENT_MONEY"]) ."円\"";
                }
                if ($patern == "2") {
                    $contents .= "\n\n\n";
                    $contents .= ",,,引落し手数料,⑤,\"". number_format($row["FEE"]) ."円\"";
                }
                $contents .= "\n\n\n";
                $contents .= "\n\n\n";
                $contents .= ",,,引落し額（授業料－軽減額）";
                $contents .= "\n\n\n";
                if ($patern == "1") {
                    if ($row["ADJUSTMENT_MONEY"] > 0) {
                        $contents .= ",,,,①-(②+③+④),\"". number_format($totalMoney) ."円\"";
                    } else {
                        $contents .= ",,,,①-(②+③),\"". number_format($totalMoney) ."円\"";
                    }
                } else {
                    $contents .= ",,,,①-(②+③+④)+⑤,\"". number_format($totalMoney) ."円\"";
                }

                if ($patern == "2") {
                    $contents .= "\n\n\n";
                    $contents .= ",,4 . 引落し日（振替日）,{$date2WarekiDate}";
                    $contents .= "\n\n\n";
                    $contents .= ",,※　振替日の前日までに授業料振替登録口座に引落し額を納入してください。";
                }

                $contents .= "\n\n\n";
                $contents .= "\n\n\n";
                $contents .= ",,,,,以上";
            }
            $moneyLine = 1;
            $gnameLine = "";
            if ($paternCd == "B") {
                $contents .= ",,1 . 授業料軽減決定額,\"". number_format($totalReducMoney) ."円\"";
                $contents .= "\n\n\n";

                $totalMoney = $row["MONEY_DUE"] - ($grantTotal + $totalReducCountryMoney + $row["REDUCTIONMONEY"] + $row["ADJUSTMENT_MONEY"]);
                $patern = "1";
                if ($totalMoney > 0) {
                    $totalMoney = $totalMoney + $row["FEE"];
                    $patern = "2";
                }

                $contents .= ",,2 . 授業料引落し額,\"". number_format($totalMoney) ."円\"";
                $contents .= "\n\n\n";

                $contents .= ",,3 . 内　　訳,授業料,①,\"". number_format($row["MONEY_DUE"]) ."円\"";
                $contents .= "\n\n\n";

                $grantCnt = 1;
                foreach ($grantMap as $grantCd => $gVal) {
                    if ($gVal["GRANT_NAME"]) {
                        $contents .= ",,,{$gVal["GRANT_NAME"]}減免額,②,\"". number_format($gVal["GRANT_MONEY"]) ."円\"";
                        $contents .= "\n\n\n";

                        $gnameLine = "2";
                    }
                    $grantCnt++;
                    if ($grantCnt > $grantCnt2) {
                        break;
                    }
                }
                $contents .= ",,,就学支援金(国),③,\"". number_format($totalReducCountryMoney) ."円\"";
                $contents .= "\n\n\n";
                $contents .= ",,,授業料支援(軽減)補助金(府県),④,\"". number_format($row["REDUCTIONMONEY"]) ."円\"";
                if ($row["ADJUSTMENT_MONEY"] > 0) {
                    $contents .= "\n\n\n";
                    $contents .= ",,,学校負担金,⑤,\"". number_format($row["ADJUSTMENT_MONEY"]) ."円\"";
                }

                if ($patern == "1") {
                    $contents .= "\n\n\n";
                    $contents .= "\n\n\n";
                    $contents .= ",,,引落し額（授業料－軽減額）";
                    $contents .= "\n\n\n";
                    if ($row["ADJUSTMENT_MONEY"] > 0) {
                        $contents .= ",,,,①-(②+③+④+⑤),\"". number_format($totalMoney) ."円\"";
                    } else {
                        $contents .= ",,,,①-(②+③+④),\"". number_format($totalMoney) ."円\"";
                    }
                    $contents .= "\n\n\n";
                } else {
                    $contents .= "\n\n\n";
                    if ($row["ADJUSTMENT_MONEY"] > 0) {
                        $contents .= ",,,引落し手数料,⑥,\"". number_format($row["FEE"]) ."円\"";
                    } else {
                        $contents .= ",,,引落し手数料,⑤,\"". number_format($row["FEE"]) ."円\"";
                    }
                    $contents .= "\n\n\n";
                    $contents .= "\n\n\n";
                    $contents .= ",,,引落し額（授業料－軽減額＋引落し手数料）";
                    $contents .= "\n\n\n";
                    if ($row["ADJUSTMENT_MONEY"] > 0) {
                        $contents .= ",,,,①-(②+③+④+⑤)+⑥,\"". number_format($totalMoney) ."円\"";
                    } else {
                        $contents .= ",,,,①-(②+③+④)+⑤,\"". number_format($totalMoney) ."円\"";
                    }
                    $contents .= "\n\n\n";
                    $contents .= "\n\n\n";
                    $contents .= "\n\n\n";

                    $contents .= ",,4 . 引落し日（振替日）,{$date2WarekiDate}";
                    $contents .= "\n\n\n";

                    $contents .= ",,※　振替日の前日までに授業料振替登録口座に引落し額を納入してください。";
                    $contents .= "\n\n\n";
                }

                $contents .= ",,,,,以上";
            }
            if ($paternCd == "C") {

                $totalMoney = $row["MONEY_DUE"] + $row["FEE"];
                $contents .= ",,1 . 授業料引落し額,\"". number_format($totalMoney) ."円\"";
                $contents .= "\n\n\n";

                $contents .= ",,2 . 内　　訳,授業料,①,\"". number_format($row["MONEY_DUE"]) ."円\"";
                $contents .= "\n\n\n";
                $contents .= ",,,引落し手数料,②,\"". number_format($row["FEE"]) ."円\"";
                $contents .= "\n\n\n";

                $contents .= ",,,引落し額（授業料－引落し手数料）";
                $contents .= "\n\n\n";

                $contents .= ",,,,①+②,\"". number_format($totalMoney) ."円\"";
                $contents .= "\n\n\n";
                $contents .= "\n\n\n";
                $contents .= "\n\n\n";

                $contents .= ",,3 . 引落し日（振替日）,{$date2WarekiDate}";
                $contents .= "\n\n\n";

                $contents .= ",,※　振替日の前日までに授業料振替登録口座に引落し額を納入してください。";
                $contents .= "\n\n\n";

                $contents .= ",,,,,以上";
            }
            if ($paternCd == "D") {
                $totalMoney = $row["MONEY_DUE"] - $grantTotal + $row["FEE"];
                $contents .= ",,1 . 授業料引落し額,\"". number_format($totalMoney) ."円\"";
                $contents .= "\n\n\n";

                $contents .= ",,2 . 内　　訳,授業料,①,\"". number_format($row["MONEY_DUE"]) ."円\"";
                $contents .= "\n\n\n";

                $grantCnt = 1;
                $noArray = array(1 => ②, 2 => ③, 3 => ④);
                foreach ($grantMap as $grantCd => $gVal) {
                    if ($gVal["GRANT_NAME"]) {
                        $contents .= ",,,{$gVal["GRANT_NAME"]}減免額,{$noArray[$grantCnt]},\"". number_format($gVal["GRANT_MONEY"]) ."円\"";
                        $contents .= "\n\n\n";

                        $gnameLine = "2";
                    }
                    $grantCnt++;
                    if ($grantCnt > $grantCnt2) {
                        break;
                    }
                }
                $contents .= ",,,引落し手数料,{$noArray[$grantCnt]},\"". number_format($row["FEE"]) ."円\"";
                $contents .= "\n\n\n";

                $contents .= ",,,引落し額（授業料－減免額＋引落し手数料）";
                $contents .= "\n\n\n";

                if ($grantCnt2 == 1) {
                    $contents .= ",,,,①-②+③,\"". number_format($totalMoney) ."円\"";
                } else {
                    $contents .= ",,,,①-②-③+④,\"". number_format($totalMoney) ."円\"";
                }
                $contents .= "\n\n\n";
                $contents .= "\n\n\n";
                $contents .= "\n\n\n";

                $contents .= ",,3 . 引落し日（振替日）,{$date2WarekiDate}";
                $contents .= "\n\n\n";

                $contents .= ",,※　振替日の前日までに授業料振替登録口座に引落し額を納入してください。";
                $contents .= "\n\n\n";

                $contents .= ",,,,,以上";
            }
            if ($paternCd == "E") {
                $totalMoney = $row["MONEY_DUE"] - $grantTotal;
                $contents .= ",,1 . 授業料引落し額,\"". number_format($totalMoney) ."円\"";
                $contents .= "\n\n\n";

                $contents .= ",,2 . 内　　訳,授業料,①,\"". number_format($row["MONEY_DUE"]) ."円\"";
                $contents .= "\n\n\n";

                $grantCnt = 1;
                $noArray = array(1 => ②, 2 => ③);
                foreach ($grantMap as $grantCd => $gVal) {
                    if ($gVal["GRANT_NAME"]) {
                        $contents .= ",,,{$gVal["GRANT_NAME"]}減免額,{$noArray[$grantCnt]},\"". number_format($gVal["GRANT_MONEY"]) ."円\"";
                        $contents .= "\n\n\n";

                        $gnameLine = "2";
                    }
                    $grantCnt++;
                    if ($grantCnt > $grantCnt2) {
                        break;
                    }
                }

                $contents .= ",,,引落し額（授業料－減免額）";
                $contents .= "\n\n\n";

                if ($grantCnt2 == 1) {
                    $contents .= ",,,,①-②,\"". number_format($totalMoney) ."円\"";
                } else {
                    $contents .= ",,,,①-②-③,\"". number_format($totalMoney) ."円\"";
                }

                $contents .= "\n\n\n";
                $contents .= "\n\n\n";
                $contents .= "\n\n\n";

                $contents .= ",,,,,以上";
            }
            if ($paternCd == "F") {
                $contents .= ",,1 . 授業料軽減決定額,\"". number_format($totalReducMoney) ."円\"";
                $contents .= "\n\n\n";

                $totalMoney = $row["MONEY_DUE"] - ($totalReducCountryMoney + $row["REDUCTIONMONEY"]);
                $patern = "1";
                if ($totalMoney > 0) {
                    $totalMoney = $totalMoney + $row["FEE"];
                    $patern = "2";
                }
                $contents .= ",,2 . 授業料引落し額,\"". number_format($totalMoney) ."円\"";
                $contents .= "\n\n\n";

                $contents .= ",,3 . 内　　訳,授業料,①,\"". number_format($row["MONEY_DUE"]) ."円\"";
                $contents .= "\n\n\n";
                $contents .= ",,,就学支援金(国),②,\"". number_format($totalReducCountryMoney) ."円\"";
                $contents .= "\n\n\n";
                $contents .= ",,,授業料支援(軽減)補助金(府県),③,\"". number_format($row["REDUCTIONMONEY"]) ."円\"";
                if ($patern == "2") {
                    $contents .= "\n\n\n";
                    $contents .= ",,,引落し手数料,④,\"". number_format($row["FEE"]) ."円\"";
                }
                $contents .= "\n\n\n";
                $contents .= "\n\n\n";
                $contents .= ",,,引落し額（授業料－軽減額）";
                $contents .= "\n\n\n";
                if ($patern == "1") {
                    $contents .= ",,,,①-(②+③),\"". number_format($totalMoney) ."円\"";
                } else {
                    $contents .= ",,,,①-(②+③)+④,\"". number_format($totalMoney) ."円\"";
                }

                if ($patern == "2") {
                    $contents .= "\n\n\n";
                    $contents .= ",,4 . 引落し日（振替日）,{$date2WarekiDate}";
                    $contents .= "\n\n\n";
                    $contents .= ",,※　振替日の前日までに授業料振替登録口座に引落し額を納入してください。";
                }

                $contents .= "\n\n\n";
                $contents .= "\n\n\n";
                $contents .= ",,,,,以上";
            }

            $contents .= "\n\n\n\n\n\n\n\n\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($data) {
            $filename = CTRL_YEAR."年度_授業料軽減決定通知書.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
