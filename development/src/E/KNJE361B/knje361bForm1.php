<?php

require_once('for_php7.php');

class knje361bForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje361bindex.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knje361bQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "新規","value" => "1");
        $opt_shori[]    = array("label" => "更新","value" => "2");
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

        //既卒も含むチェックボックス
        $extra  = " id=\"KISOTU\"";
        $extra .= $model->field["KISOTU"] == "on" ? " checked" : "";
        $arg["data"]["KISOTU"] = knjCreateCheckBox($objForm, "KISOTU", "on", $extra);
        
        //学内推薦チェックボックス
        $extra  = " id=\"SUISEN\"";
        $extra .= $model->field["SUISEN"] == "on" ? " checked" : "";
        $arg["data"]["SUISEN"] = knjCreateCheckBox($objForm, "SUISEN", "on", $extra);

        //年度一覧コンボ
        $query = knje361bquery::getYearSeme();
        $extra = "onchange=\"btn_submit('');\"";
        $model->field["YEAR"] = $model->field["YEAR"] ? $model->field["YEAR"] : CTRL_YEAR.CTRL_SEMESTER;
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "");

        //大学コンボ 指定大学1
        $query = knje361bquery::getCollege();
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_CD"], "SCHOOL_CD", $extra, 1, "BLANK");

        //学部コンボ 指定大学1
        $query = knje361bquery::getFaculty($model->field["SCHOOL_CD"]);
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["FACULTYCD"], "FACULTYCD", $extra, 1, "BLANK");

        //学科コンボ 指定大学1
        $query = knje361bquery::getDepartment($model->field["SCHOOL_CD"], $model->field["FACULTYCD"]);
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["DEPARTMENTCD"], "DEPARTMENTCD", $extra, 1, "BLANK");

        //大学コンボ 指定大学2
        $query = knje361bquery::getCollege();
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_CD2"], "SCHOOL_CD2", $extra, 1, "BLANK");

        //学部コンボ 指定大学2
        $query = knje361bquery::getFaculty($model->field["SCHOOL_CD2"]);
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["FACULTYCD2"], "FACULTYCD2", $extra, 1, "BLANK");

        //学科コンボ 指定大学2
        $query = knje361bquery::getDepartment($model->field["SCHOOL_CD2"], $model->field["FACULTYCD2"]);
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["DEPARTMENTCD2"], "DEPARTMENTCD2", $extra, 1, "BLANK");

        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE361B");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje361bForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    $extra = "onclick=\"return btn_submit('exec');\"";
    //今年度・今学期名及びタイトルの表示
    $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
