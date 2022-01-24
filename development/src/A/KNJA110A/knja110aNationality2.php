<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja110aNationality2
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform2", "POST", "knja110aindex.php", "", "subform2");

        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();

        $query = knja110aQuery::getNationality2($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //国籍
        $model->nationality2 = $row["BASE_REMARK1"];
        $query = knja110aQuery::getNameMst("A024");
        $extra = "onChange=\"parent.document.forms[0].NATIONALITY2_FLG.value = 1;document.forms[0].NATIONALITY2_FLG.value = 1;\"";
        makeCmb($objForm, $arg, $db, $query, $model->nationality2, "NATIONALITY2", $extra, 1, "BLANK");
        $model->detail011Back["NATIONALITY2"] = $row["BASE_REMARK1"];

        //氏名
        $extra = "onChange=\"parent.document.forms[0].NATIONALITY_NAME_FLG.value = 1;document.forms[0].NATIONALITY_NAME_FLG.value = 1;\"";
        $arg["data"]["NATIONALITY_NAME"] = knjCreateTextBox($objForm, $row["NAME"], "NATIONALITY_NAME", 50, 80, $extra);
        $model->detail011Back["NATIONALITY_NAME"] = $row["NAME"];

        //氏名かな
        $extra = "onChange=\"parent.document.forms[0].NATIONALITY_NAME_KANA_FLG.value = 1;document.forms[0].NATIONALITY_NAME_KANA_FLG.value = 1;\"";
        $arg["data"]["NATIONALITY_NAME_KANA"] = knjCreateTextBox($objForm, $row["NAME_KANA"], "NATIONALITY_NAME_KANA", 80, 160, $extra);
        $model->detail011Back["NATIONALITY_NAME_KANA"] = $row["NAME_KANA"];

        //英字氏名
        $extra = "onChange=\"parent.document.forms[0].NATIONALITY_NAME_ENG_FLG.value = 1;document.forms[0].NATIONALITY_NAME_ENG_FLG.value = 1;\"";
        $arg["data"]["NATIONALITY_NAME_ENG"] = knjCreateTextBox($objForm, $row["NAME_ENG"], "NATIONALITY_NAME_ENG", 40, 40, $extra);
        $model->detail011Back["NATIONALITY_NAME_ENG"] = $row["NAME_ENG"];

        //戸籍氏名
        $extra = "onChange=\"parent.document.forms[0].NATIONALITY_REAL_NAME_FLG.value = 1;document.forms[0].NATIONALITY_REAL_NAME_FLG.value = 1;\"";
        $arg["data"]["NATIONALITY_REAL_NAME"] = knjCreateTextBox($objForm, $row["REAL_NAME"], "NATIONALITY_REAL_NAME", 50, 80, $extra);
        $model->detail011Back["NATIONALITY_REAL_NAME"] = $row["REAL_NAME"];

        //戸籍氏名かな
        $extra = "onChange=\"parent.document.forms[0].NATIONALITY_REAL_NAME_KANA_FLG.value = 1;document.forms[0].NATIONALITY_REAL_NAME_KANA_FLG.value = 1;\"";
        $arg["data"]["NATIONALITY_REAL_NAME_KANA"] = knjCreateTextBox($objForm, $row["REAL_NAME_KANA"], "NATIONALITY_REAL_NAME_KANA", 60, 160, $extra);
        $model->detail011Back["NATIONALITY_REAL_NAME_KANA"] = $row["REAL_NAME_KANA"];

        Query::dbCheckIn($db);

        //終了ボタンを作成する
        $extra = "onclick=\"return btn_submit('nationalityUpd');\"";
        $arg["nationality2Upd"] = knjCreateBtn($objForm, "nationality2Upd", "更 新", $extra);

        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "NATIONALITY_NAME_FLG");
        knjCreateHidden($objForm, "NATIONALITY_NAME_FLG");
        knjCreateHidden($objForm, "NATIONALITY_NAME_KANA_FLG");
        knjCreateHidden($objForm, "NATIONALITY_NAME_ENG_FLG");
        knjCreateHidden($objForm, "NATIONALITY_REAL_NAME_FLG");
        knjCreateHidden($objForm, "NATIONALITY_REAL_NAME_KANA_FLG");

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja110aNationality2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
