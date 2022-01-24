<?php

require_once('for_php7.php');

class knjl411Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl411Form1", "POST", "knjl411index.php", "", "knjl411Form1");

        //セキュリティーチェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //イベント参加者データ取得
        $info = $db->getRow(knjl411Query::getRecruitDat($model), DB_FETCHMODE_ASSOC);

        //ヘッダー情報（年度、管理番号、氏名）
        $arg["HEADER_INFO"] = (CTRL_YEAR + 1).'年度　　'.$info["RECRUIT_NO"].'：'.$info["NAME"];

        //データ一覧取得
        $setval = array();
        $query = knjl411Query::getRecruitEventDat($model);
        if ($model->recruit_no) {
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["TOUROKU_DATE"] = str_replace("-", "/", $row["TOUROKU_DATE"]);
                $arg["data"][] = $row;
            }

        }

        //編集用データ取得
        if (isset($model->recruit_no) && !isset($model->warning) && $model->touroku_date && $model->event_cd && $model->media_cd && $model->cmd != "change") {
            $Row = $db->getRow(knjl411Query::getRecruitEventDat($model, "1"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //登録日付
        $value = ($Row["TOUROKU_DATE"] == "") ? "" : str_replace("-", "/", $Row["TOUROKU_DATE"]);
        $arg["data1"]["TOUROKU_DATE"] = View::popUpCalendar($objForm, "TOUROKU_DATE", $value);

        //分類
        $query = knjl411Query::getRecruitClass();
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "EVENT_CLASS_CD", $Row["EVENT_CLASS_CD"], $extra, 1);

        //イベント
        $query = knjl411Query::getRecruitEventYmst($model, $Row["EVENT_CLASS_CD"], $info["SCHOOL_KIND"]);
        makeCmb($objForm, $arg, $db, $query, "EVENT_CD", $Row["EVENT_CD"], "", 1);

        //媒体
        $query = knjl411Query::getNameMst('L401');
        makeCmb($objForm, $arg, $db, $query, "MEDIA_CD", $Row["MEDIA_CD"], "", 1);

        //状態
        $query = knjl411Query::getNameMst('L402');
        makeCmb($objForm, $arg, $db, $query, "STATE_CD", $Row["STATE_CD"], "", 1);

        for ($i = 1; $i <= 5; $i++) {
            //希望課程学科
            $query = knjl411Query::getCourseMajorMst();
            $extra = "onChange=\"return btn_submit('change');\"";
            makeCmb($objForm, $arg, $db, $query, "HOPE_COURSE_MAJOR{$i}", $Row["HOPE_COURSE_MAJOR{$i}"], $extra, 1, "blank");

            //希望コース
            $query = knjl411Query::getCoursecodeMst($model, $Row["HOPE_COURSE_MAJOR{$i}"]);
            makeCmb($objForm, $arg, $db, $query, "HOPE_COURSECODE{$i}", $Row["HOPE_COURSECODE{$i}"], "", 1, "blank");
        }

        //備考
        $extra = "onkeyup =\"charCount(this.value, 2, (40 * 2), true);\" oncontextmenu =\"charCount(this.value, 2, (40 * 2), true);\"";
        $arg["data1"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", "2", "80", "wrap", $extra, $Row["REMARK"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl411Form1.html", $arg);
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
