<?php

require_once('for_php7.php');

class knjf020jSubForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjf020jindex.php", "", "sel");
        $arg["jscript"] = "";

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0] == "") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        $RowH = knjf020jQuery::getMedexamHdat($model);      //生徒健康診断ヘッダデータ取得
        $RowT = knjf020jQuery::getMedexamToothDat($model); //生徒健康診断歯口腔データ取得

        $db = Query::dbCheckOut();

        $result   = $db->query(knjf020jQuery::GetStudent($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["SCHREGNO"], $array)) {
                $opt_right[]  = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            } else {
                $opt_left[]   = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            }
        }
        $result->free();

        //レイアウト調整用
        $arg["data"]["SPAN1"] = 2;
        $arg["data"]["SPAN2"] = 1;

        $arg["data"]["SPAN3"] = 4;

        /* 編集項目 */
        //チェックボックス
        for ($i = 0; $i < 25; $i++) {
            if ($i == 23) {
                $extra = "onClick=\"return check_all(this);\"";
                if ($model->replace_data["check"][8] == "1") {
                    $extra .= "checked='checked'";
                }
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);
            } else {
                $extra = "";
                if ($model->replace_data["check"][$i] == "1") {
                    $extra .= "checked='checked'";
                }
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);
            }
        }
        //健康診断実施日付
        if ($RowH["TOOTH_DATE"] == "") {
            $RowH["TOOTH_DATE"] = CTRL_DATE;
        }
        $RowH["TOOTH_DATE"]        = str_replace("-", "/", $RowH["TOOTH_DATE"]);
        $arg["data"]["TOOTH_DATE"] = View::popUpCalendar($objForm, "TOOTH_DATE", $RowH["TOOTH_DATE"]);

        //歯列・咬合コンボボックス
        $query = knjf020jQuery::getNameMst($model, "F510");
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD"], "JAWS_JOINTCD", $extra, 1, "BLANK");

        $arg["data"]["JAWS_JOINTCD_LABEL"] = '歯列・咬合';

        //咬合コンボボックス
        $query = knjf020jQuery::getNameMst($model, "F512");
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD3"], "JAWS_JOINTCD3", $extra, 1, "BLANK");

        //顎関節コンボボックス
        $query = knjf020jQuery::getNameMst($model, "F511");
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD2"], "JAWS_JOINTCD2", $extra, 1, "BLANK");

        //歯垢の状態コンボ
        $query = knjf020jQuery::getNameMst($model, "F520");
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["PLAQUECD"], "PLAQUECD", $extra, 1, "BLANK");

        //歯肉の状態コンボ
        $query = knjf020jQuery::getNameMst($model, "F513");
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["GUMCD"], "GUMCD", $extra, 1, "BLANK");

        //歯肉の状態コンボ
        $query = knjf020jQuery::getNameMst($model, "F521");
        $extra = "style=\"width:250px;\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["CALCULUS"], "CALCULUS", $extra, 1, "BLANK");

        //矯正
        if ($RowT["ORTHODONTICS"] == 1) {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi');\" checked='checked'";
            $ari_nasi = "<span id='ari_nasi' style='color:black;'>有</span>";
        } else {
            $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi');\"";
            $ari_nasi = "<span id='ari_nasi' style='color:black;'>無</span>";
        }
        $arg["data"]["ORTHODONTICS"] = knjCreateCheckBox($objForm, "ORTHODONTICS", 1, $extra).$ari_nasi;

        //その他疾病及び異常コンボ
        $disableCodes = getDisableCodes($db, $model, "F530");
        $query = knjf020jQuery::getNameMst($model, "F530");
        $extra = "style=\"width:250px;\" onclick=\"OptionUse(this, document.forms[0].OTHERDISEASE, '".implode(",", $disableCodes)."');\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD"], "OTHERDISEASECD", $extra, 1, "BLANK");

        //その他疾病及び異常テキスト
        if (in_array($RowT["OTHERDISEASECD"], $disableCodes) == true || $RowT["OTHERDISEASECD"] == '') {
            $extra = "disabled style=\"background-color:darkgray\"";
        } else {
            $extra = "";
        }

        $arg["data"]["OTHERDISEASE"] = knjCreateTextBox($objForm, $RowT["OTHERDISEASE"], "OTHERDISEASE", 40, 60, $extra);

        //口腔の疾病及び異常コンボ
        $disableCodes = getDisableCodes($db, $model, "F531");
        $query = knjf020jQuery::getNameMst($model, "F531");
        $extra = "style=\"width:250px;\" onclick=\"OptionUse2(this, document.forms[0].OTHERDISEASE2, '".implode(",", $disableCodes)."');\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD2"], "OTHERDISEASECD2", $extra, 1, "BLANK");

        //口腔の疾病及び異常テキスト
        if (in_array($RowT["OTHERDISEASECD2"], $disableCodes) == true || $RowT["OTHERDISEASECD2"] == '') {
            $extra = "disabled style=\"background-color:darkgray\"";
        } else {
            $extra = "";
        }

        $arg["data"]["OTHERDISEASE2"] = knjCreateTextBox($objForm, $RowT["OTHERDISEASE2"], "OTHERDISEASE2", 40, 60, $extra);

        //所見１コンボボックス
        $disableCodes = getDisableCodes($db, $model, "F540");
        $query = knjf020jQuery::getNameMst($model, "F540");
        $extra = "style=\"width:250px;\" onChange=\"OptionUse3('".implode(",", $disableCodes)."')\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARKCD"], "DENTISTREMARKCD", $extra, 1, "BLANK");

        //所見１テキスト
        if (in_array($RowT["DENTISTREMARKCD"], $disableCodes) == true || $RowT["DENTISTREMARKCD"] == '') {
            $extra = "disabled style=\"background-color:darkgray\"";
        } else {
            $extra = "";
        }
        $arg["data"]["DENTISTREMARK"] = knjCreateTextBox($objForm, $RowT["DENTISTREMARK"], "DENTISTREMARK", 40, 60, $extra);

        //所見２コンボボックス
        $disableCodes = getDisableCodes($db, $model, "F540");
        $query = knjf020jQuery::getNameMst($model, "F540");
        if ($RowT["DENTISTREMARKCD"] == '' || $RowT["DENTISTREMARKCD"] == '00') {
            //未選択時と「00:未受験時」はコンボ欄無効
            $extra = "style=\"width:150px; background-color:darkgray\" disabled ";
        } else {
            $extra = "style=\"width:150px;\" ";
        }
        $extra .= "onChange=\"OptionUse3('".implode(",", $disableCodes)."')\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARKCD2"], "DENTISTREMARKCD2", $extra, 1, "BLANK");

        //所見２テキスト
        if (in_array($RowT["DENTISTREMARKCD2"], $disableCodes) == true || $RowT["DENTISTREMARKCD2"] == '') {
            $extra = "disabled style=\"background-color:darkgray\"";
        } else {
            $extra = "";
        }
        $arg["data"]["DENTISTREMARK2"] = knjCreateTextBox($objForm, $RowT["DENTISTREMARK2"], "DENTISTREMARK2", 40, 60, $extra);

        //所見３コンボボックス
        $disableCodes = getDisableCodes($db, $model, "F540");
        $query = knjf020jQuery::getNameMst($model, "F540");
        if ($RowT["DENTISTREMARKCD2"] == '' || $RowT["DENTISTREMARKCD2"] == '00') {
            //未選択時と「00:未受験時」はコンボ欄無効
            $extra = "style=\"width:150px; background-color:darkgray\" disabled ";
        } else {
            $extra = "style=\"width:150px;\" ";
        }
        $extra .= "onChange=\"OptionUse3('".implode(",", $disableCodes)."')\"";
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARKCD3"], "DENTISTREMARKCD3", $extra, 1, "BLANK");

        //所見３テキスト
        if (in_array($RowT["DENTISTREMARKCD3"], $disableCodes) == true || $RowT["DENTISTREMARKCD3"] == '') {
            $extra = "disabled style=\"background-color:darkgray\"";
        } else {
            $extra = "";
        }
        $arg["data"]["DENTISTREMARK3"] = knjCreateTextBox($objForm, $RowT["DENTISTREMARK3"], "DENTISTREMARK3", 40, 60, $extra);

        //所見日付
        $RowT["DENTISTREMARKDATE"] = str_replace("-", "/", $RowT["DENTISTREMARKDATE"]);
        $arg["data"]["DENTISTREMARKDATE"] = View::popUpCalendar($objForm, "DENTISTREMARKDATE", $RowT["DENTISTREMARKDATE"]);

        //事後措置１コンボ
        $disableCodes = getDisableCodes($db, $model, "F541");
        $query = knjf020jQuery::getNameMst($model, "F541");
        $extra = "style=\"width:250px;\" onChange=\"OptionUse4('".implode(",", $disableCodes)."')\"".$entMove["DENTISTTREATCD"];
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTTREATCD"], "DENTISTTREATCD", $extra, 1, "BLANK");

        //事後措置１テキスト
        $extra = $entMove["DENTISTTREAT"];
        if (in_array($RowT["DENTISTTREATCD"], $disableCodes) == true || $RowT["DENTISTTREATCD"] == '') {
            $extra .= " disabled style=\"background-color:darkgray\"";
        }
        $arg["data"]["DENTISTTREAT"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT"], "DENTISTTREAT", 40, 60, $extra);

        //事後措置２コンボ
        $disableCodes = getDisableCodes($db, $model, "F541");
        $query = knjf020jQuery::getNameMst($model, "F541");
        if ($RowT["DENTISTTREATCD"] == '' || $RowT["DENTISTTREATCD"] == '01') {
            //未選択時と「01:なし」はコンボ欄無効
            $extra = "style=\"width:250px; background-color:darkgray\" disabled ";
        } else {
            $extra = "style=\"width:250px;\" ";
        }
        $extra .= "onChange=\"OptionUse4('".implode(",", $disableCodes)."')\"".$entMove["DENTISTTREATCD2"];
        makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTTREATCD2"], "DENTISTTREATCD2", $extra, 1, "BLANK");

        //事後措置２テキスト
        $extra = $entMove["DENTISTTREAT2_1"];
        if (in_array($RowT["DENTISTTREATCD2"], $disableCodes) == true || $RowT["DENTISTTREATCD2"] == '') {
            $extra .= " disabled style=\"background-color:darkgray\"";
        }
        $arg["data"]["DENTISTTREAT2_1"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT2_1"], "DENTISTTREAT2_1", 40, 60, $extra);

        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, 20);

        //生徒一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, 20);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $RowH);

        /* ヘッダ */
        $arg["info"] = array("TOP"        => $model->year."年度  "
                                             .$model->control_data["学期名"][$model->semester]
                                             ."  対象クラス  ".$model->Hrname,
                             "LEFT_LIST"  => "対象者一覧",
                             "RIGHT_LIST" => $model->sch_label."一覧"
        );

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf020jSubForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新・戻るボタン
    $extra = "onclick=\"return doSubmit()\"";
    //更新
    $arg["BUTTONS"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $link = REQUESTROOT."/F/KNJF020J/knjf020jindex.php?cmd=back&ini2=1";
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["BUTTONS"] .= "    ".knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    //全て追加
    $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //追加
    $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //削除
    $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //全て削除
    $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $RowH)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "REPLACEHIDDENDATE", $RowH["TOOTH_DATE"]);
}

//所見欄を無効にするcdの文字列
function getDisableCodes($db, $model, $setInNamecd1)
{
    $query = knjf020jQuery::getNameMstDisableCodes($model, $setInNamecd1);
    $disableCodes = array();
    $db = Query::dbCheckOut();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $disableCodes[] .= $row["NAMECD2"];
    }
    $result->free();

    return $disableCodes;
}
