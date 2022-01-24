<?php

require_once('for_php7.php');

class knjx_c031fForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_c031findex.php", "", "main");

        //権限チェック
        if (!($model->auth == DEF_UPDATABLE || $model->auth == DEF_UPDATE_RESTRICT)) {
            $arg["jscript"] = "OnAuthError();";
        }

        //事前チェック（出欠管理者コントロール）
        if (get_count($model->item_array) == 0) {
            $arg["jscript"] = "preCheck();";
        }
        if (get_count($model->item_array2) == 0) {
            $arg["jscript"] = "preCheck();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx_c031fQuery::getSecurityHigh($model));
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        $height = "30";
        $flg = false;

        //校種
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種コンボ
            $query = knjx_c031fQuery::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "");
            $height = "60";
            $flg = true;
            $arg["BR_SK"] = "<br>";
        }

        //課程学科
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query = knjx_c031fQuery::getCourseMajor($model);
            $extra = "onChange=\"btn_submit('')\";";
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1);
            $height = "60";
            $arg["BR_CM"] = ($flg) ? "　　" : "<br>";
        }

        //サイズ調整
        $arg["HEIGHT"] = $height;

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", $size);

        //ヘッダ有チェックボックス
        $extra  = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt_shubetsu = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        //年組コンボ内容ラジオボタン 1:HRクラス 2:複式クラス
        $opt = array(1, 2);
        $model->field["SELECT_CLASS_TYPE"] = ($model->field["SELECT_CLASS_TYPE"] == "") ? "1" : $model->field["SELECT_CLASS_TYPE"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, "onChange=\"btn_submit('change_radio')\"; id=\"SELECT_CLASS_TYPE{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SELECT_CLASS_TYPE", $model->field["SELECT_CLASS_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年組コンボ内容ラジオボタン表示
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1) $arg["class_type"] = 1;

        //年度一覧コンボボックス
        $query = knjx_c031fQuery::getSelectFieldSQL($model);
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            //複式学級コンボボックス
            $query = knjx_c031fQuery::getGroupHrClass($model);
            makeCmb($objForm, $arg, $db, $query, "GROUP_HR_CLASS", $model->field["GROUP_HR_CLASS"], "", 1);
        } else {
            //年組一覧コンボボックス
            $query = knjx_c031fQuery::getSelectFieldSQL2($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], "", 1);
        }

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
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_c031fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
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
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model) {
    $query = knjx_c031fQuery::selectSemesAll($model->field["YEAR"]);
    $result = $db->query($query);
    $data = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month  = array();
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $year = substr($model->field["YEAR"],0,4);
            $getdata = $db->getRow(knjx_c031fQuery::selectMonthQuery($year, $month, $model), DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
            }
        }
    }
    if($model->field["MONTH"] == "" || $model->field["MONTH"] == NULL){
        $model->field["MONTH"] = "";
    }

    $arg["data"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->field["MONTH"], $opt_month, "", 1);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $db, $model) {
    //実行ボタン
    if ($model->Properties["useXLS"]) {
        $model->schoolCd = $db->getOne(knjx_c031fQuery::getSchoolCd());
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
    if ($model->getPrgId) {
        $extra = "onclick=\"window.opener.btn_submit('main');closeWin();\"";
    } else {
        $extra = "onclick=\"closeWin();\"";
    }
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
