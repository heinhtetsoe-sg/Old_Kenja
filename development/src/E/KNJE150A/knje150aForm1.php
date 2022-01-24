<?php

require_once('for_php7.php');


class knje150aForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje150aForm1", "POST", "knje150aindex.php", "", "knje150aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //対象ラジオボタン 1:学習の記録 2:出欠の記録
        $model->field["OUT_DIV"] = $model->field["OUT_DIV"] ? $model->field["OUT_DIV"] : '1';
        $opt_outdiv = array(1, 2, 3);
        $extra = array("id=\"OUT_DIV1\"", "id=\"OUT_DIV2\"", "id=\"OUT_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "OUT_DIV", $model->field["OUT_DIV"], $extra, $opt_outdiv, get_count($opt_outdiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷ボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje150aForm1.html", $arg); 
    }
}
?>
