<?php

require_once('for_php7.php');

class knjl041tForm1
{
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl041tQuery::GetName("L003",$model->ObjYear));
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
        $query = knjl041tQuery::getMajorcd();
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

        /**********/
        /* リスト */
        /**********/
        //タイトル
        $arg["LEFT_TITLE"]  = "欠席者一覧";
        $arg["RIGHT_TITLE"] = "志願者一覧";

        //左のリスト
        $opt_left = $tmp_id = array();
        $result = $db->query(knjl041tQuery::GetLeftList($model));

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
        //右のリスト
        $opt_right = array();
        $result = $db->query(knjl041tQuery::GetRightList($model));

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
                            "extrahtml"   => "checked id=\"csv_item\"",
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
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl041tindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl041tForm1.html", $arg);
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
