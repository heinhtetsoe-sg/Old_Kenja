<?php

require_once('for_php7.php');

class knjp744Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjp744index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //参照年度コンボ
        $query = knjp744Query::getYear();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->sansyouYear, "SANSYOU_YEAR", $extra, 1);

        //コピー用データチェック（処理年度のデータ有無）
        $query = knjp744Query::checkDataExists($model, "LEVY_REQUEST_INCOME_AUTO_DAT");
        $exist_flg  = $db->getOne($query);
        if ($model->Properties["not_select_schregno_auto_income"] != "1") {
            $query = knjp744Query::checkDataExists($model, "LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT");
            $exist_flg += $db->getOne($query);
        }

        knjCreateHidden($objForm, "exist_flg", $exist_flg);

        //コピー用データチェック（必須項目の処理年度の存在チェック）
        $err_flg = false;
        $dataCnt = 0;
        $result = $db->query(knjp744Query::getCopyQuery($model, 'check'));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->Properties["not_select_schregno_auto_income"] != "1") {
                $errCnt = $row["COLLECT_FLG"] + $row["INCOME_FLG"] + $row["SCHREGNO_FLG"] + $row["STAFFCD_FLG"];
            } else {
                $errCnt = $row["COLLECT_FLG"] + $row["INCOME_FLG"] + $row["STAFFCD_FLG"];
            }
            if ($errCnt > 0) {
                $err_flg = true;
            } else {
                $dataCnt++;
            }
        }
        $result->free();

        knjCreateHidden($objForm, "err_flg", $err_flg);
        knjCreateHidden($objForm, "dataCnt", $dataCnt);

        //コピーボタン
        $extra = ($db->getOne(knjp744Query::getYear())) ? "onclick=\"btn_submit('copy');\"" : "disabled";
        $arg["copy_btn"] = knjCreateBtn($objForm, "copy_btn", "左の年度からコピー", $extra);

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp744Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeKind');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //自動設定リスト取得
        $query = knjp744Query::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $row["AUTO_NO"] = View::alink(
                "knjp744index.php",
                $row["AUTO_NO"],
                "target=right_frame",
                array("AUTO_NO"        => $row["AUTO_NO"],
                                                   "SCHOOL_KIND"    => $model->schoolKind,
                                                   "cmd"            => "edit2"
                                                   )
            );

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "changeKind") {
            unset($model->auto_no);
            unset($model->selectdata);
            $model->field = array();

            $arg["jscript"] = "window.open('knjp744index.php?cmd=edit2','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp744Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } elseif ($name == "SANSYOU_YEAR") {
        $value = ($value && $value_flg) ? $value : (CTRL_YEAR - 1);
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
