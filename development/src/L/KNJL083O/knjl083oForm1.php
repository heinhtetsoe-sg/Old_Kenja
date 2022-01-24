<?php

require_once('for_php7.php');

class knjl083oForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $result = $db->query(knjl083oQuery::GetName("L003",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }
        $result->free();

        $model->applicantdiv = (!strlen($model->applicantdiv)) ? $opt[0]["value"] : $model->applicantdiv;

        $extra = "Onchange=\"btn_submit('main');\"";
        $arg["TOP"]["APPLICANTDIV"] = createCombo($objForm, "APPLICANTDIV", $model->applicantdiv, $opt, $extra, 1);


        //対象者コンボ
        $opt = array();
        $opt[0] = array("label" => "3：手続者"    , "value" => "3");

        $model->appli_type = (!strlen($model->appli_type)) ? "3" : $model->appli_type;
        $extra = "Onchange=\"btn_submit('main');\"";
        $arg["TOP"]["APPLI_TYPE"] = createCombo($objForm, "APPLI_TYPE", $model->appli_type, $opt, $extra, 1);


        //入試区分
        $opt    = array();
        $opt[0] = array("label" => "9:全体", "value" => "9");
        $model->testdiv = "9";

        $extra = "Onchange=\"btn_submit('main');\"";
        $arg["TOP"]["TESTDIV"] = createCombo($objForm, "TESTDIV", $model->testdiv, $opt, $extra, 1);


        //リストタイトル用
        $left_title = array("1" => "特待対象者一覧", "2" => "手続延期者一覧", "3" => "手続者一覧");
        $arg["LEFT_TITLE"]  = $left_title[$model->appli_type];
        //リストタイトル用
        $right_title = array("1" => "合格者一覧(2科目成績順)", "2" => "合格者一覧(受験番号順)", "3" => "合格者一覧(受験番号順)　繰上、特別合格含む");
        $arg["RIGHT_TITLE"] = $right_title[$model->appli_type];


        //対象者一覧
        $opt_left = $tmp_id = array();
        $result = $db->query(knjl083oQuery::GetLeftList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array("label" => $row["EXAMNO"]."：".$row["ADJOURNMENT"].$row["NAME"],
                                "value" => "9999"."-".$row["EXAMNO"]."-".$row["ENTDIV"]);
            $tmp_id[]   = $row["EXAMNO"];
        }
        $result->free();

        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["LEFT_PART"] = createCombo($objForm, "SPECIALS", "left", $opt_left, $extra, 30);


        //合格者一覧
        $opt_right = array();
        $result = $db->query(knjl083oQuery::GetRightList($model));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["EXAMNO"], $tmp_id)) {
                $opt_right[]    = array("label" => $row["EXAMNO"]."：".$row["ADJOURNMENT"].$row["NAME"],
                                        "value" => "9999"."-".$row["EXAMNO"]."-".$row["ENTDIV"]);
            }
        }
        $result->free();

        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["RIGHT_PART"] = createCombo($objForm, "APPROVED", "right", $opt_right, $extra, 30);

        // ≪ボタン
        $extra = "onclick=\"return move3('sel_add_all','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = createBtn($objForm, "sel_add_all", "≪", $extra);
        // ＜ボタン
        $extra = "onclick=\"return move3('left','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_ADD"] = createBtn($objForm, "sel_add", "＜", $extra);
        // ＞ボタン
        $extra = "onclick=\"return move3('right','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_DEL"] = createBtn($objForm, "sel_del", "＞", $extra);
        // ≫ボタン
        $extra = "onclick=\"return move3('sel_del_all','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = createBtn($objForm, "sel_del_all", "≫", $extra);

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
                            "extrahtml"   => "checked",
                            "value"       => "1" ) );
        $arg["CSV_ITEM"] = $objForm->ge("csvfile").$objForm->ge("btn_csv").$objForm->ge("chk_header");

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl083oindex.php", "", "sel");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl083oForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //保存ボタン
    $arg["button"]["BTN_OK"] = createBtn($objForm, "btn_keep", "更 新", "onclick=\"return doSubmit();\"");
    //取消ボタン
    $arg["button"]["BTN_CLEAR"] = createBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["BTN_END"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    //入学クラスボタン
    $extra = $disabled ." onclick=\"loadwindow('knjl083oindex.php?cmd=classKakutei', 200, 50, 700, 400);\"";
    $arg["button"]["btn_entClass"] = knjCreateBtn($objForm, "btn_entClass", "正規合格者のクラス確定", $extra);

    //入学許可書ボタン
    $extra  = " onclick=\"wopen('".REQUESTROOT."/L/KNJL343/knjl343index.php?cmd=";
    $extra .= "&SEND_PRGID=KNJL343&SEND_AUTH=" .AUTHORITY;
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_callL343"] = knjCreateBtn($objForm, "btn_callL343", "入学許可書", $extra);
}

//Hidden作成
function makeHidden(&$objForm)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("selectdata"));
    $objForm->ae(createHiddenAe("selectdata2"));
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
