<?php

require_once('for_php7.php');

class knjl676iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("main", "POST", "knjl676iindex.php", "", "main");

        $db = Query::dbCheckOut();

        //入試年度
        $arg["data"]["YEAR"] = $model->entexamyear . "年度";

        //塾検索ボタン
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=&pricd=PRISCHOOLCD_ID&priname=label_priName&priclasscd=PRISCHOOL_CLASS_CD_ID&priclassname=label_priClassName&priaddr=&prischool_div=&submitFlg=1', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 280)\"";
        $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "塾検索", $extra);

        //塾コード
        $extra  = "id=\"PRISCHOOLCD_ID\" onblur=\"checkPrischoolcd(this);\" style=\"ime-mode: inactive;\"";
        $arg["data"]["PRISCHOOLCD"] = knjCreateTextBox($objForm, $model->field["PRISCHOOLCD"], "PRISCHOOLCD", 7, 7, $extra);
        //塾名
        $priSchRow = $db->getRow(knjl676iQuery::getPriSchoolRow($model->field["PRISCHOOLCD"]), DB_FETCHMODE_ASSOC);
        $arg["data"]["PRISCHOOL_NAME"] = $priSchRow["PRISCHOOL_NAME"];

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
        $query = knjl676iQuery::getFinschoolName($model->field["FINSCHOOLCD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"];

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjl676iForm1.html", $arg);
    }
}

function makeListToList(&$objForm, &$arg, $db, $model)
{
    //初期化
    $opt_right = $opt_left = array();
    $leftCnt = $rightCnt = 0;
    $opt = array();
    $opt2 = array();

    //タイトル
    $arg["data"]["TITLE_LEFT"]  = "塾生一覧";
    $arg["data"]["TITLE_RIGHT"] = "中学生一覧";

    //塾生取得（既に塾コード登録済み）
    $query = knjl676iQuery::getSelectQueryLeft($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = $row["EXAMNO"];
    }
    $result->free();

    //選択中の塾コード以外に塾コード登録済みの生徒も中学生一覧の対象外
    $query = knjl676iQuery::getSelectQueryLeft2($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt2[] = $row["EXAMNO"];
    }
    $result->free();

    //リストを作成する
    $result = $db->query(knjl676iQuery::getSelectQuery($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!isset($model->warning)) {
            if (!in_array($row["VALUE"], $opt2)) {
                $opt_right[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
                $rightCnt++;
            } elseif (in_array($row["VALUE"], $opt)) {
                $opt_left[] = array("label" => $row["LABEL"],
                            "value" => $row["VALUE"]);
                $leftCnt++;
            }
        } else {
            $arySelectLeft = explode(",", $model->selectLeft);
            $arySelectRight = explode(",", $model->selectRight);
            if (in_array($row["VALUE"], $arySelectRight)) {
                $opt_right[] = array("label" => $row["LABEL"],
                                     "value" => $row["VALUE"]);
                $rightCnt++;
            } elseif (in_array($row["VALUE"], $arySelectLeft)) {
                $opt_left[] = array("label" => $row["LABEL"],
                                    "value" => $row["VALUE"]);
                $leftCnt++;
            }
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
    $disabled = ($model->field["PRISCHOOLCD"]) ? "" : " disabled ";

    //更新ボタン
    $extra = $disabled . "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectLeft");
    knjCreateHidden($objForm, "selectLeftText");
    knjCreateHidden($objForm, "selectRight");
    knjCreateHidden($objForm, "selectRightText");
}
