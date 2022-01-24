<?php

require_once('for_php7.php');

class knje390SubForm2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knje390index.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();
        
        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;
        }
        //新規作成時は全て項目をNULLにする
        if ($model->cmd === 'subform2_formatnew') {
            $model->record_date = "";
            $model->field2 = array();
            $newflg = "1";
        }
        //通常の場合は最新版を表示
        if ($model->record_date == "" && $model->field2["WRITING_DATE"] == "" && $model->cmd !== 'subform2_formatnew') {
            //ログイン年度の最新データをセット
            $getMaxDate = $db->getOne(knje390Query::getMaxRecordDataQuery($model, $model->main_year));
            $model->record_date = $getMaxDate;
            $newflg = "";
        }
        knjCreateHidden($objForm, "NEW_FLG", $newflg);

        //年度表示
        $arg["data"]["NENDO"] = $model->main_year.'年度';

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];
        // Add by PP for Title 2020-02-03 start
        $arg["TITLE"] = $info["NAME_SHOW"]."のA アセスメント表画面";
         if($info["NAME_SHOW"] != ""){
            $arg["TITLE"] = $info["NAME_SHOW"]."のA アセスメント表画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubForm2_CurrentCursor915\");</script>";
        } else {
          echo "<script>var error915= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubForm2_CurrentCursor915\", error915);
              sessionStorage.removeItem(\"KNJE390SubForm2_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        //項目名取得
        $label = array();
        $maxDataDiv = 0;
        $query = knje390Query::getChallengedAssessmentStatusGrowupDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["DATA_DIV"] == "0") {
                $label["STATUS_NAME"] = $row["STATUS_NAME"];
                $label["GROWUP_NAME"] = $row["GROWUP_NAME"];
            } else {
                $label["DATA_DIV_NAME".$row["DATA_DIV"]] = $row["DATA_DIV_NAME"];
                $maxDataDiv = $row["DATA_DIV"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "MAX_DATA_DIV", $maxDataDiv);

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform2" || $model->cmd == "subform2A" || $model->cmd == "subform2_clear" || $model->cmd == "subform2_yomikomi"){
            if (isset($model->schregno) && !isset($model->warning) && $model->record_date != ""){
                if ($model->cmd == "subform2_yomikomi") {
                    $getMaxDateYomikomi = $db->getOne(knje390Query::getMaxRecordDataQuery($model, $model->field2["YOMIKOMI_YEAR"]));
                    $Row = $db->getRow(knje390Query::getSubQuery2($model, $getMaxDateYomikomi), DB_FETCHMODE_ASSOC);
                } else {
                    $Row = $db->getRow(knje390Query::getSubQuery2($model), DB_FETCHMODE_ASSOC);
                }
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field2;
            }
        } else {
            $Row =& $model->field2;
        }

        //作成年月日
        $set1_3monthYear = $model->main_year+1;
        knjCreateHidden($objForm, "SDATE", $model->main_year.'/04/01');
        knjCreateHidden($objForm, "EDATE", $set1_3monthYear.'/03/31');
        if ($Row["WRITING_DATE"]) {
            $extra = " aria-label=\"年度\" STYLE=\"background:darkgray\" readOnly ";
            // Add by PP for PC-Talker 2020-02-03 start
            $setextra = "aria-label=\"作成年月日\"";
            $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
            $arg["data"]["SPACE"] = ' ';
            $arg["data"]["WRITING_DATE"] = getTextOrArea($objForm, "WRITING_DATE", 6, 1, $Row["WRITING_DATE"], $model, $setextra);
            // Add by PP for PC-Talker 2020-02-20 end
        } else {
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = "";
            $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
            $arg["data"]["WRITING_DATE"] = View::popUpCalendar($objForm, "WRITING_DATE", $Row["WRITING_DATE"]);
            // Add by PP for PC-Talker 2020-02-20 end
        }

        //読込年度
        $query = knje390Query::getMainYear($model, "A");
        makeCmb($objForm, $arg, $db, $query, "YOMIKOMI_YEAR", $model->field2["YOMIKOMI_YEAR"], $extra, 1, "");

        //B プロフィールデータ日付コンボ
        $extra = "";
        $query = knje390Query::getTorikomiRecordDate($model, "B");
        makeCmb($objForm, $arg, $db, $query, "TORIKOMI_B_DATE", $model->field2["TORIKOMI_B_DATE"], $extra, 1, "");
        
        //プロフィールの障害名等、障害の実態･特性の取込
        if ($model->cmd == "subform2_torikomi_main") {
            $getRow = $db->getRow(knje390Query::getSubQuery1($model, $model->field2["TORIKOMI_B_DATE"]), DB_FETCHMODE_ASSOC);
            $Row["CHALLENGED_NAMES"] = $getRow["CHALLENGED_NAMES"];
            $Row["CHALLENGED_STATUS"] = $getRow["CHALLENGED_STATUS"];
        }
        //障害名等
        $moji = ($model->Properties["useFormNameE390_D_1"] == "KNJE390_D_1_2.frm") ? 17 : 15;
        $gyou = ($model->Properties["useFormNameE390_D_1"] == "KNJE390_D_1_2.frm") ?  4 :  6;
        // Add by PP for PC-Talker 2020-02-03 start
        $comment = getTextAreaComment($moji, $gyou);
        $comment_name = str_replace(array( '(', ')' ), '', $comment);
        $extra = "style=\"height:85px;\"";
        // cannot use style
        $extra_1 = "aria-label=\"障害名等$comment_name\"";
        $arg["data"]["CHALLENGED_NAMES"] = getTextOrArea($objForm, "CHALLENGED_NAMES", $moji, $gyou, $Row["CHALLENGED_NAMES"], $model, $extra_1);
        setInputChkHidden($objForm, "CHALLENGED_NAMES", $moji, $gyou, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //障害の実態･特性
        // Add by PP for PC-Talker 2020-02-03 start
        $comment = getTextAreaComment(50, 3);
        $comment_status = str_replace(array( '(', ')' ), '', $comment);
        $extra = "style=\"height:45px;\"";
        // cannot use style
        $extra_1 = "aria-label=\"障害の実態・特性$comment_status\"";
        $arg["data"]["CHALLENGED_STATUS"] = getTextOrArea($objForm, "CHALLENGED_STATUS", 50, 3, $Row["CHALLENGED_STATUS"], $model, $extra_1);
        setInputChkHidden($objForm, "CHALLENGED_STATUS", 50, 3, $arg);
        // Add by PP for PC-Talker 2020-02-20 end

        //項目名（横）
        $arg["data"]["STATUS_NAME"] = $label["STATUS_NAME"];
        $arg["data"]["GROWUP_NAME"] = $label["GROWUP_NAME"];

        if ($maxDataDiv > 0) {
            for ($i = 1; $i <= $maxDataDiv; $i++) {
                $setTmp = array();

                //項目名（縦）
                $setTmp["DATA_DIV_NAME"] = $label["DATA_DIV_NAME".$i];

                //extra
                $extra = "style=\"height:210px;\"";

                //テキスト（左）
                $moji = ($model->Properties["useFormNameE390_D_1"] == "KNJE390_D_1_2.frm") ? 33 : 33;
                $gyou = ($model->Properties["useFormNameE390_D_1"] == "KNJE390_D_1_2.frm") ? 15 : 30;
                // Add by PP for PC-Talker 2020-02-03 start
                $comment = getTextAreaComment($moji, $gyou);
                $comment_status = str_replace(array( '(', ')' ), '', $comment);
                $extra = "aria-label=\"{$label["STATUS_NAME"]}の{$label["DATA_DIV_NAME".$i]}{$comment_status}\"";
                $setTmp["STATUS"]       = getTextOrArea($objForm, "DIV".$i."_STATUS", $moji, $gyou, $Row["DIV".$i."_STATUS"], $model, $extra);
                // Add by PP for PC-Talker 2020-02-20 end
                $setTmp["STATUS_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_STAT", "statusarea_DIV".$i."_STATUS");

                //テキスト（右）
                $moji = ($model->Properties["useFormNameE390_D_1"] == "KNJE390_D_1_2.frm") ? 17 : 17;
                $gyou = ($model->Properties["useFormNameE390_D_1"] == "KNJE390_D_1_2.frm") ? 15 : 30;
                // Add by PP for PC-Talker 2020-02-03 start
                $comment = getTextAreaComment($moji, $gyou);
                $comment_growup = str_replace(array( '(', ')' ), '', $comment);
                $extra = "aria-label=\"{$label["GROWUP_NAME"]}の{$label["DATA_DIV_NAME".$i]}{$comment_growup}\"";
                $setTmp["GROWUP"]       = getTextOrArea($objForm, "DIV".$i."_GROWUP", $moji, $gyou, $Row["DIV".$i."_GROWUP"], $model, $extra);
                // Add by PP for PC-Talker 2020-02-20 end
                $setTmp["GROWUP_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_GROWUP_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_GROWUP_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_GROWUP_STAT", "statusarea_DIV".$i."_GROWUP");

                $setTmp["CNT"] = $i;

                $arg["data2"][] = $setTmp;
            }
        }

        //データをカウント
        $mainCountData = knje390Query::getCheckMainDataQuery($db, $model, "2");

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData, $Row);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        if(get_count($model->warning)== 0 && $model->cmd !="subform1_clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="subform1_clear"){
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML5($model, "knje390SubForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData, $Row)
{
    //データがないまたは作成年月日が指定していない場合は、更新、取消、戻る以外は使用不可
    if ($mainCountData == 0) {
        $disabled = "disabled";
    } else {
        if ($model->record_date != "" && $Row["WRITING_DATE"] != "") {
            $disabled = "";
        } else {
            $disabled = "disabled";
        }
    }

    //障害名等マスタ
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_statusname\" onclick=\"current_cursor('btn_statusname'); loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名等マスタ参照", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //B様式取込ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_Btorikomi\" onclick=\"current_cursor('btn_Btorikomi'); return btn_submit('subform2_torikomi_main');\"";
    $arg["button"]["btn_Btorikomi"] = KnjCreateBtn($objForm, "btn_Btorikomi", "B様式取込", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //読込ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_yomikomi\" onclick=\"current_cursor('btn_yomikomi'); return btn_submit('subform2_yomikomi');\" aria-label='読込'";
    $arg["button"]["btn_yomikomi"] = knjCreateBtn($objForm, "btn_yomikomi", "読 込", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //新規作成ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_formatnew\" onclick=\"current_cursor('btn_formatnew'); return btn_submit('subform2_formatnew');\"";
    $arg["button"]["btn_formatnew"] = knjCreateBtn($objForm, "btn_formatnew", "新規作成", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('subform2_updatemain');\" aria-label='更新'";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する (second btn)
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update_1\" onclick=\"current_cursor('btn_update_1'); return btn_submit('subform2_updatemain');\" aria-label='更新'";
    $arg["button"]["btn_update_1"] = knjCreateBtn($objForm, "btn_update_1", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('subform2_clear');\" aria-label='取消'";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する (second btn)
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset_1\" onclick=\"current_cursor('btn_reset_1'); return btn_submit('subform2_clear');\" aria-label='取消'";
    $arg["button"]["btn_reset_1"] = knjCreateBtn($objForm, "btn_reset_1", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //戻るボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "onclick=\"return btn_submit('edit');\" aria-label='戻る'";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
    
    //発達検査ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform2_check&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "id=\"btn_replace\" onclick=\"current_cursor('btn_replace'); window.open('$link','_self');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "発達検査", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //発達検査ボタン (second btn)
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $link = REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform2_check&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "id=\"btn_replace_1\" onclick=\"current_cursor('btn_replace_1'); window.open('$link','_self');\"";
    $arg["button"]["btn_replace_1"] = KnjCreateBtn($objForm, "btn_replace_1", "発達検査", $extra.$disabled);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
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
    $result1->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//テキストボックス or テキストエリア作成
// Add by PP for PC-Talker 2020-02-03 start
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $setExtra="") {
// Add by PP for PC-Talker 2020-02-20 end
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
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:".$height."px; overflow:auto;\" id=\"".$name."\" $setExtra";
        // Add by PP for PC-Talker 2020-02-20 end
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\" $setExtra";
        // Add by PP for PC-Talker 2020-02-20 end
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
