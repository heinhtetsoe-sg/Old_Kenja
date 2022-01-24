<?php

require_once('for_php7.php');

class knjz021cForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz021cindex.php", "", "edit");

        //データベース接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjz021cQuery::getRow($db,$model);
        } else {
            $Row =& $model->field;
        }
        if ($Row == null) {
            $query = knjz021cQuery::getDefault($model->year);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        
        //受験型コンボ
        $query = knjz021cQuery::getName($model->year, "L105");
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["PRE_EXAM_TYPE"] == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["PRE_EXAM_TYPE"] = knjCreateCombo($objForm, "PRE_EXAM_TYPE", $model->field["PRE_EXAM_TYPE"], $opt, $extra, 1);

        //試験科目コンボ
        $query = knjz021cQuery::getName($model->year, "L109");
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTSUBCLASSCD"] == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["TESTSUBCLASSCD"] = knjCreateCombo($objForm, "TESTSUBCLASSCD", $model->field["TESTSUBCLASSCD"], $opt, $extra, 1);

        //満点
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PERFECT"] = knjCreateTextBox($objForm, $Row["PERFECT"], "PERFECT", 3, 3, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);

        //hidden
        makeHidden($objForm, $Row, $model->year);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz021cindex.php?cmd=list','left_frame');";
        }

        //データベース切断
        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz021cForm2.html", $arg);
    }
}

//hidden作成
function makeHidden(&$objForm, $Row, $year) {
    knjCreateHidden($objForm, "ENTEXAMYEAR", $Row["ENTEXAMYEAR"] ? $Row["ENTEXAMYEAR"] : CTRL_YEAR + 1);
    knjCreateHidden($objForm, "APPLICANTDIV", $Row["APPLICANTDIV"] ? $Row["APPLICANTDIV"] : "1");
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
    knjCreateHidden($objForm, "year", $year);
    knjCreateHidden($objForm, "cmd", "");

}


?>
