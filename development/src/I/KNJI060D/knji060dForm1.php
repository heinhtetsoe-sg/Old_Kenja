<?php

require_once('for_php7.php');

class knji060dForm1
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
        $arg["start"] = $objForm->get_start("knji060dForm1", "POST", "knji060dindex.php", "", "knji060dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        if ($model->Properties["tyousasyoHankiNintei"] == '1') {
            //調査書種類ラジオボタン 1:進学用（通年用） 1h:進学用（半期認定）2:就職用（通年用） 2h:就職用（半期認定）
            $arg["hanki"] = "1";
            $opt_output = array("1", "1h", "2", "2h");
            $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT1h\"", "id=\"OUTPUT2\"", "id=\"OUTPUT2h\"");
        } else {
            //調査書種類ラジオボタン 1:進学用 2:就職用
            $arg["notHanki"] = "1";
            $opt_output = array(1, 2);
            $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        }
        for ($i = 0; $i < get_count($opt_output); $i++) {
            $objForm->ae(array("type" => "radio",
                               "name" => "OUTPUT",
                               "value" => $model->field["OUTPUT"],
                               "extrahtml" => $extra[$i],
                               "multiple" => $opt_output));

            $arg["data"]["OUTPUT".$opt_output[$i]] = $objForm->ge("OUTPUT", $opt_output[$i]);
        }
        //調査書種類ラジオボタン 1:進学用 2:就職用
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //漢字氏名印字ラジオボタン 1:する 2:しない
        $opt = array(1, 2);
        $model->field["KANJI"] = ($model->field["KANJI"] == "") ? "1" : $model->field["KANJI"];
        $extra = array("id=\"KANJI1\"", "id=\"KANJI2\"");
        $radioArray = knjCreateRadio($objForm, "KANJI", $model->field["KANJI"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        if ($model->Properties["useProvFlg"] == 1) {
            $arg["data"]["HYOTEI_KARI"] = '仮';
        }

        //評定読替の項目を表示するかしないかのフラグ
        if (in_array($model->Properties["hyoteiYomikae"], array("1", "1_off"))) {
            $arg["hyoteiYomikae"] = '1';

            if ($model->Properties["hyoteiYomikaeRadio"] == "1") {
                $arg["data"]["HYOTEI_TITLE"] = "評定読替・切替";
                if ((!$model->cmd || $model->cmd == "edit") && $model->field["HYOTEI"] == '') {
                    $model->field["HYOTEI"] = "notPrint1";
                }
                $opt = array(1, 2, 3, 4);
                $extra  = array("id=\"HYOTEI1\"", "id=\"HYOTEI2\"", "id=\"HYOTEI3\"", "id=\"HYOTEI4\"");
                $labels = array(
                    "HYOTEI1" => "評定読替・切替しない",
                    "HYOTEI2" => $arg["data"]["HYOTEI_KARI"]."評定が１の場合は２で処理する。",
                    "HYOTEI3" => "評定が１の場合は表示しない。",
                    "HYOTEI4" => "評定ではなく100段階評価で表示する。");
                $replace_vals = array(
                    "HYOTEI1" => "off",
                    "HYOTEI2" => "on",
                    "HYOTEI3" => "notPrint1",
                    "HYOTEI4" => "print100");
                $radioArray = knjCreateRadio($objForm, "HYOTEI", $model->field["HYOTEI"], $extra, $opt, get_count($opt));
                $hyoutei = "";
                foreach ($radioArray as $key => $val) {
                    if ($hyoutei) {
                        $hyoutei .= "<BR>";
                    }
                    if (preg_match("/ checked/", $val)) {
                        $val = preg_replace("/ checked/", "", $val);
                    }
                    $checked = $replace_vals[$key] == $model->field["HYOTEI"] ? " checked " : "";
                    $val = preg_replace("/'\d+'/", "'".$replace_vals[$key]."'".$checked, $val); // radioの値を1,2,3から変更する
                    $hyoutei .= $val."<LABEL for='".$key."'>".$labels[$key]."</LABEL>";
                }
                $arg["data"]["HYOTEI"] = $hyoutei;
                // 注意:KNJE070、KNJI060では値は"on"。KNJG010は"1"(KNJG010.classがパラメータ"on"で渡す)。
            } else {
                //評定読替チェックボックス
                $arg["data"]["HYOTEI_TITLE"] = "評定読替";
                if (!$model->cmd && $model->Properties["hyoteiYomikae"] == "1") {
                    $model->field["HYOTEI"] = "on";
                }
                $extra  = $model->field["HYOTEI"] == "on" ? "checked" : "";
                $extra .= " id=\"HYOTEI\"";
                $arg["data"]["HYOTEI"] = knjCreateCheckBox($objForm, "HYOTEI", "on", $extra, "").'<LABEL for="HYOTEI">'.$arg["data"]["HYOTEI_KARI"].'評定が１の場合は２で処理する。</LABEL>';
            }
        }

        //評定平均算出
        if ("1" == $model->Properties["tyousasho2020GvalCalcCheck"]) {
            $arg["showGvalCalcCheck"] = '1';

            //評定平均算出
            $opt = array(1, 2); // 1:単純平均 2:加重平均
            if ($model->field["GVAL_CALC_CHECK"] == "") {
                if ($schooldiv["GVAL_CALC"] == "1") { // GVAL_CALCは0:平均値 1:単位による重み付け
                    $model->field["GVAL_CALC_CHECK"] = "2";
                } else {
                    $model->field["GVAL_CALC_CHECK"] = "1";
                }
            }
            //ラジオボタンを作成
            $on = "onclick=\"return gvalCalcChecked();\" ";
            $extra = array("id=\"GVAL_CALC_CHECK0\" ".$on, "id=\"GVAL_CALC_CHECK1\" ".$on);
            $radioArray = knjCreateRadio($objForm, "GVAL_CALC_CHECK", $model->field["GVAL_CALC_CHECK"], $extra, $opt, count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        if ("1" == $model->Properties["tyousasho2020PrintAvgRank"]) {
            $arg["showPrintAvgRank"] = '1';
            //評定平均順位を表示
            $opt = array(1, 2); // 1:する 2:しない
            if ($model->field["PRINT_AVG_RANK"] == "") {
                $model->field["PRINT_AVG_RANK"] = "2";
            }
            //ラジオボタンを作成
            $extra = array("id=\"PRINT_AVG_RANK1\" ", "id=\"PRINT_AVG_RANK2\" ");
            $radioArray = knjCreateRadio($objForm, "PRINT_AVG_RANK", $model->field["PRINT_AVG_RANK"], $extra, $opt, count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //学習成績概評チェックボックス
        if ($model->cmd == 'edit') {
            if ($model->Properties["tyousasyoNotCheckGaihyoComment"] == '1') {
                $model->field["COMMENT"] = "";
            } else {
                $model->field["COMMENT"] = "on";
            }
        }
        $extra  = ($model->field["COMMENT"] == "on") ? "checked" : "";
        $extra .= " id=\"COMMENT\"";
        $arg["data"]["COMMENT"] = knjCreateCheckBox($objForm, "COMMENT", "on", $extra, "");

        //出欠日数チェックボックス
        $extra  = ($model->field["tyousasyoNotPrintAnotherAttendrec"] == "on" || $model->cmd == "edit" && "1" == $model->Properties["tyousasyoSetNotPrintAnotherAttendrec"]) ? "checked " : "";
        $extra .= " id=\"tyousasyoNotPrintAnotherAttendrec\"";
        $arg["data"]["tyousasyoNotPrintAnotherAttendrec"] = knjCreateCheckBox($objForm, "tyousasyoNotPrintAnotherAttendrec", "on", $extra, "");

        //近大チェック
        $kindai_flg = $db->getOne(knji060dQuery::checkKindai());
        if (!$kindai_flg) {
            $arg["notKindai"] = '1';
        }

        //未履修科目出力ラジオボタン 1:する 2:しない
        $opt = array(1, 2);
        if ($model->field["MIRISYU"] == "") {
            if ((!$model->cmd || $model->cmd == "edit") && $model->field["HYOTEI"] == 'notPrint1') {
                $model->field["MIRISYU"] = "1";
            } else {
                $model->field["MIRISYU"] = "2";
            }
        }
        $click = " onclick=\"checkRisyu();\"";
        $extra = array("id=\"MIRISYU1\"".$click, "id=\"MIRISYU2\"".$click);
        $radioArray = knjCreateRadio($objForm, "MIRISYU", $model->field["MIRISYU"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //履修のみ科目出力ラジオボタン 1:する 2:しない
        $opt = array(1, 2);
        $model->field["RISYU"] = ($model->field["RISYU"] == "") ? "1" : $model->field["RISYU"];
        $click = " onclick=\"checkRisyu();\"";
        $extra = array("id=\"RISYU1\"".$click, "id=\"RISYU2\"".$click);
        $radioArray = knjCreateRadio($objForm, "RISYU", $model->field["RISYU"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //総合的な学習の時間の単位を０表示ラジオボタン 1:する 2:しない
        $opt_sougou = array(1, 2);
        $model->field["TANIPRINT_SOUGOU"] = ($model->field["TANIPRINT_SOUGOU"] == "") ? $model->Properties["tyousasyoTaniPrint"] : $model->field["TANIPRINT_SOUGOU"];
        $extra = array("id=\"TANIPRINT_SOUGOU1\"", "id=\"TANIPRINT_SOUGOU2\"");
        $radioArray = knjCreateRadio($objForm, "TANIPRINT_SOUGOU", $model->field["TANIPRINT_SOUGOU"], $extra, $opt_sougou, get_count($opt_sougou));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //留学の単位を０表示ラジオボタン 1:する 2:しない
        $opt_ryugaku = array(1, 2);
        $model->field["TANIPRINT_RYUGAKU"] = ($model->field["TANIPRINT_RYUGAKU"] == "") ? $model->Properties["tyousasyoTaniPrint"] : $model->field["TANIPRINT_RYUGAKU"];
        $extra = array("id=\"TANIPRINT_RYUGAKU1\"", "id=\"TANIPRINT_RYUGAKU2\"");
        $radioArray = knjCreateRadio($objForm, "TANIPRINT_RYUGAKU", $model->field["TANIPRINT_RYUGAKU"], $extra, $opt_ryugaku, get_count($opt_ryugaku));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ページ選択チェックボックス
        $shojikouExtendsOthers = array();
        foreach (array("tyousasyo2020EshojikouExtends", "tyousasyo2020GshojikouExtends") as $key => $propName) {
            $val = $model->Properties[$propName];
            if (in_array($val, array("2", "2_2"))) {
                $shojikouExtendsOthers[$propName] = $val;
            } elseif (in_array($val, array("2page!", "2page"))) {
                $shojikouExtendsOthers[$propName] = $val;
            }
        }

        if (count($shojikouExtendsOthers)) {
            $arg["show_pageselect"] = "1";
            $shojikouExtendsSelect = array();
            $ext = $model->Properties["tyousasyo2020shojikouExtends"];
            if (in_array($ext, array("2", "2_2"))) {
                $shojikouExtendsSelect["tyousasyo2020shojikouExtends"] = $ext;
            } elseif (in_array($ext, array("2page!", "2page"))) {
                $shojikouExtendsSelect["tyousasyo2020shojikouExtends"] = $ext;
            } else {
                $shojikouExtendsSelect["tyousasyo2020shojikouExtends"] = "0";
            }
            $shojikouExtendsSelect = array_merge($shojikouExtendsSelect, $shojikouExtendsOthers);

            $opt_page = array();
            $extra = array();
            $replace_vals = array();
            $i = 1;
            foreach ($shojikouExtendsSelect as $name => $val) {
                $opt_page[] = $i;
                $extra[] = "id=\"tyousasyo2020shojikouExtends{$i}\"";
                $replace_vals["tyousasyo2020shojikouExtends{$i}"] = $val;
                $i++;
            }

            $model->field["tyousasyo2020shojikouExtends"] = ($model->field["tyousasyo2020shojikouExtends"] == "") ? "0": $model->field["tyousasyo2020shojikouExtends"];
            $radioArray = knjCreateRadio($objForm, "tyousasyo2020shojikouExtends", $model->field["tyousasyo2020shojikouExtends"], $extra, $opt_page, get_count($opt_page));
            $pageSelect = "";
            foreach ($radioArray as $key => $val) {
                if ($pageSelect) {
                    $pageSelect .= "　 　";
                }
                if (preg_match("/ checked/", $val)) {
                    $val = preg_replace("/ checked/", "", $val);
                }

                $checked = $replace_vals[$key] == $model->field["tyousasyo2020shojikouExtends"] ? " checked " : "";
                $val = preg_replace("/'\d+'/", "'".$replace_vals[$key]."'".$checked, $val); // radioの値を1,2,3から変更する
                $label = "";
                if (in_array($replace_vals[$key], array("0"))) {
                    $label = "4ページ";
                } elseif (in_array($replace_vals[$key], array("2", "2_2"))) {
                    $label = "3ページ";
                } elseif (in_array($replace_vals[$key], array("2page!", "2page"))) {
                    $label = "2ページ";
                }
                $pageSelect .= $val."<LABEL for='".$key."'>".$label."</LABEL>";
            }
            $arg["data"]["tyousasyo2020shojikouExtends"] = $pageSelect;
        }

        //フォーム選択チェックボックス
        if ($model->Properties["tyousasyoNoSelectNenYoForm"] != '1') {
            $arg["no_tyousasyoNoSelectNenYoForm"] = "1";
            $extra  = ($model->field["FORM6"] == "on") ? "checked" : "";
            $extra .= " id=\"FORM6\"";
            $arg["data"]["FORM6"] = knjCreateCheckBox($objForm, "FORM6", "on", $extra, "");
            // ○年用フォーム
            $arg["data"]["FORM6NAME"] = "{$model->Properties["nenYoForm"]}年用フォーム";
        }

        //指導上参考となる諸事欄、３分割フォーム
        knjCreateHidden($objForm, "useSyojikou3", "1");
//        if ($model->Properties["tyousasyoNoSelectUseSyojikou3"] == '1') {
//            knjCreateHidden($objForm, "useSyojikou3", "1");
//        } else {
//            $arg["no_tyousasyoNoSelectUseSyojikou3"] = "1";
//            $extra  = ($model->field["useSyojikou3"] == "1" || ($model->cmd == 'edit' && $model->Properties["useSyojikou3"] == '1')) ? "checked " : "";
//            $extra .= " id=\"useSyojikou3\"";
//            $arg["data"]["useSyojikou3"] = knjCreateCheckBox($objForm, "useSyojikou3", "1", $extra, "");
//        }
        if ($arg["no_tyousasyoNoSelectNenYoForm"] || $arg["no_tyousasyoNoSelectUseSyojikou3"]) {
            $arg["form_select"] = "1";
        }

        //何年用のフォームを使うのかの初期値を判断する
        $query = knji060dQuery::getSchoolDiv();
        $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($schooldiv["SCHOOLDIV"] == '0') {
            if ($schooldiv["NEN"] == '0') {
                knjCreateHidden($objForm, "NENYOFORM_SYOKITI", '3');
            } else {
                knjCreateHidden($objForm, "NENYOFORM_SYOKITI", $schooldiv["NEN"]);
            }
        } else {
            if ($schooldiv["NEN"] == '0') {
                knjCreateHidden($objForm, "NENYOFORM_SYOKITI", '4');
            } else {
                knjCreateHidden($objForm, "NENYOFORM_SYOKITI", $schooldiv["NEN"]);
            }
        }
        knjCreateHidden($objForm, "NENYOFORM_CHECK", $model->Properties["nenYoForm"]);
        knjCreateHidden($objForm, "NENYOFORM");

        $arr = preg_split("/\s*,\s*/", $model->Properties["tyousasho2020PrintHeaderName"]);
        if (in_array("check", $arr)) {
            $arg["showtyousasho2020PrintHeaderNameRadioButton"] = "1";
            //ラジオボタンを作成
            $opt = array(1, 2);
            if (!$model->field["KNJE070D_PRINTHEADERNAME"]) {
                if (in_array("on", $arr)) {
                    $model->field["KNJE070D_PRINTHEADERNAME"] = "1";
                } else {
                    $model->field["KNJE070D_PRINTHEADERNAME"] = "2";
                }
            }
            $extra = array("id=\"KNJE070D_PRINTHEADERNAME1\" ", "id=\"KNJE070D_PRINTHEADERNAME2\" ");
            $radioArray = knjCreateRadio($objForm, "KNJE070D_PRINTHEADERNAME", $model->field["KNJE070D_PRINTHEADERNAME"], $extra, $opt, count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //校長印刷ラジオボタン 1:する 2:しない
        $opt_kotyo = array(1, 2);
        $model->field["OUTPUT_PRINCIPAL"] = ($model->field["OUTPUT_PRINCIPAL"] == "") ? "1" : $model->field["OUTPUT_PRINCIPAL"];
        $extra = array("id=\"OUTPUT_PRINCIPAL1\"", "id=\"OUTPUT_PRINCIPAL2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_PRINCIPAL", $model->field["OUTPUT_PRINCIPAL"], $extra, $opt_kotyo, get_count($opt_kotyo));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //校長印
        $arr = preg_split("/\s*,\s*/", $model->Properties["tyousashoPrintStampSelect"]);
        $tyousashoPrintStampSelect = "";
        if (in_array("1", $arr)) {
            $tyousashoPrintStampSelect = "1";
        } elseif (in_array("2", $arr)) {
            $tyousashoPrintStampSelect = "2";
        }
        if (in_array($tyousashoPrintStampSelect, array("1", "2"))) {
            //校長印出力ラジオボタン 1:する 2:しない
            $arg["stamp1"] = "1";
            $opt = array(1, 2);
            if (!$model->field["KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"]) {
                if ($model->cmd == "" && in_array("on", $arr)) {
                    $model->field["KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"] = "1";
                } else {
                    $model->field["KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"] = "2";
                }
            }
            $extra = array("id=\"KNJE070_CHECK_PRINT_STAMP_PRINCIPAL1\" " , "id=\"KNJE070_CHECK_PRINT_STAMP_PRINCIPAL2\" ");
            $radioArray = knjCreateRadio($objForm, "KNJE070_CHECK_PRINT_STAMP_PRINCIPAL", $model->field["KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"], $extra, $opt, count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }
        //記載者印
        if (in_array($tyousashoPrintStampSelect, array("1"))) {
            $arg["stamp2"] = "1";
            $opt = array(1, 2);
            if (!$model->field["KNJE070_CHECK_PRINT_STAMP_HR_STAFF"]) {
                if ($model->cmd == "" && in_array("on", $arr)) {
                    $model->field["KNJE070_CHECK_PRINT_STAMP_HR_STAFF"] = "1";
                } else {
                    $model->field["KNJE070_CHECK_PRINT_STAMP_HR_STAFF"] = "2";
                }
            }
            $extra = array("id=\"KNJE070_CHECK_PRINT_STAMP_HR_STAFF1\" " , "id=\"KNJE070_CHECK_PRINT_STAMP_HR_STAFF2\" ");
            $radioArray = knjCreateRadio($objForm, "KNJE070_CHECK_PRINT_STAMP_HR_STAFF", $model->field["KNJE070_CHECK_PRINT_STAMP_HR_STAFF"], $extra, $opt, count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
            if ($model->Properties["tyousasyoPrintHomeRoomStaff"] == "1") {
                $arg["data"]["KISAI_STAMP_TITLE"] = "担任";
            } else {
                $arg["data"]["KISAI_STAMP_TITLE"] = "記載責任者";
            }
        }

        //記載日付
        if (!isset($model->field["DATE"]) && $model->Properties["tyousasyoCheckCertifDate"] != '1') {
            $model->field["DATE"] = str_replace('-', '/', $model->control["学籍処理日"]);
        }
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //記載責任者コンボボックス
        $query = knji060dQuery::getSelectStaff(CTRL_YEAR);
        makeCmbSeki($objForm, $arg, $db, $query, "SEKI", $model->field["SEKI"], "", 1, "blank", $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knji060dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmbSeki(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $model)
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
    list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
    $result = $db->query($query);
    if ($model->Properties["tyousasyoPrintHomeRoomStaff"] != "1") {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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

            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
    }
    $result->free();
    if ($model->Properties["tyousasyoPrintHomeRoomStaff"] == "1") {
        $value = "";
    } else {
        $value = ($value && $value_flg) ? $value : STAFFCD;
    }


    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJI060D");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO");
    knjCreateHidden($objForm, "G_YEAR");
    knjCreateHidden($objForm, "G_SEMESTER");
    knjCreateHidden($objForm, "G_GRADE");
    knjCreateHidden($objForm, "OS", "1");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "tyousasyoAttendrecRemarkFieldSize", $model->tyousasyoAttendrecRemarkFieldSize);

    knjCreateHidden($objForm, "gaihyouGakkaBetu", $model->Properties["gaihyouGakkaBetu"]);
    knjCreateHidden($objForm, "3_or_6_nenYoForm", $model->Properties["3_or_6_nenYoForm"]);
    knjCreateHidden($objForm, "train_ref_1_2_3_field_size", $model->Properties["train_ref_1_2_3_field_size"]);
    knjCreateHidden($objForm, "train_ref_1_2_3_gyo_size", $model->Properties["train_ref_1_2_3_gyo_size"]);
    knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentani", $model->Properties["tyousasyoSougouHyoukaNentani"]);

    knjCreateHidden($objForm, "tyousasyoTotalstudyactFieldSize", $model->Properties["tyousasyoTotalstudyactFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoTotalstudyvalFieldSize", $model->Properties["tyousasyoTotalstudyvalFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoSpecialactrecFieldSize", $model->Properties["tyousasyoSpecialactrecFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoTokuBetuFieldSize", $model->Properties["tyousasyoTokuBetuFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoEMPTokuBetuFieldSize", $model->Properties["tyousasyoEMPTokuBetuFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoKinsokuForm", $model->Properties["tyousasyoKinsokuForm"]);
    knjCreateHidden($objForm, "tyousasyoNotPrintAnotherStudyrec", $model->Properties["tyousasyoNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "tyousasyoNotPrintEnterGrade", $model->Properties["tyousasyoNotPrintEnterGrade"]);
    knjCreateHidden($objForm, "tyousasyoSyusyokuPrintGappeiTougou", $model->Properties["tyousasyoSyusyokuPrintGappeiTougou"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "TANIPRINT_SOUGOU", $model->Properties["tyousasyoTaniPrint"]);
    knjCreateHidden($objForm, "TANIPRINT_RYUGAKU", $model->Properties["tyousasyoTaniPrint"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
    knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
    knjCreateHidden($objForm, "useAssessCourseMst", $model->Properties["useAssessCourseMst"]);
    knjCreateHidden($objForm, "useMaruA_avg", $model->Properties["useMaruA_avg"]);
    knjCreateHidden($objForm, "tyousasyoRemarkFieldSize", $model->Properties["tyousasyoRemarkFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoUseEditKinsoku", $model->Properties["tyousasyoUseEditKinsoku"]);
    knjCreateHidden($objForm, "certifPrintRealName", $model->Properties["certifPrintRealName"]);
    knjCreateHidden($objForm, "tyousasyoHankiNintei", $model->Properties["tyousasyoHankiNintei"]);
    knjCreateHidden($objForm, "useTotalstudySlashFlg", $model->Properties["useTotalstudySlashFlg"]);
    knjCreateHidden($objForm, "useAttendrecRemarkSlashFlg", $model->Properties["useAttendrecRemarkSlashFlg"]);
    knjCreateHidden($objForm, "tyousasyoCheckCertifDate", $model->Properties["tyousasyoCheckCertifDate"]);
    knjCreateHidden($objForm, "tyousasyoPrintHomeRoomStaff", $model->Properties["tyousasyoPrintHomeRoomStaff"]);
    knjCreateHidden($objForm, "tyousasyoPrintCoursecodename", $model->Properties["tyousasyoPrintCoursecodename"]);
    knjCreateHidden($objForm, "tyousasyoPrintChairSubclassSemester2", $model->Properties["tyousasyoPrintChairSubclassSemester2"]);
    knjCreateHidden($objForm, "tyousasyoHanasuClasscd", $model->Properties["tyousasyoHanasuClasscd"]);
    knjCreateHidden($objForm, "tyousasyoJiritsuKatsudouRemark", $model->Properties["tyousasyoJiritsuKatsudouRemark"]);
    knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentaniPrintCombined", $model->Properties["tyousasyoSougouHyoukaNentaniPrintCombined"]);
    knjCreateHidden($objForm, "tyousasyo2020", "1");
    knjCreateHidden($objForm, "hyoteiYomikaeRadio", $model->Properties["hyoteiYomikaeRadio"]);
    knjCreateHidden($objForm, "tyousasho2020PrintHeaderName", $model->Properties["tyousasho2020PrintHeaderName"]);
}
