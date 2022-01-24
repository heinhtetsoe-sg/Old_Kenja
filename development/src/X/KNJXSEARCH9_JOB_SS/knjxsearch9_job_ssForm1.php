<?php

require_once('for_php7.php');

class knjxsearch9_job_ssForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("knjxsearch9_job_ssForm1", "POST", "index.php", "", "knjxsearch9_job_ssForm1");
        $db           = Query::dbCheckOut();

        //検索ボタン
        $extra = "onclick=\"wopen('index.php?cmd=search_view','knjxsearch9_job_ss',0,0,500,250);\"";
        $arg["SEARCH_BTN"] = knjCreateBtn($objForm, "SEARCH_BTN", "検索条件入力", $extra);

        //検索結果表示
        if ($model->cmd == "search") {
            $result = $db->query(knjxsearch9_job_ssQuery::searchStudent($model->search_fields));
            $i = 0;
            list($path, $cmd) = explode("?cmd=", $model->path);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
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
        knjCreateHidden($objForm, "COMPANY_NAME");
        knjCreateHidden($objForm, "SHUSHOKU_ADDR");
        knjCreateHidden($objForm, "programid", $model->programid);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::post("cmd") == "") {
            $arg["reload"] ="wopen('index.php?cmd=search_view','knjxsearch9_job_ss',0,0,500,250);";
        }
        View::toHTML($model, "knjxsearch9_job_ssForm1.html", $arg);
    }
}
