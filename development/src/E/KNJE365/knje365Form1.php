<?php

require_once('for_php7.php');

class knje365Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje365Form1", "POST", "knje365index.php", "", "knje365Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //文理選択ラジオボタン 1:文系 2:理系 3:全体
        $model->field["BRDIV"] = $model->field["BRDIV"] ? $model->field["BRDIV"] : '3';
        $opt_brdiv = array(1, 2, 3);
        $extra = array("id=\"BRDIV1\"", "id=\"BRDIV2\"", "id=\"BRDIV3\"");
        $radioArray = knjCreateRadio($objForm, "BRDIV", $model->field["BRDIV"], $extra, $opt_brdiv, get_count($opt_brdiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力順ラジオボタン 1:年組番 2:学籍番号 3:コース別成績
        $model->field["SORT"] = $model->field["SORT"] ? $model->field["SORT"] : '1';
        $opt_sort = array(1, 2, 3);
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"", "id=\"SORT3\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje365Form1.html", $arg); 
    }
}
?>
