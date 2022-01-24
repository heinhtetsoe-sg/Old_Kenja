<?php

require_once('for_php7.php');

class knjl674iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("main", "POST", "knjl674iindex.php", "", "main");

        $db = Query::dbCheckOut();

        //入試年度
        $arg["data"]["YEAR"] = $model->entexamyear . "年度";

        //中学校検索ボタン
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain3&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "中学検索", $extra);
        //読込ボタン
        $extra = "onclick=\"btn_submit('read')\"";
        $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);

        //出身学校コード
        $extra = "id=\"FINSCHOOLCD_ID\" onblur=\"this.value=toInteger(this.value);\" style=\"ime-mode: inactive;\" onkeydown=\"goEnter(this);\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $model->field["FINSCHOOLCD"], "FINSCHOOLCD", 7, 7, $extra);
        //学校名
        $query = knjl674iQuery::getFinschoolName($model->field["FINSCHOOLCD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"];

        //特待コードコンボ
        $extra = "onChange=\"checkHonorCmb(this);\"";
        $query = knjl674iQuery::getHonordivQuery($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["HONORDIV"], "HONORDIV", $extra, 1, "BLANK", $model->cmd);

        //特待理由コンボ
        $extra = "onChange=\"checkHonorCmb(this);\"";
        $query = knjl674iQuery::getHonorReasondivQuery($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["HONOR_REASONDIV"], "HONOR_REASONDIV", $extra, 1, "BLANK");

        if ($model->cmd == "" || $model->cmd == "main") {
            $model->field["UPD_HONORDIV"] = $model->field["HONORDIV"];
            $model->field["UPD_HONOR_REASONDIV"] = $model->field["HONOR_REASONDIV"];
        }

        //特待コードコンボ (更新用)
        $extra = "";
        $query = knjl674iQuery::getHonordivQuery($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["UPD_HONORDIV"], "UPD_HONORDIV", $extra, 1, "BLANK");

        //特待理由コンボ (更新用)
        $extra = "";
        $query = knjl674iQuery::getHonorReasondivQuery($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["UPD_HONOR_REASONDIV"], "UPD_HONOR_REASONDIV", $extra, 1, "BLANK");

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjl674iForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $cmd = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();

    if ($name == "HONORDIV" && ($cmd == "" || $cmd == "edit")) {
        $default = 1;
    }

    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeListToList(&$objForm, &$arg, $db, $model)
{
    //初期化
    $opt_right = $opt_left = array();
    $leftCnt = $rightCnt = 0;

    //タイトル
    $arg["data"]["TITLE_LEFT"]  = "特待生一覧";
    $arg["data"]["TITLE_RIGHT"] = "中学生一覧";

    //特待生取得（特待生登録済み）
    $opt = array();
    $query = knjl674iQuery::getSelectQueryLeft($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = $row["EXAMNO"];
    }
    $result->free();

    $result = $db->query(knjl674iQuery::getSelectQuery($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $opt)) {
            $opt_right[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
            $rightCnt++;
        } else {
            $opt_left[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
            $leftCnt++;
        }
    }
    $result->free();

    $arg["data"]["leftCount"]  = $leftCnt;
    $arg["data"]["rgihtCount"] = $rightCnt;

    //一覧リスト（右）
    $extra = "multiple style=\"width:500px\" width:\"500px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 30);

    //一覧リスト（左）
    $extra = "multiple style=\"width:500px\" width:\"500px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 30);

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
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectLeft");
    knjCreateHidden($objForm, "selectLeftText");
    knjCreateHidden($objForm, "selectRight");
    knjCreateHidden($objForm, "selectRightText");
    knjCreateHidden($objForm, "HID_HONORDIV", $model->field["HONORDIV"]);
    knjCreateHidden($objForm, "HID_HONOR_REASONDIV", $model->field["HONOR_REASONDIV"]);
}
