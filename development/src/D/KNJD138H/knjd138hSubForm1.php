<?php

require_once('for_php7.php');

class knjd138hSubForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knjd138hindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $setData  = $db->getRow(knjd138hQuery::getTrainRow($model, $model->schregno), DB_FETCHMODE_ASSOC);
            $setData2 = $db->getRow(knjd138hQuery::getTrain2Row($model, $model->schregno), DB_FETCHMODE_ASSOC);
            $setData3 = $db->getRow(knjd138hQuery::getTrain3Row($model, $model->schregno), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $setData  =& $model->field;
            $setData2 =& $model->field;
            $setData3 =& $model->field;
        }

        //全てチェックボックス
        $extra = "onclick=\"checkAll()\"";
        $arg["ALL"] = knjCreateCheckBox($objForm, "ALL", "on", $extra);

        //選択のチェックボックス
        $extra = "";
        $arg["RCHECK1"] = knjCreateCheckBox($objForm, "RCHECK1", "on", $extra);
        $arg["RCHECK2"] = knjCreateCheckBox($objForm, "RCHECK2", "on", $extra);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //学習活動
        $extra = "style=\"height:118px;\"";
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $setData["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYTIME"]["moji"]."文字X".$model->getPro["TOTALSTUDYTIME"]["gyou"]."行まで)";

        //評価
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $setData2["REMARK1"], $model);
        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->getPro["REMARK1"]["moji"]."文字X".$model->getPro["REMARK1"]["gyou"]."行まで)";

        //更新ボタン
        $extra = "onclick=\"return doSubmit()\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻るボタン
        $link = REQUESTROOT."/D/KNJD138H/knjd138hindex.php?cmd=back";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd138hSubForm1.html", $arg);
    }
}
/********************************************** 以下関数 ***********************************************/
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //生徒一覧
    $opt_left = $opt_right = array();
    //置換処理選択時の生徒の情報
    $array = explode(",", $model->selectdata);
    if ($array[0] == "") $array[0] = $model->schregno;

    $query = knjd138hQuery::getStudent($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["SCHREGNO"],$array)) {
            $opt_left[]   = array("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        } else {
            $opt_right[]  = array("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

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
        $extra = "style=\"height:".$height."px;\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
