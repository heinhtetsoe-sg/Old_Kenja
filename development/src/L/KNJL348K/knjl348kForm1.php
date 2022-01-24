<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjl348kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl348kindex.php", "", "main");
        
        //年度学期表示
        $arg["YEAR"] = $model->examyear;

        //出力対象ラジオ---1:前後期重複受験者名簿,2:SS(標準偏差値)相関データ
        $opt_out2[0]=1;
        $opt_out2[1]=2;

        if (!$model->output) $model->output = 1;

        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => $model->output,
                            "multiple"   => $opt_out,
                            "extrahtml"  => "onclick=\"return btn_submit('main');\"" ) );

        $arg["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["OUTPUT2"] = $objForm->ge("OUTPUT",2);

        //試験区分を作成する
        $opt_testdiv = array();
        $testcnt = 0;
        $db = Query::dbCheckOut();

        $result = $db->query(knjl348kQuery::GetTestdiv($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_testdiv[] = array("label" => $row["NAME1"],
                                   "value" => $row["NAMECD2"]);
            $testcnt++;
        }
        if ($testcnt == 0){
            $opt_testdiv[$testcnt] = array("label" => "　　",
                                           "value" => "99");
        }

        if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

        $result->free();
        Query::dbCheckIn($db);
        $dis_testdiv = ($model->output == 1) ? "disabled" : "";
        $objForm->ae( array("type"      => "select",
                            "name"      => "TESTDIV",
                            "size"      => 1,
                            "value"     => $model->testdiv,
                            "extrahtml" => $dis_testdiv,
                            "options"   => $opt_testdiv ) );

        $arg["TESTDIV"] = $objForm->ge("TESTDIV");


        //実行
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('exec');\"" ));
        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl348kForm1.html", $arg);
    }
}
?>
