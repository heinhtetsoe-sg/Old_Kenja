<?php

require_once('for_php7.php');


class knjb1300Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjb1300Form1", "POST", "knjb1300index.php", "", "knjb1300Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjb1300Query::getSemester();
        $extra = "onchange=\"return btn_submit('knjb1300');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ作成
        $query = knjb1300Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "", 1);

        //帳票出力ラジオボタン 1:講座名簿 2:履修科目名簿
        $opt_output = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //対象日付作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //履修履歴コンボ
        $query = knjb1300Query::getRirekiCode($model);
        makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE", $model->field["RIREKI_CODE"], "", 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1300Form1.html", $arg);
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
    if ($name == 'GRADE') {
        $opt[] = array('label' => '全て',
                       'value' => 99);
    }

    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJB1300");
    knjCreateHidden($objForm, "STAFFCD", STAFFCD);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

    $semes = $db->getRow(knjb1300Query::getSemeDate($model), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", $semes["SDATE"]);
    knjCreateHidden($objForm, "EDATE", $semes["EDATE"]);
}
?>
