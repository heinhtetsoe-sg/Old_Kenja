<?php

require_once('for_php7.php');

class knjm330mForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm330mForm1", "POST", "knjm330mindex.php", "", "knjm330mForm1");

        $db = Query::dbCheckOut();

        //年度CMB
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //生徒の選択のラジオボタン
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onclick=\"return btn_submit('')\"", "id=\"OUTPUT2\" onclick=\"return btn_submit('')\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クラス選択コンボボックスを作成する
        if ($model->field["OUTPUT"] == "2") {
            $disabled = "disabled";
        } else {
            $disabled = "";
        }
        $row1 = array();
        //$row1[]= array('label' => '新入生', 'value' => '00000');
        $query = knjm330mQuery::getAuth(CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if (!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }
        $extra = "onchange=\"return btn_submit('');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra.$disabled, 1);
        
        //学籍番号　始め
        if ($model->field["OUTPUT"] == "1") {
            $disabled = "disabled";
            $model->field["S_SCHREGNO"] = "";
            $model->field["E_SCHREGNO"] = "";
        } else {
            $disabled = "";
        }
        $extra = "";
        $arg["data"]["S_SCHREGNO"] = knjCreateTextBox($objForm, $model->field["S_SCHREGNO"], "S_SCHREGNO", 8, 8, $extra.$disabled);
        
        //学籍番号　終り
        $arg["data"]["E_SCHREGNO"] = knjCreateTextBox($objForm, $model->field["E_SCHREGNO"], "E_SCHREGNO", 8, 8, $extra.$disabled);

        //科目CMB作成
        $query = knjm330mQuery::getSubclass($model);
        $result = $db->query($query);
        $opt_subclass = array();
        $opt_subclass[]= array('label' => '', 'value' => '');
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_subclass[] = array('label' => $row["SUBCLASSCD"]."  ".$row["SUBCLASSABBV"],
                                    'value' => $row["SUBCLASSCD"]);
        }
        $result->free();
        
        if (!$model->field["KAMOKU"]){
            $model->field["KAMOKU"] = $opt_subclass[0]["value"];
        }
        $extra = "onchange=\"return btn_submit('');\"";
        $arg["data"]["KAMOKU"] = knjCreateCombo($objForm, "KAMOKU", $model->field["KAMOKU"], $opt_subclass, $extra, 1);

        //提出回数CMB作成
        $opt_kaisu = array();
        $opt_kaisu[]= array('label' => '', 'value' => '');
        $query = knjm330mQuery::getReportcount($model);
        $result = $db->query($query);
        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_kaisu[] = array('label' => $row["STANDARD_SEQ"],
                                 'value' => $row["STANDARD_SEQ"]);

        }
        $result->free();
        if (!$model->field["TKAISU"]) $model->field["TKAISU"] = $opt_kaisu[0]["value"];
        $extra = "";
        $arg["data"]["TKAISU"] = knjCreateCombo($objForm, "TKAISU", $model->field["TKAISU"], $opt_kaisu, $extra, 1);



        //印刷ボタンを作成する//
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する/
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM330M");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "useRepStandarddateCourseDat", $model->Properties["useRepStandarddateCourseDat"]);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm330mForm1.html", $arg);
    }
}
?>
