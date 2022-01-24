<?php

require_once('for_php7.php');

class knjl030uForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl030uindex.php", "", "main");
        $db           = Query::dbCheckOut();

        if ($model->isWarning()) {
            $row =& $model->field;
        } else {
            $query = knjl030uQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        //会場名
        $extra = "";
        $arg["EXAMHALL_NAME"] = knjCreateTextBox($objForm, $row["EXAMHALL_NAME"], "EXAMHALL_NAME", 30, 30, $extra);

        //会場名
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["CAPA_CNT"] = knjCreateTextBox($objForm, $row["CAPA_CNT"], "CAPA_CNT", 5, 4, $extra);

        if ($model->mode == "update") {
            $value = "更 新";
        } else {
            $value = "追 加";
        }
        //更新ボタン作成
        $extra = "onclick=\"return btn_submit('".$model->mode ."')\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", $value, $extra);

        //戻るボタン作成
        $extra = "onclick=\"top.main_frame.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "名前", "VALUE");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl030uForm2.html", $arg); 
    }
}
?>
