<?php

require_once('for_php7.php');

class knjl072mForm1
{
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        if ($model->judgement == 2) {
            $arg["COMMENT"] = "※選択した受験番号の並び順が補員順位となります。";
        }

        //入試制度
        $opt = array();
        $result = $db->query(knjl072mQuery::GetName("L003",$model->ObjYear));
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

        //合否区分コンボ
        $opt = array();
        $value_flg = false;
        $query = knjl072mQuery::getJudgement();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            $left_title[$row["VALUE"]] = $row["NAME1"]; //リストのタイトル用
            if ($model->judgement == $row["VALUE"]) $value_flg = true;
        }
        $model->judgement = ($model->judgement && $value_flg) ? $model->judgement : $opt[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "JUDGEMENT",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->judgement,
                            "options"    => $opt));
        $arg["TOP"]["JUDGEMENT"] = $objForm->ge("JUDGEMENT");

        //リストタイトル用
        $leftSortTitle  = ($model->judgement == 1) ? "成績" : "補員";
        $rightSortTitle = ($model->judgement == 4) ? "補員" : "成績";
        $arg["LEFT_TITLE"]  = $left_title[$model->judgement] . "者一覧（並び順：" .$leftSortTitle ."順位）";
        $arg["RIGHT_TITLE"] = "候補者一覧（並び順：" .$rightSortTitle ."順位）";

        //リストtoリストのソートをするかどうかのフラグ
        if ($model->judgement == 2) {
            $sort_flg = 'false';
        } else {
            $sort_flg = 1;
        }

        //左リスト
        $opt_left = array();
        $result = $db->query(knjl072mQuery::GetLeftList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examno = preg_replace('/^0*/', '', $row["EXAMNO"]);
            $opt_left[] = array("label" => sprintf("%03d", $row["RANK"]) . ' ' . make_space('aaa', $examno) . $examno."：".$row["NAME"],
                                "value" => sprintf("%03d", $row["RANK"]) . '_' . $row["EXAMNO"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "SPECIALS",
                            "size"        => "25",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','SPECIALS','APPROVED',$sort_flg);\"",
                            "options"     => $opt_left));

        //右のリストに含めない人の配列を作る
        $tmp_id = array();
        $result = $db->query(knjl072mQuery::get_dont_use_list($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmp_id[]   = $row["EXAMNO"];
        }

        //右リスト
        $opt_right = array();
        $result = $db->query(knjl072mQuery::GetRightList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["EXAMNO"], $tmp_id)) {
                $examno = preg_replace('/^0*/', '', $row["EXAMNO"]);
                $opt_right[] = array("label" => sprintf("%03d", $row["RANK"]) . ' ' . make_space('aaa', $examno) . $examno."：".$row["NAME"],
                                     "value" => sprintf("%03d", $row["RANK"]) . '_' . $row["EXAMNO"]);
            }
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "APPROVED",
                            "size"        => "25",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','SPECIALS','APPROVED',$sort_flg);\"",
                            "options"     => $opt_right));

        $result->free();
        Query::dbCheckIn($db);

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move3('sel_add_all','SPECIALS','APPROVED',$sort_flg);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move3('left','SPECIALS','APPROVED',$sort_flg);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move3('right','SPECIALS','APPROVED',$sort_flg);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move3('sel_del_all','SPECIALS','APPROVED',$sort_flg);\"" ) );

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
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl072mindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl072mForm1.html", $arg);
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
