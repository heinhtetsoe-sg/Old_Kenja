<?php

require_once('for_php7.php');

class knjb104cForm1
{
    public $dataRow = array(); //表示用一行分データをセット

    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjb104cindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg['YEAR'] = $model->year;

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104cQuery::getSemester($model, $model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['SEMESTER'] = knjCreateCombo($objForm, 'SEMESTER', $model->semester, $opt, ' onchange="btn_submit(\'list\');"', '');

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104cQuery::getGrade($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['GRADE'] = knjCreateCombo($objForm, 'GRADE', $model->grade, $opt, ' onchange="btn_submit(\'list\');"', '');

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104cQuery::getKousa($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['KOUSA'] = knjCreateCombo($objForm, 'KOUSA', $model->kousa, $opt, ' onchange="btn_submit(\'list\');"', '');

        $arg["EXECUTEDATE"] = View::popUpCalendar2($objForm, "EXECUTEDATE", str_replace("-", "/", $model->executeDate), '', 'btn_submit(\'list\');');

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjb104cQuery::getPeriod($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['PERIODCD'] = knjCreateCombo($objForm, 'PERIODCD', $model->periodCd, $opt, ' onchange="btn_submit(\'list\');"', '');

        $extra =" onclick=\"btn_submit('copy');\"";
        $arg["copy_btn"] = knjCreateBtn($objForm, "copy_btn", "左の学期のデータをコピー", $extra);

        $result = $db->query(knjb104cQuery::selectQuery1($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            list($temp, $damy) = explode(':', $row['HR_NAME']);
            $grade = substr($temp, 0, 2);
            $hrclass = substr($temp, 2, 3);
            $row['link']  = "knjb104cindex.php?cmd=edit&exp_year={$row['YEAR']}&exp_semester={$row['SEMESTER']}&exp_grade={$grade}&exp_hrclass={$hrclass}&exp_executedate={$row['EXECUTEDATE']}&exp_faccd={$row['FACCD']}&exp_chaircd={$row['CHAIRCD']}";
            $row['link'] .= "&exp_facname={$row['FACILITYNAME']}&exp_chairname={$row['CHAIRNAME']}&exp_subclassname={$row['SUBCLASSNAME']}&exp_hrname={$row['HR_NAME']}&exp_periodcd={$row['PERIODCD']}";
            $row['link'] .= "&exp_testkindcd={$row['TESTKINDCD']}&exp_testitemcd={$row['TESTITEMCD']}";
            if ($row['FLAG'] == '1') {
                $row['link'] = "<a href=\"{$row['link']}\" target=\"right_frame\">{$row['CHAIRNAME']}</a>";
                $row['COLOR'] = '#FFFFFF';
            } else {
                $row['link'] = $row['CHAIRNAME'];
                $row['COLOR'] = '#FFCCCC';
            }
            $arg['data1'][] = $row;
        }

        $opt = array();
        $hrNameList = explode('<br>', $model->exp_hrname);
        for ($i = 0; $i < get_count($hrNameList); $i++) {
            list($value, $label) = explode(':', $hrNameList[$i]);
            $opt[] = array('label' => $label, 'value' => $value);
        }
        $arg['GRADE_HR_CLASS'] = knjCreateCombo($objForm, 'GRADE_HR_CLASS', $model->hrgradeclass, $opt, ' onchange="btn_submit(\'list\');"', '');

        if ($model->exp_year) {
            $idx = 0;
            $result = $db->query(knjb104cQuery::selectQuery2($db, $model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row['FLAG'] == '1') {
                    $row['COLOR'] = '#FFCCCC';
                } else {
                    $row['COLOR'] = '#FFFFFF';
                }
                $row['ID'] = 'LEFT_'.$idx;
                $arg['data2'][] = $row;
                $idx++;
            }
        }
        //hidden
        knjCreateHidden($objForm, "cmd", "list");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb104cForm1.html", $arg);
    }
}
