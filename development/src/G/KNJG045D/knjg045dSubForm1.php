<?php

require_once('for_php7.php');

class knjg045dSubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjg045dindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();
        //更新後、親画面に反映
        $cmd = explode('-', $model->cmd);
        if ($cmd[1] == "A") {
            $sftDiv = array();
            $sftDiv["shutcho"]      = "1";
            $sftDiv["kyuka"]        = "2";

            $setStaffName = array();
            $query = knjg045dQuery::getStaffData($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($sftDiv[$cmd[0]] == $row["STAFF_DIV"]) {
                    $setStaffName[] = $row["STAFFNAME_SHOW"];
                }
            }
            knjCreateHidden($objForm, "setStaffName", implode(',',$setStaffName));
            //反映処理
            $arg["reload"] = "refStaffName('STAFFNAME_SHOW{$sftDiv[$cmd[0]]}');";
            $model->cmd = $cmd[0];
            //更新後は画面を閉じる
            $arg["close"] = "parent.closeit();";
        }

        //マスタ情報
        $arg["SHOKUIN_SET"] = '1';
        $setTitle = '職員・状況';

        $arg["TITLE"] = $setTitle;

        $rirekiCnt = makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg045dSubForm1.html", $arg); 
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {

    $setStaffDiv = $model->setStaffDiv;
    if ($model->cmd === 'shutcho') {
        $setStaffDiv = '1';
    } else if ($model->cmd === 'kyuka') {
        $setStaffDiv = '2';
    }

    //画面表示設定
    if ($setStaffDiv === '1') {
        $arg["koumoku"] = '出張者';
        $arg["subtitle"] = '(出張者)';
        $arg["shutcho"] = '1';
    } else if ($setStaffDiv === '2') {
        $arg["koumoku"] = '休暇等';
        $arg["subtitle"] = '(休暇等)';
        $arg["kyuka"] = '1';
    }

    //各データ取得
    $counter = 0;
    if (VARS::get("DIARY_DATE")) {
        $setDiaryDate = VARS::get("DIARY_DATE");
    } else if ($model->setDiaryDate) {
        $setDiaryDate = $model->setDiaryDate;
    }
    //各キーを保持
    knjCreateHidden($objForm, "setDiaryDate", $setDiaryDate);
    knjCreateHidden($objForm, "setStaffDiv", $setStaffDiv);

    $query = knjg045dQuery::getSelectSubQuery($model, $setDiaryDate);
    $result = $db->query($query);
    for ($i = 1; $i <= 20; $i++) {
        $staff = "";
        if ($setStaffDiv === '1') {
            $seq = sprintf("%03d", $i + 100);
        } else if ($setStaffDiv === '2') {
            $seq = sprintf("%03d", $i + 200);
        }

        //STAFFコンボ
        $query = knjg045dQuery::getSelectSubQuery($model, $setDiaryDate);
        $staff = $db->getOne(knjg045dQuery::getSchoolDiaryDetailSeqDat($model, "REMARK1", $setDiaryDate, $setStaffDiv, $staff, $seq));
        //$extra = "onchange=\"return btn_submit('sub');\"";
        makeCmbSub($objForm, $arg, $setData, $i, $db, $query, $staff, "STAFF",  $extra, 1, "BLANK");

        if ($setStaffDiv === '1') {

            //出張先
            $val = $db->getOne(knjg045dQuery::getSchoolDiaryDetailSeqDat($model, "REMARK2", $setDiaryDate, $setStaffDiv, $staff));
            $extra = "";
            $setData["REMARK2"] = KnjCreateTextArea($objForm, "REMARK2"."_".$i, 1, 24, "soft", $extra, $val);

            //用件
            $val = $db->getOne(knjg045dQuery::getSchoolDiaryDetailSeqDat($model, "REMARK3", $setDiaryDate, $setStaffDiv, $staff));
            $extra = "";
            $setData["REMARK3"] = KnjCreateTextArea($objForm, "REMARK3"."_".$i, 1, 50, "soft", $extra, $val);

        } else if ($setStaffDiv === '2') {

            //休暇区分コンボ
            $val = $db->getOne(knjg045dQuery::getSchoolDiaryDetailSeqDat($model, "REMARK2", $setDiaryDate, $setStaffDiv, $staff));
            $query = knjg045dQuery::getNameMst(CTRL_YEAR,'G100');
            $extra = "";
            makeCmbSub($objForm, $arg, $setData, $i, $db, $query, $val, "REMARK2",  $extra, 1, "BLANK");

            //事由
            $val = $db->getOne(knjg045dQuery::getSchoolDiaryDetailSeqDat($model, "REMARK3", $setDiaryDate, $setStaffDiv, $staff));
            $extra = "";
            $setData["REMARK3"] = KnjCreateTextArea($objForm, "REMARK3"."_".$i, 1, 32, "soft", $extra, $val);

            //時間
            $val = $db->getOne(knjg045dQuery::getSchoolDiaryDetailSeqDat($model, "REMARK4", $setDiaryDate, $setStaffDiv, $staff));
            $extra = "";
            $setData["REMARK4"] = KnjCreateTextArea($objForm, "REMARK4"."_".$i, 1, 12, "soft", $extra, $val);
        }

        $setData["backcolor"] = "#ffffff";

        $arg["data"][] = $setData;
        $counter++;

    }

    $result->free();
    Query::dbCheckIn($db);
    //データ数を保持
    knjCreateHidden($objForm, "setcounter", $counter);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update_detail')\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//makeCmb
function makeCmbSub(&$objForm, &$arg, &$setData, $i, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
//    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    $setData[$name] = knjCreateCombo($objForm, $name."_".$i, $value, $opt, $extra, $size);

    $result->free();
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
    knjCreateHidden($objForm, "DIARY_DATE", $model->diaryDate);
}
?>

