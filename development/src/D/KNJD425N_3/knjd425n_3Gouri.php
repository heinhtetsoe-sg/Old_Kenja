<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425n_3Gouri
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform", "POST", "knjd425n_3index.php", "", "subform");

        //学籍番号・生徒氏名表示
        $arg["data"]["YEAR"] = $model->exp_year;
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();

        $query = knjd425n_3Query::getReasonableAccommodation($model);
        $reasonableAccommodation = $db->getOne($query);

        $extra = " readonly ";
        $arg["REASONABLE_ACCOMMODATION"] = knjCreateTextArea($objForm, "REASONABLE_ACCOMMODATION", 20, 90, "", $extra, $reasonableAccommodation);

        Query::dbCheckIn($db);

        //戻るボタンを作成
        $extra = "onclick=\"parent.closeit();\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd425n_3Gouri.html", $arg);
    }
}
?>
