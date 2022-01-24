<?php

require_once('for_php7.php');


class knjg020aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjg020aForm1", "POST", "knjg020aindex.php", "", "knjg020aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjg020aQuery::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('knjg020a')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->setSchoolKind, $extra, 1, "");
        } else {
            $model->setSchoolKind = SCHOOLKIND;
        }

        //学校マスタ
        $query = knjg020aQuery::getSchoolMst($model);
        $model->schoolMst = array();
        $model->schoolMst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //出力対象ラジオボタン 1:卒業生 2:在校生 3:卒業生・在校生
        $opt1 = array(1, 2, 3);
        $model->field["STD_DIV"] = ($model->field["STD_DIV"] == "") ? "3" : $model->field["STD_DIV"];
        $extra = array("id=\"STD_DIV1\" onClick=\"btn_submit('knjg020a')\"", "id=\"STD_DIV2\" onClick=\"btn_submit('knjg020a')\"", "id=\"STD_DIV3\" onClick=\"btn_submit('knjg020a')\"");
        $radioArray = knjCreateRadio($objForm, "STD_DIV", $model->field["STD_DIV"], $extra, $opt1, get_count($opt1));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        makePrintMeisai($objForm, $arg, $db, $model);
        $cnt = 1;
        $page = get_count($model->printZumiData);
        //hidden
        knjCreateHidden($objForm, "PAGE_CNT", $page);
        $paramPrintPage = "";
        $paramPrintPageSep = "";
        for ($pi = $page; $pi > 0; $pi--) {
            $setVal = $model->printZumiData[$pi];
            $setVal["LINE_NO"] = $cnt;
            $setVal["PAGE_NO"] = $pi;
            $extra = "";
            if ($model->field["UPD_CHECK"][$pi]) {
                $extra = " checked ";
                $paramPrintPage .= $paramPrintPageSep.$pi;
                $paramPrintPageSep = ",";
            }
            $setVal["UPD_CHECK"] = knjCreateCheckBox($objForm, "UPD_CHECK_{$pi}", $pi, $extra);
            $arg["data2"][] = $setVal;
            $cnt++;
        }

        //印刷ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "更新／プレビュー／印刷", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJG020A");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", $model->setSchoolKind);
        knjCreateHidden($objForm, "certif_no_8keta", $model->Properties["certif_no_8keta"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        $paramPrintPageArray = preg_split("/,/", $paramPrintPage);
        $paramPrintPage = "";
        $paramPrintPageSep = "";
        for ($pi = get_count($paramPrintPageArray); $pi > 0; $pi--) {
            $paramPrintPage .= $paramPrintPageSep.$paramPrintPageArray[$pi - 1];
            $paramPrintPageSep = ",";
        }
        knjCreateHidden($objForm, "PrintPage", $paramPrintPage);

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "updEdit") {
            $arg["printOut"] = "newwin('" . SERVLET_URL . "');";
        }

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg020aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//明細
function makePrintMeisai(&$objForm, &$arg, $db, &$model) {
    $query  = knjg020aQuery::getPageData($model);
    $result = $db->query($query);
    $model->printData = array();
    $model->printZumiData = array();
    $page = 1;
    $line = 1;
    $allCnt = 1;
    $printFlg = false;
    $notPrintFlg = false;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($line > 20) {
            if ($printFlg && !$notPrintFlg) {
                $model->printZumiData[$page]["STATUS"] = "印刷済み";
            } else if (!$printFlg && $notPrintFlg) {
                $model->printZumiData[$page]["STATUS"] = "未印刷";
            } else {
                $model->printZumiData[$page]["STATUS"] = "一部印刷";
            }
            $page++;
            $line = 1;
            $printFlg = false;
            $notPrintFlg = false;
        }
        if ($line == "1") {
            $model->printZumiData[$page]["S_CERTIF_NO"] = $allCnt;
        }
        $model->printZumiData[$page]["E_CERTIF_NO"] = $allCnt;
        $model->printData[$page][$line] = $row;
        if ($row["HAKKOUZUMI"] == "1") {
            $printFlg = true;
        } else {
            $notPrintFlg = true;
        }
        $line++;
        $allCnt++;
    }
    $result->free();
    if ($line > 1) {
        if ($printFlg && !$notPrintFlg) {
            $model->printZumiData[$page]["STATUS"] = "印刷済み";
        } else if (!$printFlg && $notPrintFlg) {
            $model->printZumiData[$page]["STATUS"] = "未印刷";
        } else {
            $model->printZumiData[$page]["STATUS"] = "一部印刷";
        }
    }
}
?>
