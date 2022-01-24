<?php

require_once('for_php7.php');

class knje701Form2
{
    public function main(&$model)
    {
        if ($model->cmd == "reset") {
            $model->field["HEIGANCD"]    = "";
            $model->field["HEIGANGNAME"] = "";
            $model->field["FACULITYCD"]  = null;
        }

        $objForm = new form();

        $arg["start"] = $objForm->get_start("edit", "POST", "knje701index.php", "", "edit");

        $chaircd = $option = array();

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)  &&
           ($model->cmd         != "change") &&
           ($model->heigancd    != "")       &&
           ($model->heigangname != "")) {
            $result = $db->query(knje701Query::getHdat($model));
            while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rowH = $rowTemp;
            }
            $result = $db->query(knje701Query::getDat($model));
            while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rowD = $rowTemp;
            }
        } else {
            $rowH =& $model->field;
            $rowD =& $model->field;
        }

        //学部コンボ
        $result = $db->query(knje701Query::getFaculty());
        $opt    = array();
        $opt[]  = array("label" => "", "value" => "");
        while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $rowTemp["LABEL"], "value" => $rowTemp["VALUE"]);
        }
        $result->free();
        $extra = "onchange=\"return btn_submit('header_chenge');\"";

        if ($rowD["FACULITYCD"] == "") {
            $rowD["FACULITYCD"] = $model->field["FACULITYCD"];
        }
        if ($model->field["FACULITYCD"] === "") {
            $rowD["FACULITYCD"] = $model->field["FACULITYCD"];
        }
        $arg["data"]["FACULITYCD"] = knjCreateCombo($objForm, "FACULITYCD", $rowD["FACULITYCD"], $opt, $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //併願コード
        $extra = "onblur=\"numCheck(this);\"";
        if ($rowH["HEIGAN_CD"] == "") {
            $rowH["HEIGAN_CD"] = $model->field["HEIGANCD"];
        }
        $arg["data"]["HEIGANCD"] = knjCreateTextBox($objForm, $rowH["HEIGAN_CD"], "HEIGANCD", 2, 2, $extra);

        //併願グループ名称
        $extra = "";
        if ($rowH["HEIGAN_GROUPNAME"] == "") {
            $rowH["HEIGAN_GROUPNAME"] = $model->field["HEIGANGNAME"];
        }
        $arg["data"]["HEIGANGNAME"] = knjCreateTextBox($objForm, $rowH["HEIGAN_GROUPNAME"], "HEIGANGNAME", 9, 9, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "selectleftval");

        if (VARS::post("cmd") == "add" ||
            VARS::post("cmd") == "update" ||
            VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knje701index.php?cmd=list','left_frame');";
        }

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knje701Form2.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //初期化
    $opt_left = $opt_right = array();

    //学部に対応する学科一覧取得(右側)
    $query = knje701Query::getDepartment($model, "right");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array('label'=> $row["LABEL"],'value' => $row["VALUE"]);
    }
    $result->free();

    //学部に対応する学科一覧取得(左側)
    if ($model->field["FACULITYCD"] === null) {
        //リンククリック時
        $query = knje701Query::getDepartment($model, "left");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label'=> $row["LABEL"],'value' => $row["VALUE"]);
        }
        $result->free();
    }

    //追加・更新時エラーチェックに引っかかった場合
    if ($model->checkFlg == false) {
        //初期化
        $opt_left = $opt_right = array();
        //学部に対応する学科一覧取得(右側)
        $query = knje701Query::getDepartment($model, "right");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label'=> $row["LABEL"],'value' => $row["VALUE"]);
        }
        $result->free();

        //学部に対応する学科一覧取得(左側)
        $query = knje701Query::getDepartment($model, "left");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label'=> $row["LABEL"],'value' => $row["VALUE"]);
        }
        $result->free();
        $model->checkFlg = true;
    }

    //一覧リスト（右）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left', '1')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', '1')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '1');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '1');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '1');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '1');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
