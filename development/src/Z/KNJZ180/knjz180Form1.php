<?php

require_once('for_php7.php');

class knjz180form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz180index.php", "", "edit");

        $opt = array();
        $db = Query::dbCheckOut();

        $query = "select distinct substr(char(HOLIDAY),1,7) date_year from holiday_mst ";
        $query .=" union select distinct '".CTRL_YEAR."-04' as date_year from holiday_mst order by date_year";
        $result = $db->query($query);

        $i = 0;
        $getdate = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $mid_date = strtr($row["DATE_YEAR"], "-", "/")."/01";

            if ($getdate !=common::DateConv1($mid_date, 12)) {
                $f_date[$i] = common::DateConv1($mid_date, 12);
                $i++;
            }
            $getdate =common::DateConv1($mid_date, 12);
        }

        $flg_year_select = true;
        for ($i = 0; $i < get_count($f_date); $i++) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            $opt[] = array("label" => $f_date[$i],"value" => $f_date[$i]);
            if ($model->year_select=="" && $i==0) {
                $model->year_select = $f_date[0];
            }
            if ($model->year_select==$f_date[$i]) {
                $flg_year_select = false;
            }
        }
        if ($flg_year_select) {
            $model->year_select = $f_date[0];
        }
        //年度範囲を表示
        $date_start = $model->year_select."-04-01";
        $year_end = (int)$model->year_select+1;
        $date_end = $year_end."-03-31";
        if (checkdate("04", "01", $model->year_select)) {
            $query  = "select * from holiday_mst ";
            $query .= "where HOLIDAY between'".$date_start."' and '" .$date_end. "' order by HOLIDAY";
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //レコードを連想配列のまま配列$arg[data]に追加していく。
                array_walk($row, "htmlspecialchars_array");
                $row["HOLIDAY"] = str_replace("-", "/", $row["HOLIDAY"]);
                $arg["data"][] = $row;
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae(array("type"       => "select",
                                "name"       => "year_select",
                                "size"       => "1",
                                "value"      => $model->year_select,
                                "extrahtml"   => "onchange=\"return btn_submit('list');\"",
                                "options"    => $opt));

        $arg["top"]["year_select"] = $objForm->ge("year_select");

        //終了ボタンを作成する
        $objForm->ae(array("type"        => "button",
                                "name"        => "btn_copy",
                                "value"       => "前年度からコピー",
                                "extrahtml"   => "onclick=\"return btn_submit('copy');\"" ));

        $arg["button"]["btn_copy"] = $objForm->ge("btn_copy");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                                "name"      => "cmd"
                                ));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz180Form1.html", $arg);
    }
}
