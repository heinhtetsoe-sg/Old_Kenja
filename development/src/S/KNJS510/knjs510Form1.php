<?php

require_once('for_php7.php');

class knjs510form1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjs510index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //祝祭日区分名称
        $holi_div = array("1" => '日付指定', "2" => '回数曜日指定', "3" => '春分の日', "4" => '秋分の日');
        //曜日名称
        $week = array("1" => '日', "2" => '月', "3" => '火', "4" => '水', "5" => '木', "6" => '金', "7" => '土');

        //公休日リスト表示
        $query = knjs510Query::getPubHolidayList();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["HOLIDAY_DIV"]     = $row["HOLIDAY_DIV"].':'.$holi_div[$row["HOLIDAY_DIV"]];
            $row["HOLIDAY_MONTH"]   = ($row["HOLIDAY_MONTH"]) ? sprintf("%02d", $row["HOLIDAY_MONTH"]).'月' : "";
            $row["HOLIDAY_DAY"]     = ($row["HOLIDAY_DAY"]) ? sprintf("%02d", $row["HOLIDAY_DAY"]).'日' : "";
            $row["HOLIDAY_WEEK_PERIOD"] = ($row["HOLIDAY_WEEK_PERIOD"]) ? $row["HOLIDAY_WEEK_PERIOD"].'週目' : "";
            $row["HOLIDAY_WEEKDAY"] = ($row["HOLIDAY_WEEKDAY"]) ? $week[$row["HOLIDAY_WEEKDAY"]].'曜日' : "";

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjs510Form1.html", $arg); 
    }
}
?>
