<?php

require_once('for_php7.php');


class knja113Ikkatsu
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("Ikkatsu", "POST", "knja113index.php", "", "Ikkatsu");

        //DB接続
        $db = Query::dbCheckOut();

        //カレンダー呼び出し
        $my = new mycalendar();

        //校種表示
        $arg["data"]["SCHOOL_KIND"] = $db->getOne(knja113Query::getSchKind($model, $model->schoolkind));

        //交付種別表示
        $arg["data"]["SCHOLARSHIP"] = $db->getOne(knja113Query::getScholarshipMst($model, $model->schoolkind, $model->scholarship));

        //開始日付
        $arg["data"]["FROM_DATE"] = str_replace("\n", "", $my->MyMonthWin2($objForm, "FROM_DATE", $model->field["FROM_DATE"]));

        //終了日付
        $arg["data"]["TO_DATE"] = str_replace("\n", "", $my->MyMonthWin2($objForm, "TO_DATE", $model->field["TO_DATE"]));

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $model->field["REMARK"], "REMARK", 50, 50, $extra);

        //リストtoリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolkind);
        knjCreateHidden($objForm, "SCHOLARSHIP", $model->scholarship);
        knjCreateHidden($objForm, "GRADE", $model->grade);
        knjCreateHidden($objForm, "GRADE_HR_CLASS", $model->grade_hr_class);
        knjCreateHidden($objForm, "CLUBCD", $model->clubcd);
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "RIGHT_FRAME", "Ikkatsu");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if ((VARS::post("cmd") == "Ikkatsu" || $model->cmd == "Ikkatsu2") && !isset($model->warning)) {
            $arg["reload"] = "window.open('knja113index.php?cmd=list&SCHOOL_KIND=".$model->schoolkind."&SCHOLARSHIP=".$model->scholarship."&GRADE=".$model->grade."&GRADE_HR_CLASS=".$model->grade_hr_class."&CLUBCD=".$model->clubcd."&RIGHT_FRAME=Ikkatsu','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja113Ikkatsu.html", $arg);
    }
}

//生徒一覧リスト作成
function makeListToList(&$objForm, &$arg, $db, &$model)
{
    $selectdata = (strlen($model->selectdata)) ? explode(",", $model->selectdata) : array();

    //生徒一覧取得
    $opt_right = $opt_left = array();
    $query = knja113Query::getSchList($model);
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
