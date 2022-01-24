<?php

require_once('for_php7.php');

class knjd106aForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd106aindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = $db->getRow(knjd106aQuery::getRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //コース名コンボ設定
        /*↓引数のデフォルト設定ではうまく動作しなかったためif文で判別*/
        if ($model->field["GRADE"]) {
            $query = knjd106aQuery::getCourseList($model->field["GRADE"]);
        } else {
            $query = knjd106aQuery::getCourseList('01');
        }
        $value = $Row["COURSECD"]."-".$Row["MAJORCD"]."-".$Row["COURSECODE"];
        makeCmb($objForm, $arg, $db, $query, "COURSE", $value, "", 1);

        //全体評
        $extra = "style=\"height:103px;\"";
        $arg["data"]["FOOTNOTE"] = knjCreateTextArea($objForm, "FOOTNOTE", 7, 91, "", $extra, $Row["FOOTNOTE"]);

        //コメント
        $arg["COMMENT"] = "※全角45文字×7行まで";

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $Row, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjd106aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd106aForm2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = KnjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = KnjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = KnjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row, $model)
{
    KnjCreateHidden($objForm, "cmd");
    KnjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
    KnjCreateHidden($objForm, "GRADE", $model->grade);
    KnjCreateHidden($objForm, "TESTCD", $model->testcd);
}

?>
