<?php

require_once('for_php7.php');

class knjl013vForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl013vindex.php", "", "edit");
        $db     = Query::dbCheckOut();

        $extra = "onchange=\"return btn_submit('main')\"";
        $arg['header']['YEAR'] = $model->year;
        $arg['header']['EXAM_SCHOOL_KIND'] = makeCmb($objForm, $arg, $db, knjl013vQuery::getExamSchoolKind($model), 'EXAM_SCHOOL_KIND', $model->examSchoolKind, $extra, 1);
        $arg['header']['SIKEN_ID'] = makeCmb($objForm, $arg, $db, knjl013vQuery::getSikenId($model), 'SIKEN_ID', $model->sikenId, '', 1);
        $arg['header']['EXAMNO_START'] = knjCreateTextBox($objForm, $model->examnoStart, 'EXAMNO_START', 8, 8, '');
        $arg['header']['EXAMNO_END'] = knjCreateTextBox($objForm, $model->examnoEnd, 'EXAMNO_END', 8, 8, '');
        $arg['header']['RECEPTNO_START'] = knjCreateTextBox($objForm, $model->receptnoStart, 'RECEPTNO_START', 4, 4, '');
        $arg['header']['RECEPTNO_END'] = knjCreateTextBox($objForm, $model->receptnoEnd, 'RECEPTNO_END', 4, 4, '');
        $arg['header']['FINSCHOOLCD'] = makeCmb($objForm, $arg, $db, knjl013vQuery::getFinschool($model), 'FINSCHOOLCD', $model->finschoolcd, '', 1, 'BLANK');

        $opt = array();
        $opt[] = array('label' => '1年次内申(評定)', 'value' => '1');
        $opt[] = array('label' => '2年次内申(評定)', 'value' => '2');
        $opt[] = array('label' => '3年次内申(評定)', 'value' => '3');
        if ($model->examSchoolKind == 'J') {
            $opt[] = array('label' => '4年次内申(評定)', 'value' => '8');
            $opt[] = array('label' => '5年次内申(評定)', 'value' => '9');
            $opt[] = array('label' => '6年次内申(評定)', 'value' => '10');
        }
        $opt[] = array('label' => '出欠情報', 'value' => '4');
        $opt[] = array('label' => '特別活動', 'value' => '5');
        $opt[] = array('label' => '特記事項', 'value' => '6');
        $opt[] = array('label' => '備考1～3', 'value' => '7');
        $extra = "onchange=\"return btn_submit('edit');\"";
        $arg['header']['SELECT_PATTERN'] = knjCreateCombo($objForm, 'SELECT_PATTERN', $model->selectPattern, $opt, $extra, 1);

        if ($model->selectPattern == 1) {
            $arg['SELECT_PATARN1'] = '1';
        } elseif ($model->selectPattern == 2) {
            $arg['SELECT_PATARN2'] = '1';
        } elseif ($model->selectPattern == 3) {
            $arg['SELECT_PATARN3'] = '1';
        } elseif ($model->selectPattern == 4) {
            $arg['SELECT_PATARN4'] = '1';
        } elseif ($model->selectPattern == 5) {
            $arg['SELECT_PATARN5'] = '1';
        } elseif ($model->selectPattern == 6) {
            $arg['SELECT_PATARN6'] = '1';
        } elseif ($model->selectPattern == 7) {
            $arg['SELECT_PATARN7'] = '1';
        } elseif ($model->selectPattern == 8) {
            $arg['SELECT_PATARN8'] = '1';
        } elseif ($model->selectPattern == 9) {
            $arg['SELECT_PATARN9'] = '1';
        } elseif ($model->selectPattern == 10) {
            $arg['SELECT_PATARN10'] = '1';
        }
        if ($model->examSchoolKind == 'J') {
            $arg['EXAM_SCHOOL_KIND_J'] = '1';
        }

        $list = array('','JAPANESE','MATH','SOCIETY','SCIENCE','ENGLISH','HEALTH_PHYSICAL','TECH_HOME','MUSIC','ART');
        $model->examnos = array();
        if (!isset($model->warning) && $model->cmd != "main") {
            $cnt = 0;
            $grades = array();
            $result = $db->query(knjl013vQuery::getGradeCd($db, $model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $grades[] = $row['GRADE_CD'];
                $cnt++;
                if ($model->examSchoolKind == 'J') {
                    if ($cnt == 6) {
                        break;
                    }
                } else {
                    if ($cnt == 3) {
                        break;
                    }
                }
            }
            $rows = array();
            if ($model->sikenId) {
                if ($model->examSchoolKind == 'J') {
                    $result = $db->query(knjl013vQuery::selectQuery($model, $grades[0], $grades[1], $grades[2], $grades[3], $grades[4], $grades[5]));
                } else {
                    $result = $db->query(knjl013vQuery::selectQuery($model, $grades[0], $grades[1], $grades[2]));
                }
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
		            $row2 = $db->getRow(knjl013vQuery::selectQueryRemark($model, $row['EXAMNO']), DB_FETCHMODE_ASSOC);
		            if(isset($row2)){
		                $row = array_merge($row, $row2);
		            }
                    $rows[] = $row;
                }
            }
        } else {
            $rows = $model->data;
        }
        for ($counter = 0; $counter < get_count($rows); $counter++) {
            $row = $rows[$counter];
            $row['JAPANESE1'] = knjCreateTextBox($objForm, $row['JAPANESE1'], 'JAPANESE1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1, '.$counter.')"');
            $row['MATH1'] = knjCreateTextBox($objForm, $row['MATH1'], 'MATH1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1, '.$counter.')"');
            $row['SOCIETY1'] = knjCreateTextBox($objForm, $row['SOCIETY1'], 'SOCIETY1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1, '.$counter.')"');
            $row['SCIENCE1'] = knjCreateTextBox($objForm, $row['SCIENCE1'], 'SCIENCE1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1, '.$counter.')"');
            $row['ENGLISH1'] = knjCreateTextBox($objForm, $row['ENGLISH1'], 'ENGLISH1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1, '.$counter.')"');
            $row['HEALTH_PHYSICAL1'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL1'], 'HEALTH_PHYSICAL1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1, '.$counter.')"');
            $row['TECH_HOME1'] = knjCreateTextBox($objForm, $row['TECH_HOME1'], 'TECH_HOME1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1, '.$counter.')"');
            $row['MUSIC1'] = knjCreateTextBox($objForm, $row['MUSIC1'], 'MUSIC1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1, '.$counter.')"');
            $row['ART1'] = knjCreateTextBox($objForm, $row['ART1'], 'ART1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(1, '.$counter.')"');
            $row['TOTAL31'] = knjCreateTextBox($objForm, $row['TOTAL31'], 'TOTAL31-' . $counter, 3, 3, 'readonly="readonly"');
            $row['TOTAL51'] = knjCreateTextBox($objForm, $row['TOTAL51'], 'TOTAL51-' . $counter, 3, 3, 'readonly="readonly"');
            $row['TOTAL91'] = knjCreateTextBox($objForm, $row['TOTAL91'], 'TOTAL91-' . $counter, 3, 3, 'readonly="readonly"');

            $row['JAPANESE2'] = knjCreateTextBox($objForm, $row['JAPANESE2'], 'JAPANESE2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2, '.$counter.')"');
            $row['MATH2'] = knjCreateTextBox($objForm, $row['MATH2'], 'MATH2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2, '.$counter.')"');
            $row['SOCIETY2'] = knjCreateTextBox($objForm, $row['SOCIETY2'], 'SOCIETY2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2, '.$counter.')"');
            $row['SCIENCE2'] = knjCreateTextBox($objForm, $row['SCIENCE2'], 'SCIENCE2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2, '.$counter.')"');
            $row['ENGLISH2'] = knjCreateTextBox($objForm, $row['ENGLISH2'], 'ENGLISH2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2, '.$counter.')"');
            $row['HEALTH_PHYSICAL2'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL2'], 'HEALTH_PHYSICAL2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2, '.$counter.')"');
            $row['TECH_HOME2'] = knjCreateTextBox($objForm, $row['TECH_HOME2'], 'TECH_HOME2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2, '.$counter.')"');
            $row['MUSIC2'] = knjCreateTextBox($objForm, $row['MUSIC2'], 'MUSIC2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2, '.$counter.')"');
            $row['ART2'] = knjCreateTextBox($objForm, $row['ART2'], 'ART2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(2, '.$counter.')"');
            $row['TOTAL32'] = knjCreateTextBox($objForm, $row['TOTAL32'], 'TOTAL32-' . $counter, 3, 3, 'readonly="readonly"');
            $row['TOTAL52'] = knjCreateTextBox($objForm, $row['TOTAL52'], 'TOTAL52-' . $counter, 3, 3, 'readonly="readonly"');
            $row['TOTAL92'] = knjCreateTextBox($objForm, $row['TOTAL92'], 'TOTAL92-' . $counter, 3, 3, 'readonly="readonly"');

            $row['JAPANESE3'] = knjCreateTextBox($objForm, $row['JAPANESE3'], 'JAPANESE3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3, '.$counter.')"');
            $row['MATH3'] = knjCreateTextBox($objForm, $row['MATH3'], 'MATH3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3, '.$counter.')"');
            $row['SOCIETY3'] = knjCreateTextBox($objForm, $row['SOCIETY3'], 'SOCIETY3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3, '.$counter.')"');
            $row['SCIENCE3'] = knjCreateTextBox($objForm, $row['SCIENCE3'], 'SCIENCE3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3, '.$counter.')"');
            $row['ENGLISH3'] = knjCreateTextBox($objForm, $row['ENGLISH3'], 'ENGLISH3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3, '.$counter.')"');
            $row['HEALTH_PHYSICAL3'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL3'], 'HEALTH_PHYSICAL3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3, '.$counter.')"');
            $row['TECH_HOME3'] = knjCreateTextBox($objForm, $row['TECH_HOME3'], 'TECH_HOME3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3, '.$counter.')"');
            $row['MUSIC3'] = knjCreateTextBox($objForm, $row['MUSIC3'], 'MUSIC3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3, '.$counter.')"');
            $row['ART3'] = knjCreateTextBox($objForm, $row['ART3'], 'ART3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(3, '.$counter.')"');
            $row['TOTAL33'] = knjCreateTextBox($objForm, $row['TOTAL33'], 'TOTAL33-' . $counter, 3, 3, 'readonly="readonly"');
            $row['TOTAL53'] = knjCreateTextBox($objForm, $row['TOTAL53'], 'TOTAL53-' . $counter, 3, 3, 'readonly="readonly"');
            $row['TOTAL93'] = knjCreateTextBox($objForm, $row['TOTAL93'], 'TOTAL93-' . $counter, 3, 3, 'readonly="readonly"');

            if ($model->examSchoolKind == 'J') {
                $row['JAPANESE4'] = knjCreateTextBox($objForm, $row['JAPANESE4'], 'JAPANESE4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4, '.$counter.')"');
                $row['MATH4'] = knjCreateTextBox($objForm, $row['MATH4'], 'MATH4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4, '.$counter.')"');
                $row['SOCIETY4'] = knjCreateTextBox($objForm, $row['SOCIETY4'], 'SOCIETY4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4, '.$counter.')"');
                $row['SCIENCE4'] = knjCreateTextBox($objForm, $row['SCIENCE4'], 'SCIENCE4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4, '.$counter.')"');
                $row['ENGLISH4'] = knjCreateTextBox($objForm, $row['ENGLISH4'], 'ENGLISH4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4, '.$counter.')"');
                $row['HEALTH_PHYSICAL4'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL4'], 'HEALTH_PHYSICAL4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4, '.$counter.')"');
                $row['TECH_HOME4'] = knjCreateTextBox($objForm, $row['TECH_HOME4'], 'TECH_HOME4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4, '.$counter.')"');
                $row['MUSIC4'] = knjCreateTextBox($objForm, $row['MUSIC4'], 'MUSIC4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4, '.$counter.')"');
                $row['ART4'] = knjCreateTextBox($objForm, $row['ART4'], 'ART4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(4, '.$counter.')"');
                $row['TOTAL34'] = knjCreateTextBox($objForm, $row['TOTAL34'], 'TOTAL34-' . $counter, 3, 3, 'readonly="readonly"');
                $row['TOTAL54'] = knjCreateTextBox($objForm, $row['TOTAL54'], 'TOTAL54-' . $counter, 3, 3, 'readonly="readonly"');
                $row['TOTAL94'] = knjCreateTextBox($objForm, $row['TOTAL94'], 'TOTAL94-' . $counter, 3, 3, 'readonly="readonly"');

                $row['JAPANESE5'] = knjCreateTextBox($objForm, $row['JAPANESE5'], 'JAPANESE5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5, '.$counter.')"');
                $row['MATH5'] = knjCreateTextBox($objForm, $row['MATH5'], 'MATH5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5, '.$counter.')"');
                $row['SOCIETY5'] = knjCreateTextBox($objForm, $row['SOCIETY5'], 'SOCIETY5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5, '.$counter.')"');
                $row['SCIENCE5'] = knjCreateTextBox($objForm, $row['SCIENCE5'], 'SCIENCE5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5, '.$counter.')"');
                $row['ENGLISH5'] = knjCreateTextBox($objForm, $row['ENGLISH5'], 'ENGLISH5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5, '.$counter.')"');
                $row['HEALTH_PHYSICAL5'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL5'], 'HEALTH_PHYSICAL5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5, '.$counter.')"');
                $row['TECH_HOME5'] = knjCreateTextBox($objForm, $row['TECH_HOME5'], 'TECH_HOME5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5, '.$counter.')"');
                $row['MUSIC5'] = knjCreateTextBox($objForm, $row['MUSIC5'], 'MUSIC5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5, '.$counter.')"');
                $row['ART5'] = knjCreateTextBox($objForm, $row['ART5'], 'ART5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(5, '.$counter.')"');
                $row['TOTAL35'] = knjCreateTextBox($objForm, $row['TOTAL35'], 'TOTAL35-' . $counter, 3, 3, 'readonly="readonly"');
                $row['TOTAL55'] = knjCreateTextBox($objForm, $row['TOTAL55'], 'TOTAL55-' . $counter, 3, 3, 'readonly="readonly"');
                $row['TOTAL95'] = knjCreateTextBox($objForm, $row['TOTAL95'], 'TOTAL95-' . $counter, 3, 3, 'readonly="readonly"');

                $row['JAPANESE6'] = knjCreateTextBox($objForm, $row['JAPANESE6'], 'JAPANESE6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6, '.$counter.')"');
                $row['MATH6'] = knjCreateTextBox($objForm, $row['MATH6'], 'MATH6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6, '.$counter.')"');
                $row['SOCIETY6'] = knjCreateTextBox($objForm, $row['SOCIETY6'], 'SOCIETY6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6, '.$counter.')"');
                $row['SCIENCE6'] = knjCreateTextBox($objForm, $row['SCIENCE6'], 'SCIENCE6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6, '.$counter.')"');
                $row['ENGLISH6'] = knjCreateTextBox($objForm, $row['ENGLISH6'], 'ENGLISH6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6, '.$counter.')"');
                $row['HEALTH_PHYSICAL6'] = knjCreateTextBox($objForm, $row['HEALTH_PHYSICAL6'], 'HEALTH_PHYSICAL6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6, '.$counter.')"');
                $row['TECH_HOME6'] = knjCreateTextBox($objForm, $row['TECH_HOME6'], 'TECH_HOME6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6, '.$counter.')"');
                $row['MUSIC6'] = knjCreateTextBox($objForm, $row['MUSIC6'], 'MUSIC6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6, '.$counter.')"');
                $row['ART6'] = knjCreateTextBox($objForm, $row['ART6'], 'ART6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)" onchange="kamokuChange(6, '.$counter.')"');
                $row['TOTAL36'] = knjCreateTextBox($objForm, $row['TOTAL36'], 'TOTAL36-' . $counter, 3, 3, 'readonly="readonly"');
                $row['TOTAL56'] = knjCreateTextBox($objForm, $row['TOTAL56'], 'TOTAL56-' . $counter, 3, 3, 'readonly="readonly"');
                $row['TOTAL96'] = knjCreateTextBox($objForm, $row['TOTAL96'], 'TOTAL96-' . $counter, 3, 3, 'readonly="readonly"');
            }
            $row['SPECIAL_ACT1'] = knjCreateCheckBox($objForm, "SPECIAL_ACT1-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT1'] == '') ? '' : 'checked="checked"'));
            $row['SPECIAL_ACT2'] = knjCreateCheckBox($objForm, "SPECIAL_ACT2-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT2'] == '') ? '' : 'checked="checked"'));
            $row['SPECIAL_ACT3'] = knjCreateCheckBox($objForm, "SPECIAL_ACT3-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT3'] == '') ? '' : 'checked="checked"'));
            $row['SPECIAL_ACT4'] = knjCreateCheckBox($objForm, "SPECIAL_ACT4-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT4'] == '') ? '' : 'checked="checked"'));
            $row['SPECIAL_ACT5'] = knjCreateCheckBox($objForm, "SPECIAL_ACT5-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT5'] == '') ? '' : 'checked="checked"'));
            $row['SPECIAL_ACT6'] = knjCreateCheckBox($objForm, "SPECIAL_ACT6-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT6'] == '') ? '' : 'checked="checked"'));
            $row['SPECIAL_ACT7'] = knjCreateCheckBox($objForm, "SPECIAL_ACT7-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT7'] == '') ? '' : 'checked="checked"'));
            $row['SPECIAL_ACT8'] = knjCreateCheckBox($objForm, "SPECIAL_ACT8-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT8'] == '') ? '' : 'checked="checked"'));
            $row['SPECIAL_ACT9'] = knjCreateCheckBox($objForm, "SPECIAL_ACT9-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT9'] == '') ? '' : 'checked="checked"'));
            $row['SPECIAL_ACT10'] = knjCreateCheckBox($objForm, "SPECIAL_ACT10-" . $counter, 1, "onclick=\"chagenCheck({$counter})\"" . (($row['SPECIAL_ACT10'] == '') ? '' : 'checked="checked"'));

            $row['ACT_TOTAL'] = knjCreateTextBox($objForm, $row['ACT_TOTAL'], 'ACT_TOTAL-' . $counter, 1, 1, 'readonly="readonly"');
            $row['ATTENDANCE1'] = knjCreateTextBox($objForm, $row['ATTENDANCE1'], 'ATTENDANCE1-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)"');
            $row['ATTENDANCE2'] = knjCreateTextBox($objForm, $row['ATTENDANCE2'], 'ATTENDANCE2-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)"');
            $row['ATTENDANCE3'] = knjCreateTextBox($objForm, $row['ATTENDANCE3'], 'ATTENDANCE3-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)"');
            if ($model->examSchoolKind == 'J') {
                $row['ATTENDANCE4'] = knjCreateTextBox($objForm, $row['ATTENDANCE4'], 'ATTENDANCE4-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)"');
                $row['ATTENDANCE5'] = knjCreateTextBox($objForm, $row['ATTENDANCE5'], 'ATTENDANCE5-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)"');
                $row['ATTENDANCE6'] = knjCreateTextBox($objForm, $row['ATTENDANCE6'], 'ATTENDANCE6-' . $counter, 3, 3, 'onblur="this.value=toInteger(this.value)"');
            }

            $row['SPECIAL_REMARK'] = KnjCreateTextArea($objForm, "SPECIAL_REMARK-" . $counter, 4, (20 * 2 + 1), "soft", '', $row["SPECIAL_REMARK"]);

            $row['REMARK1NAME'] = $db->getOne(knjl013vQuery::getRemarkName($model, 1));
            $row['REMARK2NAME'] = $db->getOne(knjl013vQuery::getRemarkName($model, 2));
            $row['REMARK3NAME'] = $db->getOne(knjl013vQuery::getRemarkName($model, 3));
            $row['REMARK1NAME'] = ($row['REMARK1NAME'] == '') ? '備考1' : $row['REMARK1NAME'];
            $row['REMARK2NAME'] = ($row['REMARK2NAME'] == '') ? '備考2' : $row['REMARK2NAME'];
            $row['REMARK3NAME'] = ($row['REMARK3NAME'] == '') ? '備考3' : $row['REMARK3NAME'];
            $row['REMARK1'] = knjCreateTextBox($objForm, $row['REMARK1'], 'REMARK1-' . $counter, 100, 50, '');
            $row['REMARK2'] = knjCreateTextBox($objForm, $row['REMARK2'], 'REMARK2-' . $counter, 100, 50, '');
            $row['REMARK3'] = knjCreateTextBox($objForm, $row['REMARK3'], 'REMARK3-' . $counter, 100, 50, '');

            $row['EXAMNOHIDDEN'] =knjCreateHidden($objForm, 'EXAMNO-' . $counter, $row['EXAMNO']);
            $row['NAMEHIDDEN'] =knjCreateHidden($objForm, 'NAME-' . $counter, $row['NAME']);

            $arg['data'][] = $row;
            $model->examnos[] = $row['EXAMNO'];
        }
        $idx = 1;
        $list = array('','JAPANESE','MATH','SOCIETY','SCIENCE','ENGLISH','HEALTH_PHYSICAL','TECH_HOME','MUSIC','ART');
        $nameSpare1 = array();
        $nameSpare3 = array();
        $result = $db->query(knjl013vQuery::getKamokuName($model));
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg['title']['KamokuName' . $idx] = $row2['NAME1'];
            if ($row2['NAMESPARE1'] == '1') {
                $nameSpare1[] = $list[$idx];
            }
            if ($row2['NAMESPARE3'] == '1') {
                $nameSpare3[] = $list[$idx];
            }
            $idx++;
        }
        array_shift($list);

        /* ボタン作成 */
        //検索ボタン
        $extra = "onclick=\"btn_submit('edit')\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

        //更新ボタン
        $extra = "onmousedown=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$entMove);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "examnos", implode(',', $model->examnos));
        knjCreateHidden($objForm, "TOTAL3", implode(',', $nameSpare3));
        knjCreateHidden($objForm, "TOTAL5", implode(',', $nameSpare1));
        knjCreateHidden($objForm, "TOTAL9", implode(',', $list));
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl013vForm1.html", $arg);
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
