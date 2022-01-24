<?php

require_once('for_php7.php');
class knjf175cForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjf175cForm1", "POST", "knjf175cindex.php", "", "knjf175cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //校種コンボ
        $query = knjf175cQuery::getSchkind();
        $extra = "onchange=\"return btn_submit('knjf175c');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1, "");

        //開始日付作成
        $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["SDATE"];
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);

        //終了日付作成
        $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);

        //欠席者一覧印刷チェックボックス
        $arg["data"]["PRINT"] = knjCreateCheckBox($objForm, "PRINT", 'on', "id='PRINT'");

        //印影出力チェックボックス
        $query = knjf175cQuery::getStampCnt($model);
        $StampCnt = $db->getOne($query);
        if ($StampCnt > 0) {
            $query = knjf175cQuery::getStampName($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $extra = "id='PRINT_STAMP_{$row["SEQ"]}'";
                $name = "PRINT_STAMP_{$row["SEQ"]}";
                $row["PRINT_STAMP_NAME"] = knjCreateCheckBox($objForm, $name, $row["SEQ"], $extra);
                $arg["data2"][] = $row;
            }
            $arg["ineiKuma"] = "1";
            $ineiNotdata     = 0;
        } else {
            $arg["data"]["PRINT_STAMP_KOUCHOU"]     = knjCreateCheckBox($objForm, "PRINT_STAMP_KOUCHOU", 'on', "id='PRINT_STAMP_KOUCHOU'");
            $arg["data"]["PRINT_STAMP_KYOUTOU"]     = knjCreateCheckBox($objForm, "PRINT_STAMP_KYOUTOU", 'on', "id='PRINT_STAMP_KYOUTOU'");
            $arg["data"]["PRINT_STAMP_HOKENSYUJI"]  = knjCreateCheckBox($objForm, "PRINT_STAMP_HOKENSYUJI", 'on', "id='PRINT_STAMP_HOKENSYUJI'");
            $arg["data"]["PRINT_STAMP_YOUGOKYOUYU"] = knjCreateCheckBox($objForm, "PRINT_STAMP_YOUGOKYOUYU", 'on', "id='PRINT_STAMP_YOUGOKYOUYU'");
            $arg["ineiNotKuma"] = "1";
            $ineiNotdata        = 1;
        }

        //ボタン作成
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJF175C");
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "INEINOTDATA", $ineiNotdata);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf175cForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
