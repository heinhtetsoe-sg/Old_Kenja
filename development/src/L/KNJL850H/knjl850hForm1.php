<?php
class knjl850hForm1 {

    function main(&$model) {

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();
        
        //$Row =& $model->field;

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear;

        //学校種別コンボ
        $query = knjl850hQuery::getNameMst($model->examyear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試区分コンボ
        $query = knjl850hQuery::getTestDiv($model->examyear, $model->field["APPLICANTDIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "");

        //帳票種類ラジオボタン（1:合格者一覧表、2:合格者一覧表（掲示差込用）CSV出力、3:合格証、4:入学許可証、5:繰上候補者一覧表、6:繰上候補者通知書
        $opt = array(1, 2, 3, 4, 5, 6);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //No. : 数字4桁
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["NUMBER"] = knjCreateTextBox($objForm, $model->field["NUMBER"], "NUMBER", 4, 4, $extra);
        
        //連絡予定期間 : 全角40文字
        $extra = "";
        $arg["data"]["YOTEIKIKAN"] = knjCreateTextBox($objForm, $model->field["YOTEIKIKAN"], "YOTEIKIKAN", 45, 80, $extra);
        
        //不合格通知予定日 : 全角10文字
        $extra = "";
        $arg["data"]["YOTEIBI"] = knjCreateTextBox($objForm, $model->field["YOTEIBI"], "YOTEIBI", 20, 20, $extra);
        
        //連絡電話番号用 : 全角15文字
        $extra = "";
        $arg["data"]["DENWABANGO"] = knjCreateTextBox($objForm, $model->field["DENWABANGO"], "DENWABANGO", 20, 30, $extra);

        //印刷ボタン
        //$extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $extra = "onclick=\"return btn_submit('print');\"";
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
        knjCreateHidden($objForm, "PRGID", "KNJL850H");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl850hindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        
        //チェック処理後に帳票出力
        $arg["print"] = $model->print == "on" ? "newwin('" . SERVLET_URL . "');" :"";
        $model->print = "off";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl850hForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

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
?>
