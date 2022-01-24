<?php

require_once('for_php7.php');

class knjd128xForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd128xindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjd128xQuery::getSecurityHigh($model));
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //校種コンボ
        if ($model->Properties["useSchool_KindField"] == "1") {
            $extra = " onchange=\"return btn_submit2('changeKind');\" ";
            $query = knjd128xQuery::getSchkind($model);
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
        }

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt_shubetsu = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期コンボ
        $model->field["SEMESTER"] = ($model->field["SEMESTER"] == "") ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $extra = " id = \"SEMESTER\" onchange=\"return btn_submit('');\"";
        $query = knjd128xQuery::getSemesterList($model);
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //テスト種別コンボ (ヘッダ出力用)
        $extra = "";
        $query = knjd128xQuery::getTestitem($db, $model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD1", $model->field["TESTKIND_ITEMCD1"], $extra, 1, "ALL");

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", $size);

        //テスト種別コンボ (データ取込用)
        $extra = "";
        $query = knjd128xQuery::getTestitem($db, $model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD2", $model->field["TESTKIND_ITEMCD2"], $extra, 1);

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        //年度一覧コンボボックス
        $query = knjd128xQuery::getSelectFieldSQL();
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR_SEMES", $model->field["YEAR_SEMES"], $extra, 1);

        //科目一覧コンボボックス
        $extra = "onchange=\"return btn_submit('');\"";
        $opt = array();
        $result = $db->query(knjd128xQuery::getSubclassList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
        $model->field["SUBCLASSCD"] = ($model->field["SUBCLASSCD"] && $model->cmd != "changeKind") ? $model->field["SUBCLASSCD"] : $opt[0]["value"] ;
        $arg["data"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $model->field["SUBCLASSCD"], $opt, $extra, 1);

        //テスト種別コンボ (データ出力用)
        $extra = " id = \"TESTKIND_ITEMCD4\" ";
        $query = knjd128xQuery::getTestitem2($db, $model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD4", $model->field["TESTKIND_ITEMCD4"], $extra, 1, "ALL");

        //学年コンボ
        $query = knjd128xQuery::getSubclassTrgtGrade($model);
        $extra = "id = \"TRGTGRADE\" onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "TRGTGRADE", $model->field["TRGTGRADE"], $extra, 1, "ALL");

        //講座一覧コンボボックス
        $query = knjd128xQuery::getChairList($model);
        $extra = " id = \"CHAIRCD\" ";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "ALL");

        //ヘッダ有チェックボックス
        $extra  = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD128X");
        knjCreateHidden($objForm, "NENDO_GAKKI");
        knjCreateHidden($objForm, "SEMESTER_GAKKI");

        //CSV処理用データ
        knjCreateHidden($objForm, "HID_TESTKIND_ITEMCD");
        knjCreateHidden($objForm, "HID_TESTKIND_ITEMCD_LABEL");
        knjCreateHidden($objForm, "HID_TRGTGRADE");
        knjCreateHidden($objForm, "HID_CHAIRCD");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd128xForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $all="")
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    
    if ($all == "ALL") {
        $opt[] = array('label' => "(全て出力)", 'value' => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg, $db, $model) {

    //実行ボタン
    if ($model->Properties["useXLS"]) {
        $model->schoolCd = $db->getOne(knjd128xQuery::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"]  = CTRL_YEAR."年度　";
        $arg["data"]["TITLE"] = "　エクセル出力／ＣＳＶ取込";
    } else {
        $extra = "onclick=\"return btn_submit('exec');\"";
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"]  = CTRL_YEAR."年度　";
        $arg["data"]["TITLE"] = "　ＣＳＶ出力／取込";
    }
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
