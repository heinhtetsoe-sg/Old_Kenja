<?php

require_once('for_php7.php');

class knje390nSubForm4_2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4_2", "POST", "knje390nindex.php", "", "subform4_2");

        //DB接続
        $db = Query::dbCheckOut();

        //表示日付をセット
        if ($model->record_date === 'NEW') {
            $setHyoujiDate = '';
        } else {
            $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
        }

        //生徒情報
        $info = $db->getRow(knje390nQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
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
        if ($model->cmd == "subform4_check") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        //発達検査情報取得
        if ($model->cmd == "subform4_check_set"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390nQuery::getSubQuery4CheckGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field4;
            }
        } else {
            $Row =& $model->field4;
        }
        
        //検査日
        $Row["CHECK_DATE"] = str_replace("-", "/", $Row["CHECK_DATE"]);
        $arg["data"]["CHECK_DATE"] = View::popUpCalendar($objForm, "CHECK_DATE", $Row["CHECK_DATE"]);
        
        //検査結果・所見等
        $extra = "style=\"height:180px; overflow:auto;\"";
        $arg["data"]["CHECK_REMARK"] = knjCreateTextArea($objForm, "CHECK_REMARK", 10, 57, "soft", $extra, $Row["CHECK_REMARK"]);
        $arg["data"]["CHECK_REMARK_SIZE"] = '<font size="1" color="red">(全角28文字10行まで)</font>';

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390nSubForm4_2.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knje390nQuery::getSubQuery4CheckRecordList($model);
    $result = $db->query($query);
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowlist["CONTENTS_NAME"] = '検査項目'.($retCnt+1);
        $rowlist["CHECK_DATE"] = str_replace("-", "/", $rowlist["CHECK_DATE"]);
        $rowlist["CHECK_CONTENTS"] = substr($rowlist["CHECK_REMARK"], 0,105);
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
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('check4_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('check4_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('check4_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform4A');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

