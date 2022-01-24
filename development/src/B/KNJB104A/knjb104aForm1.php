<?php

require_once('for_php7.php');

class knjb104aForm1
{
    public $dataRow = array(); //表示用一行分データをセット
    
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjb104aindex.php", "", "edit");

        $db = Query::dbCheckOut();
        
        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104aQuery::getYear($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }

        $arg['YEAR'] = knjCreateCombo($objForm, 'YEAR', $model->year, $opt, ' onchange="btn_submit(\'list\');"', '');
        $arg['YEAR2'] = knjCreateCombo($objForm, 'YEAR2', $model->year2, $opt, ' onchange="btn_submit(\'list\');"', '');

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104aQuery::getSemester($model, $model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['SEMESTER'] = knjCreateCombo($objForm, 'SEMESTER', $model->semester, $opt, ' onchange="btn_submit(\'list\');"', '');

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104aQuery::getSemester($model, $model->year2));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['SEMESTER2'] = knjCreateCombo($objForm, 'SEMESTER2', $model->semester2, $opt, ' onchange="btn_submit(\'list\');"', '');

        $extra =" onclick=\"btn_submit('copy');\"";
        $arg["copy_btn"] = knjCreateBtn($objForm, "copy_btn", "左の学期のデータをコピー", $extra);

        $rowspan = 1;
        $idx = 0;
        $subdata = array();
        $result = $db->query(knjb104aQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['link'] = "knjb104aindex.php?cmd=edit&exp_year={$model->year}&exp_semester={$model->semester}&exp_faccd={$row['FACCD']}&exp_chaircd={$row['CHAIRCD']}&exp_facname={$row['FACILITYNAME']}&exp_chairname={$row['CHAIRNAME']}";
            $row['COLOR'] = ($row['FLAG'] == '1') ? '#FFFFFF' : 'FFCCCC';
            if ($faccd == $row['FACCD']) {
                $subdata[] = $row;
                $rowspan++;
            } else {
                $faccd = $row['FACCD'];
                if ($idx != 0) {
                    $arg['data'][$idx - 1]['rowspan'] = $rowspan;
                    $arg['data'][$idx - 1]['subdata'] = $subdata;
                }
                $rowspan = 1;
                $subdata = array();
                $arg['data'][] = $row;
                $idx++;
            }
        }
        if ($idx != 0) {
            $arg['data'][$idx - 1]['rowspan'] = $rowspan;
            $arg['data'][$idx - 1]['subdata'] = $subdata;
        }

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb104aForm1.html", $arg);
    }
}
