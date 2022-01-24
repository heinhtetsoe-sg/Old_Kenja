<?php

require_once('for_php7.php');


class knjp952Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp952Form1", "POST", "knjp952index.php", "", "knjp952Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["CTRL_YEAR"] = CTRL_YEAR;

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp952Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('knjp952');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1, "");

        //収入科目コンボボックス
        $query = knjp952Query::getLMdat($model);
        $extra = "onchange=\"return btn_submit('knjp952');\"";
        makeCmb($objForm, $arg, $db, $query, "OUTGO_L_M_CD", $model->field["OUTGO_L_M_CD"], $extra, 1, "BLANK");

        //伝票番号コンボボックス
        $query = knjp952Query::getRequestNo($model);
        makeCmb($objForm, $arg, $db, $query, "REQUEST_NO", $model->field["REQUEST_NO"], "", 1, "BLANK");

        //checkbox
        $extra = " id=\"CHANGE_CLASS\" ";
        if ($model->field["CHANGE_CLASS"] == "1") {
            $extra .= " checked ";
        }
        $arg["data"]["CHANGE_CLASS"] = knjCreateCheckBox($objForm, "CHANGE_CLASS", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp952Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJP952");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "KNJP952_PrintStdNameMeisai", $model->Properties["KNJP952_PrintStdNameMeisai"]);
    knjCreateHidden($objForm, "KNJP952_PrintStdCntMeisai", $model->Properties["KNJP952_PrintStdCntMeisai"]);
    knjCreateHidden($objForm, "useLevyIraisyo", $model->Properties["useLevyIraisyo"]);
}

?>
