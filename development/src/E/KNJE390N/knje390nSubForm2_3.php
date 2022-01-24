<?php

require_once('for_php7.php');
class knje390nSubForm2_3
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2_2", "POST", "knje390nindex.php", "", "subform2_2");

        //DB接続
        $db = Query::dbCheckOut();

        //カレンダー呼び出し
        $my = new mycalendar();

        //生徒情報
        $info = $db->getRow(knje390nQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];


        //項目名取得
        $label = array();
        $query = knje390nQuery::getChallengedAssessmentStatusGrowupDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["DATA_DIV"] == "0") {
                $arg["SHEET_PATTERN"] = $row["SHEET_PATTERN"];
                $arg["STATUS_NAME"] = $row["STATUS_NAME"];
                $arg["GROWUP_NAME"] = $row["GROWUP_NAME"];
                $arg["isGroupColumn"] = $row["SHEET_PATTERN"] == "2" ? "1" : "";
                break;
            }
        }
        $result->free();

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform2_actual") {
            unset($model->getYear);
            unset($model->getSubclass);
        }
        $model->getYear = $model->getYear ? $model->getYear : $model->main_year;

        $model->field2['SUBCLASS'] = $model->getSubclass ? $model->getSubclass : $model->field2['SUBCLASS'];
        // 科目コンボ
        $extra = " onchange=\"btn_submit('subform2_actual')\" ";
        $query = knje390nQuery::getSubQuery2GradeKindSchregGroupDat($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASS", $model->field2['SUBCLASS'], $extra, 1, "");

        //教科等の実態情報取得
        if (isset($model->schregno) && !isset($model->warning)){
            $query = knje390nQuery::getSubQuery2StatusSubclassData($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            // $Row['SUBCLASS'] = $Row['CLASSCD'].'-'.$Row['SCHOOL_KIND'].'-'.$Row['CURRICULUM_CD'].'-'.$Row['SUBCLASSCD'];
            if ($Row) {
                $Row['STATUS'] = $Row['STATUS'];
                $Row['FUTURE_CARE'] = $Row['FUTURE_CARE'];
                $model->field2['SELECT_SUBCLASS'] = "1";
            } else {
                $Row['STATUS'] = "";
                $Row['FUTURE_CARE'] = "";
                $model->field2['SELECT_SUBCLASS'] = "0";
            }
        } else {
            $Row =& $model->field2;
        }
        knjCreateHidden($objForm, "SELECT_SUBCLASS", $model->field2['SELECT_SUBCLASS']);

        //実態
        $moji = 25;
        $gyou = 30;
        if (!$arg["isGroupColumn"]) {
            // 1枠表示時の 文字数と行数
            $moji = 40;
            $gyou = 30;
        }
        $arg["data"]["SUBCLASS_STATUS"]       = getTextOrArea($objForm, "SUBCLASS_STATUS", $moji, $gyou, $Row["STATUS"], $model);
        $arg["data"]["SUBCLASS_STATUS_COMMENT"] = getTextAreaComment($moji, $gyou);
        setInputChkHidden($objForm, 'SUBCLASS_STATUS', $moji, $gyou, $arg);
        //支援
        $arg["data"]["SUBCLASS_FUTURE_CARE"] = getTextOrArea($objForm, "SUBCLASS_FUTURE_CARE", 15, 30, $Row["FUTURE_CARE"], $model);
        $arg["data"]["SUBCLASS_FUTURE_CARE_COMMENT"] = getTextAreaComment(15, 30);
        setInputChkHidden($objForm, 'SUBCLASS_FUTURE_CARE', 15, 30, $arg);

        //ボタン作成
        makeBtn($objForm, $arg, $model);
        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390nSubForm2_3.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knje390nQuery::getSubQuery2StatusSubclassList($model);

    $result = $db->query($query);
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowlist['SUBCLASS'] = $rowlist["CLASSCD"].'-'.$rowlist["SCHOOL_KIND"].'-'.$rowlist["CURRICULUM_CD"].'-'.$rowlist["SUBCLASSCD"];
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="") {
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
function makeBtn(&$objForm, &$arg, $model) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('actual2_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('actual2_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('actual2_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform2A');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row) {
    knjCreateHidden($objForm, "cmd");
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

