<?php

require_once('for_php7.php');

class knje390SubForm4
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4", "POST", "knje390index.php", "", "subform4");

        //DB接続
        $db = Query::dbCheckOut();

        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;//年度データはないが取込時に使用
        }
        if (!$model->record_date) {
            $model->record_date = 'NEW';
        }
        //表示日付をセット
        if ($model->record_date === 'NEW') {
            $setHyoujiDate = '';
        } else {
            $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
        }

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;
        // Add by PP for Title 2020-02-03 start
        if($info["NAME_SHOW"] != ""){
            $arg["TITLE"] = $info["NAME_SHOW"]."のD 移行支援計画画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // for 915 error
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubForm4_CurrentCursor915\");</script>";
        } else {
          echo "<script>var error195= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubForm4_CurrentCursor915\", error195);
              sessionStorage.removeItem(\"KNJE390SubForm4_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform4" || $model->cmd == "subform4A" || $model->cmd == "subform4_clear"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390Query::getSubQuery4($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field4;
            }
        } else {
            $Row =& $model->field4;
        }
        
        //作成年月日
        $extra = "";
        $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
        $arg["data"]["WRITING_DATE"] = View::popUpCalendar($objForm, "WRITING_DATE", $Row["WRITING_DATE"]);

        //B プロフィールデータ日付コンボ
        $extra = "";
        $query = knje390Query::getTorikomiRecordDate($model, "B");
        makeCmb($objForm, $arg, $db, $query, "TORIKOMI_B_DATE", $model->field4["TORIKOMI_B_DATE"], $extra, 1, "");

        //B プロフィールの障害名･診断名、障害の実態･特性の取込
        if ($model->cmd == "subform4_torikomi_main") {
            $getRow = $db->getRow(knje390Query::getSubQuery1($model, $model->field4["TORIKOMI_B_DATE"]), DB_FETCHMODE_ASSOC);
            $Row["CHALLENGED_NAMES"] = $getRow["CHALLENGED_NAMES"];
            $Row["CHALLENGED_STATUS"] = $getRow["CHALLENGED_STATUS"];
        }
        //障害名･診断名
        // Add by PP for PC-Talker 2020-02-03 start
        $comment = getTextAreaComment(15, 3);
        $comment_label = str_replace(array( '(', ')' ), '', $comment);
        $extra = "style=\"height:45px;\"";
        $label="障害名等{$comment_label}";
        $arg["data"]["CHALLENGED_NAMES"] = getTextOrArea($objForm, "CHALLENGED_NAMES", 15, 3, $Row["CHALLENGED_NAMES"], $model, $label);
        setInputChkHidden($objForm, "CHALLENGED_NAMES", 15, 3, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //障害の実態･特性
        // Add by PP for PC-Talker 2020-02-03 start
        $comment = getTextAreaComment(15, 8);
        $comment_label = str_replace(array( '(', ')' ), '', $comment);
        $extra = "style=\"height:110px;\"";
        $label="障害の状況{$comment_label}";
        $arg["data"]["CHALLENGED_STATUS"] = getTextOrArea($objForm, "CHALLENGED_STATUS", 15, 8, $Row["CHALLENGED_STATUS"], $model);
        setInputChkHidden($objForm, "CHALLENGED_STATUS", 15, 8, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //実態・分析、課題・つけたい力
        // Add by PP for PC-Talker 2020-02-03 start
        $main_label="実態の";
        $extra = "style=\"height:210px;\"";
        $comment = getTextAreaComment(35, 15);
        $comment_label = str_replace(array( '(', ')' ), '', $comment);
        $label="{$main_label}基本的生活習慣身辺自立{$comment_label}";
        $arg["data"]["LIFESTYLE_STATUS"] = getTextOrArea($objForm, "LIFESTYLE_STATUS", 35, 15, $Row["LIFESTYLE_STATUS"], $model, $label);
        setInputChkHidden($objForm, "LIFESTYLE_STATUS", 35, 15, $arg);

        $label="{$main_label}行動・社会性{$comment_label}";
        $arg["data"]["SOCIALITY_STATUS"] = getTextOrArea($objForm, "SOCIALITY_STATUS", 35, 15, $Row["SOCIALITY_STATUS"], $model, $label);
        setInputChkHidden($objForm, "SOCIALITY_STATUS", 35, 15, $arg);

        $label="{$main_label}言語コミュニケーション{$comment_label}";
        $arg["data"]["COMMUNICATION_STATUS"] = getTextOrArea($objForm, "COMMUNICATION_STATUS", 35, 15, $Row["COMMUNICATION_STATUS"], $model, $label);
        setInputChkHidden($objForm, "COMMUNICATION_STATUS", 35, 15, $arg);

        $label="{$main_label}身体・運動{$comment_label}";
        $arg["data"]["PHYSICAL_ACTIVITY_STATUS"] = getTextOrArea($objForm, "PHYSICAL_ACTIVITY_STATUS", 35, 15, $Row["PHYSICAL_ACTIVITY_STATUS"], $model, $label);
        setInputChkHidden($objForm, "PHYSICAL_ACTIVITY_STATUS", 35, 15, $arg);

        $label="{$main_label}学習{$comment_label}";
        $arg["data"]["STUDY_STATUS"] = getTextOrArea($objForm, "STUDY_STATUS", 35, 15, $Row["STUDY_STATUS"], $model, $label);
        setInputChkHidden($objForm, "STUDY_STATUS", 35, 15, $arg);

        $label="{$main_label}興味・強み{$comment_label}";
        $arg["data"]["INTERESTING_STATUS"] = getTextOrArea($objForm, "INTERESTING_STATUS", 35, 15, $Row["INTERESTING_STATUS"], $model, $label);
        setInputChkHidden($objForm, "INTERESTING_STATUS", 35, 15, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //希望・願い
        //本人
        // Add by PP for PC-Talker 2020-02-03 start
        $person = "本人の";
        $guardian = "保護者の";
        $main_label = "希望・願いの";
        $current = "現在";
        $course = "進路";
        $graduation ="卒業後";
        // Add by PP for PC-Talker 2020-02-20 end

        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:35px;\"";
        $comment = getTextAreaComment(20, 2);
        $comment_label = str_replace(array( '(', ')' ), '', $comment);
        $label = "{$person}{$main_label}{$current}{$comment_label}";
        $arg["data"]["ONES_HOPE_PRESENT"] = getTextOrArea($objForm, "ONES_HOPE_PRESENT", 20, 2, $Row["ONES_HOPE_PRESENT"], $model, $label);
        setInputChkHidden($objForm, "ONES_HOPE_PRESENT", 20, 2, $arg);

        $label = "{$person}{$main_label}{$course}{$comment_label}";
        $arg["data"]["ONES_HOPE_CAREER"] = getTextOrArea($objForm, "ONES_HOPE_CAREER", 20, 2, $Row["ONES_HOPE_CAREER"], $model, $label);
        setInputChkHidden($objForm, "ONES_HOPE_CAREER", 20, 2, $arg);

        $label = "{$person}{$main_label}{$graduation}{$comment_label}";
        $arg["data"]["ONES_HOPE_AFTER_GRADUATION"] = getTextOrArea($objForm, "ONES_HOPE_AFTER_GRADUATION", 20, 2, $Row["ONES_HOPE_AFTER_GRADUATION"], $model, $label);
        setInputChkHidden($objForm, "ONES_HOPE_AFTER_GRADUATION", 20, 2, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //保護者
        // Add by PP for PC-Talker 2020-02-03 start
        $comment = getTextAreaComment(25, 2);
        $comment_label = str_replace(array( '(', ')' ), '', $comment);
        $label = "{$guardian}{$main_label}{$current}{$comment_label}";
        $arg["data"]["GUARDIAN_HOPE_PRESENT"] = getTextOrArea($objForm, "GUARDIAN_HOPE_PRESENT", 25, 2, $Row["GUARDIAN_HOPE_PRESENT"], $model, $label);
        setInputChkHidden($objForm, "GUARDIAN_HOPE_PRESENT", 25, 2, $arg);

        $label = "{$guardian}{$main_label}{$course}{$comment_label}";
        $arg["data"]["GUARDIAN_HOPE_CAREER"] = getTextOrArea($objForm, "GUARDIAN_HOPE_CAREER", 25, 2, $Row["GUARDIAN_HOPE_CAREER"], $model, $label);
        setInputChkHidden($objForm, "GUARDIAN_HOPE_CAREER", 25, 2, $arg);

        $label = "{$guardian}{$main_label}{$graduation}{$comment_label}";
        $arg["data"]["GUARDIAN_HOPE_AFTER_GRADUATION"] = getTextOrArea($objForm, "GUARDIAN_HOPE_AFTER_GRADUATION", 25, 2, $Row["GUARDIAN_HOPE_AFTER_GRADUATION"], $model, $label);
        setInputChkHidden($objForm, "GUARDIAN_HOPE_AFTER_GRADUATION", 25, 2, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //引継ぎ事項
        // Add by PP for PC-Talker 2020-02-03 start
        $comment = getTextAreaComment(45, 5);
        $comment_label = str_replace(array( '(', ')' ), '', $comment);
        $extra = "style=\"height:80px;\"";
        $label = "引継ぎ事項{$comment_label}";
        $arg["data"]["TAKEOVER"] = getTextOrArea($objForm, "TAKEOVER", 45, 5, $Row["TAKEOVER"], $model, $label);
        setInputChkHidden($objForm, "TAKEOVER", 45, 5, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //履歴用日付
        $model->field4["BACKUP_DATE"] = str_replace("-", "/", $model->field4["BACKUP_DATE"]);
        $arg["data"]["BACKUP_DATE"] = View::popUpCalendar($objForm, "BACKUP_DATE", $model->field4["BACKUP_DATE"]);
        
        //データをカウント
        $mainCountData = knje390Query::getCheckMainDataQuery($db, $model, "4");

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        if(get_count($model->warning)== 0 && $model->cmd !="subform1_clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="subform1_clear"){
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML5($model, "knje390SubForm4.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData)
{
    //データがない場合は、更新、取消、戻る以外は使用不可
    if ($mainCountData == 0) {
        $disabled = "disabled";
    } else {
        $disabled = "";
    }

    //障害名等マスタ
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_statusname\" onclick=\"current_cursor('btn_statusname'); loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名等マスタ参照", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //B様式取込ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_Btorikomi\" onclick=\"current_cursor('btn_Btorikomi'); return btn_submit('subform4_torikomi_main');\"";
    $arg["button"]["btn_Btorikomi"] = KnjCreateBtn($objForm, "btn_Btorikomi", "B様式取込", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //履歴ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_rireki\" onclick=\"current_cursor('btn_rireki'); return btn_submit('subform4_rireki');\"";
    $arg["button"]["btn_rireki"] = knjCreateBtn($objForm, "btn_rireki", "更新(履歴)", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('subform4_updatemain');\" aria-label=\"更新\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('subform4_clear');\"  aria-label=\"取消\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update_1\" onclick=\"current_cursor('btn_update_1'); return btn_submit('subform4_updatemain');\" aria-label=\"更新\"";
    $arg["button"]["btn_update_1"] = knjCreateBtn($objForm, "btn_update_1", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset_1\" onclick=\"current_cursor('btn_reset_1'); return btn_submit('subform4_clear');\"  aria-label=\"取消\"";
    $arg["button"]["btn_reset_1"] = knjCreateBtn($objForm, "btn_reset_1", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
    
    //戻るボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "onclick=\"return btn_submit('edit');\" aria-label=\"戻る\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //発達検査ボタン
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform4_check&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_replace\" onclick=\"current_cursor('btn_replace'); window.open('$link','_self');\"";
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "発達検査", $extra.$disabled);

    //スムーズな以降に向けての支援ボタン
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform4_smooth&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_replace2\" onclick=\"current_cursor('btn_replace2'); window.open('$link','_self');\"";
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
    $arg["button"]["btn_replace2"] = KnjCreateBtn($objForm, "btn_replace2", "スムーズな移行に向けての支援", $extra.$disabled);

    //進路指導計画ボタン
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform4_careerguidance&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_replace3\" onclick=\"current_cursor('btn_replace3'); window.open('$link','_self');\"";
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
    $arg["button"]["btn_replace3"] = KnjCreateBtn($objForm, "btn_replace3", "進路指導計画", $extra.$disabled);

    //発達検査ボタン
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform4_check&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_replace_1\" onclick=\"current_cursor('btn_replace_1'); window.open('$link','_self');\"";
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
    $arg["button"]["btn_replace_1"] = KnjCreateBtn($objForm, "btn_replace_1", "発達検査", $extra.$disabled);

    //スムーズな以降に向けての支援ボタン
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform4_smooth&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_replace2_1\" onclick=\"current_cursor('btn_replace2_1'); window.open('$link','_self');\"";
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
    $arg["button"]["btn_replace2_1"] = KnjCreateBtn($objForm, "btn_replace2_1", "スムーズな移行に向けての支援", $extra.$disabled);

    //進路指導計画ボタン
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform4_careerguidance&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_replace3_1\" onclick=\"current_cursor('btn_replace3_1'); window.open('$link','_self');\"";
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
    $arg["button"]["btn_replace3_1"] = KnjCreateBtn($objForm, "btn_replace3_1", "進路指導計画", $extra.$disabled);

}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
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

//テキストボックス or テキストエリア作成
// Add by PP for PC-Talker  2020-02-03 start
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $setLabel="") {
// Add by PP for PC-Talker  2020-02-20 end
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
        // Add by PP for PC-Talker  2020-02-03 start
        $extra = "style=\"height:".$height."px; overflow:auto;\" id=\"".$name."\" aria-label=\"$setLabel\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        // Add by PP for PC-Talker  2020-02-03 start
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\" aria-label=\"$setLabel\"";
        // Add by PP for PC-Talker  2020-02-20 end
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
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
?>
