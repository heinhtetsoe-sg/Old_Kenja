<?php

require_once('for_php7.php');

class knjl537fform {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl537findex.php", "", "right_list");

        //DB接続
        $db = Query::dbCheckOut();

        // 年度
        $arg["ENTEXAMYEAR"] = $model->entexamyear;

        //コード
        $extra = "";
        $examno = (isset($model->warning)) ? $model->field["DEV_CD"] : "";
        $arg["DEV_CD"] = knjCreateTextBox($objForm, $examno, "DEV_CD", 3, 3, $extra);

        //記号
        $extra = "";
        $examno = (isset($model->warning)) ? $model->field["DEV_MARK"] : "";
        $arg["DEV_MARK"] = knjCreateTextBox($objForm, $examno, "DEV_MARK", 6, 6, $extra);

        //from
        $extra = "";
        $examno = (isset($model->warning)) ? $model->field["DEV_LOW"] : "";
        $arg["DEV_LOW"] = knjCreateTextBox($objForm, $examno, "DEV_LOW", 5, 5, $extra);

        //to
        $extra = "";
        $examno = (isset($model->warning)) ? $model->field["DEV_HIGH"] : "";
        $arg["DEV_HIGH"] = knjCreateTextBox($objForm, $examno, "DEV_HIGH", 5, 5, $extra);

        //登録ボタン
        $extra = "onclick=\"return btn_submit('insert');\"";
        $arg["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "登 録", $extra);

        //取消ボタン
        $extra = "onclick=\"btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl537fForm.html", $arg);
    }
}
?>
