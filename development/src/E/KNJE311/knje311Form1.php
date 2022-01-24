<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje311Form1.php 56587 2017-10-22 12:54:51Z maeshiro $

class knje311Form1 {
    function main(&$model) {
        $year = $model->field["YEAR"];
        $school_sort = $model->field["SCHOOL_SORT"];

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knje311Form1", "POST", "knje311index.php", "", "knje311Form1");

        // 配列を取得
        $opt_year = knje311Query::getYear();
        $opt_school_sort = knje311Query::getNameMst("E001");

        // 年度
        $objForm->ae( array("type"          => "select",
                            "name"          => "YEAR",
                            "size"          => "1",
                            "extrahtml"     => "onChange=\"return myBtnSubmit('knje311');\"",
                            "value"         => $year,
                            "options"       => $opt_year));
        $arg["YEAR"] = $objForm->ge("YEAR");

        // 種別（学校・会社）
        $objForm->ae( array("type"          => "select",
                            "name"          => "SCHOOL_SORT",
                            "size"          => "1",
                            "extrahtml"     => "onChange=\"return myBtnSubmit('knje311');\"",
                            "value"         => $school_sort,
                            "options"       => $opt_school_sort));
        $arg["SCHOOL_SORT"] = $objForm->ge("SCHOOL_SORT");

        // 受験先種別ｺｰﾄﾞを決める
        if ("04" < $school_sort) { // 会社
            $senkou_kind = 1;
            $arg["BANGO"] = "求人<BR>番号";
        } else { // 学校
            $senkou_kind = 0;
            $arg["BANGO"] = "学校<BR>コード";
        }

        $db = Query::dbCheckOut();

        // 校内選考登録データ一覧
        $i = 0;
        if (isset($year) && isset($school_sort)) {
            $result = $db->query(knje311Query::sqlList($year, $senkou_kind, $school_sort));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                // 選考結果
                $objForm->ae( array("type"          => "text",
                                    "name"          => "SW_SENKOU_FIN",
                                    "size"          => 5,
                                    "maxlength"     => 1,
                                    "multiple"      => 1,
                                    "extrahtml"     => "STYLE=\"WIDTH:100%\" WIDTH=\"100%\" STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value); myCheckText(this)\"; ",
                                    "value"         => $row["SENKOU_FIN"]));
                $row["SW_SENKOU_FIN"] = $objForm->ge("SW_SENKOU_FIN");
                // 連番(SEQ)
                $objForm->ae(array("type"           => "checkbox",
                                   "name"           => "SW_SEQ",
                                   "checked"        => "",
                                   "value"          => $row["SEQ"],
                                   "extrahtml"      => "STYLE=\"display:none\" ",
                                   "multiple"       => "1" ));
                $row["SW_SEQ"] = $objForm->ge("SW_SEQ");
                // No.
                $i++;
                $row["KENSU"] = $i;
                $arg["data"][] = $row;
            }
            $result->free();
        }
        Query::dbCheckIn($db);

        // ===ボタン===
        $dis_btn = ($i == 0) ? "disabled" : "";
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_csv",
                            "value"     => "ＣＳＶ出力",
                            "extrahtml" => "onclick=\"return myBtnSubmit('csv');\" " .$dis_btn ) );
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_update",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return myBtnSubmit('update');\" " .$dis_btn ) );
        $objForm->ae( array("type"      => "reset",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return myBtnReset('knje311');\"" ) );
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );
        $arg["btn_update"]  = $objForm->ge("btn_update");
        $arg["btn_csv"]     = $objForm->ge("btn_csv");
        $arg["btn_reset"]   = $objForm->ge("btn_reset");
        $arg["btn_end"]     = $objForm->ge("btn_end");

        // hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DATA_SEQ"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DATA_SENKOU_FIN"
                            ) );

        // 権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje311Form1.html", $arg); 
    }
}
?>
