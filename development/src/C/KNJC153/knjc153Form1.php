<?php

require_once('for_php7.php');


class knjc153Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc153Form1", "POST", "knjc153index.php", "", "knjc153Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期コードをhiddenで送る
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //DB接続
        $db = Query::dbCheckOut();

        //クラス一覧リスト作成する
        $query = knjc153Query::getAuth($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, isset($row1) ? $row1 : array(), $extra, 15);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, array(), $extra, 15);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //各月リストを作成する
        $extra = "";
        if ($model->field["MONTH"] == '') {
            $yearmonthday = explode('-', CTRL_DATE);
            $model->field["MONTH"] = $yearmonthday[1];
        }

        //クラス一覧リスト作成する
        $months = array();
        $query = knjc153Query::getNameMst("C044", "INT(VALUE(NAMESPARE1, '99'))");
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $months[]= array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
        }
        $result->free();
        $arg["data"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->field["MONTH"], $months, $extra, 1);

        //欠席率テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align: right;\" ";
        $arg["data"]["RATE"] = knjCreateTextBox($objForm, isset($model->field["RATE"]) ? $model->field["RATE"] : 20, "RATE", 3, 3, $extra);

        //印刷日付作成
        $model->field["PRINT_DATE"] = $model->field["PRINT_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["PRINT_DATE"];
        $arg["data"]["PRINT_DATE"] = View::popUpCalendar($objForm, "PRINT_DATE", $model->field["PRINT_DATE"]);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '', '', '');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC153");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "printSubclassLastChairStd", $model->Properties["printSubclassLastChairStd"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc153Form1.html", $arg); 
    }
}
?>
