<?php

require_once('for_php7.php');


class knjp184kModel extends Model {
    var $field = array();
    var $cmd;

    function knjp184kModel() {
    }

    function init()
    {
        $this->field = array("EXPENSE_M_CD"     =>  VARS::post("EXPENSE_M_CD"),
                             "PAID_MONEY_DATE"  =>  VARS::post("PAID_MONEY_DATE"),
                             "OUTPUT"           =>  VARS::post("OUTPUT")
                     );

        $this->cmd = VARS::request("cmd");
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {

        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        $header_data = false;
        if ($this->field["OUTPUT"] == "1") {
            list($contents, $header_data) = $this->getKingakuBetuCsv($db);
        } else if ($this->field["OUTPUT"] == "2") {
            list($contents, $header_data) = $this->getSeitoBetuCsv($db);
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "1") {
                $setName = "金額別";
            } else {
                $setName = "生徒別";
            }
            $filename = sprintf("%s.csv", $setName);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    function getKingakuBetuCsv($db) {

        $contents = "";
        $header_data = false;

        $HrCourse = array();
        $query = knjp184kQuery::getHrCourse();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $HrCourse[$row["HR_COURSE"]] = $row["HR_COURSE"];
            $defHrCourse[$row["HR_COURSE"]] = "";
        }
        $result->free();

        $grade = array();
        $query = knjp184kQuery::getGrade();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grade[] = $row["GRADE"];
        }
        $result->free();

        //学年ごとの金額
        $moneyArray = array();
        foreach ($grade as $key => $val) {
            $moneyArray[$val] = array();
            $query = knjp184kQuery::getPaidMoney($this, $val);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $moneyArray[$val][] = $row["PAID_MONEY"];
                $header_data = true;
            }
        }
        $result->free();

        $contents = "年,MO引落額,データ,";
        $contents .= implode($HrCourse, ",").",総計\n";

        $outPutData = array();
        $outCnt = 0;
        $souGouKeiHrCourse = $defHrCourse;
        //学年
        foreach ($grade as $gKey => $gVal) {
            if (get_count($moneyArray[$gVal]) == 0) {
                continue;
            }
            $gradeTotal = array();
            $setGval = $gVal;
            $totalHrCourse[$gVal] = $defHrCourse;
            //金額
            foreach ($moneyArray[$gVal] as $mKey => $money) {
                $setTotalCnt = 0;
                $setTotalKuni = 0;
                $setTotalFuken = 0;
                $setTotalAdjust = 0;
                $setTotalBurden = 0;
                $outPutData[$outCnt] .= $setGval.",".$money.",合計/人数,";
                $outPutData[$outCnt + 1] .= ",,合計/国合計,";
                $outPutData[$outCnt + 2] .= ",,合計/府県合計,";
                $outPutData[$outCnt + 3] .= ",,合計/調整金,";
                $outPutData[$outCnt + 4] .= ",,合計/負担金,";
                //コース(クラスの頭一桁・・・JとかSとか)
                foreach ($HrCourse as $hKey => $hVal) {
                    $query = knjp184kQuery::getKingakuBetuCnt($this, $gVal, $hVal, $money);
                    $setCnt = $db->getOne($query);
                    $outPutData[$outCnt] .= $setCnt.",";
                    $setTotalCnt += $setCnt;
                    $totalHrCourse[$gVal][$hVal]["TOTAL"] += $setCnt;

                    $query = knjp184kQuery::getKingakuBetuKuniCnt($this, $gVal, $hVal, $money);
                    $setCnt = $db->getOne($query);
                    $outPutData[$outCnt + 1] .= $setCnt.",";
                    $setTotalKuni += $setCnt;
                    $totalHrCourse[$gVal][$hVal]["KUNI"] += $setCnt;

                    $query = knjp184kQuery::getKingakuBetuFukenCnt($this, $gVal, $hVal, $money);
                    $setCnt = $db->getOne($query);
                    $outPutData[$outCnt + 2] .= $setCnt.",";
                    $setTotalFuken += $setCnt;
                    $totalHrCourse[$gVal][$hVal]["FUKEN"] += $setCnt;

                    $query = knjp184kQuery::getKingakuBetuAdjustCnt($this, $gVal, $hVal, $money);
                    $setCnt = $db->getOne($query);
                    $outPutData[$outCnt + 3] .= $setCnt.",";
                    $setTotalAdjust += $setCnt;
                    $totalHrCourse[$gVal][$hVal]["ADJUST"] += $setCnt;

                    $query = knjp184kQuery::getKingakuBetuBurdenCnt($this, $gVal, $hVal, $money);
                    $setCnt = $db->getOne($query);
                    $outPutData[$outCnt + 4] .= $setCnt.",";
                    $setTotalBurden += $setCnt;
                    $totalHrCourse[$gVal][$hVal]["BURDEN"] += $setCnt;
                }
                $outPutData[$outCnt]     .= $setTotalCnt."\n";
                $outPutData[$outCnt + 1] .= $setTotalKuni."\n";
                $outPutData[$outCnt + 2] .= $setTotalFuken."\n";
                $outPutData[$outCnt + 3] .= $setTotalAdjust."\n";
                $outPutData[$outCnt + 4] .= $setTotalBurden."\n";
                $setGval = "";
                $outCnt = $outCnt + 5;
            }
            //学年合計
            $setGTotalCnt = 0;
            $setGTotalKuni = 0;
            $setGTotalFuken = 0;
            $setGTotalAdjust = 0;
            $setGTotalBurden = 0;
            $outPutData[$outCnt]     .= ",,".$gVal."合計/人数,";
            $outPutData[$outCnt + 1] .= ",,".$gVal."合計/国合計,";
            $outPutData[$outCnt + 2] .= ",,".$gVal."合計/府県合計,";
            $outPutData[$outCnt + 3] .= ",,".$gVal."合計/調整金,";
            $outPutData[$outCnt + 4] .= ",,".$gVal."合計/負担金,";
            foreach ($totalHrCourse[$gVal] as $tKey => $total) {
                $outPutData[$outCnt]     .= $total["TOTAL"].",";
                $setGTotalCnt += $total["TOTAL"];

                $outPutData[$outCnt + 1] .= $total["KUNI"].",";
                $setGTotalKuni += $total["KUNI"];

                $outPutData[$outCnt + 2] .= $total["FUKEN"].",";
                $setGTotalFuken += $total["FUKEN"];

                $outPutData[$outCnt + 3] .= $total["ADJUST"].",";
                $setGTotalAdjust += $total["ADJUST"];

                $outPutData[$outCnt + 4] .= $total["BURDEN"].",";
                $setGTotalBurden += $total["BURDEN"];

                $souGouKeiHrCourse[$tKey]["TOTAL"] += $total["TOTAL"];
                $souGouKeiHrCourse[$tKey]["KUNI"]  += $total["KUNI"];
                $souGouKeiHrCourse[$tKey]["FUKEN"] += $total["FUKEN"];
                $souGouKeiHrCourse[$tKey]["ADJUST"] += $total["ADJUST"];
                $souGouKeiHrCourse[$tKey]["BURDEN"] += $total["BURDEN"];
            }
            $outPutData[$outCnt]     .= $setGTotalCnt."\n";
            $outPutData[$outCnt + 1] .= $setGTotalKuni."\n";
            $outPutData[$outCnt + 2] .= $setGTotalFuken."\n";
            $outPutData[$outCnt + 3] .= $setGTotalAdjust."\n";
            $outPutData[$outCnt + 4] .= $setGTotalBurden."\n";
            $outCnt = $outCnt + 5;
        }
        //全体合計
        $setSTotalCnt = 0;
        $setSTotalKuni = 0;
        $setSTotalFuken = 0;
        $setSTotalAdjust = 0;
        $setSTotalBurden = 0;
        $outPutData[$outCnt]     .= ",,全体の合計/人数,";
        $outPutData[$outCnt + 1] .= ",,全体の合計/国合計,";
        $outPutData[$outCnt + 2] .= ",,全体の合計/府県合計,";
        $outPutData[$outCnt + 3] .= ",,全体の合計/調整金,";
        $outPutData[$outCnt + 4] .= ",,全体の合計/負担金,";
        foreach ($souGouKeiHrCourse as $sKey => $sTotal) {
            $outPutData[$outCnt]     .= $sTotal["TOTAL"].",";
            $setSTotalCnt += $sTotal["TOTAL"];

            $outPutData[$outCnt + 1] .= $sTotal["KUNI"].",";
            $setSTotalKuni += $sTotal["KUNI"];

            $outPutData[$outCnt + 2] .= $sTotal["FUKEN"].",";
            $setSTotalFuken += $sTotal["FUKEN"];

            $outPutData[$outCnt + 3] .= $sTotal["ADJUST"].",";
            $setSTotalAdjust += $sTotal["ADJUST"];

            $outPutData[$outCnt + 4] .= $sTotal["BURDEN"].",";
            $setSTotalBurden += $sTotal["BURDEN"];
        }
        $outPutData[$outCnt]     .= $setSTotalCnt."\n";
        $outPutData[$outCnt + 1] .= $setSTotalKuni."\n";
        $outPutData[$outCnt + 2] .= $setSTotalFuken."\n";
        $outPutData[$outCnt + 3] .= $setSTotalAdjust."\n";
        $outPutData[$outCnt + 4] .= $setSTotalBurden."\n";

        foreach ($outPutData as $key => $val) {
            $contents .= $val;
        }
        return array($contents, $header_data);
    }

    function getSeitoBetuCsv($db) {

        $contents = "";
        $header_data = false;
        $header = array("FUMEI_1"               => "データ修正フラグ",
                        "SCHREGNO"              => "学籍番号",
                        "GRADE"                 => "年",
                        "HR_CLASS1"             => "組1",
                        "HR_CLASS2"             => "組2",
                        "ATTENDNO"              => "番",
                        "PASSNO"                => "認定",
                        "NAME"                  => "生徒名",
                        "GUARD_NAME"            => "負担者名",
                        "REDUC_REMARK"          => "備考",
                        "NINZU"                 => "人数",
                        "PAID_MONEY_DATE"       => "MO引落日",
                        "PAID_MONEY"            => "MO引落額",
                        "OFFSET_FLG_COUNTRY"    => "相殺",
                        "KUNI_KEI"              => "国合計",
                        "KIHON_KEI"             => "基本計",
                        "KIHON_04"              => CTRL_YEAR."04",
                        "KIHON_05"              => CTRL_YEAR."05",
                        "KIHON_06"              => CTRL_YEAR."06",
                        "KIHON_07"              => CTRL_YEAR."07",
                        "KIHON_08"              => CTRL_YEAR."08",
                        "KIHON_09"              => CTRL_YEAR."09",
                        "KIHON_10"              => CTRL_YEAR."10",
                        "KIHON_11"              => CTRL_YEAR."11",
                        "KIHON_12"              => CTRL_YEAR."12",
                        "KIHON_01"              => (CTRL_YEAR + 1)."01",
                        "KIHON_02"              => (CTRL_YEAR + 1)."02",
                        "KIHON_03"              => (CTRL_YEAR + 1)."03",
                        "KASAN_KEI"             => "加算計",
                        "KASAN_04"              => CTRL_YEAR."04",
                        "KASAN_05"              => CTRL_YEAR."05",
                        "KASAN_06"              => CTRL_YEAR."06",
                        "KASAN_07"              => CTRL_YEAR."07",
                        "KASAN_08"              => CTRL_YEAR."08",
                        "KASAN_09"              => CTRL_YEAR."09",
                        "KASAN_10"              => CTRL_YEAR."10",
                        "KASAN_11"              => CTRL_YEAR."11",
                        "KASAN_12"              => CTRL_YEAR."12",
                        "KASAN_01"              => (CTRL_YEAR + 1)."01",
                        "KASAN_02"              => (CTRL_YEAR + 1)."02",
                        "KASAN_03"              => (CTRL_YEAR + 1)."03",
                        "FUKEN_KEI"             => "府県合計",
                        "PREF"                  => "府県",
                        "OFFSET_FLG"            => "相殺対象",
                        "LOCK_FLG"              => "相殺済",
                        "REDUCTIONMONEY_1"      => "4-6支援額",
                        "REDUC_DEC_FLG_1"       => "決定",
                        "REDUCTIONMONEY_2"      => "7-3支援額",
                        "REDUC_DEC_FLG_2"       => "決定",
                        "ADJUST"                => "調整金",
                        "BURDEN"                => "負担金");

        $contents .= implode($header, ",")."\n";

        $outPutArray = array();
        $query = knjp184kQuery::getSeitoBetuMoney($this);
        $result = $db->query($query);
        $cnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $outPutArray[$cnt]["SCHREGNO"] = $row["SCHREGNO"];
            $outPutArray[$cnt]["NAME"] = $row["NAME"];
            $outPutArray[$cnt]["GRADE"] = $row["GRADE"];
            $outPutArray[$cnt]["HR_CLASS1"] = $row["HR_CLASS1"];
            $outPutArray[$cnt]["HR_CLASS2"] = $row["HR_CLASS2"];
            $outPutArray[$cnt]["ATTENDNO"] = $row["ATTENDNO"];
            $outPutArray[$cnt]["PAID_MONEY"] = $row["PAID_MONEY"];
            $outPutArray[$cnt]["PAID_MONEY_DATE"] = $row["PAID_MONEY_DATE"];
            $outPutArray[$cnt]["PASSNO"] = $row["PASSNO"];
            $outPutArray[$cnt]["GUARD_NAME"] = $row["GUARD_NAME"];
            $outPutArray[$cnt]["OFFSET_FLG_COUNTRY"] = $row["OFFSET_FLG_COUNTRY"];
            $outPutArray[$cnt]["REDUC_REMARK"] = $row["REDUC_REMARK"];
            $outPutArray[$cnt]["TOTAL_ADJUSTMENT_MONEY"] = $row["TOTAL_ADJUSTMENT_MONEY"];
            $outPutArray[$cnt]["TOTAL_BURDEN_CHARGE"] = $row["TOTAL_BURDEN_CHARGE"];
            $outPutArray[$cnt]["NINZU"] = "1";
            $outPutArray[$cnt]["PREF"] = $row["PREF"];
            $outPutArray[$cnt]["OFFSET_FLG"] = $row["OFFSET_FLG"];
            $outPutArray[$cnt]["LOCK_FLG"] = $row["LOCK_FLG"];
            if ("13" == $this->field["EXPENSE_M_CD"]) {
                $outPutArray[$cnt]["REDUCTIONMONEY_1"] = $row["REDUCTIONMONEY_1"];
                $outPutArray[$cnt]["REDUC_DEC_FLG_1"] = $row["REDUC_DEC_FLG_1"];
                $outPutArray[$cnt]["REDUCTIONMONEY_2"] = $row["REDUCTIONMONEY_2"];
                $outPutArray[$cnt]["REDUC_DEC_FLG_2"] = $row["REDUC_DEC_FLG_2"];
            }
            $outPutArray[$cnt]["FUKEN_KEI"] = $row["REDUCTIONMONEY_1"] + $row["REDUCTIONMONEY_2"];

            $outPutArray[$cnt]["KIHON_04"] = $row["KIHON_04"];
            $outPutArray[$cnt]["KIHON_05"] = $row["KIHON_05"];
            $outPutArray[$cnt]["KIHON_06"] = $row["KIHON_06"];
            $outPutArray[$cnt]["KIHON_07"] = $row["KIHON_07"];
            $outPutArray[$cnt]["KIHON_08"] = $row["KIHON_08"];
            $outPutArray[$cnt]["KIHON_09"] = $row["KIHON_09"];
            $outPutArray[$cnt]["KIHON_10"] = $row["KIHON_10"];
            $outPutArray[$cnt]["KIHON_11"] = $row["KIHON_11"];
            $outPutArray[$cnt]["KIHON_12"] = $row["KIHON_12"];
            $outPutArray[$cnt]["KIHON_01"] = $row["KIHON_01"];
            $outPutArray[$cnt]["KIHON_02"] = $row["KIHON_02"];
            $outPutArray[$cnt]["KIHON_03"] = $row["KIHON_03"];
            $outPutArray[$cnt]["KIHON_KEI"] = $row["KIHON_04"] + $row["KIHON_05"] + $row["KIHON_06"] + $row["KIHON_07"] + $row["KIHON_08"] + $row["KIHON_09"] + $row["KIHON_10"] + $row["KIHON_11"] + $row["KIHON_12"] + $row["KIHON_01"] + $row["KIHON_02"] + $row["KIHON_03"];

            $outPutArray[$cnt]["KASAN_04"] = $row["KASAN_04"];
            $outPutArray[$cnt]["KASAN_05"] = $row["KASAN_05"];
            $outPutArray[$cnt]["KASAN_06"] = $row["KASAN_06"];
            $outPutArray[$cnt]["KASAN_07"] = $row["KASAN_07"];
            $outPutArray[$cnt]["KASAN_08"] = $row["KASAN_08"];
            $outPutArray[$cnt]["KASAN_09"] = $row["KASAN_09"];
            $outPutArray[$cnt]["KASAN_10"] = $row["KASAN_10"];
            $outPutArray[$cnt]["KASAN_11"] = $row["KASAN_11"];
            $outPutArray[$cnt]["KASAN_12"] = $row["KASAN_12"];
            $outPutArray[$cnt]["KASAN_01"] = $row["KASAN_01"];
            $outPutArray[$cnt]["KASAN_02"] = $row["KASAN_02"];
            $outPutArray[$cnt]["KASAN_03"] = $row["KASAN_03"];
            $outPutArray[$cnt]["KASAN_KEI"] = $row["KASAN_04"] + $row["KASAN_05"] + $row["KASAN_06"] + $row["KASAN_07"] + $row["KASAN_08"] + $row["KASAN_09"] + $row["KASAN_10"] + $row["KASAN_11"] + $row["KASAN_12"] + $row["KASAN_01"] + $row["KASAN_02"] + $row["KASAN_03"];

            $outPutArray[$cnt]["KUNI_KEI"] = $outPutArray[$cnt]["KIHON_KEI"] + $outPutArray[$cnt]["KASAN_KEI"];

            $cnt++;
        }
        $result->free();

        foreach ($outPutArray as $key => $val) {
            $contents .= $val["FUMEI_1"].",";
            $contents .= $val["SCHREGNO"].",";
            $contents .= $val["GRADE"].",";
            $contents .= $val["HR_CLASS1"].",";
            $contents .= $val["HR_CLASS2"].",";
            $contents .= $val["ATTENDNO"].",";
            $contents .= $val["PASSNO"].",";
            $contents .= $val["NAME"].",";
            $contents .= $val["GUARD_NAME"].",";
            $contents .= $val["REDUC_REMARK"].",";
            $contents .= $val["NINZU"].",";
            $contents .= $val["PAID_MONEY_DATE"].",";
            $contents .= $val["PAID_MONEY"].",";
            $contents .= $val["OFFSET_FLG_COUNTRY"].",";
            $contents .= $val["KUNI_KEI"].",";
            $contents .= $val["KIHON_KEI"].",";
            $contents .= $val["KIHON_04"].",";
            $contents .= $val["KIHON_05"].",";
            $contents .= $val["KIHON_06"].",";
            $contents .= $val["KIHON_07"].",";
            $contents .= $val["KIHON_08"].",";
            $contents .= $val["KIHON_09"].",";
            $contents .= $val["KIHON_10"].",";
            $contents .= $val["KIHON_11"].",";
            $contents .= $val["KIHON_12"].",";
            $contents .= $val["KIHON_01"].",";
            $contents .= $val["KIHON_02"].",";
            $contents .= $val["KIHON_03"].",";
            $contents .= $val["KASAN_KEI"].",";
            $contents .= $val["KASAN_04"].",";
            $contents .= $val["KASAN_05"].",";
            $contents .= $val["KASAN_06"].",";
            $contents .= $val["KASAN_07"].",";
            $contents .= $val["KASAN_08"].",";
            $contents .= $val["KASAN_09"].",";
            $contents .= $val["KASAN_10"].",";
            $contents .= $val["KASAN_11"].",";
            $contents .= $val["KASAN_12"].",";
            $contents .= $val["KASAN_01"].",";
            $contents .= $val["KASAN_02"].",";
            $contents .= $val["KASAN_03"].",";
            $contents .= $val["FUKEN_KEI"].",";
            $contents .= $val["PREF"].",";
            $contents .= $val["OFFSET_FLG"].",";
            $contents .= $val["LOCK_FLG"].",";
            $contents .= $val["REDUCTIONMONEY_1"].",";
            $contents .= $val["REDUC_DEC_FLG_1"].",";
            $contents .= $val["REDUCTIONMONEY_2"].",";
            $contents .= $val["REDUC_DEC_FLG_2"].",";
            $contents .= $val["TOTAL_ADJUSTMENT_MONEY"].",";
            $contents .= $val["TOTAL_BURDEN_CHARGE"].",";
            $contents .= "\n";
        }

        return array($contents, true);
    }
}
?>
