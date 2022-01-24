<?php

require_once('for_php7.php');


class knjl362aForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl362aForm1", "POST", "knjl362aindex.php", "", "knjl362aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //extra
        $extra = " onChange=\"return btn_submit('knjl362a');\"";

        //入試制度コンボの設定
        $query = knjl362aQuery::getApctDiv("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボの設定
        $query = knjl362aQuery::getTestDiv("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //ラジオボタン 1:出身学校 2:塾 3:受験者 4:保護者
        $opt_div = array(1, 2, 3, 4);
        $model->field["FINSCHOOLDIV"] = ($model->field["FINSCHOOLDIV"] == "") ? "3" : $model->field["FINSCHOOLDIV"];
        $extra = array("id=\"FINSCHOOLDIV1\" onClick=\"return btn_submit('knjl362a')\"", "id=\"FINSCHOOLDIV2\" onClick=\"return btn_submit('knjl362a')\"", "id=\"FINSCHOOLDIV3\" onClick=\"return btn_submit('knjl362a')\"", "id=\"FINSCHOOLDIV4\" onClick=\"return btn_submit('knjl362a')\"");
        $radioArray = knjCreateRadio($objForm, "FINSCHOOLDIV", $model->field["FINSCHOOLDIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /**********/
        /* リスト */
        /**********/
        //一覧リスト取得
        $result = $db->query(knjl362aQuery::getList($model));
        $row1 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row1[]    = array("label" => $row["LABEL"], 
                               "value" => $row["VALUE"]);
        }
        $result->free();
        if ($model->field["FINSCHOOLDIV"] == "3") {
            $arg["data"]["NAME_LIST"] = '受験者';
        } else if ($model->field["FINSCHOOLDIV"] == "4") {
            $arg["data"]["NAME_LIST"] = '保護者';
        } else if ($model->field["FINSCHOOLDIV"] == "1") {
            $arg["data"]["NAME_LIST"] = '出身校';
        } else {
            $arg["data"]["NAME_LIST"] = '塾';
        }
        //一覧リスト作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "SCHOOL_NAME",
                            "extrahtml"  => "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"move('left','SCHOOL_SELECTED','SCHOOL_NAME',1)\"",
                            "size"       => "20",
                            "options"    => isset($row1)?$row1:array()));
        $arg["data"]["SCHOOL_NAME"] = $objForm->ge("SCHOOL_NAME");
        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "SCHOOL_SELECTED",
                            "extrahtml"  => "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"move('right','SCHOOL_SELECTED','SCHOOL_NAME',1)\"",
                            "size"       => "20",
                            "options"    => array()));
        $arg["data"]["SCHOOL_SELECTED"] = $objForm->ge("SCHOOL_SELECTED");
        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('sel_del_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );
        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");
        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('sel_add_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );
        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");
        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('right','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );
        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");
        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('left','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );
        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        /************/
        /* 開始位置 */
        /************/
        //開始位置（行）コンボボックスを作成する
        $row = array(array('label' => "１行",'value' => 1),
                     array('label' => "２行",'value' => 2),
                     array('label' => "３行",'value' => 3),
                     array('label' => "４行",'value' => 4),
                     array('label' => "５行",'value' => 5),
                     array('label' => "６行",'value' => 6),
                     array('label' => "７行",'value' => 7),
                     array('label' => "８行",'value' => 8),
                    );
        $objForm->ae( array("type"       => "select",
                            "name"       => "POROW",
                            "size"       => "1",
                            "value"      => $model->field["POROW"],
                            "options"    => isset($row)?$row:array()));
        $arg["data"]["POROW"] = $objForm->ge("POROW");
        //開始位置（列）コンボボックスを作成する
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

        /**********/
        /* ボタン */
        /**********/
        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );
        $arg["button"]["btn_print"] = $objForm->ge("btn_print");
        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        /**********/
        /* hidden */
        /**********/
        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJL362A"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => $model->ObjYear,
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );  

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl362aForm1.html", $arg); 
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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
