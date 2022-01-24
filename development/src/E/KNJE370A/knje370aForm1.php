<?php

require_once('for_php7.php');

class knje370aForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje370aForm1", "POST", "knje370aindex.php", "", "knje370aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knje370aQuery::getYear();
        $extra = "onChange=\"return btn_submit('changeYear'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //学期取得
        if ($model->field["YEAR"] == CTRL_YEAR) {
            $semester = CTRL_SEMESTER;
        } else {
            $semester = $db->getOne(knje370aQuery::getMaxSemester($model));
        }
        knjCreateHidden($objForm, "SEMESTER", $semester);

        //既卒
        $kisotsu = $db->getOne(knje370aQuery::checkGradCnt($model, $semester));

        //出力指定
        $opt = array(1, 2);
        $model->field["DATA_DIV"] = ($model->field["DATA_DIV"] == "") ? "1" : $model->field["DATA_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATA_DIV{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATA_DIV", $model->field["DATA_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //画面サイズ切替
        $arg["ALLWIDTH"] = ($model->field["DATA_DIV"] == "2") ? "650" : "550";

        if ($model->field["DATA_DIV"] == "1") {
            $arg["data"]["GRADE_HR"] = "クラス一覧";
        } else {
            //クラスコンボ
            $query = knje370aQuery::getAuth($model, $semester, $kisotsu);
            $extra = "onChange=\"btn_submit('changeGradeHr')\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR", $model->field["GRADE_HR"], $extra, 1);
        }

        //クラス一覧リスト作成する
        if ($model->field["DATA_DIV"] == "1") {
            $query = knje370aQuery::getAuth($model, $semester, $kisotsu);
        } else {
            $query = knje370aQuery::getStudentLeft($model, $semester);
            $result = $db->query($query);
            $sentakuZumi = explode("','", $model->selectdata);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rowLeft[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
            }
            $result->free();

            if ($model->field["GRADE_HR"] == "ZZZZZ") {
                $query = knje370aQuery::getGradStudent($model, $semester);
            } else {
                $query = knje370aQuery::getStudent($model, $semester);
            }
        }
        $result = $db->query($query);
        $sentakuZumi = explode("','", $model->selectdata);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->cmd == "changeGradeHr") {
                list ($grad_flg, $ghratt, $schregno) = explode('-', $row["VALUE"]);
                if (!in_array($schregno, $sentakuZumi)) {
                    $rowRight[] = array('label' => $row["LABEL"],
                                        'value' => $row["VALUE"]);
                }
            } else {
                if ($model->field["DATA_DIV"] == "1") {
                    if (!in_array($row["VALUE"], $sentakuZumi)) {
                        $rowRight[] = array('label' => $row["LABEL"],
                                            'value' => $row["VALUE"]);
                    } else {
                        $rowLeft[]  = array('label' => $row["LABEL"],
                                            'value' => $row["VALUE"]);
                    }
                } else {
                    if ($model->warning) {
                        list ($grad_flg, $ghratt, $schregno) = explode('-', $row["VALUE"]);
                        if (!in_array($schregno, $sentakuZumi)) {
                            $rowRight[] = array('label' => $row["LABEL"],
                                                'value' => $row["VALUE"]);
                        }
                    } else {
                        $rowRight[] = array('label' => $row["LABEL"],
                                            'value' => $row["VALUE"]);
                    }
                }
            }
        }
        $result->free();

        $value = "";
        $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, isset($rowRight)?$rowRight:array(), $extra, 15);

        //出力対象クラスリストを作成する
        $value = "";
        $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, isset($rowLeft)?$rowLeft:array(), $extra, 15);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE370A");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje370aForm1.html", $arg); 
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $all="") {
    $opt = array();
    if ($all) $opt[] = array('label' => "（全て）", 'value' => "E000-ALL");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
