<?php

require_once('for_php7.php');

class knjl421Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl421Form1", "POST", "knjl421index.php", "", "knjl421Form1");

        //セキュリティーチェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjl421Query::getZ010($model);
        $model->z010Name1 = $db->getOne($query);

        //イベント参加者データ取得
        $info = $db->getRow(knjl421Query::getRecruitDat($model), DB_FETCHMODE_ASSOC);

        //ヘッダー情報（年度、管理番号、氏名）
        $arg["HEADER_INFO"] = (CTRL_YEAR + 1).'年度　　'.$info["RECRUIT_NO"].'：'.$info["NAME"].'【'.$info["STAFF_NAME"].'】';

        //データ一覧取得
        $setval = array();
        $query = knjl421Query::getRecruitEventDat($model);
        if ($model->recruit_no) {
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["TOUROKU_DATE"] = str_replace("-", "/", $row["TOUROKU_DATE"]);
                $row["ATTEND_MEETING_FLG"] = ($row["ATTEND_MEETING_FLG"] == "1") ? "レ": "";
                $arg["data"][] = $row;
            }

        }

        //編集用データ取得
        if (isset($model->recruit_no) && !isset($model->warning) && $model->touroku_date && $model->event_cd && $model->media_cd && $model->cmd != "change") {
            $Row = $db->getRow(knjl421Query::getRecruitEventDat($model, "1"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //登録日付
        $value = ($Row["TOUROKU_DATE"] == "") ? "" : str_replace("-", "/", $Row["TOUROKU_DATE"]);
        $arg["data1"]["TOUROKU_DATE"] = View::popUpCalendar($objForm, "TOUROKU_DATE", $value);

        //分類
        $query = knjl421Query::getRecruitClass();
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "EVENT_CLASS_CD", $Row["EVENT_CLASS_CD"], $extra, 1);

        //イベント
        $query = knjl421Query::getRecruitEventYmst($model, $Row["EVENT_CLASS_CD"]);
        makeCmb($objForm, $arg, $db, $query, "EVENT_CD", $Row["EVENT_CD"], "", 1);

        //媒体
        $query = knjl421Query::getNameMst($model, 'L411');
        makeCmb($objForm, $arg, $db, $query, "MEDIA_CD", $Row["MEDIA_CD"], "", 1);

        //状態
        $query = knjl421Query::getNameMst($model, 'L412');
        makeCmb($objForm, $arg, $db, $query, "STATE_CD", $Row["STATE_CD"], "", 1);

        //当日出席
        $extra  = "id=\"ATTEND_MEETING_FLG\"";
        $extra .= ($Row["ATTEND_MEETING_FLG"] == "1") ? " checked": "";
        $arg["data1"]["ATTEND_MEETING_FLG"] = knjCreateCheckBox($objForm, "ATTEND_MEETING_FLG", "1", $extra);

        //資料請求部数
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data1"]["DOC_REQ_NUMBER"] = knjCreateTextBox($objForm, $Row["DOC_REQ_NUMBER"], "DOC_REQ_NUMBER", 3, 3, $extra);

        //備考
        $extra = "id=\"REMARK\" ";
        $arg["data1"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", "2", "80", "wrap", $extra, $Row["REMARK"]);
        if ($model->z010Name1 == "bunkyo") {
            $arg["notToHtml5"] = "1";
        } else {
            $arg["toHtml5"] = "1";
            knjCreateHidden($objForm, "REMARK_KETA", 80);             //半角の文字数(全角44文字X4行まで)なら88
            knjCreateHidden($objForm, "REMARK_GYO", 2);               //そのまま行数(全角44文字X4行まで)なら4
            KnjCreateHidden($objForm, "REMARK_STAT", "statusarea1");  //残り文字数を表示するエリア名
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        if ($model->z010Name1 == "bunkyo") {
            View::toHTML($model, "knjl421Form1.html", $arg);
        } else {
            View::toHTML5($model, "knjl421Form1.html", $arg);
        }
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data1"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = " onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"closeMethod();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
}
?>
