<?php

require_once('for_php7.php');
class knjl019vForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //校種
        $query = knjl019vQuery::getNameMst($model->ObjYear, "L003");
        $extra = "onChange=\"return btn_submit('read')\"";
        makeCmb($objForm, $arg, $db, $query, "EXAM_SCHOOL_KIND", $model->schoolKind, $extra, 1);

        //試験ID
        $query = knjl019vQuery::getExamId($model);
        $extra = "onChange=\"return btn_submit('read')\"";
        makeCmb($objForm, $arg, $db, $query, "EXAM_ID", $model->examId, $extra, 1);

        //会場
        $query = knjl019vQuery::getPlaceId($model);
        $extra = "onChange=\"return btn_submit('read')\"";
        makeCmb($objForm, $arg, $db, $query, "PLACE_ID", $model->placeId, $extra, 1, "ALL");

        //科目
        $query = knjl019vQuery::getExamSubclass($model);
        $extra = "onChange=\"return btn_submit('read')\"";
        makeCmb($objForm, $arg, $db, $query, "EXAM_SUBCLASS", $model->examSubclass, $extra, 1);

        $arySubclass = array();
        $query = knjl019vQuery::getExamSubclass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["VALUE"] == $model->examSubclass) {
                $perfectdiv = $row["PERFECTDIV"];
            }

            $arySubclass[] = array(
                'SUBCLASS_CD'   => $row["VALUE"],
                'SUBCLASS_NAME' => $row["SUBCLASS_NAME"],
                'PERFECTDIV'    => $row["PERFECTDIV"],
                'ROW_WIDTH'     => "60",
            );
        }
        $cntSubclass = get_count($arySubclass);
        $arySubclass[$cntSubclass-1]["ROW_WIDTH"] = "*";
        $arg["headerSubclass"] = $arySubclass;

        //満点を取得
        $perfect = "";
        $stepsOpt = array();
        if ($perfectdiv == "1") {
            $query = knjl019vQuery::getExamSubclassPerfect($model, $model->examSubclass, $perfectdiv);
            $subclassRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $perfect = strlen($subclassRow["VALUE"]) ? $subclassRow["VALUE"] : 0;
        } else {
            $query = knjl019vQuery::getExamSubclassPerfect($model, $model->examSubclass, $perfectdiv);
            $dummy = "";
            $stepsOpt = makeCmb($objForm, $arg, $db, $query, "STEPS", $dummy, $extra, 1, "BLANK", "1");
        }
        knjCreateHidden($objForm, "PERFECT", $perfect);

        //表示順序ラジオボタン 1:受験番号順 2:かな順
        $opt_sort = array(1, 2);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array("id=\"SORT1\" onclick=\"btn_submit('read');\"", "id=\"SORT2\" onclick=\"btn_submit('read');\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt_sort, get_count($opt_sort));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }


        //欠席者非表示チェックボックス
        $model->checkHide = ($model->cmd == "main") ? "1" : $model->checkHide;
        $extra  = " id=\"CHECK_HIDE1\" onclick=\"btn_submit('read');\"";
        if ($model->checkHide == "1") {
            $extra  .= " checked";
        }
        $arg["TOP"]["CHECK_HIDE"] = knjCreateCheckBox($objForm, "CHECK_HIDE", "1", $extra, "");

        //一覧表示
        $receptnoArray = array();
        $scoreArray = array();
        $scoreCnt = 0;
        if ($model->schoolKind != "" && $model->examId != "" && $model->placeId != "" && $model->examSubclass != "") {
            //データ取得
            $query = knjl019vQuery::selectQuery($model, $arySubclass);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $receptno = $row["RECEPTNO"];

                //HIDDENに保持する用
                $receptnoArray[] = $receptno;

                //欠席
                $disabledScore = ($row["ABSENCE_FLG"] == "1") ? " disabled" : "";
                knjCreateHidden($objForm, "ABSENCE_FLG-{$receptno}", $row["ABSENCE_FLG"]);

                $scoreSubclass = array();
                foreach ($arySubclass as $key => $val) {
                    //得点
                    $score = $row["SCORE_{$val['SUBCLASS_CD']}"];

                    //編集可科目
                    if ($val['SUBCLASS_CD'] == $model->examSubclass) {
                        if ($val["PERFECTDIV"] == "1") {
                            //科目得点textbox
                            $extra  = "id=\"SCORE_{$val['SUBCLASS_CD']}-{$receptno}\" ";
                            $extra .= "style=\"text-align: right; width: 30px;\" onblur=\"checkScore(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this,".$scoreCnt.",".$val['SUBCLASS_CD'].");\"".$disabledScore;
                            $setScore = knjCreateTextBox($objForm, $score, "SCORE_{$val['SUBCLASS_CD']}-{$receptno}", 3, 3, $extra);
                            $scoreArray[] = "SCORE_{$val['SUBCLASS_CD']}-{$receptno}";
                            $scoreCnt++;
                        } else {
                            //面接評価combobox
                            $extra = "id=\"SCORE_{$val['SUBCLASS_CD']}-{$receptno}\" onchange=\"changeFlg(this);\"".$disabledScore;
                            $setScore = knjCreateCombo($objForm, "SCORE_{$val['SUBCLASS_CD']}-{$receptno}", $score, $stepsOpt, $extra, 1);
                        }
                    } else {
                        //編集不可科目
                        if ($val["PERFECTDIV"] == "1") {
                            $setScore = $score;
                        } else {
                            if ($score == "") {
                                $setScore = $score;
                            } else {
                                $query = knjl019vQuery::getExamSubclassPerfect($model, $val['SUBCLASS_CD'], $val["PERFECTDIV"], $score);
                                $perfectRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                                $setScore = $perfectRow["LABEL"]."({$perfectRow["VALUE"]})";
                            }
                        }
                    }
                    $scoreSubclass[$key]["SCORE"] = $setScore;
                    $scoreSubclass[$key]["ROW_WIDTH"] = "60";
                }
                $scoreSubclass[$key]["ROW_WIDTH"] = "*";
                $row["scoreSubclass"] = $scoreSubclass;

                $dataflg = true;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $receptnoArray, $scoreArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl019vindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl019vForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $retDiv = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    } elseif ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
    }
    $value_flg = ($blank == "ALL") ? true : false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "STEPS") {
            //評価コンボのVALUEはSTEPSをセット
            $opt[] = array('label' => $row["LABEL"]."({$row["VALUE"]})",
                           'value' => $row["STEPS"]);

            if ($value == $row["STEPS"]) {
                $value_flg = true;
            }
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $opt;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg)
{
    //CSVボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_L019V/knjx_l019vindex.php?YEAR={$model->ObjYear}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "CSV処理", $extra);

    $disable  = ($dataflg) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $receptnoArray, $scoreArray)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAM_SCHOOL_KIND");
    knjCreateHidden($objForm, "HID_EXAM_ID");
    knjCreateHidden($objForm, "HID_PLACE_ID");
    knjCreateHidden($objForm, "HID_EXAM_SUBCLASS");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $receptnoArray));
    knjCreateHidden($objForm, "CHANGE_FLG");
    knjCreateHidden($objForm, "HID_SCORE", implode(",", $scoreArray));
}
