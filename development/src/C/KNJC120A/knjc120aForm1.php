<?php

require_once('for_php7.php');

class knjc120aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc120aForm1", "POST", "knjc120aindex.php", "", "knjc120aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //リスト表示選択
        $opt = array(1, 2); //1:個人 2:クラス
        if (!$model->field["KUBUN"]) {
            $model->field["KUBUN"] = 2;
        }
        $onClick = " onclick =\" return btn_submit('knjc120a');\"";
        $extra = array("id=\"KUBUN1\"".$onClick, "id=\"KUBUN2\"".$onClick);
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //学年名
        $extra = "onChange=\"return btn_submit('knjc120a');\"";
        $query = knjc120aQuery::getGradeName();
        makeCmb($objForm, $arg, $db, $query, "GRADE_NAME", $model->field["GRADE_NAME"], $extra, 1);

        if ($model->field["KUBUN"] == "1") {
            $extra = "onChange=\"return btn_submit('knjc120a');\"";
            $query = knjc120aQuery::getGradeHrclassName($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HRCLASS_NAME", $model->field["GRADE_HRCLASS_NAME"], $extra, 1);
        }

        if ($model->field["KUBUN"] == 1) {
            $arg["schno"] = $model->field["KUBUN"];
        }
        if ($model->field["KUBUN"] == 2) {
            $arg["clsno"] = $model->field["KUBUN"];
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //出欠集計範囲(開始日)
        $result = $db->query(knjc120aQuery::getMonth());
        $month = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $month = $row['NAMECD2'];
        }
        $result->free();

        $model->field["START_ATTENDANCE"] = $model->field["START_ATTENDANCE"] == "" ? CTRL_YEAR."/".$month."/01" : $model->field["START_ATTENDANCE"];
        $arg["data"]["START_ATTENDANCE"] = View::popUpCalendar($objForm, "START_ATTENDANCE", $model->field["START_ATTENDANCE"]);

        //出欠集計範囲(終了日)
        $model->field["END_ATTENDANCE"] = $model->field["END_ATTENDANCE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["END_ATTENDANCE"];
        $arg["data"]["END_ATTENDANCE"] = View::popUpCalendar($objForm, "END_ATTENDANCE", $model->field["END_ATTENDANCE"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjc120aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank != "") {
        $opt[] = array("label" => "", "value" => "");
    }
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

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //表示切替
    $arg["data"]["TITLE_LEFT"] = "出力対象クラス";

    //初期化
    $opt_left = $opt_right = array();

    //年組取得
    if ($model->field["KUBUN"] == "1") {
        $arg["data"]["TITLE_RIGHT"] = "生徒一覧";
        $query = knjc120aQuery::getSchno($model);//生徒一覧取得
    } else {
        $arg["data"]["TITLE_RIGHT"] = "クラス一覧";
        $query = knjc120aQuery::getHrClass($model);//クラス一覧取得
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //一覧リスト（右側）
        $opt_right[] = array('label'=> $row["LABEL"],'value' => $row["VALUE"]);
    }
    $result->free();

    $disp = $model->field["KUBUN"];

    //一覧リスト（右）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left', $disp)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', $disp)\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $disp);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $disp);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $disp);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $disp);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJC120A");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "selectleftval");
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
}
