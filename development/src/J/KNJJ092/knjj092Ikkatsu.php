<?php

require_once('for_php7.php');


class knjj092Ikkatsu {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"] = $objForm->get_start("Ikkatsu", "POST", "knjj092index.php", "", "Ikkatsu");

        //クラスコンボボックスを作成する
        $query = knjj092Query::getHrClass($model);
        $extra = "onChange=\"return btn_submit('Ikkatsu');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, "1");

        //委員会（左側で選ばれた委員会に合わせる）
        $queryN = knjj092Query::getCommitteeNameList($model);
        $value_flg = false;
        $resultN = $db->query($queryN);
        while ($rowN = $resultN->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optN[] = array('label' => $rowN["COMMITTEENAME"],
                            'value' => $rowN["COMMITTEECD"]);
            if ($model->SelectCommittee === $rowN["COMMITTEECD"]) $value_flg = true;
        }
        $resultN->free();

        $selectcommittee = ($model->SelectCommittee && $value_flg) ? $model->SelectCommittee : $optN[0]["value"];
        //委員会コード
        $model->field["COMMITTEE_IKKATSU"] = ($model->field["COMMITTEE_IKKATSU"] == "") ? $selectcommittee: $model->field["COMMITTEE_IKKATSU"];
        knjCreateHidden($objForm, "COMMITTEE_IKKATSU", $model->field["COMMITTEE_IKKATSU"]);

        //生徒一覧リスト
        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        //更新ボタン
        $auth = common::SecurityCheck(STAFFCD, PROGRAMID);
        $extra = ($auth == DEF_UPDATABLE || $auth == DEF_UPDATE_RESTRICT) ? "onclick=\"return doSubmit();\"" : "disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //戻るボタン
        $extra = "onclick=\"return btn_submit('edit');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COMMITTEECODEALL", $model->field["COMMITTEECODEALL"]);
        knjCreateHidden($objForm, "selectdata");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "ikkatsu_Insert") {
            $arg["reload"] = "window.open('knjj092index.php?cmd=list&RELOADCOMMITTEE=".$selectcommittee."','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj092Ikkatsu.html", $arg);
    }
}

//生徒一覧リスト作成
function makeStudentList(&$objForm, &$arg, $db, &$model) {
    //生徒対象者一覧リストを作成する
    $leftList = array();
    $query = knjj092Query::getSchregLeftList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $leftList[]  = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
        $left_schregno[] = $row["VALUE"];
    }
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["STUDENT_SELECTED"] = knjCreateCombo($objForm, "STUDENT_SELECTED", "", $leftList, $extra, 20);

    //対象生徒リストを作成する
    $rightList = array();
    $query = knjj092Query::getSchregRightList($model, $left_schregno);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $rightList[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["STUDENT_NAME"] = knjCreateCombo($objForm, "STUDENT_NAME", "", $rightList, $extra, 20);

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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
