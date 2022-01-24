<?php

require_once('for_php7.php');


class knje353Form1 {
    function main(&$model) {

        $objForm = new form;
        $arg["start"] = $objForm->get_start("knje353Form1", "POST", "knje353index.php", "", "knje353Form1");

        // 配列を取得
        $opt_year           = knje353Query::getYear();
        $opt_school_sort    = knje353Query::getNameMst($model->control["年度"], "E001", "off");
        $opt_senkou_kai     = knje353Query::getNameMst($model->control["年度"], "E003", "on");

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


        //出力順ラジオボタン 1:成績(降順) 2:成績(昇順) 3:出席番号
        $opt_div = array(1, 2);
        $model->field["OUTPUT_DIV"] = ($model->field["OUTPUT_DIV"] == "") ? "1" : $model->field["OUTPUT_DIV"];
        $extra = "";
        createRadio($objForm, $arg, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt_rank, get_count($opt_div));

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
        View::toHTML($model, "knje353Form1.html", $arg); 
    }
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    $objForm->ae( array("type"      => "radio",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));
    for ($i = 1; $i <= $count; $i++) {
        $arg["data"][$name.$i] = $objForm->ge($name, $i);
    }
}

?>
