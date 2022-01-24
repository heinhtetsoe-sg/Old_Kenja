<?php

require_once('for_php7.php');

class knjl502jForm2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl502jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->year && $model->applicantdiv && $model->examType) {
            $query = knjl502jQuery::getRow($model->year, $model->applicantdiv, $model->examType);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $query  = knjl502jQuery::getRow2($model->year, $model->applicantdiv, $model->examType);
            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["SUBCLASS_SELECT_".$row["SUBCLASSCD"]] = ($row["SUBCLASSCD"] != "") ? "1":"";
                $Row["JUDGE_SUMMARY_".$row["SUBCLASSCD"]] = $row["JUDGE_SUMMARY"];
            }
        } else {
            $Row =& $model->field;
        }

        /****************/
        /**テキスト作成**/
        /****************/
        //入試方式CDテキストボックス
        $extra = "";
        $arg["data"]["EXAM_TYPE"] = knjCreateTextBox($objForm, $Row["EXAM_TYPE"], "EXAM_TYPE", 2, 2, $extra);

        //入試方式名称テキストボックス
        $extra = "";
        $arg["data"]["EXAMTYPE_NAME"] = knjCreateTextBox($objForm, $Row["EXAMTYPE_NAME"], "EXAMTYPE_NAME", 15, 60, $extra);

        //帳票出力略称テキストボックス
        $extra = "";
        $arg["data"]["EXAMTYPE_NAME_ABBV"] = knjCreateTextBox($objForm, $Row["EXAMTYPE_NAME_ABBV"], "EXAMTYPE_NAME_ABBV", 15, 15, $extra);

        /******************/
        /** checkbox作成 **/
        /******************/
        //科目選択checkbox
        $setCheck = "";
        foreach ($model->subClassArr as $key => $name1) {
            list($subStr, $subclassCd) = explode("_", $key);
            $keyNm = "SUBCLASS_SELECT_".$key;
            $checked = ($Row[$keyNm] == "1") ? "checked":"";
            $extra   = "onclick=\"return btn_submit('set');\" id=\"{$keyNm}\"".$checked;
            $setCheck .= knjCreateCheckBox($objForm, $keyNm, "1", $extra)."<LABEL for=\"{$keyNm}\">{$name1}</LABEL><BR>";
        }
        $arg["data"]["SUBCLASS_SELECT"] = $setCheck;

        //判定集計checkbox
        $setCheck = "";
        foreach ($model->subClassArr as $key => $name1) {
            list($subStr, $subclassCd) = explode("_", $key);
            $keyNm = "JUDGE_SUMMARY_".$key;
            $keyNm2 = "SUBCLASS_SELECT_".$key;
            $checked = ($Row[$keyNm] == "1" && $Row[$keyNm2] == "1") ? "checked":"";
            $disabled = ($Row[$keyNm2] == "1") ? "":"disabled"; //科目選択にチェックが入ってない場合、非活性とする
            $extra   = $disabled." id=\"{$keyNm}\"".$checked;
            $setCheck .= knjCreateCheckBox($objForm, $keyNm, "1", $extra)."<BR>";
        }
        $arg["data"]["JUDGE_SUMMARY"] = $setCheck;

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
            $arg["reload"]  = "parent.left_frame.location.href='knjl502jindex.php?cmd=list2"
                            . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl502jForm2.html", $arg);
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
