<?php

require_once('for_php7.php');

class knjl072sForm1
{
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl072sQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
        }

        if (!strlen($model->applicantdiv)) $model->applicantdiv = $opt[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLICANTDIV",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->applicantdiv,
                            "options"    => $opt));
        $arg["TOP"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        //学科コンボ
        $opt = array();
        $value_flg = false;
        $query = knjl072sQuery::getMajorcd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->majorcd == $row["VALUE"]) $value_flg = true;
        }
        $model->majorcd = ($model->majorcd && $value_flg) ? $model->majorcd : $opt[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "MAJORCD",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->majorcd,
                            "options"    => $opt));
        $arg["TOP"]["MAJORCD"] = $objForm->ge("MAJORCD");

        //学科コンボ
        $opt = array();
        $opt[] = array('label' => '1', 'value' => '1');
        if ($model->applicantdiv != "1") {
            $opt[] = array('label' => '2', 'value' => '2');
            $opt[] = array('label' => '3', 'value' => '3');
            $opt[] = array('label' => '4', 'value' => '4');
            $opt[] = array('label' => '5', 'value' => '5');
            $opt[] = array('label' => '6', 'value' => '6');
        }

        $model->wishno = $model->wishno ? $model->wishno : 1;

        $objForm->ae( array("type"       => "select",
                            "name"       => "WISHNO",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->wishno,
                            "options"    => $opt));
        $arg["TOP"]["WISHNO"] = $objForm->ge("WISHNO");

        /********************/
        /* チェックボックス */
        /********************/
        //追検査者のみ
        if ($model->supplement == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " onClick=\"btn_submit('main')\"";
        $extra .= " id=\"SUPPLEMENT\"";
        $arg["data"]["SUPPLEMENT"] = knjCreateCheckBox($objForm, "SUPPLEMENT", "1", $extra);

        //募集枠数
        $arg["data"]["CAPACITY"] = 0;
        $result = $db->query(knjl072sQuery::getCapacity($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["CAPACITY"] = $row["CAPACITY"];
        }

        /**********/
        /* リスト */
        /**********/
        //タイトル
        $arg["LEFT_TITLE"]  = "合格者一覧";
        $arg["RIGHT_TITLE"] = "受検者一覧";

        //合格者一覧
        $opt_left = $tmp_id = array();
        $result = $db->query(knjl072sQuery::GetLeftList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examno = preg_replace('/^0*/', '', $row["EXAMNO"]);
            $opt_left[] = array("label" => make_space('aaaaa', $examno) . $examno."：".$row["NAME"], "value" => $row["EXAMNO"]);
            $tmp_id[]   = $row["EXAMNO"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "SPECIALS",
                            "size"        => "30",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','SPECIALS','APPROVED',1);\"",
                            "options"     => $opt_left));
        //候補者一覧
        $opt_right = array();
        $result = $db->query(knjl072sQuery::GetRightList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["EXAMNO"], $tmp_id)) {
                $examno = preg_replace('/^0*/', '', $row["EXAMNO"]);
                $opt_right[]    = array("label" => make_space('aaaaa', $examno) . $examno."：".$row["NAME"], "value" => $row["EXAMNO"]);
            }
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "APPROVED",
                            "size"        => "30",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','SPECIALS','APPROVED',1);\"",
                            "options"     => $opt_right));

        $result->free();
        Query::dbCheckIn($db);

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move3('sel_add_all','SPECIALS','APPROVED',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move3('left','SPECIALS','APPROVED',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move3('right','SPECIALS','APPROVED',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move3('sel_del_all','SPECIALS','APPROVED',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("SPECIALS"),
                                   "RIGHT_PART"  => $objForm->ge("APPROVED"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //CSV用
        $objForm->ae( array("type"        => "file",
                            "name"        => "csvfile",
                            "size"        => "409600") );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_csv",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "chk_header",
                            "extrahtml"   => "checked id=\"chk_header\"",
                            "value"       => "1" ) );
        $arg["CSV_ITEM"] = $objForm->ge("csvfile").$objForm->ge("btn_csv").$objForm->ge("chk_header");

        //保存ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata2"
                            ) );
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl072sindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl072sForm1.html", $arg);
    }
}

/****************************/
/* ネスト用のスペースの生成 */
/****************************/
function make_space($longer_name, $name) {
    $mojisuu_no_sa = strlen($longer_name) - strlen($name);
    for ($i = 0; $i < $mojisuu_no_sa; $i++) {
        $spaces .= '&nbsp;';
    }
    return $spaces;
}
?>
