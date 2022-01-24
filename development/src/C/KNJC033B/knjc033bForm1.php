<?php

require_once('for_php7.php');

class knjc033bForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc033bindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //欠課コード、名称取得
        $C001 = array();
        $updField = $sep = "";
        $attendField = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
        $query = knjc033bQuery::getNameMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $updField .= $sep.$attendField[$row["NAMECD2"]];
            $C001[$row["NAMECD2"]] = array('label' => $row["NAME1"],
                                           'field' => $attendField[$row["NAMECD2"]]);
            $sep = ",";
        }
        knjCreateHidden($objForm, "UPD_FIELD", $updField);

        //事前チェック
        if (get_count($C001) < 1) {
            $arg["jscript"] = "preCheck();";
        }

        //月コンボ
        makeMonthSemeCmb($objForm, $arg, $db, $model);

        //学級コンボ
        $query = knjc033bQuery::getHrClass($model);
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");

        //開始日付、終了日付取得
        $sdate = $edate = "";
        $year = (sprintf('%2d', $model->field["MONTH"]) < 4) ? CTRL_YEAR+1 : CTRL_YEAR;
        $sdate = $year.'-'.$model->field["MONTH"].'-01';
        $edate = $year.'-'.$model->field["MONTH"].'-'.date('t', mktime(0, 0, 0, intval($model->field["MONTH"]), 1, $year));
        //学期の開始日付、終了日付
        $sem = $db->getRow(knjc033bQuery::selectSemesAll($model->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
        if ($model->field["MONTH"] == date('m', strtotime($sem["SDATE"]))) {
            $sdate = $sem["SDATE"];
        }
        if ($model->field["MONTH"] == date('m', strtotime($sem["EDATE"]))) {
            $edate = $sem["EDATE"];
        }

        //締め日取得
        $model->appointed_day = $db->getOne(knjc033bQuery::getAppointedDay($model));

        //異動者取得
        $idou_list1 = $idou_list2 = array();
        if ($model->field["MONTH"]) {
            //異動基準日取得
            if ($model->appointed_day != "") {
                $idou_date = $year.'-'.$model->field["MONTH"].'-'.$model->appointed_day;
            } else if ($model->field["MONTH"] == date('m', strtotime($sem["EDATE"]))) {
                $idou_date = $edate;
            } else {
                $idou_date = $year.'-'.$model->field["MONTH"].'-'.date('t', mktime(0, 0, 0, intval($model->field["MONTH"]), 1, $year));
            }

            //異動者一覧（退学・転学・卒業）
            $query = knjc033bQuery::getIdouData($model, $idou_date);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $idou_list1[$row["SCHREGNO"]] = $row["CNT"];
            }
            //異動者一覧（留学・休学）
            $query = knjc033bQuery::getTransferData($model, $idou_date);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $idou_list2[$row["SCHREGNO"]] = $row["CNT"];
            }
        }

        //科目一覧取得
        $subclass = $sep = "";
        $subclass_list = array();
        $query = knjc033bQuery::getSubclassList($model, $sdate, $edate);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclass .= $sep.$row["SUBCLASS"];
            $subclass_list[$row["SUBCLASS"]] = $row["SUBCLASSABBV"];
            $sep = ",";
        }
        knjCreateHidden($objForm, "SUBCLASS", $subclass);

        //生徒一覧取得
        $schno = $sep = "";
        $sch_array = array();
        $query = knjc033bQuery::getSchList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schno .= $sep.$row["SCHREGNO"];
            $sch_array[] = $row;
            $sep = ",";
        }
        knjCreateHidden($objForm, "SCH_ARRAY", $schno);

        //項目取得
        $subclass_cnt = $c001_cnt = 0;
        foreach ($subclass_list as $sub_key => $sub_val) {
            $col_cnt = 0;
            foreach ($C001 as $c001_key => $c001_val) {
                //欠課時数名称
                $arg["c001"][$c001_cnt]["LABEL"] = $c001_val["label"];
                $c001_cnt++;
                $col_cnt++;
            }
            //科目名
            $arg["subclass"][$subclass_cnt]["col"] = $col_cnt;
            $arg["subclass"][$subclass_cnt]["LABEL"] = $sub_val;
            $subclass_cnt++;
        }

        //表幅
        $arg["WIDTH"] = 60 * (int)$c001_cnt + 7 * (int)$c001_cnt;

        //出欠データ取得
        $setData = array();
        $query = knjc033bQuery::getMainQuery($model, $sdate, $edate, $subclass);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setData[$row["SCHREGNO"]][$row["SUBCLASS"]] = $row;
        }

        //明細表示
        $schCnt = 0;
        foreach ($sch_array as $sch_key => $sch_val) {
            $attendData = "";
            $att_field = $sep = "";
            foreach ($subclass_list as $sub_key => $sub_val) {
                foreach ($C001 as $c001_key => $c001_val) {
                    $tmpData = "";
                    if (!isset($model->warning)) {
                        $value = $setData[$sch_val["SCHREGNO"]][$sub_key][$c001_val["field"]];
                    } else {
                        $value = $model->field[$c001_val["field"]][$sub_key][$sch_key];
                    }

                    //'0'は空白表示
                    if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                        $value = ($value != 0) ? $value : "";
                    }

                    if ($setData[$sch_val["SCHREGNO"]][$sub_key]["FLG"] == "1") {
                        //テキスト入力
                        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; onPaste=\"return showPaste(this, ".$schCnt.");\";";
                        $tmpData = knjCreateTextBox($objForm, $value, $c001_val["field"].":".$sub_key."[]", 2, 3, $extra);

                        $att_field .= $sep.$sub_key;
                        $sep = ",";
                    } else {
                        $extra = " STYLE=\"text-align: right; background-color: silver;\"; readonly";
                        $tmpData = knjCreateTextBox($objForm, $value, $c001_val["field"].":".$sub_key."[]", 2, 3, $extra);
                    }
                    //格納
                    $attendData .= "<td width=\"60\" align=\"center\">{$tmpData}</td>\n";
                }
            }

            $sch_val["ATTEND"] = $attendData;

            //異動期間は背景色を黄色にする
            $sch_val["BGCOLOR_IDOU"] = ($idou_list1[$sch_val["SCHREGNO"]] > 0 || $idou_list2[$sch_val["SCHREGNO"]] > 0) ? "bgcolor=yellow" : "bgcolor=#ffffff";

            $sch_val["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$sch_val["SCHREGNO"]."\">";
            $sch_val["ATTEND_FIELD"] = "<input type=\"hidden\" name=\"ATTEND_FIELD[]\" value=\"".$att_field."\">";

            $arg["attend_data"][] = $sch_val;

            $schCnt++;
        }
        knjCreateHidden($objForm, "objCntSub", $schCnt);

        if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
            $arg["COMMENT"] = "　　　　　　※'0'データは、空白で表示します。";
        }

        //ボタン作成
        makeButton($objForm, $arg, $db, $model, $schno, $subclass);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML($model, "knjc033bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model) {
    $query = knjc033bQuery::selectSemesAll();
    $result = $db->query($query);
    $data = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month = setMonth($db, $data, $model);

    if ($model->field["MONTHCD"] == "") {
        $model->field["MONTHCD"] = $opt_month[0]["value"];
        list ($model->field["MONTH"], $model->field["SEMESTER"]) = preg_split("/-/", $opt_month[0]["value"]);
    }

    $extra = "onChange=\"btn_submit('main')\";";
    $arg["MONTHCD"] = knjCreateCombo($objForm, "MONTHCD", $model->field["MONTHCD"], $opt_month, $extra, 1);
    return;
}

//学期・月データ取得
function setMonth($db, $data, $model) {
    $opt_month = array();
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query = knjc033bQuery::selectMonthQuery($month, $model);
            $getdata = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
            }
        }
    }
    return $opt_month;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model, $schno, $subclass) {
    //更新ボタン
    $extra = ($schno && $subclass && $model->field["MONTHCD"]) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
