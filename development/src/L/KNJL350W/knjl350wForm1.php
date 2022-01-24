<?php

require_once('for_php7.php');

class knjl350wForm1
{
    function main(&$model) {
        //権限チェック
        $adminFlg = knjl350wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1"){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl350windex.php", "", "edit");

        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //年度
        $query = knjl350wQuery::getYear();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1, "");

        //入試制度コンボ
        $query = knjl350wQuery::get_name_cd($model->field["YEAR"], "L003");
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db2, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試区分
        $query = knjl350wQuery::getCourseTestDiv($model);
        $result = $db->query($query);
        $courseTestDiv = "";
        $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $courseTestDiv .= $sep."'{$row["TESTDIV"]}'";
            $sep = ",";
        }
        $model->courseTestDiv = $courseTestDiv ? $courseTestDiv : "''";
        $query = knjl350wQuery::get_name_cdAft($model, "L004", "TESTDIV");
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db2, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "");

        //累計区分
        $query = knjl350wQuery::get_name_cdAft($model, "L040", "RUIKEI_DIV");
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db2, $query, "RUIKEI_DIV", $model->field["RUIKEI_DIV"], $extra, 1, "");

        //追検査
        //2018/02/07 濱崎修正　後期選抜、秋季選抜の時のみ
        if ($model->field["TESTDIV"] == "5" || $model->field["TESTDIV"] == "8") {
            $arg["TESTDIV2_SET"] = "1";
            $extra = " id=\"TESTDIV2\" onClick=\"return btn_submit('main');\"";
            $checked = $model->field["TESTDIV2"] == "1" ? " checked " : "";
            $arg["TESTDIV2"] = knjCreateCheckBox($objForm, "TESTDIV2", "1", $checked.$extra);
            $model->field["TESTDIV2"] = $model->field["TESTDIV2"] ? $model->field["TESTDIV2"] : "0";
        } else {
            $model->field["TESTDIV2"] = "0";
        }

        //V_SCHOOL_MSTから学校コードを取得
        $query = knjl350wQuery::getSchoolMst($model);
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->schoolcd = $rtnRow["KYOUIKU_IINKAI_SCHOOLCD"];
        $model->prefcd = $rtnRow["PREF_CD"];
        if ($model->schoolcd == ''){
            $arg["jscript"] .= " OnSchoolcdError();";
        }

        //タイトル
        $arg["TITLE"] = "進路状況集計表";
        //報告履歴
        $query = knjl350wQuery::getReport($model);
        $setHoukoku = $db2->getRow($query, DB_FETCHMODE_ASSOC);
        $dispDateTime = "";
        if ($setHoukoku["EXECUTE_DATE"]) {
            list($setDate, $setTime) = explode(".", $setHoukoku["EXECUTE_DATE"]);
            $dispDateTime = date("Y/m/d H:i", strtotime($setDate))." 報告者：".$setHoukoku["STAFFNAME"];
        }
        $arg["REPORT"] = $dispDateTime."　".$setHoukoku["LABEL"];

        $btnDisabeld = $setHoukoku["FIXED_FLG"] == "1" ? " disabled " : "";
        $btnFixedDisabeld = $setHoukoku["FIXED_FLG"] == "1" && !$setHoukoku["EXECUTE_DATE"] ? "" : " disabled ";

        //県への報告
        $extra = "onclick=\"return btn_submit('houkoku');\"".$btnFixedDisabeld;
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告", $extra);

        //項目名(列)
        $model->nameArray = array("NYUUGAKU_TEIIN", 
                                  "BOSYUU_TEIIN", 
                                  "SIGAN_M", 
                                  "SIGAN_W", 
                                  "SIGAN_K", 
                                  "SIGAN_T", 
                                  "OBOEGAKI_M", 
                                  "OBOEGAKI_W", 
                                  "OBOEGAKI_K", 
                                  "OBOEGAKI_T", 
                                  "KAIGAI_M", 
                                  "KAIGAI_W", 
                                  "KAIGAI_K", 
                                  "KAIGAI_T", 
                                  "KISOTSU_M", 
                                  "KISOTSU_W", 
                                  "KISOTSU_K", 
                                  "KISOTSU_T", 
                                  "SEIJIN_M", 
                                  "SEIJIN_W", 
                                  "SEIJIN_K", 
                                  "SEIJIN_T", 
                                  "TSUUGAKUGAI_M", 
                                  "TSUUGAKUGAI_W", 
                                  "TSUUGAKUGAI_K", 
                                  "TSUUGAKUGAI_T", 
                                  "SPORTTOKU_M", 
                                  "SPORTTOKU_W", 
                                  "SPORTTOKU_K", 
                                  "SPORTTOKU_T", 
                                  "GOUKAKU_M", 
                                  "GOUKAKU_W", 
                                  "GOUKAKU_K", 
                                  "GOUKAKU_T", 
                                  "HOSHOUNIN_M", 
                                  "HOSHOUNIN_W", 
                                  "HOSHOUNIN_K", 
                                  "HOSHOUNIN_T",
                                  "INNER_TO_OUT_J_M",
                                  "INNER_TO_OUT_J_W",
                                  "INNER_TO_OUT_J_K",
                                  "INNER_TO_OUT_J_T",
                                  "TOKUREI3_M", 
                                  "TOKUREI3_W", 
                                  "TOKUREI3_K", 
                                  "TOKUREI3_T",
                                  "OUTER_M", 
                                  "OUTER_W", 
                                  "OUTER_K", 
                                  "OUTER_T"
                                  );

        unset($model->fields["CODE"]);

        //タイトル
        $query = knjl350wQuery::getTitleQuery($model);
        $result = $db2->query($query);
        $setWidth = 0;
        while ($titleRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["TITLE".$titleRow["LARGE_DIV"]] = $titleRow["LARGE_NAME"];
            if ($titleRow["LARGE_DIV"] == "98" || $titleRow["LARGE_DIV"] == "99") {
                $setWidth += 160;
            } else {
                $setWidth += 210;
            }
        }
        $arg["TITLE_WIDTH"] = $setWidth;

        //課程学科コース設定
        $query = knjl350wQuery::getCMC($model);
        $result = $db->query($query);
        $model->cmc = array();
        while ($cmcRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->cmc[] = $cmcRow;
        }
        $totalData = array();
        foreach ($model->cmc as $cmcKey => $cmcVal) {
            if ($model->cmd == "recalc") {
                $query = knjl350wQuery::getRecalcQuery($model, $cmcVal);
            } else {
                $query = knjl350wQuery::getReadQuery($model, $cmcVal);
            }
            $setData = array();
            $setData["MAJORNAME_SIZE"] = strlen($cmcVal["CMC_NAME"]) > 42 ? -2 : 3;
            $setData["MAJORNAME"] = $cmcVal["CMC_NAME"];
            $setCmc = $cmcVal["COURSECD"].$cmcVal["MAJORCD"].$cmcVal["COURSECODE"];
            $getData = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $query = knjl350wQuery::getCapacityQuery($model, $cmcVal);
            $getCapacity = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $setData["NYUUGAKU_COLOR"] = "white";
            if ($setCmc != "99999999" && $getData["NYUUGAKU_TEIIN"] != $getCapacity["NYUUGAKU_TEIIN"]) {
                $setData["NYUUGAKU_COLOR"] = "pink";
            }
            $getData["NYUUGAKU_TEIIN"] = $getCapacity["NYUUGAKU_TEIIN"];
            $setData["BOSYUU_COLOR"] = "white";
            if ($setCmc != "99999999" && $getData["BOSYUU_TEIIN"] != $getCapacity["BOSYUU_TEIIN"]) {
                $setData["BOSYUU_COLOR"] = "pink";
            }
            $getData["BOSYUU_TEIIN"] = $getCapacity["BOSYUU_TEIIN"];
            foreach ($model->nameArray as $name) {
                $nameCMC = $name.$cmcVal["COURSECD"]."_".$cmcVal["MAJORCD"]."_".$cmcVal["COURSECODE"];
                $readOnly = "";
                $readStyle = "";
                $setSize = "3";
                if ($setCmc == "99999999" || preg_match("/_M$/", $name) || preg_match("/_W$/", $name) || preg_match("/_K$/", $name)
                    || $setHoukoku["FIXED_FLG"] == "1" || $name == "NYUUGAKU_TEIIN" || $name == "BOSYUU_TEIIN") {
                    $readOnly = " readonly ";
                    $readStyle = "; background-color:cccccc;";
                    $setSize = "4";
                }
                if ($setCmc == "99999999" && ($name == "NYUUGAKU_TEIIN" || $name == "BOSYUU_TEIIN")) {
                    $getData["NYUUGAKU_TEIIN"] = $totalData["NYUUGAKU_TEIIN"];
                    $getData["BOSYUU_TEIIN"] = $totalData["BOSYUU_TEIIN"];
                }
                //textbox
                $extra = $readOnly."style=\"text-align:right{$readStyle}\" onblur=\"this.value = toInteger(this.value)\" onChange=\"changeColor('{$nameCMC}')\" ";
                $val = ($getData[$name] == "") ? "0" : $getData[$name] ;
                $setData[$name] = knjCreateTextBox($objForm, $val, $nameCMC, $setSize, $setSize, $extra);
                $setData["ID_".$name] = "ID_".$nameCMC;
                if ($setCmc != "99999999") {
                    $totalData[$name] += $val;
                }
            }
            if ($model->cmd == "recalc" && $setCmc == "99999999") {
                foreach ($totalData as $name => $totalVal) {
                    $readOnly = " readonly ";
                    $readStyle = "; background-color:cccccc;";
                    $setSize = "4";
                    //textbox
                    $extra = $readOnly."style=\"text-align:right{$readStyle}\" onblur=\"this.value = toInteger(this.value)\" ";
                    $val = ($totalVal == "") ? "0" : $totalVal;
                    $totalData[$name] = knjCreateTextBox($objForm, $val, $name.$cmcVal["COURSECD"]."_".$cmcVal["MAJORCD"]."_".$cmcVal["COURSECODE"], $setSize, $setSize, $extra);
                }
                $totalData["NYUUGAKU_COLOR"] = "white";
                $totalData["BOSYUU_COLOR"] = "white";
                $totalData["MAJORNAME"] = $cmcVal["CMC_NAME"];
                $arg["data"][] = $totalData;
            } else {
                $arg["data"][] = $setData;
            }
        }

        //再計算ボタン
        $extra = "onclick=\"return btn_submit('recalc');\"".$btnDisabeld;
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"".$btnDisabeld;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"".$btnDisabeld;
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷ボタン
        $extra = "onclick=\"newwin('".SERVLET_URL."');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('fixed');\"".$btnDisabeld;
        $arg["btn_fixed"] = knjCreateBtn($objForm, "btn_fixed", "確 定", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED[]", $model->updated);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "DBNAME2", DB_DATABASE2);
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");
        knjCreateHidden($objForm, "KYOUIKU_IINKAI_SCHOOLCD_YEAR", (CTRL_YEAR + 1));

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //印刷処理
        $arg["print"] = $model->print == "on" ? "newwin('".SERVLET_URL."');" :"";
        $model->print = "off";

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl350wForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") {
        if ($blank == "ALL") {
            $opt[] = array("label" => "-- 全て --", "value" => "");
        } else {
            $opt[] = array('label' => "", 'value' => "");
        }
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($name == 'APPLICANTDIV' || $name == 'TESTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR + 1;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
