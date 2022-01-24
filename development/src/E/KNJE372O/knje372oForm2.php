<?php
class knje372oForm2
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knje372oindex.php", "", "edit");

        $db = Query::dbCheckOut();

        $model->oyear = ($model->oyear) ? $model->oyear : CTRL_YEAR;

        //推薦枠コード
        $query = knje372oQuery::getAftRecommendationLimitMst($model->oyear,"","");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["RECOMMENDATION_CD"], "RECOMMENDATION_CD", $extra, 1, "BLANK");

        //課程学科
        $query = knje372oQuery::getCourseMajorMst($model->oyear,"","");
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSEMAJOR"], "COURSEMAJOR", $extra, 1, "BLANK");

        //コース
        $query = knje372oQuery::getCourseCodeMst($model->oyear,"","");
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSECODE"], "COURSECODE", $extra, 1, "BLANK");

        //教科コンボ
        $query = knje372oQuery::getClassMst($model->oyear);
        $value = $model->field["CLASSCD"].$model->field["SCHOOL_KIND"];
        $extra = " onchange=\"return btn_submit('changeClass')\" ";
        makeCmb($objForm, $arg, $db, $query, $value, "CLASSCD", $extra, 1, "BLANK");

        //科目一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectLeft");
        knjCreateHidden($objForm, "selectLeftText");
        knjCreateHidden($objForm, "hidden_recommendation_cd", $model->recommendation_cd);
        knjCreateHidden($objForm, "hidden_coursemajor", $model->coursemajor);
        knjCreateHidden($objForm, "hidden_coursecode", $model->coursecode);
        knjCreateHidden($objForm, "hidden_classcd", $model->classcd.$model->school_kind);

        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit") {
            $arg["jscript"] = "window.open('knje372oindex.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje372oForm2.html", $arg);
    }

}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $value = ($value != "") ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    $result->free();
}

function makeListToList(&$objForm, &$arg, $db, $model) {
    //科目一覧取得
    $opt_left = array();
    $opt_right = array();
    $selectLeft = array();
    $selectLeftText = array();

    //左リスト値
    if ($model->selectLeft) {
        $selectLeft = explode(",", $model->selectLeft);
    }
    //左リストTEXT
    if ($model->selectLeftText) {
        $selectLeftText = explode(",", $model->selectLeftText);
    }

    $result = $db->query(knje372oQuery::getSubclassList($model,'',''));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($row["RECOMMENDATION_CD"] != "" && $row["RECOMMENDATION_CD"] == $model->field["RECOMMENDATION_CD"]) {
            $opt_left[] = array("label" => $row["LABEL"], 
                                "value" => $row["VALUE"]);
        } else {
            if (!in_array($row["RECOMMENDATION_CD"], $selectLeft)) {
                $opt_right[] = array("label" => $row["LABEL"], 
                                     "value" => $row["VALUE"]);
            }
        }
    }
    $result->free();

    $extraLeft = "ondblclick=\"move1('right');\"";
    $extraRight = "ondblclick=\"move1('left');\"";
    $arg["main_part"] = array( "LEFT_LIST"   => "更新対象科目",
                                "RIGHT_LIST"  => "科目一覧",
                                "LEFT_PART"   => knjCreateCombo($objForm, "LEFT_PART", "left", $opt_left, $extraLeft." multiple style=\"WIDTH:100%; HEIGHT:330px\"", 15),
                                "RIGHT_PART"  => knjCreateCombo($objForm, "RIGHT_PART", "left", $opt_right, $extraRight." multiple style=\"WIDTH:100%; HEIGHT:330px\"", 15),
                                "SEL_ADD_ALL" => knjCreateBtn($objForm, "sel_add_all2", "≪", "onclick=\"return moves('left');\""),
                                "SEL_ADD"     => knjCreateBtn($objForm, "sel_add2", "＜", "onclick=\"return move1('left');\""),
                                "SEL_DEL"     => knjCreateBtn($objForm, "sel_del2", "＞", "onclick=\"return move1('right');\""),
                                "SEL_DEL_ALL" => knjCreateBtn($objForm, "sel_del_all2", "≫", "onclick=\"return moves('right');\"")
                            );
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
