<?php

require_once('for_php7.php');

class knja611Form1
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja611index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg['YEAR'] = $model->year;

        $opt[] = array('label'=>'', 'value'=>'');
        $result = $db->query(knja611Query::getHrClassAuth($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['HR_CLASS'] = knjCreateCombo($objForm, 'HR_CLASS', $model->gradeHrClass, $opt, ' onchange="btn_submit(\'edit\');"', '');

        $cnt = 0;
        $schregnos = array();
        $extra = " onPaste=\"return showPaste(this);\"";
        $result = $db->query(knja611Query::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['CNT'] = $cnt;
            $row['IQ'] = knjCreateTextBox($objForm, $row['IQ'], 'IQ-'.$cnt, 3, 3, $extra);
            $arg['data'][] = $row;
            $schregnos[] = $row['SCHREGNO'];
            $cnt++;
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $ext = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $ext);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "cnt", $cnt);
        knjCreateHidden($objForm, "schregnos", implode(',', $schregnos));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja611Form1.html", $arg);
    }
}
