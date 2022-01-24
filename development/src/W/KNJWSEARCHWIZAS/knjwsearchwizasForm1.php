<?php

require_once('for_php7.php');

class knjwsearchwizasForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");
        //DB接続
        $db     = Query::dbCheckOut();

        //extraセット
        $extraInt = "onblur=\"this.value=toInteger(this.value)\";";

        //出力種別ラジオボタン
        $opt_shubetsu[0] = 1;        //志願者
        $opt_shubetsu[1] = 2;        //在籍者

        if ($model->dispData["search_div"] != "") {
            $model->search["SEARCH_DIV"] = ($model->search["SEARCH_DIV"] == "") ? "1" : $model->search["SEARCH_DIV"];
        } else {
            $model->search["SEARCH_DIV"] = ($model->dispData["serch2"] == "") ? "1" : $model->dispData["serch2"];
        }
        $extra = array("id=\"SEARCH_DIV1\"".$model->searchDivExtra, "id=\"SEARCH_DIV2\"".$model->searchDivExtra);
        $radioArray = createRadio($objForm, "SEARCH_DIV", $model->search["SEARCH_DIV"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //表示項目設定
        foreach ($model->dispData as $key => $val) {
            if ($model->dispData["search_div"] != "") {
                if (($key == "schno" && $model->search["SEARCH_DIV"] == "1") OR
                    ($key == "appno" && $model->search["SEARCH_DIV"] == "2")) {
                    continue;
                }
            }
            $arg[$key] = $val;
        }

        //指導要録
        if ($model->dispData["rec_get_flg"]) {
            $opt_recget[0] = 1; //未
            $opt_recget[1] = 2; //済

            $model->search["REC_GET_FLG"] = ($model->search["REC_GET_FLG"] == "") ? "1" : $model->search["REC_GET_FLG"];
            $extra = array("id=\"REC_GET_FLG1\"", "id=\"REC_GET_FLG2\"");
            $radioArray = createRadio($objForm, "REC_GET_FLG", $model->search["REC_GET_FLG"], $extra, $opt_recget, get_count($opt_recget));
            foreach($radioArray as $key => $val) $arg[$key] = $val;
        }

        //検索口座区分ラジオボタン
        $opt_account[0] = 1;        //全て
        $opt_account[1] = 2;        //未使用

        $model->search["SEARCH_ACCOUNT"] = ($model->search["SEARCH_ACCOUNT"] == "") ? "1" : $model->search["SEARCH_ACCOUNT"];
        $extra = array("id=\"SEARCH_ACCOUNT1\"", "id=\"SEARCH_ACCOUNT2\"");
        $radioArray = createRadio($objForm, "SEARCH_ACCOUNT", $model->search["SEARCH_ACCOUNT"], $extra, $opt_account, get_count($opt_account));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //支店
        $query = knjwsearchwizasQuery::getBranch();
        makeCombo($objForm, $arg, $db, $query, $model->search["VIRTUAL_BANK_CD"], "VIRTUAL_BANK_CD", "", 1, "BLANK");

        //仮想口座番号From
        $arg["F_VIRTUAL_ACCOUNT_NO"] = createTextBox($objForm, $model->search["F_VIRTUAL_ACCOUNT_NO"], "F_VIRTUAL_ACCOUNT_NO", 7, 7, $extraInt);

        //仮想口座番号To
        $arg["T_VIRTUAL_ACCOUNT_NO"] = createTextBox($objForm, $model->search["T_VIRTUAL_ACCOUNT_NO"], "T_VIRTUAL_ACCOUNT_NO", 7, 7, $extraInt);

        //処理年度
        $query = knjwsearchwizasQuery::getExeYear();
        //志願者が選択されていたら使用不可
        if ($model->dispData["search_div"]) {
            $disabled = $model->search["SEARCH_DIV"] == 1 ? "disabled" : "";
            if ($model->search["SEARCH_DIV"] == 1) $model->search["EXE_YEAR"] = "";
        } else {
            $disabled = "";
        }
        $blank = $model->dispData["exe_year"] == 1 ? "BLANK" : "";
        makeCombo($objForm, $arg, $db, $query, $model->search["EXE_YEAR"], "EXE_YEAR", $disabled, 1, $blank);

        //入学年度
        $query = knjwsearchwizasQuery::getEntYear();
        //在籍者が選択されていたら使用不可
        if ($model->dispData["search_div"]) {
            $disabled = $model->search["SEARCH_DIV"] == 2 ? "disabled" : "";
            if ($model->search["SEARCH_DIV"] == 2) $model->search["YEAR"] = "";
        } else {
            $disabled = "";
        }
        makeCombo($objForm, $arg, $db, $query, $model->search["YEAR"], "YEAR", $disabled, 1, "BLANK");

        //卒業区分
        $query = knjwsearchwizasQuery::getGrdDiv();
        makeCombo($objForm, $arg, $db, $query, $model->search["GRD_DIV"], "GRD_DIV", "", 1, "BLANK");

        //受験区分
        $query = knjwsearchwizasQuery::getApplicantDiv();
        makeCombo($objForm, $arg, $db, $query, $model->search["APPLICANT_DIV"], "APPLICANT_DIV", "", 1, "BLANK");

        //受験区分ラジオボタン
        $opt_applicant[0] = 1;        //転編入
        $opt_applicant[1] = 2;        //転入
        $opt_applicant[2] = 3;        //編入

        $model->search["APPLICANT"] = ($model->search["APPLICANT"] == "") ? "1" : $model->search["APPLICANT"];
        $extra = array("id=\"APPLICANT1\"", "id=\"APPLICANT2\"", "id=\"APPLICANT3\"");
        $radioArray = createRadio($objForm, "APPLICANT", $model->search["APPLICANT"], $extra, $opt_applicant, get_count($opt_applicant));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //手続区分
        $query = knjwsearchwizasQuery::getProcedure();
        makeCombo($objForm, $arg, $db, $query, $model->search["PROCEDURE_DIV"], "PROCEDURE_DIV", "", 1, "BLANK");

        //学籍番号
        $arg["SCHREGNO"] = createTextBox($objForm, $model->search["SCHREGNO"], "SCHREGNO", 8, 8, $extraInt);
        
        //学籍番号範囲
        $arg["SCHREGNO1"] = createTextBox($objForm, $model->search["SCHREGNO1"], "SCHREGNO1", 8, 8, $extraInt);
        $arg["SCHREGNO2"] = createTextBox($objForm, $model->search["SCHREGNO2"], "SCHREGNO2", 8, 8, $extraInt);

        //志願者番号
        $arg["APPLICANTNO"] = createTextBox($objForm, $model->search["APPLICANTNO"], "APPLICANTNO", 7, 7, $extraInt);

        //支払区分
        $query = knjwsearchwizasQuery::getPayment();
        makeCombo($objForm, $arg, $db, $query, $model->search["MANNER_PAYMENT"], "MANNER_PAYMENT", "", 1, "BLANK");
        
        //入金区分
        $query = knjwsearchwizasQuery::getMisyukin();
        makeCombo($objForm, $arg, $db, $query, $model->search["PAYMENT_MONEY"], "PAYMENT_MONEY", "", 1, "BLANK");

        //伝票番号
        $arg["SLIP_NO"] = createTextBox($objForm, $model->search["SLIP_NO"], "SLIP_NO", 8, 8, $extraInt);

        //受験・入学区分
        if ($model->search["SEARCH_DIV"] == "1") {
            $query = knjwsearchwizasQuery::getApplicantDiv();
            $arg["ENT_APP_TITLE"] = "受験区分";
        } else {
            $query = knjwsearchwizasQuery::getEntDiv();
            $arg["ENT_APP_TITLE"] = "入学区分";
        }
        makeCombo($objForm, $arg, $db, $query, $model->search["ENT_APP_DIV"], "ENT_APP_DIV", "", 1, "BLANK");

        //氏名
        $arg["NAME"] = createTextBox($objForm, $model->search["NAME"], "NAME", 40, 40, "");

        //かな氏名
        $arg["NAME_KANA"] = createTextBox($objForm, $model->search["NAME_KANA"], "NAME_KANA", 40, 40, "");

        //性別
        $query = knjwsearchwizasQuery::getSex($model);
        makeCombo($objForm, $arg, $db, $query, $model->search["SEX"], "SEX", "", 1, "BLANK");

        //未収金
        $query = knjwsearchwizasQuery::getMisyukin();
        makeCombo($objForm, $arg, $db, $query, $model->search["MISYUKIN"], "MISYUKIN", "", 1, "BLANK");

        //所属
        $query = knjwsearchwizasQuery::getBelong($model);
        makeCombo($objForm, $arg, $db, $query, $model->search["BELONGING_DIV"], "BELONGING_DIV", "", 1, "BLANK");

        //学生区分
        $query = knjwsearchwizasQuery::getStuDiv();
        makeCombo($objForm, $arg, $db, $query, $model->search["STUDENT_DIV"], "STUDENT_DIV", "", 1, "BLANK");

        //コース
        $query = knjwsearchwizasQuery::getCourse();
        makeCombo($objForm, $arg, $db, $query, $model->search["COURSE"], "COURSE", "", 1, "BLANK");

        //年次
        $query = knjwsearchwizasQuery::getAnnual();
        makeCombo($objForm, $arg, $db, $query, $model->search["ANNUAL"], "ANNUAL", "", 1, "BLANK");

        //卒業予定月
        $query = knjwsearchwizasQuery::getGrdMonth();
        makeCombo($objForm, $arg, $db, $query, $model->search["GRD_MONTH"], "GRD_MONTH", "", 1, "BLANK");

        //卒業予定日
        $arg["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE", str_replace("-", "/", $model->search["GRD_DATE"]),"");

        //卒業予定月
        $query = knjwsearchwizasQuery::getGrdShow();
        makeCombo($objForm, $arg, $db, $query, $model->search["GRD_SHOW"], "GRD_SHOW", "", 1, "BLANK");

        //卒業要件ラジオボタン
        $opt_grd_require[0] = 1;        //卒業
        $opt_grd_require[1] = 2;        //保留

        $model->search["GRD_REQUIRE"] = ($model->search["GRD_REQUIRE"] == "") ? "1" : $model->search["GRD_REQUIRE"];
        $extra = array("id=\"GRD_REQUIRE1\"", "id=\"GRD_REQUIRE2\"");
        $radioArray = createRadio($objForm, "GRD_REQUIRE", $model->search["GRD_REQUIRE"], $extra, $opt_grd_require, get_count($opt_grd_require));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //提携先
        $query = knjwsearchwizasQuery::getTeikei();
        makeCombo($objForm, $arg, $db, $query, $model->search["TEIKEI"], "TEIKEI", "", 1, "BLANK");

        //科目名
        $query = knjwsearchwizasQuery::getSubclass();
        makeCombo($objForm, $arg, $db, $query, $model->search["SUBCLASS"], "SUBCLASS", "", 1, "BLANK");

        //レポート回数
        $query = knjwsearchwizasQuery::getReport();
        makeCombo($objForm, $arg, $db, $query, $model->search["REPORT"], "REPORT", "", 1, "BLANK");

        //入力済／未入力
        $query = knjwsearchwizasQuery::getInport();
        makeCombo($objForm, $arg, $db, $query, $model->search["INPORT"], "INPORT", "", 1, "BLANK");

        //出席状況
        $query = knjwsearchwizasQuery::getAttendInfo();
        makeCombo($objForm, $arg, $db, $query, $model->search["ATTEND_INFO"], "ATTEND_INFO", "", 1, "BLANK");

        //スクーリング
        $query = knjwsearchwizasQuery::getSchooling();
        makeCombo($objForm, $arg, $db, $query, $model->search["SCHOOLING_DIV"], "SCHOOLING_DIV", "", 1);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //生徒リスト表示
        if ($model->cmd == "search") {
            makeStudentList($objForm, $arg, $db, $model);
            $model->firstFlg = false;
            $arg["search"] = 1;
        } else if ($model->cmd == "search2") {
            $arg["reload"] = "Link2('".REQUESTROOT."')";
        }

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->searchBack == "back") {
            echo "aa";
            $arg["jscript"] = "btn_submit('search')";
        }

        View::toHTML($model, "knjwsearchwizasForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();
//    if ($name == "EXE_YEAR") {
//        $value = ($value != "") ? $value : CTRL_YEAR;
//    } else {
        $value = ($value != "") ? $value : $opt[0]["value"];
//    }
    $arg[$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒リスト表示
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    $arg["INFOTITLE"] = $model->search["SEARCH_DIV"] == "1" ? "志願者番号" : "学籍番号";
    $result = $db->query(knjwsearchwizasQuery::GetStudents($model));
    $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
    $i = 0;
    list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);

    while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["NAME"] = $model->search["SEARCH_DIV"] == "1" ? $row["AP_B_NAME"] : $row["SCH_B_NAME"];
        $a = array("cmd"         => $cmd,
                   "APPLICANTNO" => $row["APPLICANTNO"],
                   "SCHREGNO"    => $row["SCHREGNO"],
                   "SLIP_NO"     => $row["SLIP_NO"],
                   "NAME"        => $row["NAME"],
                   "SEARCH_DIV"  => $model->search["SEARCH_DIV"]);
        $row["NAME"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"",$a);
        $row["INFONO"] = $model->search["SEARCH_DIV"] == "1" ? $row["APPLICANTNO"] : $row["SCHREGNO"];
        $row["IMAGE"] = $image[($row["SEX"]-1)];
        $arg["data"][] = $row;
        $i++;
    }
    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //検索ボタンを作成する
    if ($model->searchMode) {
        $extra = "onclick=\"btn_submit('search2')\"";
    } else {
        $extra = "onclick=\"btn_submit('search')\"";
    }
    $arg["btn_search"] = createBtn($objForm, "btn_search", "検　索", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    createHidden($objForm, "cmd");
    createHidden($objForm, "path", REQUESTROOT .$model->path[$model->programid]);
    createHidden($objForm, "PROGRAMID", $model->programid);
    createHidden($objForm, "searchMode", $model->searchMode);

    list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
    createHidden($objForm, "right_path", $path);

    foreach ($model->dispData as $key => $val) {
        createHidden($objForm, $key, $val);
    }
}

?>
