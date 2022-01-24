<?php

require_once('for_php7.php');

class knje390SubForm3
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3", "POST", "knje390index.php", "", "subform3");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["TokushiShienPlanPatern"] == "1") {
            $arg["TokushiShienPlanPatern"] = "1";
        } else {
            $arg["NotTokushiShienPlanPatern"] = "1";
        }

        if ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") {
            $arg["useFormNameE390_B_1"] = "1";
        } else {
            $arg["NotuseFormNameE390_B_1"] = "1";
        }

        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;
        }
        if (!$model->record_date) {
            $model->record_date = 'NEW';
        }
        //表示日付をセット
        if ($model->record_date === 'NEW' && $model->main_year === CTRL_YEAR) {
            $setHyoujiDate = '';
        } else {
            if ($model->record_date === 'NEW') {
                $setHyoujiDate = '　　<font color="RED"><B>'.$model->main_year.'年度 最終更新データ 参照中</B></font>';
            } else {
                $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
            }
        }

        //年度表示
        $arg["data"]["NENDO"] = $model->main_year.'年度';

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;
        // Add by PP for Title 2020-02-03 start
         if($info["NAME_SHOW"] != ""){
            $arg["TITLE"] = $info["NAME_SHOW"]."のC 支援内容･計画画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // Add by PP for Title 2020-02-20 end

        //項目名取得
        $label = array();
        $maxDataDiv = 0;
        $query = knje390Query::getChallengedSupportplanStatusDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["DATA_DIV"] == "0") {
                $label["STATUS_NAME"]   = $row["STATUS_NAME"];
                $label["STATUS2_NAME"]  = $row["STATUS2_NAME"];
                $label["STATUS3_NAME"]  = $row["STATUS3_NAME"];
                $label["STATUS4_NAME"]  = $row["STATUS4_NAME"];
            } else {
                $label["DATA_DIV_NAME".$row["DATA_DIV"]] = $row["DATA_DIV_NAME"];
                $maxDataDiv = $row["DATA_DIV"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "MAX_DATA_DIV", $maxDataDiv);

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform3" || $model->cmd == "subform3A" || $model->cmd == "subform3_clear" || $model->cmd == "subform3_yomikomi"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390Query::getSubQuery3($model, $maxDataDiv), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field3;
            }
        } else {
            $Row =& $model->field3;
        }
        
        //作成年月日
        $set1_3monthYear = $model->main_year+1;
        knjCreateHidden($objForm, "SDATE", $model->main_year.'/04/01');
        knjCreateHidden($objForm, "EDATE", $set1_3monthYear.'/03/31');
        $extra = "";
        $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
        $arg["data"]["WRITING_DATE"] = View::popUpCalendar($objForm, "WRITING_DATE", $Row["WRITING_DATE"]);

        //読込年度
        $query = knje390Query::getMainYear($model, "C");
        makeCmb($objForm, $arg, $db, $query, "YOMIKOMI_YEAR", $model->field3["YOMIKOMI_YEAR"], $extra, 1, "");

        //学校生活への期待や願い
        //本人
        $moji = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ? 50 : 50;
        $gyou = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ?  3 :  1;
        $arg["data"]["ONES_HOPE_PRESENT"] = getTextOrArea($objForm, "ONES_HOPE_PRESENT", $moji, $gyou, $Row["ONES_HOPE_PRESENT"], $model);
        setInputChkHidden($objForm, "ONES_HOPE_PRESENT", $moji, $gyou, $arg);

        //保護者
        $arg["data"]["GUARDIAN_HOPE_PRESENT"] = getTextOrArea($objForm, "GUARDIAN_HOPE_PRESENT", 50, 1, $Row["GUARDIAN_HOPE_PRESENT"], $model);
        setInputChkHidden($objForm, "GUARDIAN_HOPE_PRESENT", 50, 1, $arg);
        if($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") {
            knjCreateHidden($objForm, "GUARDIAN_HOPE_PRESENT", $Row["GUARDIAN_HOPE_PRESENT"]);
        }

        //支援目標
        $moji = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ? 50 : 50;
        $gyou = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ?  4 :  2;
        $arg["data"]["SUPPORT_GOAL"] = getTextOrArea($objForm, "SUPPORT_GOAL", $moji, $gyou, $Row["SUPPORT_GOAL"], $model);
        setInputChkHidden($objForm, "SUPPORT_GOAL", $moji, $gyou, $arg);

        //項目名（横）
        $arg["data"]["STATUS_NAME"]     = $label["STATUS_NAME"];
        $arg["data"]["STATUS2_NAME"]    = $label["STATUS2_NAME"];
        $arg["data"]["STATUS3_NAME"]    = $label["STATUS3_NAME"];
        $arg["data"]["STATUS4_NAME"]    = $label["STATUS4_NAME"];

        if ($maxDataDiv > 0) {
            for ($i = 1; $i <= $maxDataDiv; $i++) {
                $setTmp = array();

                if ($i == 1) {
                    $setTmp["ROW_NAME"] = "　<br>具<br>　<br>体<br>　<br>的<br>　<br>な<br>　<br>支<br>　<br>援<br>　";
                    if ($maxDataDiv != 1) $setTmp["ROWSPAN"] = "rowspan=\"".$maxDataDiv."\"";
                }

                //項目名（縦）
                $extra = "";
                $setTmp["DATA_DIV_NAME"] = $label["DATA_DIV_NAME".$i];

                //extra
                $extra = "style=\"height:210px;\"";

                //テキスト（左）
                $moji = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ? 25 : 25;
                $gyou = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ? 10 : 15;
                $setTmp["STATUS"] = getTextOrArea($objForm, "DIV".$i."_STATUS", $moji, $gyou, $Row["DIV".$i."_STATUS"], $model);
                $setTmp["STATUS_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_STAT", "statusarea_DIV".$i."_STATUS");
                //テキスト（中）
                $moji = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ? 12 : 12;
                $gyou = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ? 10 : 15;
                $setTmp["STATUS2"] = getTextOrArea($objForm, "DIV".$i."_STATUS2", $moji, $gyou, $Row["DIV".$i."_STATUS2"], $model);
                $setTmp["STATUS2_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS2_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS2_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS2_STAT", "statusarea_DIV".$i."_STATUS2");
                //テキスト（右）
                $moji = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ? 12 : 12;
                $gyou = ($model->Properties["useFormNameE390_B_1"] == "KNJE390_B_1_2.frm") ? 10 : 15;
                $setTmp["STATUS3"] = getTextOrArea($objForm, "DIV".$i."_STATUS3", $moji, $gyou, $Row["DIV".$i."_STATUS3"], $model);
                $setTmp["STATUS3_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS3_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS3_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS3_STAT", "statusarea_DIV".$i."_STATUS3");

                $setTmp["CNT"] = $i;

                $arg["data2"][] = $setTmp;
            }
        }

        //評価・連携の記録
        $arg["data"]["RECORD"] = getTextOrArea($objForm, "RECORD", 50, 10, $Row["RECORD"], $model);
        setInputChkHidden($objForm, "RECORD", 50, 10, $arg);

        //履歴用日付
        $model->field3["BACKUP_DATE"] = str_replace("-", "/", $model->field3["BACKUP_DATE"]);
        $arg["data"]["BACKUP_DATE"] = View::popUpCalendar($objForm, "BACKUP_DATE", $model->field3["BACKUP_DATE"]);
        
        //データをカウント
        $mainCountData = knje390Query::getCheckMainDataQuery($db, $model, "3");

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData);

        //hidden作成
        makeHidden($objForm, $db, $model);

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
        View::toHTML5($model, "knje390SubForm3.html", $arg);
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

    if ($model->Properties["TokushiShienPlanPatern"] == "1") {
        //1.現在の生活および将来の生活に関する希望
        $link = REQUESTROOT."/E/KNJE460_SEIKATU_ZYOUHOU/knje460_seikatu_zyouhouindex.php?mode=1&SEND_PRGID=KNJE390&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->main_year}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "id=\"seikatu_zyouhou\" onclick=\"document.location.href='$link';current_cursor('seikatu_zyouhou');\"";
        $arg["button"]["btn_seikatu_zyouhou"] = KnjCreateBtn($objForm, "btn_seikatu_zyouhou", "1.現在の生活････", $extra);

        //2.支援をする上での基礎情報
        $link = REQUESTROOT."/E/KNJE460_KISO_ZYOUHOU/knje460_kiso_zyouhouindex.php?mode=1&SEND_PRGID=KNJE390&SEND_AUTH={$model->auth}&SEND_selectSchoolKind={$model->selectSchoolKind}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->main_year}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "id=\"kiso_zyouhou\" onclick=\"document.location.href='$link';current_cursor('kiso_zyouhou');\"";
        $arg["button"]["btn_kiso_zyouhou"] = KnjCreateBtn($objForm, "btn_kiso_zyouhou", "2.支援をする････", $extra);

        //3.合理的配慮
        $link = REQUESTROOT."/E/KNJE460_GOURITEKI_HAIRYO/knje460_gouriteki_hairyoindex.php?mode=1&SEND_PRGID=KNJE390&SEND_AUTH={$model->auth}&SEND_selectSchoolKind={$model->selectSchoolKind}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->main_year}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "id=\"gouriteki_hairyo\" onclick=\"document.location.href='$link';current_cursor('gouriteki_hairyo');\"";
        $arg["button"]["btn_gouriteki_hairyo"] = KnjCreateBtn($objForm, "btn_gouriteki_hairyo", "3.合理的配慮", $extra);

        //4.3年後に目指したい自立の姿
        $link = REQUESTROOT."/E/KNJE460_ZIRITU/knje460_zirituindex.php?mode=1&SEND_PRGID=KNJE390&SEND_AUTH={$model->auth}&SEND_selectSchoolKind={$model->selectSchoolKind}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->main_year}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "id=\"ziritu\" onclick=\"document.location.href='$link';current_cursor('ziritu');\"";
        $arg["button"]["btn_ziritu"] = KnjCreateBtn($objForm, "btn_ziritu", "4.3年後に目････", $extra);

        //5.各関係機関からの具体的な支援について
        $link = REQUESTROOT."/E/KNJE460_SIEN_KIKAN/knje460_sien_kikanindex.php?mode=1&SEND_PRGID=KNJE390&SEND_AUTH={$model->auth}&SEND_selectSchoolKind={$model->selectSchoolKind}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->main_year}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "id=\"sien_kikan\" onclick=\"document.location.href='$link';current_cursor('sien_kikan');\"";
        $arg["button"]["btn_sien_kikan"] = KnjCreateBtn($objForm, "btn_sien_kikan", "5.各関係機関････", $extra);
    }

    //読込ボタンを作成する
    // Add by PP PC-Talker 2020-02-03 start
    $extra = "onclick=\"return btn_submit('subform3_yomikomi');\" aria-label='読込'";
    $arg["button"]["btn_yomikomi"] = knjCreateBtn($objForm, "btn_yomikomi", "読 込", $extra);
    // Add by PP PC-Talker focus 2020-02-20 end

    //履歴ボタンを作成する
    // Add by PP PC-Talker 2020-02-03 start
    $extra = "onclick=\"return btn_submit('subform3_rireki');\" aria-label='更新(履歴)'";
    $arg["button"]["btn_rireki"] = knjCreateBtn($objForm, "btn_rireki", "更新(履歴)", $extra.$disabled);
    // Add by PP PC-Talker focus 2020-02-20 end

    //更新ボタンを作成する
    // Add by PP PC-Talker 2020-02-03 start
    $extra = "onclick=\"return btn_submit('subform3_updatemain');\" aria-label='更新'";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP PC-Talker focus 2020-02-20 end

    //クリアボタンを作成する
    // Add by PP PC-Talker 2020-02-03 start
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('subform3_clear');\" aria-label='取消'";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    // Add by PP PC-Talker focus 2020-02-20 end

    //戻るボタン
    // Add by PP PC-Talker 2020-02-03 start
    $extra = "onclick=\"return btn_submit('edit');\" aria-label='戻る'";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
    // Add by PP PC-Talker focus 2020-02-20 end
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
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
