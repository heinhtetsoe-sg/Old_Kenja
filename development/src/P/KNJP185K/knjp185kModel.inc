<?php

require_once('for_php7.php');


class knjp185kModel extends Model {
    var $field = array();
    var $cmd;

    function knjp185kModel() {
    }

    function init()
    {
        $this->field = array("YEAR_MONTH"       =>  VARS::post("YEAR_MONTH"),       //対象年月
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
        $query = knjp185kQuery::getHrCourse();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $HrCourse[$row["HR_COURSE"]] = $row["HR_COURSE"];
            $defHrCourse[$row["HR_COURSE"]] = "";
        }
        $result->free();

        $grade = array();
        $query = knjp185kQuery::getGrade();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grade[] = $row["GRADE"];
        }
        $result->free();

        $var1 = $db->getOne(knjp185kQuery::getVar1());

        $isMinou = false;
        list($retContents, $retHeader_data) = $this->getKingakuBetuData($db, $HrCourse, $defHrCourse, $grade, $isMinou);

        if ($retHeader_data) {
            $contents  = "振込対象者 国支援金額 府県補助金額 集計CSV\n";
            $contents .= "年,振込金額,データ,";
            $contents .= implode($HrCourse, ",").",総計\n";
            $contents .= $retContents;
            $contents .= "\n\n";
            $header_data = $retHeader_data ? $retHeader_data : $header_data;
        }

        $isMinou = true;
        list($retContents, $retHeader_data) = $this->getKingakuBetuData($db, $HrCourse, $defHrCourse, $grade, $isMinou);

        if ($retHeader_data) {
            $contents .= "未納者 国支援金額 府県補助金額 集計CSV\n";
            $contents .= "年,振込金額,データ,";
            $contents .= implode($HrCourse, ",").",総計\n";
            $contents .= $retContents;
            $header_data = $retHeader_data ? $retHeader_data : $header_data;
        }

        return array($contents, $header_data);
    }

    function getKingakuBetuData($db, $HrCourse, $defHrCourse, $grade, $isMinou) {

        $contents = "";
        $header_data = false;

        $outPutData = array();
        $outCnt = 0;
        $souGouKeiHrCourse = $defHrCourse;

        //学年ごとの金額
        $moneyArray = array();
        foreach ($grade as $key => $val) {
            $moneyArray[$val] = array();
            $query = knjp185kQuery::getPaidMoney($this, $val, $isMinou, $var1);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $moneyArray[$val][] = $row["MONEY"];
                $header_data = true;
            }
        }
        $result->free();

        $query = knjp185kQuery::getKingakuBetuCnt($this, $gVal, $hVal, $money, $isMinou);
        $result = $db->query($query);
        $setData = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setData[$row["GRADE"]][$row["HR"]][$row["MONEY"]] = $row;
        }
        $result->free();

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
                $setTotalFurikomi = 0;
                $setTotalKuni = 0;
                $setTotalFuken = 0;
                $outPutData[$outCnt] .= $setGval.",".$money.",合計/人数,";
                $outPutData[$outCnt + 1] .= ",,合計/振込金額,";
                $outPutData[$outCnt + 2] .= ",,合計/国支援金額,";
                $outPutData[$outCnt + 3] .= ",,合計/府県補助金額,";
                //コース(クラスの頭一桁・・・JとかSとか)
                foreach ($HrCourse as $hKey => $hVal) {
                    $setCnt = array();
                    $setCnt = $setData[$gVal][$hVal][$money];
                    $outPutData[$outCnt] .= $setCnt["CNT"].",";
                    $setTotalCnt += $setCnt["CNT"];
                    $totalHrCourse[$gVal][$hVal]["TOTAL"] += $setCnt["CNT"];

                    $outPutData[$outCnt + 1] .= $setCnt["FURI_MONEY"].",";
                    $setTotalFurikomi += $setCnt["FURI_MONEY"];
                    $totalHrCourse[$gVal][$hVal]["FURIKOMI"] += $setCnt["FURI_MONEY"];

                    $outPutData[$outCnt + 2] .= $setCnt["PLAN_MONEY"].",";
                    $setTotalKuni += $setCnt["PLAN_MONEY"];
                    $totalHrCourse[$gVal][$hVal]["KUNI"] += $setCnt["PLAN_MONEY"];

                    $outPutData[$outCnt + 3] .= $setCnt["REDUC_MONEY"].",";
                    $setTotalFuken += $setCnt["REDUC_MONEY"];
                    $totalHrCourse[$gVal][$hVal]["FUKEN"] += $setCnt["REDUC_MONEY"];
                }
                $outPutData[$outCnt]     .= $setTotalCnt."\n";
                $outPutData[$outCnt + 1] .= $setTotalFurikomi."\n";
                $outPutData[$outCnt + 2] .= $setTotalKuni."\n";
                $outPutData[$outCnt + 3] .= $setTotalFuken."\n";
                $setGval = "";
                $outCnt = $outCnt + 4;
            }
            //学年合計
            $setGTotalCnt = 0;
            $setGTotalFurikomi = 0;
            $setGTotalKuni = 0;
            $setGTotalFuken = 0;
            $outPutData[$outCnt]     .= ",,".$gVal."合計/人数,";
            $outPutData[$outCnt + 1] .= ",,".$gVal."合計/振込金額,";
            $outPutData[$outCnt + 2] .= ",,".$gVal."合計/国支援金額,";
            $outPutData[$outCnt + 3] .= ",,".$gVal."合計/府県補助金額,";

            foreach ($totalHrCourse[$gVal] as $tKey => $total) {
                $outPutData[$outCnt] .= $total["TOTAL"].",";
                $setGTotalCnt += $total["TOTAL"];

                $outPutData[$outCnt + 1] .= $total["FURIKOMI"].",";
                $setGTotalFurikomi += $total["FURIKOMI"];

                $outPutData[$outCnt + 2] .= $total["KUNI"].",";
                $setGTotalKuni += $total["KUNI"];

                $outPutData[$outCnt + 3] .= $total["FUKEN"].",";
                $setGTotalFuken += $total["FUKEN"];

                $souGouKeiHrCourse[$tKey]["TOTAL"]      += $total["TOTAL"];
                $souGouKeiHrCourse[$tKey]["FURIKOMI"]   += $total["FURIKOMI"];
                $souGouKeiHrCourse[$tKey]["KUNI"]       += $total["KUNI"];
                $souGouKeiHrCourse[$tKey]["FUKEN"]      += $total["FUKEN"];
            }
            $outPutData[$outCnt]     .= $setGTotalCnt."\n";
            $outPutData[$outCnt + 1] .= $setGTotalFurikomi."\n";
            $outPutData[$outCnt + 2] .= $setGTotalKuni."\n";
            $outPutData[$outCnt + 3] .= $setGTotalFuken."\n";
            $outCnt = $outCnt + 4;
        }
        //全体合計
        $setSTotalCnt = 0;
        $setSTotalFurikomi = 0;
        $setSTotalKuni = 0;
        $setSTotalFuken = 0;
        $outPutData[$outCnt]     .= ",,全体の合計/人数,";
        $outPutData[$outCnt + 1] .= ",,全体の合計/振込金額,";
        $outPutData[$outCnt + 2] .= ",,全体の合計/国支援金額,";
        $outPutData[$outCnt + 3] .= ",,全体の合計/府県補助金額,";

        foreach ($souGouKeiHrCourse as $sKey => $sTotal) {
            $outPutData[$outCnt]     .= $sTotal["TOTAL"].",";
            $setSTotalCnt += $sTotal["TOTAL"];

            $outPutData[$outCnt + 1] .= $sTotal["FURIKOMI"].",";
            $setSTotalFurikomi += $sTotal["FURIKOMI"];

            $outPutData[$outCnt + 2] .= $sTotal["KUNI"].",";
            $setSTotalKuni += $sTotal["KUNI"];

            $outPutData[$outCnt + 3] .= $sTotal["FUKEN"].",";
            $setSTotalFuken += $sTotal["FUKEN"];
        }
        $outPutData[$outCnt]     .= $setSTotalCnt."\n";
        $outPutData[$outCnt + 1] .= $setSTotalFurikomi."\n";
        $outPutData[$outCnt + 2] .= $setSTotalKuni."\n";
        $outPutData[$outCnt + 3] .= $setSTotalFuken."\n";

        foreach ($outPutData as $key => $val) {
            $contents .= $val;
            $header_data = true;
        }
        return array($contents, $header_data);
    }

    function getSeitoBetuCsv($db) {

        $contents = "";
        $contents .= "振込生徒";
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
                        "REDUC_DEC_FLG_2"       => "決定");

        $contents .= implode($header, ",")."\n";

        $var1 = $db->getOne(knjp185kQuery::getVar1());
        $isMinou = false;
        $contents .= $this->getSeitoBetuData($db, $isMinou, $var1);

        $contents .= "\n\n未納者";

        $isMinou = true;
        $contents .= $this->getSeitoBetuData($db, $isMinou, $var1);

        return array($contents, true);
    }


    function getSeitoBetuData($db, $isMinou, $var1) {
        $mcd11 = "11";
        $mcd12 = "12";
        $mcd13 = "13";
        $taisyouMcd = (substr($this->field["YEAR_MONTH"], 4) == "09") ? $mcd12 : $mcd13;

        $outPutArray = array();
        $query = knjp185kQuery::getSeitoBetuMoney($this, $isMinou, $var1);
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
            $outPutArray[$cnt]["NINZU"] = "1";
            $outPutArray[$cnt]["PREF"] = $row["PREF"];
            $outPutArray[$cnt]["OFFSET_FLG"] = $row["OFFSET_FLG"];
            $outPutArray[$cnt]["LOCK_FLG"] = $row["LOCK_FLG"];
            if ("13" == $taisyouMcd) {
                $outPutArray[$cnt]["REDUCTIONMONEY_1"] = $row["REDUCTIONMONEY_1"];
                $outPutArray[$cnt]["REDUC_DEC_FLG_1"] = $row["REDUC_DEC_FLG_1"];
                $outPutArray[$cnt]["REDUCTIONMONEY_2"] = $row["REDUCTIONMONEY_2"];
                $outPutArray[$cnt]["REDUC_DEC_FLG_2"] = $row["REDUC_DEC_FLG_2"];
            }
            $outPutArray[$cnt]["FUKEN_KEI"] = $row["REDUCTIONMONEY_1"] + $row["REDUCTIONMONEY_2"];

            if (substr($this->field["YEAR_MONTH"], 4) == "12") {
                $outPutArray[$cnt]["REDUCTIONMONEY_1"] = $outPutArray[$cnt]["REDUCTIONMONEY_1"] - $row["ADJUSTMENT_MONEY1"];
                $outPutArray[$cnt]["REDUCTIONMONEY_2"] = $outPutArray[$cnt]["REDUCTIONMONEY_2"] - $row["ADJUSTMENT_MONEY2"];
                $outPutArray[$cnt]["FUKEN_KEI"] = $outPutArray[$cnt]["FUKEN_KEI"] - $row["TOTAL_ADJUSTMENT_MONEY"];
            }
            if (substr($this->field["YEAR_MONTH"], 4) == "03") {
                $outPutArray[$cnt]["REDUCTIONMONEY_1"] = $outPutArray[$cnt]["REDUCTIONMONEY_1"] + $row["BURDEN_CHARGE1"];
                $outPutArray[$cnt]["REDUCTIONMONEY_2"] = $outPutArray[$cnt]["REDUCTIONMONEY_2"] + $row["BURDEN_CHARGE2"];
                $outPutArray[$cnt]["FUKEN_KEI"] = $outPutArray[$cnt]["FUKEN_KEI"] + $row["TOTAL_BURDEN_CHARGE"];
            }

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
            $contents .= "\n";
        }

        return $contents;
    }
}
?>
