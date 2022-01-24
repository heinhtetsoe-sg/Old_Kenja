<?php

require_once('for_php7.php');


class knjj120Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //フォーム作成
        //$arg["start"] = $objForm->get_start("knjj120Form1", "POST", "knjj120index.php", "", "knjj120Form1");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        if($model->cmd == "clear") $model->field = array();
        if($model->cmd != "change") {
            $schkind     = $model->field["SCHKIND"];
            $clubcd      = $model->field["CLUBCD"];
            $schkind2    = $model->field["SCHKIND2"];
            $detail_date = $model->field["DETAIL_DATE"];
            $meet_name   = $model->field["MEET_NAME"];
            $model->field = array();
            $model->field["SCHKIND"]     = $schkind;
            $model->field["CLUBCD"]      = $clubcd;
            $model->field["SCHKIND2"]    = $schkind2;
            $model->field["DETAIL_DATE"] = $detail_date;
            $model->field["MEET_NAME"]   = $meet_name;
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1" && $model->Properties["useClubMultiSchoolKind"] != "1") {
            $arg["schkind"] = "1";
            $query = knjj120Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjj120');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        //部クラブコンボボックスを作成する
        $query = knjj120Query::getClubList($model);
        $extra = "onChange=\"return btn_submit('knjj120');\"";
        makeCmb($objForm, $arg, $db, $query, "CLUBCD", $model->field["CLUBCD"], $extra, "1");

        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            //部活動校種取得
            $model->field["MULTI_SCHKIND"] = $db->getOne(knjj120Query::getClubMultiSchoolKind($model));

            //校種コンボ
            $arg["schkind2"] = "1";
            $query = knjj120Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjj120');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND2", $model->field["SCHKIND2"], $extra, 1);
        }

        //生徒一覧リスト
        makeStudentList($objForm, $arg, $db, $model);

        //日付
        $model->field["DETAIL_DATE"] = ($model->field["DETAIL_DATE"]) ? str_replace("-", "/", $model->field["DETAIL_DATE"]) : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DETAIL_DATE"] = View::popUpCalendar($objForm, "DETAIL_DATE", $model->field["DETAIL_DATE"]);

        //大会コンボ
        $query = knjj120Query::getMeetList($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "MEETLIST", $model->field["MEETLIST"], $extra, 1, 1);

        //大会反映ボタン
        $arg["button"]["btn_refl"] = knjCreateBtn($objForm, "btn_refl", "反 映", "onclick=\"return refl('');\"");

        //大会名称テキストボックス
        $arg["data"]["MEET_NAME"] = knjCreateTextBox($objForm, $model->field["MEET_NAME"], "MEET_NAME", 60, 60, "");

        //区分ラジオボタン 1:個人 2:団体
        $opt_div = array(1, 2);
        $model->field["DIV"] = ($model->field["DIV"] == "") ? "1" : $model->field["DIV"];
        $extra = array("id=\"DIV1\"", "id=\"DIV2\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //開催地域コンボ作成
        $query = knjj120Query::getClubHost($model);
        makeCmb($objForm, $arg, $db, $query, "HOSTCD", $model->field["HOSTCD"], "", 1, 1);

        //種目コンボ作成
        $extra = "onchange=\"return btn_submit('change')\"";
        $query = knjj120Query::getClubItem($model, $model->field["CLUBCD"]);
        makeCmb($objForm, $arg, $db, $query, "ITEMCD", $model->field["ITEMCD"], $extra, 1, 1);

        //種目種類コンボ作成
        $extra = ($model->field["ITEMCD"] == "") ? "disabled" : "";
        $query = knjj120Query::getClubItemKind($model, $model->field["ITEMCD"]);
        makeCmb($objForm, $arg, $db, $query, "KINDCD", $model->field["KINDCD"], $extra, 1, 1);

        //成績コンボ作成
        $query = knjj120Query::getClubRecord($model);
        makeCmb($objForm, $arg, $db, $query, "RECORDCD", $model->field["RECORDCD"], "", 1, 1);

        //記録テキストボックス
        $arg["data"]["DOCUMENT"] = knjCreateTextBox($objForm, $model->field["DOCUMENT"], "DOCUMENT", 40, 40, "");

        //備考テキストボックス
        $arg["data"]["DETAIL_REMARK"] = knjCreateTextBox($objForm, $model->field["DETAIL_REMARK"], "DETAIL_REMARK", 40, 40, "");

        //印刷ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["start"] = $objForm->get_start("knjj120Form1", "POST", "knjj120index.php", "", "knjj120Form1");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj120Form1.html", $arg);
    }
}

//生徒一覧リスト作成
function makeStudentList(&$objForm, &$arg, $db, &$model)
{
    $right = $left = array();
    //一覧リストを作成する
    $query = knjj120Query::getClubMember($model, "right");
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $right[] = array('label' => $row["LABEL"],
                         'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧リストを作成する
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["STUDENT_NAME"] = knjCreateCombo($objForm, "STUDENT_NAME", "", $right, $extra, 20);

    //対象リストを作成する
    $query = knjj120Query::getClubMember($model, "left");
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $left[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $left = ($model->field["SCHREGNO"]) ? $left : array();

    //対象リストを作成する
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["STUDENT_SELECTED"] = knjCreateCombo($objForm, "STUDENT_SELECTED", "", $left, $extra, 20);

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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
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

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //表彰状添付関連ボタン
    //選択ボタン
    $arg["data"]["FILESEL"] = knjCreateFile($objForm, "FILESEL", "", 512000);

    //追加ボタン
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR.'/04/01');
        knjCreateHidden($objForm, "CHK_EDATE", (CTRL_YEAR+1).'/03/31');
}
?>
