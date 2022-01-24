<?php

require_once('for_php7.php');

class knjb3042_chair_stdForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb3042_chair_stdForm1", "POST", "knjb3042_chair_stdindex.php", "", "knjb3042_chair_stdForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //基本時間割
        knjCreateHidden($objForm, "SCH_PTRN", $model->field["SCH_PTRN"]);
        knjCreateHidden($objForm, "BSCSEQ", $model->field["BSCSEQ"]);
        //選択講座コード(時間割で選択されているセルの講座コード)
        knjCreateHidden($objForm, "SELECT_CHAIRCD", $model->field["SELECT_CHAIRCD"]);

        //年度
        $arg["data"]["YEAR"] = $model->field["YEAR"];
        knjCreateHidden($objForm, "YEAR", $model->field["YEAR"]);
        //学期
        $arg["data"]["SEMESTER"] = $model->field["SEMESTER"];
        knjCreateHidden($objForm, "SEMESTER", $model->field["SEMESTER"]);
        //学期開始日～学期終了日取得
        $query = knjb3042_chair_stdQuery::getSemesterDate($model);
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $model->field["SEMESTER_START"] = $row["SDATE"];
        $model->field["SEMESTER_END"] = $row["EDATE"];
        knjCreateHidden($objForm, "SEMESTER_START", $row["SDATE"]);
        knjCreateHidden($objForm, "SEMESTER_END", $row["EDATE"]);

        // 受講生の重複チェック
        if ($model->cmd == 'getStdOverlap') {
            $response = array();
            $response["STDLIST"] = array();
            $query = knjb3042_chair_stdQuery::getStdOverlap($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["CHAIRCNT"] > 1) {
                    $response["STDLIST"][]   = $row["SCHREGNO"];
                }
            }
            echo json_encode($response);
            die();
        }

        // 重複講座一覧取得
        $query = knjb3042_chair_stdQuery::getOverlapChairList($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "OVERLAP_CHAIR", $model->field["OVERLAP_CHAIR"], $extra, 1);

        //適用開始日付
        $startDate = str_replace("/", "-", $model->field["START_DATE"]);
        $startDate = $startDate ? $startDate : CTRL_DATE;
        if ($startDate < $model->field["SEMESTER_START"] || $startDate > $model->field["SEMESTER_END"]) {
            $startDate = $model->field["SEMESTER_START"];
        }
        $extra = "extra=dateChange(f.document.forms[0][\\'START_DATE\\'].value);";
        $arg["START_DATE"] = View::popUpCalendar($objForm, "START_DATE", str_replace("-", "/", $startDate), $extra);

        //科目・群
        $opt = array(1, 2);
        $model->field['SELECT_TYPE'] = ($model->field['SELECT_TYPE'] == "") ? '1' : $model->field['SELECT_TYPE'];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SELECT_TYPE{$val}\" onClick=\"btn_submit('main')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SELECT_TYPE", $model->field['SELECT_TYPE'], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        // 職員の高さを揃える
        $staffMaxCnt = 0;

        // 講座情報取得
        $query = knjb3042_chair_stdQuery::getChairInfo($model);
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        if ($row) {
            $arg["data"]["CHAIRCD"]      = $row["CHAIRCD"];
            $arg["data"]["CHAIRNAME"]    = $row["CHAIRABBV"];
            $arg["data"]["SUBCLASSCD"]   = $row["SUBCLASSCD"];
            $arg["data"]["SUBCLASSNAME"] = $row["SUBCLASSABBV"];
        }
        // 講座の職員情報取得
        $query = knjb3042_chair_stdQuery::getChairStaffList($model, $model->field["OVERLAP_CHAIR"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $staff = array();
            $staff["STAFFCD"] = $row["STAFFCD"];
            $staff["STAFFNAME"] = $row["STAFFNAME"];
            $staff["LABEL"] = $row["STAFFCD"].":".$row["STAFFNAME"];
            $arg["data"]["STAFFLIST"][]   = $staff;
        }
        // 職員の人数取得
        $staffMaxCnt = get_count($arg["data"]["STAFFLIST"]);

        // 講座の名簿取得
        $query = knjb3042_chair_stdQuery::getChairStd($model, $model->field["OVERLAP_CHAIR"]);
        $extra = ' style="width:100%;height:200px" multiple="multiple" data-chaircd="'.$model->field["OVERLAP_CHAIR"].'"';
        $arg["data"]["STDLIST"] = makeCmb2($objForm, $arg, $db, $query, "STDLIST", $model->field["STDLIST"], $extra, 1);

        if ($model->field["SELECT_TYPE"] == "1") {
            $query = knjb3042_chair_stdQuery::getChairGroup($model, $model->field["OVERLAP_CHAIR"]);
        } else {
            $query = knjb3042_chair_stdQuery::getChairCompGroup($model, $model->field["OVERLAP_CHAIR"]);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chair = array();
            $chair["CHAIRCD"]      = $row["CHAIRCD"];
            $chair["CHAIRNAME"]    = $row["CHAIRABBV"];
            $chair["SUBCLASSCD"]   = $row["SUBCLASSCD"];
            $chair["SUBCLASSNAME"] = $row["SUBCLASSABBV"];

            // 講座の職員情報取得
            $query = knjb3042_chair_stdQuery::getChairStaffList($model, $row["CHAIRCD"]);
            $result2 = $db->query($query);
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $staff = array();
                $staff["STAFFCD"] = $row2["STAFFCD"];
                $staff["STAFFNAME"] = $row2["STAFFNAME"];
                $staff["LABEL"] = $row2["STAFFCD"].":".$row2["STAFFNAME"];
                $chair["STAFFLIST"][]   = $staff;
            }
            if ($staffMaxCnt < get_count($chair["STAFFLIST"])) {
                // 職員の人数取得
                $staffMaxCnt = get_count($chair["STAFFLIST"]);
            }

            // 講座の名簿取得
            $query = knjb3042_chair_stdQuery::getChairStd($model, $row["CHAIRCD"]);
            $extra = ' style="width:100%;height:200px" multiple="multiple" data-chaircd="'.$row["CHAIRCD"].'"';
            $chair["STDLIST"] = makeCmb2($objForm, $arg, $db, $query, "STDLIST", $model->field["STDLIST"], $extra, 1);

            $arg["data2"]["CHAIRLIST"][]   = $chair;
        }
        $result->free();


        // 職員の高さを揃えるため、<br>を追加する
        // 重複講座の職員
        for ($i = get_count($arg["data"]["STAFFLIST"]); $i < $staffMaxCnt; $i++) { 
            $arg["data"]["STAFFLIST"][] = "　";
        }
        // 一覧講座の職員
        for ($i=0; $i < get_count($arg["data2"]["CHAIRLIST"]); $i++) { 
            $chair = $arg["data2"]["CHAIRLIST"][$i];
            for ($j = get_count($chair["STAFFLIST"]); $j < $staffMaxCnt; $j++) { 
                $arg["data2"]["CHAIRLIST"][$i]["STAFFLIST"][] = "　";
            }
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onClick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //戻る
        $extra = "onClick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATE_CHAIRLIST", "");
        knjCreateHidden($objForm, "PRGID", "KNJB3042_CHAIR_STD");

        if (VARS::post("cmd") == "update") {
            // 親画面(講座時間割)を再読み込み
            $arg["jscript"] = "parentReload();";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjb3042_chair_stdForm1.html", $arg);
    }
}

/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "OVERLAP_CHAIR") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }

    return;
}
//コンボ作成2
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {

    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $label = "　 ".$row["SCHREGNO"].":".$row["NAME"];
        if ($row["CHAIRCNT"] > 1) {
            $label = "重 ".$row["SCHREGNO"].":".$row["NAME"];
        }
        $opt[] = array('label' => $label,
                       'value' => $row["SCHREGNO"],
                       'prop' => $row["SCHREGNO"],
                       'prop2' => $row["NAME"]);
    }
    $result->free();

    $ret = '<select name='.$name.' '.$extra.' size="'.$size.'">'."\n";
    for($i=0;$i<get_count($opt);$i++){
        $ret .= '<option value="'.$opt[$i]['value'].'" data-prop="'.$opt[$i]['prop'].'" data-prop2="'.$opt[$i]['prop2'].'">'.$opt[$i]['label'].'</option>'."\n";
    }
    $ret .= '</select>'."\n";
    return $ret;
}

?>
