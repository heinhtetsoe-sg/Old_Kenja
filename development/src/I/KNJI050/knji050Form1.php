<?php

require_once('for_php7.php');

class knji050Form1
{

    public function main(&$model)
    {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knji050Form1", "POST", "knji050index.php", "", "knji050Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //帳票種別チェックボックス
        $name = array("KOSEKI", "SEITO", "SIMEI", "SCHZIP", "ADDR2", "SCHOOLZIP", "KATSUDO", "GAKUSHU", "TANI");
        foreach ($name as $key => $val) {
            if ($val == "KOSEKI" || $val == "SCHZIP" || $val == "ADDR2" || $val == "SCHOOLZIP") {
                //$extra = $model->field[strtolower($val)] == "1" ? "checked" : "";
                $extra = "checked";
            } else {
                //$extra = ($model->field[strtolower($val)] == "1" || $model->cmd == "") ? "checked" : "";
                $extra = "checked";
            }
            $extra .= " onclick=\"kubun();\" id=\"$val\"";

            $arg["data"][$val] = knjCreateCheckBox($objForm, strtolower($val), "1", $extra, "");
        }

        //未履修科目出力・履修のみ科目出力のデフォルト値設定
        if ($model->field["MIRISYU"] == "" || $model->field["RISYU"] == "" || $model->field["RISYUTOUROKU"] == "") {
            $query = knji050Query::getRisyuMirsyu($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // 値がYなら「しない」それ以外は「する」
                $model->field["MIRISYU"] = "Y" == $row["NAMESPARE1"] ? "2" : "1";
                $model->field["RISYU"] = "Y" == $row["NAMESPARE2"] ? "2" : "1";
                $model->field["RISYUTOUROKU"] = "Y" == $row["NAMESPARE3"] ? "2" : "1";
            }
            $result->free();
            $model->field["MIRISYU"] = isset($model->field["MIRISYU"]) ? $model->field["MIRISYU"] : "2"; // デフォルトは「しない」
            $model->field["RISYU"] = isset($model->field["RISYU"]) ? $model->field["RISYU"] : "1"; // デフォルトは「する」
            $model->field["RISYUTOUROKU"] = isset($model->field["RISYUTOUROKU"]) ? $model->field["RISYUTOUROKU"] : "2"; // デフォルトは「しない」
        }

        //未履修科目出力ラジオボタンを作成する 1:する 2:しない
        $opt_mirisyu = array(1, 2);
        $extra  = array("id=\"MIRISYU1\" onclick=\"checkRisyu();\"", "id=\"MIRISYU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "MIRISYU", $model->field["MIRISYU"], $extra, $opt_mirisyu, get_count($opt_mirisyu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //履修のみ科目出力ラジオボタン 1:する 2:しない
        $optRisyu = array(1, 2);
        $extra  = array("id=\"RISYU1\" onclick=\"checkRisyu();\"", "id=\"RISYU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "RISYU", $model->field["RISYU"], $extra, $optRisyu, get_count($optRisyu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //履修登録のみ科目出力ラジオボタン 1:する 2:しない
        $optRisyuT = array(1, 2);
        $extra  = array("id=\"RISYUTOUROKU1\" onclick=\"checkRisyu();\"", "id=\"RISYUTOUROKU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "RISYUTOUROKU", $model->field["RISYUTOUROKU"], $extra, $optRisyuT, get_count($optRisyuT));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        if ($model->Properties["hyoteiYomikaeRadio"] == "1") {
            $arg["showHyoteiYomikaeRadio"] = "1";
            if ($model->field["HYOTEI"] == "") {
                $model->field["HYOTEI"] = "notPrint1";
            }
            $replace_vals = array(
                "HYOTEI1" => "0",
                "HYOTEI2" => "notPrint1",
                "HYOTEI3" => "print100");

            $opt = array(1, 2, 3);
            $extra  = array("id=\"HYOTEI1\"", "id=\"HYOTEI2\"", "id=\"HYOTEI3\"");
            $radioArray = knjCreateRadio($objForm, "HYOTEI", $model->field["HYOTEI"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                if (preg_match("/ checked/", $val)) {
                    $val = preg_replace("/ checked/", "", $val);
                }
                $checked = $replace_vals[$key] == $model->field["HYOTEI"] ? " checked " : "";
                $val = preg_replace("/'\d+'/", "'".$replace_vals[$key]."'".$checked, $val); // radioの値を1,2,3から変更する
                $arg["data"][$key] = $val;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knji050Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEKI") {
        $value = ($value && $value_flg) ? $value : STAFFCD;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    ////CSVボタンを作成する
    //$extra = "onclick=\"return btn_submit('csv');\"";
    //$arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJI050");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO");
    knjCreateHidden($objForm, "G_YEAR");
    knjCreateHidden($objForm, "G_SEMESTER");
    knjCreateHidden($objForm, "G_GRADE");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);

    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
    knjCreateHidden($objForm, "seitoSidoYorokuFormType", $model->Properties["seitoSidoYorokuFormType"]);
    knjCreateHidden($objForm, "seitoSidoYorokuFieldSize", $model->Properties["seitoSidoYorokuFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuSougouFieldSize", $model->Properties["seitoSidoYorokuSougouFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuSpecialactremarkFieldSize", $model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuKinsokuForm", $model->Properties["seitoSidoYorokuKinsokuForm"]);
    knjCreateHidden($objForm, "seitoSidoYorokuZaisekiMae", $model->Properties["seitoSidoYorokuZaisekiMae"]);
    knjCreateHidden($objForm, "seitoSidoYorokuKoumokuMei", $model->Properties["seitoSidoYorokuKoumokuMei"]);
    knjCreateHidden($objForm, "seitoSidoYorokuHyotei0ToBlank", $model->Properties["seitoSidoYorokuHyotei0ToBlank"]);
    knjCreateHidden($objForm, "seitoSidoYorokuYoshiki2PrintOrder", $model->Properties["seitoSidoYorokuYoshiki2PrintOrder"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalremarkSize", $model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_SpecialactremarkSize", $model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_Attendrec_RemarkSize", $model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherStudyrec", $model->Properties["seitoSidoYorokuNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherAttendrec", $model->Properties["seitoSidoYorokuNotPrintAnotherAttendrec"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg", $model->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]);
    knjCreateHidden($objForm, "train_ref_1_2_3_field_size", $model->Properties["train_ref_1_2_3_field_size"]);
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
    knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
    knjCreateHidden($objForm, "useAssessCourseMst", $model->Properties["useAssessCourseMst"]);
    knjCreateHidden($objForm, "seitoSidoYorokuHozonkikan", $model->Properties["seitoSidoYorokuHozonkikan"]);
}
