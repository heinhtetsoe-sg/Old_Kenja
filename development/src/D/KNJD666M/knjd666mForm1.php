<?php

require_once('for_php7.php');


class knjd666mForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd666mForm1", "POST", "knjd666mindex.php", "", "knjd666mForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd666mQuery::getSemester();
        $extra = "onchange=\"return btn_submit('chgSeme');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //校種コンボ作成
        $query = knjd666mQuery::getSchoolKind();
        $extra = "onchange=\"return btn_submit('chgSeme');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //クラスコンボ作成
        $query = knjd666mQuery::getHrClass(CTRL_YEAR, $model->field["SEMESTER"], $model->field["SCHOOL_KIND"]);
        $extra = "onchange=\"return btn_submit('knjd666m');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //校種コンボ作成
        $query = knjd666mQuery::getTestCd(CTRL_YEAR, $model->field["SEMESTER"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1);

        //リストToリスト作成 生徒
        makeListToList($objForm, $arg, $db, $model, $model->field["SEMESTER"]);

        //リストToリスト作成 科目
        makeListToListSub($objForm, $arg, $db, $model, $model->field["SEMESTER"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd666mForm1.html", $arg);
    }
}

function makeListToList(&$objForm, &$arg, $db, $model, $seme)
{
    //初期化
    $opt_right = $opt_left = array();

    //対象者リストを作成する
    $query = knjd666mQuery::getStudent($model, $seme);
    $result = $db->query($query);
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["SCHREGNO"], $selectdata)) {
            $opt_left[] = array('label' => $row["SCHREGNO_SHOW"]." ".$row["ATTENDNO"]."番"." ".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]);
        } else {
            $opt_right[] = array('label' => $row["SCHREGNO_SHOW"]." ".$row["ATTENDNO"]."番"." ".$row["NAME_SHOW"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);
        
    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

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

function makeListToListSub(&$objForm, &$arg, $db, $model, $seme)
{

    //初期化
    $opt_right_sub = $opt_left_sub = array();

    //対象者リストを作成する
    $query = knjd666mQuery::getSubclass(CTRL_YEAR, $seme, $model->field["GRADE_HR_CLASS"], $model);
    $result = $db->query($query);
    $selectdata = ($model->selectdataSub) ? explode(',', $model->selectdataSub) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selectdata)) {
            $opt_left_sub[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        } else {
            $opt_right_sub[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1sub('left')\"";
    $arg["data"]["CATEGORY_NAME_SUB"] = knjCreateCombo($objForm, "category_name_sub", "", $opt_right_sub, $extra, 20);
        
    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1sub('right')\"";
    $arg["data"]["CATEGORY_SELECTED_SUB"] = knjCreateCombo($objForm, "CATEGORY_SELECTED_SUB", "", $opt_left_sub, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"movessub('right');\"";
    $arg["button"]["btn_rights_sub"] = knjCreateBtn($objForm, "btn_rights_sub", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"movessub('left');\"";
    $arg["button"]["btn_lefts_sub"] = knjCreateBtn($objForm, "btn_lefts_sub", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1sub('right');\"";
    $arg["button"]["btn_right1_sub"] = knjCreateBtn($objForm, "btn_right1_sub", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1sub('left');\"";
    $arg["button"]["btn_left1_sub"] = knjCreateBtn($objForm, "btn_left1_sub", "＜", $extra);
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

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
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

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJD666M");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataSub");
}
