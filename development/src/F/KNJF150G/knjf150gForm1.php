<?php

require_once('for_php7.php');

class knjf150gForm1
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf150gindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学年コンボ
        $extra = " onchange=\"btn_submit('edit');\"";

        $opt = array();
        $opt[] = array("label" => "全学年", "value" => "all");
        $result = $db->query(knjf150gQuery::getGrade($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $opt, $extra, 1);

        //クラスコンボ
        $extra = "";
        $opt = array();
        $opt[] = array("label" => "全クラス", "value" => "all");
        if ($model->grade != '') {
            $result = $db->query(knjf150gQuery::getHrClass($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
            $result->free();
        }
        $arg["HR_CLASS"] = knjCreateCombo($objForm, "HR_CLASS", $model->hrClass, $opt, $extra, 1);

        $arg["START_DATE"] = View::popUpCalendar2($objForm, "START_DATE", str_replace("-", "/", $model->startDate), '', 'btn_submit(\'edit\');');
        $arg["END_DATE"] = View::popUpCalendar2($objForm, "END_DATE", str_replace("-", "/", $model->endDate), '', 'btn_submit(\'edit\');');

        if ($model->cmd == 'search' && !isset($model->warning)) {
            $model->data = array();
            $result = $db->query(knjf150gQuery::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["VISIT_TIME"] = str_replace("-", "/", $row["VISIT_DATE"]).' '.$row["VISIT_HOUR"].':'.$row["VISIT_MINUTE"];

                $row["VISIT_TIME"] = View::alink(
                    REQUESTROOT . "/F/KNJF150/knjf150index.php",
                    $row["VISIT_TIME"],
                    "target=_self tabindex=\"-1\"",
                    array("SCHREGNO"        => $row["SCHREGNO"],
                        "VISIT_DATE"      => $row["VISIT_DATE"],
                        "VISIT_HOUR"      => $row["VISIT_HOUR"],
                        "VISIT_MINUTE"    => $row["VISIT_MINUTE"],
                        "TYPE"            => $row["TYPE"],
                        "ATTENDNO"        => $row['ATTENDNO'],
                        "NAME"            => $row['NAME'],
                        "HR_CLASS"        => $row['HR_CLASS'],
                        "GRADE"           => $row['GRADE'],
                        "SEND_PROGRAMID"  => 'KNJF150G',
                        "cmd"             => "subform".$row["TYPE"]."A")
                );
                $arg['data'][] = $row;
                $model->data[] = $row;
            }
        } else {
            $arg['data'] = $model->data;
        }

        //対象選択ボタン（一部）
        $extra = " onclick=\"btn_submit('search');\"";
        $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "検索", $extra);

        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf150gForm1.html", $arg);
    }
}
