<?php

require_once('for_php7.php');


class knjmp911Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjmp911index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //本締めデータチェック
        $model->getCount = "";
        $model->getCount = $db->getOne(knjmp911Query::getCloseFlgData());
        if ($model->getCount > 0) {
            $arg["COMENT"] = '(本締め 実行済み)';
            $disabled = "disabled";
        }
        //年度締めフラグ
        $extra  = "id=\"CLOSE_FLG\"";
        if ($model->field["CLOSE_FLG"] == "1" || $model->getCount > 0) {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["CLOSE_FLG"] = knjCreateCheckBox($objForm, "CLOSE_FLG", "1", $extra.$disabled);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp911Form1.html", $arg); 
    }
}
//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    if ($model->getCount > 0) {
        $disabled = "disabled";
    }
    //実行
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "実 行", $extra.$disabled);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
