<?php

require_once('for_php7.php');

class knjb1602Form1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb1602index.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //親画面なし
        $securityCnt = $db->getOne(knjb1602Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //処理名コンボボックス
        $opt_shori   = array();
        $opt_shori[] = array("label" => "更新","value" => "1");
        $opt_shori[] = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //年度一覧コンボボックス（講座別）
        $optnull  = array("label" => "(全て出力)","value" => "");   //初期値：空白項目
        $result   = $db->query(knjb1602query::getSelectYear());   
        $opt_year = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"]."年度 ".$row["SEMESTERNAME"], 
                                "value" => $row["YEAR"].$row["SEMESTER"]);
        }
        $result->free();
        $model->field["YEAR"] = ($model->field["YEAR"] == "") ? CTRL_YEAR.CTRL_SEMESTER : $model->field["YEAR"];
        $extra = "onchange=\"btn_submit('');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, $extra, 1);

        //講座一覧コンボボックス
        $result = $db->query(knjb1602query::getSelectChair($model));   
        $opt_chaircd = array();
        $opt_chaircd[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chaircd[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"], 
                                   "value" => $row["CHAIRCD"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["CHAIRCD"] = knjCreateCombo($objForm, "CHAIRCD", $model->field["CHAIRCD"], $opt_chaircd, $extra, 1);

        //年度一覧コンボボックス（個人別）
        $result   = $db->query(knjb1602query::getSelectYear());   
        $opt_year2 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year2[] = array("label" => $row["YEAR"]."年度 ".$row["SEMESTERNAME"], 
                                "value" => $row["YEAR"].$row["SEMESTER"]);
        }
        $result->free();
        $model->field["YEAR2"] = ($model->field["YEAR2"] == "") ? CTRL_YEAR.CTRL_SEMESTER : $model->field["YEAR2"];
        $extra = "onchange=\"btn_submit('');\"";
        $arg["data"]["YEAR2"] = knjCreateCombo($objForm, "YEAR2", $model->field["YEAR2"], $opt_year2, $extra, 1);

        //年組一覧コンボボックス
        $result = $db->query(knjb1602query::getSelectHrclass($model));   
        $opt_hrclass = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_hrclass[] = array("label" => $row["HR_NAME"], 
                                   "value" => $row["GRADE"].$row["HR_CLASS"]);
        }
        $result->free();
        $model->field["GRADE_HR_CLASS"] = ($model->field["GRADE_HR_CLASS"] == "") ? $opt_hrclass[0]["value"] : $model->field["GRADE_HR_CLASS"];
        $extra = "onchange=\"btn_submit('');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $opt_hrclass, $extra, 1);

        //生徒一覧コンボボックス
        $result = $db->query(knjb1602query::getSelectStudent($model));   
        $opt_student = array();
        $opt_student[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_student[] = array("label" => $row["ATTENDNO"]."番 ".$row["NAME_SHOW"], 
                                   "value" => $row["SCHREGNO"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["STUDENT"] = knjCreateCombo($objForm, "STUDENT", $model->field["STUDENT"], $opt_student, $extra, 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //出力取込種別ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力（講座別） 5:データ出力（個人別）)
        $opt = array(1, 2, 3, 4, 5);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"", "id=\"OUTPUT5\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjb1602query::getSchoolCd());
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
        knjCreateHidden($objForm, "PRGID", "KNJB1602");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
 
        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1602Form1.html", $arg);
    }
}
?>
