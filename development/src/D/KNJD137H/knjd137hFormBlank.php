<?php

require_once('for_php7.php');

class knjd137hFormBlank {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd137hindex.php", "", "edit");

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //画面のリロード
        if ($model->exp_year && $model->exp_semester) {
            $model->setWarning("MSG305");
            $arg["NOT_WARNING"] = 1;
            $arg["reload"] = "closeWin();";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd137hFormBlank.html", $arg);
    }
}
?>
