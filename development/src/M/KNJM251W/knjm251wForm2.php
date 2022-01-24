<?php

require_once('for_php7.php');

class knjm251wForm2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjm251windex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル設定
        $ChosenData = makeTitle($arg, $db, $model);

        //教科名称取得
        $className = $db->getOne(knjm251wQuery::GetClassName($model));
        $arg["rightclasscd"] = $className;

        //科目リストToリスト作成
        $leftCnt = makeSubclassList($objForm, $arg, $db, $model);
        
        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) { //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjm251windex.php?cmd=list&init=1', 'left_frame')";
        }

        View::toHTML($model, "knjm251wForm2.html", $arg); 
    }
}

//タイトル設定
function makeTitle(&$arg, $db, $model)
{
    $ChosenData = array();
    $ChosenData = $db->getRow(knjm251wQuery::getChosenData($model, $model->subclasscd),DB_FETCHMODE_ASSOC);
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        $arg["info"]    = array("TOP"        => "レポート読替科目 : ".$ChosenData["CLASSCD"]."-".$ChosenData["SCHOOL_KIND"]."-".$ChosenData["CURRICULUM_CD"]."-".$ChosenData["SUBCLASSCD"]."&nbsp;&nbsp;".$ChosenData["SUBCLASSNAME"],
                                "LEFT_LIST"  => "レポート科目",
                                "RIGHT_LIST" => "科目一覧" );
    } else {
        $arg["info"]    = array("TOP"        => "レポート読替科目 : ".$ChosenData["SUBCLASSCD"]."&nbsp;&nbsp;".$ChosenData["SUBCLASSNAME"],
                                "LEFT_LIST"  => "レポート科目",
                                "RIGHT_LIST" => "科目一覧" );
    }
    return $ChosenData;
}

//科目リストToリスト作成
function makeSubclassList(&$objForm, &$arg, $db, &$model)
{
    $leftCnt = 0;   //合併登録件数
    $opt_left = $opt_right = array();
    if (isset($model->subclasscd)) {

        $result      = $db->query(knjm251wQuery::selectQuery($model, $model->subclasscd, $model->rightclasscd, $model->school_kind, $model->curriculum_Cd));
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
    $arg["main_part"]["LEFT_PART"]   = knjCreateCombo($objForm, "classyear", "right", $opt_left, $extra, 20);
    //科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
    $arg["main_part"]["RIGHT_PART"]  = knjCreateCombo($objForm, "classmaster", "left", $opt_right, $extra, 20);

    //各種ボタン
    $extra = "onclick=\"return moves('sel_add_all');\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

    $extra = "onclick=\"return move('left');\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

    $extra = "onclick=\"return move('right');\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

    $extra = "onclick=\"return moves('sel_del_all');\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

    return $leftCnt;
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    //hidden
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
