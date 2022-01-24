<?php

require_once('for_php7.php');

class knjl672aForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["top"]["YEAR"] = $model->ObjYear;

        //入試制度(校種)
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl672aQuery::getNameMst($model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["VALUE"].":".$row["LABEL"];
            $opt[] = array('label' => $label, 'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') {
                $model->field["APPLICANTDIV"] = $row["VALUE"];
            }
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('edit');\"";
        $arg["top"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //入試区分コンボ
        $extra = "onchange=\"return btn_submit('edit');\"";
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl672aQuery::getNameMst($model->ObjYear, $namecd1);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //入学課程学科
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl672aQuery::getCourseMajorMst($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            if ($model->field["COURSEMAJOR"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $model->field["COURSEMAJOR"] = ($model->field["COURSEMAJOR"] && $value_flg) ? $model->field["COURSEMAJOR"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('edit');\"";
        $arg["top"]["COURSEMAJOR"] = knjCreateCombo($objForm, "COURSEMAJOR", $model->field["COURSEMAJOR"], $opt, $extra, 1);

        //入学コース
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl672aQuery::getCourceCode($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            if ($model->field["COURSECODE"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $model->field["COURSECODE"] = ($model->field["COURSECODE"] && $value_flg) ? $model->field["COURSECODE"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('edit');\"";
        $arg["top"]["COURSECODE"] = knjCreateCombo($objForm, "COURSECODE", $model->field["COURSECODE"], $opt, $extra, 1);

        //データ取得
        $receptList = array();
        if ($model->isWarning()) {
            for ($i=0; $i < get_count($model->line["EXAMNO"]); $i++) {
                $receptList[$model->line["EXAMNO"][$i]]["SP_SCHOLAR_CD"] = $model->line["SP_SCHOLAR_CD"][$i];
            }
        }

        $leftOpt = array();
        $rightOpt = array();
        $query  = knjl672aQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['PROCEDUREDIV'] == "1" && $row['ENTDIV'] == "1") {
                $leftOpt[] = array("value" => $row['EXAMNO'], "label" => $row['EXAMNO']." ".$row['NAME']);
            } else {
                $rightOpt[] = array("value" => $row['EXAMNO'], "label" => $row['EXAMNO']." ".$row['NAME']);
            }
        }
        $result->free();

        $arg["data"]["LEFT_CNT"] = get_count($leftOpt);
        $extra = "multiple style=\"width:250px\" ondblclick=\"move('right');\" ";
        $arg["data"]["LEFT_SEL"] = knjCreateCombo($objForm, "SELECT_EXAMNO", "", $leftOpt, $extra, 20);

        $arg["data"]["RIGHT_CNT"] = get_count($rightOpt);
        $extra = "multiple style=\"width:250px\" ondblclick=\"move('left');\" ";
        $arg["data"]["RIGHT_SEL"] = knjCreateCombo($objForm, "EXAMNO", "", $rightOpt, $extra, 20);

        $disable  = (get_count($leftOpt) > 0 ||get_count($rightOpt) > 0) ? "" : " disabled";
        //ラジオボタン(1:取り込み/2:書き出し)
        $opt = array(1, 2);
        $model->field["CSV_INOUT"] = ($model->field["CSV_INOUT"] == "") ? "1" : $model->field["CSV_INOUT"];
        $extra = array("id=\"CSV_INOUT1\"", "id=\"CSV_INOUT2\"");
        $radioArray = knjCreateRadio($objForm, "CSV_INOUT", $model->field["CSV_INOUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["csv"][$key] = $val;
        }
        //ファイル
        $extra = "".$disable;
        $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);
        //実行ボタン
        $extra = "onclick=\"return btn_submit('csv');\"".$disable;
        $arg["csv"]["btn_csv"] = knjCreateBtn($objForm, "btn_input", "実 行", $extra);
        //ヘッダ有チェックボックス
        $check_header = "";
        if ($model->cmd == "" || $model->field["HEADER"] == "on") {
            $check_header = " checked ";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        /**************/
        /**ボタン作成**/
        /**************/
        makeBtn($objForm, $arg, $disable);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl672aindex.php", "", "edit");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl672aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($blank == "BLANK") {
            continue;
        }
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

    $arg["top"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $disable)
{

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('left', 'ALL');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('right', 'ALL');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //更新ボタン
    $extra = " onclick=\"return btn_submit('update');\"".$disable;
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = " onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = " onclick=\"return btn_submit('close');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);

    knjCreateHidden($objForm, "ENTRY_EXAMNO");
    knjCreateHidden($objForm, "NONENTRY_EXAMNO");
}
