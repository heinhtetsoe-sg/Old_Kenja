<?php

require_once('for_php7.php');

class knjl012vForm2
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl012vindex.php", "", "edit");
        $db     = Query::dbCheckOut();

        if ($model->examSchoolKind == 'J') {
            $arg['EXAM_SCHOOL_KIND_J'] = '1';
        }

        if ($model->cmd == 'delete') {
            $row = array();
        } elseif (!isset($model->warning)) {
            if ($model->examSchoolKind == 'J') {
                $row = $db->getRow(knjl012vQuery::selectQuery($model, '01', '02', '03', '04', '05', '06'), DB_FETCHMODE_ASSOC);
            } else {
                $row = $db->getRow(knjl012vQuery::selectQuery($model, '01', '02', '03'), DB_FETCHMODE_ASSOC);
            }
            if (!isset($row)) {
                $row = array();
            }
            $row2 = $db->getRow(knjl012vQuery::selectQueryRemark($model), DB_FETCHMODE_ASSOC);
            if (isset($row2)) {
                $row = array_merge($row, $row2);
            }
            $row['EXAMNO'] = $model->field['EXAMNO'];
        } else {
            $row = $model->field;
        }

        $row['JAPANESE1'] = knjCreateTextBox($objForm, $row['JAPANESE1'], 'JAPANESE1', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1)"');
        $row['MATH1'] = knjCreateTextBox($objForm, $row['MATH1'], 'MATH1', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1)"');
        $row['SOCIETY1'] = knjCreateTextBox($objForm, $row['SOCIETY1'], 'SOCIETY1', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1)"');
        $row['SCIENCE1'] = knjCreateTextBox($objForm, $row['SCIENCE1'], 'SCIENCE1', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1)"');
        $row['ENGLISH1'] = knjCreateTextBox($objForm, $row['ENGLISH1'], 'ENGLISH1', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1)"');
        $row['HEALTH_PHYSICAL1'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL1'], 'HEALTH_PHYSICAL1', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1)"');
        $row['TECH_HOME1'] = knjCreateTextBox($objForm, $row['TECH_HOME1'], 'TECH_HOME1', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1)"');
        $row['MUSIC1'] = knjCreateTextBox($objForm, $row['MUSIC1'], 'MUSIC1', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1)"');
        $row['ART1'] = knjCreateTextBox($objForm, $row['ART1'], 'ART1', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1)"');
        $row['TOTAL31'] = knjCreateTextBox($objForm, $row['TOTAL31'], 'TOTAL31', 3, 3, 'readonly="readonly"');
        $row['TOTAL51'] = knjCreateTextBox($objForm, $row['TOTAL51'], 'TOTAL51', 3, 3, 'readonly="readonly"');
        $row['TOTAL91'] = knjCreateTextBox($objForm, $row['TOTAL91'], 'TOTAL91', 3, 3, 'readonly="readonly"');
        $row['JAPANESE2'] = knjCreateTextBox($objForm, $row['JAPANESE2'], 'JAPANESE2', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2)"');
        $row['MATH2'] = knjCreateTextBox($objForm, $row['MATH2'], 'MATH2', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2)"');
        $row['SOCIETY2'] = knjCreateTextBox($objForm, $row['SOCIETY2'], 'SOCIETY2', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2)"');
        $row['SCIENCE2'] = knjCreateTextBox($objForm, $row['SCIENCE2'], 'SCIENCE2', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2)"');
        $row['ENGLISH2'] = knjCreateTextBox($objForm, $row['ENGLISH2'], 'ENGLISH2', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2)"');
        $row['HEALTH_PHYSICAL2'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL2'], 'HEALTH_PHYSICAL2', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2)"');
        $row['TECH_HOME2'] = knjCreateTextBox($objForm, $row['TECH_HOME2'], 'TECH_HOME2', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2)"');
        $row['MUSIC2'] = knjCreateTextBox($objForm, $row['MUSIC2'], 'MUSIC2', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2)"');
        $row['ART2'] = knjCreateTextBox($objForm, $row['ART2'], 'ART2', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2)"');
        $row['TOTAL32'] = knjCreateTextBox($objForm, $row['TOTAL32'], 'TOTAL32', 3, 3, 'readonly="readonly"');
        $row['TOTAL52'] = knjCreateTextBox($objForm, $row['TOTAL52'], 'TOTAL52', 3, 3, 'readonly="readonly"');
        $row['TOTAL92'] = knjCreateTextBox($objForm, $row['TOTAL92'], 'TOTAL92', 3, 3, 'readonly="readonly"');
        $row['JAPANESE3'] = knjCreateTextBox($objForm, $row['JAPANESE3'], 'JAPANESE3', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3)"');
        $row['MATH3'] = knjCreateTextBox($objForm, $row['MATH3'], 'MATH3', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3)"');
        $row['SOCIETY3'] = knjCreateTextBox($objForm, $row['SOCIETY3'], 'SOCIETY3', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3)"');
        $row['SCIENCE3'] = knjCreateTextBox($objForm, $row['SCIENCE3'], 'SCIENCE3', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3)"');
        $row['ENGLISH3'] = knjCreateTextBox($objForm, $row['ENGLISH3'], 'ENGLISH3', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3)"');
        $row['HEALTH_PHYSICAL3'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL3'], 'HEALTH_PHYSICAL3', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3)"');
        $row['TECH_HOME3'] = knjCreateTextBox($objForm, $row['TECH_HOME3'], 'TECH_HOME3', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3)"');
        $row['MUSIC3'] = knjCreateTextBox($objForm, $row['MUSIC3'], 'MUSIC3', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3)"');
        $row['ART3'] = knjCreateTextBox($objForm, $row['ART3'], 'ART3', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3)"');
        $row['TOTAL33'] = knjCreateTextBox($objForm, $row['TOTAL33'], 'TOTAL33', 3, 3, 'readonly="readonly"');
        $row['TOTAL53'] = knjCreateTextBox($objForm, $row['TOTAL53'], 'TOTAL53', 3, 3, 'readonly="readonly"');
        $row['TOTAL93'] = knjCreateTextBox($objForm, $row['TOTAL93'], 'TOTAL93', 3, 3, 'readonly="readonly"');
        if ($model->examSchoolKind == 'J') {
            $row['JAPANESE4'] = knjCreateTextBox($objForm, $row['JAPANESE4'], 'JAPANESE4', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4)"');
            $row['MATH4'] = knjCreateTextBox($objForm, $row['MATH4'], 'MATH4', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4)"');
            $row['SOCIETY4'] = knjCreateTextBox($objForm, $row['SOCIETY4'], 'SOCIETY4', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4)"');
            $row['SCIENCE4'] = knjCreateTextBox($objForm, $row['SCIENCE4'], 'SCIENCE4', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4)"');
            $row['ENGLISH4'] = knjCreateTextBox($objForm, $row['ENGLISH4'], 'ENGLISH4', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4)"');
            $row['HEALTH_PHYSICAL4'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL4'], 'HEALTH_PHYSICAL4', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4)"');
            $row['TECH_HOME4'] = knjCreateTextBox($objForm, $row['TECH_HOME4'], 'TECH_HOME4', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4)"');
            $row['MUSIC4'] = knjCreateTextBox($objForm, $row['MUSIC4'], 'MUSIC4', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4)"');
            $row['ART4'] = knjCreateTextBox($objForm, $row['ART4'], 'ART4', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4)"');
            $row['TOTAL34'] = knjCreateTextBox($objForm, $row['TOTAL34'], 'TOTAL34', 3, 3, 'readonly="readonly"');
            $row['TOTAL54'] = knjCreateTextBox($objForm, $row['TOTAL54'], 'TOTAL54', 3, 3, 'readonly="readonly"');
            $row['TOTAL94'] = knjCreateTextBox($objForm, $row['TOTAL94'], 'TOTAL94', 3, 3, 'readonly="readonly"');

            $row['JAPANESE5'] = knjCreateTextBox($objForm, $row['JAPANESE5'], 'JAPANESE5', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5)"');
            $row['MATH5'] = knjCreateTextBox($objForm, $row['MATH5'], 'MATH5', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5)"');
            $row['SOCIETY5'] = knjCreateTextBox($objForm, $row['SOCIETY5'], 'SOCIETY5', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5)"');
            $row['SCIENCE5'] = knjCreateTextBox($objForm, $row['SCIENCE5'], 'SCIENCE5', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5)"');
            $row['ENGLISH5'] = knjCreateTextBox($objForm, $row['ENGLISH5'], 'ENGLISH5', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5)"');
            $row['HEALTH_PHYSICAL5'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL5'], 'HEALTH_PHYSICAL5', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5)"');
            $row['TECH_HOME5'] = knjCreateTextBox($objForm, $row['TECH_HOME5'], 'TECH_HOME5', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5)"');
            $row['MUSIC5'] = knjCreateTextBox($objForm, $row['MUSIC5'], 'MUSIC5', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5)"');
            $row['ART5'] = knjCreateTextBox($objForm, $row['ART5'], 'ART5', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5)"');
            $row['TOTAL35'] = knjCreateTextBox($objForm, $row['TOTAL35'], 'TOTAL35', 3, 3, 'readonly="readonly"');
            $row['TOTAL55'] = knjCreateTextBox($objForm, $row['TOTAL55'], 'TOTAL55', 3, 3, 'readonly="readonly"');
            $row['TOTAL95'] = knjCreateTextBox($objForm, $row['TOTAL95'], 'TOTAL95', 3, 3, 'readonly="readonly"');

            $row['JAPANESE6'] = knjCreateTextBox($objForm, $row['JAPANESE6'], 'JAPANESE6', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6)"');
            $row['MATH6'] = knjCreateTextBox($objForm, $row['MATH6'], 'MATH6', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6)"');
            $row['SOCIETY6'] = knjCreateTextBox($objForm, $row['SOCIETY6'], 'SOCIETY6', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6)"');
            $row['SCIENCE6'] = knjCreateTextBox($objForm, $row['SCIENCE6'], 'SCIENCE6', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6)"');
            $row['ENGLISH6'] = knjCreateTextBox($objForm, $row['ENGLISH6'], 'ENGLISH6', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6)"');
            $row['HEALTH_PHYSICAL6'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL6'], 'HEALTH_PHYSICAL6', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6)"');
            $row['TECH_HOME6'] = knjCreateTextBox($objForm, $row['TECH_HOME6'], 'TECH_HOME6', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6)"');
            $row['MUSIC6'] = knjCreateTextBox($objForm, $row['MUSIC6'], 'MUSIC6', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6)"');
            $row['ART6'] = knjCreateTextBox($objForm, $row['ART6'], 'ART6', 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6)"');
            $row['TOTAL36'] = knjCreateTextBox($objForm, $row['TOTAL36'], 'TOTAL36', 3, 3, 'readonly="readonly"');
            $row['TOTAL56'] = knjCreateTextBox($objForm, $row['TOTAL56'], 'TOTAL56', 3, 3, 'readonly="readonly"');
            $row['TOTAL96'] = knjCreateTextBox($objForm, $row['TOTAL96'], 'TOTAL96', 3, 3, 'readonly="readonly"');
        }
        $row['SPECIAL_ACT1'] = knjCreateCheckBox($objForm, "SPECIAL_ACT1", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT1'] == '') ? '' : 'checked="checked"'));
        $row['SPECIAL_ACT2'] = knjCreateCheckBox($objForm, "SPECIAL_ACT2", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT2'] == '') ? '' : 'checked="checked"'));
        $row['SPECIAL_ACT3'] = knjCreateCheckBox($objForm, "SPECIAL_ACT3", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT3'] == '') ? '' : 'checked="checked"'));
        $row['SPECIAL_ACT4'] = knjCreateCheckBox($objForm, "SPECIAL_ACT4", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT4'] == '') ? '' : 'checked="checked"'));
        $row['SPECIAL_ACT5'] = knjCreateCheckBox($objForm, "SPECIAL_ACT5", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT5'] == '') ? '' : 'checked="checked"'));
        $row['SPECIAL_ACT6'] = knjCreateCheckBox($objForm, "SPECIAL_ACT6", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT6'] == '') ? '' : 'checked="checked"'));
        $row['SPECIAL_ACT7'] = knjCreateCheckBox($objForm, "SPECIAL_ACT7", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT7'] == '') ? '' : 'checked="checked"'));
        $row['SPECIAL_ACT8'] = knjCreateCheckBox($objForm, "SPECIAL_ACT8", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT8'] == '') ? '' : 'checked="checked"'));
        $row['SPECIAL_ACT9'] = knjCreateCheckBox($objForm, "SPECIAL_ACT9", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT9'] == '') ? '' : 'checked="checked"'));
        $row['SPECIAL_ACT10'] = knjCreateCheckBox($objForm, "SPECIAL_ACT10", 1, "onclick=\"chagenCheck()\"" . (($row['SPECIAL_ACT10'] == '') ? '' : 'checked="checked"'));

        $row['ACT_TOTAL'] = knjCreateTextBox($objForm, $row['ACT_TOTAL'], 'ACT_TOTAL', 1, 1, 'readonly="readonly"');
        $row['ATTENDANCE1'] = knjCreateTextBox($objForm, $row['ATTENDANCE1'], 'ATTENDANCE1', 3, 3, 'onblur="this.value=toInteger(this.value)"');
        $row['ATTENDANCE2'] = knjCreateTextBox($objForm, $row['ATTENDANCE2'], 'ATTENDANCE2', 3, 3, 'onblur="this.value=toInteger(this.value)"');
        $row['ATTENDANCE3'] = knjCreateTextBox($objForm, $row['ATTENDANCE3'], 'ATTENDANCE3', 3, 3, 'onblur="this.value=toInteger(this.value)"');
        if ($model->examSchoolKind == 'J') {
            $row['ATTENDANCE4'] = knjCreateTextBox($objForm, $row['ATTENDANCE4'], 'ATTENDANCE4', 3, 3, 'onblur="this.value=toInteger(this.value)"');
            $row['ATTENDANCE5'] = knjCreateTextBox($objForm, $row['ATTENDANCE5'], 'ATTENDANCE5', 3, 3, 'onblur="this.value=toInteger(this.value)"');
            $row['ATTENDANCE6'] = knjCreateTextBox($objForm, $row['ATTENDANCE6'], 'ATTENDANCE6', 3, 3, 'onblur="this.value=toInteger(this.value)"');
        }

        $row['SPECIAL_REMARK'] = KnjCreateTextArea($objForm, "SPECIAL_REMARK", 4, (20 * 2 + 1), "soft", '', $row["SPECIAL_REMARK"]);

        $row['REMARK1NAME'] = $db->getOne(knjl012vQuery::getRemarkName($model, 1));
        $row['REMARK2NAME'] = $db->getOne(knjl012vQuery::getRemarkName($model, 2));
        $row['REMARK3NAME'] = $db->getOne(knjl012vQuery::getRemarkName($model, 3));
        $row['REMARK1NAME'] = ($row['REMARK1NAME'] == '') ? '備考1' : $row['REMARK1NAME'];
        $row['REMARK2NAME'] = ($row['REMARK2NAME'] == '') ? '備考2' : $row['REMARK2NAME'];
        $row['REMARK3NAME'] = ($row['REMARK3NAME'] == '') ? '備考3' : $row['REMARK3NAME'];
        $row['REMARK1'] = knjCreateTextBox($objForm, $row['REMARK1'], 'REMARK1', 100, 50, '');
        $row['REMARK2'] = knjCreateTextBox($objForm, $row['REMARK2'], 'REMARK2', 100, 50, '');
        $row['REMARK3'] = knjCreateTextBox($objForm, $row['REMARK3'], 'REMARK3', 100, 50, '');

        $idx = 1;
        $list = array('','JAPANESE','MATH','SOCIETY','SCIENCE','ENGLISH','HEALTH_PHYSICAL','TECH_HOME','MUSIC','ART');
        $nameSpare1 = array();
        $nameSpare3 = array();
        $result = $db->query(knjl012vQuery::getKamokuName($model));
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['KamokuName' . $idx] = $row2['NAME1'];
            if ($row2['NAMESPARE1'] == '1') {
                $nameSpare1[] = $list[$idx];
            }
            if ($row2['NAMESPARE3'] == '1') {
                $nameSpare3[] = $list[$idx];
            }
            $idx++;
        }
        array_shift($list);
        $arg['data'] = $row;

        /* ボタン作成 */
        //更新ボタン
        $extra = "onmousedown=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$entMove);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011V/knjl011vindex.php?cmd=move";
        $extra = " onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "EXAMNO", $model->field['EXAMNO']);
        knjCreateHidden($objForm, "TOTAL3", implode(',', $nameSpare3));
        knjCreateHidden($objForm, "TOTAL5", implode(',', $nameSpare1));
        knjCreateHidden($objForm, "TOTAL9", implode(',', $list));
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl012vForm2.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array();
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
