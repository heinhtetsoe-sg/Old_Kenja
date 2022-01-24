<?php

require_once('for_php7.php');


class knja321Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja321Form1", "POST", "knja321index.php", "", "knja321Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度・学期
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　　".CTRL_SEMESTERNAME;

        //クラス選択コンボ
        $query = knja321Query::getGradeHrclass($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "");

        //学年選択コンボ
        $query = knja321Query::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "");

        //radio
        $opt = array(1, 2, 3);
        $model->field["CSV_DIV"] = ($model->field["CSV_DIV"] == "") ? "1" : $model->field["CSV_DIV"];
        $extra = array("id=\"CSV_DIV1\" onclick=\"return btn_submit('knja321');\"", "id=\"CSV_DIV2\" onclick=\"return btn_submit('knja321');\"", "id=\"CSV_DIV3\" onclick=\"return btn_submit('knja321');\"");
        $radioArray = knjCreateRadio($objForm, "CSV_DIV", $model->field["CSV_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        if ($model->field["CSV_DIV"] == '1') {
        	$arg['flag']['csv1'] = true;
        } else {
        	$arg['flag']['csv2'] = true;
        }

        //テンプレートダウンロードボタンを作成する
        $extra = "onclick=\"return btn_submit('template_dl');\"";
        $arg["button"]["btn_download"] = knjCreateBtn($objForm, "btn_download", "テンプレートダウンロード", $extra);

        //csvボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "ＣＳＶ出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja321Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
