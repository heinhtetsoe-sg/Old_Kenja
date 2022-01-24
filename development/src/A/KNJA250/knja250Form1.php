<?php

require_once('for_php7.php');


class knja250Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja250Form1", "POST", "knja250index.php", "", "knja250Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //hidden
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学年リストボックスを作成する
        $db = Query::dbCheckOut();
        $opt_grade=array();
        $query = knja250Query::getSelectGrade($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grade[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
        if($model->field["GAKUNEN"]=="") $model->field["GAKUNEN"] = $opt_grade[0]["value"];
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKUNEN",
                            "size"       => get_count($opt_grade),
                            "value"      => $model->field["GAKUNEN"],
                            "options"    => $opt_grade,
                            "extrahtml"  => "multiple" ) );

        $arg["data"]["GAKUNEN"] = $objForm->ge("GAKUNEN");

        //異動日付チェックボックスを作成
        $extra = "onclick=\" return datecheck(this)\" id=\"OUTPUT\"";
        $arg["data"]["OUTPUT"] = knjCreateCheckBox($objForm, "OUTPUT", "on", $extra);

        //異動終了日付
        $year = CTRL_YEAR+1;
        $arg["data"]["DATE"] = View::popUpCalendar($objForm,"DATE",str_replace("-","/",$year."/03/31"));

        //全てチェックボックスを作成
        $extra = "onclick=\" return allcheck(this)\" id=\"ALLCHECK\"";
        $arg["data"]["ALLCHECK"] = knjCreateCheckBox($objForm, "ALLCHECK", "on", $extra);

        //異動区分チェックボックスを作成（卒業）
        $extra = "id=\"GRAD\"";
        $arg["data"]["GRAD"] = knjCreateCheckBox($objForm, "GRAD", "on", $extra);

        //異動区分チェックボックスを作成（転学）
        $extra = "id=\"MOVE\"";
        $arg["data"]["MOVE"] = knjCreateCheckBox($objForm, "MOVE", "on", $extra);

        //異動区分チェックボックスを作成（退学）
        $extra = "id=\"DROP\"";
        $arg["data"]["DROP"] = knjCreateCheckBox($objForm, "DROP", "on", $extra);

        //異動区分チェックボックスを作成（留学）
        $extra = "id=\"FOREIGN\"";
        $arg["data"]["FOREIGN"] = knjCreateCheckBox($objForm, "FOREIGN", "on", $extra);

        //異動区分チェックボックスを作成（休学）
        $extra = "id=\"HOLI\"";
        $arg["data"]["HOLI"] = knjCreateCheckBox($objForm, "HOLI", "on", $extra);

        //異動区分チェックボックスを作成（出停）
        $extra = "id=\"SUSPEND\"";
        $arg["data"]["SUSPEND"] = knjCreateCheckBox($objForm, "SUSPEND", "on", $extra);

        //異動区分チェックボックスを作成（編入）
        $extra = "id=\"ADMISSION\"";
        $arg["data"]["ADMISSION"] = knjCreateCheckBox($objForm, "ADMISSION", "on", $extra);

        //異動区分チェックボックスを作成（転入）
        $extra = "id=\"MOVINGIN\"";
        $arg["data"]["MOVINGIN"] = knjCreateCheckBox($objForm, "MOVINGIN", "on", $extra);

        //異動区分チェックボックス(除籍)
        $extra = "id=\"REMOVE\"";
        $arg["data"]["REMOVE"] = knjCreateCheckBox($objForm, "REMOVE", "on", $extra);

        //異動区分チェックボックス(転科出)
        $extra = "id=\"TENKA_O\"";
        $arg["data"]["TENKA_O"] = knjCreateCheckBox($objForm, "TENKA_O", "on", $extra);

        //異動区分チェックボックス(転科入)
        $extra = "id=\"TENKA_I\"";
        $arg["data"]["TENKA_I"] = knjCreateCheckBox($objForm, "TENKA_I", "on", $extra);

        //異動区分チェックボックス(転籍)
        $query = knja250Query::getA003_7();
        $a003_7Cnt = $db->getOne($query);
        if ($a003_7Cnt > 0) {
            $extra = "id=\"TENSEKI_O\"";
            $arg["data"]["TENSEKI_O"] = knjCreateCheckBox($objForm, "TENSEKI_O", "on", $extra);
            $extra = "id=\"TENSEKI_I\"";
            $arg["data"]["TENSEKI_I"] = knjCreateCheckBox($objForm, "TENSEKI_I", "on", $extra);
        }

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA250");
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja250Form1.html", $arg); 
    }
}
?>
