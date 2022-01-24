<?php

require_once('for_php7.php');

class knjd411Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd411index.php", "", "edit");

        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //グループ情報
        $getGroupRow = array();
        $getGroupRow = $db->getRow(knjd411Query::getViewGradeKindSchreg($model, "set"), DB_FETCHMODE_ASSOC);
        if ($model->schregno) {
            $getGroupName = $db->getOne(knjd411Query::getGroupcd($model, $getGroupRow["GAKUBU_SCHOOL_KIND"], $getGroupRow["CONDITION"], $getGroupRow["GROUPCD"]));
            if ($getGroupName) {
                $arg["GROUP_NAME"] = '履修科目グループ:'.$getGroupName;
            } else {
                $arg["GROUP_NAME"] = '履修科目グループ未設定';
            }
            $getConditionName = $db->getOne(knjd411Query::getConditionName($model, $getGroupRow["CONDITION"]));
            $arg["CONDITION_NAME"] = ($getConditionName) ? '('.$getConditionName.')' : "";
        }

        //履修科目設定表示
        $rirekiCnt = makeList($arg, $db, $model);
        
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && isset($model->getSetSubclasscd) && isset($model->getUnitcd) && !isset($model->warning)) || !isset($model->schregno)){
            $Row = $db->getRow(knjd411Query::getDataGradeKindSchreg($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row =& $model->field;
        }

        //入力項目タイトル等
        //科目名
        $arg["data"]["SUBCLASS_NAME"] = $db->getOne(knjd411Query::getSubclassMst($model, $model->getSetSubclasscd));
        
        //単元
        $arg["data"]["UNIT_NAME_SET"] = $db->getOne(knjd411Query::getYmstUnitName($model, $Row, $model->getSetSubclasscd));
        
        //個別単元名
        if ($model->getUnitcd) {
            $extra = " onkeypress=\" btn_keypress();\"";
        } else {
            $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        }
        $arg["data"]["UNITNAME"] = knjCreateTextBox($objForm, $Row["UNITNAME"], "UNITNAME", 45, 90, $extra);

        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "UNITCD", $model->getUnitcd);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd411Form1.html", $arg);
    }
}

//履修科目一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knjd411Query::getViewGradeKindSchreg($model, "");
    $result = $db->query($query);
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //科目名取得
        $rowlist["SET_SUBCLASSCD"] = $rowlist["CLASSCD"].'-'.$rowlist["SCHOOL_KIND"].'-'.$rowlist["CURRICULUM_CD"].'-'.$rowlist["SUBCLASSCD"];
        $rowlist["SUBCLASS_NAME"] = $db->getOne(knjd411Query::getSubclassMst($model, $rowlist["SET_SUBCLASSCD"]));
        //単元
        $rowlist["UNIT_NAME"] = $db->getOne(knjd411Query::getYmstUnitName($model, $rowlist, $rowlist["SET_SUBCLASSCD"]));
        //個別単元名
        $rowlist["UNIT_NAME_ONLY"] = $db->getOne(knjd411Query::getDatUnitName($model, $rowlist, $rowlist["SET_SUBCLASSCD"]));
        
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
