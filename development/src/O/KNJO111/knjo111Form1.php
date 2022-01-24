<?php
class knjo111Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjo111index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //テンプレート
        $arg["call"] = importXml::htmlcreate($objForm,$db,$model);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]    = $objForm->get_finish();

        //View::toHTML($model, "knjo111Form1.html", $arg);
        View::toHTML6($model, "knjo111Form1.html", $arg,"jquery-1.11.0.min.js");
    }
}


?>
