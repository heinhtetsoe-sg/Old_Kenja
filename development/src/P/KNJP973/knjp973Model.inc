<?php

require_once('for_php7.php');

class knjp973Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjp973Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("GRADE"            =>  VARS::post("GRADE"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                            );

        $this->schoolKind = VARS::post("SCHOOL_KIND");

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->getPropertiesAll();  //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $lf = "\n";
        $comma = ",";
        $contents = ""; //CSVデータ出力用
        $data = false;

        //タイトル
        $title = CTRL_YEAR."年度"."　収支総括";

        //日付
        $wareki = common::DateConv1(date("Y/m/d"), 0);
        $setDate = $wareki.date(" H時i分");

        $contents .= $title.$comma.$comma.$setDate.$lf;

        //科目名称取得
        $this->levyLmstArr = array();
        $query = knjp973Query::getLmst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->levyLmstArr[$row["INCOME_L_CD"]] = $row["LEVY_L_NAME"];
        }

        //繰越金取得
        $carryArr = array();
        $query = knjp973Query::getCarryOver($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $carryArr[$row["SCHREGNO"]] = $row["CARRYOVER"];
        }

        //端数返金取得
        $hasuuHenkinArr = array();
        $query = knjp973Query::getHasuuHenkin($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hasuuHenkinArr[$row["SCHREGNO"]] = $row["HASUU_HENKIN"];
        }

        //返金取得
        $henkinArr = array();
        $query = knjp973Query::getHenkin($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $henkinArr[$row["SCHREGNO"]] += $row["HENKIN"];
        }

        //返金取得
        $allFee     = 0;
        $schKindFee = "";
        $tesuryo    = 0;
        $query = knjp973Query::getTesuryo($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SCHOOL_KIND"] == "99") {
                $allFee     = $row["BANK_TRANSFER_FEE"];
            } else {
                $schKindFee = $row["BANK_TRANSFER_FEE"];
            }
        }
        $tesuryo = ($schKindFee != "") ? $schKindFee: $allFee;

        //端数取得
        $this->hasuuArr    = array();
        $this->hasuuFlgArr = array();
        $query = knjp973Query::getHasuu($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->hasuuArr[$row["INCOME_L_CD"]] = $row["HASUU"];
        }

        $rowTotalArr = array();//横計

        //生徒基本情報＋【科目コード、納入額、支出額、給付金（その他返金額）】取得
        $schData = array();
        $query = knjp973Query::getHrClassCsv($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            //精算額
            $diffMoney = $row["INCOME_MONEY"] - $row["OUTGO_MONEY"];

            $schData[$row["SCHREGNO"]][$row["INCOME_L_CD"]] = array("HR_NAME"       => $row["HR_NAME"],     //クラス名称
                                                                    "ATTENDNO"      => $row["ATTENDNO"],    //出席番号
                                                                    "SCH_NAME"      => $row["SCH_NAME"],    //生徒氏名
                                                                    "INCOME_MONEY"  => $row["INCOME_MONEY"],//納入額
                                                                    "OUTGO_MONEY"   => $row["OUTGO_MONEY"], //支出額
                                                                    "DIFF_MONEY"    => $diffMoney           //精算額
                                                                    );

            //納入額総計
            $rowTotalArr[$row["SCHREGNO"]]["INCOME_TOTAL"] += $row["INCOME_MONEY"];
            //支出額総計
            $rowTotalArr[$row["SCHREGNO"]]["OUTGO_TOTAL"]  += $row["OUTGO_MONEY"];
            //精算総額
            $rowTotalArr[$row["SCHREGNO"]]["DIFF_TOTAL"]   += $diffMoney;
            //その他返金額(給付金)
            $rowTotalArr[$row["SCHREGNO"]]["KYUFU_MONEY"]   = $row["KYUFU_MONEY"];

            $data = true;
        }

        $attNoFlg   = "";
        $schNameFlg = "";

        $this->rowTitle = array();
        $this->rowTitle["SCH_COUNT"]      = "人数";
        $this->rowTitle["MONEY_TOTAL"]    = "合計";
        $this->rowTitle["HASUU"]          = "端数";

        $this->subName1 = array();
        $this->subName1["INCOME_MONEY"] = "納入額";
        $this->subName1["OUTGO_MONEY"]  = "支出額";
        $this->subName1["DIFF_MONEY"]   = "精算額";

        $this->subName2 = array();
        $this->subName2["INCOME_TOTAL"] = "納入額総計";
        $this->subName2["OUTGO_TOTAL"]  = "支出額総計";
        $this->subName2["CARRY_TOTAL"]  = "繰越総額";
        $this->subName2["HASUU_TOTAL"]  = "端数返金額";
        if ($this->Properties["useBenefit"] == "1") {
            $this->subName2["KYUFU_MONEY"]  = "その他返金額";
        }
        $this->subName2["DIFF_TOTAL"]   = "精算総額";
        $this->subName2["DATA_MONEY"]   = "データ入力金額";

        $this->cntArr       = array();//人数カウント
        $this->colTotalArr  = array();//縦合計

        //データセット(生徒毎)
        foreach ($schData as $schregNo => $arrLcd) {
            foreach ($this->levyLmstArr as $incomeLcd => $nameL) {
                $arrVal = $arrLcd[$incomeLcd];
                //クラス名称、科目名
                if ($hrNameFlg != $arrVal["HR_NAME"]) {
                    //クラス集計
                    if ($hrNameFlg != "") {
                        //人数、合計、端数セット
                        $contents .= $this->setRowTotal();

                        //クラス変更時は初期化
                        $this->cntArr       = array();
                        $this->colTotalArr  = array();

                        $contents .= $lf;//改行
                    }

                    $contents .= $arrVal["HR_NAME"].$comma."".$comma;//クラス名称

                    //科目名
                    foreach ($this->levyLmstArr as $cd => $name) {
                        $contents .= $name.$comma."".$comma."".$comma;
                    }
                    $contents .= $lf;//改行

                    $contents .= "番号".$comma."氏名";
                    //項目名
                    foreach ($this->levyLmstArr as $cd => $name) {
                        foreach ($this->subName1 as $fName => $subName) {
                            $contents .= $comma.$subName;
                        }
                    }
                    foreach ($this->subName2 as $fName => $subName) {
                        $contents .= $comma.$subName;
                    }
                    $contents .= $lf;//改行
                }

                if ($attNoFlg != $arrVal["ATTENDNO"]) {
                    $contents .= $arrVal["ATTENDNO"].$comma;//出席番号
                    $contents .= $arrVal["SCH_NAME"].$comma;//名前
                }
                //納入額、支出額、精算額
                foreach ($this->subName1 as $fName => $subName) {
                    $contents .= $arrVal[$fName].$comma;

                    //人数カウント（０以外をカウント）
                    if ($arrVal[$fName] != 0) {
                        $this->cntArr[$incomeLcd][$fName]++;
                    }
                    //縦合計
                    $this->colTotalArr[$incomeLcd][$fName] += $arrVal[$fName];
                }

                $attNoFlg   = $arrVal["ATTENDNO"];
                $hrNameFlg  = $arrVal["HR_NAME"];
            }

            //横計セット（納入額総計etc
            foreach ($this->subName2 as $fName => $subName) {
                //繰越総額
                if ($fName == "CARRY_TOTAL") {
                    $carry = 0;
                    if ($carryArr[$schregNo] != '') {
                        $carry = $carryArr[$schregNo];
                        $rowTotalArr[$schregNo]["DIFF_TOTAL"] -= $carry;//繰越を精算総額から引く
                    }
                    $rowTotalArr[$schregNo][$fName] = $carry;
                    $contents .= $carry.$comma;

                //端数返金額
                } else if ($fName == "HASUU_TOTAL") {
                    $hasuu = 0;
                    if ($hasuuHenkinArr[$schregNo] != '') {
                        $hasuu = $hasuuHenkinArr[$schregNo];
                        $rowTotalArr[$schregNo]["DIFF_TOTAL"] += $hasuu;//端数返金を精算総額に足しこむ
                    }
                    $rowTotalArr[$schregNo][$fName] = $hasuu;
                    $contents .= $hasuu.$comma;

                //その他返金額(給付金)
                } else if ($fName == "KYUFU_MONEY") {
                    $kyuhu = 0;
                    if ($rowTotalArr[$schregNo][$fName] != '') {
                        $kyuhu = $rowTotalArr[$schregNo][$fName];
                        $rowTotalArr[$schregNo]["DIFF_TOTAL"] += $kyuhu;//端数返金を精算総額に足しこむ
                    }
                    $contents .= $kyuhu.$comma;

                //データ入力金額
                } else if ($fName == "DATA_MONEY") {
                    $henkin = ($henkinArr[$schregNo] == '') ? 0: $henkinArr[$schregNo];
                    $finMoney = $rowTotalArr[$schregNo]["DIFF_TOTAL"] - $henkin;
                    $setPrice = ($finMoney <= 0) ? $finMoney: $finMoney - $tesuryo;
                    if ($finMoney == 0) {
                        $contents .= '返金済'.$comma;
                    } else {
                        $contents .= $setPrice.$comma;
                    }
                    $rowTotalArr[$schregNo][$fName] = $setPrice;
                } else {
                    $contents .= $rowTotalArr[$schregNo][$fName].$comma;
                }

                //人数カウント（０以外をカウント）
                if ($rowTotalArr[$schregNo][$fName] != 0) {
                    $this->cntArr[$fName]++;
                }
                //縦合計
                $this->colTotalArr[$fName] += $rowTotalArr[$schregNo][$fName];
            }

            $contents .= $lf;//改行
        }
        if ($hrNameFlg != "") {
            //人数、合計、端数セット
            $contents .= $this->setRowTotal();
        }

        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            //タイトル
            $couse = CTRL_YEAR."年度"."　収支総括";
            $filename = sprintf("%s.csv", $couse);

            common::downloadContents($contents, $filename, "SJIS-win");

            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }//getDownloadModelの綴り

    //人数、合計、端数をセット
    function setRowTotal() {
        $contents  = "";
        $comma     = ",";
        $lf        = "\n";
        $toalHasuu = 0;

        foreach ($this->rowTitle as $key => $rowTitleName) {
            $contents .= $comma.$rowTitleName;//タイトル

            //項目毎
            foreach ($this->levyLmstArr as $cd => $name) {
                //納入、支出、精算の縦計をそれぞれセット
                foreach ($this->subName1 as $fName => $subName) {
                    //人数カウント
                    if ($key == "SCH_COUNT") {
                        if ($this->cntArr[$cd][$fName] == '') $this->cntArr[$cd][$fName] = 0;//空は0をセット
                        $contents .= $comma.$this->cntArr[$cd][$fName];

                    //合計
                    } else if ($key == "MONEY_TOTAL") {
                        $contents .= $comma.$this->colTotalArr[$cd][$fName];

                    //端数
                    } else if ($key == "HASUU") {
                        if ($fName == "OUTGO_MONEY") {
                            $hasuu = ($this->hasuuArr[$cd] != '') ? $this->hasuuArr[$cd]: 0;
                            //一度印字した端数は印字しない
                            if ($this->hasuuFlgArr[$cd] != '1') {
                                $toalHasuu += $hasuu;
                                $contents .= $comma.$hasuu;
                                if ($hasuu > 0) {
                                    $this->hasuuFlgArr[$cd] = '1';
                                }
                            } else {
                                $contents .= $comma."0";
                            }
                        } else {
                            $contents .= $comma;
                        }
                    }
                }
            }
            //横計の縦計
            foreach ($this->subName2 as $fName => $subName) {
                //人数
                if ($key == "SCH_COUNT") {
                    if ($this->cntArr[$fName] == '') $this->cntArr[$fName] = 0;//空は0をセット
                    $contents .= $comma.$this->cntArr[$fName];

                //合計
                } else if ($key == "MONEY_TOTAL") {
                    $contents .= $comma.$this->colTotalArr[$fName];

                //端数
                } else if ($key == "HASUU") {
                    if ($fName == "OUTGO_TOTAL") {
                        $contents .= $comma.$toalHasuu;
                    } else {
                        $contents .= $comma;
                    }
                }
            }

            $contents .= $lf;//改行
        }
        return $contents;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

}
?>
