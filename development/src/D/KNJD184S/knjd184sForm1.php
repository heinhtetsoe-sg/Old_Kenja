<?php

require_once('for_php7.php');


class knjd184sForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd184sForm1", "POST", "knjd184sindex.php", "", "knjd184sForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd184sQuery::getSemester($getCountsemester);
        $extra = "onchange=\"return btn_submit('knjd184s');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //表示指定ラジオボタン 1:クラス 2:個人
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knjd184s')\"", "id=\"DISP2\" onClick=\"return btn_submit('knjd184s')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["DISP"] == 1) {
            //学年コンボ
            $query = knjd184sQuery::getGrade($model);
            $extra = "onChange=\"return btn_submit('knjd184s');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        } else if ($model->field["DISP"] == 2) {
            //クラスコンボ作成
            $query = knjd184sQuery::getHrClass(CTRL_YEAR, $seme, $model);
            $extra = "onchange=\"return btn_submit('knjd184s');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        }

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", str_replace("-", "/", $model->field["DATE"]));


        //修了日付初期値セット
        $lastSemester = $db->getOne(knjd184sQuery::getLastSemester($model));  //当年度の最終学期を取得
        $lastSemester = $lastSemester == "" ? "3" : $lastSemester;  //DB登録が無いなら、3学期を最終学期とする。
        $extraDisabled = "";
        if ($model->field["SEMESTER"] != $lastSemester) {
            $extraDisabled = " disabled ";
        }
        if ($model->field["CERTIF_DATE"] == "") {
            $cdateWk = $db->getOne(knjd184sQuery::getCertifDate($model));
            if ($cdateWk == "") {
                $model->field["CERTIF_DATE"] = str_replace("-", "/", CTRL_DATE);
            } else {
                $model->field["CERTIF_DATE"] = str_replace("-", "/", $cdateWk);
            }
        }
        //修了日付
        $arg["data"]["CERTIF_DATE"] = View::popUpCalendarAlp($objForm, "CERTIF_DATE", str_replace("-", "/", $model->field["CERTIF_DATE"]), $extraDisabled);

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
        View::toHTML($model, "knjd184sForm1.html", $arg);
    }
}

function makeListToList(&$objForm, &$arg, $db, $model, $seme) {

    //表示切替
    if ($model->field["DISP"] == 2) {
        $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
        $arg["data"]["TITLE_RIGHT"] = "生徒一覧";
    } else {
        $arg["data"]["TITLE_LEFT"]  = "出力対象クラス";
        $arg["data"]["TITLE_RIGHT"] = "クラス一覧";
    }

    //対象外の生徒取得
    $query = knjd184sQuery::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //初期化
    $opt_right = $opt_left = array();

    //年組取得
    $query = knjd184sQuery::getHrClass(CTRL_YEAR, $seme, $model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->field["DISP"] != 2) {
            //一覧リスト（右側）
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //対象者リストを作成する
    if ($model->field["DISP"] == 2) {
        $query = knjd184sQuery::getStudent($model, $seme);
        $result = $db->query($query);
        $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
            if (in_array($row["SCHREGNO"], $selectdata)) {
                $opt_left[] = array('label' => $row["SCHREGNO_SHOW"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]);
            } else {
                $opt_right[] = array('label' => $row["SCHREGNO_SHOW"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();
    }

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
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJD184S");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}

?>