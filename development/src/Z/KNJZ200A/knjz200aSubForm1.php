<?php

require_once('for_php7.php');

class knjz200aSubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjz200aindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //学年情報
        $getGradeRow = array();
        $getGradeRow = $db->getRow(knjz200aQuery::getIBGrade($model, "name"), DB_FETCHMODE_ASSOC);
        $arg["GRADEINFO"] = CTRL_YEAR.'年度　　'.$getGradeRow["LABEL"];

        //単位マスタ取得
        if($model->cmd == "replace"){
            if (isset($model->ibgrade_course) && !isset($model->warning)){
                $Row = $db->getRow(knjz200aQuery::getSubQuery1($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->replace["field"];
            }
        } else {
            $Row =& $model->replace["field"];
        }

        //編集項目選択
        $setMaxCount = 2;
        $setcheckAllCount = 1;
        
        for ($i=0; $i<$setMaxCount; $i++)
        {
            $extra  = ($model->replace["check"][$i] == "1") ? "checked" : "";
            if ($i==$setcheckAllCount) $extra .= " onClick=\"return check_all(this);\"";

            $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra, "");
        }

        //必要時間
        $extra = "onblur=\"checkDecimal(this)\" STYLE=\"text-align: right\"";
        $arg["data"]["NEED_TIME"] = knjCreateTextBox($objForm, $Row["NEED_TIME"], "NEED_TIME", 5, 5, $extra);

        //学年リストToリスト作成
        makeGradeList($objForm, $arg, $db, $model);

        //科目リストToリスト作成
        makeSubclassList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz200aSubForm1.html", $arg); 
    }
}

//学年リストToリスト作成
function makeGradeList(&$objForm, &$arg, $db, $model)
{
    //一括処理選択時の学年情報
    $array = explode(",", $model->replace["selectdata_course"]);
    if ($array[0] == "") $array[0] = $model->ibgrade_course;

    //学年一覧取得
    $result = $db->query(knjz200aQuery::getIBGrade($model, "grade_list"));
    $course_left = $course_right = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        if(!in_array($row["VALUE"], $array)){
            $course_right[] = array("label" => $row["LABEL"],
                                    "value" => $row["VALUE"]);
        } else {
            $course_left[]  = array("label" => $row["LABEL"],
                                    "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //学年一覧リストを作成する//
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move('left','course','left_course','right_course',1)\"";
    $arg["data"]["RIGHT_COURSE"] = knjCreateCombo($objForm, "right_course", "", $course_right, $extra, 10);

    //学年一覧リストを作成する//
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move('right','course','left_course','right_course',1)\"";
    $arg["data"]["LEFT_COURSE"] = knjCreateCombo($objForm, "left_course", "", $course_left, $extra, 10);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('sel_add_all','course');\"";
    $arg["button"]["COURSE_ADD_ALL"] = knjCreateBtn($objForm, "course_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('left','course');\"";
    $arg["button"]["COURSE_ADD"] = knjCreateBtn($objForm, "course_add", "＜", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('right','course');\"";
    $arg["button"]["COURSE_DEL"] = knjCreateBtn($objForm, "course_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('sel_del_all','course');\"";
    $arg["button"]["COURSE_DEL_ALL"] = knjCreateBtn($objForm, "course_del_all", ">>", $extra);
}

//科目リストToリスト作成
function makeSubclassList(&$objForm, &$arg, $db, $model)
{
    //一括処理選択時の科目情報
    $array = explode(",", $model->replace["selectdata_subclass"]);
    if ($array[0] == "") $array[0] = $model->ibclasscd.'-'.$model->ibprg_course.'-'.$model->ibcurriculum_cd.'-'.$model->ibsubclasscd;

    //科目一覧取得
    $result = $db->query(knjz200aQuery::getIBSubclasscd($model, "list"));
    $subclass_left = $subclass_right = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        if(!in_array($row["VALUE"], $array)){
            $subclass_right[] = array("label" => $row["LABEL"],
                                      "value" => $row["VALUE"]);
        } else {
            $subclass_left[]  = array("label" => $row["LABEL"],
                                      "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //科目一覧リストを作成する//
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move('left','subclass','left_subclass','right_subclass',1)\"";
    $arg["data"]["RIGHT_SUBCLASS"] = knjCreateCombo($objForm, "right_subclass", "", $subclass_right, $extra, 15);

    //対象科目一覧リストを作成する//
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move('right','subclass','left_subclass','right_subclass',1)\"";
    $arg["data"]["LEFT_SUBCLASS"] = knjCreateCombo($objForm, "left_subclass", "", $subclass_left, $extra, 15);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('sel_add_all','subclass');\"";
    $arg["button"]["SUBCLASS_ADD_ALL"] = knjCreateBtn($objForm, "subclass_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('left','subclass');\"";
    $arg["button"]["SUBCLASS_ADD"] = knjCreateBtn($objForm, "subclass_add", "＜", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('right','subclass');\"";
    $arg["button"]["SUBCLASS_DEL"] = knjCreateBtn($objForm, "subclass_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('sel_del_all','subclass');\"";
    $arg["button"]["SUBCLASS_DEL_ALL"] = knjCreateBtn($objForm, "subclass_del_all", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit()\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $link = REQUESTROOT."/Z/KNJZ200A/knjz200aindex.php?cmd=back";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"window.open('$link','_self');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata_course");
    knjCreateHidden($objForm, "selectdata_subclass");
}
?>
