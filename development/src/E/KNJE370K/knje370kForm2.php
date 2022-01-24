<?php

require_once('for_php7.php');

class knje370kForm2
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knje370kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //警告メッセージが表示される場合、またはリロードした場合
        if (isset($model->warning) || $model->cmd == "change_group") {
            $Row =& $model->field;
        } else {
            $query = knje370kQuery::getSelectData($model, 1);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        //学校グループコード
        $extra = "";
        $arg["data"]["COLLEGE_GRP_CD"]   = knjCreateTextBox($objForm, $Row["COLLEGE_GRP_CD"], "COLLEGE_GRP_CD", 2, 2, $extra);

        //学校グループ名称
        $extra = "";
        $arg["data"]["COLLEGE_GRP_NAME"] = knjCreateTextBox($objForm, $Row["COLLEGE_GRP_NAME"], "COLLEGE_GRP_NAME", 20, 20, $extra);

        //学校系列コンボ
        $opt = array();
        $result = $db->query(knje370kQuery::getSchoolGroup());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array( "label" => $row["LABEL"],
                          "value" => $row["VALUE"] );
        }
        $extra = " onchange=\"return btn_submit('change_group')\" ";
        $arg["data"]["SCHOOL_GROUP"] = knjCreateCombo($objForm, "SCHOOL_GROUP", $model->field["SCHOOL_GROUP"], $opt, $extra, 1);

        //学校一覧取得
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

        $result = $db->query(knje370kQuery::getSchoolList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if ($model->cmd == 'change_group') {
                if (!in_array($row["SCHOOL_CD"], $selectLeft)) {
                    $opt_right[] = array(   "label" => $row["SCHOOL_CD"]."　".$row["SCHOOL_NAME"], 
                                            "value" => $row["SCHOOL_CD"]    );
                }
            } else {
                if ($model->cmd != 'updEdit' && strlen($row["COLLEGE_GRP_CD"]) > 0) {
                    $opt_left[] = array("label" => $row["SCHOOL_CD"]."　".$row["SCHOOL_NAME"], 
                                        "value" => $row["SCHOOL_CD"]    );
                } else {
                    if (!in_array($row["SCHOOL_CD"], $selectLeft)) {
                        $opt_right[] = array("label" => $row["SCHOOL_CD"]."　".$row["SCHOOL_NAME"], 
                                             "value" => $row["SCHOOL_CD"]    );
                    }
                }
            }
        }
        $result->free();

        //左リストで選択されたものを再セット
        if ($model->cmd == 'change_group' || $model->cmd == 'updEdit') {
            foreach ($selectLeft as $key => $selectSchCd) {
                $opt_left[] = array("label" => $selectLeftText[$key], "value" => $selectSchCd);
            }
        }

        $extraLeft = "ondblclick=\"move1('right');\"";
        $extraRight = "ondblclick=\"move1('left');\"";
        $arg["main_part"] = array( "LEFT_LIST"   => "対象学校一覧",
                                   "RIGHT_LIST"  => "学校一覧",
                                   "LEFT_PART"   => knjCreateCombo($objForm, "LEFT_PART", "left", $opt_left, $extraLeft." multiple style=\"WIDTH:100%; HEIGHT:330px\"", 15),
                                   "RIGHT_PART"  => knjCreateCombo($objForm, "RIGHT_PART", "left", $opt_right, $extraRight." multiple style=\"WIDTH:100%; HEIGHT:330px\"", 15),
                                   "SEL_ADD_ALL" => knjCreateBtn($objForm, "sel_add_all2", "≪", "onclick=\"return moves('left');\""),
                                   "SEL_ADD"     => knjCreateBtn($objForm, "sel_add2", "＜", "onclick=\"return move1('left');\""),
                                   "SEL_DEL"     => knjCreateBtn($objForm, "sel_del2", "＞", "onclick=\"return move1('right');\""),
                                   "SEL_DEL_ALL" => knjCreateBtn($objForm, "sel_del_all2", "≫", "onclick=\"return moves('right');\"")
                                );

        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectLeft");
        knjCreateHidden($objForm, "selectLeftText");

        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit") {
            $arg["jscript"] = "window.open('knje370kindex.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje370kForm2.html", $arg);
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
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    $result->free();
}

?>
