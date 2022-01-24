<?php

require_once('for_php7.php');

class knje156aForm1
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje156aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg['YEAR'] = $model->year;
        $arg['SEMESTERNAME'] = CTRL_SEMESTERNAME;

        $opt[] = array('label'=>'', 'value'=>'');
        $result = $db->query(knje156aQuery::getHrClassAuth($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['HR_CLASS'] = knjCreateCombo($objForm, 'HR_CLASS', $model->gradeHrClass, $opt, ' onchange="btn_submit(\'edit\');"', '');

        $gyo = 10;
        $moji = 17;
        $extra = '';
        if ($model->Properties['Specialactremork_3disp_J'] == '1') {
            $arg['CLASSACT'] = KnjCreateTextArea($objForm, "CLASSACT", $gyo, ($moji * 2 + 1), "soft", $extra, $model->classAct);
            $arg['STUDENTACT'] = KnjCreateTextArea($objForm, "STUDENTACT", $gyo, ($moji * 2 + 1), "soft", $extra, $model->studentAct);
            $arg['SCHOOLEVENT'] = KnjCreateTextArea($objForm, "SCHOOLEVENT", $gyo, ($moji * 2 + 1), "soft", $extra, $model->schoolEvent);
        } else {
            $arg['SPECIALACTREMARK'] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", $gyo, ($moji * 2 + 1), "soft", $extra, $model->specialActRemark);
        }

        $cnt = 0;
        $schregnos = array();
        $result = $db->query(knje156aQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row['CNT'] = $cnt;
            for ($i = 1; $i < 14; $i++) {
                $extra = ($row['REC'.$i] == '1')?' checked="checked"':'';
                $row['REC'.$i] = knjCreateCheckBox($objForm, "REC".$i.'_'.$cnt, "1", $extra, "");
            }
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
        View::toHTML($model, "knje156aForm1.html", $arg);
    }
}
