<?php

require_once('for_php7.php');

class knjl363wForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl363windex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //出力CSV選択ラジオボタン 1:KNJL361W 2:KNJL362W 3:KNJL363W 4:KNJL364W 5:KNJL365W 6:KNJL366W
        $opt = $extra = array();
        $requestroot = REQUESTROOT;
        for ($i = 1; $i <= 6; $i++) {
            $opt[]      = $i;
            $extra[]    = "id=\"CSV_PRG{$i}\" onclick =\"Page_jumper('{$requestroot}');\"";
        }
        $radioArray = knjCreateRadio($objForm, "CSV_PRG", 3, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //報告取り下げボタン
        $extra = "onclick=\"return btn_submit('cancel');\"";
        $arg["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "報告取り下げ", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('". SERVLET_URL ."');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL363W");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl363wForm1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //学校一覧取得
    $opt_right = $opt_left = array();
    $query = knjl363wQuery::getSchoolData($model);
    $result = $db->query($query);
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setExeDate = $row["EXECUTE_DATE"];
        if ($setExeDate) {
            list($setDate, $setTime) = explode(".", $setExeDate);
            $dispDateTime = "【".date("Y/m/d H:i", strtotime($setDate))."】";
        } else {
            $dispDateTime = "";
        }
        if (in_array($row["EDBOARD_SCHOOLCD"], $selectdata)) {
            $opt_left[] = array('label' => $row["MITEISHUTSU"].$row["EDBOARD_SCHOOLCD"].":".$row["EDBOARD_SCHOOLNAME"].$dispDateTime,
                                'value' => $row["EDBOARD_SCHOOLCD"]);
        } else {
            $opt_right[] = array('label' => $row["MITEISHUTSU"].$row["EDBOARD_SCHOOLCD"].":".$row["EDBOARD_SCHOOLNAME"].$dispDateTime,
                                 'value' => $row["EDBOARD_SCHOOLCD"]);
        }
    }
    $result->free();

    //一覧リスト
    $extra = "multiple style=\"width:580\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //対象一覧リスト
    $extra = "multiple style=\"width:580\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象選択ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //対象取消ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
?>
