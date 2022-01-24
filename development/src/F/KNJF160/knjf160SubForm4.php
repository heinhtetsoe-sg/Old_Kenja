<?php

require_once('for_php7.php');

class knjf160SubForm4
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4", "POST", "knjf160index.php", "", "subform4");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjf160Query::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)){
            $Row = $db->getRow(knjf160Query::getInvestOther($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //extra
        $extra_alphint = "onBlur=\"this.value=toAlphaNumber(this.value);\"";
        $extra_int = "STYLE=\"text-align: right\" onBlur=\"this.value=toInteger(this.value);\"";
        $extra_year = "STYLE=\"text-align: right\" onBlur=\"this.value=toInteger(this.value); YearCheck(this);\"";
        $extra_month = "STYLE=\"text-align: right\" onBlur=\"this.value=toInteger(this.value); MonthCheck(this);\"";
        $extra_opt = " onclick=\"OptionUse('this');\"";

        //薬アレルギー
        $arg["data"]["ALLERGY_MEDICINE"] = knjCreateTextBox($objForm, $Row["ALLERGY_MEDICINE"], "ALLERGY_MEDICINE", 80, 80, "");

        //食品アレルギー
        $arg["data"]["ALLERGY_FOOD"] = knjCreateTextBox($objForm, $Row["ALLERGY_FOOD"], "ALLERGY_FOOD", 80, 80, "");

        //その他アレルギー
        $arg["data"]["ALLERGY_OTHER"] = knjCreateTextBox($objForm, $Row["ALLERGY_OTHER"], "ALLERGY_OTHER", 80, 80, "");

        //血液型
        $opt_blood[] = array('label' => "", 'value' => "");
        $opt_blood[] = array('label' => "Ａ型", 'value' => "A");
        $opt_blood[] = array('label' => "Ｂ型", 'value' => "B");
        $opt_blood[] = array('label' => "Ｏ型", 'value' => "O");
        $opt_blood[] = array('label' => "ＡＢ型", 'value' => "AB");
        $arg["data"]["BLOOD"] = knjCreateCombo($objForm, "BLOOD", $Row["BLOOD"], $opt_blood, "", 1);

        //Rh
        $opt_rh[] = array('label' => "", 'value' => "");
        $opt_rh[] = array('label' => "＋", 'value' => "+");
        $opt_rh[] = array('label' => "－", 'value' => "-");
        $arg["data"]["RH"] = knjCreateCombo($objForm, "RH", $Row["RH"], $opt_rh, "", 1);

        //麻疹（年齢）
        $arg["data"]["MEASLES_AGE"] = knjCreateTextBox($objForm, $Row["MEASLES_AGE"], "MEASLES_AGE", 2, 2, $extra_int);

        //風疹（年齢）
        $arg["data"]["G_MEASLES_AGE"] = knjCreateTextBox($objForm, $Row["G_MEASLES_AGE"], "G_MEASLES_AGE", 2, 2, $extra_int);

        //水痘（年齢）
        $arg["data"]["VARICELLA_AGE"] = knjCreateTextBox($objForm, $Row["VARICELLA_AGE"], "VARICELLA_AGE", 2, 2, $extra_int);

        //中耳炎（年齢）
        $arg["data"]["OTITIS_MEDIA_AGE"] = knjCreateTextBox($objForm, $Row["OTITIS_MEDIA_AGE"], "OTITIS_MEDIA_AGE", 2, 2, $extra_int);

        //結核（年齢）
        $arg["data"]["TB_AGE"] = knjCreateTextBox($objForm, $Row["TB_AGE"], "TB_AGE", 2, 2, $extra_int);

        //川崎病（年齢）
        $arg["data"]["KAWASAKI_AGE"] = knjCreateTextBox($objForm, $Row["KAWASAKI_AGE"], "KAWASAKI_AGE", 2, 2, $extra_int);

        //溶連菌感染症（年齢）
        $arg["data"]["INFECTION_AGE"] = knjCreateTextBox($objForm, $Row["INFECTION_AGE"], "INFECTION_AGE", 2, 2, $extra_int);

        //流行性耳下腺炎（年齢）
        $arg["data"]["MUMPS_AGE"] = knjCreateTextBox($objForm, $Row["MUMPS_AGE"], "MUMPS_AGE", 2, 2, $extra_int);

        //心臓疾患（病名）
        $arg["data"]["HEART_DISEASE"] = knjCreateTextBox($objForm, $Row["HEART_DISEASE"], "HEART_DISEASE", 30, 30, "");
        //心臓疾患（年齢）
        $arg["data"]["HEART_S_AGE"] = knjCreateTextBox($objForm, $Row["HEART_S_AGE"], "HEART_S_AGE", 2, 2, $extra_int);
        //心臓疾患（経過）
        $query = knjf160Query::getNameMst2('F230');
        makeCmb($objForm, $arg, $db, $query, "HEART_SITUATION", $Row["HEART_SITUATION"], "", 1);
        //心臓疾患（全治年齢）
        $arg["data"]["HEART_E_AGE"] = knjCreateTextBox($objForm, $Row["HEART_E_AGE"], "HEART_E_AGE", 2, 2, $extra_int);

        //腎臓疾患（病名）
        $arg["data"]["KIDNEY_DISEASE"] = knjCreateTextBox($objForm, $Row["KIDNEY_DISEASE"], "KIDNEY_DISEASE", 30, 30, "");
        //腎臓疾患（年齢）
        $arg["data"]["KIDNEY_S_AGE"] = knjCreateTextBox($objForm, $Row["KIDNEY_S_AGE"], "KIDNEY_S_AGE", 2, 2, $extra_int);
        //腎臓疾患（経過）
        makeCmb($objForm, $arg, $db, $query, "KIDNEY_SITUATION", $Row["KIDNEY_SITUATION"], "", 1);
        //腎臓疾患（全治年齢）
        $arg["data"]["KIDNEY_E_AGE"] = knjCreateTextBox($objForm, $Row["KIDNEY_E_AGE"], "KIDNEY_E_AGE", 2, 2, $extra_int);

        //ぜんそく（年齢）
        $arg["data"]["ASTHMA_S_AGE"] = knjCreateTextBox($objForm, $Row["ASTHMA_S_AGE"], "ASTHMA_S_AGE", 2, 2, $extra_int);
        //ぜんそく（経過）
        makeCmb($objForm, $arg, $db, $query, "ASTHMA_SITUATION", $Row["ASTHMA_SITUATION"], "", 1);
        //ぜんそく（全治年齢）
        $arg["data"]["ASTHMA_E_AGE"] = knjCreateTextBox($objForm, $Row["ASTHMA_E_AGE"], "ASTHMA_E_AGE", 2, 2, $extra_int);

        //けいれん・ひきつけ（年齢）
        $arg["data"]["CONVULSIONS_S_AGE"] = knjCreateTextBox($objForm, $Row["CONVULSIONS_S_AGE"], "CONVULSIONS_S_AGE", 2, 2, $extra_int);
        //けいれん・ひきつけ（経過）
        makeCmb($objForm, $arg, $db, $query, "CONVULSIONS_SITUATION", $Row["CONVULSIONS_SITUATION"], "", 1);
        //けいれん・ひきつけ（全治年齢）
        $arg["data"]["CONVULSIONS_E_AGE"] = knjCreateTextBox($objForm, $Row["CONVULSIONS_E_AGE"], "CONVULSIONS_E_AGE", 2, 2, $extra_int);

        //その他（病名）
        $arg["data"]["OTHER_DISEASE"] = knjCreateTextBox($objForm, $Row["OTHER_DISEASE"], "OTHER_DISEASE", 80, 80, "");

        //ツベルクリン（接種状況）
        $query = knjf160Query::getNameMst2('F231');
        makeCmb($objForm, $arg, $db, $query, "TUBERCULIN", $Row["TUBERCULIN"], "", 1);
        //ツベルクリン（年）
        $arg["data"]["TUBERCULIN_YEAR"] = knjCreateTextBox($objForm, $Row["TUBERCULIN_YEAR"], "TUBERCULIN_YEAR", 4, 4, $extra_year);
        //ツベルクリン（月）
        $arg["data"]["TUBERCULIN_MONTH"] = knjCreateTextBox($objForm, $Row["TUBERCULIN_MONTH"], "TUBERCULIN_MONTH", 2, 2, $extra_month);
        //ツベルクリン（判定）
        $query = knjf160Query::getNameMst2('F232');
        makeCmb($objForm, $arg, $db, $query, "TUBERCULIN_JUDGE", $Row["TUBERCULIN_JUDGE"], "", 1);

        //風疹（接種状況）
        $query = knjf160Query::getNameMst2('F231');
        makeCmb($objForm, $arg, $db, $query, "G_MEASLES", $Row["G_MEASLES"], "", 1);
        //風疹（年）
        $arg["data"]["G_MEASLES_YEAR"] = knjCreateTextBox($objForm, $Row["G_MEASLES_YEAR"], "G_MEASLES_YEAR", 4, 4, $extra_year);
        //風疹（月）
        $arg["data"]["G_MEASLES_MONTH"] = knjCreateTextBox($objForm, $Row["G_MEASLES_MONTH"], "G_MEASLES_MONTH", 2, 2, $extra_month);

        //ＢＣＧ（接種状況）
        makeCmb($objForm, $arg, $db, $query, "BCG", $Row["BCG"], "", 1);
        //ＢＣＧ（年）
        $arg["data"]["BCG_YEAR"] = knjCreateTextBox($objForm, $Row["BCG_YEAR"], "BCG_YEAR", 4, 4, $extra_year);
        //ＢＣＧ（月）
        $arg["data"]["BCG_MONTH"] = knjCreateTextBox($objForm, $Row["BCG_MONTH"], "BCG_MONTH", 2, 2, $extra_month);

        //水痘（接種状況）
        makeCmb($objForm, $arg, $db, $query, "VARICELLA", $Row["VARICELLA"], "", 1);
        //水痘（年）
        $arg["data"]["VARICELLA_YEAR"] = knjCreateTextBox($objForm, $Row["VARICELLA_YEAR"], "VARICELLA_YEAR", 4, 4, $extra_year);
        //水痘（月）
        $arg["data"]["VARICELLA_MONTH"] = knjCreateTextBox($objForm, $Row["VARICELLA_MONTH"], "VARICELLA_MONTH", 2, 2, $extra_month);

        //ポリオ（接種状況）
        makeCmb($objForm, $arg, $db, $query, "POLIO", $Row["POLIO"], "", 1);
        //ポリオ（年）
        $arg["data"]["POLIO_YEAR"] = knjCreateTextBox($objForm, $Row["POLIO_YEAR"], "POLIO_YEAR", 4, 4, $extra_year);
        //ポリオ（月）
        $arg["data"]["POLIO_MONTH"] = knjCreateTextBox($objForm, $Row["POLIO_MONTH"], "POLIO_MONTH", 2, 2, $extra_month);

        //流行性耳下腺炎（接種状況）
        makeCmb($objForm, $arg, $db, $query, "MUMPS", $Row["MUMPS"], "", 1);
        //流行性耳下腺炎（年）
        $arg["data"]["MUMPS_YEAR"] = knjCreateTextBox($objForm, $Row["MUMPS_YEAR"], "MUMPS_YEAR", 4, 4, $extra_year);
        //流行性耳下腺炎（月）
        $arg["data"]["MUMPS_MONTH"] = knjCreateTextBox($objForm, $Row["MUMPS_MONTH"], "MUMPS_MONTH", 2, 2, $extra_month);

        //日本脳炎（接種状況）
        makeCmb($objForm, $arg, $db, $query, "ENCEPHALITIS", $Row["ENCEPHALITIS"], "", 1);
        //日本脳炎１（年月）
        $arg["data"]["ENCEPHALITIS_YEAR1"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_YEAR1"], "ENCEPHALITIS_YEAR1", 4, 4, $extra_year);
        $arg["data"]["ENCEPHALITIS_MONTH1"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_MONTH1"], "ENCEPHALITIS_MONTH1", 2, 2, $extra_month);
        //日本脳炎２（年月）
        $arg["data"]["ENCEPHALITIS_YEAR2"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_YEAR2"], "ENCEPHALITIS_YEAR2", 4, 4, $extra_year);
        $arg["data"]["ENCEPHALITIS_MONTH2"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_MONTH2"], "ENCEPHALITIS_MONTH2", 2, 2, $extra_month);
        //日本脳炎３（年月）
        $arg["data"]["ENCEPHALITIS_YEAR3"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_YEAR3"], "ENCEPHALITIS_YEAR3", 4, 4, $extra_year);
        $arg["data"]["ENCEPHALITIS_MONTH3"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_MONTH3"], "ENCEPHALITIS_MONTH3", 2, 2, $extra_month);
        //日本脳炎４（年月）
        $arg["data"]["ENCEPHALITIS_YEAR4"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_YEAR4"], "ENCEPHALITIS_YEAR4", 4, 4, $extra_year);
        $arg["data"]["ENCEPHALITIS_MONTH4"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_MONTH4"], "ENCEPHALITIS_MONTH4", 2, 2, $extra_month);
        //日本脳炎５（年月）
        $arg["data"]["ENCEPHALITIS_YEAR5"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_YEAR5"], "ENCEPHALITIS_YEAR5", 4, 4, $extra_year);
        $arg["data"]["ENCEPHALITIS_MONTH5"] = knjCreateTextBox($objForm, $Row["ENCEPHALITIS_MONTH5"], "ENCEPHALITIS_MONTH5", 2, 2, $extra_month);

        //二種混合（接種状況）
        makeCmb($objForm, $arg, $db, $query, "MIXED", $Row["MIXED"], "", 1);
        //二種混合１（年月）
        $arg["data"]["MIXED_YEAR1"] = knjCreateTextBox($objForm, $Row["MIXED_YEAR1"], "MIXED_YEAR1", 4, 4, $extra_year);
        $arg["data"]["MIXED_MONTH1"] = knjCreateTextBox($objForm, $Row["MIXED_MONTH1"], "MIXED_MONTH1", 2, 2, $extra_month);
        //二種混合２（年月）
        $arg["data"]["MIXED_YEAR2"] = knjCreateTextBox($objForm, $Row["MIXED_YEAR2"], "MIXED_YEAR2", 4, 4, $extra_year);
        $arg["data"]["MIXED_MONTH2"] = knjCreateTextBox($objForm, $Row["MIXED_MONTH2"], "MIXED_MONTH2", 2, 2, $extra_month);
        //二種混合３（年月）
        $arg["data"]["MIXED_YEAR3"] = knjCreateTextBox($objForm, $Row["MIXED_YEAR3"], "MIXED_YEAR3", 4, 4, $extra_year);
        $arg["data"]["MIXED_MONTH3"] = knjCreateTextBox($objForm, $Row["MIXED_MONTH3"], "MIXED_MONTH3", 2, 2, $extra_month);
        //二種混合４（年月）
        $arg["data"]["MIXED_YEAR4"] = knjCreateTextBox($objForm, $Row["MIXED_YEAR4"], "MIXED_YEAR4", 4, 4, $extra_year);
        $arg["data"]["MIXED_MONTH4"] = knjCreateTextBox($objForm, $Row["MIXED_MONTH4"], "MIXED_MONTH4", 2, 2, $extra_month);
        //二種混合５（年月）
        $arg["data"]["MIXED_YEAR5"] = knjCreateTextBox($objForm, $Row["MIXED_YEAR5"], "MIXED_YEAR5", 4, 4, $extra_year);
        $arg["data"]["MIXED_MONTH5"] = knjCreateTextBox($objForm, $Row["MIXED_MONTH5"], "MIXED_MONTH5", 2, 2, $extra_month);

        //麻疹（接種状況）
        $opt_mes[] = array('label' => "", 'value' => "");
        $opt_mes[] = array('label' => "ある", 'value' => "1");
        $opt_mes[] = array('label' => "ない", 'value' => "2");
        $arg["data"]["MEASLES"] = knjCreateCombo($objForm, "MEASLES", $Row["MEASLES"], $opt_mes, $extra_opt, 1);

        //disabled
        $disabled = ($Row["MEASLES"] == "1") ? "" : " disabled";

        //麻疹（回数）
        $query = knjf160Query::getNameMst2('F233');
        makeCmb($objForm, $arg, $db, $query, "MEASLES_TIMES", $Row["MEASLES_TIMES"], $disabled, 1);
        //麻疹１（年月）
        $arg["data"]["MEASLES_YEAR1"] = knjCreateTextBox($objForm, $Row["MEASLES_YEAR1"], "MEASLES_YEAR1", 4, 4, $extra_year.$disabled);
        $arg["data"]["MEASLES_MONTH1"] = knjCreateTextBox($objForm, $Row["MEASLES_MONTH1"], "MEASLES_MONTH1", 2, 2, $extra_month.$disabled);
        //麻疹２（年月）
        $arg["data"]["MEASLES_YEAR2"] = knjCreateTextBox($objForm, $Row["MEASLES_YEAR2"], "MEASLES_YEAR2", 4, 4, $extra_year.$disabled);
        $arg["data"]["MEASLES_MONTH2"] = knjCreateTextBox($objForm, $Row["MEASLES_MONTH2"], "MEASLES_MONTH2", 2, 2, $extra_month.$disabled);
        //麻疹３（年月）
        $arg["data"]["MEASLES_YEAR3"] = knjCreateTextBox($objForm, $Row["MEASLES_YEAR3"], "MEASLES_YEAR3", 4, 4, $extra_year.$disabled);
        $arg["data"]["MEASLES_MONTH3"] = knjCreateTextBox($objForm, $Row["MEASLES_MONTH3"], "MEASLES_MONTH3", 2, 2, $extra_month.$disabled);
        //麻疹（ワクチン）
        $query = knjf160Query::getNameMst2('F234');
        makeCmb($objForm, $arg, $db, $query, "VACCINE", $Row["VACCINE"], $disabled, 1);
        //麻疹（ロット番号）
        $arg["data"]["LOT_NO"] = knjCreateTextBox($objForm, $Row["LOT_NO"], "LOT_NO", 10, 10, $extra_alphint.$disabled);
        //麻疹（確認方法）
        $disabled = ($Row["MEASLES"] == "1" || $Row["MEASLES"] == "2") ? "" : " disabled";
        $query = knjf160Query::getNameMst2('F235');
        makeCmb($objForm, $arg, $db, $query, "CONFIRMATION", $Row["CONFIRMATION"], $disabled, 1);

        //麻疹（罹患歴）
        $opt_ames[] = array('label' => "", 'value' => "");
        $opt_ames[] = array('label' => "ある", 'value' => "1");
        $opt_ames[] = array('label' => "ない", 'value' => "2");
        $arg["data"]["A_MEASLES"] = knjCreateCombo($objForm, "A_MEASLES", $Row["A_MEASLES"], $opt_ames, $extra_opt, 1);

        //disabled
        $disabled = ($Row["A_MEASLES"] == "1") ? "" : " disabled";

        //麻疹（罹患歴・年齢）
        $arg["data"]["A_MEASLES_AGE"] = knjCreateTextBox($objForm, $Row["A_MEASLES_AGE"], "A_MEASLES_AGE", 2, 2, $extra_int.$disabled);
        //麻疹（罹患歴・確認方法）
        $disabled = ($Row["A_MEASLES"] == "1" || $Row["A_MEASLES"] == "2") ? "" : " disabled";
        makeCmb($objForm, $arg, $db, $query, "A_CONFIRMATION", $Row["A_CONFIRMATION"], $disabled, 1);

        //抗体検査履歴
        $opt_anti[] = array('label' => "", 'value' => "");
        $opt_anti[] = array('label' => "ある", 'value' => "1");
        $opt_anti[] = array('label' => "ない", 'value' => "2");
        $arg["data"]["ANTIBODY"] = knjCreateCombo($objForm, "ANTIBODY", $Row["ANTIBODY"], $opt_anti, $extra_opt, 1);

        //disabled
        $disabled = ($Row["ANTIBODY"] == "1") ? "" : " disabled";

        //検査日
        $arg["data"]["ANTIBODY_YEAR"] = knjCreateTextBox($objForm, $Row["ANTIBODY_YEAR"], "ANTIBODY_YEAR", 4, 4, $extra_year.$disabled);
        $arg["data"]["ANTIBODY_MONTH"] = knjCreateTextBox($objForm, $Row["ANTIBODY_MONTH"], "ANTIBODY_MONTH", 2, 2, $extra_month.$disabled);
        //抗体有無
        $opt_pos[] = array('label' => "", 'value' => "");
        $opt_pos[] = array('label' => "抗体ある", 'value' => "1");
        $opt_pos[] = array('label' => "抗体ない", 'value' => "2");
        $arg["data"]["ANTIBODY_POSITIVE"] = knjCreateCombo($objForm, "ANTIBODY_POSITIVE", $Row["ANTIBODY_POSITIVE"], $opt_pos, $disabled, 1);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf160SubForm4.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform4_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform4_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = " onclick=\"return btn_submit('subform4_delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform4_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
