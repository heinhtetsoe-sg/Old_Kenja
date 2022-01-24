<?php
class knjl810hForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear;

        //学校種別コンボ
        $query = knjl810hQuery::getNameMst($model->examyear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試区分コンボ
        $query = knjl810hQuery::getTestDiv($model->examyear, $model->field["APPLICANTDIV"]);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "");

        //氏名・性別チェックボックス
        $extra  = " id=\"CHECK1\"";
        if ($model->field["CHECK"] == 1) {
            $extra  .= " checked";
        }
        $arg["data"]["CHECK"] = knjCreateCheckBox($objForm, "CHECK", "1", $extra, "");

        //帳票種類ラジオボタン（1:志願者一覧表、2:出欠席記入表、3:成績記入表、4:机上タックシール）
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //志願者一覧表ラジオボタン 1:共通 2:公立 3:私立
        $opt_shubetsu = array(1, 2, 3);
        $model->field["PASS_SCHOOL_CD"] = ($model->field["PASS_SCHOOL_CD"]=="") ? "1" : $model->field["PASS_SCHOOL_CD"];
        $extra = array("id=\"PASS_SCHOOL_CD1\"", "id=\"PASS_SCHOOL_CD2\"", "id=\"PASS_SCHOOL_CD3\"");
        $radioArray = knjCreateRadio($objForm, "PASS_SCHOOL_CD", $model->field["PASS_SCHOOL_CD"], $extra, $opt_shubetsu, count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //入試種別コンボ
        $query = knjl810hQuery::getKindDiv($model->examyear, $model->field["APPLICANTDIV"], $model->field["TESTDIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "KINDDIV", $model->field["KINDDIV"], $extra, 1, "ALL");

        //出欠席記入表ラジオボタン 1:筆記 2:面接 3:作文
        $opt_shubetsu = array(1, 2, 3);
        $model->field["SYUKESSEKI_HANTEI_HOU"] = ($model->field["SYUKESSEKI_HANTEI_HOU"]=="") ? "1" : $model->field["SYUKESSEKI_HANTEI_HOU"];
        $click = "onclick =\" return btn_submit('main');\"";
        $extra = array("id=\"SYUKESSEKI_HANTEI_HOU1\"".$click, "id=\"SYUKESSEKI_HANTEI_HOU2\"".$click, "id=\"SYUKESSEKI_HANTEI_HOU3\"".$click);
        $radioArray = knjCreateRadio($objForm, "SYUKESSEKI_HANTEI_HOU", $model->field["SYUKESSEKI_HANTEI_HOU"], $extra, $opt_shubetsu, count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //成績記入表ラジオボタン 1:筆記 2:面接 3:作文
        $opt_shubetsu = array(1, 2, 3);
        $model->field["SCORE"] = ($model->field["SCORE"]=="") ? "1" : $model->field["SCORE"];
        $click = "onclick =\" return btn_submit('main');\"";
        $extra = array("id=\"SCORE1\"".$click, "id=\"SCORE2\"".$click, "id=\"SCORE3\"".$click);
        $radioArray = knjCreateRadio($objForm, "SCORE", $model->field["SCORE"], $extra, $opt_shubetsu, count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出欠席記入表 会場コンボ
        $query = knjl810hQuery::getHallList($model->examyear, $model->field["APPLICANTDIV"], $model->field["TESTDIV"], $model->field["SYUKESSEKI_HANTEI_HOU"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "HALLCD1", $model->field["HALLCD1"], $extra, 1, "ALL");

        //成績記入表 会場コンボ
        $query = knjl810hQuery::getHallList($model->examyear, $model->field["APPLICANTDIV"], $model->field["TESTDIV"], $model->field["SCORE"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "HALLCD2", $model->field["HALLCD2"], $extra, 1, "ALL");

        //机上タックシール 会場コンボ
        $query = knjl810hQuery::getHallList($model->examyear, $model->field["APPLICANTDIV"], $model->field["TESTDIV"], "1");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "HALLCD3", $model->field["HALLCD3"], $extra, 1, "ALL");

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL810H");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl810hindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl810hForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
