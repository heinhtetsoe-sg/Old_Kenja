<?php

require_once('for_php7.php');
class knje390mSubForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje390mindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();
        //カレンダー呼び出し
        $my = new mycalendar();

        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;//年度データはないが、念の為にセット
            if ($model->record_date != "") {
                $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
                $model->main_year = $recordDateArr[0];
            }
        }

        if ($model->cmd == "subform1_change") {
            $model->record_date = $model->field["RECORD_HISTORY"];
        } elseif (!$model->record_date) {
            $model->record_date = CTRL_DATE;
            $Row = $db->getRow(knje390mQuery::getRecordDate($model, "B"), DB_FETCHMODE_ASSOC);
            if ($Row["VALUE"]) {
                $model->record_date = $Row["VALUE"];
            }
        }
        if ($model->record_date != "") {
            $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
            $model->main_year = $recordDateArr[0];
        }

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1" || $model->cmd == "subform1A" || $model->cmd == "subform1_clear"|| $model->cmd == "subform1_change") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery1($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //作成履歴
        $extra = "onchange=\"return btn_submit('subform1_change')\"";
        $query = knje390mQuery::getRecordDate($model, "B");
        makeCmb($objForm, $arg, $db, $query, "RECORD_HISTORY", $Row["RECORD_DATE"], $extra, 1, 1);

        $Row["RECORD_DATE"] = str_replace("-", "/", $Row["RECORD_DATE"]);
        //データをカウント
        $mainCountData = knje390mQuery::getCheckMainDataQuery($db, $model, "1");
        //作成年月日
        $extra = "";
        $arg["data"]["RECORD_DATE"] = $Row["RECORD_DATE"];
        if ($mainCountData == 0 || strlen($Row["RECORD_DATE"]) === 0) {
            $Row["RECORD_DATE"] = str_replace("-", "/", CTRL_DATE);
            $arg["data"]["RECORD_DATE"] = View::popUpCalendar($objForm, "RECORD_DATE", $Row["RECORD_DATE"]);
        } else {
            knjCreateHidden($objForm, "RECORD_DATE", str_replace("/", "-", $Row["RECORD_DATE"]));
        }
        knjCreateHidden($objForm, "CTRL_DATE", str_replace("/", "-", CTRL_DATE));

        //作成者
        $extra = "";
        if ($Row["RECORD_STAFFNAME"] === null) {
            $Row["RECORD_STAFFNAME"] = $db->getOne(knje390mQuery::getStaffName($model));
        }
        $arg["data"]["RECORD_STAFFNAME"] = getTextOrArea($objForm, "RECORD_STAFFNAME", 25, 1, $Row["RECORD_STAFFNAME"]);
        setInputChkHidden($objForm, "RECORD_STAFFNAME", 25, 1, $arg);

        //通学方法
        $query = knje390mQuery::getNameMst("E036");
        makeCmb($objForm, $arg, $db, $query, "TSUUGAKU_DIV1", $Row["TSUUGAKU_DIV1"], "", 1, 1);
        
        $query = knje390mQuery::getNameMst("E036");
        makeCmb($objForm, $arg, $db, $query, "TSUUGAKU_DIV2", $Row["TSUUGAKU_DIV2"], "", 1, 1);

        $extra = "";
        $arg["data"]["TSUUGAKU_DIV1_REMARK"] = getTextOrArea($objForm, "TSUUGAKU_DIV1_REMARK", 35, 2, $Row["TSUUGAKU_DIV1_REMARK"]);
        setInputChkHidden($objForm, "TSUUGAKU_DIV1_REMARK", 35, 2, $arg);
        $arg["data"]["TSUUGAKU_DIV2_REMARK"] = getTextOrArea($objForm, "TSUUGAKU_DIV2_REMARK", 35, 2, $Row["TSUUGAKU_DIV2_REMARK"]);
        setInputChkHidden($objForm, "TSUUGAKU_DIV2_REMARK", 35, 2, $arg);

        //障害名･診断名
        $extra = "style=\"height:85px;\"";
        $arg["data"]["CHALLENGED_NAMES"] = getTextOrArea($objForm, "CHALLENGED_NAMES", 40, 2, $Row["CHALLENGED_NAMES"]);
        setInputChkHidden($objForm, "CHALLENGED_NAMES", 40, 2, $arg);
        //実態概要･障害の特性
        $extra = "style=\"height:45px;\"";
        $arg["data"]["CHALLENGED_STATUS"] = getTextOrArea($objForm, "CHALLENGED_STATUS", 40, 3, $Row["CHALLENGED_STATUS"]);
        setInputChkHidden($objForm, "CHALLENGED_STATUS", 40, 3, $arg);

        //療育手帳
        $query = knje390mQuery::getNameMst("E061");
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_NAME", $Row["CHALLENGED_CARD_NAME"], "", 1, 1);

        //身体障害者手帳
        //身体障害の種別
        $query = knje390mQuery::getNameMst("E031");
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_CLASS", $Row["CHALLENGED_CARD_CLASS"], "", 1, 1);
        //身体障害の等級
        $query = knje390mQuery::getNameMst("E032");
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_RANK", $Row["CHALLENGED_CARD_RANK"], "", 1, 1);
        //手帳の障害名
        $query = knje390mQuery::getChallengedCardNameMst();
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_AREA_NAME", $Row["CHALLENGED_CARD_AREA_NAME"], "", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_AREA_NAME2", $Row["CHALLENGED_CARD_AREA_NAME2"], "", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_AREA_NAME3", $Row["CHALLENGED_CARD_AREA_NAME3"], "", 1, 1);

        //精神障害者保健福祉手帳
        //等級
        $extra = "";
        $query = knje390mQuery::getNameMst("E063");
        makeCmb($objForm, $arg, $db, $query, "CHALLENGED_CARD_REMARK", $Row["CHALLENGED_CARD_REMARK"], "", 1, 1);

        //療育手帳の次回判定
        $arg["data"]["CHALLENGED_CARD_CHECK_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_CHECK_YM", $Row["CHALLENGED_CARD_CHECK_YM"]);
        //身体障害手帳の次回認定
        //チェックボックスを作成
        $extra = $Row["CHALLENGED_CARD_GRANT_FLG"] == "1" ? "checked" : "";
        $extra .= " id=\"CHALLENGED_CARD_GRANT_FLG\" ";
        $arg["data"]["CHALLENGED_CARD_GRANT_FLG"] = knjCreateCheckBox($objForm, "CHALLENGED_CARD_GRANT_FLG", "1", $extra, "");
        //次回認定月を作成
        $arg["data"]["CHALLENGED_CARD_GRANT_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_GRANT_YM", $Row["CHALLENGED_CARD_GRANT_YM"]);
        //精神障害者保健福祉手帳の有効期限
        $arg["data"]["CHALLENGED_CARD_BAST_YM"] = $my->MyMonthWin($objForm, "CHALLENGED_CARD_BAST_YM", $Row["CHALLENGED_CARD_BAST_YM"]);

        //受給者証の有無
        $extra  = "id=\"WELFARE_MEDICAL_RECEIVE_FLG\"";
        if ($Row["WELFARE_MEDICAL_RECEIVE_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["WELFARE_MEDICAL_RECEIVE_FLG"] = knjCreateCheckBox($objForm, "WELFARE_MEDICAL_RECEIVE_FLG", "1", $extra);

        //種類
        $query = knje390mQuery::getChallengedCertifNameMst();
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV", $Row["WELFARE_MEDICAL_RECEIVE_DIV"], "", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV2", $Row["WELFARE_MEDICAL_RECEIVE_DIV2"], "", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV3", $Row["WELFARE_MEDICAL_RECEIVE_DIV3"], "", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV4", $Row["WELFARE_MEDICAL_RECEIVE_DIV4"], "", 1, 1);
        makeCmb($objForm, $arg, $db, $query, "WELFARE_MEDICAL_RECEIVE_DIV5", $Row["WELFARE_MEDICAL_RECEIVE_DIV5"], "", 1, 1);

        //校区
        //小学校
        $query = knje390mQuery::getSchoolInfo($Row["P_SCHOOL_CD"]);
        $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["P_SCHOOL_NAME"] = $schoolRow["FINSCHOOL_NAME"];
        //学校検索
        $setKind = $db->getOne(knje390mQuery::getL019namecd2("4"));
        $schoolCdVal = "document.forms[0]['P_SCHOOL_CD'].value";
        $arg["data"]["P_SCHOOL_CD"] = View::popUpSchoolCd($objForm, "P_SCHOOL_CD", $Row["P_SCHOOL_CD"], $schoolCdVal, "btn_kensaku", "", "P_SCHOOL_CD", "P_SCHOOL_NAME", "", "P_SCHOOL_RITSU", "", "", $setKind, "");

        //中学校
        $query = knje390mQuery::getSchoolInfo($Row["J_SCHOOL_CD"]);
        $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["J_SCHOOL_NAME"] = $schoolRow["FINSCHOOL_NAME"];
        //学校検索
        $setKind = $db->getOne(knje390mQuery::getL019namecd2("5"));
        $schoolCdVal = "document.forms[0]['J_SCHOOL_CD'].value";
        $arg["data"]["J_SCHOOL_CD"] = View::popUpSchoolCd($objForm, "J_SCHOOL_CD", $Row["J_SCHOOL_CD"], $schoolCdVal, "btn_kensaku", "", "J_SCHOOL_CD", "J_SCHOOL_NAME", "", "J_SCHOOL_RITSU", "", "", $setKind, "");

        //避難場所
        $extra = "";
        $arg["data"]["EVACUATION_AREA"] = getTextOrArea($objForm, "EVACUATION_AREA", 20, 1, $Row["EVACUATION_AREA"]);
        setInputChkHidden($objForm, "EVACUATION_AREA", 20, 1, $arg);
        //留意事項
        $extra = "";
        $arg["data"]["IMPORTANT_NOTICE"] = getTextOrArea($objForm, "IMPORTANT_NOTICE", 40, 3, $Row["IMPORTANT_NOTICE"]);
        setInputChkHidden($objForm, "IMPORTANT_NOTICE", 40, 3, $arg);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = getTextOrArea($objForm, "REMARK", 50, 10, $Row["REMARK"]);
        setInputChkHidden($objForm, "REMARK", 50, 10, $arg);

        //履歴用日付
        $model->field["BACKUP_DATE"] = str_replace("-", "/", $model->field["BACKUP_DATE"]);
        $arg["data"]["BACKUP_DATE"] = View::popUpCalendar($objForm, "BACKUP_DATE", $model->field["BACKUP_DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning) == 0 && $model->cmd != "subform1_clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="subform1_clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData)
{
    //データがない場合は、更新、取消、戻る以外は使用不可
    if ($mainCountData == 0) {
        $disabled = " disabled ";
    } else {
        $disabled = "";
    }

    //障害名･診断名マスタ
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名参照", $extra);

    //生育歴
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform1_growth&SCHREGNO=".$model->schregno;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace5"] = KnjCreateBtn($objForm, "btn_replace5", "生育歴", $extra.$disabled);
    
    //療育・教育歴ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform1_educate&SCHREGNO=".$model->schregno;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "療育・教育歴", $extra.$disabled);

    //医療ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform1_medical&SCHREGNO=".$model->schregno;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace2"] = KnjCreateBtn($objForm, "btn_replace2", "医療", $extra.$disabled);

    //訓練機関
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform1_training&SCHREGNO=".$model->schregno;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace6"] = KnjCreateBtn($objForm, "btn_replace6", "訓練機関", $extra.$disabled);
    
    //健康管理ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform1_healthcare&SCHREGNO=".$model->schregno;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace3"] = KnjCreateBtn($objForm, "btn_replace3", "健康管理", $extra.$disabled);

    //視力・聴力
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform1_visionEar&SCHREGNO=".$model->schregno;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace7"] = KnjCreateBtn($objForm, "btn_replace7", "視力・聴力", $extra.$disabled);
    
    //福祉ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform1_welfare&SCHREGNO=".$model->schregno;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace4"] = KnjCreateBtn($objForm, "btn_replace4", "福祉", $extra.$disabled);

    //コピーして新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform1_copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_rireki", "コピーして新規作成", $extra.$disabled);

    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform1_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform1_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "useFinschoolcdFieldSize", $model->Properties["useFinschoolcdFieldSize"]);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"], 'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg)
{
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}
function getTextAreaComment($moji, $gyo)
{
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
