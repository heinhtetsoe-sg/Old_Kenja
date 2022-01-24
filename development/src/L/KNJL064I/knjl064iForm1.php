<?php

require_once('for_php7.php');


class knjl064iForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl064iindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = $model->examyear;

        //入試制度コンボ
        $extra = " onchange=\"return btn_submit('main');\"";
        $query = knjl064iQuery::getNameMst($model, "L003", $model->examyear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        if ($model->field["APPLICANTDIV"] == "1") {
            $arg["APPLICANTDIV_J"] = "1";
        } else {
            $arg["APPLICANTDIV_H"] = "1";
        }

        //入試区分コンボ
        $query = knjl064iQuery::getEntexamTestDivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //重複チェック項目コンボ
        $query = knjl064iQuery::getCenterTitle();
        makeCmb($objForm, $arg, $db, $query, "CENTER_TITLE", $model->field["CENTER_TITLE"], $extra, 1, "BLANK");

        //一致条件フラグ設定
        $model->nameKanaMatchFlg = false;
        $model->birthDayMatchFlg = false;
        $model->telNoMatchFlg    = false;
        if ($model->field["CENTER_TITLE"] != "") {
            //カナ氏名一致
            if (in_array($model->field["CENTER_TITLE"], array(1, 2, 3, 5))) {
                $model->nameKanaMatchFlg = true;
            }
            //生年月日一致
            if (in_array($model->field["CENTER_TITLE"], array(1, 2, 4, 6))) {
                $model->birthDayMatchFlg = true;
            }
            //電話番号一致
            if (in_array($model->field["CENTER_TITLE"], array(1, 3, 4, 7))) {
                $model->telNoMatchFlg    = true;
            }
        }

        makeList($objForm, $arg, $model, $db);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl064iForm1.html", $arg);
    }
}

function makeList(&$objForm, &$arg, &$model, $db)
{
    $baseSelectedOpt = $beforeSelectedOpt = array();
    $multiOtherArray = array(); //jsエラーチェック用
    $count = 1;
    if ($model->field["CENTER_TITLE"]) {
        $query = knjl064iQuery::selectMainQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["LABEL"] = sprintf("%03d(%s、%s、%s、%s)-(%s、%s、%s、%s)", $count, $row["EXAMNO"], $row["NAME_KANA"], str_replace("-", "/", $row["BIRTHDAY"]), $row["TELNO"], $row["OTHER_EXAMNO"], $row["OTHER_NAME_KANA"], str_replace("-", "/", $row["OTHER_BIRTHDAY"]), $row["OTHER_TELNO"]);
            $row["VALUE"] = $row["EXAMNO"] . "-" . $row["OTHER_EXAMNO"];
            if ($row["RECOM_EXAMNO"] != "") {
                $baseSelectedOpt[] = array("label" => $row["LABEL"],
                                        "value" => $row["VALUE"]
                                        );
            } else {
                $beforeSelectedOpt[] = array("label" => $row["LABEL"],
                                            "value" => $row["VALUE"]
                                            );
            }

            //1対多になっている受験番号を覚えておく
            if ($row["EXAMNO_CNT"] > 1) {
                $multiOtherArray[$row["EXAMNO"]] = $row["EXAMNO_CNT"];
            }
            $count++;
        }
    }

    foreach ($multiOtherArray as $key => $val) {
        knjCreateHidden($objForm, "MULTI_OTHER_".$key, 0); //上段にいくつあるかのカウント
    }

    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"return move2('right','BASE_SELECTED', 'BEFORE_SELECTED', 1, 1);\" ";
    $arg["BASE_SELECTED"] = knjCreateCombo($objForm, "BASE_SELECTED", "", $baseSelectedOpt, $extra, "10");

    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"return move2('left','BASE_SELECTED','BEFORE_SELECTED', 1, 1);\" ";
    $arg["BEFORE_SELECTED"] = knjCreateCombo($objForm, "BEFORE_SELECTED", "", $beforeSelectedOpt, $extra, "10");

    $extra = "onclick=\"return moveAll('left','BASE_SELECTED','BEFORE_SELECTED', 1);\"";
    $arg["btn_base_all"] = knjCreateBtn($objForm, "btn_base_all", "↑↑", $extra);

    $extra = "onclick=\"return move2('left','BASE_SELECTED','BEFORE_SELECTED', 1, 1);\"";
    $arg["btn_base"] = knjCreateBtn($objForm, "btn_base", "↑", $extra);

    $extra = "onclick=\"return move2('right','BASE_SELECTED', 'BEFORE_SELECTED', 1, 1);\"";
    $arg["btn_before"] = knjCreateBtn($objForm, "btn_before", "↓", $extra);

    $extra = "onclick=\"return moveAll('right','BASE_SELECTED', 'BEFORE_SELECTED', 1);\"";
    $arg["btn_before_all"] = knjCreateBtn($objForm, "btn_before_all", "↓↓", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($name != "TESTDIV" && $row["NAMESPARE2"] && $default_flg) {
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
function makeBtn(&$objForm, &$arg)
{
    //完全一致者自動照合ボタン
    $extra = " onclick=\"return btn_submit('syougou');;\"";
    $arg["btn_syougou"] = knjCreateBtn($objForm, "btn_syougou", "完全一致者自動照合", $extra);
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
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", $model->examyear);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", KNJL213R);
    knjCreateHidden($objForm, "upd_data_base");
    knjCreateHidden($objForm, "upd_data_before");
}
