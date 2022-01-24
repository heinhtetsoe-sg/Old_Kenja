<?php

require_once('for_php7.php');


class knje370eForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje370eForm1", "POST", "knje370eindex.php", "", "knje370eForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_YEAR",
                            "value"      => CTRL_YEAR ) );

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_SEMESTER",
                            "value"     => CTRL_SEMESTER ) );

        //選考分類コンボ
        $query = knje370eQuery::getNameMst('E054');
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SELECT_DIV", $model->field["SELECT_DIV"], $extra, 1);

        //評定読替の項目を表示するかしないかのフラグ
        if ($model->Properties["hyoteiYomikae"] == '1') {
            $arg["hyoteiYomikae"] = '1';
        }
        if ($model->Properties["useProvFlg"] == 1) {
            $arg["data"]["HYOTEI_KARI"] = '仮';
        }
        //評定読替チェックボックス
        $extra  = ($model->field["HYOTEI"] == "on" || !$model->cmd) ? "checked" : "";
        $extra .= " id=\"HYOTEI\"";
        $arg["data"]["HYOTEI"] = knjCreateCheckBox($objForm, "HYOTEI", "on", $extra, "");

        //DB切断
        Query::dbCheckIn($db);

        //印刷ボタンを作成する
        $objForm->ae( array("type"           => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "', '');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);


        //終了ボタンを作成する
        $objForm->ae( array("type"           => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJE370E"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useCurriculumcd"                 , $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useClassDetailDat"               , $model->Properties["useClassDetailDat"]);
        knjCreateHidden($objForm, "useProvFlg"                      , $model->Properties["useProvFlg"]);
        knjCreateHidden($objForm, "gaihyouGakkaBetu"                , $model->Properties["gaihyouGakkaBetu"]);
        knjCreateHidden($objForm, "tyousasyoNotPrintAnotherStudyrec", $model->Properties["tyousasyoNotPrintAnotherStudyrec"]);
        knjCreateHidden($objForm, "useAssessCourseMst"              , $model->Properties["useAssessCourseMst"]);
        knjCreateHidden($objForm, "useMaruA_avg"                    , $model->Properties["useMaruA_avg"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje370eForm1.html", $arg); 
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $gradeH3 = "";
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["SCHOOL_KIND"] == "H" && (int)$row["GRADE_CD"] == 3) $gradeH3 = $row["VALUE"];
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "GRADE" && strlen($gradeH3)) ? $gradeH3 : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
