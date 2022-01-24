<?php
class knjl730hForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjl730hindex.php", "", "main");

        $db = Query::dbCheckOut();

        //入試年度
        $arg["data"]["YEAR"] = $model->ObjYear . "年度";

        //入試制度
        $extra = "onChange=\"return btn_submit('change');\"";
        $query = knjl730hQuery::getNameMst($model, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分
        $extra = "onChange=\"return btn_submit('changeTestDiv');\"";
        $query = knjl730hQuery::getTestDiv($model);
        if ($model->cmd == "change") {
            $model->field["TESTDIV"] = "";
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //試験内容 (1:筆記 2:面接 3:作文)
        $opt = array(1, 2, 3);
        $model->field["EXAM_TYPE"] = ($model->field["EXAM_TYPE"] == "") ? "1" : $model->field["EXAM_TYPE"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"EXAM_TYPE{$val}\" onClick=\"btn_submit('changeExamType')\" ");
        }
        $radioArray = knjCreateRadio($objForm, "EXAM_TYPE", $model->field["EXAM_TYPE"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjl730hForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($name == "APPLICANTDIV") {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
    }
    $value = ($value != "") ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    $result->free();
}

function makeListToList(&$objForm, &$arg, $db, $model)
{
    //初期化
    $opt_right = $opt_left = array();
    $selectLeft = $selectLeftText = array();

    //会場コンボ
    $query = knjl730hQuery::getExamHall($model);
    $extra = "onChange=\"return btn_submit('main');\"";
    if ($model->cmd != "main" && $model->cmd != "edit") {
        $model->field["EXAMHALLCD"] = "";
    }
    makeCmb($objForm, $arg, $db, $query, $model->field["EXAMHALLCD"], "EXAMHALLCD", $extra, 1);

    //対象外の志願者取得（会場割振りされた志願者は対象外）
    $delFlg = true;
    $opt = array();
    $query = knjl730hQuery::getSelectQueryLeft($model, $seme);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = $row["RECEPTNO"];
        $delFlg = false;
    }
    $result->free();
    $opt2 = array();//別会場含む対象外
    $query = knjl730hQuery::getSelectQueryLeft2($model, $seme);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt2[] = $row["RECEPTNO"];
    }
    $result->free();

    //削除チェックボックス
    $name = array("DEL_HALL");
    foreach ($name as $key => $val) {
        if ($model->cmd == "edit") {
            $extra  =  "";
        } else {
            $extra  = ($delFlg && $model->field[$val] == "1") ? "checked" : "";
        }
        $extra .= " id=\"" . $val . "\"";
        if (!$delFlg) {
            $extra .= " disabled ";
        }
        $arg["data"][$val] = knjCreateCheckBox($objForm, $val, "1", $extra, "");
    }

    //タイトル
    // $arg["data"]["TITLE_LEFT"]  = "";
    $arg["data"]["TITLE_RIGHT"] = "志願者一覧（受験番号順）";

    //対象者リストを作成する
    $result = $db->query(knjl730hQuery::getSelectQuery($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        // if (!in_array($row["RECOMMENDATION_CD"], $selectLeft)) {
        if (!in_array($row["VALUE"], $opt2)) {
            $opt_right[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        } else if (in_array($row["VALUE"], $opt)) {
            $opt_left[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
        }
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
function makeBtn(&$objForm, &$arg, $model)
{
    $disabled = ($model->field["APPLICANTDIV"] && $model->field["TESTDIV"]) ? "" : " disabled ";
    //会場追加ボタン作成
    $extra = $disabled . " onclick=\"return btn_submit('halladd')\"";
    $arg["button"]["btn_halladd"] = knjCreateBtn($objForm, "btn_halladd", "会場追加", $extra);

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
    knjCreateHidden($objForm, "CHANGE_FLG");
}
?>

