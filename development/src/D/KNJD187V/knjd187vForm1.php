<?php

require_once('for_php7.php');

class knjd187vForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd187vForm1", "POST", "knjd187vindex.php", "", "knjd187vForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $model->field["SEMESTER1"] = $model->field["SEMESTER1"] == null ? CTRL_SEMESTER : $model->field["SEMESTER1"];

        //学期コンボ
        ////学期コンボ1作成
        $query = knjd187vQuery::getSemester($model);
        $extra = " onchange=\"btn_submit('semester');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER1"], "SEMESTER1", $extra, 1);
        $seme = $model->field["SEMESTER1"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER1"];
        ////学期コンボ2作成
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER2"], "SEMESTER2", "", 1);

        //表示指定ラジオボタン 1:クラス 2:個人
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"btn_submit('change')\"", "id=\"DISP2\" onClick=\"btn_submit('change')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //画面切り替え用
        if ($model->field["DISP"] == "1") {
            $arg["is_disp1"] = 1;
        } else {
            $arg["is_disp2"] = 1;
        }

        //中間・期末指定ラジオボタン 1:中間 2:期末
        if ($model->field["DISP"] == "1") {
            $opt_disp = array(1, 2);
            $model->field["DIV"] = ($model->field["DIV"] == "") ? "1" : $model->field["DIV"];
            $extra = array("id=\"DIV1\" onClick=\"btn_submit('change')\"", "id=\"DIV2\" onchange=\"btn_submit('change')\"");
            $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $extra, $opt_disp, get_count($opt_disp));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //年組コンボ
        $query = knjd187vQuery::getGrade($model, $seme);
        $extra = "onchange=\"btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);
        if ($model->field["DISP"] == "2") {
            $query = knjd187vQuery::getGradeHrClass($model, $seme, $model->field["GRADE"]);
            makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra, 1);
        }

        //リストToリスト作成
        if ($model->field["DISP"] == "1" && $model->field["DIV"] == "2") {
            //クラス選択 - 期末
        } else {
            $arg["is_LRlist"] = 1;
            makeListToList($objForm, $arg, $db, $model, $seme);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd187vForm1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $seme)
{
    //初期化
    $opt_right = $opt_left = array();

    //対象者リスト
    if ($model->field["DISP"] == "1") {
        $query = knjd187vQuery::getGradeHrClass($model, $seme, $model->field["GRADE"]);
        $arg["data"]["RNAME"] = "クラス一覧";
    } else {
        $query = knjd187vQuery::getStudent($model, $seme);
        $arg["data"]["RNAME"] = "生徒一覧";
    }
    $result = $db->query($query);
    if ($model->field["DISP"] == "1") {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array(
                'label' => $row["LABEL"],
                'value' => $row["VALUE"]
            );
        }
    } else {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array(
                'label' => $row["SCHREGNO"]." ".$row["ATTENDNO"]."番"." ".$row["NAME_SHOW"],
                'value' => $row["SCHREGNO"]
            );
        }
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:100%\" ondblclick=\"moveLeft(this)\"";
    $arg["data"]["CLSS_OR_STDNTS_LIST"] = knjCreateCombo($objForm, "CLSS_OR_STDNTS_LIST", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:100%\" ondblclick=\"moveRight(this)\"";
    $arg["data"]["CLSS_OR_STDNTS_SELECTED"] = knjCreateCombo($objForm, "CLSS_OR_STDNTS_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moveRight(this);\"";
    $arg["button"]["btn_rights_all"] = knjCreateBtn($objForm, "btn_rights_all", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moveLeft(this);\"";
    $arg["button"]["btn_lefts_all"] = knjCreateBtn($objForm, "btn_lefts_all", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moveRight(this);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moveLeft(this);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
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

        if ($value == $row["VALUE"]) {
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
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //チェック指定フラグ
    $inpChk = "true";
    if ($model->field["DISP"] == "1" && $model->field["DIV"] == "2") {
        //クラス選択かつ個人成績無しの場合は入力チェック不要
        $inpChk = "false";
    }

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', {$inpChk});\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD187V");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataText");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
