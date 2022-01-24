<?php

require_once('for_php7.php');


class knjm437mForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm437mindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR;
        $arg["TOP"]["SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //テスト種別コンボ
        $extra = "onChange=\"btn_submit('main')\";";
        $query = knjm437mQuery::getTestCmb($model);
        makeCmb($objForm, $arg, $db, $query, "TEST_KIND", $model->test_kind, $extra, 1, "blank");

        //日付
        $model->input_date = (strlen($model->input_date)) ? $model->input_date : str_replace("-", "/", CTRL_DATE);
        $arg["TOP"]["INPUT_DATE"] = View::popUpCalendar($objForm, "INPUT_DATE", $model->input_date);

        //科目コンボ
        $extra = "onChange=\"btn_submit('main')\";";
        $query = knjm437mQuery::ReadQuery($model);
        makeCmb($objForm, $arg, $db, $query, "SELSUB", $model->sub, $extra, 1, "blank");

        //データ件数
        $counts = 0;

        //リスト
        if (strlen($model->sub)) {
            $colorFlg = false; //５行毎に背景色を変更
            $result = $db->query(knjm437mQuery::getList($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //行No
                $counts++;
                //学籍番号
                knjCreateHidden($objForm, "SCHREGNO_".$counts, $row["SCHREGNO"]);
                //氏名
                //受験許可
                //受験
                $chk = ($row["TEST_KIND"] != "" && $row["TEST_KIND"] == $model->test_kind) ? " checked " : "";
                $dis = ($row["SEM_PASS_FLG"] == "1") ? "" : " disabled ";
                if ($row["TEST_KIND"] != "" && $row["TEST_KIND"] != $model->test_kind) $dis = " disabled ";
                $row["CHK_JUKEN"] = knjCreateCheckBox($objForm, "CHK_JUKEN_".$counts, "1", $chk.$dis);
                //登録テスト種別
                //登録日付
                $row["INPUT_DATE"] = str_replace("-", "/", $row["INPUT_DATE"]);

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
        View::toHTML($model, "knjm437mForm1.html", $arg); 
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
