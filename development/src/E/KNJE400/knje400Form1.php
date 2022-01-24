<?php

require_once('for_php7.php');

class knje400Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knje400Form1", "POST", "knje400index.php", "", "knje400Form1");

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //教育委員会チェック
        $getFlg = $db->getOne(knje400Query::getNameMst());
        if ($getFlg !== '2') {
            $arg["jscript"] = "OnEdboardError();";
        }

        //年度
        $extra = "onChange=\"return btn_submit('knje400');\"";
        $query = knje400Query::getYear();
        makeCmb($objForm, $arg, $db2, $query, "YEAR", $model->year, $extra, 1);

        //V_SCHOOL_MSTから学校コードを取得
        $query = knje400Query::getSchoolMst($model);
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->schoolcd = $rtnRow["KYOUIKU_IINKAI_SCHOOLCD"];

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db2, $model);

        //教育歴情報取得
        if ($model->docNumber) {
            $Row = $db2->getRow(knje400Query::getRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row = array();
        }

        //通達日をhiddenにセット
        knjCreateHidden($objForm, "TRANSMISSION_DATE", $Row["TRANSMISSION_DATE"]);

        $readOnly = " readOnly ";
        $rOnlyRExtra = " STYLE=\"text-align:right;background:darkgray\"".$readOnly;
        $rOnlyExtra = " STYLE=\"background:darkgray\"".$readOnly;
        //文書番号
        $arg["data"]["DOC_NUMBER"] = knjCreateTextBox($objForm, $Row["DOC_NUMBER"], "DOC_NUMBER", 5, 5, $rOnlyRExtra);

        //通達文タイトル
        $extra = "";
        $arg["data"]["NOTICE_TITLE"] = knjCreateTextBox($objForm, $Row["NOTICE_TITLE"], "NOTICE_TITLE", 30, 60, $rOnlyExtra);

        //通達文
        $extra = "";
        $arg["data"]["NOTICE_MESSAGE"] = KnjCreateTextArea($objForm, "NOTICE_MESSAGE", 5, 83, "soft", $rOnlyExtra, $Row["NOTICE_MESSAGE"]);

        //掲載期間開始
        $extra = "";
        $arg["data"]["VIEWING_PERIOD_FROM"] = knjCreateTextBox($objForm, str_replace("-","/",$Row["VIEWING_PERIOD_FROM"]), "VIEWING_PERIOD_FROM", 10, 10, $rOnlyExtra);

        //掲載期間終了
        $extra = "";
        $arg["data"]["VIEWING_PERIOD_TO"] = knjCreateTextBox($objForm, str_replace("-","/",$Row["VIEWING_PERIOD_TO"]), "VIEWING_PERIOD_TO", 10, 10, $rOnlyExtra);

        //提出期限
        $extra = "";
        $arg["data"]["SUBMISSION_DATE"] = knjCreateTextBox($objForm, str_replace("-","/",$Row["SUBMISSION_DATE"]), "SUBMISSION_DATE", 10, 10, $rOnlyExtra);

        //回答要チェックボックス
        if ($Row["REQUEST_ANSWER_FLG"] == "1") {

            //回答対象PRG
            $query = knje400Query::getPrg($model, $Row["REQUEST_ANSWER_PRG"]);
            $setAnsPrg = $db2->getRow($query, DB_FETCHMODE_ASSOC);

            //請求一覧
            $indexName = mb_strtolower($setAnsPrg["NAME3"]);
            $extraPrg  = " onClick=\" wopen('".REQUESTROOT."/{$setAnsPrg["NAME2"]}/{$setAnsPrg["NAME3"]}/{$indexName}index.php?";
            $extraPrg .= "cmd=main";
            $extraPrg .= "&AUTH=".$model->auth;
            $extraPrg .= "&DOC_NUMBER=".$model->docNumber;
            $extraPrg .= "&SEND_YEAR=".$model->year;
            $extraPrg .= "&CALLID=KNJE400";
            $extraPrg .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";

            //追加
            $arg["data"]["KAITOU"] = "回答要　";
            $extra = "style=\"color:#1E90FF;font-weight:bold;font:bold\" onclick=\"return btn_submit('prg');\"";
            $arg["data"]["KAITOU"] .= knjCreateBtn($objForm, "btn_prg", "調査報告", $extraPrg);
            $arg["data"]["KAITOU"] .= "：".knjCreateTextBox($objForm, $setAnsPrg["NAME1"], "REQUEST_ANSWER_PRG", 30, 30, $rOnlyExtra);

        } else {
            $arg["data"]["KAITOU"] = "回答不要";
        }

        //作成日
        $extra = "";
        $arg["data"]["WRITING_DATE"] = knjCreateTextBox($objForm, str_replace("-","/",$Row["WRITING_DATE"]), "WRITING_DATE", 10, 10, $rOnlyExtra);

        //クラス一覧リスト
        makeClassList($objForm, $arg, $db2, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $diabled);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje400Form1.html", $arg); 
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
    $query = knje400Query::getList($model);
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
function makeBtn(&$objForm, &$arg) {
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//クラス一覧リスト作成
function makeClassList(&$objForm, &$arg, $db, &$model)
{
    //一覧(選択対象除く)
    $row1 = array();
    $query = knje400Query::getSchoolData($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[] = array('label' => $row["FLG_NAME"]." ".$row["EDBOARD_SCHOOLCD"].":".$row["EDBOARD_SCHOOLNAME"],
                        'value' => $row["EDBOARD_SCHOOLCD"]);
    }
    $result->free();
    
    //選択対象
    $row2 = array();
    if ($model->docNumber) {
        $query = knje400Query::getSelectSchoolData($model);
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
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJE400");
    knjCreateHidden($objForm, "selectdata");
}
?>
