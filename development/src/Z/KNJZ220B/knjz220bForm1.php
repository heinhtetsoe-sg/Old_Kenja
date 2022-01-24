<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：テスト項目の設定をDBからでなく、直接設定 山城 2004/10/26 */
/* ･NO002：テスト項目の設定を３学期は、期末のみ設定 山城 2004/11/29 */
/********************************************************************/

class knjz220bForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjz220bForm1", "POST", "knjz220bindex.php", "", "knjz220bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //教科コンボ
        $extra = " onchange=\"return btn_submit('changeClass');\" ";
        $query = knjz220bQuery::getClass($model);
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1);

        //科目コンボ
        $extra = " onchange=\"return btn_submit('changeSub');\" ";
        $query = knjz220bQuery::getSubclass($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1);

        //科目リストToリスト作成
        $leftCnt = makeSubclassList($objForm, $arg, $db, $model);

        //ボタン
        makeButton($objForm, $arg);
        //Hidden
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz220bForm1.html", $arg); 
    }
}

//科目リストToリスト作成
function makeSubclassList(&$objForm, &$arg, $db, &$model)
{
    $leftCnt = 0;   //合併登録件数
    $opt_left = $opt_right = array();
    if (isset($model->field["SUBCLASSCD"])) {

        $result = $db->query(knjz220bQuery::getSubclassList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["ATTEND_SUBCLASSCD"]) {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $opt_left[]  = array("label" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]);
                } else {
                    $opt_left[]  = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["SUBCLASSCD"]);
                }
                $leftCnt++;
            } else {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $opt_right[]  = array("label" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]);
                } else {
                    $opt_right[]  = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"], 
                                         "value" => $row["SUBCLASSCD"]);
                }
            }
        }
        $result->free();
    }

    //合併元科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"";
    $arg["main_part"]["LEFT_PART"]   = knjCreateCombo($objForm, "leftList", "right", $opt_left, $extra, 20);
    //科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
    $arg["main_part"]["RIGHT_PART"]  = knjCreateCombo($objForm, "rightList", "left", $opt_right, $extra, 20);
    //各種ボタン
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return moves('sel_add_all');\"");
    $arg["main_part"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add", "＜", "onclick=\"return move('left');\"");
    $arg["main_part"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del", "＞", "onclick=\"return move('right');\"");
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return moves('sel_del_all');\"");

    return $leftCnt;
}


//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $opt[] = array("label" => "", "value" => "00");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}


?>
