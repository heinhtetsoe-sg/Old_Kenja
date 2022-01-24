<?php

require_once('for_php7.php');

class knjx090Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx090index.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx090Query::getSecurityHigh());
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

        $optnull = array("label" => "(全て出力)","value" => "");   //初期値：空白項目

        //年度一覧コンボボックス
        $result = $db->query(knjx090query::getSelectFieldSQL($model));
        $opt_year = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"]."年度 ".$row["SEMESTERNAME"],
                                "value" => $row["YEAR"].$row["SEMESTER"]);
        }
        $result->free();
        $model->field["YEAR"] = $model->field["YEAR"] == "" ? CTRL_YEAR.CTRL_SEMESTER : $model->field["YEAR"];
        $extra = "onchange=\"btn_submit('main');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, $extra, 1);

        //講座一覧コンボボックス
        $result = $db->query(knjx090query::getSelectFieldSQL2($model));
        $opt_chaircd = array();
        $opt_chaircd[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chaircd[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                                   "value" => $row["CHAIRCD"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["CHAIRCD"] = knjCreateCombo($objForm, "CHAIRCD", $model->field["CHAIRCD"], $opt_chaircd, $extra, 1);

        //学籍番号一覧コンボボックス
        $result     = $db->query(knjx090query::getSelectFieldSQL3($model));
        $opt_schno  = array();
        $opt_schno[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_schno[] = array("label" => $row["SCHREGNO"]." ".$row["NAME"],
                                 "value" => $row["SCHREGNO"]);
        }
        $result->free();
        $extra = "style=\"width:250px;\"";
        $arg["data"]["SCHREGNO"] = knjCreateCombo($objForm, "SCHREGNO", $model->field["SCHREGNO"], $opt_schno, $extra, 1);

        //適用開始日付・終了日付一覧コンボボックス 04/11/25Add
        $result     = $db->query(knjx090query::getSelectFieldSQL4($model));
        $opt_appdate  = array();
        $opt_appdate[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-", "/", $row);
            $opt_appdate[] = array("label" => $row["APPDATE"]." ～ ".$row["APPENDDATE"],
                                      "value" => $row["APPDATE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["APPDATE"] = knjCreateCombo($objForm, "APPDATE", $model->field["APPDATE"], $opt_appdate, $extra, 1);

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
                                    "size"      => 5120000,
                                    "extrahtml" => "" ));

        $arg["FILE"] = $objForm->ge("FILE");

        //CSV出力文言
        $arg["OUTPUT_CSV"] = $model->Properties["useXLS"] ? "" : "1";

        //学年一覧コンボボックス
        $result     = $db->query(knjx090query::getSelectFieldSQL5($model));
        $opt_grade  = array();
        $opt_grade[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_grade[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt_grade, $extra, 1);

        // 「HRクラスの自動名簿生成」ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_cre",
                            "value"     => "HRクラスの自動名簿生成",
                            "extrahtml" => "onclick=\"return btn_submit('csv2');\"" ));

        $arg["btn_cre"] = $objForm->ge("btn_cre");

        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjx090Query::getSchoolCd());
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
        knjCreateHidden($objForm, "PRGID", "KNJX090");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx090Form1.html", $arg);
    }
}
