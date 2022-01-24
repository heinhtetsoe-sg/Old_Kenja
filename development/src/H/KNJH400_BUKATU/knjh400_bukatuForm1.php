<?php

require_once('for_php7.php');

class knjh400_bukatuForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_bukatuindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg['SCHREGNO'] = $model->schregno;
        $arg['NAME'] = $db->getOne(knjh400_bukatuQuery::getName($model));

        $result = $db->query(knjh400_bukatuQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg['data'][] = $row;
        }
        $result->free();

        $result = $db->query(knjh400_bukatuQuery::selectQuery2($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["DIV_SHOW"] = ($row["DIV"] == "1") ? '1:個人' : '2:団体';
            $arg['data2'][] = $row;
        }
        $result->free();

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh400_bukatuForm1.html", $arg);
    }
}
