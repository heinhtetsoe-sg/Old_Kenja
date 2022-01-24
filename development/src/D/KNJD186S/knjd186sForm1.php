<?php

require_once('for_php7.php');
class knjd186sForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd186sForm1", "POST", "knjd186sindex.php", "", "knjd186sForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd186sQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd186s'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //クラスコンボ作成
        $query = knjd186sQuery::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('knjd186s'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //出欠集計開始日付
        $date = ($model->field["SDATE"]) ? $model->field["SDATE"] : $model->control["学期開始日付"]["9"];
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $date);

        //出欠集計終了日付
        $date = ($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $date);

        //出欠集計単位コンボ作成
        $query = knjd186sQuery::getBikoTermTypeList($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "BIKO_TERM_TYPE", $model->field["BIKO_TERM_TYPE"], $extra, 1);

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd186sForm1.html", $arg); 
    }
}

//リストtoリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model) {
    //対象外の生徒取得
    $query = knjd186sQuery::getSchnoIdou($model);
    $result = $db->query($query);
    $opt_idou = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //対象者リストを作成する
    $query = knjd186sQuery::getSchno($model);
    $result = $db->query($query);
    $opt_right = $opt_left = array();
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if (in_array($row["SCHREGNO"], $selectdata)) {
            $opt_left[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]);
        } else {
            $opt_right[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();

    //生徒一覧を作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

    //出力対象一覧を作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

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

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
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
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJD186S");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");

    knjCreateHidden($objForm, "HREPORTREMARK_DAT_REMARK1_SIZE_J", $model->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_J"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_REMARK2_SIZE_J", $model->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_J"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J", $model->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J", $model->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_COMMUNICATION_SIZE_J", $model->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
}
?>
