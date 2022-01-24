<?php
class knjl540iForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->ObjYear ."年度";

        //学科コンボ
        $listExamType = array();
        foreach ($model->examTypeList as $key => $val) {
            $listExamType[] = array(
                "LABEL" => $key.":".$val,
                "VALUE" => $key
            );
        }
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmbList($objForm, $arg, $listExamType, $model->exam_type, "EXAM_TYPE", $extra, 1, "TOP", "");

        //入試区分コンボ
        $extra = "onChange=\"return btn_submit('main');\"";
        $query = knjl540iQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //会場コンボ
        $query = knjl540iQuery::getExamHall($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        if ($model->cmd != "main" && $model->cmd != "edit") {
            $model->examhallcd = "";
        }
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1);

        $dataflg = false;

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model, $dataflg);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //CSVフォーム部品作成
        makeCsvForm($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm);

        Query::dbCheckIn($db);

        $arg["start"] = $objForm->get_start("main", "POST", "knjl540iindex.php", "", "main");

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjl540iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $all = "", $retDiv = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($all) {
        $opt[] = array("label" => "全て", "value" => "ALL");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $opt;
    }
}

//makeCmbList
function makeCmbList(&$objForm, &$arg, $orgOpt, &$value, $name, $extra, $size, $argName = "", $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    foreach ($orgOpt as $row) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') {
            $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeListToList(&$objForm, &$arg, $db, $model, &$dataflg)
{
    //初期化
    $opt_right = $opt_left = array();

    //タイトル
    $arg["data"]["TITLE_LEFT"]  = "欠席者一覧";
    $arg["data"]["TITLE_RIGHT"] = "受験希望者一覧（受験番号順）";

    //対象者リストを作成する
    $result = $db->query(knjl540iQuery::getSelectQuery($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //欠席者一覧(左側)
        if ($row["JUDGEMENT"] == "4") {
            $opt_left[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
        //受験希望者一覧(右側)（受験番号順）
        } else {
            $opt_right[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
        $dataflg = true;
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:500px\" width:\"500px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 40);

    //一覧リスト（左）
    $extra = "multiple style=\"width:500px\" width:\"500px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 40);

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
function makeBtn(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //更新ボタン
    $extra ="onclick=\"return btn_submit('update');\"".$disable;
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
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

//CSVフォーム部品作成
function makeCsvForm(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //ファイル
    $extra = "".$disable;
    $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

    //実行
    $extra = "onclick=\"return btn_submit('exec');\"".$disable;
    $arg["csv"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //ヘッダ有チェックボックス
    if ($model->field["HEADER"] == "on") {
        $check_header = " checked";
    } else {
        $check_header = ($model->cmd == "main") ? " checked" : "";
    }
    $extra = "id=\"HEADER\"".$check_header;
    $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

    //CSV取込書出種別ラジオボタン 1:取込 2:書出 3:ヘッダー
    $opt_shubetsu = array(1, 2, 3);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
    $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["csv"][$key] = $val;
    }
}
