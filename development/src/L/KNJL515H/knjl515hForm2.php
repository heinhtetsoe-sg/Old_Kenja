<?php

require_once('for_php7.php');

class knjl515hForm2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl515hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $model->year = ($model->year == "") ? substr(CTRL_DATE, 0, 4): $model->year;

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "sendLink" || $model->cmd == "reset")
             && $model->year && $model->applicantdiv && $model->distinctId) {
            $query = knjl515hQuery::getRow($model->year, $model->applicantdiv, $model->distinctId);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["TEST_DATE"] = str_replace("-", "/", $Row["TEST_DATE"]);
        } else {
            $Row =& $model->field;
        }

        //タイトル
        $arg["TITLE"] = "<b>".$model->year."年度　入試判別</b>";

        //初期値セット
        if ($model->applicantdiv == "") {
            $extra = "onchange=\"return btn_submit('list');\"";
            $query = knjl515hQuery::getNameMst($model, "L003");
            makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");
        }
        $arg["APPLICANTDIV"] = $db->getOne(knjl515hQuery::getNameMst($model, "L003", $model->applicantdiv));

        /**************/
        /**コンボ作成**/
        /**************/
        //入試種別
        $query = knjl515hQuery::getEntexamTestdivMst($model);
        $extra = "onchange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $Row["TESTDIV"], $extra, 1, "BLANK");

        //入試方式
        $query = knjl515hQuery::getEntexamExamtypeMst($model);
        $extra = "onchange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $Row["EXAM_TYPE"], $extra, 1, "BLANK");

        //試験日
        $query = knjl515hQuery::getTestDate($model);
        $extra = "onchange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, "TEST_DATE", $Row["TEST_DATE"], $extra, 1, "BLANK");

        /****************/
        /**テキスト作成**/
        /****************/
        //入試種別CDテキストボックス
        $extra = "STYLE=\"ime-mode: inactive; text-align: right;\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["DISTINCT_ID"] = knjCreateTextBox($objForm, $Row["DISTINCT_ID"], "DISTINCT_ID", 3, 3, $extra);

        //入試種別名称テキストボックス
        $extra = "";
        $arg["DISTINCT_NAME"] = knjCreateTextBox($objForm, $Row["DISTINCT_NAME"], "DISTINCT_NAME", 40, 60, $extra);

        /**************/
        /**ボタン作成**/
        /**************/
        //追加ボタン
        $addDis = ($model->distinctId == "") ? "": " disabled";
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra.$addDis);

        //更新ボタン
        $upDis = ($model->distinctId == "") ? " disabled": "";
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra.$upDis);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        /******************/
        /**リストToリスト**/
        /******************/
        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**************/
        /**hidden作成**/
        /**************/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectRightdata");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "reset") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl515hindex.php?cmd=list2"
                            . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."';";

        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl515hForm2.html", $arg);
    }
}
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //生徒一覧
    $rightList = $leftList = $leftArr = array();
    $query = knjl515hQuery::getMiraiMeikeiDat($model);
    $result = $db->query($query);
    while ($rowL = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($rowL["DISTINCT_ID"] != "" && $rowL["DISTINCT_ID"] == $model->distinctId) {
            $leftList[] = array('label' => $rowL["LABEL"],
                                'value' => $rowL["VALUE"]);
            $leftArr[] = $row["VALUE"];
        }
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($row["DISTINCT_ID"] == "" && !in_array($row["VALUE"], $leftArr)) {
            $rightList[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //試験名称一覧
    $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //同一処理試験作成
    $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 20);

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
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
