<?php

require_once('for_php7.php');

class knjb104bForm1
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb104bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg['YEAR'] = $model->year;

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104bQuery::getSemester($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['SEMESTER'] = knjCreateCombo($objForm, 'SEMESTER', $model->semester, $opt, ' onchange="btn_submit(\'edit\');"', '');

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104bQuery::getGrade($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['GRADE'] = knjCreateCombo($objForm, 'GRADE', $model->grade, $opt, ' onchange="btn_submit(\'edit\');"', '');

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104bQuery::getKousa($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['KOUSA'] = knjCreateCombo($objForm, 'KOUSA', $model->kousa, $opt, ' onchange="btn_submit(\'edit\');"', '');

        $ext = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $ext);

        $ext = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $ext);

        $ext = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $ext);


        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104bQuery::getFacility($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }

        $chaircds = array();
        $counter = 0;
        $result = $db->query(knjb104bQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $value = (!isset($model->warning)) ? $row['FACCD'] : $model->data2['FACILITY_'.$counter];
            $row['FACILITY'] = knjCreateCombo($objForm, 'FACILITY_'.$counter, $value, $opt, "onchange=\"changeFacility(this,'".$row['CHAIRCD']."')\"",'', '');
            $arg['data'][] = $row;
            $chaircds[] = $row['CHAIRCD'];
            $counter++;
        }
        $result->free();

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "chaircds", implode(',', $chaircds));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb104bForm1.html", $arg);
    }
}
