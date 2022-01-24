<?php

require_once('for_php7.php');

class knjl505iForm2
{
    public function main(&$model)
    {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl505iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset")) {
            $query = knjl505iQuery::getRow($model, $model->generalCd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /****************/
        /*  コンボ作成  */
        /****************/
        //入試学科コンボ
        if ($model->field["REMARK1"] != "") {
            $Row["REMARK1"] = $model->field["REMARK1"];
        } elseif ($Row["REMARK1"] == "") {
            $Row["REMARK1"] = "1";
        }
        $extra = " onChange=\"return btn_submit('chgGakkaCmg');\" ";
        $arg["data"]["REMARK1"] = knjCreateCombo($objForm, "REMARK1", $Row["REMARK1"], $model->subjectList, $extra, 1);

        //賢者課程学科
        $query = knjl505iQuery::getTotalcd($model->year, $Row["REMARK1"]);
        if ($Row["REMARK1"] == "1") {
            $totalCdRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            knjCreateHidden($objForm, "ENTER_TOTALCD", $totalCdRow["VALUE"]);
        } elseif ($Row["REMARK1"] == "2") {
            $extra = "";
            makeCombo($objForm, $arg, $db, $query, $Row["ENTER_TOTALCD"], "ENTER_TOTALCD", $extra, 1, $model);

            $arg["dispGakkaCmb"] = "1";
        }

        /******************/
        /*  テキスト作成  */
        /******************/
        //類テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["GENERAL_CD"] = knjCreateTextBox($objForm, $Row["GENERAL_CD"], "GENERAL_CD", 2, 2, $extra);

        //類名称テキストボックス
        $arg["data"]["GENERAL_NAME"] = knjCreateTextBox($objForm, $Row["GENERAL_NAME"], "GENERAL_NAME", 40, 20, "");

        //略称テキストボックス
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
            $arg["reload"]  = "parent.left_frame.location.href='knjl505iindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl505iForm2.html", $arg);
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
