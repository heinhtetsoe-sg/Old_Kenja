<?php

require_once('for_php7.php');

class knjp412Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp412Form1", "POST", "knjp412index.php", "", "knjp412Form1");

        //DB接続
        $db = Query::dbCheckOut();

        $opt=array();

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //makeCmb2
        $query = knjp412Query::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "ALL");

        //中高判定フラグを作成する
        $row = $db->getOne(knjp412Query::GetJorH());
        if ($row == 1) {
            $jhflg = 1;
        } else {
            $jhflg = 2;
        }

        //CSV
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "ＣＳＶ出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "JHFLG", $jhflg);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJP412");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp412Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "全学年", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
