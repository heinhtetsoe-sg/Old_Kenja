<?php

require_once('for_php7.php');

class knjh610Form2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh610index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ($model->thisIsGet || $model->cmd == "reset") {
            $Row = knjh610Query::getRow($model->center_subclass_cd, $db, $model, $model->center_class_cd);
        } else {
            $Row =& $model->field;
        }

        //共通テスト教科コード（コンボ）
        $query = knjh610Query::getCenterclasscd();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "CENTER_CLASS_CD", $Row["CENTER_CLASS_CD"], $extra, 1, "BLANK");

        //共通テスト科目コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CENTER_SUBCLASS_CD"] = knjCreateTextBox($objForm, $Row["CENTER_SUBCLASS_CD"], "CENTER_SUBCLASS_CD", 6, 6, $extra);

        //共通テスト科目名称
        $arg["data"]["SUBCLASS_NAME"] = knjCreateTextBox($objForm, $Row["SUBCLASS_NAME"], "SUBCLASS_NAME", 40, 20, "");

        //科目略称
        $arg["data"]["SUBCLASS_ABBV"] = knjCreateTextBox($objForm, $Row["SUBCLASS_ABBV"], "SUBCLASS_ABBV", 10, 5, "");

        //文理区分（コンボ）
        $query = knjh610Query::getBunri();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "BUNRIDIV", $Row["BUNRIDIV"], $extra, 1);

        //受験型
        $extra = $Row["SUBCLASS_DIV"] == "1" ? " checked " : "";
        $arg["data"]["SUBCLASS_DIV"] = knjCreateCheckBox($objForm, "SUBCLASS_DIV", "1", $extra);

        //教科コード（コンボ）
        $query = knjh610Query::getClasscd($model);
        $extra = "onChange=\"btn_submit('edit')\";";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $Row["CLASSCD"], $extra, 1, "BLANK");

        //科目コード（コンボ）
        $classCd = $Row["CLASSCD"] ? $Row["CLASSCD"] : 'dummy';
        $query = knjh610Query::getSubclasscd($classCd, $model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $Row["SUBCLASSCD"], $extra, 1, "BLANK");

        //満点
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PERFECT"] = knjCreateTextBox($objForm, $Row["PERFECT"], "PERFECT", 3, 3, $extra);

        //配点
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ALLOT_POINT"] = knjCreateTextBox($objForm, $Row["ALLOT_POINT"], "ALLOT_POINT", 3, 3, $extra);

        /********/
        /*ボタン*/
        /********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjh610index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh610Form2.html", $arg);
    }
}

/********************************************* 以下関数 *******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($name == "BUNRIDIV") {
        $opt[] = array("label" => "", "value" => "0");
    }
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
