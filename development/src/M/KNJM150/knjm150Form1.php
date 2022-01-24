<?php

require_once('for_php7.php');

class knjm150Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm150Form1", "POST", "knjm150index.php", "", "knjm150Form1");

        //DB接続
        $db = Query::dbCheckOut();

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
        $query = knjm150Query::getSubclass($model);
        $result = $db->query($query);
        $opt_subclass = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_subclass[] = array('label' => $row["SUBCLASSCD"]."  ".$row["SUBCLASSABBV"],
                                    'value' => $row["SUBCLASSCD"]);

        }
        $result->free();

        if (!$model->field["KAMOKU"]) {
            $model->field["KAMOKU"] = $opt_subclass[0]["value"];
        }
        if ($model->field["OUTPUT"] != 2) {
            $extrahtml = "disabled";
        }
        $extra = $extrahtml;
        $arg["data"]["KAMOKU"] = knjCreateCombo($objForm, "KAMOKU", $model->field["KAMOKU"], $opt_subclass, $extra, 1);

        //印刷ボタンを作成する//
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する/
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM150");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm150Form1.html", $arg);
    }
}
?>
