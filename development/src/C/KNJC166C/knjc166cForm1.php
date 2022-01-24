<?php

require_once('for_php7.php');


class knjc166cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc166cForm1", "POST", "knjc166cindex.php", "", "knjc166cForm1");

        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学年
        $query = knjc166cQuery::getValidGrade($model);
        $extra = " onchange=\"return btn_submit('knjc166c')\" ";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);
        
        //学校 日大判定
        $schoolName = $db->getOne(knjc166cQuery::getSchoolName());
        if ($schoolName == "nichi-ni") {
            $arg["data"]["KAIKIN_TITLE"] = "皆勤種別　　　";
            $query = knjc166cQuery::getNichiniKaikinCdList($model);
        }
        
        //皆勤種別
        else{
            $arg["data"]["KAIKIN_TITLE"] = "皆勤・精勤種別";
            $query = knjc166cQuery::getKaikinCdList($model);
        }
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["KAIKIN_CD"], "KAIKIN_CD", $extra, 1);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC166C");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "STAFFCD", STAFFCD);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc166cForm1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
