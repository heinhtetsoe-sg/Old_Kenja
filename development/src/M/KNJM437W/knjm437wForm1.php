<?php

class knjm437wForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm437windex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR;
        $arg["TOP"]["SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //テスト種別
        $query = knjm437wQuery::getTestcd($model);
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->testcd, $extra, 1, "");

        //日付
        $model->test_date = (strlen($model->test_date)) ? $model->test_date : str_replace("-", "/", CTRL_DATE);
        $arg["TOP"]["TEST_DATE"] = View::popUpCalendar($objForm, "TEST_DATE", $model->test_date);

        //科目コンボ
        $extra = "onChange=\"btn_submit('main')\";";
        $query = knjm437wQuery::ReadQuery($model);
        makeCmb($objForm, $arg, $db, $query, "SELSUB", $model->sub, $extra, 1, "blank");

        //データ件数
        $counts = 0;

        //リスト
        if (strlen($model->sub)) {
            $colorFlg = false; //５行毎に背景色を変更
            $query = knjm437wQuery::getList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //行No
                $counts++;
                //学籍番号
                knjCreateHidden($objForm, "SCHREGNO_".$counts, $row["SCHREGNO"]);
                //氏名
                //受験許可
                //受験
                $chk = ($row["TESTCD"] != "" && $row["TESTCD"] == $model->testcd && $row["ATTEND"] == "1") ? " checked " : "";
                $dis = ($row["SEM_PASS_FLG"] == "1") ? "" : " disabled ";
                if ($row["TESTCD"] != "" && $row["TESTCD"] != $model->testcd) $dis = " disabled ";
                $row["CHK_JUKEN"] = knjCreateCheckBox($objForm, "CHK_JUKEN_".$counts, "1", $chk.$dis);
                knjCreateHidden($objForm, "CHK_DIS_".$counts, $dis ? "1" : "");
                //登録テスト種別
                //登録日付
                $row["TEST_DATE"] = str_replace("-", "/", $row["TEST_DATE"]);

                //５行毎に背景色を変更
                if ($counts % 5 == 1) {
                    $colorFlg = !$colorFlg;
                }
                $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

                //表示
                $arg["data"][] = $row;
            }
            $result->free();
        }

        //データ件数
        knjCreateHidden($objForm, "DATA_CNT", $counts);

        //更新ボタン
        $dis = ($counts == 0) ? " disabled " : "";
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$dis);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm437wForm1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
