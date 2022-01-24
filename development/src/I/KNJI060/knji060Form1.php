<?php

require_once('for_php7.php');

class knji060Form1
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
        $arg["start"] = $objForm->get_start("knji060Form1", "POST", "knji060index.php", "", "knji060Form1");

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

        //評定読替の項目を表示するかしないかのフラグ
        if (in_array($model->Properties["hyoteiYomikae"], array("1", "1_off"))) {
            $arg["hyoteiYomikae"] = '1';

            //評定読替チェックボックス
            if ((!$model->cmd || $model->cmd == "edit") && $model->Properties["hyoteiYomikae"] == "1") {
                $model->field["HYOTEI"] = "on";
            }
            $extra  = $model->field["HYOTEI"] == "on" ? "checked" : "";
            $extra .= " id=\"HYOTEI\"";
            $arg["data"]["HYOTEI"] = knjCreateCheckBox($objForm, "HYOTEI", "on", $extra, "");
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
        $kindai_flg = $db->getOne(knji060Query::checkKindai());
        if (!$kindai_flg) {
            $arg["notKindai"] = '1';
        }

        //未履修科目出力ラジオボタン 1:する 2:しない
        $opt = array(1, 2);
        $model->field["MIRISYU"] = ($model->field["MIRISYU"] == "") ? "2" : $model->field["MIRISYU"];
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
        if ($model->Properties["tyousasyoNoSelectUseSyojikou3"] == '1') {
            knjCreateHidden($objForm, "useSyojikou3", "1");
        } else {
            $arg["no_tyousasyoNoSelectUseSyojikou3"] = "1";
            $extra  = ($model->field["useSyojikou3"] == "1" || ($model->cmd == 'edit' && $model->Properties["useSyojikou3"] == '1')) ? "checked " : "";
            $extra .= " id=\"useSyojikou3\"";
            $arg["data"]["useSyojikou3"] = knjCreateCheckBox($objForm, "useSyojikou3", "1", $extra, "");
        }
        if ($arg["no_tyousasyoNoSelectNenYoForm"] || $arg["no_tyousasyoNoSelectUseSyojikou3"]) {
            $arg["form_select"] = "1";
        }

        //何年用のフォームを使うのかの初期値を判断する
        $query = knji060Query::getSchoolDiv();
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

        //記載日付
        if (!isset($model->field["DATE"]) && $model->Properties["tyousasyoCheckCertifDate"] != '1') {
            $model->field["DATE"] = $model->control["学籍処理日"];
        }
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //記載責任者コンボボックス
        $query = knji060Query::getSelectStaff(CTRL_YEAR);
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
        View::toHTML($model, "knji060Form1.html", $arg);
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
    knjCreateHidden($objForm, "PRGID", "KNJI060");
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
}
