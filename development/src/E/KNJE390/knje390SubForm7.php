<?php

require_once('for_php7.php');

class knje390SubForm7
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform7", "POST", "knje390index.php", "", "subform7");

        //DB接続
        $db = Query::dbCheckOut();
        
        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;
        }
        //新規作成時は全て項目をNULLにする
        if ($model->cmd === 'subform7_formatnew') {
            $model->record_date = "";
            $model->field7 = array();
            $newflg = "1";
        }
        //通常の場合は最新版を表示
        if ($model->record_date == "" && $model->field7["WRITING_DATE"] == "" && $model->cmd !== 'subform7_formatnew') {
            //ログイン年度の最新データをセット
            $getMaxDate = $db->getOne(knje390Query::getMaxRecordData7Query($model));
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
            $arg["TITLE"] = $info["NAME_SHOW"]."の引継ぎ資料(事業者)画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // textbox 915 error
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubForm7_CurrentCursor915\");</script>";
        } else {
          echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubForm7_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJE390SubForm7_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        //種別
        // Add by PP for PC-Talker and current cursor 2020-02-03 start
        $extra = "id=\"DATA_DIV\" onchange=\"current_cursor('DATA_DIV'); return btn_submit('subform7A')\"";
        // Add by PP for PC-Talker and current cursor 2020-02-20 end
        $query = knje390Query::getNameMst("E043");
        makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->field7["DATA_DIV"], $extra, 1, "");

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform7" || $model->cmd == "subform7A" || $model->cmd == "subform7_clear"){
            if (isset($model->schregno) && !isset($model->warning) && $model->record_date != ""){
                $Row = $db->getRow(knje390Query::getSubQuery7($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field7;
            }
        } else {
            $Row =& $model->field7;
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

        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "style=\"height:125px; overflow:auto;\"";
        $comment = "全角11文字8行まで";
        //概要
        $arg["data"]["OUTLINE"] = knjCreateTextArea($objForm, "OUTLINE", 8, 23, "soft", $extra."aria-label=\"概要 $comment\"", $Row["OUTLINE"]);
        //方策
        $arg["data"]["MEASURE"] = knjCreateTextArea($objForm, "MEASURE", 8, 23, "soft", $extra."aria-label=\"方策 $comment\"", $Row["MEASURE"]);
        //今後の展開
        $arg["data"]["FUTURE_DEVELOPMENT"] = knjCreateTextArea($objForm, "FUTURE_DEVELOPMENT", 8, 23, "soft", $extra."aria-label=\"今後の展開 $comment\"", $Row["FUTURE_DEVELOPMENT"]);
        //備考
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", 8, 23, "soft", $extra."aria-label=\"備考 $comment\"", $Row["REMARK"]);
        $arg["data"]["TEXT_SIZE"] = '<font size="1" color="red">(全角11文字8行まで)</font>';
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
        View::toHTML($model, "knje390SubForm7.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData, $Row)
{
    //新規作成ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_formatnew\" onclick=\"current_cursor('btn_formatnew'); return btn_submit('subform7_formatnew');\"";
    $arg["button"]["btn_formatnew"] = knjCreateBtn($objForm, "btn_formatnew", "新規作成", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('subform7_updatemain');\" aria-label='更新'";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //更新ボタンを作成する
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_update_1\" onclick=\"current_cursor('btn_update_1'); return btn_submit('subform7_updatemain');\" aria-label='更新'";
    $arg["button"]["btn_update_1"] = knjCreateBtn($objForm, "btn_update_1", "更 新", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する (second btn)
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('subform7_clear');\" aria-label='取消'";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //クリアボタンを作成する (second btn)
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "id=\"btn_reset_1\" onclick=\"current_cursor('btn_reset_1'); return btn_submit('subform7_clear');\" aria-label='取消'";
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
