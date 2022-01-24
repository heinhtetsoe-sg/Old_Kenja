<?php

require_once('for_php7.php');

class knja226mForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja226mForm1", "POST", "knja226mindex.php", "", "knja226mForm1");

        $db = Query::dbCheckOut();

        //ログイン年度
        $arg["data"]["CTRL_YEAR"] = $model->control["年度"];

        //年度コンボ作成
        $opt = array(
            array('label' => CTRL_YEAR + 1, 'value' => CTRL_YEAR + 1),
            array('label' => CTRL_YEAR, 'value' => CTRL_YEAR)
             );
        $value = CTRL_YEAR + 1;
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $value, $opt, "", 1);

        //生徒の選択のラジオボタン
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "2" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onclick=\"return btn_submit('')\"", "id=\"OUTPUT2\" onclick=\"return btn_submit('')\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        
        //学籍番号　始め
        if ($model->field["OUTPUT"] == "2") {
            $disabled = "disabled";
            $model->field["S_SCHREGNO"] = "";
            $model->field["E_SCHREGNO"] = "";
        } else {
            $disabled = "";
        }
        $extra = "onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["S_SCHREGNO"] = knjCreateTextBox($objForm, $model->field["S_SCHREGNO"], "S_SCHREGNO", 8, 8, $extra.$disabled);
        
        //学籍番号　終り
        $arg["data"]["E_SCHREGNO"] = knjCreateTextBox($objForm, $model->field["E_SCHREGNO"], "E_SCHREGNO", 8, 8, $extra.$disabled);

        //印刷ボタンを作成する//
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する/
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA226M");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja226mForm1.html", $arg);
    }
}
?>
