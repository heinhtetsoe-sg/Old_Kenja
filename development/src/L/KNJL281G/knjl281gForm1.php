<?php

require_once('for_php7.php');

class knjl281gForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl281gQuery::getNameMst($model->ObjYear, "L003");
        $extra = "Onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //radio
        $opt = array(1, 2, 3, 4);
        $model->testPattern = ($model->testPattern == "") ? "1" : $model->testPattern;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TEST_PATTERN{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "TEST_PATTERN", $model->testPattern, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP2"][$key] = $val;

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "") {
            //データ取得
            $query = knjl281gQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //学籍番号
                $value = ($model->isWarning()) ? $model->schregno[$row["EXAMNO"]] : $row["SCHREGNO"];
                $valueYear = substr($value, 0, 4);
                $valueRen = substr($value, 4);
                if ($row["TEXT_OPEN"] == '1') {
                    $row["SCHREGNO_YEAR"] = $valueYear ? $valueYear : $model->ObjYear;
                    $extra = " OnChange=\"Setflg(this);\" id=\"".$row["EXAMNO"]."\" onblur=\"checkNo(this);\"";
                    $row["SCHREGNO_REN"] = knjCreateTextBox($objForm, $valueRen, "SCHREGNO_{$row["EXAMNO"]}", 4, 4, $extra);
                } else {
                    $row["SCHREGNO_YEAR"] = $valueYear;
                    $row["SCHREGNO_REN"] = $valueRen;
                }

                $arg["data"][] = $row;
            }

            //データ取得
            $query = knjl281gQuery::selectOtherSchregQuery($model);
            $result = $db->query($query);
            $otherSchregno = "";
            $sep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $valueRen = substr($row["SCHREGNO"], 4);
                $otherSchregno .= $sep.$valueRen;
                $sep = ",";
            }
            //hidden
            knjCreateHidden($objForm, "OTHER_SCHREGNO", $otherSchregno);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $arr_examno);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl281gindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl281gForm1.html", $arg);
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $arr_examno) {
    $disable  = (get_count($arr_examno) > 0) ? "" : " disabled";

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
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "tyoufuku");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
}
?>
