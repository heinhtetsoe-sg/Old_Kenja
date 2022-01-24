<?php

class knjd619eForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd619eForm1", "POST", "knjd619eindex.php", "", "knjd619eForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //値の設定
        $model->field["YEAR"] = CTRL_YEAR;
        $model->field["SEMESTER"] = ($model->field["SEMESTER"] == "") ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学年コンボ
        $query = knjd619eQuery::getSelectGrade($model);
        $extra = "onChange=\"return btn_submit('knjd619e');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "ALL");

        //学期コンボボックスを作成する
        $query = knjd619eQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd619e');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //考査種別コンボ
        $query = knjd619eQuery::getTestItem($model);
        $extra = "onChange=\"return btn_submit('knjd619e');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd619eForm1.html", $arg); 
    }
}

/**************************************** 以下関数 **************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
    } else if ($blank == "blank") {
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
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //ＣＳＶボタン
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");
    //閉じるボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD619E");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

    /*** 以下、ＣＳＶ出力用 ***/
    //選択学年名を保持
    knjCreateHidden($objForm, "selectGradeName");
    //選択学期名を保持
    knjCreateHidden($objForm, "selectSemeName");
    //選択考査種別名を保持
    knjCreateHidden($objForm, "selectTestName");
}
?>
