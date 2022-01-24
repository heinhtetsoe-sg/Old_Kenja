<?php

require_once('for_php7.php');

class knjl011vForm3
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl011vindex.php", "", "edit");
        $db     = Query::dbCheckOut();

        $arg['header']['EXAMNO'] = $model->field["EXAMNO"];
        $arg['header']['EXAM_SCHOOL_KIND'] = makeCmb($objForm, $arg, $db, knjl011vQuery::getExamSchoolKind($model), 'EXAM_SCHOOL_KIND2', $model->examSchoolKind2, ' onclick="setSelected(this)" onchange="submitCheck(this)"', '1', 'BLANK');
        $arg['header']['APPLICANT_DIV'] = makeCmb($objForm, $arg, $db, knjl011vQuery::getApplicantDiv($model, $model->examSchoolKind2), 'APPLICANT_DIV2', $model->applicantDiv2, ' onclick="setSelected(this)" onchange="submitCheck(this)"', '1', 'BLANK');

        $param = '';
        $sep = '';
        $counter = 0;
        if ($model->examSchoolKind2 != '' && $model->applicantDiv2 != '') {
            $result = $db->query(knjl011vQuery::selectQuery2($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row['RECEPTNO'] = knjCreateTextBox($objForm, $row['RECEPTNO'], 'RECEPTNO-'.$counter, 4, 4, "onblur=\"this.value=toInteger(this.value)\"");
                $arg['data'][] = $row;
                $param .= $sep . $row['COURSE_DIV'] . ':' . $row['FREQUENCY'];
                $sep = ',';
                $counter++;
            }
        }

        $disabled = ($model->examSchoolKind2 != '' && $model->applicantDiv2 != '') ? '' : " disabled='disabled'";

        //更新ボタン
        $extra = "onclick=\"return btn_submit('receptUpdate');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);

        //終了ボタン
        $extra = "onclick=\"return btn_submit('move2');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "param", $param);
        Query::dbCheckIn($db);

        if ($model->cmd == "add" || $model->cmd == "delete") {
            $arg["jscript"] = "window.open('knjl011vindex.php?cmd=search','left_frame')";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011vForm3.html", $arg);
    }
}
//コンボ作成
function MakeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => '', 'value' => '');
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
