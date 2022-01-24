<?php

require_once('for_php7.php');

class knjb0012Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb0012Form1", "POST", "knjb0012index.php", "", "knjb0012Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->exeYear;

        //学期コンボ
        $query = knjb0012Query::getSemester($model);
        $extra = "onchange=\"return btn_submit('chgSeme')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //履修履歴コンボ
        $query = knjb0012Query::getRirekiCode($model);
        $extra = "onChange=\"return btn_submit('subMain');\"";
        makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE", $model->field["RIREKI_CODE"], $extra, 1);

        //生徒情報
        $query = knjb0012Query::getSchregInfo($model);
        $model->schregInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["SCHREG_INFO"] = $model->schregInfo["HR_NAME"]."　{$model->schregInfo["ATTENDNO"]}番　{$model->schregInfo["NAME"]}({$model->schregInfo["SCHREGNO"]})";

        //基本時間割コンボ
        $query = knjb0012Query::getSchPatternH($model);
        $opt = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[]= array('label' => $row["SEMESTERNAME"]." Seq".$row["BSCSEQ"].":".$row["TITLE"],
                          'value' => $row["YEAR"].",".$row["BSCSEQ"].",".$row["SEMESTER"]);
        }
        $result->free();
        $extra = "onchange=\"return btn_submit('subMain')\"";
        $model->field["SCH_PTRN"] = $model->field["SCH_PTRN"] ? $model->field["SCH_PTRN"] : $opt[0]["value"];
        $arg["data"]["SCH_PTRN"] = knjCreateCombo($objForm, "SCH_PTRN", $model->field["SCH_PTRN"], $opt, $extra, 1);

        //校時(基本時間割から)
        $query = knjb0012Query::getPeriod($model);
        $result = $db->query($query);
        $setPeri = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $setPeri[$row["PERIODCD"]]= $row["NAME1"];
        }

        //受講データ
        $query = knjb0012Query::getChairStd($model);
        $result = $db->query($query);
        $setChairStd = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $setChairStd[$row["CHAIRCD"]]= $row["CHAIRCD"];
        }

        $schChrCnt = 0;
        foreach ($setPeri as $periCd => $periVal) {
            $query = knjb0012Query::getChairDat($model, $periCd);
            $result = $db->query($query);
            $setChair = array();
            $setChairCnt = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $setChair[$row["DAYCD"].":".$row["CHAIRCD"]]= $row["CHAIRNAME"];
                $setChairCnt[$row["DAYCD"]] = $setChairCnt[$row["DAYCD"]] > 0 ? $setChairCnt[$row["DAYCD"]] + 1 : 1;
            }
            $result->free();

            $maxCnt = 1;
            foreach ($setChairCnt as $cntKey => $cntVal) {
                if ($cntVal > $maxCnt) {
                    $maxCnt = $cntVal;
                }
            }

            $setVal["MEISAI1"] = "";
            $setVal["MEISAI2"] = "";
            $setVal["MEISAI3"] = "";
            $setVal["MEISAI4"] = "";
            $setVal["MEISAI5"] = "";
            $setVal["MEISAI6"] = "";
            $setVal["MEISAI7"] = "";
            $setBr = "";
            $befDay = "";
            foreach ($setChair as $chairKey => $chairVal) {
                list($dayCd, $chairCd) = preg_split("/:/", $chairKey);
                if ($befDay <> $dayCd) {
                    $setBr = "";
                }
                //checkbox
                $setName = "CHAIR{$schChrCnt}";
                $extra = "id = \"{$setName}\" onClick=\"ClickValAll(this)\"";
                if ($setChairStd[$chairCd]) {
                    $extra .= " checked ";
                }
                $setCheck = knjCreateCheckBox($objForm, $setName, $chairCd, $extra);

                $setVal["MEISAI".$dayCd] .= $setBr.$setCheck."<LABEL for=\"{$setName}\">".$chairVal."</LABEL>";
                $setBr = "<br>";
                $schChrCnt++;
                $befDay = $dayCd;
            }

            $habaTyousei = 26;
            $setHaba = $maxCnt > 1 ? $habaTyousei : 30;
            $setVal["PERI_HEIGHT"] = (int)$maxCnt * (int)$setHaba;
            $arg["meisai"][] = $setVal;

            $setPeri["PERIOD_NAME"] = $periVal;
            $setPeri["PERI_HEIGHT"] = (int)$maxCnt * (int)$setHaba;
            $arg["head"][] = $setPeri;
        }

        //button
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"return closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJB0012");
        knjCreateHidden($objForm, "CHAIR_CNT", $schChrCnt);
        //印刷パラメータ
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", $model->exeYear);
        knjCreateHidden($objForm, "PRINT_SCHREGNO", $model->schregNo);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb0012Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "全　て",
                       'value' => "");
    }
    $value_flg = false;
    $retInstate = "(";
    $retInstate2 = "(";
    $sep = "";
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
            $retInstate2 .= "'{$row["VALUE"]}'";
        }
        $retInstate .= $sep."'{$row["VALUE"]}'";
        $sep = ",";
    }
    $result->free();
    $retInstate .= ")";
    $retInstate2 .= ")";
    $retInstate = $retInstate == "()" ? "('')" : $retInstate;
    $retInstate2 = $retInstate2 == "()" ? "('')" : $retInstate2;

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $value ? $retInstate2 : $retInstate;
}
?>
