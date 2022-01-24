<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knje320Form1.php 56587 2017-10-22 12:54:51Z maeshiro $

class knje320Form1 {
    function main(&$model) {

        $objForm = new form;
        $arg["start"] = $objForm->get_start("knje320Form1", "POST", "knje320index.php", "", "knje320Form1");

        // 配列を取得
        $opt_year           = knje320Query::getYear();
        $opt_school_sort    = knje320Query::getNameMst($model->control["年度"], "E001", "off");
        $opt_senkou_kai     = knje320Query::getNameMst($model->control["年度"], "E003", "on");

        //-----
        // 年度コンボ
        $objForm->ae( array("type"       => "select",
                            "name"       => "YEAR",
                            "size"       => "1",
                            "value"      => $model->control["年度"],
                            "options"    => $opt_year ) );
        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        // 学期
        $arg["data"]["GAKKI"] = $model->control["学期名"][$model->control["学期"]];

        //-----
        // 種別（学校・会社）コンボ
        $objForm->ae( array("type"      => "select",
                            "name"      => "SCHOOL_SORT",
                            "size"      => "1",
                            "value"     => $model->field["SCHOOL_SORT"],
                            "options"   => $opt_school_sort) );
        $arg["data"]["SCHOOL_SORT"] = $objForm->ge("SCHOOL_SORT");

        //-----
        // 選考会コンボ
        $objForm->ae( array("type"      => "select",
                            "name"      => "SENKOU_KAI",
                            "size"      => "1",
                            "value"     => $model->field["SENKOU_KAI"],
                            "options"   => $opt_senkou_kai) );
        $arg["data"]["SENKOU_KAI"] = $objForm->ge("SENKOU_KAI");

        //===ボタン===
        // 印刷ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );
        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        // 終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //===hidden===
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => PROGRAMID
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => $model->control["学期"]
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knje320Form1.html", $arg); 
    }
}
?>
