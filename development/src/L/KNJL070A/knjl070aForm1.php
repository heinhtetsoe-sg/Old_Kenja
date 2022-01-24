<?php

require_once('for_php7.php');

class knjl070aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "reset") {
            unset($model->upd_shdiv);
            unset($model->upd_course);
        }

        //年度
        $arg["YEAR"] = $model->ObjYear;

        //extra
        $change = " onchange=\"return btn_submit('main');\" tabindex=-1";
        $click = " onclick=\"return btn_submit('main');\"";

        //初期画面判定
        $defaultFlg = (!$model->applicantdiv && !$model->testdiv) ? true : false;

        //受験校種コンボボックス
        $extra = $change;
        $query = knjl070aQuery::getNameMst($model->ObjYear, 'L003');
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //試験コンボボックス
        $extra = $change;
        $query = knjl070aQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //表示順ラジオボタン 1:成績順 2:受験番号順
        $opt = array(1, 2);
        if (!$model->sort) {
            $model->sort = 1;
        }
        $extra = array("id=\"SORT1\"".$click, "id=\"SORT2\"".$click);
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //表示対象（専併）ラジオボタン 1:専願 2:併願 3:両方
        $opt = array(1, 2, 3);
        if (!$model->shdiv) {
            $model->shdiv = 3;
        }
        $extra = array("id=\"SHDIV1\"".$click, "id=\"SHDIV2\"".$click, "id=\"SHDIV3\"".$click);
        $radioArray = knjCreateRadio($objForm, "SHDIV", $model->shdiv, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //合格種別コンボボックス
        $extra = $change;
        $namecd1 = 'L'.$model->skArray[$model->applicantdiv].'13';
        $query = knjl070aQuery::getNameMst($model->ObjYear, $namecd1);
        makeCmb($objForm, $arg, $db, $query, "PASS_DIV", $model->pass_div, $extra, 1, "ALL");

        //志望コースコンボボックス
        $extra = $change;
        $namecd1 = 'L'.$model->skArray[$model->applicantdiv].'58';
        $query = knjl070aQuery::getNameMst($model->ObjYear, $namecd1);
        makeCmb($objForm, $arg, $db, $query, "WISH_COURSE", $model->wish_course, $extra, 1, "ALL");

        //更新対象コンボボックス（専併区分）
        $extra = " onchange=\"return cmbChgBGColor(this);\"";
        $query = knjl070aQuery::getNameMst($model->ObjYear, "L006");
        makeCmb($objForm, $arg, $db, $query, "UPD_SHDIV", $model->upd_shdiv, $extra, 1, "BLANK");

        //合格コースコンボボックス
        $extra = " onchange=\"return cmbChgBGColor(this);\"";
        $namecd1 = 'L'.$model->skArray[$model->applicantdiv].'13';
        $query = knjl070aQuery::getNameMst($model->ObjYear, $namecd1);
        makeCmb($objForm, $arg, $db, $query, "UPD_COURSE", $model->upd_course, $extra, 1, "BLANK");

        //ALLチェック
        $extra  = (strlen($model->upd_shdiv) && strlen($model->upd_course)) ? "" : "disabled";
        $extra .= " onclick=\"check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //事前専願表示用データ取得
        $honorOpt = array();
        $result = $db->query(knjl070aQuery::getHonor($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $honorOpt[$row["HONORDIV"]] = array("HONORDIV_NAME" => $row["HONORDIV_NAME"]);
        }

        //一覧表示
        $arr_ReceptNo = array();
        $dataFlg = false;
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl070aQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 && !$defaultFlg) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_ReceptNo[] = $row["RECEPTNO"];
                knjCreateHidden($objForm, "EXAMNO-".$row["RECEPTNO"], $row["EXAMNO"]);
                knjCreateHidden($objForm, "SHDIV-".$row["RECEPTNO"], $row["SHDIV"]);

                //背景色
                $row["BGCOLOR"] = "bgcolor=\"#ffffff\"";

                for ($i = 1; $i <= 2; $i++) {
                    knjCreateHidden($objForm, "COURSECD{$i}-".$row["RECEPTNO"], $row["COURSECD".$i]);

                    $id = "COURSE".$i."-".$row["RECEPTNO"];
                    $flg = ($row["SHDIV"] < $i) ? 1 : 0;

                    $bgcolor = "";
                    if ($flg) {
                        $bgcolor = "class=\"no_search\"";
                    } else {
                        if (strlen($model->upd_shdiv) && $i == $model->upd_shdiv) {
                            $bgcolor = "bgcolor=\"lime\"";
                        } elseif (strlen($model->upd_course) && $row["COURSECD".$i] == $model->upd_course) {
                            $bgcolor = "bgcolor=\"lime\"";
                        }

                        if (strlen($model->upd_shdiv) && strlen($model->upd_course)) {
                            if ($i == $model->upd_shdiv && $row["COURSECD".$i] == $model->upd_course) {
                                $bgcolor = "bgcolor=\"pink\"";
                            } elseif ($i != $model->upd_shdiv) {
                                $bgcolor = "";
                            }
                        }

                        if (!strlen($bgcolor)) {
                            $bgcolor = "bgcolor=\"#ffffff\"";
                        }
                    }
                    $row["EVENT".$i] = "id=\"{$id}\" ".$bgcolor." ";
                }
                //対象者チェックボックス
                $extra  = (strlen($model->upd_shdiv) && strlen($model->upd_course) && $model->upd_shdiv <= $row["SHDIV"]) ? "" : "disabled";
                $extra .= " id=\"CHECKED_{$row["RECEPTNO"]}\" onclick=\"checkboxChgBGColor(this, '{$row["RECEPTNO"]}');\"";
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["RECEPTNO"], $extra, "1");

                $row["BIKOU"] = knjCreateTextBox($objForm, $row['BIKOU'], "BIKOU-".$row["RECEPTNO"], 40, 20, '');

                $arg["data"][] = $row;
                $dataFlg = true;
            }
            $result->free();
        }

        $disabled = ($dataFlg) ? "" : " disabled";

        /**************/
        /* ＣＳＶ作成 */
        /**************/
        //ファイル
        $extra = "".$disable;
        $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);
        //取込ボタン
        $extra = "onclick=\"return btn_submit('csvInput');\"".$disabled;
        $arg["csv"]["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra);
        //出力ボタン
        $extra = "onclick=\"return btn_submit('csvOutput');\"".$disabled;
        $arg["csv"]["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra);
        //ヘッダ有チェックボックス
        $extra = "id=\"HEADER\"";
        if ($model->header == "on") {
            $extra .= " checked";
        }
        $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        /****************/
        /*  ボタン作成  */
        /****************/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"".$disabled;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"".$disabled;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_ReceptNo));

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL070A");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);

        //データ保持用
        knjCreateHidden($objForm, "HIDDEN_APPLICANTDIV");
        knjCreateHidden($objForm, "HIDDEN_TESTDIV");
        knjCreateHidden($objForm, "HIDDEN_SORT");
        knjCreateHidden($objForm, "HIDDEN_SHDIV");
        knjCreateHidden($objForm, "HIDDEN_PASS_DIV");
        knjCreateHidden($objForm, "HIDDEN_WISH_COURSE");
        knjCreateHidden($objForm, "HIDDEN_UPD_SHDIV");
        knjCreateHidden($objForm, "HIDDEN_UPD_COURSE");
        knjCreateHidden($objForm, "HIDDEN_HEADER");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl070aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl070aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();

    if ($name == "PASS_DIV" || $name == "UPD_COURSE") {
        $opt[] = array("label" => "未入力", "value" => "NO_DATA");
        if ($value === "NO_DATA") {
            $value_flg = true;
        }
    }

    $value = (strlen($value) && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
