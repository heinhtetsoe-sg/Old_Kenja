<?php

require_once('for_php7.php');

class knje390SubForm3_2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3_2", "POST", "knje390index.php", "", "subform3_2");

        //DB接続
        $db = Query::dbCheckOut();
        
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

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform3_check") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        //発達検査情報取得
        if ($model->cmd == "subform3_check_set"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390Query::getSubQuery3CheckGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field3;
            }
        } else {
            $Row =& $model->field3;
        }
        
        //検査日
        $Row["CHECK_DATE"] = str_replace("-", "/", $Row["CHECK_DATE"]);
        $arg["data"]["CHECK_DATE"] = View::popUpCalendar($objForm, "CHECK_DATE", $Row["CHECK_DATE"]);
        
        //検査機関
        $query = knje390Query::getCheckCentercdComboName();
        makeCmb($objForm, $arg, $db, $query, "CENTERCD", $Row["CENTERCD"], "", 1, 1);
        
        //検査名
        $extra = "style=\"height:35px; overflow:auto;\"";
        $arg["data"]["CHECK_NAME"] = knjCreateTextArea($objForm, "CHECK_NAME", 2, 21, "soft", $extra, $Row["CHECK_NAME"]);
        $arg["data"]["CHECK_NAME_SIZE"] = '<font size="1" color="red">(全角10文字2行まで)</font>';

        //検査者
        $extra = "";
        $arg["data"]["CHECKER"] = knjCreateTextBox($objForm, $Row["CHECKER"], "CHECKER", 20, 20, $extra);

        //検査結果・所見等
        $extra = "style=\"height:180px; overflow:auto;\"";
        $arg["data"]["CHECK_REMARK"] = knjCreateTextArea($objForm, "CHECK_REMARK", 10, 57, "soft", $extra, $Row["CHECK_REMARK"]);
        $arg["data"]["CHECK_REMARK_SIZE"] = '<font size="1" color="red">(全角28文字10行まで)</font>';

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390SubForm3_2.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knje390Query::getSubQuery3CheckRecordList($model);
    $result = $db->query($query);
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowlist["RECORD_DIV_NAME"] = '検査機関'.($retCnt+1);
        $centerName = $db->getOne(knje390Query::getCheckCentercdName($rowlist["CENTERCD"]));
        $rowlist["CENTER_NAME"] = $centerName;
        $rowlist["CHECK_DATE"] = str_replace("-", "/", $rowlist["CHECK_DATE"]);
        
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //D様式参照ボタンを作成する
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=subform2_check_B_sanshou&MAIN_YEAR=".$model->main_year."&SCHREGNO=".$model->schregno."&RECORD_DATE=".$model->record_date."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_Dsanshou"] = knjCreateBtn($objForm, "btn_Dsanshou", "D様式参照", $extra.$disabled);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('check3_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra.$disabled);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('check3_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('check3_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disabled);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('subform3A');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra.$disabled);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

