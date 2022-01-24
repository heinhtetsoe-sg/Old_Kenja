<?php

require_once('for_php7.php');

class knjxSearch_StudentForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"] = $objForm->get_start("knjxSearch_StudentForm1", "POST", "knjxSearch_Studentindex.php", "", "knjxSearch_StudentForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //検索ボタン
        $extra = "onclick=\"wopen('knjxSearch_Studentindex.php?cmd=search_view','knjxSearch_Student',0,0,450,250);\"";
        $arg["SEARCH_BTN"] = knjCreateBtn($objForm, "SEARCH_BTN", "検索条件入力", $extra);

        //検索結果表示
        if ($model->cmd == "search") {
            $result = $db->query(knjxSearch_StudentQuery::SearchStudent($model->search_fields));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i =0;
            list($path, $cmd) = explode("?cmd=", $model->path);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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

        //hidden(検索条件値を格納する)
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO");
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "ATTENDNO");
        knjCreateHidden($objForm, "COURSEMAJOR");
        knjCreateHidden($objForm, "COURSECODE");
        knjCreateHidden($objForm, "NAME");
        knjCreateHidden($objForm, "NAMESHOW");
        knjCreateHidden($objForm, "KANA");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
    
        if(VARS::post("cmd")==""){
            $arg["reload"] ="wopen('knjxSearch_Studentindex.php?cmd=search_view','knjxSearch_Student',0,0,450,250);";
        }
        View::toHTML($model, "knjxSearch_StudentForm1.html", $arg);
    }
}
?>
