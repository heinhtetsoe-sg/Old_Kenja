<?php

require_once('for_php7.php');

class knjb3043_chair_std_selectForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb3043_chair_std_selectForm1", "POST", "knjb3043_chair_std_selectindex.php", "", "knjb3043_chair_std_selectForm1");

        //DB接続
        $db = Query::dbCheckOut();

        // 展開表パターン
        knjCreateHidden($objForm, "PRESEQ", $model->field["PRESEQ"]);
        //時間割で選択されているセルの項目
        knjCreateHidden($objForm, "SELECT_CHAIRCD", $model->field["SELECT_CHAIRCD"]);
        knjCreateHidden($objForm, "SELECT_PREORDER", $model->field["SELECT_PREORDER"]);

        //年度
        $arg["data"]["YEAR"] = $model->field["YEAR"];
        knjCreateHidden($objForm, "YEAR", $model->field["YEAR"]);
        //学期
        $arg["data"]["SEMESTER"] = $model->field["SEMESTER"];
        knjCreateHidden($objForm, "SEMESTER", $model->field["SEMESTER"]);
        //学期開始日～学期終了日取得
        $query = knjb3043_chair_std_selectQuery::getSemesterDate($model);
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
            $query = knjb3043_chair_std_selectQuery::getStdOverlap($model);
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
        // 展開表画面で選択されている講座を初期選択にする(複数講座の場合、配列[0]を初期選択にする)
        if (!$model->field["OVERLAP_CHAIR"]) {
            $selectChairList = explode(",", $model->field["SELECT_CHAIRCD"]);
            $model->field["OVERLAP_CHAIR"] = $selectChairList[0];
        }
        $query = knjb3043_chair_std_selectQuery::getOverlapChairList($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "OVERLAP_CHAIR", $model->field["OVERLAP_CHAIR"], $extra, 1);

        // 適用開始日(学期開始日付 固定)
        $startDate = str_replace("/", "-", $model->field["START_DATE"]);
        $startDate = $startDate ? $startDate : $model->field["SEMESTER_START"];
        if ($startDate < $model->field["SEMESTER_START"] || $startDate > $model->field["SEMESTER_END"]) {
            $startDate = $model->field["SEMESTER_START"];
        }

        $extra = "readonly style=\"background:#e9e9e9\"";
        $arg["START_DATE"] = knjCreateTextBox($objForm, str_replace("-", "/", $startDate), "START_DATE", 10, 10, $extra);

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

        // 下段コンボ用
        $hrToChairCmb = array();

        // 講座情報取得
        $query = knjb3043_chair_std_selectQuery::getChairInfo($model);
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        if ($row) {
            $arg["data"]["CHAIRCD"]      = $row["CHAIRCD"];
            $arg["data"]["CHAIRNAME"]    = $row["CHAIRABBV"];
            $arg["data"]["SUBCLASSCD"]   = $row["SUBCLASSCD"];
            $arg["data"]["SUBCLASSNAME"] = $row["SUBCLASSABBV"];

            // 下段コンボ用
            $hrToChairCmb[0]["VALUE"] = $row["CHAIRCD"];
            $hrToChairCmb[0]["LABEL"] = $row["CHAIRCD"].":".$row["CHAIRABBV"];
        }
        // 講座の職員情報取得
        $query = knjb3043_chair_std_selectQuery::getChairStaffList($model, $model->field["OVERLAP_CHAIR"]);
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

        // 講座受講生徒
        $stdData["SCHREGNO"] = array();

        // 講座の名簿取得
        $query = knjb3043_chair_std_selectQuery::getChairStd($model, $model->field["OVERLAP_CHAIR"]);
        $extra = ' style="width:100%;height:200px" multiple="multiple" data-chaircd="'.$model->field["OVERLAP_CHAIR"].'"';
        $arg["data"]["STDLIST"] = makeCmb2($objForm, $arg, $db, $query, "STDLIST", $model->field["STDLIST"], $extra, 1, "", $stdData, $model);

        if ($model->field["SELECT_TYPE"] == "1") {
            $query = knjb3043_chair_std_selectQuery::getChairGroup($model, $model->field["OVERLAP_CHAIR"]);
        } else {
            $query = knjb3043_chair_std_selectQuery::getChairCompGroup($model, $model->field["OVERLAP_CHAIR"]);
        }
        $result = $db->query($query);
        $chairCnt = 1;

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chair = array();
            $chair["CHAIRCD"]      = $row["CHAIRCD"];
            $chair["CHAIRNAME"]    = $row["CHAIRABBV"];
            $chair["SUBCLASSCD"]   = $row["SUBCLASSCD"];
            $chair["SUBCLASSNAME"] = $row["SUBCLASSABBV"];

            // 下段コンボ用
            $hrToChairCmb[$chairCnt]["VALUE"] = $row["CHAIRCD"];
            $hrToChairCmb[$chairCnt]["LABEL"] = $row["CHAIRCD"] .":".$row["CHAIRABBV"];
            $chairCnt++;

            // HR検索用
            $allChairCd[] = $row["CHAIRCD"];

            // 講座の職員情報取得
            $query = knjb3043_chair_std_selectQuery::getChairStaffList($model, $row["CHAIRCD"]);
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
            $query = knjb3043_chair_std_selectQuery::getChairStd($model, $row["CHAIRCD"]);
            $extra = ' style="width:100%;height:200px" multiple="multiple" data-chaircd="'.$row["CHAIRCD"].'"';
            $chair["STDLIST"] = makeCmb2($objForm, $arg, $db, $query, "STDLIST", $model->field["STDLIST"], $extra, 1, "", $stdData, $model);

            $arg["data2"]["CHAIRLIST"][] = $chair;

        }
        $result->free();

        $allChairCd[] = $model->field["OVERLAP_CHAIR"];

        // クラス情報取得
        $query = knjb3043_chair_std_selectQuery::getHrInfo($model, $allChairCd);
        $result = $db->query($query);

        $hrCnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data['HR_NAME'] = $row['HR_NAME'];

            $query = knjb3043_chair_std_selectQuery::getHrStd($model, $row['GRADE'],$row['HR_CLASS'], $stdData["SCHREGNO"]);
            $extra = 'style="width:100%;height:200px" multiple="multiple" data-hr="'.$row['GRADE'].'-'.$row['HR_CLASS'].'"';
            $value = null;
            $data['HR_STDLIST'] = makeCmb3($objForm, $arg, $db, $query, "HR_STDLIST_".$hrCnt, $value, $extra, 15, "", $model);
            $arg['data']['HRLIST'][] = $data;
            $hrCnt++;
        }
        $result->free();

        // 下段コンボ
        $opt = array();
        foreach ($hrToChairCmb as $item) {
            $opt[] = array("value" => $item["VALUE"], "label" => $item["LABEL"]);
        }
        $extra = "";
        $arg["HR_TO_CHAIR_CMB"] = knjCreateCombo($objForm, 'HR_TO_CHAIR_CMB', $hrToChairCmb[0]["VALUE"], $opt, $extra, 1);

        // 職員の高さを揃えるため、<br>を追加する
        // 重複講座の職員
        for ($i = get_count($arg["data"]["STAFFLIST"]); $i < $staffMaxCnt; $i++) { 
            $arg["data"]["STAFFLIST"][] = array("LABEL" => "　");
        }
        // 一覧講座の職員
        for ($i=0; $i < get_count($arg["data2"]["CHAIRLIST"]); $i++) { 
            $chair = $arg["data2"]["CHAIRLIST"][$i];
            for ($j = get_count($chair["STAFFLIST"]); $j < $staffMaxCnt; $j++) { 
                $arg["data2"]["CHAIRLIST"][$i]["STAFFLIST"][] = array("LABEL" => "　");
            }
        }

        /**********/
        /* ボタン */
        /**********/
        //HRに戻す
        $extra = " onclick=\"return retrunHr();\"";
        $arg["button"]["btn_backHr"] = knjCreateBtn($objForm, "btn_backHr", "ホームルームに戻す", $extra);
        //上段へ登録
        $extra = " onclick=\"return hrToChair();\"";
        $arg["button"]["btn_HrToChair"] = knjCreateBtn($objForm, "btn_HrToChair", "へ登録", $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJB3043_CHAIR_STD_SELECT");
        knjCreateHidden($objForm, "CHAIR_CNT", $chairCnt);
        knjCreateHidden($objForm, "HR_CNT", $hrCnt);

        if (VARS::post("cmd") == "update") {
            // 親画面(講座時間割)を再読み込み
            $arg["jscript"] = "parentReload();";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjb3043_chair_std_selectForm1.html", $arg);
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
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", &$stdData, $model) {

    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $label = ($row["CHAIRCNT"] > 1) ? "重 " : "　 ";
        if ($model->Properties["studentListDispSchregNo"] == "1") {
            $label .= $row["SCHREGNO"].":".$row["NAME"];
            $sortkey = $row["SCHREGNO"];
        } else {
            $label .= $row["HR_NAMEABBV"]."-".$row["ATTENDNO"].":".$row["NAME"];
            $sortkey = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
        }
        $opt[] = array('label'   => $label,
                       'value'   => $row["SCHREGNO"],
                       'prop'    => $row["SCHREGNO"],
                       'prop2'   => $row["NAME"],
                       'prop3'   => $row["GRADE"]."-".$row["HR_CLASS"],
                       'sortkey' => $sortkey
        );

        $stdData["SCHREGNO"][] = $row["SCHREGNO"];
    }
    $result->free();

    $ret = '<select name='.$name.' '.$extra.' size="'.$size.'">'."\n";
    for($i=0;$i<get_count($opt);$i++){
        $ret .= '<option value="'.$opt[$i]['value'].'" data-prop="'.$opt[$i]['prop'].'" data-prop2="'.$opt[$i]['prop2'].'" data-prop3="'.$opt[$i]['prop3'].'" data-sortkey="'.$opt[$i]['sortkey'].'">'.$opt[$i]['label'].'</option>'."\n";
    }
    $ret .= '</select>'."\n";
    return $ret;
}

//コンボ作成3
function makeCmb3(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $model) {

    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->Properties["studentListDispSchregNo"] == "1") {
            $label = "　 ".$row["SCHREGNO"].":".$row["NAME"];
            $sortkey = $row["SCHREGNO"];
        } else {
            $label = "　 ".$row["HR_NAMEABBV"]."-".$row["ATTENDNO"].":".$row["NAME"];
            $sortkey = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
        }
        $opt[] = array('label' => $label,
            'value'   => $row["SCHREGNO"],
            'prop'    => $row["SCHREGNO"],
            'prop2'   => $row["NAME"],
            'sortkey' => $sortkey
        );
    }
    $result->free();

    $ret = '<select name='.$name.' '.$extra.' size="'.$size.'">'."\n";
    for($i=0;$i<get_count($opt);$i++){
        $ret .= '<option value="'.$opt[$i]['value'].'" data-prop="'.$opt[$i]['prop'].'" data-prop2="'.$opt[$i]['prop2'].'" data-sortkey="'.$opt[$i]['sortkey'].'">'.$opt[$i]['label'].'</option>'."\n";
    }
    $ret .= '</select>'."\n";
    return $ret;
}

?>
