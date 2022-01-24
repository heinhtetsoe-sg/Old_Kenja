<?php

require_once('for_php7.php');

class knjl012vForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl012vindex.php", "", "edit");
        $db     = Query::dbCheckOut();

        $db = Query::dbCheckOut();
        $arg['header']['YEAR'] = makeCmb($objForm, $arg, $db, knjl012vQuery::getYear($model), 'YEAR', $model->year, 'onchange="btn_submit(\'left\')"', '1', 'BLANK');
        $arg['header']['APPLICANT_DIV'] = makeCmb($objForm, $arg, $db, knjl012vQuery::getApplicantDiv($model), 'APPLICANT_DIV', $model->applicantDiv, 'onchange="btn_submit(\'left\')"', '1', 'BLANK');
        $arg['header']['EXAM_SCHOOL_KIND'] = makeCmb($objForm, $arg, $db, knjl012vQuery::getExamSchoolKind($model), 'EXAM_SCHOOL_KIND', $model->examSchoolKind, 'onchange="btn_submit(\'left\')"', '1', 'BLANK');

        if ($model->cmd == 'search') {
            $model->data = array();
            $result = $db->query(knjl012vQuery::getMenbars($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row['YEAR'] = $model->year;
                $model->data[] = $row;
            }
        }
        $arg['data'] = $model->data;
        //終了ボタン
        $extra = "onclick=\"return btn_submit('search');\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        Query::dbCheckIn($db);


        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl012vForm1.html", $arg);
    }
}

//コンボ作成
function MakeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
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
