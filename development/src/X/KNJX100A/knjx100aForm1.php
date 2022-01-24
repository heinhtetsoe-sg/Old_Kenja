<?php

require_once('for_php7.php');

class knjx100aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx100aindex.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx100aQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\" onchange=\"btn_submit('');\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //DATADIVチェックボックス
        $check_datadiv = ($model->field["CHECK_DATADIV"] == "on") ? " checked" : "";
        $extra = "id=\"CHECK_DATADIV\"".$check_datadiv;
        $arg["data"]["CHECK_DATADIV"] = knjCreateCheckBox($objForm, "CHECK_DATADIV", "on", $extra);

        //DATADIVラベル
        if ($model->field["SHORI_MEI"] == "2") {
            $arg["data"]["LABEL_DATADIV"] = "テスト時間割も削除する";
        } else {
            $arg["data"]["LABEL_DATADIV"] = "テスト時間割も通常時間割として更新する";
        }

        $optnull    = array("label" => "(全て出力)","value" => "");   //初期値：空白項目

        //年度＆学期コンボ
        $optYear = array();
        $value_flg = false;
        $initFlg = false;
        $initValue = ""; //初期値
        $result = $db->query(knjx100aquery::getYearSemesterCombo());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optYear[] = array("label" => $row["YEAR"]."年度 ".$row["SEMESTERNAME"],
                               "value" => $row["YEAR"]."-".$row["SEMESTER"]);
            if ($model->field["YEAR_SEME"]  == $row["YEAR"]."-".$row["SEMESTER"]) {
                $value_flg = true;
            }
            if (CTRL_YEAR."-".CTRL_SEMESTER == $row["YEAR"]."-".$row["SEMESTER"]) {
                $initFlg = true;
            }
            $initValue = $row["YEAR"]."-".$row["SEMESTER"]; //MAX年度＆学期
        }
        $result->free();
        if ($initFlg) {
            $initValue = CTRL_YEAR."-".CTRL_SEMESTER;
        } //処理年度＆学期
        $model->field["YEAR_SEME"] = ($model->field["YEAR_SEME"] && $value_flg) ? $model->field["YEAR_SEME"] : $initValue;
        $extra = "onchange=\"btn_submit('');\"";
        $arg["data"]["YEAR_SEME"] = knjCreateCombo($objForm, "YEAR_SEME", $model->field["YEAR_SEME"], $optYear, $extra, 1);

        //実施日付コンボ
        $opt_date = array();
        $opt_date[] = $optnull;
        $value_flg = false;
        $result = $db->query(knjx100aquery::getExecutedateCombo($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_date[] = array("label" => str_replace("-", "/", $row["EXECUTEDATE"]),
                                "value" => $row["EXECUTEDATE"]);
            if ($model->field["EXECUTEDATE"] == $row["EXECUTEDATE"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $model->field["EXECUTEDATE"] = ($model->field["EXECUTEDATE"] && $value_flg) ? $model->field["EXECUTEDATE"] : $opt_date[0]["value"];
        $extra = "onchange=\"btn_submit('');\"";
        $arg["data"]["EXECUTEDATE"] = knjCreateCombo($objForm, "EXECUTEDATE", $model->field["EXECUTEDATE"], $opt_date, $extra, 1);

        //講座コンボ
        $opt_chaircd  = array();
        $opt_chaircd[] = $optnull;
        $value_flg = false;
        $result = $db->query(knjx100aquery::getChaircdCombo($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chaircd[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                                   "value" => $row["CHAIRCD"]);
            if ($model->field["CHAIRCD"] == $row["CHAIRCD"]) {
                $value_flg = true;
            }
        }
        $result->free();
        $model->field["CHAIRCD"] = ($model->field["CHAIRCD"] && $value_flg) ? $model->field["CHAIRCD"] : $opt_chaircd[0]["value"];
        $extra = "";
        $arg["data"]["CHAIRCD"] = knjCreateCombo($objForm, "CHAIRCD", $model->field["CHAIRCD"], $opt_chaircd, $extra, 1);

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
        $objForm->add_element(array("type"      => "file",
                                    "name"      => "FILE",
                                    "size"      => 1024000,
                                    "extrahtml" => "" ));

        $arg["FILE"] = $objForm->ge("FILE");

        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjx100aQuery::getSchoolCd());
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
        knjCreateHidden($objForm, "PRGID", "KNJX100A");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx100aForm1.html", $arg);
    }
}
