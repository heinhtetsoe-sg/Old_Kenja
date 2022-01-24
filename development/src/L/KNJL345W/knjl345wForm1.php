<?php

require_once('for_php7.php');

class knjl345wForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl345windex.php", "", "main");

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //出力CSV選択ラジオボタン 1:KNJL341W 2:KNJL342W 3:KNJL343W 4:KNJL344W 5:KNJL345W 6:KNJL346W
        $opt = $extra = array();
        $requestroot = REQUESTROOT;
        for ($i = 1; $i <= 6; $i++) {
            $opt[]      = $i;
            $extra[]    = "id=\"CSV_PRG{$i}\" onclick =\"Page_jumper('{$requestroot}');\"";
        }
        $radioArray = knjCreateRadio($objForm, "CSV_PRG", 5, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //V_SCHOOL_MSTから学校コードを取得
        $model->schoolcd = $db->getOne(knjl345wQuery::getSchoolInfo($model));

        //報告表示
        $query = knjl345wQuery::getReport($model);
        $getHoukoku = $db2->getRow($query, DB_FETCHMODE_ASSOC);
        if ($getHoukoku["EXECUTE_DATE"]) {
            list ($execute_time , $hoge) = explode(".", $getHoukoku["EXECUTE_DATE"]);
            $setDateTime = date("Y/m/d H:i", strtotime($execute_time));
            $arg["REPORT"] = $setDateTime." 報告者：".$getHoukoku["STAFFNAME"]." 報告済み　　";
        }
        $btnFixedDisabeld = !$getHoukoku["EXECUTE_DATE"] ? "" : " disabled ";

        //県への報告
        $extra = "onclick=\"return btn_submit('houkoku');\"".$btnFixedDisabeld;
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告", $extra);

        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL345W");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //印刷処理
        $arg["print"] = $model->print == "on" ? "newwin('".SERVLET_URL."');" :"";
        $model->print = "off";

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl345wForm1.html", $arg);
    }
}
?>
