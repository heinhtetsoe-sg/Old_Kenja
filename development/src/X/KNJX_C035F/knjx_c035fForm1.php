<?php

require_once('for_php7.php');

class knjx_c035fForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        if ($model->Properties["useXLS"]) {
            $arg["TITLE"] = "".CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　エクセル出力／ＣＳＶ取込 画面";
        } else {
            $arg["TITLE"] = "".CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込 画面";
        }

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_c035findex.php", "", "main");

        //権限チェック
        if (!($model->auth == DEF_UPDATABLE || $model->auth == DEF_UPDATE_RESTRICT)) {
            $arg["jscript"] = "OnAuthError();";
        }

        //事前チェック（出欠管理者コントロール）
        if (get_count($model->item_array) == 0) {
            $arg["jscript"] = "preCheck();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx_c035fQuery::getSecurityHigh($model));
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        $height = "30";
        $flg = false;

        //校種
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種コンボ
            $query = knjx_c035fQuery::getNameMstA023($model);
            // Edit by PP for PC-Talker 2020-01-20 start
            $extra = "id=\"SCHOOL_KIND\" onChange=\"current_cursor('SCHOOL_KIND'); btn_submit('')\"; aria-label=\"\"";
            // Edit by PP for PC-Talker 2020-01-31 end
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
            $height = "60";
            $flg = true;
            $arg["BR_SK"] = "<br>";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $model->field["SCHOOL_KIND"] = SCHOOLKIND;
        }

        //課程学科コンボ
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query = knjx_c035fQuery::getCourseMajor($model, $model->field["SCHOOL_KIND"]);
            $extra = "onChange=\"btn_submit('')\";";
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1);
            $height = "60";
            $arg["BR_CM"] = ($flg) ? "　　" : "<br>";
        }

        //サイズ調整
        $arg["HEIGHT"] = $height;

        //処理名コンボボックス
        $opt_shori = array();
        $opt_shori[] = array("label" => "更新","value" => "1");
        $opt_shori[] = array("label" => "削除","value" => "2");
        // Edit by PP for PC-Talker 2020-01-20 start
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\" aria-label=\"データ取込 のラジオボタンの処理名\"", $size);
        // Edit by PP for PC-Talker 2020-01-31 end

        //ヘッダ有チェックボックス
        $extra  = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt_shubetsu = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        //年度一覧コンボボックス
        $query = knjx_c035fQuery::getSelectFieldSQL($model);
        // Edit by PP for PC-Talker 2020-01-20 start
        $extra = "id=\"YEAR\" onchange=\"current_cursor('YEAR'); return btn_submit('');\" aria-label=\"年度と学期\"";
        // Edit by PP for PC-Talker 2020-01-31 end
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //科目コンボ
        $query = knjx_c035fQuery::getSubclasscd($model);
        // Edit by PP for PC-Talker 2020-01-20 start
        $extra = "id=\"SUBCLASSCD\" onChange=\"current_cursor('SUBCLASSCD'); btn_submit('')\"; aria-label=\"科目\"";
        // Edit by PP for PC-Talker 2020-01-31 end
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1);

        //講座コンボ
        $query = knjx_c035fQuery::getChaircd($model);
        // Edit by PP for PC-Talker 2020-01-20 start
        $extra = "aria-label=\"講座\"";
        // Edit by PP for PC-Talker 2020-01-31 end
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1);

        //対象月コンボボックス
        makeMonthSemeCmb($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJX_C035F");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_c035fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    if ($name == 'CHAIRCD') {
        $opt[] = array('label' => '-全て-', 'value' => 'all');
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR.CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query = knjx_c035fQuery::selectSemesAll($model->field["YEAR"]);
    $result = $db->query($query);
    $data = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month = setMonth($db, $model, $data);

    // Edit by PP for PC-Talker 2020-01-20 start
    $extra = "aria-label=\"対象月\"";
    // Edit by PP for PC-Talker 2020-01-31 end
    $arg["data"]["MONTHCD"] = knjCreateCombo($objForm, "MONTHCD", $model->field["MONTHCD"], $opt_month, $extra, 1);

    return $data;
}

//学期・月データ取得
function setMonth($db, $model, $data)
{
    $opt_month = array();
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query = knjx_c035fQuery::selectMonthQuery(substr($model->field["YEAR"], 0, 4), $month, $model);
            $getdata = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
            }
        }
    }
    return $opt_month;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $db, $model)
{
    //実行ボタン
    if ($model->Properties["useXLS"]) {
        $model->schoolCd = $db->getOne(knjx_c035fQuery::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"  aria-label=\"実行\"";
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　エクセル出力／ＣＳＶ取込";
    } else {
        $extra = "id=\"btn_exec\" onclick=\"current_cursor('btn_exec');return btn_submit('exec');\" aria-label=\"実行\"";
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";
    }
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    if ($model->getPrgId) {
        $extra = "onclick=\" window.opener.btn_submit('main'); closeWin();\" aria-label=\"終了\"";
    } else {
        $extra = "onclick=\"closeWin();\" aria-label=\"終了\"";
    }
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
