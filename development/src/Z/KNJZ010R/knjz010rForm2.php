<?php

require_once('for_php7.php');

class knjz010rForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz010rindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2") && $model->year && $model->examcoursecd && $model->applicantdiv && $model->testdiv && $model->coursecd && $model->majorcd){
            $query = knjz010rQuery::getRow($model->year, $model->examcoursecd, $model->applicantdiv, $model->testdiv, $model->coursecd, $model->majorcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /**************/
        /**コンボ作成**/
        /**************/
        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('chgAppDiv');\"";
        $query = knjz010rQuery::getNameMst($model, $model->year, "L003");
        makeCombo($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "blank");

        //入試区分コンボ
        $model->testDivCd = "";
        if ($Row["APPLICANTDIV"]) {
            $model->testDivCd = $Row["APPLICANTDIV"] == '2' ? "L024" : "L004";
        }
        if ($model->cmd == 'chgAppDiv') {
            $Row["TESTDIV"] = "";
        }
        $query = knjz010rQuery::getNameMst($model, $model->year, $model->testDivCd);
        makeCombo($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", "", 1, "blank");

        //課程学科コンボ
        $query = knjz010rQuery::selectTotalcd($model->year);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCombo($objForm, $arg, $db, $query, $Row["TOTALCD"], "TOTALCD", $extra, 1, "blank");

        //コース記号テキストボックス
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        if ($Row["TOTALCD"] === '1002') {
            $opt[] = array("label" => "ＧＳ", "value" => "ＧＳ");
            $opt[] = array("label" => "ＧＡ", "value" => "ＧＡ");
            $opt[] = array("label" => "ＧＢ", "value" => "ＧＢ");
            $opt[] = array("label" => "ＧＣ", "value" => "ＧＣ");
            $opt[] = array("label" => "ＧⅠ", "value" => "ＧⅠ");
            $opt[] = array("label" => "ＧⅡ", "value" => "ＧⅡ");
        } else if ($Row["TOTALCD"] === '1003'){
            $opt[] = array("label" => "ＳＧ", "value" => "ＳＧ");
        }
        $arg["data"]["EXAMCOURSE_MARK"] = knjCreateCombo($objForm, "EXAMCOURSE_MARK", $Row["EXAMCOURSE_MARK"], $opt, "", 1);

        //入学課程学科コンボ
        $query = knjz010rQuery::selectTotalcd($model->year);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCombo($objForm, $arg, $db, $query, $Row["ENTER_TOTALCD"], "ENTER_TOTALCD", $extra, 1, "blank");

        //入学コースコンボ
        $query = knjz010rQuery::selectCourceCode($model->year);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCombo($objForm, $arg, $db, $query, $Row["ENTER_COURSECODE"], "ENTER_COURSECODE", $extra, 1, "blank");

        /****************/
        /**テキスト作成**/
        /****************/
        //コースコードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAMCOURSECD"] = knjCreateTextBox($objForm, $Row["EXAMCOURSECD"], "EXAMCOURSECD", 4, 4, $extra);

        //コース名テキストボックス
        $arg["data"]["EXAMCOURSE_NAME"] = knjCreateTextBox($objForm, $Row["EXAMCOURSE_NAME"], "EXAMCOURSE_NAME", 30, 30, "");
        
        //コース名略称テキストボックス
        $arg["data"]["EXAMCOURSE_ABBV"] = knjCreateTextBox($objForm, $Row["EXAMCOURSE_ABBV"], "EXAMCOURSE_ABBV", 20, 20, "");
        
        //コース定員テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CAPACITY"] = knjCreateTextBox($objForm, $Row["CAPACITY"], "CAPACITY", 3, 3, $extra);

        /**************/
        /**ボタン作成**/
        /**************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);

        /**************/
        /**hidden作成**/
        /**************/
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "chgAppDiv"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz010rindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz010rForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = false) {
    $opt = array();
    $value_flg = false;
    if ($blank == "blank") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
