<?php

require_once('for_php7.php');


class knjl230cForm1
{
    function main(&$model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjl230cindex.php", "", "main");

        //年度
        $arg["YEAR"] = $model->examyear;

        //入試制度コンボ
        $query = knjl230cQuery::getApplicantdiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //プレテスト区分
        $query = knjl230cQuery::getPreTestdiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PRE_TESTDIV", $model->pre_testdiv, $extra, 1, "");

        //DB切断
        Query::dbCheckIn($db);

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('exec');\"" ));
        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl230cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $value_flg = false;
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
