<?php

require_once('for_php7.php');

class knjx231Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx231index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx231Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //出力取込種別ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //年度一覧コンボボックス
        $result     = $db->query(knjx231query::getSelectFieldSQL());
        $opt_year   = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"]."年度 ".$row["SEMESTERNAME"],
                                "value" => $row["YEAR"].$row["SEMESTER"]);
        }
        $result->free();
        $model->field["YEAR"] = ($model->field["YEAR"] == "") ? CTRL_YEAR.CTRL_SEMESTER : $model->field["YEAR"];
        $extra = "onchange=\"btn_submit('');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, $extra, 1);

        //年組一覧コンボボックス
        $result      = $db->query(knjx231query::getSelectFieldSQL2($model));
        $opt_gr_hr   = array();
        $opt_gr_hr[] = array("label" => "(全て出力)", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_gr_hr[] = array("label" => $row["HR_NAME"],
                                 "value" => $row["GRADE"].$row["HR_CLASS"]);
        }
        $result->free();
        $model->field["GRADE_HR_CLASS"] = ($model->field["GRADE_HR_CLASS"] == "") ? $opt_hrclass[0]["value"] : $model->field["GRADE_HR_CLASS"];
        $extra = "onchange=\"btn_submit('');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $opt_gr_hr, $extra, 1);

        //生徒一覧コンボボックス
        $result      = $db->query(knjx231query::getSelectFieldSQL3($model));
        $opt_student   = array();
        $opt_student[] = array("label" => "(全て出力)", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_student[] = array("label" => $row["ATTENDNO"]."番 ".$row["NAME_SHOW"],
                                   "value" => $row["SCHREGNO"]);
        }
        $result->free();
        $arg["data"]["STUDENT"] = knjCreateCombo($objForm, "STUDENT", $model->field["STUDENT"], $opt_student, "", 1);

        //科目一覧コンボボックス
        $result      = $db->query(knjx231query::getSelectFieldSQL4($model));
        $opt_subclass   = array();
        $opt_subclass[] = array("label" => "(全て出力)", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_subclass[] = array("label" => $row["SUBCLASSCD"].' '.$row["SUBCLASSNAME"],
                                     "value" => $row["SUBCLASSCD"]);
        }
        $result->free();
        $extra = "onchange=\"btn_submit('');\"";
        $arg["data"]["SUBCLASS"] = knjCreateCombo($objForm, "SUBCLASS", $model->field["SUBCLASS"], $opt_subclass, $extra, 1);

        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjx231Query::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            //今年度・今学期名及びタイトルの表示
            $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　エクセル出力／ＣＳＶ取込";
        } else {
            $extra = "onclick=\"return btn_submit('exec');\"";
            //今年度・今学期名及びタイトルの表示
            $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";
        }
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJX231");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->virus);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        $fieldName = array();
        foreach ($model->fieldSize as $key => $val) {
            $fieldName[] = $key;
        }
        knjCreateHidden($objForm, "XLS_FIELDNAME", $fieldName);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx231Form1.html", $arg);
    }
}
