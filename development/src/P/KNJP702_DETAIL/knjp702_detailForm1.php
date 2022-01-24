<?php

require_once('for_php7.php');

class knjp702_detailForm1
{
    function main(&$model)
    {
        //パラメータチェック
        if ($model->exp_lcd == "" || $model->exp_mcd == ""){
            $arg["jscript"] = "OnParameterError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp702_detailindex.php", "", "edit");

        //取得した値をセット
        $SetRow = knjp702_detailQuery::getSetRow($model);
        $db = Query::dbCheckOut();

        $arg["top"]["YEAR"]               = $model->year;
        $arg["top"]["COLLECT_L_NAME"]     = $SetRow["COLLECT_L_NAME"];
        $arg["top"]["COLLECT_M_NAME"]     = $SetRow["COLLECT_M_NAME"];
        $arg["top"]["COLLECT_M_MONEY"]    = ($SetRow["COLLECT_M_MONEY"] != "") ? number_format($SetRow["COLLECT_M_MONEY"]).'円' : "";

        //DB接続
        $db = Query::dbCheckOut();

        //データ一覧
        $model->data["NAMEVALUE"] = "";
        $query = knjp702_detailQuery::getRow($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = ($model->data["NAMEVALUE"] == "") ? "" : ",";
            $model->data["NAMEVALUE"] .= $sep.$row["NAMEVALUE"];
            $value = ($model->data["TOKUSYU_VAL".$row["NAMEVALUE"]] == "") ? $row["TOKUSYU_VAL"] : $model->data["TOKUSYU_VAL".$row["NAMEVALUE"]];

            //値をセット
            $opt = array();
            $query = knjp702_detailQuery::getNameMstQuery($model, $row["NAMEVALUE"]);
            $value_flg = false;
            $rowval = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $opt[] = array('label' => "", 'value' => "");
            $opt[] = array('label' => $rowval["NAME2LABEL"], 'value' =>'1');
            $opt[] = array('label' => $rowval["NAME3LABEL"], 'value' =>'2');
            $extra = "";
            $row["TOKUSYU_VAL"] = knjCreateCombo($objForm, "TOKUSYU_VAL".$row["NAMEVALUE"], $value, $opt, $extra, 1);

            $arg["data"][] = $row;
        }
        $result->free();

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //終了ボタン
        $extra = "onclick=\"parentSubmit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
        
        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjp702_detailindex.php?cmd=list';";
        }

        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp702_detailForm1.html", $arg); 
    }
}
?>
