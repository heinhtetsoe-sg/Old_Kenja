<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje460n_seikatu_zyouhouForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje460n_seikatu_zyouhouindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //カレンダー呼び出し
        $my = new mycalendar();

        //年度の設定
        $model->field1["YEAR"] = ($model->cmd == "edit") ? $model->field1["YEAR"] : $model->exp_year;

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //タイトル
        $arg["data"]["TITLE"] = "現在の願い・将来の希望、合理的な配慮等画面";

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1" || $model->cmd == "edit"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje460n_seikatu_zyouhouQuery::getMainQuery($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field1;
            }
        } else {
            $Row =& $model->field1;
        }

        //更新年度コンボ
        $extra = "onChange=\"return btn_submit('edit');\"";
        $query = knje460n_seikatu_zyouhouQuery::getYearCmb($model);
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field1["YEAR"], $extra, 1);

        //記入者コンボ
        // $extra = "";
        // if($Row["ENTRANT_NAME"] == ""){
        //     $Row["ENTRANT_NAME"] = knje460n_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '01', '03', 'REMARK');
        // }
        // $extra = "";
        // $query = knje460n_seikatu_zyouhouQuery::getStaffMst();
        // makeCmb($objForm, $arg, $db, $query, "ENTRANT_NAME", $Row["ENTRANT_NAME"], $extra, 1, "BLANK");
        if($Row["ENTRANT_NAME"] == ""){
            $Row["ENTRANT_NAME"] = $db->getOne(knje460n_seikatu_zyouhouQuery::getStaffMst(STAFFCD));
        }
        $arg["data"]["ENTRANT_NAME"] = getTextOrArea($objForm, "ENTRANT_NAME",  25, 1, $Row["ENTRANT_NAME"], $model);
        setInputChkHidden($objForm, "ENTRANT_NAME", 25, 1, $arg);


        // //学校名
        // $arg["data"]["SCHOOL_NAME"] = $Row["SCHOOL_NAME"];
        // 
        // //学部
        // $arg["data"]["FACULTY_NAME"] = $Row["FACULTY_NAME"];
        // 
        // //学年
        // $arg["data"]["GRADE"] = $Row["GRADE"];
        // 
        // //よみがな
        // $arg["data"]["KANA"] = $Row["KANA"];
        // 
        // //氏名
        // $arg["data"]["NAME"] = $Row["NAME"];
        // 
        // //性別
        // $arg["data"]["SEX"] = $Row["SEX"];
        // 
        // //生年月日
        // $arg["data"]["BIRTHDAY"] = $Row["BIRTHDAY"];
        // 
        // //住所
        // $arg["data"]["ADDR"] = $Row["ADDR"];
        // 
        // //電話番号
        // $arg["data"]["TELNO"] = $Row["TELNO"];
        // 
        // //療育手帳
        // $query = knje460n_seikatu_zyouhouQuery::getNameMst("E061", $Row["CHALLENGED_CARD_NAME"], 1);
        // $arg["data"]["CHALLENGED_CARD_NAME"] = $db->getOne($query);
        // 
        // //身体障害者手帳
        // //身体障害の種別
        // $query = knje460n_seikatu_zyouhouQuery::getNameMst("E031", $Row["CHALLENGED_CARD_CLASS"], 1);
        // $arg["data"]["CHALLENGED_CARD_CLASS"] = $db->getOne($query);
        // //身体障害の等級
        // $query = knje460n_seikatu_zyouhouQuery::getNameMst("E032", $Row["CHALLENGED_CARD_RANK"], 1);
        // $arg["data"]["CHALLENGED_CARD_RANK"] = $db->getOne($query);
        // //手帳の障害名
        // $query = knje460n_seikatu_zyouhouQuery::getChallengedCardNameMst($Row["CHALLENGED_CARD_AREA_NAME"], 1);
        // $arg["data"]["CHALLENGED_CARD_AREA_NAME"] = $db->getOne($query);
        // $query = knje460n_seikatu_zyouhouQuery::getChallengedCardNameMst($Row["CHALLENGED_CARD_AREA_NAME2"], 1);
        // $arg["data"]["CHALLENGED_CARD_AREA_NAME2"] = $db->getOne($query);
        // $query = knje460n_seikatu_zyouhouQuery::getChallengedCardNameMst($Row["CHALLENGED_CARD_AREA_NAME3"], 1);
        // $arg["data"]["CHALLENGED_CARD_AREA_NAME3"] = $db->getOne($query);
        // 
        // //精神障害者保健福祉手帳
        // //等級
        // $query = knje460n_seikatu_zyouhouQuery::getNameMst("E063", $Row["CHALLENGED_CARD_REMARK"], "1");
        // $arg["data"]["CHALLENGED_CARD_REMARK"] = $db->getOne($query);
        // 
        // //療育手帳の次回判定
        // $arg["data"]["CHALLENGED_CARD_CHECK_YM"] = getMonth($my, $Row["CHALLENGED_CARD_CHECK_YM"]);
//        $arg["data"]["CHALLENGED_CARD_CHECK_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_CHECK_YM", $Row["CHALLENGED_CARD_CHECK_YM"]);

        // //身体障害手帳の次回認定
        // //チェックボックスを作成
        // $val = $Row["CHALLENGED_CARD_GRANT_FLG"] == "1" ? "次回認定なし" : "　";
        // //次回認定月を作成
        // if ($val == "　") {
        //     $month = getMonth($my, $Row["CHALLENGED_CARD_GRANT_YM"]);
        //     if ($month) {
        //         $val = $month;
        //     }
        // }
        // $arg["data"]["CHALLENGED_CARD_GRANT_YM"] = $val;
//        $arg["data"]["CHALLENGED_CARD_GRANT_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_GRANT_YM", $Row["CHALLENGED_CARD_GRANT_YM"]);
        //精神障害者保健福祉手帳の有効期限
        // $arg["data"]["CHALLENGED_CARD_BAST_YM"] = getMonth($my, $Row["CHALLENGED_CARD_BAST_YM"]);
//        $arg["data"]["CHALLENGED_CARD_BAST_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_BAST_YM", $Row["CHALLENGED_CARD_BAST_YM"]);


        /******************/
        /* テキストエリア */
        /******************/
        //本人願い
        // if($Row["MY_WISH"] == ""){
        //     $Row["MY_WISH"] = knje460n_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '01', '01', 'REMARK');
        // }
        $extra = " id=MY_WISH";
        $arg["data"]["MY_WISH"] = KnjCreateTextArea($objForm, "MY_WISH", ($model->wish_gyou + 1), ($model->wish_moji * 2 + 1), "soft", $extra, $Row["MY_WISH"]);
        setInputChkHidden($objForm, "MY_WISH", $model->wish_moji, $model->wish_gyou, $arg);
        //本人希望
        // if($Row["MY_HOPE"] == ""){
        //     $Row["MY_HOPE"] = knje460n_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '01', '01', 'REMARK');
        // }
        $extra = " id=MY_HOPE";
        $arg["data"]["MY_HOPE"] = KnjCreateTextArea($objForm, "MY_HOPE", ($model->hope_gyou + 1), ($model->hope_moji * 2 + 1), "soft", $extra, $Row["MY_HOPE"]);
        setInputChkHidden($objForm, "MY_HOPE", $model->hope_moji, $model->hope_gyou, $arg);

        //保護者願い
        // if($Row["GUARDIAN_WISH"] == ""){
        //     $Row["GUARDIAN_WISH"] = knje460n_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '01', '02', 'REMARK');
        // }
        $extra = " id=GUARDIAN_WISH ";
        $arg["data"]["GUARDIAN_WISH"] = KnjCreateTextArea($objForm, "GUARDIAN_WISH", ($model->wish_gyou + 1), ($model->wish_moji * 2 + 1), "soft", $extra, $Row["GUARDIAN_WISH"]);
        setInputChkHidden($objForm, "GUARDIAN_WISH", $model->wish_moji, $model->wish_gyou, $arg);
        //保護者希望
        // if($Row["GUARDIAN_HOPE"] == ""){
        //     $Row["GUARDIAN_HOPE"] = knje460n_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '01', '02', 'REMARK');
        // }
        $extra = " id=GUARDIAN_HOPE ";
        $arg["data"]["GUARDIAN_HOPE"] = KnjCreateTextArea($objForm, "GUARDIAN_HOPE", ($model->hope_gyou + 1), ($model->hope_moji * 2 + 1), "soft", $extra, $Row["GUARDIAN_HOPE"]);
        setInputChkHidden($objForm, "GUARDIAN_HOPE", $model->hope_moji, $model->hope_gyou, $arg);

        //合理的配慮
        // if($Row["GOURITEKI_HAIRYO"] == ""){
        //     $Row["GOURITEKI_HAIRYO"] = knje460n_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '03', '01', 'REMARK');
        // }
        $extra = " id=GOURITEKI_HAIRYO ";
        $arg["data"]["GOURITEKI_HAIRYO"] = KnjCreateTextArea($objForm, "GOURITEKI_HAIRYO", ($model->gouriteki_hairyo_gyou + 1), ($model->gouriteki_hairyo_moji * 2 + 1), "soft", $extra, $Row["GOURITEKI_HAIRYO"]);
        setInputChkHidden($objForm, "GOURITEKI_HAIRYO", $model->gouriteki_hairyo_moji, $model->gouriteki_hairyo_gyou, $arg);

        //目指したい自立の姿
        // if($Row["ZIRITU"] == ""){
        //     //$Row["ZIRITU"] = knje460n_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '03', '01', 'REMARK');
        // }
        $extra = " id=ZIRITU ";
        $arg["data"]["ZIRITU"] = KnjCreateTextArea($objForm, "ZIRITU", ($model->ziritu_gyou + 1), ($model->ziritu_moji * 2 + 1), "soft", $extra, $Row["ZIRITU"]);
        setInputChkHidden($objForm, "ZIRITU", $model->ziritu_moji, $model->ziritu_gyou, $arg);

        //今年度の支援方針
        // if($Row["SIEN"] == ""){
        //     $Row["SIEN"] = knje460n_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, '03', '01', 'REMARK');
        // }
        $extra = " id=SIEN ";
        $arg["data"]["SIEN"] = KnjCreateTextArea($objForm, "SIEN", ($model->sien_gyou + 1), ($model->sien_moji * 2 + 1), "soft", $extra, $Row["SIEN"]);
        setInputChkHidden($objForm, "SIEN", $model->sien_moji, $model->sien_gyou, $arg);

        Query::dbCheckIn($db);

        //戻るボタンを作成する
        $link = REQUESTROOT."/E/KNJE390N/knje390nindex.php?cmd=subform3&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //更新ボタンを作成
        $extra = "onclick=\"return btn_submit('subform1_update');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更新", $extra);

        //hidden
        $nx = 1;
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COLCNT", $outcnt);
        knjCreateHidden($objForm, "HID_ROWCNT", "1");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje460n_seikatu_zyouhouForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    if ($blank == "BLANK") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg) {
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}
function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}

//年月の変換
function getMonth($my, $value) {
    $initialdate = "";
    if(substr($value,0,1)=='H' | substr($value,0,1)=='S'|
      substr($value,0,1)=='T' | substr($value,0,1)=='M' ){
        $initialdate = $value;
    }else{
        $initialdate = $my->ChgWToJ($value); //西暦YYYY-MM-DDをGYY/MM/DDに変換
    }
    return $initialdate;
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px; overflow:auto;\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

?>
