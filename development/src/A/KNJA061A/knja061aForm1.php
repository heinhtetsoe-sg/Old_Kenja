<?php

require_once('for_php7.php');


class knja061aForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja061aForm1", "POST", "knja061aindex.php", "", "knja061aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $year = ($model->field["DISP"] == "2") ? CTRL_YEAR + 1 : CTRL_YEAR;

        //年度
        $arg["data"]["YEAR"] = $year;

        //表示指定ラジオボタン 1:在校生 2:新入生
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knja061a')\"", "id=\"DISP2\" onClick=\"return btn_submit('knja061a')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;


        if ($model->field["DISP"] == 1) {
            $arg["data"]["GRADE"] = "1";
            $arg["data"]["GRADE_HR_CLASS"] = "1";

            //校種コンボ
            $query = knja061aQuery::getSchkind($model);
            $extra = "onChange=\"return btn_submit('knja061a');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

            //学年コンボ
            $query = knja061aQuery::getGrade($model);
            $extra = "onChange=\"return btn_submit('knja061a');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
            //クラスコンボ作成
            $query = knja061aQuery::getHrClass(CTRL_YEAR, CTRL_SEMESTER, $model);
            $extra = "onchange=\"return btn_submit('knja061a');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

            //区分コンボ 1:寮生 2:通学生 3:全て
            $opt = array();
            $opt[] = array("label" => "寮生",   "value" => "1");
            $opt[] = array("label" => "通学生", "value" => "2");
            $opt[] = array("label" => "全て",   "value" => "ALL");
            $extra = "";
            $extra = "onChange=\"return btn_submit('knja061a');\"";
            $model->field["OUTPUT"] = $model->field["OUTPUT"] ? $model->field["OUTPUT"] : "1";
            $arg["data"]["OUTPUT"] = knjCreateCombo($objForm, "OUTPUT", $model->field["OUTPUT"], $opt, $extra, 1);
        } else {
            //課程コンボ
            $query = knja061aQuery::getCourse($year);
            $extra = "onChange=\"return btn_submit('knja061a');\"";
            makeCmb($objForm, $arg, $db, $query, "COURSECD", $model->field["COURSECD"], $extra, 1);
        }

        //性別コンボ
        $model->field["SEX"] = $model->field["SEX"] ? $model->field["SEX"] : "1";
        $query = knja061aQuery::getNameMst("Z002");
        $extra = "";
        $extra = "onChange=\"return btn_submit('knja061a');\"";
        makeCmb($objForm, $arg, $db, $query, "SEX", $model->field["SEX"], $extra, 1);


        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, CTRL_SEMESTER);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $year);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja061aForm1.html", $arg);
    }
}

function makeListToList(&$objForm, &$arg, $db, $model, $seme) {

    //表示切替
    $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
    $arg["data"]["TITLE_RIGHT"] = "生徒一覧";

    //初期化
    $opt_right = $opt_left = array();

    //在校生
    $query = knja061aQuery::getStudent($model, $seme);
    $result = $db->query($query);
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["SCHREGNO"], $selectdata)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["SCHREGNO"]);
        } else {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();

    //新入生
    if ($model->field["DISP"] == 2) {
        $opt_right = $opt_left = array();
        $query = knja061aQuery::getFreshman($model);
        $result = $db->query($query);
        $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["SCHREGNO"], $selectdata)) {
                $opt_left[] = array('label' => $row["LABEL"],
                                    'value' => $row["SCHREGNO"]);
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();
    }

    //一覧リスト（右）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    if ($blank == "ALL") $opt[] = array("label" => "全て", "value" => "ALL");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if($name == "SEX") {
        $opt[] = array("label" => "全て", "value" => "ALL");
        if ($value == "ALL") $value_flg = true;
    }

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

function makeHidden(&$objForm, $model, $year) {
    knjCreateHidden($objForm, "YEAR", $year);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJA061A");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
}
?>
