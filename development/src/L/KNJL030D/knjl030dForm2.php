<?php

require_once('for_php7.php');

class knjl030dForm2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl030dindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->isWarning()) {
            $row =& $model->field;
        } else {
            $query = knjl030dQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //会場名
        $extra = "";
        $arg["EXAMHALL_NAME"] = knjCreateTextBox($objForm, $row["EXAMHALL_NAME"], "EXAMHALL_NAME", 20, 30, $extra);

        //人数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["CAPA_CNT"] = knjCreateTextBox($objForm, $row["CAPA_CNT"], "CAPA_CNT", 5, 4, $extra);

        //更新ボタン
        $label = ($model->mode == "update") ? "更 新" : "追 加";
        $extra = "onclick=\"return btn_submit('".$model->mode ."')\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", $label, $extra);

        //戻るボタン
        $extra = "onclick=\"top.main_frame.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "名前", "VALUE");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjl030dForm2.html", $arg); 
    }
}
?>
