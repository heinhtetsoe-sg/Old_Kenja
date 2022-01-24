<?php

require_once('for_php7.php');


class knjm310dForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm310dForm1", "POST", "knjm310dindex.php", "", "knjm310dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //ラジオボタンを作成する
        $extra = "";
        $arg["data"] = knjCreateRadio($objForm, 'PRINT_DIV', '1', $extra, '', 2);

        //学期コンボボックス作成
        $extra = "";
        $opt2 = array();
        $opt2[] = array('label' => "全て", 'value' => "8");  //9は、既にSEMESTER_MSTで利用されているため、8を利用。
        $query = knjm310dQuery::getSemesterName();
        makeCmb($objForm, $arg, $db, $query, $model->field["SETSEMESTER"], "SETSEMESTER", $extra, 1, $opt2);

        //年度テキストを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, 'btn_print', 'プレビュー／印刷', $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, 'DBNAME', DB_DATABASE);

        knjCreateHidden($objForm, 'PRGID', 'KNJM310D');

        knjCreateHidden($objForm, 'SEMESTER', CTRL_SEMESTER);

        knjCreateHidden($objForm, 'cmd');

        knjCreateHidden($objForm, 'YEAR', CTRL_YEAR);

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm310dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $opt2=null, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    if (!is_null($opt) && is_array($opt)) {
        $opt = array_merge($opt, $opt2);
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
