<?php

require_once('for_php7.php');


class knjl214fForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl214findex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = $model->examyear;

        //入試制度コンボ
        $extra = " onchange=\"return btn_submit('main');\"";
        $query = knjl214fQuery::getNameMst($model->examyear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボ
        if ($model->field["APPLICANTDIV"] == "1") {
            $arg["JUNIOR"] = "1";
            $query = knjl214fQuery::getTestDivKotei();
        } else {
            $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
            $query = knjl214fQuery::getNameMst($model->examyear, $namecd1);
        }
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //入試回数(2:高校のみ)
        if ($model->field["APPLICANTDIV"] == "2") {
            $query = knjl214fQuery::getTestdiv0($model->examyear, $model->field["TESTDIV"]);
            $extra = " onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "TESTDIV0", $model->field["TESTDIV0"], $extra, 1, "");
        }

        //重複チェック項目コンボ
        $query = knjl214fQuery::getCenterTitle();
        makeCmb($objForm, $arg, $db, $query, "CENTER_TITLE", $model->field["CENTER_TITLE"], $extra, 1, "BLANK");

        //志願者基礎データ
        $model->baseArrayKakutei = array();
        $baseArray = getBaseArray($model, $db);

        //事前相談データ
        $beforeArray = getBeforeArray($model, $db);

        makeList($objForm, $arg, $model, $baseArray, $beforeArray);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl214fForm1.html", $arg);
    }
}

//志願者基礎データ
function getBaseArray($model, $db) {
    $baseArray = array();
    $query = knjl214fQuery::getBaseDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $key = $row["EXAMNO"];
        if (strlen($row["REMARK1"])) {
            $model->baseArrayKakutei[$key] = array("EXAMNO"           => $row["EXAMNO"],
                                     "NAME"             => $row["NAME"],
                                     "NAME_KANA"        => $row["NAME_KANA"],
                                     "FS_CD"            => $row["FS_CD"],
                                     "FINSCHOOL_NAME"   => $row["FINSCHOOL_NAME"],
                                     "REMARK1"          => $row["REMARK1"]
                                );
        } else {
            $baseArray[$key] = array("EXAMNO"           => $row["EXAMNO"],
                                     "NAME"             => $row["NAME"],
                                     "NAME_KANA"        => $row["NAME_KANA"],
                                     "FS_CD"            => $row["FS_CD"],
                                     "FINSCHOOL_NAME"   => $row["FINSCHOOL_NAME"],
                                     "REMARK1"          => $row["REMARK1"]
                                );
        }
    }
    $result->free();
    return $baseArray;
}

//事前相談データ
function getBeforeArray($model, $db) {
    $beforeArray = array();
    $query = knjl214fQuery::getBeforeDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $flg = false;
        foreach ($model->baseArrayKakutei as $examno => $baseList) {
            if ($row["RECRUIT_NO"] == $baseList["REMARK1"]) {
                $flg = true;
            }
        }
        if ($flg) continue;
        $key = $row["RECRUIT_NO"];
        $beforeArray[$key] = array("RECRUIT_NO"     => $row["RECRUIT_NO"],
                                   "NAME"           => $row["NAME"],
                                   "NAME_KANA"      => $row["NAME_KANA"],
                                   "FS_CD"          => $row["FS_CD"],
                                   "FINSCHOOL_NAME" => $row["FINSCHOOL_NAME"]
                            );
    }
    $result->free();
    return $beforeArray;
}

//重複チェック項目
function makeList(&$objForm, &$arg, $model, $baseArray, $beforeArray) {
    //重複チェック項目
    //VALUES('1', '（かな氏名○、出身学校×）リスト')
    //VALUES('2', '（かな氏名×、出身学校○）リスト')
    //VALUES('3', '（かな氏名○、出身学校○）リスト')
    $checkList = array();
    if (strlen($model->field["CENTER_TITLE"])) {
        foreach ($baseArray as $examno => $baseList) {
            foreach ($beforeArray as $pageno => $beforeList) {
                if ($model->field["CENTER_TITLE"] == "1" && $beforeList["NAME_KANA"] == $baseList["NAME_KANA"] &&  $beforeList["FS_CD"] != $baseList["FS_CD"]) {
                    $checkList[$examno][] = $pageno;
                } else if ($model->field["CENTER_TITLE"] == "2" && $beforeList["NAME_KANA"] != $baseList["NAME_KANA"] &&  $beforeList["FS_CD"] == $baseList["FS_CD"]) {
                    $checkList[$examno][] = $pageno;
                } else if ($model->field["CENTER_TITLE"] == "3" && $beforeList["NAME_KANA"] == $baseList["NAME_KANA"] &&  $beforeList["FS_CD"] == $baseList["FS_CD"]) {
                    $checkList[$examno][] = $pageno;
                }
            }
        }
    }

    $centerList = array();
    $number = 0;
    foreach ($checkList as $examno => $array) {
        $number++;
        $errFlg = 1 < get_count($array) ? "NG" : "OK"; //重複データはERROR
        foreach ($array as $key => $pageno) {
            $baseLabel = $examno . "、" . $baseArray[$examno]["NAME"] . "、" . $baseArray[$examno]["NAME_KANA"] . "、" . $baseArray[$examno]["FINSCHOOL_NAME"];
            $beforeLabel = $pageno . "、" . $beforeArray[$pageno]["NAME"] . "、" . $beforeArray[$pageno]["NAME_KANA"]. "、" . $beforeArray[$pageno]["FINSCHOOL_NAME"];
            //受験番号に対して、複数の事前番号がある場合、
            //１件目は通常表示。２件目以降は●表示。
            if ($key == 0) {
                $centerList[] = array('label' => sprintf("%03d", $number) . "(" . $baseLabel . ")" . "-" . "(" . $beforeLabel . ")",
                                      'value' => $examno . "-" . $pageno . "-" . $errFlg);
            } else {
                $centerList[] = array('label' => "●" . "(" . $beforeLabel . ")" . "→" . "(" . "重複データあり" . ")",
                                      'value' => $examno . "-" . $pageno . "-" . $errFlg);
            }
        }
    }
    //
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ";
    $arg["CENTER_SELECTED"] = knjCreateCombo($objForm, "CENTER_SELECTED", "", $centerList, $extra, "10");
    //
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ";
    $arg["BASE_SELECTED"] = knjCreateCombo($objForm, "BASE_SELECTED", "", array(), $extra, "10");
    //
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ";
    $arg["BEFORE_SELECTED"] = knjCreateCombo($objForm, "BEFORE_SELECTED", "", array(), $extra, "10");
    //志願者データボタン
    //
    $extra = "onclick=\"return move2('left','BASE_SELECTED','CENTER_SELECTED',1);\"";
    $arg["btn_base"] = knjCreateBtn($objForm, "btn_base", "↑", $extra);
    //
    $extra = "onclick=\"return move2('right','BASE_SELECTED','CENTER_SELECTED',1);\"";
    $arg["btn_base_center"] = knjCreateBtn($objForm, "btn_base_center", "↓", $extra);
    //事前相談データボタン
    //
    $extra = "onclick=\"return move2('left','BEFORE_SELECTED','CENTER_SELECTED',1);\"";
    $arg["btn_before"] = knjCreateBtn($objForm, "btn_before", "↓", $extra);
    //
    $extra = "onclick=\"return move2('right','BEFORE_SELECTED','CENTER_SELECTED',1);\"";
    $arg["btn_before_center"] = knjCreateBtn($objForm, "btn_before_center", "↑", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = " onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //更新ボタン
    $extra = "onclick=\"return doSubmit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", $model->examyear);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL213F");
    knjCreateHidden($objForm, "upd_data_base");
    knjCreateHidden($objForm, "upd_data_before");
}
?>
