<?php

require_once('for_php7.php');

/********************************************************************/
/* レポート提出用（通信制）                         山城 2004/04/26 */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm330Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm330Form1", "POST", "knjm330index.php", "", "knjm330Form1");

        //年度CMB
        $opt_nen = array();
        //今年度と来年度までを設定
        $opt_nen[0] = array("label" => $model->control["年度"],
                            "value" => $model->control["年度"]);
        $opt_nen[1] = array("label" => $model->control["年度"]+1,
                            "value" => $model->control["年度"]+1);
        if (!$model->field["YEAR"]){
            $model->field["YEAR"] = $model->control["年度"];
        }

        $extra = "style=\"width:60px \" onchange=\"return btn_submit('');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_nen, $extra, 1);

        //出力方法（1:一年分2:一枚単位）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array(" onClick=\"return btn_submit('');\"", " onClick=\"return btn_submit('');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //科目CMB作成
        $db = Query::dbCheckOut();
        $query = knjm330Query::getSubclass($model);
        $result = $db->query($query);
        $opt_subclass = array();
        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_subclass[] = array('label' => $row["SUBCLASSCD"]."  ".$row["SUBCLASSABBV"],
                                    'value' => $row["SUBCLASSCD"]);

        }
        $result->free();
        Query::dbCheckIn($db);
        
        if (!$model->field["KAMOKU"]){
            $model->field["KAMOKU"] = $opt_subclass[0]["value"];
        }
        if ($model->field["OUTPUT"] != 2) {
            $extrahtml = "disabled";
        } else {
            $extrahtml = "onchange=\"return btn_submit('');\"";
        }
        $extra = $extrahtml;
        $arg["data"]["KAMOKU"] = knjCreateCombo($objForm, "KAMOKU", $model->field["KAMOKU"], $opt_subclass, $extra, 1);

        //提出回数CMB作成
        $opt_kaisu = array();
        $db = Query::dbCheckOut();
        $query = knjm330Query::getReportcount($model);
        $result = $db->query($query);
        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_kaisu[] = array('label' => $row["STANDARD_SEQ"],
                                 'value' => $row["STANDARD_SEQ"]);

        }
        $result->free();
        Query::dbCheckIn($db);
        if (!$model->field["TKAISU"]) $model->field["TKAISU"] = $opt_kaisu[0]["value"];
        $extra = $extrahtml;
        $arg["data"]["TKAISU"] = knjCreateCombo($objForm, "TKAISU", $model->field["TKAISU"], $opt_kaisu, $extra, 1);

        $arg["data"]["TKAISU"] = $objForm->ge("TKAISU");

        //表紙チェックボックス
        if(!$model->field["HYOUSI"]){
            $check_1 = "";
        }else {
            $check_1 = "checked";
        }
        if ($model->field["OUTPUT"] == 2) {
            $out_disA = "";
        }else {
            $out_disA = "disabled";
        }
        $extra = "$check_1 \" $out_disA \";";
        $arg["data"]["HYOUSI"] = knjCreateCheckBox($objForm, "HYOUSI", 1, $extra);

        //印刷ボタンを作成する//
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する/
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM330");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm330Form1.html", $arg);
    }
}
?>
