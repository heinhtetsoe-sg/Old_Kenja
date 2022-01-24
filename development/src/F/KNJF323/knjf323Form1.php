<?php

require_once('for_php7.php');

class knjf323Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf323index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //データを取得
        $original = $db->getRow(knjf323Query::selectQuery($model), DB_FETCHMODE_ASSOC);

        //生徒情報表示
        $arg["data"]["SCHREGNO"]    = $model->schregno;
        $arg["data"]["SCH_INFO"]    = ($original["ATTENDNO"]) ? $original["HR_NAME"].$original["ATTENDNO"].'番' : $original["HR_NAME"];
        $arg["data"]["NAME"]        = $original["NAME"];
        $arg["data"]["SEX"]         = $original["SEX"];
        $arg["data"]["BIRTHDAY"]    = ($original["BIRTHDAY"]) ? common::DateConv1(str_replace("-", "/", $original["BIRTHDAY"]),0).'生' : "";
        $arg["data"]["BLOODTYPE"]   = $original["BLOODTYPE"];
        $arg["data"]["BLOOD_RH"]    = $original["BLOOD_RH"];

        //データを取得
        if (isset($model->schregno) && !isset($model->warning)) {
            unset($model->field);
            $row = $db->getRow(knjf323Query::selectQuery($model), DB_FETCHMODE_ASSOC);
        } else {
            $row =& $model->field;
        }

        //既往症コンボ
        $query = knjf323Query::getNameMst("F143");
        makeCmb($objForm, $arg, $db, $query, "MEDICAL_HISTORY1", $row["MEDICAL_HISTORY1"], "", 1);
        makeCmb($objForm, $arg, $db, $query, "MEDICAL_HISTORY2", $row["MEDICAL_HISTORY2"], "", 1);
        makeCmb($objForm, $arg, $db, $query, "MEDICAL_HISTORY3", $row["MEDICAL_HISTORY3"], "", 1);

        //診断名テキストボックス
        $arg["data"]["DIAGNOSIS_NAME"] = knjCreateTextBox($objForm, $row["DIAGNOSIS_NAME"], "DIAGNOSIS_NAME", 100, 50, "");

        //（運動）指導区分コンボ
        $query = knjf323Query::getNameMst("F141", "ABBV1");
        makeCmb($objForm, $arg, $db, $query, "GUIDE_DIV", $row["GUIDE_DIV"], "", 1);

        //（運動）部活動コンボ
        $query = knjf323Query::getNameMst("F142");
        makeCmb($objForm, $arg, $db, $query, "JOINING_SPORTS_CLUB", $row["JOINING_SPORTS_CLUB"], "", 1);

        //アレルギー疾患有無ラジオボタン 1:あり 2:なし
        for ($i=1; $i <= 6 ; $i++) {
            $name = "CARE_FLG".sprintf("%02d", $i);
            $opt = array(1, 2);
            $row[$name] = ($row[$name] != "1") ? "2" : $row[$name];
            $extra = array("id=\"{$name}1\"", "id=\"{$name}2\"");
            $radioArray = knjCreateRadio($objForm, $name, $row[$name], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $original);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf323Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $original) {
    //気管支ぜん息ボタン
    $extra = ($original["CARE_FLG01"] == 1) ? "onclick=\"return btn_submit('subform1');\"" : "disabled";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "気管支ぜん息", $extra);
    //アトピー性皮膚炎ボタン
    $extra = ($original["CARE_FLG02"] == 1) ? "onclick=\"return btn_submit('subform2');\"" : "disabled";
    $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "アトピー性皮膚炎", $extra);
    //アレルギー性結膜炎ボタン
    $extra = ($original["CARE_FLG03"] == 1) ? "onclick=\"return btn_submit('subform3');\"" : "disabled";
    $arg["button"]["btn_subform3"] = KnjCreateBtn($objForm, "btn_subform3", "アレルギー性結膜炎", $extra);
    //食物アレルギーボタン
    $extra = ($original["CARE_FLG04"] == 1 || $original["CARE_FLG05"] == 1) ? "onclick=\"return btn_submit('subform4');\"" : "disabled";
    $arg["button"]["btn_subform4"] = KnjCreateBtn($objForm, "btn_subform4", "食物アレルギー・アナフィラキシー", $extra);
    //アレルギー性鼻炎ボタン
    $extra = ($original["CARE_FLG06"] == 1) ? "onclick=\"return btn_submit('subform6');\"" : "disabled";
    $arg["button"]["btn_subform6"] = KnjCreateBtn($objForm, "btn_subform6", "アレルギー性鼻炎", $extra);

    //更新ボタン（アレルギー疾患）
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->schregno) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //印刷ボタン
    $extra = "onclick=\"return newwin('".SERVLET_URL."');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

    //ＣＳＶ出力ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

    //更新ボタン（健康診断）
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->schregno) ? "onclick=\"return btn_submit('update2');\"" : "disabled";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJF323");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
}
?>
