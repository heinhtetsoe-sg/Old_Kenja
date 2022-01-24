<?php

require_once('for_php7.php');

class knjf150eForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("knjf150eForm1", "POST", "knjf150eindex.php", "", "knjf150eForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //事前チェック（プロパティー設定）
        $arg["closing"] = "";
        if ($model->Properties["useKoudome"] != "true" && $model->Properties["useVirus"] != "true") {
            $arg["closing"] = "  closing_window(); " ;
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $query = knjf150eQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('sem');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);

        //初期化
        if ($model->cmd == 'sem') unset($model->field);

        //直近データ(最大50件表示)
        $opt_seqno = array();
        $opt_seqno[] = array('label' => "(((新規)))", 'value' => "");
        $show_seqno = array();
        $result = $db->query(knjf150eQuery::getList($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_seqno[] = array('label' => "受付番号：" .$row["SEQNO"] ."、更新年月日：" .str_replace("-","/",$row["FIRST_DATE1"]) ." " .$row["FIRST_DATE2"],
                                 'value' => $row["SEQNO"]);
        }
        $result->free();
        $extra = "onChange=\"return btn_submit('main');\"";
        $arg["data"]["SEQNO"] = knjCreateCombo($objForm, "SEQNO", $model->field["SEQNO"], $opt_seqno, $extra, 1);

        //１レコード取得
        if ($model->field["SEQNO"] != "" && !isset($model->warning) && $model->cmd != 'change') {
            $Row = $db->getRow(knjf150eQuery::getListRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        if ($model->cmd != 'change' && !isset($model->warning)) {
            if (!$model->field["SEQNO"]) {
                if ($model->semester == CTRL_SEMESTER) {
                    $Row["FROM_DATE"]   = str_replace("-", "/", CTRL_DATE);
                    $Row["TO_DATE"]     = str_replace("-", "/", CTRL_DATE);
                } else {
                    $Row["FROM_DATE"]   = $model->control["学期開始日付"][$model->semester];
                    $Row["TO_DATE"]     = $model->control["学期開始日付"][$model->semester];
                }
            } else if ($model->field["SEQNO"]) {
                $Row["FROM_DATE"]   = str_replace("-", "/", $Row["FROM_DATE"]);
                $Row["TO_DATE"]     = str_replace("-", "/", $Row["TO_DATE"]);
            } else {
                $Row["FROM_DATE"]   = str_replace("-", "/", CTRL_DATE);
                $Row["TO_DATE"]     = str_replace("-", "/", CTRL_DATE);
            }
        }
        //開始日付
        $arg["data"]["FROM_DATE"] = View::popUpCalendar($objForm, "FROM_DATE", $Row["FROM_DATE"]);
        //終了日付
        $arg["data"]["TO_DATE"] = View::popUpCalendar($objForm, "TO_DATE", $Row["TO_DATE"]);

        if ($model->Properties["hibiNyuuryoku"] != "ATTEND_DAY_DAT") {
            $arg["useAttendDat"] = "1";
            //校時取得
            $data_cnt = 0;
            $opt_periodcd = array();
            $opt_periodcd[] = array('label' => "", 'value' => "");
            $result = $db->query(knjf150eQuery::getNameMst($model, 'B001'));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_periodcd[] = array('label' => $row["NAMECD2"]."　".$row["NAME1"],
                                        'value' => $row["NAMECD2"]);
                if ($data_cnt == 0) $model->s_period = $row["NAMECD2"];
                $model->e_period = $row["NAMECD2"];
                $data_cnt++;
            }
            $result->free();
            //開始校時
            $extra = "";
            $arg["data"]["FROM_PERIOD"] = knjCreateCombo($objForm, "FROM_PERIOD", $Row["FROM_PERIOD"], $opt_periodcd, $extra, 1);
            //終了校時
            $extra = "";
            $arg["data"]["TO_PERIOD"] = knjCreateCombo($objForm, "TO_PERIOD", $Row["TO_PERIOD"], $opt_periodcd, $extra, 1);
        }
        //閉鎖区分
        $query = knjf150eQuery::getNameMst($model, 'C045');
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "INPUT_TYPE", $Row["INPUT_TYPE"], $extra, 1, "blank");

        //勤怠
        $query = knjf150eQuery::getNameMst($model, 'C001');
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "DI_CD", $Row["DI_CD"], $extra, 1, "blank");

        $arg["list"] = "";
        if (strlen($Row["INPUT_TYPE"])) {
            $arg["list"] = 1;
            //リストToリスト作成
            makeListToList($objForm, $arg, $db, $model, $Row);
        }

        if ($model->Properties["hibiNyuuryoku"] != "ATTEND_DAY_DAT") {
            //症状・理由
            $query = knjf150eQuery::getNameMst($model, 'C900');
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "DI_REMARK_CD", $Row["DI_REMARK_CD"], $extra, 1, "blank");
        }

        //症状・理由（その他）
        $extra = "";
        $arg["data"]["DI_REMARK"] = knjCreateTextBox($objForm, $Row["DI_REMARK"], "DI_REMARK", 40, 40, $extra);

        /**********/
        /* ボタン */
        /**********/
        //実行
        $extra = (!$model->field["SEQNO"] && strlen($Row["INPUT_TYPE"])) ? "onclick=\"return btn_submit('exec');\"" : " disabled ";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //削除
        $extra = ($model->field["SEQNO"]) ? "onclick=\"return btn_submit('delete');\"" : " disabled ";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "SCHREGNO", $model->field["SCHREGNO"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjf150eForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, &$Row) {
    if ($Row["INPUT_TYPE"] == '3') {
        $arg["data"]["HR_LABEL"] = '年組';
        $query = knjf150eQuery::getHr($model);
    } else {
        $arg["data"]["HR_LABEL"] = '学年';
        $query = knjf150eQuery::getGrade($model);
    }

    //一覧取得
    $opt_left = $opt_right = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (isset($model->warning)) {
            if (in_array($row["VALUE"], $model->selectdata)) {
                $opt_left[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        } else {
            if ($row["FLG"]) {
                $opt_left[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:250px; height:200px\" ondblclick=\"move1('left', 'HR_NAME', 'HR_SELECTED')\"";
    $arg["data"]["HR_NAME"] = knjCreateCombo($objForm, "HR_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:250px; height:200px\" ondblclick=\"move1('right', 'HR_NAME', 'HR_SELECTED')\"";
    $arg["data"]["HR_SELECTED"] = knjCreateCombo($objForm, "HR_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', 'HR_NAME', 'HR_SELECTED');\"";
    $arg["hr_button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', 'HR_NAME', 'HR_SELECTED');\"";
    $arg["hr_button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 'HR_NAME', 'HR_SELECTED');\"";
    $arg["hr_button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', 'HR_NAME', 'HR_SELECTED');\"";
    $arg["hr_button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}
?>
