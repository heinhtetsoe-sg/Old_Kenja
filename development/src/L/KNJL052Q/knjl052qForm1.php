<?php

require_once('for_php7.php');

class knjl052qForm1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl052qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        if (SCHOOLKIND == "J") {
            $query = knjl052qQuery::getNameMst($model->ObjYear, "L024");
        } else {
            $query = knjl052qQuery::getNameMst($model->ObjYear, "L004");
        }
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //表示項目用
        if (SCHOOLKIND == "J") {
            $arg["disable_J"] = "";
            $arg["only_J"]    = "1";
        } else {
            $arg["disable_J"] = "1";
            $arg["only_J"]    = "";
        }

        //一覧表示
        $arr_receptno = $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            if (SCHOOLKIND != "J") {
                //評価データ取得
                $intValArray = $intLabelArray = array();
                $query = knjl052qQuery::getInterviewName($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $intValArray[]      = $row["VALUE"];
                    $intLabelArray[]    = $row["LABEL"];
                }
                $result->free();
            }

            //評価一覧
            $intValList = array();
            $intValList[] = array("name" => "INTERVIEW_VALUE", "var" => $model->interview_value);
            if (SCHOOLKIND == "J") {
                $intValList[] = array("name" => "INTERVIEW_VALUE2", "var" => $model->interview_value2);
            }

            //評価が未入力の生徒は、初期値2:Bを設定する
            if (SCHOOLKIND != "J") {
                $query = knjl052qQuery::selectNullQuery($model);
                $result = $db->query($query);
                $db->autoCommit(false);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                     $query = knjl052qQuery::getCount($model, $row["RECEPTNO"]);
                     $count = $db->getOne($query);
                     $query = knjl052qQuery::getINSERTQuery($model, $row["RECEPTNO"], $count);
                     $db->query($query);
                }
                $db->commit();
                $result->free();
            }

            //データ取得
            $query = knjl052qQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];
                $arr_examno[]   = $row["EXAMNO"];

                foreach ($intValList as $lkey => $lval) {
                    if (SCHOOLKIND == "J") {
                        //評価コンボボックス
                        $extra = "onchange=\"Setflg(this, '{$row["RECEPTNO"]}');\"";
                        $query = knjl052qQuery::getInterviewNameJ($model);
                        $value = ($model->isWarning()) ? $lval["var"][$row["RECEPTNO"]] : $row[$lval["name"]];
                        $row[$lval["name"]]  = makeCmbReturn($objForm, $arg, $db, $query, $lval["name"]."-".$row["RECEPTNO"], $value, $extra, 1, "blank");
                    } else {
                        //評価ラジオボタン
                        $extra = array();
                        $value = ($model->isWarning()) ? $lval["var"][$row["RECEPTNO"]] : $row[$lval["name"]];
                        $click = "onClick=\"clickUpdate(this, '{$row["RECEPTNO"]}');\"";
                        foreach ($intValArray as $akey => $aval) {
                            $extra[] = "id=\"".$lval["name"]."-".$row["RECEPTNO"].$akey."\"".$click;
                        }
                        $radioArray = knjCreateRadio($objForm, $lval["name"]."-".$row["RECEPTNO"], $value, $extra, $intValArray, get_count($intValArray));

                        $counter = 0;
                        $setData = $sep = "";
                        foreach ($radioArray as $rkey => $rval) {
                            $setData .= $sep.$rval."<LABEL for=\"".$lval["name"]."-".$row["EXAMNO"].$counter."\">".$intLabelArray[$counter]."</LABEL>";
                            $counter++;
                            $sep = "&nbsp;&nbsp;";
                        }
                        $row[$lval["name"]] = $setData;
                    }
                }
                $arg["data"][] = $row;
            }
        }

        if (SCHOOLKIND == "J") {
            //更新ボタン
            $extra = "onclick=\"return btn_submit('update');\"";
            $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //取消ボタン
            $extra = "onclick=\"return btn_submit('reset');\"";
            $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        }
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_UP_RECEPTNO");
        knjCreateHidden($objForm, "HID_UP_INTERVIEW_VALUE");
        knjCreateHidden($objForm, "SET_SCROLL_VAL");

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL052Q");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl052qindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "mainH") {
            $arg["setScroll"] = " setScroll('$model->set_scroll_val');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl052qForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
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

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成（表内用）
function makeCmbReturn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
