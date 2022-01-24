<?php

require_once('for_php7.php');

class knjd230mForm1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd230mindex.php", "", "edit");
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        $query = knjd230mQuery::getSpecialReasonDiv($model->qualified_cd);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $arg["YEAR"] = CTRL_YEAR;

        /******************/
        /* コンボボックス */
        /******************/
        //特殊事情
        $query = knjd230mQuery::getSpecialReasonDiv();
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["SPECIAL_REASON_DIV"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["SPECIAL_REASON_DIV"] = ($model->field["SPECIAL_REASON_DIV"] && $value_flg) ? $model->field["SPECIAL_REASON_DIV"] : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('change')\"";
        $arg["SPECIAL_REASON_DIV"] = knjCreateCombo($objForm, "SPECIAL_REASON_DIV", $model->field["SPECIAL_REASON_DIV"], $opt, $extra, 1);

        //学年クラス
        $query = knjd230mQuery::getHrclass();
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["HR_CLASS"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["HR_CLASS"] = ($model->field["HR_CLASS"] && $value_flg) ? $model->field["HR_CLASS"] : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('change')\"";
        $arg["HR_CLASS"] = knjCreateCombo($objForm, "HR_CLASS", $model->field["HR_CLASS"], $opt, $extra, 1);


        /**********/
        /* リスト */
        /**********/
        $query = knjd230mQuery::getList($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            //チェックボックス
            if (strlen($row["SPECIAL_REASON_DIV"])) {
                $extra = "checked='checked' ";
                $row["COLOR"] = '#ccffcc';
            } else {
                $extra = "";
                $row["COLOR"] = '#ffffff';
            }
            $extra .= " onClick=\"chenge_color(this)\"";
            $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["SCHREGNO"], $extra);
            //主催
            $extra = "";
            $row["REMARK"] = knjCreateTextBox($objForm, $row["REMARK"], "REMARK_{$row["SCHREGNO"]}", 80, 40, $extra);

            $arg["data"][] = $row;
        }
        $result->free();

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_upate"] = knjCreateBtn($objForm, "btn_upate", "更 新", $extra);
        //クリア
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjd230mindex.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd230mForm1.html", $arg);
    }
}
?>
