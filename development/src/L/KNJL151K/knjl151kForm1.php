<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knjl151kForm1
{
    public function main($model)
    {
        $objForm = new form();
        $db  = Query::dbCheckOut();

        //対象年度
        $arg["data"]["YEAR"] = CTRL_YEAR + 1;

        //対象データコンボ
        $opt_target[] = array("label" => " 1：基礎・住所・得点データ", "value" => 1);
        $opt_target[] = array("label" => " 2：事前相談データ",         "value" => 2);
        $opt_target[] = array("label" => " 3：内申データ",             "value" => 3);

        $extra = "";
        $arg["data"]["TARGET"] = knjCreateCombo($objForm, "TARGET", $model->target, $opt_target, $extra, 1);

        //対象ファイル
        $extra = "";
        $arg["data"]["FILE"] = knjCreateFile($objForm, "FILE", 2048000, $extra);

        //ヘッダ有無
        $extra = ($model->headercheck == "1")? "checked" : "";
        $arg["data"]["HEADERCHECK"] = knjCreateCheckBox($objForm, "HEADERCHECK", "1", $extra);

        //ボタン
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "実  行", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終  了", $extra);

        $extra = "onclick=\"return btn_submit('output');\"";
        $arg["button"]["btn_output"] = knjCreateBtn($objForm, "btn_output", "テンプレート書出し", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["start"]   = $objForm->get_start("main", "POST", "knjl151kindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl151kForm1.html", $arg);
    }
}
