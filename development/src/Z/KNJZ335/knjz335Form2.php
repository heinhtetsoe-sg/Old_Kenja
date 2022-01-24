<?php

require_once('for_php7.php');

class knjz335Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz335index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        $staffName = $db->getOne(knjz335Query::getStaffName($model));
        $arg["info"]["TOP"] = "職員名 : ".$staffName;

        //学校リストToリスト作成
        $leftCnt = makeSubclassList($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) { //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjz335index.php?cmd=list&init=1', 'left_frame')";
        }

        View::toHTML($model, "knjz335Form2.html", $arg); 
    }
}

//学校リストToリスト作成
function makeSubclassList(&$objForm, &$arg, $db, &$model)
{
    $leftCnt = 0;
    $opt_left = $opt_right = array();
    if (isset($model->leftStaffCd)) {

        $result = $db->query(knjz335Query::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["STAFFCD"]) {
                $opt_left[]  = array("label" => $row["EDBOARD_SCHOOLCD"]."：".$row["EDBOARD_SCHOOLNAME"], 
                                     "value" => $row["EDBOARD_SCHOOLCD"]);
            } else {
                $opt_right[]  = array("label" => $row["EDBOARD_SCHOOLCD"]."：".$row["EDBOARD_SCHOOLNAME"], 
                                     "value" => $row["EDBOARD_SCHOOLCD"]);
            }
        }
        $result->free();
    }

    //学校一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"";
    $arg["main_part"]["LEFT_PART"]   = knjCreateCombo($objForm, "CHANGESCHOOL", "right", $opt_left, $extra, 20);
    //学校一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
    $arg["main_part"]["RIGHT_PART"]  = knjCreateCombo($objForm, "EDBOARDSCHOOL", "left", $opt_right, $extra, 20);
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
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更新", "onclick=\"return doSubmit();\"");
    //取消ボタン
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}

?>
