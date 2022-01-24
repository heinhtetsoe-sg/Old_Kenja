<?php

require_once('for_php7.php');


class knjf071Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjf071Form1", "POST", "knjf071index.php", "", "knjf071Form1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        /****************/
        /* ラジオボタン */
        /****************/
        //統計対象データ選択 1:文字 2:数値
        $opt_eye = array(1, 2);
        $model->field["EYE_MOJI"] = ($model->field["EYE_MOJI"] == "") ? "1" : $model->field["EYE_MOJI"];
        $extra = array("id=\"EYE_MOJI1\"", "id=\"EYE_MOJI2\"");
        $radioArray = knjCreateRadio($objForm, "EYE_MOJI", $model->field["EYE_MOJI"], $extra, $opt_eye, get_count($opt_eye));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }
        //統計対象データ選択 1:生活 2:裸眼 3:矯正
        $opt_kubun = array(1, 2, 3);
        $model->field["EYE_KUBUN"] = ($model->field["EYE_KUBUN"] == "") ? "1" : $model->field["EYE_KUBUN"];
        $extra = array("id=\"EYE_KUBUN1\"", "id=\"EYE_KUBUN2\"", "id=\"EYE_KUBUN3\"");
        $radioArray = knjCreateRadio($objForm, "EYE_KUBUN", $model->field["EYE_KUBUN"], $extra, $opt_kubun, get_count($opt_kubun));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /********************/
        /* チェックボックス */
        /********************/
        //異年令を除く
        $extra  = ($model->field["AGE_NOZOKU"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"AGE_NOZOKU\"";
        $arg["data"]["AGE_NOZOKU"] = knjCreateCheckBox($objForm, "AGE_NOZOKU", "on", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJF071");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "VisionMark_SaveValueCd", $model->Properties["VisionMark_SaveValueCd"]);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf071Form1.html", $arg);
    }
}
