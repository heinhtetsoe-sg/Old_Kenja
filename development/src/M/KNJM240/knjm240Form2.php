<?php

require_once('for_php7.php');


class knjm240Form2
{

    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm240index.php", "", "edit");

        $db = Query::dbCheckOut();
        if (isset($model->warning) || !$model->chaircd) {
            $row = $model->field;
        } else {
            //SQL文発行
            $query = knjm240Query::selectQuery($model,'');
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        $arg["data"] = $row;
        if (is_array($row)) {
            $model->AddorUp = "up";
        } else {
            $model->AddorUp = "add";
        }
        $arg["NAME"] = $model->subclass_show;
        $arg["CHAIRCD"] = $model->chaircd;
        $arg["SUBCLASSCD"] = $model->subclasscd;
        $arg["data"]["KAMOKU"] = $model->subclass_show;

        if (isset($model->warning) || !$model->chaircd) $row["REP_SEQ_ALL"] = $row["SCHCNT"];
        $extra = "onblur=\"this.value=toInteger(this.value);check(this)\"";
        $arg["data"]["SCHCNT"] = knjCreateTextBox($objForm, $row["REP_SEQ_ALL"], "SCHCNT", 2, 2, $extra);

        if (isset($model->warning) || !$model->chaircd) $row["REP_LIMIT"] = $row["CHECKCNT"];
        $extra = "onblur=\"this.value=toInteger(this.value);check(this)\"";
        $arg["data"]["CHECKCNT"] = knjCreateTextBox($objForm, $row["REP_LIMIT"], "CHECKCNT", 2, 2, $extra);

        Query::dbCheckIn($db);

        $arg["SUBCLASSCD"]  = $model->subclasscd;

        //修正ボタンを作成する
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登　録", $extra);

        //クリアボタンを作成する
        $extra = " onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && !isset($model->warning)) {
            $arg["reload"]  = "parent.left_frame.location.reload();";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm240Form2.html", $arg);
    }
}
?>
