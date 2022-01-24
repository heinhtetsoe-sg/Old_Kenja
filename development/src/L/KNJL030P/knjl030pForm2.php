<?php

require_once('for_php7.php');

class knjl030pForm2 {

    function main(&$model) {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl030pindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->year && $model->applicantdiv && $model->recno) {
            $query = knjl030pQuery::getRow($model->year, $model->applicantdiv, $model->recno);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /******************/
        /*  テキスト作成  */
        /******************/
        //受験番号（開始）テキスト
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAMNO_FROM"] = knjCreateTextBox($objForm, $Row["EXAMNO_FROM"], "EXAMNO_FROM", 6, 6, $extra);

        //受験番号（終了）テキスト
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAMNO_TO"] = knjCreateTextBox($objForm, $Row["EXAMNO_TO"], "EXAMNO_TO", 6, 6, $extra);

        /****************/
        /*  コンボ作成  */
        /****************/
        //入試区分コンボ
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjl030pQuery::getNameMst($model->year, $namecd1);
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, $model, "blank");

        /****************/
        /*  ボタン作成  */
        /****************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //更新ボタン
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

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
        knjCreateHidden($objForm, "RECNO", $model->recno);
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && $model->cmd != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl030pindex.php?cmd=list"
                            . "&year=" .$model->year."&APPLICANTDIV=" .$model->applicantdiv."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl030pForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $model, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
        $i++;
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"])    $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
