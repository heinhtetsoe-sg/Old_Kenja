<?php

require_once('for_php7.php');


class knjd624hForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd624hForm1", "POST", "knjd624hindex.php", "", "knjd624hForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;


        //学期コンボ作成
        $query = knjd624hQuery::getSemester($model, 0);
        $extra = "onchange=\"return btn_submit('knjd624h')\"";
        
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //出力種別ラジオボタンを作成
        $opt = array(1, 2);
        $model->field["RADIO"] = ($model->field["RADIO"] == "") ? "1" : $model->field["RADIO"];
        $extra = array("id=\"RADIO1\"", "id=\"RADIO2\"");
        $radioArray = knjCreateRadio($objForm, "RADIO", $model->field["RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学年コンボ作成
        $query = knjd624hQuery::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('knjd624h')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        $model->schoolKind = $db->getOne(knjd624hQuery::getSchoolKind($model));

        //テストコンボ作成
        $query = knjd624hQuery::getTest($model, $model->field["SEMESTER"], $model->schoolKind);
        $extra = "onchange=\"return btn_submit('knjd624h')\"";
        $opt = makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1);
        if (empty($opt)) {
            $query = knjd624hQuery::getTest($model, $model->field["SEMESTER"]);
            $extra = "onchange=\"return btn_submit('knjd624h')\"";
            $opt = makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd624hForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($value && $value_flg) {
    } else if ($name == "SEMESTER") {
        $value = CTRL_SEMESTER;
    } else {
        $value = $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $opt;
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD624H");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "cmd");
    //教育課程コード
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "knjd624hDistPageIsOther", $model->Properties["knjd624hDistPageIsOther"]);
    knjCreateHidden($objForm, "knjd624hPage1Columns9", $model->Properties["knjd624hPage1Columns9"]);
    knjCreateHidden($objForm, "knjd624hStudentLines55", $model->Properties["knjd624hStudentLines55"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
}

?>
