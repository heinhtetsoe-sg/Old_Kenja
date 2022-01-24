<?php

require_once('for_php7.php');

class knjl351wForm1
{
    function main(&$model)
    {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl351windex.php", "", "main");

        //権限チェック
        $adminFlg = knjl351wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1") {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut2();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"] = $model->entexamYear;
        $requestroot = REQUESTROOT;

        //radio
        $opt = array(1, 2, 3);
        $model->field["OUTPUT_DIV"] = ($model->field["OUTPUT_DIV"] == "") ? "1" : $model->field["OUTPUT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT_DIV{$val}\" onclick=\"Page_jumper('{$requestroot}');\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //入試制度コンボ
        $query = knjl351wQuery::get_name_cd($model->entexamYear, "L003");
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分
        $query = knjl351wQuery::get_name_cdAft($model, "L004", "TESTDIV");
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "");

        //累計区分
        $query = knjl351wQuery::get_name_cdAft($model, "L040", "RUIKEI_DIV");
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["RUIKEI_DIV"], "RUIKEI_DIV", $extra, 1, "ALL");

        //追検査
        $extra = " id=\"TESTDIV2\" onClick=\"return btn_submit('main');\"";
        $checked = $model->field["TESTDIV2"] == "1" ? " checked " : "";
        $arg["data"]["TESTDIV2"] = knjCreateCheckBox($objForm, "TESTDIV2", "1", $checked.$extra);
        $model->field["TESTDIV2"] = $model->field["TESTDIV2"] ? $model->field["TESTDIV2"] : "0";

        //学校一覧リスト
        makeSchoolList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "DBNAME2", DB_DATABASE2);
        knjCreateHidden($objForm, "YEAR", $model->entexamYear);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl351wForm1.html", $arg);
    }
}

//クラス一覧リスト作成
function makeSchoolList(&$objForm, &$arg, $db, &$model)
{
    $row1 = array();
    $query = knjl351wQuery::getSchoolData($model);
    $result = $db->query($query);
    $model->schoolArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setExeDate = $row["EXECUTE_DATE"];
        if ($setExeDate) {
            list($setDate, $setTime) = explode(".", $setExeDate);
            $dispDateTime = "【".date("Y/m/d H:i", strtotime($setDate))."】";
        } else {
            $dispDateTime = "";
        }
        $row1[] = array('label' => $row["MITEISHUTSU"].$row["EDBOARD_SCHOOLCD"].":".$row["EDBOARD_SCHOOLNAME"].$dispDateTime,
                        'value' => $row["EDBOARD_SCHOOLCD"]);
        $model->schoolArray[$row["EDBOARD_SCHOOLCD"]] = $row["EDBOARD_SCHOOLNAME"];
    }
    $result->free();

    //一覧リストを作成する
    $extra = "multiple style=\"width:580px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //出力対象教科リストを作成する
    $extra = "multiple style=\"width:580px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

    //印刷ボタン
    $extra = "onclick=\"newwin('".SERVLET_URL."');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
