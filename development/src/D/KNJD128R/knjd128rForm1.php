<?php

require_once('for_php7.php');

class knjd128rForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd128rindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ(成績用)
        $query = knjd128rQuery::getSemester();
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //学期(在籍・講座用)
        $model->field["SCH_SEMESTER"] = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //学級コンボ
        $query = knjd128rQuery::getHrClass($model);
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");

        //テスト種別コンボ
        $query = knjd128rQuery::getTest($model);
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTKINDCD"], "TESTKINDCD", $extra, 1, "");

        //学期の開始日付、終了日付
        $sem = $db->getRow(knjd128rQuery::getSemester($model->field["SCH_SEMESTER"]), DB_FETCHMODE_ASSOC);

        //異動者取得
        $idou_list1 = $idou_list2 = array();

        //異動基準日取得
        if ($sem["SDATE"] <= CTRL_DATE && CTRL_DATE <= $sem["EDATE"]) {
            $idou_date = CTRL_DATE;
        } else {
            $idou_date = $sem["EDATE"];
        }

        //異動者一覧（退学・転学・卒業）
        $query = knjd128rQuery::getIdouData($model, $idou_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $idou_list1[$row["SCHREGNO"]] = $row["CNT"];
        }

        //異動者一覧（留学・休学）
        $query = knjd128rQuery::getTransferData($model, $idou_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $idou_list2[$row["SCHREGNO"]] = $row["CNT"];
        }

        //科目一覧取得
        $subclass = $sep = "";
        $subclass_list = array();
        $query = knjd128rQuery::getSubclassList($model);
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
        $query = knjd128rQuery::getSchList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schno .= $sep.$row["SCHREGNO"];
            $sch_array[] = $row;
            $sep = ",";
        }
        knjCreateHidden($objForm, "SCH_ARRAY", $schno);

        //項目取得
        $subclass_cnt = 0;
        foreach ($subclass_list as $sub_key => $sub_val) {
            //科目名
            $arg["subclass"][$subclass_cnt]["LABEL"] = $sub_val;
            $subclass_cnt++;
        }

        //表幅
        $arg["WIDTH"] = 90 * (int)$subclass_cnt + 7 * (int)$subclass_cnt;

        //成績データ取得
        $setData = array();
        $query = knjd128rQuery::getMainQuery($model, $sdate, $edate, $subclass);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setData[$row["SCHREGNO"]][$row["SUBCLASS"]] = $row;
        }

        //明細表示
        $schCnt = 0;
        foreach ($sch_array as $sch_key => $sch_val) {
            $recordData = "";
            $rec_field = $sep = "";
            foreach ($subclass_list as $sub_key => $sub_val) {
                $tmpData = "";
                if (!isset($model->warning)) {
                    $value = $setData[$sch_val["SCHREGNO"]][$sub_key]["VALUE_DI"] == "*" ? $setData[$sch_val["SCHREGNO"]][$sub_key]["VALUE_DI"] : $setData[$sch_val["SCHREGNO"]][$sub_key]["SCORE"];
                } else {
                    $value = $model->field["SCORE"][$sub_key][$sch_key];
                }

                if ($setData[$sch_val["SCHREGNO"]][$sub_key]["FLG"] == "1") {
                    //テキスト入力
                    $extra = " STYLE=\"text-align: right\"; onblur=\"scoreCheck(this)\"; onPaste=\"return showPaste(this, ".$schCnt.");\";";
                    $tmpData = knjCreateTextBox($objForm, $value, "SCORE".":".$sub_key."[]", 3, 3, $extra);

                    $rec_field .= $sep.$sub_key;
                    $sep = ",";
                } else {
                    $extra = " STYLE=\"text-align: right; background-color: silver;\"; readonly";
                    $tmpData = knjCreateTextBox($objForm, $value, "SCORE".":".$sub_key."[]", 3, 3, $extra);
                }
                //格納
                $recordData .= "<td width=\"90\" align=\"center\">{$tmpData}</td>\n";
            }

            $sch_val["RECORD"] = $recordData;

            //異動期間は背景色を黄色にする
            $sch_val["BGCOLOR_IDOU"] = ($idou_list1[$sch_val["SCHREGNO"]] > 0 || $idou_list2[$sch_val["SCHREGNO"]] > 0) ? "bgcolor=yellow" : "bgcolor=#ffffff";

            $sch_val["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$sch_val["SCHREGNO"]."\">";
            $sch_val["RECORD_FIELD"] = "<input type=\"hidden\" name=\"RECORD_FIELD[]\" value=\"".$rec_field."\">";

            $arg["record_data"][] = $sch_val;

            $schCnt++;
        }
        knjCreateHidden($objForm, "objCntSub", $schCnt);

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
        View::toHTML($model, "knjd128rForm1.html", $arg);
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
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model, $schno, $subclass) {
    //更新ボタン
    $extra = ($schno && $subclass && $model->field["TESTKINDCD"]) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
