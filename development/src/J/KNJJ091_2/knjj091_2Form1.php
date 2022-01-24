<?php

require_once('for_php7.php');


class knjj091_2Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjj091_2Form1", "POST", "knjj091_2index.php", "", "knjj091_2Form1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjj091_2Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjj091_2');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        //クラスコンボボックスを作成する
        $query = knjj091_2Query::getHrClass($model);
        $extra = "onChange=\"return btn_submit('knjj091_2');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, "1");

        //生徒一覧リスト
        makeStudentList($objForm, $arg, $db, $model);

        //学期
        $query = knjj091_2Query::getSemester();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //委員会区分コンボボックスを作成する
        $query = knjj091_2Query::getCommitteeFlg();
        $extra = "onChange=\"btn_submit('knjj091_2')\"";
        makeCmb($objForm, $arg, $db, $query, "COMMITTEE_FLG", $model->field["COMMITTEE_FLG"], $extra, "1");

        //委員会コンボボックスを作成する
        $query = knjj091_2Query::getCommitteeCd($model);
        makeCmb($objForm, $arg, $db, $query, "COMMITTEECD", $model->field["COMMITTEECD"], "", "1");

        //役職コンボボックスを作成する
        $query = knjj091_2Query::getNameMst("J002");
        makeCmb($objForm, $arg, $db, $query, "EXECUTIVECD", $model->field["EXECUTIVECD"], "", "1");

        //印刷ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj091_2Form1.html", $arg);
    }
}

//生徒一覧リスト作成
function makeStudentList(&$objForm, &$arg, $db, &$model)
{
    $row1 = $row2 = array();
    //一覧リストを作成する
    $query = knjj091_2Query::getStudent($model, "1");
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["STUDENT_NAME"] = knjCreateCombo($objForm, "STUDENT_NAME", "", $row1, $extra, 20);

    //出力対象教科リストを作成する
    $query = knjj091_2Query::getStudent($model, "2");
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row2[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["STUDENT_SELECTED"] = knjCreateCombo($objForm, "STUDENT_SELECTED", "", $row2, $extra, 20);

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

    if ($name != "GRADE_HR_CLASS" && $name != 'COMMITTEE_FLG' && $name != 'SEMESTER' && $name != 'SCHKIND') {
        $opt[] = array('label' => "", 'value' => "");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = (($value != "") && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return doSubmit();\"" : "disabled";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "更 新", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '戻 る', $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
}
?>
