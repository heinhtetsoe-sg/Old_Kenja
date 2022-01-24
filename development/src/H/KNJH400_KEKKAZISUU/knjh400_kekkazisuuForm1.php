<?php

require_once('for_php7.php');

class knjh400_kekkazisuuForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_kekkazisuuindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg['SCHREGNO'] = $model->schregno;
        $arg['NAME'] = $db->getOne(knjh400_kekkazisuuQuery::getName($model));

        $opt     = array();
        $result = $db->query(knjh400_kekkazisuuQuery::getYearGrade($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $arg["YEARGRADE"] = knjCreateCombo($objForm, "YEARGRADE", $model->yearGrade, $opt, "onchange=\"btn_submit('edit');\"", '');

        $result = $db->query(knjh400_kekkazisuuQuery::getSchoolMst($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $offdays = $row['SUB_OFFDAYS'];
            $absent = $row['SUB_ABSENT'];
            $suspend = $row['SUB_SUSPEND'];
            $mourning = $row['SUB_MOURNING'];
            $virus = $row['SUB_VIRUS'];
        }
        $result->free();

        $result = $db->query(knjh400_kekkazisuuQuery::selectQuery($model, $offdays, $absent, $suspend, $mourning, $virus));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg['data'][] = $row;
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        Query::dbCheckIn($db);

        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjh400_kekkazisuuForm1.html", $arg);
    }
}
