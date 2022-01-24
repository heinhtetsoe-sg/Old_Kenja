<?php

require_once('for_php7.php');

class knjl413Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl413Form1", "POST", "knjl413index.php", "", "knjl413Form1");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR+1;

        //分類コンボ作成
        $query = knjl413Query::getRecruitClass();
        $extra = "onchange=\"return btn_submit('knjl413');\"";
        makeCmb($objForm, $arg, $db, $query, "EVENT_CLASS_CD", $model->field["EVENT_CLASS_CD"], $extra, 1);

        //イベントコンボ作成
        $query = knjl413Query::getEvent($model);
        $extra = "onchange=\"return btn_submit('knjl413');\"";
        makeCmb($objForm, $arg, $db, $query, "EVENT_CD", $model->field["EVENT_CD"], $extra, 1);

        //案内コンボ作成
        $query = knjl413Query::getRecruitSendYmst($model);
        $extra = "onchange=\"return btn_submit('knjl413');\"";
        makeCmb($objForm, $arg, $db, $query, "SEND_CD_PRGID", $model->field["SEND_CD_PRGID"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //再印刷チェックボックス
        $extra  = ($model->field["REPRINT"] == "1") ? "checked" : "";
        $extra .= " id=\"REPRINT\"";
        $arg["data"]["REPRINT"] = knjCreateCheckBox($objForm, "REPRINT", "1", $extra, "");

        //送付日付作成
        $model->field["SEND_DATE"] = $model->field["SEND_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["SEND_DATE"];
        $arg["data"]["SEND_DATE"] = View::popUpCalendar($objForm, "SEND_DATE", $model->field["SEND_DATE"]);

        //送付方法コンボ作成
        $query = knjl413Query::getL403();
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "SEND_METHOD", $model->field["SEND_METHOD"], $extra, 1);

        //radio
        if ($model->field["SEND_METHOD"] == "02") {
            $opt = array(1, 2);
            $model->field["PRINT_GUARDNAME"] = ($model->field["PRINT_GUARDNAME"] == "") ? "1" : $model->field["PRINT_GUARDNAME"];
            $extra = array();
            foreach($opt as $key => $val) {
                array_push($extra, " id=\"PRINT_GUARDNAME{$val}\" ");
            }
            $radioArray = knjCreateRadio($objForm, "PRINT_GUARDNAME", $model->field["PRINT_GUARDNAME"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //更新→印刷
        if ($model->cmd == "print") {
            $arg["reload"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl413Form1.html", $arg);
    }
}
//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();

    //生徒一覧取得
    $optR = $optL = array();
    $query = knjl413Query::getRecruitDat();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //hidden
        knjCreateHidden($objForm, "EMAIL_".$row["VALUE"], $row["EMAIL"]);

        if ($model->cmd != "knjl413" && in_array($row["VALUE"], $selectdata)) {
            $optL[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        } else {
            $optR[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $optR, $extra, 20);

    //対象生徒一覧
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $optL, $extra, 20);

    //対象取消ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象削除ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $prg = "";
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
function makeBtn(&$objForm, &$arg, $model) {
    if ($model->field["SEND_METHOD"] == "01") {
        //更新・メールボタン
        $extra = "onclick=\"return mail_send('');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "更新／メール作成", $extra);
    } else {
        //更新・印刷ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", $model->field["SEND_METHOD"] == "03" ? "更新" : "更新／印刷／プレビュー", $extra);
    }
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "REPRINT", $model->field["REPRINT"]);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR+1);
    knjCreateHidden($objForm, "SEND_CD");
    knjCreateHidden($objForm, "PRGID");
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
?>
