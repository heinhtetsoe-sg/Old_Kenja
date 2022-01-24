<?php

require_once('for_php7.php');

class knjd186qForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd186qForm1", "POST", "knjd186qindex.php", "", "knjd186qForm1");

        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = $model->control["年度"];
        $arg["data"]["SEMESTERNAME"] = $model->control["学期名"][CTRL_SEMESTER];

        //異動対象日付
        $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", str_replace("-", "/", $model->field["DATE"]));

        //前期科目のみ出力
        $extra = " onclick=\"checkPrint(this);\" ";
        if ($model->cmd == '') {
            if (CTRL_SEMESTER == '1') {
                $extra .= "checked";
            }
        } else if ($model->field["PRINT_ZENKI"] == "1") {
            $extra .= "checked";
        }
        $extra .= " id=\"PRINT_ZENKI\" ";
        $arg["data"]["PRINT_ZENKI"] = knjCreateCheckBox($objForm, "PRINT_ZENKI", "1", $extra, "");
        //後期科目のみ出力
        $extra = " onclick=\"checkPrint(this);\" ";
        if ($model->cmd == '') {
            if (CTRL_SEMESTER == '2') {
                $extra .= "checked";
            }
        } else if ($model->field["PRINT_KOUKI"] == "1") {
            $extra .= "checked";
        }
        $extra .= " id=\"PRINT_KOUKI\" ";
        $arg["data"]["PRINT_KOUKI"] = knjCreateCheckBox($objForm, "PRINT_KOUKI", "1", $extra, "");

        $query = knjd186qQuery::getHrClass();
        $result = $db->query($query);
        $opt_right = $opt_left = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();

        //対象
        $value = "";
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, $opt_right, $extra, 20);

        //生徒一覧
        $value = "";
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, $opt_left, $extra, 20);

        //移動ボタン
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD186Q");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "HREPORTREMARK_DAT_COMMUNICATION_SIZE_H" , $model->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"]);
        knjCreateHidden($objForm, "printSubclassLastChairStd", $model->Properties["printSubclassLastChairStd"]);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd186qForm1.html", $arg); 
    }
}
?>
