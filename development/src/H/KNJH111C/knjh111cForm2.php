<?php

require_once('for_php7.php');

class knjh111cForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh111cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg")) {
        } else {
            $Row =& $model->field;
        }

        //対象クラス
        $arg["GRADE_HR_CLASS"] = $db->getOne(knjh111cQuery::getHrName($model));
 
        if ($model->chFlg == "1") {
            $model->field["RIGHT_TEST_DATE"] = ($model->topTestDate != "") ? $model->topTestDate: $model->field["RIGHT_TEST_DATE"];
            $model->field["RIGHT_TEST_CD"]   = ($model->topTestCd != "")   ? $model->topTestCd  : $model->field["RIGHT_TEST_CD"];
        }

        //試験日
        $model->field["RIGHT_TEST_DATE"] = str_replace('-', '/', ($model->field["RIGHT_TEST_DATE"] != "") ? $model->field["RIGHT_TEST_DATE"]: CTRL_DATE);
        $extra = " btn_submit('edit');\" onkeydown=\"goEnter(this);\"";
        $arg["data"]["RIGHT_TEST_DATE"] = makepopUpCalendar($objForm, "RIGHT_TEST_DATE", $model->field["RIGHT_TEST_DATE"], "reload=true", $extra, "");
        knjCreateHidden($objForm, "SEME_SDATE", $model->sDate);
        knjCreateHidden($objForm, "SEME_EDATE", $model->eDate);
        $model->calenderFlg = 1;

        //受験級
        $query = knjh111cQuery::getTestCd($model);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "RIGHT_TEST_CD", $model->field["RIGHT_TEST_CD"], $extra, 1, "");

/**** リストtoリスト ****/
        //生徒対象者一覧リストを作成する
        $leftList = array();
        $schregNo = $attNo = "";
        $query = knjh111cQuery::getSchregLeftList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $leftList[]  = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
            list($schregNo, $attNo) = explode(":", $row["VALUE"]);
            $left_schregno[] = $schregNo;
        }
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 20);

        //対象生徒リストを作成する
        $rightList = array();
        $query = knjh111cQuery::getSchregRightList($model, $left_schregno);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $rightList[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

        //対象選択ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
        //対象取消ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        //対象選択ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取消ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
/**** fin ****/

        /********/
        /*ボタン*/
        /********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "UpEdit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjh111cindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh111cForm2.html", $arg); 
    }
}

/********************************************* 以下関数 *******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($name == "BUNRIDIV") {
        $opt[] = array("label" => "", "value" => "0");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//カレンダーコントロール
function makepopUpCalendar(&$objForm, $name, $value="",$param="",$extra="",$disabled="") {
    global $sess;
    //テキストエリア
    $extra = " onblur=\"isDate(this);$extra\"".$disabled;
    $setDateText = knjCreateTextBox($objForm, $value, $name, 12, 12, $extra);

    //読込ボタンを作成する
    $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=$name&frame='+getFrameName(self) + '&date=' + document.forms[0]['$name'].value + '&CAL_SESSID=$sess->id&$param' + '&CSSNO=$cssNo', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
    $setCalBtn = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);

    return View::setIframeJs() .$setDateText .$setCalBtn;
}
?>
