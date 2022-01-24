<?php

require_once('for_php7.php');


class knjz100Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成//////////////////////////////////////////////////////////////////////////////////////////////
        $arg["start"]   = $objForm->get_start("knjz100Form1", "POST", "knjz100index.php", "", "knjz100Form1");

        //中学校・塾ラジオボタン 1:中学校選択 2:塾選択
        $opt_div = array(1, 2);
        $model->field["FINSCHOOLDIV"] = ($model->field["FINSCHOOLDIV"] == "") ? "1" : $model->field["FINSCHOOLDIV"];
        $extra = array("id=\"FINSCHOOLDIV1\" onClick=\"return btn_submit('knjz100changeDiv')\"", "id=\"FINSCHOOLDIV2\" onClick=\"return btn_submit('knjz100changeDiv')\"");
        $radioArray = knjCreateRadio($objForm, "FINSCHOOLDIV", $model->field["FINSCHOOLDIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学校長宛チェックボックス
        $extra = " id=\"TO_PRINCIPAL\"";
        $arg["data"]["TO_PRINCIPAL"] = knjCreateCheckBox($objForm, "TO_PRINCIPAL", 1, $extra);

        //小学校一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
        $db = Query::dbCheckOut();
        $selectdata = explode(',', $model->field["selectdata"]);
        if ($model->field["FINSCHOOLDIV"] == "1") {
            //校種コンボ
            $arg["finschooltype"] = "1";
            $extra = "onChange=\"return btn_submit('knjz100')\"";
            $query = knjz100Query::getFinschoolTypeQuery($model);
            $useDefaultVal = $model->cmd == '' || $model->cmd == 'knjz100changeDiv' ? 1 : 0;
            $addAll = 0 == $db->getOne(knjz100Query::getFinschoolTypeNullCount($model->control["年度"])) ? "" : "ALL";
            makeCmb($objForm, $arg, $db, $query, "SELECT_FINSCHOOL_TYPE", $model->field["SELECT_FINSCHOOL_TYPE"], $extra, 1, $useDefaultVal, $addAll);
            $row2 = array();
            foreach ($selectdata as $sel) {
                $query = knjz100Query::getFinSchoolLabel($sel);
                $label = $db->getOne($query);
                if ($label != '') {
                    $row2[]    = array("label" => $label, 
                                       "value" => $sel);
                }
            }

            $query = knjz100Query::selectFinSchoolQuery($model->control["年度"], $model->field["SELECT_FINSCHOOL_TYPE"]);
            $result      = $db->query($query);   
            $row1 = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if (!in_array($row["FINSCHOOLCD"], $selectdata)) {
                    $row1[]    = array("label" => $row["FINSCHOOLCD"]."  ".$row["FINSCHOOL_NAME"], 
                                       "value" => $row["FINSCHOOLCD"]);
                }
            }
            $arg["data"]["NAME_LIST"] = '出身校';
            $result->free();
        } else {
            $result      = $db->query(knjz100Query::selectPriSchoolQuery($model->control["年度"]));   
            $row1 = array();
            $row2 = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row1[]    = array("label" => $row["PRISCHOOLCD"]."  ".$row["PRISCHOOL_NAME"], 
                                   "value" => $row["PRISCHOOLCD"]);
            }
            $arg["data"]["NAME_LIST"] = '塾';
            $result->free();
        }
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "SCHOOL_NAME",
                  "extrahtml"  => "multiple style=\"width:220px\" width=\"220px\" ondblclick=\"move('left','SCHOOL_SELECTED','SCHOOL_NAME',1)\"",
                            "size"       => "20",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["SCHOOL_NAME"] = $objForm->ge("SCHOOL_NAME");


        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "SCHOOL_SELECTED",
                  "extrahtml"  => "multiple style=\"width:220px\" width=\"220px\" ondblclick=\"move('right','SCHOOL_SELECTED','SCHOOL_NAME',1)\"",
                            "size"       => "20",
                            "options"    => isset($row2)?$row2:array()));

        $arg["data"]["SCHOOL_SELECTED"] = $objForm->ge("SCHOOL_SELECTED");


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('sel_del_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('sel_add_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('right','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('left','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //開始位置（行）コンボボックスを作成する///////////////////////////////////////////////////////////////////////
        $row = array(array('label' => "１行",'value' => 1),
              array('label' => "２行",'value' => 2),
              array('label' => "３行",'value' => 3),
              array('label' => "４行",'value' => 4),
              array('label' => "５行",'value' => 5),
              array('label' => "６行",'value' => 6),
        //			array('label' => "７行",'value' => 7),
              );

        $objForm->ae( array("type"       => "select",
                            "name"       => "POROW",
                            "size"       => "1",
                            "value"      => $model->field["POROW"],
                            "options"    => isset($row)?$row:array()));

        $arg["data"]["POROW"] = $objForm->ge("POROW");


        //開始位置（列）コンボボックスを作成する////////////////////////////////////////////////////////////////////////
        $col = array(array('label' => "１列",'value' => 1),
              array('label' => "２列",'value' => 2),
              array('label' => "３列",'value' => 3),
              );


        $objForm->ae( array("type"       => "select",
                            "name"       => "POCOL",
                            "size"       => "1",
                            "value"      => $model->field["POCOL"],
                            "options"    => isset($col)?$col:array()));

        $arg["data"]["POCOL"] = $objForm->ge("POCOL");


        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJZ100");
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        $arg["data"]["YEAR"] = $model->control["年度"];

        knjCreateHidden($objForm, "GAKKI", $model->control["学期"]);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
        knjCreateHidden($objForm, "notPrintFinschooltypeName", $model->Properties["notPrintFinschooltypeName"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz100Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $useDefaultVal, $all) {
    $opt = array();
    $opt[] = array('label' => '', 'value' => '');
    $value_flg = false;
    $defvalue = '';
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        if ($useDefaultVal && '1' == $row["IS_DEFAULT"]) $defvalue = $row["VALUE"];
    }
    $result->free();
    if ($all == "ALL") {
        $opt[] = array('label' => '-- 全て --', 'value' => '99');
        if ($value == "99") $value_flg = true;
    }
    if ($name == "SELECT_FINSCHOOL_TYPE") {
        $value = (($value === '0' || $value) && $value_flg) ? $value : ($defvalue ? $defvalue : $opt[0]["value"]);
    } else {
        $value = (($value === '0' || $value) && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
