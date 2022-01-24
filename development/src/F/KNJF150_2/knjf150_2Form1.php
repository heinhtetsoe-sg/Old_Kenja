<?php

require_once('for_php7.php');

class knjf150_2Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("knjf150_2Form1", "POST", "knjf150_2index.php", "", "knjf150_2Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $schInfo = $db->getRow(knjf150_2Query::getSchregInfo($model), DB_FETCHMODE_ASSOC);
        $arg["SCHINFO"] = $schInfo["HR_NAME"].$schInfo["ATTENDNO"].'番　'.$schInfo["NAME"];

        //生徒項目名
        $arg["SCH_LABEL"] = $model->sch_label;

        //直近データ(最大50件表示)
        $show_limit = 0;
        $opt_seqno = array();
        $opt_seqno[] = array('label' => "(((新規)))", 'value' => "");//初期値
        $show_seqno = array();
        $result = $db->query(knjf150_2Query::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($show_limit == 50) {
                break;
            }
            $opt_seqno[] = array('label' => "受付番号：" .$row["SEQNO"] ."、更新年月日：" .str_replace("-", "/", $row["UPDATED1"]) ." " .$row["UPDATED2"],
                                 'value' => $row["SEQNO"]);
            $show_limit++;
        }
        $extra = "onChange=\"return btn_submit('schno');\"";
        $arg["data"]["SEQNO"] = knjCreateCombo($objForm, "SEQNO", $model->field["SEQNO"], $opt_seqno, $extra, 1);

        //１レコード取得
        if ($model->field["SEQNO"] != "" && !isset($model->warning)) {
            $Row = $db->getRow(knjf150_2Query::getListRow($model), DB_FETCHMODE_ASSOC);
        } elseif (isset($model->warning)) {
            $Row =& $model->field;
        }

        //開始日付
        $date1 = isset($Row["FROMDATE"])?str_replace("-", "/", $Row["FROMDATE"]):str_replace("-", "/", CTRL_DATE);
        $arg["data"]["FROMDATE"] = View::popUpCalendar($objForm, "FROMDATE", $date1);

        //終了日付
        $date2 = isset($Row["TODATE"])?str_replace("-", "/", $Row["TODATE"]):str_replace("-", "/", CTRL_DATE);
        $arg["data"]["TODATE"] = View::popUpCalendar($objForm, "TODATE", $date2);

        //校時
        $data_cnt = 0;
        $opt_periodcd = $model->arr_period = array();
        $opt_periodcd[] = array('label' => "", 'value' => "");//初期値
        $result = $db->query(knjf150_2Query::getPeriodcd());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_periodcd[] = array('label' => $row["NAMECD2"] ."　" .$row["NAME1"],
                                    'value' => $row["NAMECD2"]);
            if ($data_cnt == 0) {
                $model->s_period = $row["NAMECD2"];
            }
            $model->e_period = $row["NAMECD2"];
            $model->arr_period[] = $row["NAMECD2"];
            $data_cnt++;
        }
        //開始校時
        $extra = "";
        $arg["data"]["FROMPERIOD"] = knjCreateCombo($objForm, "FROMPERIOD", $Row["FROMPERIOD"], $opt_periodcd, $extra, 1);


        //終了校時
        $extra = "";
        $arg["data"]["TOPERIOD"] = knjCreateCombo($objForm, "TOPERIOD", $Row["TOPERIOD"], $opt_periodcd, $extra, 1);

        //勤怠
        $opt_dicd = array();
        $opt_dicd[] = array('label' => "(((なし)))", 'value' => "");//初期値
        $result = $db->query(knjf150_2Query::getDicd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_dicd[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["DI_CD"] = knjCreateCombo($objForm, "DI_CD", $Row["DI_CD"], $opt_dicd, $extra, 5);

        //症状・理由リストToリスト作成
        makeDiremarkList($objForm, $arg, $db, $model);

        //その他
        $extra = "";
        $arg["data"]["SONOTA"] = knjCreateTextBox($objForm, $Row["SONOTA"], "SONOTA", 40, 20, $extra);

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //終了
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "SCHREGNO", $model->field["SCHREGNO"]);
        knjCreateHidden($objForm, "CALL_INFO", $model->field["CALL_INFO"]);

        //DB切断
        $result->free();
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf150_2Form1.html", $arg);
    }
}

//症状・理由リストToリスト作成
function makeDiremarkList(&$objForm, &$arg, $db, $model)
{
    $opt_left_id = $opt_left = $opt_right = array();

    //症状・理由一覧（左）
    if ($model->field["SEQNO"] && $model->field["SCHREGNO"]) {
        if (strlen(implode($model->selectdata, ','))) {
            foreach ($model->selectdata as $key => $val) {
                $query = knjf150_2Query::getDiRemarkcd2($model, "C900", $val);
                $optSet = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (is_array($optSet)) {
                    $opt_left[]= array('label' => $optSet["LABEL"],
                                       'value' => $optSet["VALUE"]);
                    $opt_left_id[] = $optSet["VALUE"];
                }
            }
        } else {
            $query = knjf150_2Query::getDiRemarkcd1($model, "C900");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["VALUE"]) {
                    $opt_left[] = array('label' => $row["LABEL"],
                                        'value' => $row["VALUE"]);
                    $opt_left_id[] = $row["VALUE"];
                }
            }
        }
    }

    //対象一覧（右）
    $opt_left_id = (strlen(implode($model->selectdata, ','))) ? $model->selectdata : $opt_left_id;
    $query = knjf150_2Query::getDiRemarkList($model, "C900", $opt_left_id);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($selectData[$row["VALUE"]] != "1" ) {
            $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //症状・理由一覧リストを作成する
    $extra = "multiple style=\"width:200px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 10);

    //対象一覧リストを作成する
    $extra = "multiple style=\"width:200px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 10);

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
