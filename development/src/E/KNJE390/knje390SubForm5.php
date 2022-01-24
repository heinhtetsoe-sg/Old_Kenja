<?php

require_once('for_php7.php');

class knje390SubForm5
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform5", "POST", "knje390index.php", "", "subform5");

        //DB接続
        $db = Query::dbCheckOut();
        
        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;
        }
        //新規作成時は全て項目をNULLにする
        if ($model->cmd === 'subform5_formatnew') {
            $model->record_date = "";
            $model->field5 = array();
            $newflg = "1";
        }
        //通常の場合は最新版を表示
        if ($model->record_date == "" && $model->field5["WRITING_DATE"] == "" && $model->cmd !== 'subform5_formatnew') {
            //ログイン年度の最新データをセット
            $getMaxDate = $db->getOne(knje390Query::getMaxRecordData5Query($model));
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
            $arg["TITLE"] = $info["NAME_SHOW"]."のサポートブック画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // for 915 error
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubForm5_CurrentCursor915\");</script>";
        } else {
          echo "<script>var error195= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubForm5_CurrentCursor915\", error195);
              sessionStorage.removeItem(\"KNJE390SubForm5_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform5" || $model->cmd == "subform5A" || $model->cmd == "subform5_clear"){
            if (isset($model->schregno) && !isset($model->warning) && $model->record_date != ""){
                $Row = $db->getRow(knje390Query::getSubQuery5($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field5;
            }
        } else {
            $Row =& $model->field5;
        }

        //作成年月日
        $set1_3monthYear = $model->main_year+1;
        knjCreateHidden($objForm, "SDATE", $model->main_year.'/04/01');
        knjCreateHidden($objForm, "EDATE", $set1_3monthYear.'/03/31');

        if ($Row["WRITING_DATE"]) {
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = " aria-label=\"作成年月日\" STYLE=\"background:darkgray\"  readOnly ";
            // Add by PP for PC-Talker 2020-02-20 end
            $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
            $arg["data"]["SPACE"] = ' ';
            $arg["data"]["WRITING_DATE"] = knjCreateTextBox($objForm, $Row["WRITING_DATE"], "WRITING_DATE", 12, 12, $extra);
        } else {
            $extra = "";
            $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
            $arg["data"]["WRITING_DATE"] = View::popUpCalendar($objForm, "WRITING_DATE", $Row["WRITING_DATE"]);
        }
        
        //障害名
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"CHALLENGED_NAMES\" style=\"height:40px; overflow:auto;\" aria-label='障害名 全角40文字2行まで'";
        $arg["data"]["CHALLENGED_NAMES"] = knjCreateTextArea($objForm, "CHALLENGED_NAMES", 2, 81, "soft", $extra, $Row["CHALLENGED_NAMES"]);
        $arg["data"]["CHALLENGED_NAMES_SIZE"] = '<font size="1" color="red">(全角40文字2行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end
        
        //呼び名
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:40px; overflow:auto;\" aria-label='呼び名 全角40文字2行まで'";
        $arg["data"]["CHALLENGED_POPULAR_NAME"] = knjCreateTextArea($objForm, "CHALLENGED_POPULAR_NAME", 2, 81, "soft", $extra, $Row["CHALLENGED_POPULAR_NAME"]);
        $arg["data"]["CHALLENGED_POPULAR_NAME_SIZE"] = '<font size="1" color="red">(全角40文字2行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end

        //持病名
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:90px; overflow:auto;\" aria-label='持病名 全角40文字5行まで'";
        $arg["data"]["CHRONIC_DISEASE"] = knjCreateTextArea($objForm, "CHRONIC_DISEASE", 5, 81, "soft", $extra, $Row["CHRONIC_DISEASE"]);
        $arg["data"]["CHRONIC_DISEASE_SIZE"] = '<font size="1" color="red">(全角40文字5行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end

        //コミュニケーション(会話)の仕方
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:100px; overflow:auto;\" aria-label='コミュニケーション(会話)の仕方 全角40文字6行まで'";
        $arg["data"]["HOW_COMMUNICATION"] = knjCreateTextArea($objForm, "HOW_COMMUNICATION", 6, 81, "soft", $extra, $Row["HOW_COMMUNICATION"]);
        $arg["data"]["HOW_COMMUNICATION_SIZE"] = '<font size="1" color="red">(全角40文字6行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end

        //困った行動の対処法
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:115px; overflow:auto;\" aria-label='困った行動の対処法 全角40文字7行まで'";
        $arg["data"]["TROUBLED_BEHAVIOR_SUPPORT"] = knjCreateTextArea($objForm, "TROUBLED_BEHAVIOR_SUPPORT", 7, 81, "soft", $extra, $Row["TROUBLED_BEHAVIOR_SUPPORT"]);
        $arg["data"]["TROUBLED_BEHAVIOR_SUPPORT_SIZE"] = '<font size="1" color="red">(全角40文字7行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end

        //不調のサイン
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:115px; overflow:auto;\" aria-label='不調のサイン 全角40文字7行まで'";
        $arg["data"]["BAD_CONDITION_SIGN"] = knjCreateTextArea($objForm, "BAD_CONDITION_SIGN", 7, 81, "soft", $extra, $Row["BAD_CONDITION_SIGN"]);
        $arg["data"]["BAD_CONDITION_SIGN_SIZE"] = '<font size="1" color="red">(全角40文字7行まで)</font>';
        // Add by PP for PC-Talker 2020-02-20 end

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
        View::toHTML($model, "knje390SubForm5.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData, $Row)
{
    //障害名･診断名マスタ
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_statusname\" onclick=\"current_cursor('btn_statusname'); loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名･診断名マスタ参照", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //新規作成ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_formatnew\" onclick=\"current_cursor('btn_formatnew'); return btn_submit('subform5_formatnew');\"";
    $arg["button"]["btn_formatnew"] = knjCreateBtn($objForm, "btn_formatnew", "新規作成", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('subform5_updatemain');\" aria-label='更新'";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update_1\" onclick=\"current_cursor('btn_update_1'); return btn_submit('subform5_updatemain');\" aria-label='更新'";
    $arg["button"]["btn_update_1"] = knjCreateBtn($objForm, "btn_update_1", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('subform5_clear');\" aria-label='取消'";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset_1\" onclick=\"current_cursor('btn_reset_1'); return btn_submit('subform5_clear');\" aria-label='取消'";
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
