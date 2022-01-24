<?php

require_once('for_php7.php');

class knje400aForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knje400aForm1", "POST", "knje400aindex.php", "", "knje400aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //教育委員会チェック
        $getFlg = $db->getOne(knje400aQuery::getNameMst());
        if ($getFlg !== '1') {
            $arg["jscript"] = "OnEdboardError();";
        }

        //年度
        $extra = "onChange=\"return btn_submit('knje400a');\"";
        $query = knje400aQuery::getYear();
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);
        //年度を変更したら、年度コンボ以外を初期化する
        if ($model->cmd === 'knje400a') {
            $model->docNumber = "";
            $model->field = array();
        }

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        //教育歴情報取得
        if ($model->cmd == "list_set"){
            if (isset($model->docNumber) && !isset($model->warning)){
                $Row = $db->getRow(knje400aQuery::getRow($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        //通達日をhiddenにセット
        knjCreateHidden($objForm, "TRANSMISSION_DATE", $Row["TRANSMISSION_DATE"]);
        if ($Row["TRANSMISSION_DATE"]) {
            $diabled = "disabled";
        } else {
            $diabled = "";
        }

        //文書番号
        $rOnlyExtra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["DOC_NUMBER"] = knjCreateTextBox($objForm, $Row["DOC_NUMBER"], "DOC_NUMBER", 5, 5, $rOnlyExtra);

        //通達文タイトル
        $extra = "";
        $arg["data"]["NOTICE_TITLE"] = knjCreateTextBox($objForm, $Row["NOTICE_TITLE"], "NOTICE_TITLE", 30, 60, $extra);

        //通達文
        $extra = "";
        $arg["data"]["NOTICE_MESSAGE"] = KnjCreateTextArea($objForm, "NOTICE_MESSAGE", 5, 83, "soft", $extra, $Row["NOTICE_MESSAGE"]);

        //掲載期間開始
        $arg["data"]["VIEWING_PERIOD_FROM"] = View::popUpCalendar($objForm, "VIEWING_PERIOD_FROM", str_replace("-","/",$Row["VIEWING_PERIOD_FROM"]));

        //掲載期間終了
        $arg["data"]["VIEWING_PERIOD_TO"] = View::popUpCalendar($objForm, "VIEWING_PERIOD_TO", str_replace("-","/",$Row["VIEWING_PERIOD_TO"]));

        //提出期限
        $arg["data"]["SUBMISSION_DATE"] = View::popUpCalendar($objForm, "SUBMISSION_DATE", str_replace("-","/",$Row["SUBMISSION_DATE"]));
        
        //回答要チェックボックス
        $extra = "id=\"REQUEST_ANSWER_FLG\" onClick=\"prgDisabled(this)\"";
        if ($Row["REQUEST_ANSWER_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["REQUEST_ANSWER_FLG"] = knjCreateCheckBox($objForm, "REQUEST_ANSWER_FLG", "1", $extra);

        //回答対象PRG
        $query = knje400aQuery::getPrg($model);
        $extra = $Row["REQUEST_ANSWER_FLG"] == "1" ? "" : " disabled ";
        makeCmb($objForm, $arg, $db, $query, "REQUEST_ANSWER_PRG", $Row["REQUEST_ANSWER_PRG"], $extra, 1, "BLANK");

        //作成日
        $arg["data"]["WRITING_DATE"] = View::popUpCalendar($objForm, "WRITING_DATE", str_replace("-","/",$Row["WRITING_DATE"]));

        //クラス一覧リスト
        makeClassList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $diabled);

        //hidden作成
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje400aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name != "YEAR") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knje400aQuery::getList($model);
    $result = $db->query($query);
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowlist["WRITING_DATE"] = str_replace("-","/",$rowlist["WRITING_DATE"]);
        $rowlist["SUBMISSION_DATE"] = str_replace("-","/",$rowlist["SUBMISSION_DATE"]);

        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $diabled) {
    //学校へ通達
    $extra = "onclick=\"return btn_submit('notify');\"";
    $arg["button"]["btn_notify"] = knjCreateBtn($objForm, "btn_notify", "学校へ通達", $extra.$diabled);
    //追加
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //修正
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$diabled);
    //削除
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$diabled);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//クラス一覧リスト作成
function makeClassList(&$objForm, &$arg, $db, &$model)
{
    //一覧(選択対象除く)
    $row1 = array();
    $query = knje400aQuery::getSchoolData($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[] = array('label' => $row["FLG_NAME"]." ".$row["EDBOARD_SCHOOLCD"].":".$row["EDBOARD_SCHOOLNAME"],
                        'value' => $row["EDBOARD_SCHOOLCD"]);
    }
    $result->free();
    
    //選択対象
    $row2 = array();
    if ($model->docNumber) {
        $query = knje400aQuery::getSelectSchoolData($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row2[] = array('label' => $row["FLG_NAME"]." ".$row["EDBOARD_SCHOOLCD"].":".$row["EDBOARD_SCHOOLNAME"],
                            'value' => $row["EDBOARD_SCHOOLCD"]);
        }
        $result->free();
    }

    //一覧リストを作成する
    $extra = "multiple style=\"width:260px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //選択対象学校リストを作成する
    $extra = "multiple style=\"width:260px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $row2, $extra, 20);

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

//hidden作成
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJE400A");
    knjCreateHidden($objForm, "selectdata");
}
?>
