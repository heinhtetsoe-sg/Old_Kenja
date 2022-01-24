<?php

require_once('for_php7.php');

class knje390SubForm8
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform8", "POST", "knje390index.php", "", "subform8");

        //DB接続
        $db = Query::dbCheckOut();
        
        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;
        }
        //新規作成時は全て項目をNULLにする
        if ($model->cmd === 'subform8_formatnew') {
            $model->record_date = "";
            $model->field8 = array();
            $newflg = "1";
        }
        //通常の場合は最新版を表示
        if ($model->record_date == "" && $model->field8["WRITING_DATE"] == "" && $model->cmd !== 'subform8_formatnew') {
            //ログイン年度の最新データをセット
            $getMaxDate = $db->getOne(knje390Query::getMaxRecordData8Query($model));
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
        if($info["NAME_SHOW"] != ""){
            $arg["TITLE"] = $info["NAME_SHOW"]."の関係者間資料画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // textbox 915 error
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubForm8_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubForm8_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJE390SubForm8_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform8" || $model->cmd == "subform8A" || $model->cmd == "subform8_clear"){
            if (isset($model->schregno) && !isset($model->warning) && $model->record_date != ""){
                $Row = $db->getRow(knje390Query::getSubQuery8($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field8;
            }
        } else {
            $Row =& $model->field8;
        }

        //作成年月日
        $set1_3monthYear = $model->main_year+1;
        knjCreateHidden($objForm, "SDATE", $model->main_year.'/04/01');
        knjCreateHidden($objForm, "EDATE", $set1_3monthYear.'/03/31');
        if ($Row["WRITING_DATE"]) {
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = " aria-label=\"作成年月日\" STYLE=\"background:darkgray\" readOnly ";
            // Add by PP for PC-Talker 2020-02-20 end
            $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
            $arg["data"]["SPACE"] = ' ';
            $arg["data"]["WRITING_DATE"] = knjCreateTextBox($objForm, $Row["WRITING_DATE"], "WRITING_DATE", 12, 12, $extra);
        } else {
            $extra = "";
            $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
            $arg["data"]["WRITING_DATE"] = View::popUpCalendar($objForm, "WRITING_DATE", $Row["WRITING_DATE"]);
        }

        //記入者
        $extra = "aria-label=\"記入者\"";
        $arg["data"]["WRITER"] = knjCreateTextBox($objForm, $Row["WRITER"], "WRITER", 36, 36, $extra);
        //会議名
        $extra = "aria-label=\"会議名\"";
        $arg["data"]["MEETING_NAME"] = knjCreateTextBox($objForm, $Row["MEETING_NAME"], "MEETING_NAME", 80, 80, $extra);
        //打合せ日時
        $Row["MEETING_DATE"] = str_replace("-", "/", $Row["MEETING_DATE"]);
        $arg["data"]["MEETING_DATE"] = View::popUpCalendar($objForm, "MEETING_DATE", $Row["MEETING_DATE"], "", "打合せ日時");
        //開始時間
        // Add by PP for PC-Talker(TextArea) 2020-02-03 start
        $extra = "aria-label=\"開始　　じ\"";
        // Add by PP for PC-Talker(TextArea) 2020-02-20 end
        $query = knje390Query::getTimeHour();
        makeCmb($objForm, $arg, $db, $query, "MEETING_SHOUR", $Row["MEETING_SHOUR"], $extra, 1, 1);
        // Add by PP for PC-Talker(TextArea) 2020-02-03 start
        $extra = "aria-label=\"開始　　分\"";
        // Add by PP for PC-Talker(TextArea) 2020-02-20 end
        $query = knje390Query::getTimeMinutes();
        makeCmb($objForm, $arg, $db, $query, "MEETING_SMINUTES", $Row["MEETING_SMINUTES"], $extra, 1, 1);
        //終了時間
        // Add by PP for PC-Talker(TextArea) 2020-02-03 start
        $extra = "aria-label=\"終了　　じ\"";
        // Add by PP for PC-Talker(TextArea) 2020-02-20 end
        $query = knje390Query::getTimeHour();
        makeCmb($objForm, $arg, $db, $query, "MEETING_EHOUR", $Row["MEETING_EHOUR"], $extra, 1, 1);
        // Add by PP for PC-Talker(TextArea) 2020-02-03 start
        $extra = "aria-label=\"終了　　分\"";
        // Add by PP for PC-Talker(TextArea) 2020-02-20 end
        $query = knje390Query::getTimeMinutes();
        makeCmb($objForm, $arg, $db, $query, "MEETING_EMINUTES", $Row["MEETING_EMINUTES"], $extra, 1, 1);
        //打合せ場所
        // Add by PP for PC-Talker(Textbox) 2020-02-03 start
        $extra = "aria-label=\"打合せ場所\"";
        $arg["data"]["MEETING_PALCE"] = knjCreateTextBox($objForm, $Row["MEETING_PALCE"], "MEETING_PALCE", 36, 36, $extra);
        // Add by PP for PC-Talker(Textbox) 2020-02-20 end
        //参加者
        // Add by PP for PC-Talker(TextArea) 2020-02-03 start
        $comment = "全角40文字2行まで";
        $extra = "style=\"height:40px; overflow:auto;\" aria-label=\"参加者 $comment\"";
        $arg["data"]["MEETING_PARTICIPANT"] = knjCreateTextArea($objForm, "MEETING_PARTICIPANT", 2, 81, "soft", $extra, $Row["MEETING_PARTICIPANT"]);
        $arg["data"]["MEETING_PARTICIPANT_SIZE"] = '<font size="1" color="red">(全角40文字2行まで)</font>';
        // Add by PP for PC-Talker(TextArea) 2020-02-20 end

        // Add by PP for PC-Talker(TextArea) 2020-02-03 start
        $extra = "style=\"height:150px; overflow:auto;\"";
        $comment = "全角40文字10行まで";
        //問題事象
        $arg["data"]["PROBLEM_EVENT_DOCUMENTS"] = knjCreateTextArea($objForm, "PROBLEM_EVENT_DOCUMENTS", 10, 81, "soft", $extra."aria-label=\"問題事象 $comment\"", $Row["PROBLEM_EVENT_DOCUMENTS"]);
        //事象分析
        $arg["data"]["PROBLEM_ANALYSIS_DOCUMENTS"] = knjCreateTextArea($objForm, "PROBLEM_ANALYSIS_DOCUMENTS", 10, 81, "soft", $extra."aria-label=\"事象分析 $comment\"", $Row["PROBLEM_ANALYSIS_DOCUMENTS"]);
        //課題対応
        $arg["data"]["PROBLEM_DEAL_DOCUMENTS"] = knjCreateTextArea($objForm, "PROBLEM_DEAL_DOCUMENTS", 10, 81, "soft", $extra."aria-label=\"課題対応 $comment\"", $Row["PROBLEM_DEAL_DOCUMENTS"]);
        //指導者の役割等
        $arg["data"]["STAFF_ROLE_DOCUMENTS"] = knjCreateTextArea($objForm, "STAFF_ROLE_DOCUMENTS", 10, 81, "soft", $extra."aria-label=\"指導者の役割等 $comment\"", $Row["STAFF_ROLE_DOCUMENTS"]);
        //保護者連携
        $arg["data"]["GUARDIAN_COOPERATION_DOCUMENTS"] = knjCreateTextArea($objForm, "GUARDIAN_COOPERATION_DOCUMENTS", 10, 81, "soft", $extra."aria-label=\"保護者連携 $comment\"", $Row["GUARDIAN_COOPERATION_DOCUMENTS"]);
        //福祉連携
        $arg["data"]["WELFARE_COOPERATION_DOCUMENTS"] = knjCreateTextArea($objForm, "WELFARE_COOPERATION_DOCUMENTS", 10, 81, "soft", $extra."aria-label=\"福祉連携 $comment\"", $Row["WELFARE_COOPERATION_DOCUMENTS"]);
        //その他
        $arg["data"]["OTHERS_REMARK_DOCUMENTS"] = knjCreateTextArea($objForm, "OTHERS_REMARK_DOCUMENTS", 10, 81, "soft", $extra."aria-label=\"その他 $comment\"", $Row["OTHERS_REMARK_DOCUMENTS"]);
        $arg["data"]["DOCUMENTS_SIZE"] = '<font size="1" color="red">(全角40文字10行まで)</font>';
        // Add by PP for PC-Talker(TextArea) 2020-02-20 end

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
        View::toHTML($model, "knje390SubForm8.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData, $Row)
{
    //新規作成ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_formatnew\" onclick=\"current_cursor('btn_formatnew'); return btn_submit('subform8_formatnew');\"";
    $arg["button"]["btn_formatnew"] = knjCreateBtn($objForm, "btn_formatnew", "新規作成", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('subform8_updatemain');\" aria-label='更新'";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する (second btn)
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update_1\" onclick=\"current_cursor('btn_update_1'); return btn_submit('subform8_updatemain');\" aria-label='更新'";
    $arg["button"]["btn_update_1"] = knjCreateBtn($objForm, "btn_update_1", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('subform8_clear');\" aria-label='取消'";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する (second btn)
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset_1\" onclick=\"current_cursor('btn_reset_1'); return btn_submit('subform8_clear');\" aria-label='取消'";
    $arg["button"]["btn_reset_1"] = knjCreateBtn($objForm, "btn_reset_1", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //戻るボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "onclick=\"return btn_submit('edit');\" aria-label='戻る'";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
