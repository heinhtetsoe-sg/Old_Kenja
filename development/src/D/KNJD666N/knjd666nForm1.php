<?php

require_once('for_php7.php');


class knjd666nForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd666nForm1", "POST", "knjd666nindex.php", "", "knjd666nForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd666nQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd666n');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //テスト種別コンボ
        $extra = "onChange=\"return btn_submit('knjd666n');\"";
        $query = knjd666nQuery::getTestitem($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD", $model->field["TESTKIND_ITEMCD"], $extra, 1);

        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjd666n');\"";
        $query = knjd666nQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjd666nQuery::getSchoolKind($model));

        //コースコンボ
        //※この学校はCOURSECD、MAJORCDが1種しかない前提なので、テスト時にコンボに重複表示される可能性があるので注意。
        $extra = "onChange=\"return btn_submit('knjd666n');\"";
        $query = knjd666nQuery::getCourseCode($model);
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->field["COURSECODE"], $extra, 1, "ALL");

        //出力順ラジオボタン 1:組番号順 2:10段階評価平均順 3:学年順位 4:コース順位
        $opt_disp = array(1, 2, 3, 4);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        //$extra = array("id=\"SORT1\" onClick=\"return btn_submit('knjd666n')\"", "id=\"SORT2\" onClick=\"return btn_submit('knjd666n')\"" , "id=\"SORT3\" onClick=\"return btn_submit('knjd666n')\"" , "id=\"SORT4\" onClick=\"return btn_submit('knjd666n')\"");
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"" , "id=\"SORT3\"" , "id=\"SORT4\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $seme);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd666nForm1.html", $arg);
    }
}

function makeListToList(&$objForm, &$arg, $db, $model, $seme) {

    //表示切替
    $arg["data"]["TITLE_LEFT"]  = "出力対象クラス";
    $arg["data"]["TITLE_RIGHT"] = "クラス一覧";

    //初期化
    $opt_right = $opt_left = array();

    //年組取得
    $query = knjd666nQuery::getHrClass(CTRL_YEAR, $seme, $model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //一覧リスト（右側）
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
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

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
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
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolkind);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJD666N");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
}

?>
