<?php

require_once('for_php7.php');

class knjz402j_2Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz402j_2index.php", "", "edit");
        
        //DB接続
        $db = Query::dbCheckOut();
        
        if (isset($model->viewcd) && !isset($model->warning) && ($model->cmd != "class")) {
            //教育課程用
            if (strlen($model->subclasscd) > 6) {
                $Row = $db->getRow(knjz402j_2Query::getRow2($model->subclasscd, $model->viewcd, $model), DB_FETCHMODE_ASSOC);
            } else {
                $Row = $db->getRow(knjz402j_2Query::getRow($model->subclasscd, $model->viewcd, $model), DB_FETCHMODE_ASSOC);
            }
        } else {
            $Row =& $model->field;
        }

        //教育課程対応
        /*if ($model->Properties["useCurriculumcd"] == '1') {
            if (strlen($Row["CLASSCD"]) > 2) {
                $Row["CLASSCD"] = substr($Row["CLASSCD"], 0, 2);
            }
        }*/
        //教科コンボ
        $query = knjz402j_2Query::getClassMst($model);
        $extra = "onchange=\"return btn_submit('class');\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $Row["CLASSCD"], $extra, 1, "blank");

        //科目コンボ
        $query = knjz402j_2Query::IsNameMst044($Row["CLASSCD"], $model);
        if ($db->getOne($query) > 0) {
            $query = knjz402j_2Query::getSubClassMst044($Row["CLASSCD"], $model);
        } else {
            $query = knjz402j_2Query::getSubClassMst($Row["CLASSCD"], $model);
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $value = $model->classcd."-".$model->school_kind."-".$model->curriculum_cd."-".$model->subclasscd;
            if ($model->cmd == "class") {
                makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], "", 1, "blank");
            } else {
                if($model->field["SUBCLASSCD"] == "") {
                    makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $value, "", 1, "blank");
                } else {
                    $value = $model->field["SUBCLASSCD"];
                    makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $value, "", 1, "blank");
                }
            }
        } else {
            makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $Row["SUBCLASSCD"], "", 1, "blank");
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if (strlen($Row["VIEWCD"]) > 4) {
                $Row["VIEWCD"] = substr($Row["CLASSCD"], 0, 2).substr($Row["VIEWCD"], 6);
            }
            if (strstr($Row["VIEWCD"], '-')) {
                $Row["VIEWCD"] = "";
            }
        }
        //観点コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "VIEWCD",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => substr($Row["VIEWCD"] ,2) ));
        $arg["data"]["VIEWCD"] = $objForm->ge("VIEWCD");

        //観点名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "VIEWNAME",
                            "size"        => 48,
                            "maxlength"   => 75,
                            "value"       => $Row["VIEWNAME"] ));
        $arg["data"]["VIEWNAME"] = $objForm->ge("VIEWNAME");

        //観点略称
        $arg["data"]["VIEWABBV"] = knjCreateTextBox($objForm, $Row["VIEWABBV"], "VIEWABBV", 32, 32, "");

        //学校校種コンボ
        $query = knjz402j_2Query::getSchoolKind($model);
        $Row["SCHOOL_KIND"] = ($Row["SCHOOL_KIND"] != "") ? $Row["SCHOOL_KIND"] : $model->schkind;
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $Row["SCHOOL_KIND"], "", 1, "blank");

        //表示順
        $objForm->ae( array("type"        => "text",
                            "name"        => "SHOWORDER",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["SHOWORDER"] ));
        $arg["data"]["SHOWORDER"] = $objForm->ge("SHOWORDER");

        //重み
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["WEIGHT"] = knjCreateTextBox($objForm, $Row["WEIGHT"], "WEIGHT", 2, 3, $extra);

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ402J/knjz402jindex.php?year_code=".$model->year_code;
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //一括重み登録
        $link  = REQUESTROOT."/Z/KNJZ402J_3/knjz402j_3index.php?year_code=".$model->year_code."&mode=1";
        $link .= "&SEND_selectSchoolKind=".$model->selectSchoolKind;
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_weight"] = knjCreateBtn($objForm, "btn_weight", "一括重み登録", $extra);


        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_code",
                            "value"     => $model->year_code
                            ) );
        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){ 
            $arg["reload"]  = "parent.left_frame.location.href='knjz402j_2index.php?cmd=list';";
        }
        View::toHTML($model, "knjz402j_2Form2.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
