<?php
class knjo131Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjo131index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();
        
        //在学生検索ボタン
        $extra = "onclick=\"wopen('../../X/KNJXSEARCH2/index.php?PATH=/O/KNJO131/knjo131index.php&cmd=&target=KNJO131','search',0,0,700,500);\"";
        $arg["button"]["btn_zaigaku"] = knjCreateBtn($objForm, "btn_zaigaku", "在校生検索", $extra);

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

        //View::toHTML($model, "knjo131Form1.html", $arg);
        View::toHTML6($model, "knjo131Form1.html", $arg,"jquery-1.11.0.min.js");
    }
}


?>
