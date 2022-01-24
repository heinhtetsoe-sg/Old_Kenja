<?php
class knjl017vForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("main", "POST", "knjl017vindex.php", "", "main");

        $db = Query::dbCheckOut();

        //入試年度
        $arg["data"]["YEAR"] = $model->ObjYear . "年度";

        //校種コンボ
        $extra = "onChange=\"return btn_submit('change')\"";
        $query = knjl017vQuery::getSchoolKind($model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, $model->field["EXAM_SCHOOL_KIND"], "EXAM_SCHOOL_KIND", $extra, 1);

        //試験IDコンボ
        $extra = "onChange=\"return btn_submit('changeExamId')\"";
        $query = knjl017vQuery::getExamId($model);
        if ($model->cmd == "change") {
            $model->field["EXAM_ID"] = "";
            $model->field["PLACE_ID"] = "";
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["EXAM_ID"], "EXAM_ID", $extra, 1);

        //会場コンボ
        $extra = "onChange=\"return btn_submit('changePlaceId')\"";
        $query = knjl017vQuery::getPlaceId($model);
        if ($model->cmd == "changeExamId") {
            $model->field["PLACE_ID"] = "";
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["PLACE_ID"], "PLACE_ID", $extra, 1, "BLANK");

        $seatsCnt = 0;
        if ($model->field["PLACE_ID"] != "") {
            $query = knjl017vQuery::getPlaceId($model, $model->field["PLACE_ID"]);
            $hollRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $seatsCnt = $hollRow["SEATS"];
        }
        //会場 収容人数
        $arg["data"]["seatsCnt"] = $seatsCnt;

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjl017vForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
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
    $leftCnt = $rightCnt = 0;

    //タイトル
    $arg["data"]["TITLE_LEFT"]  = "会場別受験者一覧";
    $arg["data"]["TITLE_RIGHT"] = "志願者一覧";

    //対象外の志願者取得（会場割振りされた志願者は対象外）
    $opt = array();
    $query = knjl017vQuery::getSelectQueryLeft($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = $row["EXAMNO"];
    }
    $result->free();

    $opt2 = array();//別会場含む対象外
    $query = knjl017vQuery::getSelectQueryLeft2($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt2[] = $row["EXAMNO"];
    }
    $result->free();

    //対象者リストを作成する
    $result = $db->query(knjl017vQuery::getSelectQuery($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $opt2)) {
            $opt_right[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
            $rightCnt++;
        } elseif (in_array($row["VALUE"], $opt)) {
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
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 35);
        
    //一覧リスト（左）
    $extra = "multiple style=\"width:500px\" width:\"500px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 35);

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
    $disabled = ($model->field["EXAM_SCHOOL_KIND"] && $model->field["EXAM_ID"]) ? "" : " disabled ";

    //会場別受験者CSV出力ボタン
    $extra = $disabled . "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "会場別受験者CSV出力", $extra);

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
