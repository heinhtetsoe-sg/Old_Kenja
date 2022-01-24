<?php

require_once('for_php7.php');

class knjl080mForm1
{
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl080mQuery::GetName("L003",$model->ObjYear));
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

        //対象者コンボ
        $opt = array();
        $opt[0] = array("label" => "1：手続者"    , "value" => "1");

        if (!strlen($model->appli_type)) $model->appli_type = "1";

        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLI_TYPE",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->appli_type,
                            "options"    => $opt));
        $arg["TOP"]["APPLI_TYPE"] = $objForm->ge("APPLI_TYPE");

        //手続き日付
        $model->proceduredate = $model->proceduredate ? $model->proceduredate : CTRL_DATE;
        $model->proceduredate = str_replace("-", "/", $model->proceduredate);
        $arg["TOP"]["PROCEDUREDATE"] = View::popUpCalendar($objForm, "PROCEDUREDATE", $model->proceduredate);

        //リストタイトル用
        $left_title  = array("1" => "手続者一覧（入学者一覧）");
        $right_title = array("1" => "合格者一覧（辞退者は除く）");

        $arg["LEFT_TITLE"]  = $left_title[$model->appli_type];
        $arg["RIGHT_TITLE"] = $right_title[$model->appli_type];

        //対象者一覧
        $opt_left = $tmp_id = array();
        $result = $db->query(knjl080mQuery::GetLeftList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examno = preg_replace('/^0*/', '', $row["EXAMNO"]);
            //make_space() ネスト用のスペースを生成
            $opt_left[] = array("label" => make_space('aaa', $examno) . $examno."：".$row["NAME"] . "：" . str_replace("-", "/", $row["PROCEDUREDATE"]), "value" => $row["EXAMNO"]);
            $tmp_id[]   = $row["EXAMNO"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "SPECIALS",
                            "size"        => "25",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','SPECIALS','APPROVED',1);\"",
                            "options"     => $opt_left));
        //合格者一覧
        $opt_right = array();
        $result = $db->query(knjl080mQuery::GetRightList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["EXAMNO"], $tmp_id)) {
                $examno = preg_replace('/^0*/', '', $row["EXAMNO"]);
                //make_space() ネスト用のスペースを生成
                $opt_right[]    = array("label" => make_space('aaa', $examno) . $examno."：".$row["NAME"], "value" => $row["EXAMNO"]);
            }
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "APPROVED",
                            "size"        => "25",
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
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl080mindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl080mForm1.html", $arg);
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
