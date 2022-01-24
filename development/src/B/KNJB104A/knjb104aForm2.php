<?php

require_once('for_php7.php');

class knjb104aForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        $db = Query::dbCheckOut();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjb104aindex.php", "", "edit");

        $arg["YEAR"] = $model->exp_year;
        $arg["FACNAME"] = $model->exp_facname;
        $arg["CHAIRNAME"] = $model->exp_chairname;

        $result = $db->query(knjb104aQuery::getSemester($model, $model->exp_year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row['VALUE'] == $model->exp_semester) {
                $arg["SEMESTERNAME"]    = $row['LABEL'];
                break;
            }
        }

        $opt = array();
        for ($i = 1; $i <= 7; $i++) {
            $opt[] = array('label' => $i, 'value' => $i);
        }
        if ($model->cmd != 'gyouretu') {
            $row = $db->getRow(knjb104aQuery::selectQuery2($model), DB_FETCHMODE_ASSOC);
            if ($row) {
                $model->retu = $row['COLUMNS'];
                $model->gyou = $row['ROWS'];
            }
        }

        $arg['RETU'] = knjCreateCombo($objForm, 'RETU', $model->retu, $opt, '', '', '');
        $arg['GYOU'] = knjCreateCombo($objForm, 'GYOU', $model->gyou, $opt, '', '', '');
        $arg['colspan'] = $model->retu;

        $notuseList = $notuseListHidden = array();
        if ($model->cmd != 'gyouretu') {
            $result = $db->query(knjb104aQuery::selectQuery3($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $notuseList[$row['ROWS']][$row['COLUMNS']] = true;
                $notuseListHidden[] = $row['ROWS'].'*'.$row['COLUMNS'];
            }
        }
        $dataCnt = 0;
        for ($i = 1; $i <= $model->gyou; $i++) {
            $retuCnt = 0;
            for ($j = 1; $j <= $model->retu; $j++) {
                $backgrondColor = 'background-color:' . ($notuseList[$i][$j] ? "#CCCCCC" : '#FFFFFF');
                $arg['data'][$dataCnt]['RETU'][$retuCnt]['BOX'] = "<div style=\"display:inline-block;\"><div style=\"margin:10px;border:#000000 solid 1px;width:50px;height:30px;{$backgrondColor}\" onclick=\"boxClick(this, '{$i}','{$j}')\"></div></div>";
                $retuCnt++;
            }
            $dataCnt++;
        }

        $disabled = ($model->exp_year == '') ? 'disabled' : '';

        $extra = "onclick=\"return btn_submit('gyouretu');\"".$disabled;
        $arg["button"]["btn_gyouretu"] = knjCreateBtn($objForm, "btn_gyouretu", "行列変更", $extra);

        $extra = "onclick=\"return btn_submit('update');\"".$disabled;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('clear');\"".$disabled;
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "notuse", implode(',', $notuseListHidden));
        knjCreateHidden($objForm, 'exp_year', $model->exp_year);
        knjCreateHidden($objForm, 'exp_semester', $model->exp_semester);
        knjCreateHidden($objForm, 'exp_faccd', $model->exp_faccd);
        knjCreateHidden($objForm, 'exp_chaircd', $model->exp_chaircd);

        if (VARS::post("cmd") == "update") {
            $arg["jscript"] = "window.open('knjb104aindex.php?cmd=init','left_frame');";
        }

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb104aForm2.html", $arg);
    }
}
