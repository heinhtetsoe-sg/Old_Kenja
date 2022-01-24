<?php

require_once('for_php7.php');

class knjb104cForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        $db = Query::dbCheckOut();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjb104cindex.php", "", "edit");

        $arg["YEAR"] = $model->exp_year;
        $arg["FACNAME"] = $model->exp_facname;
        $arg["CHAIRNAME"] = $model->exp_chairname;
        $arg["SUBCLASSNAME"] = $model->exp_subclassnameDisp;
        $arg["HR_NAME"] = $model->exp_hrnameDisp;

        $result = $db->query(knjb104cQuery::getSemester($model, $model->exp_year));
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
            $row = $db->getRow(knjb104cQuery::selectQuery3($model), DB_FETCHMODE_ASSOC);
            if ($row) {
                $model->retu = $row['COLUMNS'];
                $model->gyou = $row['ROWS'];
            }
        }

        $arg['RETU'] = knjCreateCombo($objForm, 'RETU', $model->retu, $opt, '', '', '');
        $arg['GYOU'] = knjCreateCombo($objForm, 'GYOU', $model->gyou, $opt, '', '', '');
        $arg['colspan'] = $model->retu;

        $notuseList = $notuseListHidden = array();
        $result = $db->query(knjb104cQuery::selectQuery4($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $notuseList[$row['ROWS']][$row['COLUMNS']] = true;
            $notuseListHidden[] = $row['ROWS'].'*'.$row['COLUMNS'];
        }
        $seitoList = array();
        $result = $db->query(knjb104cQuery::selectQuery5($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seitoList[$row['ROWS']][$row['COLUMNS']] = $row;
        }
        $dataCnt = 0;
        for ($i = 1; $i <= $model->gyou; $i++) {
            $retuCnt = 0;
            for ($j = 1; $j <= $model->retu; $j++) {
                $seki = $notuseList[$i][$j] ? '' : knjCreateTextBox($objForm, $seitoList[$i][$j]['SEAT_NO'], "SEAT_NO_".$i.'_'.$j, 2, 2, ' onblur="this.value=toInteger(this.value)" style="width:25px" id="SEAT_NO_'.$i.'_'.$j.'"');
                $id = 'MAIN_'.$i.'_'.$j;
                $schregno = $seitoList[$i][$j]['SCHREGNO'];
                $seito = ($schregno)? ' '.$seitoList[$i][$j]['ATTENDNO'].'<br> '.$seitoList[$i][$j]['NAME'] : '';
                if ($notuseList[$i][$j]) {
                    $text  = "<div style=\"display:inline-block;\"><div style=\"margin:10px;border:#000000 solid 1px;width:100px;height:50px;text-align:left;background-color:#CCCCCC\"";
                    $text .= " id=\"{$id}\" data-notuse=\"{$notuseList[$i][$j]}\"></div></div>";
                } else {
                    $text  = "<div style=\"display:inline-block;\"><div style=\"margin:10px;border:#000000 solid 1px;width:100px;height:50px;text-align:left;background-color:#FFFFFF\"";
                    $text .= " id=\"{$id}\" draggable=\"true\" ondragstart=\"f_dragstart(event)\" ondragover=\"f_dragover(event)\" ondrop=\"f_drop(event)\" onclick=\"f_click(event)\" data-schregno=\"{$schregno}\" data-notuse=\"{$notuseList[$i][$j]}\">{$seki}{$seito}</div></div>";
                }
                $arg['data'][$dataCnt]['RETU'][$retuCnt]['BOX'] = $text;
                $retuCnt++;
            }
            $dataCnt++;
        }

        $disabled = ($model->exp_year == '') ? 'disabled' : '';

        $extra = "onclick=\"wariate();\"".$disabled;
        $arg["button"]["btn_wariate"] = knjCreateBtn($objForm, "btn_wariate", "自動割当実行", $extra);

        $extra = "onclick=\"return btn_submit('gyouretu');\"".$disabled;
        $arg["button"]["btn_gyouretu"] = knjCreateBtn($objForm, "btn_gyouretu", "行列変更", $extra);

        $extra = "onclick=\"return btn_submit('update');\"".$disabled;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('delete');\"".$disabled;
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);

        $extra = "onclick=\"return btn_submit('clear');\"".$disabled;
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "setData");
        knjCreateHidden($objForm, 'retu', $model->retu);
        knjCreateHidden($objForm, 'gyou', $model->gyou);

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjb104cindex.php?cmd=init','left_frame');";
        }
        if (VARS::get("exp_year") != "") {
            $arg["jscript"] = "window.open('knjb104cindex.php?cmd=list2','left_frame');";
        }

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb104cForm2.html", $arg);
    }
}
