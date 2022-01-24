<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425n_1LastYearData1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform", "POST", "knjd425n_1index.php", "", "subform");

        //学籍番号・生徒氏名表示
        $arg["data"]["YEAR"] = $model->exp_year - 1;
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();

        $query = knjd425n_1Query::getLastYearData($model, "9");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seq = ltrim($row["KIND_SEQ"], "0");

            $arg["REMARK_TITLE-".$row["KIND_SEQ"]] = $row["KIND_REMARK"];

            $extra = " readonly ";
            $arg["REMARK-".$seq] = knjCreateTextArea($objForm, "REMARK-".$seq, $model->textLimit[$seq]["gyou"], $model->textLimit[$seq]["moji"] * 2, "", $extra, $row["REMARK"]);

            //取込ボタンを作成
            $extra = "onclick=\"return btn_submit('".$seq."');\"";
            $arg["btn_input-".$seq] = KnjCreateBtn($objForm, "btn_input-".$seq, "取 込", $extra);
        }
        $result->free();

        Query::dbCheckIn($db);

        //戻るボタンを作成
        $extra = "onclick=\"parent.closeit();\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd425n_1LastYearData1.html", $arg);
    }
}
?>
