<?php

require_once('for_php7.php');

class knjl075qForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl075qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        if (SCHOOLKIND == "J") {
            $query = knjl075qQuery::getNameMst($model->ObjYear, "L024");
        } else {
            $query = knjl075qQuery::getNameMst($model->ObjYear, "L004");
        }
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);
        if ($model->testdiv == "1" || $model->testdiv == "2") {
            $arg["tokyuTestdiv1"] = "1";
        } else {
            $arg["tokyuTestdiv"] = "1";
        }

        //一覧表示
        $model->arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {

            if (SCHOOLKIND != "J") {
                //等級取得
                $toukyuValArray = $toukyuLabelArray = array();
                $query = knjl075qQuery::getNameMst($model->ObjYear, "L025", "only");
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $toukyuValArray[]   = $row["VALUE"];
                    $toukyuLabelArray[] = $row["LABEL"];
                }
                $result->free();
            }

            //等級一覧
            $ToukyuList = array();
            $ToukyuList[] = array("name" => "STOUKYU", "var" => $model->stoukyu);
            if ($model->testdiv == "1" || $model->testdiv == "2") {
                $ToukyuList[] = array("name" => "HTOUKYU", "var" => $model->htoukyu);
            }

            //データ取得
            $query = knjl075qQuery::SelectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //配列に保存
                $model->arr_examno[]   = $row["EXAMNO"];

                foreach ($ToukyuList as $lkey => $lval) {
                    if (SCHOOLKIND == "J") {
                        //等級コンボボックス
                        $extra = "onchange=\"Setflg(this, '{$row["EXAMNO"]}');\"";
                        $query = knjl075qQuery::getNameMst($model->ObjYear, "L025");
                        $value = ($model->isWarning()) ? $lval["var"][$row["EXAMNO"]] : $row[$lval["name"]];
                        $row[$lval["name"]] = makeCmbReturn($objForm, $arg, $db, $query, $lval["name"]."-".$row["EXAMNO"], $value, $extra, 1, "blank");
                    } else {
                        //等級ラジオボタン
                        $extra = array();
                        $value = ($model->isWarning()) ? $lval["var"][$row["EXAMNO"]] : $row[$lval["name"]];
                        $click = "onClick=\"Setflg(this, '{$row["EXAMNO"]}');\"";
                        foreach ($toukyuValArray as $akey => $aval) $extra[] = "id=\"".$lval["name"]."-".$row["EXAMNO"].$akey."\"".$click;
                        $radioArray = knjCreateRadio($objForm, $lval["name"]."-".$row["EXAMNO"], $value, $extra, $toukyuValArray, get_count($toukyuValArray));

                        $counter = 0;
                        $setData = $sep = "";
                        foreach($radioArray as $rkey => $rval) {
                            $setData .= $sep.$rval."<LABEL for=\"".$lval["name"]."-".$row["EXAMNO"].$counter."\">".$toukyuLabelArray[$counter]."</LABEL>";
                            $counter++;
                            $sep = "&nbsp;&nbsp;";
                        }
                        $row[$lval["name"]] = $setData;
                    }
                }

                $arg["data"][] = $row;
            }
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return btn_submit('end');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_APPLICANTDIV");
        knjCreateHidden($objForm, "HID_TESTDIV");
        knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$model->arr_examno));

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL075Q");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl075qindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl075qForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
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

        if ($row["NAMESPARE2"] && $default_flg){
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
function makeCmbReturn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
