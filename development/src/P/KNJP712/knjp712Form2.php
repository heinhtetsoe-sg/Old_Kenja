<?php

require_once('for_php7.php');

class knjp712Form2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp712index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //リストtoリスト
        makeListToList($objForm, $arg, $db, $model);

        //入金パターンコンボ
        $query = knjp712Query::getCollectPattern($model);
        makeCmb($objForm, $arg, $db, $query, "COLLECT_PATTERN_CD", $model->field["COLLECT_PATTERN_CD"], "", 1);

        //伝票日付
        if (!$model->field["SLIP_DATE"]) $model->field["SLIP_DATE"] = CTRL_DATE;
        $arg["data"]["SLIP_DATE"] = View::popUpCalendar2($objForm, "SLIP_DATE", str_replace("-","/",$model->field["SLIP_DATE"]), "", "", "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHDIV", $model->schdiv);
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolkind);
        knjCreateHidden($objForm, "COLLECT_GRP_CD", $model->collect_grp_cd);
        knjCreateHidden($objForm, "GRADE_CM", $model->grade_cm);
        knjCreateHidden($objForm, "COURSECODE", $model->coursecode);
        knjCreateHidden($objForm, "GRADE_HR_CLASS", $model->grade_hr_class);
        knjCreateHidden($objForm, "CLUBCD", $model->clubcd);
        knjCreateHidden($objForm, "DOMI_CD", $model->domi_cd);
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if ((VARS::post("cmd") == "edit" || $model->cmd == "edit2") && !isset($model->warning)) {
            $arg["reload"] = "window.open('knjp712index.php?cmd=list&SCHOOL_KIND=".$model->schoolkind."&COLLECT_GRP_CD=".$model->collect_grp_cd."&GRADE_CM=".$model->grade_cm."&COURSECODE=".$model->coursecode."&GRADE_HR_CLASS=".$model->grade_hr_class."&CLUBCD=".$model->clubcd."&DOMI_CD=".$model->domi_cd."','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp712Form2.html", $arg); 
    }
}

//生徒一覧リスト作成
function makeListToList(&$objForm, &$arg, $db, &$model) {
    $selectdata = (strlen($model->selectdata)) ? explode(",", $model->selectdata) : array();
    $opt_right = $opt_left = array();

    //生徒一覧取得
    if ($model->schdiv && $model->schoolkind && $model->collect_grp_cd && (strlen($model->grade_cm) || strlen($model->coursecode) || strlen($model->grade_hr_class) || strlen($model->clubcd) || strlen($model->domi_cd))) {
        $query = knjp712Query::getSchList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["SCHREGNO"], $selectdata)) {
                $opt_left[]  = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();
    }

    //生徒一覧（右リスト）
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["RIGHT_LIST"] = knjCreateCombo($objForm, "RIGHT_LIST", "", $opt_right, $extra, 20);

    //対象者一覧（左リスト）
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["LEFT_LIST"] = knjCreateCombo($objForm, "LEFT_LIST", "", $opt_left, $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";

    //更新ボタン
    $extra = "onClick=\"return btn_submit('update')\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra.$disable);
    //終了ボタン
    $extra = "onClick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
