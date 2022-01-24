<?php

require_once('for_php7.php');

class knjh400_sikakuForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_sikakuindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg['SCHREGNO'] = $model->schregno;
        $arg['NAME'] = $db->getOne(knjh400_sikakuQuery::getName($model));

        $result = $db->query(knjh400_sikakuQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['REGDDATE'] = str_replace('-', '/', $row['REGDDATE']);
            $arg['data'][] = $row;
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh400_sikakuForm1.html", $arg);
    }
}
