<?php

require_once('for_php7.php');

class knjxsearch2Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"] = $objForm->get_start("knjxsearch2Form1", "POST", "index.php", "", "knjxsearch2Form1");

        $db     = Query::dbCheckOut();

        //検索ボタン
        $extra = "onclick=\"wopen('index.php?cmd=search_view','knjxSearch2',0,0,450,250);\"";
        $arg["SEARCH_BTN"] = knjCreateBtn($objForm, "SEARCH_BTN", "検索条件入力", $extra);

        //検索結果表示
        if ($model->cmd == "search") {
            $result = $db->query(knjxsearch2Query::SearchStudent($model->search_fields, $model));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i =0;
            list($path, $cmd) = explode("?cmd=", $model->path);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                 array_walk($row, "htmlspecialchars_array");
                 $row["GRADUATE_CLASS"] = $row["HR_NAME"].$row["ATTENDNO"]."番";
                 $row["IMAGE"]    = $image[($row["SEXNUM"]-1)];
                 $row["BIRTHDAY"] = str_replace("-","/",$row["BIRTHDAY"]);
                 $arg["data"][]   = $row;
                 $i++;
            }
            $arg["RESULT"] = "結果　".$i."名";
            $result->free();
            if ($i == 0) {
                $arg["search_result"] = "SearchResult();";
            }
        }

        Query::dbCheckIn($db);

        //hidden(検索条件値を格納する)
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "GRADUATE_CLASS");
        knjCreateHidden($objForm, "KANJI");
        knjCreateHidden($objForm, "KANA");
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        knjCreateHidden($objForm, "SEMESTER", $model->control["学期"]);

        $arg["finish"]  = $objForm->get_finish();

        if(VARS::post("cmd")==""){
            $arg["reload"] ="wopen('index.php?cmd=search_view','knjxSearch2',0,0,450,250);";
        }
        View::toHTML($model, "knjxsearch2Form1.html", $arg);
    }
}
?>
