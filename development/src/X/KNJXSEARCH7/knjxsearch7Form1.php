<?php

require_once('for_php7.php');

class knjxsearch7Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"] = $objForm->get_start("knjxsearch7Form1", "POST", "index.php", "", "knjxsearch7Form1");

        $db = Query::dbCheckOut();

        //検索ボタン
        $extra = "onclick=\"wopen('index.php?cmd=search_view','knjxsearch7',0,0,450,250);\"";
        $arg["SEARCH_BTN"] = knjCreateBtn($objForm, "SEARCH_BTN", "検索条件入力", $extra);

        //検索結果表示
        if ($model->cmd == "search") {
            $result = $db->query(knjxsearch7Query::SearchStudent($model->control["年度"],$model->control["学期"],$model->search_fields, $model));
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
        knjCreateHidden($objForm, "GRADUATE_YEAR");
        knjCreateHidden($objForm, "GRADUATE_CLASS");
        knjCreateHidden($objForm, "KANJI");
        knjCreateHidden($objForm, "KANA");

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::post("cmd") == "") {
            $arg["reload"] ="wopen('index.php?cmd=search_view','knjxsearch7',0,0,450,250);";
        }
        View::toHTML($model, "knjxsearch7Form1.html", $arg);
    }
}
?>
