<?php

require_once('for_php7.php');


class knjb0052Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjb0052Form1", "POST", "knjb0052index.php", "", "knjb0052Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度　学期コンボ作成
        $query = knjb0052Query::getYearSemester("");
        $extra = "onchange=\"return btn_submit('knjb0052'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR_SEMESTER", $model->field["YEAR_SEMESTER"], $extra, 1);

        //学期の日付範囲取得
        $getDate = array();
        $query = knjb0052Query::getYearSemester($model);
        $getDate = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $getDate["SDATE"]));
        knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $getDate["EDATE"]));

        //対象日付作成
        $model->field["EXECUTEDATE"] = $model->field["EXECUTEDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EXECUTEDATE"];
        $arg["data"]["EXECUTEDATE"] = View::popUpCalendar2($objForm, "EXECUTEDATE", $model->field["EXECUTEDATE"], "reload=true", "btn_submit('knjb0052')", "");

        //生徒リストToリスト作成
        makeChairCdList($objForm, $arg, $db, $model);

        //対象ラジオボタン 1:全て 2:重複講座なし
        $opt_outDiv = array(1, 2);
        $model->field["OUT_DIV"] = ($model->field["OUT_DIV"] == "") ? "1" : $model->field["OUT_DIV"];
        $extra = array("id=\"OUT_DIV1\"", "id=\"OUT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "OUT_DIV", $model->field["OUT_DIV"], $extra, $opt_outDiv, get_count($opt_outDiv));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力順ラジオボタン 1:学籍番号 2:出席番号
        $opt_group = array(1, 2);
        $model->field["ORDER_DIV"] = ($model->field["ORDER_DIV"] == "") ? "1" : $model->field["ORDER_DIV"];
        $extra = array("id=\"ORDER_DIV1\"", "id=\"ORDER_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "ORDER_DIV", $model->field["ORDER_DIV"], $extra, $opt_group, get_count($opt_group));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //講座コンボ作成
        $query = knjb0052Query::getChairCdQuery($model, "");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "KIJIKU_CHAIRCD", $model->field["KIJIKU_CHAIRCD"], $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb0052Form1.html", $arg);
    }
}

function makeChairCdList(&$objForm, &$arg, $db, $model)
{

    //対象講座を作成する
    $query = knjb0052Query::getChairCdQuery($model, "");
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["VALUE"].":".$row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);

    //生徒一覧リストを作成する//
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "KIJIKU_CHAIRCD") {
            $opt[] = array('label' => $row["VALUE"].":".$row["LABEL"],
                           'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "YEAR_SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR.'-'.CTRL_SEMESTER;
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
    //ＣＳＶ出力ボタンを作成する
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJB0052");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectData");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
