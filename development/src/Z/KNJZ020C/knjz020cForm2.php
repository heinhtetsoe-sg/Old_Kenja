<?php

require_once('for_php7.php');

class knjz020cForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz020cindex.php", "", "edit");

        //データベース接続
        $db = Query::dbCheckOut();

        //マスタチェック
        $row = $db->getRow(knjz020cQuery::entexam_course(CTRL_YEAR+1));
        if (!is_array($row)) {
            $arg["Closing"] = " closing_window('受験コースマスタ'); " ;
        }
        for ($i=0; $i<2; $i++) {
            $mname = $i == 0 ? "L004" : "L009";
            $row = $db->getRow(knjz020cQuery::get_name_mst(CTRL_YEAR+1, $mname));
            if (!is_array($row)) {
                $arg["Closing"] = " closing_window('名称マスタ'); " ;
            }
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjz020cQuery::getRow($db,$model);
        }else{
            $Row =& $model->field;
        }
        if ($Row == null) {
            $query = knjz020cQuery::getDefault($model->year);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //試験科目コンボ
        $query = knjz020cQuery::getName($model->year, "L009");
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $model->testsubclasscd, "TESTSUBCLASSCD", $extra, 1, "");

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
            $arg["reload"]  = "window.open('knjz020cindex.php?cmd=list','left_frame');";
        }

        //データベース切断
        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz020cForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = false)
{
    $opt = array();
    $result = $db->query($query);

    if ($blank == "blank" ) {
        $opt[] = array("label" => "", "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();


    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//hidden作成
function makeHidden(&$objForm, $Row, $year) {
    knjCreateHidden($objForm, "APPLICANTDIV", $Row["APPLICANTDIV"]);
    knjCreateHidden($objForm, "TESTDIV", $Row["TESTDIV"]);
    knjCreateHidden($objForm, "KATEIGAKKA", $Row["KATEIGAKKA"]);
    knjCreateHidden($objForm, "COURSE", $Row["COURSE"]);
    knjCreateHidden($objForm, "COURSECD", $Row["COURSECD"]);
    knjCreateHidden($objForm, "MAJORCD", $Row["MAJORCD"]);
    knjCreateHidden($objForm, "EXAMCOURSECD", $Row["EXAMCOURSECD"]);
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
    knjCreateHidden($objForm, "year", $year);
    knjCreateHidden($objForm, "cmd", "");

}


?>
