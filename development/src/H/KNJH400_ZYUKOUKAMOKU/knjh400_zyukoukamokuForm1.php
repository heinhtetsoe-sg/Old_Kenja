<?php

require_once('for_php7.php');

class knjh400_zyukoukamokuForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_zyukoukamokuindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg['SCHREGNO'] = $model->schregno;
        $arg['NAME'] = $db->getOne(knjh400_zyukoukamokuQuery::getName($model));
        
        $opt     = array();
        $result = $db->query(knjh400_zyukoukamokuQuery::getYearGrade($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $arg["YEARGRADE"] = knjCreateCombo($objForm, "YEARGRADE", $model->yearGrade, $opt, "onchange=\"btn_submit('edit');\"" , '');

        $result = $db->query(knjh400_zyukoukamokuQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg['data'][] = $row;
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        Query::dbCheckIn($db);

        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh400_zyukoukamokuForm1.html", $arg);
    }
}
