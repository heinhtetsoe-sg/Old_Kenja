<?php
class knjl580iForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->examYear;

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

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl580iQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //手続種別ラジオボタン 1:入学申込金 2:施設設備費 3:入学確約書
        $optProcType = array();
        $model->field["PROC_TYPE"] = ($model->field["PROC_TYPE"] == "") ? "1" : $model->field["PROC_TYPE"];
        foreach ($model->procTypeList as $key => $val) {
            $optProcType[] = $key;
            $extraArray[] = "id=\"PROC_TYPE{$key}\" onclick=\"return btn_submit('main');\"";
        }
        $radioArray = knjCreateRadio($objForm, "PROC_TYPE", $model->field["PROC_TYPE"], $extraArray, $optProcType, count($optProcType));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //手続年月日
        $proDate = ($model->field["PROCEDUREDATE"] != "") ? $model->field["PROCEDUREDATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["TOP"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", $proDate, "", "", "");

        /******************/
        /**リストToリスト**/
        /******************/
        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**************/
        /* ボタン作成 */
        /**************/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**************/
        /* ＣＳＶ作成 */
        /**************/
        //ファイル
        $extra = "";
        $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);
        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["csv"]["btn_exec"] = knjCreateBtn($objForm, "btn_output", "実 行", $extra);
        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = " checked";
        } else {
            $check_header = ($model->cmd == "main" || $model->cmd == "csvInputMain") ? " checked" : "";
        }
        $extra = "id=\"HEADER\"" . $check_header;
        $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);
        //CSV取込書出種別ラジオボタン 1:取込 2:ヘッダー 3:エラー
        $opt_shubetsu = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["csv"][$key] = $val;
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl580iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl580iForm1.html", $arg);
    }
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $rightList = $leftList = array();
    $leftCnt = $rightCnt = 0;
    $query = knjl580iQuery::getBaseDatData($model);

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //手続種別
        $procType = $model->field["PROC_TYPE"];

        //指定の手続種別が不要な受験者は除く
        $necessaryProcTypes = $model->getNecessaryProcTypes($row);
        if (!in_array($procType, $necessaryProcTypes)) {
            continue;
        }

        //手続終了者一覧(左側)
        if ($row["PROC_TYPE{$procType}_FLG"] == "1") {
            $leftList[] = array('label' => $row["LABEL"] . "：" . str_replace("-", "/", $row["PROC_TYPE{$procType}_DATE"]),
                                'value' => $row["VALUE"]);
            $leftCnt++;

        //合格者一覧(右側)
        } else {
            $rightList[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
            $rightCnt++;
        }
    }
    $result->free();

    $arg["data"]["leftCount"]  = $leftCnt;
    $arg["data"]["rgihtCount"] = $rightCnt;

    //合格者一覧(右側)（受験番号順）
    $extra = "multiple style=\"width:500px\" width=\"500px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 33);

    //手続終了者一覧(右側)
    $extra = "multiple style=\"width:500px\" width=\"500px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 33);

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

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
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
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
