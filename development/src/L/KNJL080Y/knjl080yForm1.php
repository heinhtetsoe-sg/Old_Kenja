<?php

require_once('for_php7.php');

class knjl080yForm1
{
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl080yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $model->field["TESTDIV"] = "";
        $namecd = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjl080yQuery::getNameMst($namecd, $model->ObjYear);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //対象者コンボ
        $opt = array();
        $opt[0] = array("label" => "1：手続者"    , "value" => "1");
        if (!strlen($model->appli_type)) $model->appli_type = "1";

        $extra = "Onchange=\"btn_submit('main');\"";
        $arg["TOP"]["APPLI_TYPE"] = knjCreateCombo($objForm, "APPLI_TYPE", $model->appli_type, $opt, $extra, 1);

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
        $result = $db->query(knjl080yQuery::GetLeftList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examno = preg_replace('/^0*/', '', $row["EXAMNO"]);
            //make_space() ネスト用のスペースを生成
            $opt_left[] = array("label" => make_space('aaa', $examno) . $examno."：".$row["NAME"] . "：" . str_replace("-", "/", $row["PROCEDUREDATE"]) . "(\\".number_format($row["PAY_MONEY"]).")", "value" => $row["EXAMNO"].":".$row["PAY_MONEY"]);
            $tmp_id[]   = $row["EXAMNO"];
        }
        $result->free();

        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "SPECIALS", "left", $opt_left, $extra, 25);

        //合格者一覧
        $opt_right = array();
        $result = $db->query(knjl080yQuery::GetRightList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["EXAMNO"], $tmp_id)) {
                $examno = preg_replace('/^0*/', '', $row["EXAMNO"]);
                //make_space() ネスト用のスペースを生成
                $opt_right[]    = array("label" => make_space('aaa', $examno) . $examno."：".$row["NAME"] . "(\\".number_format($row["PAY_MONEY"]).")", "value" => $row["EXAMNO"].":".$row["PAY_MONEY"]);
            }
        }
        $result->free();

        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "APPROVED", "right", $opt_right, $extra, 25);

        //追加ボタン
        $extra = "onclick=\"return move3('sel_add_all','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        $extra = "onclick=\"return move3('left','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        $extra = "onclick=\"return move3('right','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        $extra = "onclick=\"return move3('sel_del_all','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

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
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");

        Query::dbCheckIn($db);

        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl080yindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl080yForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
