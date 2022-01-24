<?php

require_once('for_php7.php');


class knjm822Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm822Form1", "POST", "knjm822index.php", "", "knjm822Form1");

        //ログイン年度
        $arg["data"]["CTRL_YEAR"] = $model->control["年度"];

        //年度コンボ作成
        $opt = array();
        $opt[] = array('label' => CTRL_YEAR + 1, 'value' => CTRL_YEAR + 1);
        $opt[] = array('label' => CTRL_YEAR, 'value' => CTRL_YEAR);
        $value = CTRL_YEAR + 1;
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $value, $opt, "", 1);

        //出力区分
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷範囲を作成する
        //開始
        $arg["data"]["SSCHREGNO"] = knjCreateTextBox($objForm, $model->field["SSCHREGNO"], "SSCHREGNO", 8, 8, "");
        //終了
        $arg["data"]["ESCHREGNO"] = knjCreateTextBox($objForm, $model->field["ESCHREGNO"], "ESCHREGNO", 8, 8, "");

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM822");
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm822Form1.html", $arg); 
    }
}
?>
