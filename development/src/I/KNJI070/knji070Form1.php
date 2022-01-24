<?php

require_once('for_php7.php');

class knji070Form1 {

    function main(&$model) {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knji070Form1", "POST", "index.php", "", "knji070Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //成績証明書種類ラジオボタン 1:日本語 2:英語
        $opt = array(1, 2);
        $model->field["OUTPUT"] = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //記載日付
        $model->field["DATE"] = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-","/",CTRL_DATE);
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //記載責任者コンボボックス
        $query = knji070Query::getSelectStaff(CTRL_YEAR);
        makeCmb($objForm, $arg, $db, $query, "SEKI", $model->field["SEKI"], "", 1, "blank", $model);

        // ６年用フォーム選択チェックボックス
        if ($model->control["学校区分"] == "1") {
            $extra  = ($model->field["FORM6"] == "on") ? "checked" : "";
            $extra .= " id=\"FORM6\"";
            $arg["data"]["FORM6"] = knjCreateCheckBox($objForm, "FORM6", "on", $extra, "");
            $arg["tani"] = "1";
        }

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJI070");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO");
        knjCreateHidden($objForm, "G_YEAR");
        knjCreateHidden($objForm, "G_SEMESTER");
        knjCreateHidden($objForm, "G_GRADE");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "Knje080UseAForm", $model->Knje080UseAForm);
        knjCreateHidden($objForm, "seisekishoumeishoTaniPrintRyugaku", $model->Properties["seisekishoumeishoTaniPrintRyugaku"]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
        knjCreateHidden($objForm, "certifPrintRealName", $model->Properties["certifPrintRealName"]);
        knjCreateHidden($objForm, "seisekishoumeishoPrintCoursecodename", $model->Properties["seisekishoumeishoPrintCoursecodename"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "seisekishoumeishoCreditOnlyClasscd", $model->Properties["seisekishoumeishoCreditOnlyClasscd"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knji070Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "SEKI") {
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["VALUE"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $SET_VALUE = $ume.substr($row["VALUE"], (strlen($row["VALUE"]) - (int)$simo), (int)$simo);
            } else {
                $SET_VALUE = $row["VALUE"];
            }
            $row["LABEL"] = str_replace($row["VALUE"], $SET_VALUE, $row["LABEL"]);
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEKI") {
        $value = ($value && $value_flg) ? $value : STAFFCD;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
