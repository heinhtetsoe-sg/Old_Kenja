<?php

require_once('for_php7.php');

class knjz067aForm2
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz067aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != "edit2" && $model->cmd != "edit3" && $model->ibyear && $model->ibgrade && $model->ibclasscd && $model->ibprg_course && $model->ibcurriculum_cd && $model->ibsubclasscd) {
            $query = knjz067aQuery::getIBSubclassReplaceDat($model->ibyear, $model->ibgrade, $model->ibclasscd, $model->ibprg_course, $model->ibcurriculum_cd, $model->ibsubclasscd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //IBコースコンボ
        $query = knjz067aQuery::getIBPrgCourse($model, "list");
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "IBPRG_COURSE", $Row["IBPRG_COURSE"], $extra, 1);

        if ($model->cmd == "edit2") {
            $Row["IBCLASSCD"]       = "";
            $Row["IBCURRICULUM_CD"] = "";
            $Row["IBSUBCLASSCD"]    = "";
        }

        //IB科目コンボ
        $query = knjz067aQuery::getIBSubclasscd($model, $Row["IBPRG_COURSE"], "list");
        $value = $Row["IBCLASSCD"].'-'.$Row["IBPRG_COURSE"].'-'.$Row["IBCURRICULUM_CD"].'-'.$Row["IBSUBCLASSCD"];
        $extra = "onchange=\"return btn_submit('edit3');\"";
        makeCmb($objForm, $arg, $db, $query, "IBSUBCLASS", $value, $extra, 1, "BLANK");

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $Row);

        //ボタン作成
        makeBtn($objForm, $arg);
        
        //$arg["show_csv"] = "ON";
        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model, $db);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "edit2" && $model->cmd != "edit3") {
            $arg["reload"] = "window.open('knjz067aindex.php?cmd=list&shori=add','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz067aForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK = "")
{
    $opt = array();
    $value_flg = false;
    if ($BLANK) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $Row)
{
    $opt_right = $opt_left = array();
    $selected = array();

    //対象科目取得
    $query = knjz067aQuery::getSubclassMst($model, $Row, "select");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        $selected[] = $row["VALUE"];
    }
    $result->free();

    //対象科目作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 15);

    //科目一覧取得
    $query = knjz067aQuery::getSubclassMst($model, $Row, "list");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selected)) {
            continue;
        }
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();

    //科目一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 15);

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
function makeBtn(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
    //ＣＳＶボタン
    $extra = "onclick=\"return btn_submit('downloadCsv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model, $db)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["data"]["CSV_XLS_NAME"] = "ＣＳＶ出力<BR>／ＣＳＶ取込";

    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
