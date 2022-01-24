<?php

require_once('for_php7.php');

class knjl002aForm2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl002aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->year && $model->applicantdiv && $model->examType) {
            $query = knjl002aQuery::getRow($model->year, $model->applicantdiv, $model->examType);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $query  = knjl002aQuery::getRow2($model->year, $model->applicantdiv, $model->examType);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["SUBCLASS_".$row["TESTSUBCLASSCD"]] = $row["TESTSUBCLASSCD"];
            }
        } else {
            $Row =& $model->field;
        }

        //初期値セット
        $model->year = ($model->year == "") ? CTRL_YEAR + 1: $model->year;
        if ($model->applicantdiv == "") {
            $extra = "onchange=\"return btn_submit('list');\"";
            $query = knjl002aQuery::getNameMst($model, "L003");
            makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");
        }
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl002aQuery::getNameMst($model, "L003", $model->applicantdiv));

        /****************/
        /**テキスト作成**/
        /****************/
        //受験型CDテキストボックス
        $extra = "STYLE=\"ime-mode: inactive; text-align: right;\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAM_TYPE"] = knjCreateTextBox($objForm, $Row["EXAM_TYPE"], "EXAM_TYPE", 2, 2, $extra);

        //受験型名称テキストボックス
        $extra = "";
        $arg["data"]["EXAMTYPE_NAME"] = knjCreateTextBox($objForm, $Row["EXAMTYPE_NAME"], "EXAMTYPE_NAME", 41, 60, $extra);

        //受験型略称テキストボックス
        $extra = "";
        $arg["data"]["EXAMTYPE_NAME_ABBV"] = knjCreateTextBox($objForm, $Row["EXAMTYPE_NAME_ABBV"], "EXAMTYPE_NAME_ABBV", 11, 15, $extra);

        /******************/
        /** checkbox作成 **/
        /******************/
        //科目checkbox
        $setCheck = "";
        foreach ($model->subClassArr as $key => $name1) {
            list($subStr, $subclassCd) = explode("_", $key);
            $checked = ($subclassCd == $Row[$key]) ? " checked": "";
            $extra   = "id=\"{$key}\"".$checked;
            $setCheck .= knjCreateCheckBox($objForm, $key, $subclassCd, $extra)."<LABEL for=\"{$key}\">{$name1}</LABEL>　";
        }
        $arg["data"]["SUBCLASS"] = $setCheck;

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
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl002aindex.php?cmd=list2"
                            . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."';";

        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl002aForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
