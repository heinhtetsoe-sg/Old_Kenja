<?php

require_once('for_php7.php');

class knjd627gForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //学年
        $extra = "";
        $opt = array();
        $opt[] = array("label" => "1年", "value" => "01");
        $opt[] = array('label' => "2年", 'value' => "02");
        if ($model->grade == null) {
            $model->grade = "01";
        }
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $opt, $extra, 1);
        
        //Aランク
        if ($model->a_rank == null) {
            $model->a_rank = "1500";
        }
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["A_RANK"] = knjCreateTextBox($objForm, $model->a_rank, "A_RANK", 4, 4, $extra);

        //プレビュー・印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        
        //csv出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
        
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //非表示項目
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "HID_YEAR", $model->year);
        knjCreateHidden($objForm, "HID_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "PRGID", $model->programID);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //HTML出力終了
        $arg["start"]  = $objForm->get_start("main", "POST", "knjd627gindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjd627gForm1.html", $arg);
    }
}
