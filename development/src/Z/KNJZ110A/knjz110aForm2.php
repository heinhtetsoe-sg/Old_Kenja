<?php

require_once('for_php7.php');

class knjz110aForm2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz110aindex.php", "", "edit");
        $db  = Query::dbCheckOut();
        if (!isset($model->warning)) {
            $query = knjz110aQuery::getCityMst($model->pref_cd, $model->city_cd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //都道府県コード
        $query = knjz110aQuery::getPrefCd();
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["PREF_CD"] == $row["VALUE"]) $value_flg = true;
        }
//        $Row["PREF_CD"] = ($value && $value_flg) ? $Row["PREF_CD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["PREF_CD"] = knjCreateCombo($objForm, "PREF_CD", $Row["PREF_CD"], $opt, $extra, 1);

        //市区町村コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CITY_CD"] = knjCreateTextBox($objForm, $Row["CITY_CD"], "CITY_CD", 3, 3, $extra);

        //市区町村名称
        $extra = "";
        $arg["data"]["CITY_NAME"] = knjCreateTextBox($objForm, $Row["CITY_NAME"], "CITY_NAME", 40, 40, $extra);

        //市区町村かな名
        $extra = "";
        $arg["data"]["CITY_KANA"] = knjCreateTextBox($objForm, $Row["CITY_KANA"], "CITY_KANA", 40, 40, $extra);

        //フラグ1
        $extra = ($Row["CITY_FLG1"] == '1') ? "checked=\"checked\"" : "";
        $arg["data"]["CITY_FLG1"] = knjCreateCheckBox($objForm, "CITY_FLG1", "1", $extra);

        //フラグ2
        $extra = ($Row["CITY_FLG2"] == '1') ? "checked=\"checked\"" : "";
        $arg["data"]["CITY_FLG2"] = knjCreateCheckBox($objForm, "CITY_FLG2", "1", $extra);

        //フラグ3
        $extra = ($Row["CITY_FLG3"] == '1') ? "checked=\"checked\"" : "";
        $arg["data"]["CITY_FLG3"] = knjCreateCheckBox($objForm, "CITY_FLG3", "1", $extra);

        //フラグ4
        $extra = ($Row["CITY_FLG4"] == '1') ? "checked=\"checked\"" : "";
        $arg["data"]["CITY_FLG4"] = knjCreateCheckBox($objForm, "CITY_FLG4", "1", $extra);

        //フラグ5
        $extra = ($Row["CITY_FLG5"] == '1') ? "checked=\"checked\"" : "";
        $arg["data"]["CITY_FLG5"] = knjCreateCheckBox($objForm, "CITY_FLG5", "1", $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz110aindex.php?cmd=list&ed=1','left_frame');";
        }
        View::toHTML($model, "knjz110aForm2.html", $arg);
    }
}
?>
