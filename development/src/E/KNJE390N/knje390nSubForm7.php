<?php

require_once('for_php7.php');

class knje390nSubForm7
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform7", "POST", "knje390nindex.php", "", "subform7");

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
            $getMaxDate = $db->getOne(knje390nQuery::getMaxRecordData7Query($model));
            $model->record_date = $getMaxDate;
            $newflg = "";
        }
        knjCreateHidden($objForm, "NEW_FLG", $newflg);

        //年度表示
        $arg["data"]["NENDO"] = $model->main_year.'年度';

        //生徒情報
        $info = $db->getRow(knje390nQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //種別
        $extra = "onchange=\"return btn_submit('subform7A')\"";
        $query = knje390nQuery::getNameMst("E043");
        makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->field7["DATA_DIV"], $extra, 1, "");

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform7" || $model->cmd == "subform7A" || $model->cmd == "subform7_clear"){
            if (isset($model->schregno) && !isset($model->warning) && $model->record_date != ""){
                $Row = $db->getRow(knje390nQuery::getSubQuery7($model), DB_FETCHMODE_ASSOC);
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
            $extra = " STYLE=\"background:darkgray\" readOnly ";
            $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
            $arg["data"]["SPACE"] = ' ';
            $arg["data"]["WRITING_DATE"] = knjCreateTextBox($objForm, $Row["WRITING_DATE"], "WRITING_DATE", 12, 12, $extra);
        } else {
            $extra = "";
            $Row["WRITING_DATE"] = str_replace("-", "/", $Row["WRITING_DATE"]);
            $arg["data"]["WRITING_DATE"] = View::popUpCalendar($objForm, "WRITING_DATE", $Row["WRITING_DATE"]);
        }

        $extra = "style=\"height:125px; overflow:auto;\"";
        //概要
        $arg["data"]["OUTLINE"] = knjCreateTextArea($objForm, "OUTLINE", 8, 23, "soft", $extra, $Row["OUTLINE"]);
        //方策
        $arg["data"]["MEASURE"] = knjCreateTextArea($objForm, "MEASURE", 8, 23, "soft", $extra, $Row["MEASURE"]);
        //今後の展開
        $arg["data"]["FUTURE_DEVELOPMENT"] = knjCreateTextArea($objForm, "FUTURE_DEVELOPMENT", 8, 23, "soft", $extra, $Row["FUTURE_DEVELOPMENT"]);
        //備考
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", 8, 23, "soft", $extra, $Row["REMARK"]);
        $arg["data"]["TEXT_SIZE"] = '<font size="1" color="red">(全角11文字8行まで)</font>';

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
        View::toHTML($model, "knje390nSubForm7.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData, $Row)
{
    //新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform7_formatnew');\"";
    $arg["button"]["btn_formatnew"] = knjCreateBtn($objForm, "btn_formatnew", "新規作成", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform7_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform7_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
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
