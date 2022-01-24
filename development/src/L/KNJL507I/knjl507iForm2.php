<?php

require_once('for_php7.php');

class knjl507iForm2
{
    public function main(&$model)
    {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl507iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset")) {
            $query = knjl507iQuery::getRow($model, $model->generalCd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /****************/
        /*  コンボ作成  */
        /****************/
        //対応コースコンボ
        $query = knjl507iQuery::getRemark1($model);
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $Row["REMARK1"], "REMARK1", $extra, 1, $model, "blank");

        /******************/
        /*  テキスト作成  */
        /******************/
        //判定マークコードテキストボックス
        $arg["data"]["GENERAL_CD"] = knjCreateTextBox($objForm, $Row["GENERAL_CD"], "GENERAL_CD", 1, 1, "");

        //判定マークテキストボックス
        $arg["data"]["GENERAL_MARK"] = knjCreateTextBox($objForm, $Row["GENERAL_MARK"], "GENERAL_MARK", 6, 3, "");

        //判定マーク名称テキストボックス
        $arg["data"]["GENERAL_NAME"] = knjCreateTextBox($objForm, $Row["GENERAL_NAME"], "GENERAL_NAME", 22, 10, "");

        //判定マーク略称テキストボックス
        $arg["data"]["GENERAL_ABBV"] = knjCreateTextBox($objForm, $Row["GENERAL_ABBV"], "GENERAL_ABBV", 10, 5, "");

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
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && $model->cmd != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl507iindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl507iForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $model, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
        $i++;
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if (!isset($model->warning) && $row["DEFAULT"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
