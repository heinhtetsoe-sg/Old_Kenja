<?php

require_once('for_php7.php');

class knjm502SubForm1 {
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();
        $arg = array();
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knjm502index.php", "", "subform1");
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //全てチェックボックス
        $extra = "onclick=\"checkAll()\"";
        $arg["ALL"] = knjCreateCheckBox($objForm, "ALL", "on", $extra);

        //選択のチェックボックス
        $extra = "";
        $arg["RCHECK1"] = knjCreateCheckBox($objForm, "RCHECK1", "on", $extra);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //学期コンボ
        $opt = array();
        $value_flg = false;
        $query = knjm502Query::getSemester();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "onchange=\"return btn_submit('replace');\"";
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //通信欄
        if ($model->cmd !== 'replace') {
            $communication = $model->field["COMMUNICATION"];
        } else {
            $query = knjm502Query::getCommunication($model->field, $model->schregno);
            $communication = $db->getOne($query);
        }
        $arg["data"]["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", 10, 21, "soft", $extra_commu, $communication);

        /**********/
        /* ボタン */
        /**********/
        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit()\"" ) );
        $arg["btn_update"] = $objForm->ge("btn_update");

        //戻るボタン
        $link = REQUESTROOT."/M/KNJM502/knjm502index.php?cmd=back";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"window.open('$link','_self');\"" ) );
        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm502SubForm1.html", $arg);
    }
}
/********************************************** 以下関数 ***********************************************/
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //生徒一覧
    $opt_left = $opt_right = array();
    //置換処理選択時の生徒の情報
    $array = explode(",", $model->selectdata);
    if ($array[0]=="") $array[0] = $model->schregno;

    $query = knjm502Query::getStudent($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
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
?>
