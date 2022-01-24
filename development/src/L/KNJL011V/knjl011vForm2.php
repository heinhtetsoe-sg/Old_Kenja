<?php

require_once('for_php7.php');

class knjl011vForm2
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl011vindex.php", "", "edit");
        $db     = Query::dbCheckOut();
        if ($model->cmd == 'delete') {
            $row = array();
        } elseif ($model->cmd != 'finschoolsearch' && !isset($model->warning)) {
            $row = $db->getRow(knjl011vQuery::selectQuery($db, $model), DB_FETCHMODE_ASSOC);
            $row['EXAMNO'] = $model->field['EXAMNO'];
        } else {
            $row = $model->field;
        }
        $row['EXAMNO'] = knjCreateTextBox($objForm, $row['EXAMNO'], 'EXAMNO', 8, 8, '');
        $row['NAME'] = knjCreateTextBox($objForm, $row['NAME'], 'NAME', 80, 40, '');
        $row['NAME_KANA'] = knjCreateTextBox($objForm, $row['NAME_KANA'], 'NAME_KANA', 160, 80, '');
        $row['SEX'] = makeCmb($objForm, $arg, $db, knjl011vQuery::getSex($model), 'SEX', $row['SEX'], '', 1, 'BLANK');
        $row['BIRTHDAY'] = View::popUpCalendar($objForm, "BIRTHDAY", str_replace('-', '/', $row['BIRTHDAY']));
        $row['FINSCHOOL_NAME'] = $db->getOne(knjl011vQuery::getFinschoolName($row['FINSCHOOLCD']));
        $extra = "onblur=\"this.value=toInteger(this.value)\" onkeydown=\"keyChangeEntToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $row["FINSCHOOLCD"] = knjCreateTextBox($objForm, $row["FINSCHOOLCD"], "FINSCHOOLCD", 8, 8, $extra);
        $row['FINISH_DATE'] = View::popUpCalendar($objForm, "FINISH_DATE", str_replace('-', '/', $row['FINISH_DATE']));
        $row['ZIPCD'] = knjCreateTextBox($objForm, $row['ZIPCD'], 'ZIPCD', 8, 8, '');
        $row['ADDR1'] = knjCreateTextBox($objForm, $row['ADDR1'], 'ADDR1', 100, 50, '');
        $row['ADDR2'] = knjCreateTextBox($objForm, $row['ADDR2'], 'ADDR2', 100, 50, '');
        $row['TELNO'] = knjCreateTextBox($objForm, $row['TELNO'], 'TELNO', 14, 14, '');
        $row['EMAIL'] = knjCreateTextBox($objForm, $row['EMAIL'], 'EMAIL', 50, 50, '');

        $row['GUARD_NAME'] = knjCreateTextBox($objForm, $row['GUARD_NAME'], 'GUARD_NAME', 80, 40, '');
        $row['GUARD_NAME_KANA'] = knjCreateTextBox($objForm, $row['GUARD_NAME_KANA'], 'GUARD_NAME_KANA', 160, 80, '');
        $row['RELATION'] = makeCmb($objForm, $arg, $db, knjl011vQuery::getRelationship($model), 'RELATION', $row['RELATION'], '', 1, 'BLANK');
        $row['GUARD_ZIP'] = knjCreateTextBox($objForm, $row['GUARD_ZIP'], 'GUARD_ZIP', 8, 8, '');
        $row['GUARD_ADDR1'] = knjCreateTextBox($objForm, $row['GUARD_ADDR1'], 'GUARD_ADDR1', 100, 50, '');
        $row['GUARD_ADDR2'] = knjCreateTextBox($objForm, $row['GUARD_ADDR2'], 'GUARD_ADDR2', 100, 50, '');
        $row['GUARD_TELNO'] = knjCreateTextBox($objForm, $row['GUARD_TELNO'], 'GUARD_TELNO', 14, 14, '');

        $row['REMARK1NAME'] = $db->getOne(knjl011vQuery::getRemarkName($model, 1));
        $row['REMARK2NAME'] = $db->getOne(knjl011vQuery::getRemarkName($model, 2));
        $row['REMARK3NAME'] = $db->getOne(knjl011vQuery::getRemarkName($model, 3));
        $row['REMARK4NAME'] = $db->getOne(knjl011vQuery::getRemarkName($model, 4));
        $row['REMARK5NAME'] = $db->getOne(knjl011vQuery::getRemarkName($model, 5));
        $row['REMARK1NAME'] = ($row['REMARK1NAME'] == '') ? '備考1' : $row['REMARK1NAME'];
        $row['REMARK2NAME'] = ($row['REMARK2NAME'] == '') ? '備考2' : $row['REMARK2NAME'];
        $row['REMARK3NAME'] = ($row['REMARK3NAME'] == '') ? '備考3' : $row['REMARK3NAME'];
        $row['REMARK4NAME'] = ($row['REMARK4NAME'] == '') ? '備考4' : $row['REMARK4NAME'];
        $row['REMARK5NAME'] = ($row['REMARK5NAME'] == '') ? '備考5' : $row['REMARK5NAME'];
        $row['REMARK1'] = knjCreateTextBox($objForm, $row['REMARK1'], 'REMARK1', 100, 50, '');
        $row['REMARK2'] = knjCreateTextBox($objForm, $row['REMARK2'], 'REMARK2', 100, 50, '');
        $row['REMARK3'] = knjCreateTextBox($objForm, $row['REMARK3'], 'REMARK3', 100, 50, '');
        $row['REMARK4'] = knjCreateTextBox($objForm, $row['REMARK4'], 'REMARK4', 100, 50, '');
        $row['REMARK5'] = knjCreateTextBox($objForm, $row['REMARK5'], 'REMARK5', 100, 50, '');

        $cnt = 1;
        $result = $db->query(knjl011vQuery::selectQuery3($model));
        for ($cnt = 1; $cnt <= 5; $cnt++) {
            $row2 = $result->fetchRow(DB_FETCHMODE_ASSOC);
            if (isset($row2)) {
                if ($row2['EXAM_SCHOOL_KIND_DISP'] != '') {
                    $row['EXAM_SCHOOL_KIND_DISP'] = $row2['EXAM_SCHOOL_KIND_DISP'];
                }
                if ($row2['APPLICANT_DIV_DISP'] != '') {
                    $row['APPLICANT_DIV_DISP'] = $row2['APPLICANT_DIV_DISP'];
                }
                $row['RECEPTNO'.$cnt] = knjCreateTextBox($objForm, $row2['RECEPTNO'], 'RECEPTNO'.$cnt, 4, 4, 'readonly="readonly" disabled="disabled"');
            } else {
                $row['RECEPTNO'.$cnt] = knjCreateTextBox($objForm, '', 'RECEPTNO'.$cnt, 4, 4, 'readonly="readonly" disabled="disabled"');
            }
        }

        $arg['data'] = $row;

        /* ボタン作成 */
        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

        //内申点入力ボタン
        $link = REQUESTROOT."/L/KNJL012V/knjl012vindex.php?cmd=move&EXAMNO={$model->field['EXAMNO']}&YEAR={$model->year}&EXP_YEAR={$model->exp_year}&EXAM_SCHOOL_KIND={$model->examSchoolKind}&APPLICANT_DIV={$model->applicantDiv}";
        $extra = " onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_naisin"] = knjCreateBtn($objForm, "btn_naisin", "内申書入力", $extra);

        $disabled = '';

        //受験番号ボタン
        $link = REQUESTROOT."/L/KNJL011V/knjl011vindex.php?cmd=number";
        $extra = " onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_number"] = knjCreateBtn($objForm, "btn_number", "受験番号", $extra.$disabled);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra.$disabled);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disabled);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        Query::dbCheckIn($db);

        if ($model->cmd == "add" || $model->cmd == "delete") {
            $arg["jscript"] = "window.open('knjl011vindex.php?cmd=search','left_frame')";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011vForm2.html", $arg);
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
