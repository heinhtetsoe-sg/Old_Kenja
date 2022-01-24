<?php

require_once('for_php7.php');

class knjl082bForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;
        
        if($model->cmd == "change") {
            unset($model->selectdata);
            unset($model->selectdataLabel);
        }

        //手続日付
        $model->pro_date = ($model->pro_date == "") ? CTRL_DATE : $model->pro_date;
        $value = ($model->pro_date != "") ? str_replace("-","/",$model->pro_date) : str_replace("-","/",CTRL_DATE);
        $arg["TOP"]["PRO_DATE"] = View::popUpCalendar2($objForm, "PRO_DATE", $value, "", "", "");

        //手続日付読込
        $extra = "onclick=\"return btn_submit('change');\"";
        $arg["button"]["btn_dateset"] = knjCreateBtn($objForm, "btn_dateset", "日付読込", $extra);


        //入試制度
        $query = knjl082bQuery::GetName("L003", $model->ObjYear);
        $extra = "Onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分
        $opt = array();
        $opt_limit_date = array();
        $result = $db->query(knjl082bQuery::GetName("L004", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_limit_date[$row["NAMECD2"]] = $row["NAMESPARE3"];
            $opt[]  = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
        }
        $result->free();
        if (!strlen($model->testdiv)) $model->testdiv = $opt[0]["value"];
        $extra = "Onchange=\"btn_submit('change');\"";
        $arg["TOP"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->testdiv, $opt, $extra, 1);

        //リストタイトル用
        $arg["LEFT_TITLE"]  = "一次手続者一覧";
        $arg["RIGHT_TITLE"] = "合格者一覧（辞退者は除く）";

        //単願切換区分
        $opt = array();
        $query = knjl082bQuery::GetTangan($model);
        $model->tanganArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->tanganArray[$row["JUDGMENT_DIV"]] = $row;
            $opt[] = array("label" => $row["JUDGMENT_DIV"]."：".$row["JUDGMENT_COURSE_NAME"], "value" => $row["JUDGMENT_DIV"]);
        }
        $result->free();
        $model->changeSingleTestFlg = $model->changeSingleTestFlg ? $model->changeSingleTestFlg : $opt[0]["value"];
        $extra = "Onchange=\"btn_submit('main');\"";
        $arg["TOP"]["CHANGE_SINGLE_TEST_FLG"] = knjCreateCombo($objForm, "CHANGE_SINGLE_TEST_FLG", $model->changeSingleTestFlg, $opt, $extra, 1);

        //対象者・合格者一覧
        //合格者一覧(右のリスト)
        $opt_left = $opt_right = array();
        $query = knjl082bQuery::GetLeftList($model, "right");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["PRODATE"] = str_replace("-","/",$row["PRODATE"]);
            $tanText = $row["PROCEDUREDIV1"] == "1" ? "【単】" : "";
            $opt_right[] = array("label" => $row["EXAMNO"]."：".$tanText.$row["NAME"]."：".$row["PRODATE"], "value" => $row["EXAMNO"].":".$row["PRODATE"]);
        }
        
        //対象者一覧(左のリスト)
        $selectdata = ($model->selectdata != "") ? explode(",",$model->selectdata) : array();
        $selectdataLabel = ($model->selectdataLabel != "") ? explode(",",$model->selectdataLabel) : array();
        if ($model->selectdata != "" && $model->selectdataLabel != "") {
            for ($i = 0; $i < get_count($selectdata); $i++) {
                $opt_left[] = array('label' => $selectdataLabel[$i],
                                    'value' => $selectdata[$i]);
            }
        } else {
            $query = knjl082bQuery::GetLeftList($model, "left");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["PRODATE"] = str_replace("-","/",$row["PRODATE"]);
                $tanText = $row["PROCEDUREDIV1"] == "1" ? "【単】" : "";
                if ($row["LEFT_FLG"] == "1") {
                    $opt_left[]  = array("label" => $row["EXAMNO"]."：".$tanText.$row["NAME"]."：".$row["PRODATE"], 
                                         "value" => $row["EXAMNO"].":".$row["PRODATE"]);
                }
            }
        }
        
        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "SPECIALS", "left", $opt_left, $extra, 30);

        //合格者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "APPROVED", "right", $opt_right, $extra, 30);

        $result->free();

        //追加ボタン
        $extra = "onclick=\"return move3('sel_add_all','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        $extra = "onclick=\"return move3('left','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        $extra = "onclick=\"return move3('right','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        $extra = "onclick=\"return move3('sel_del_all','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //保存ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //締切日
        $one_two = "手続締切日：";
        $arg["LIMIT_DATE"] = $one_two .$opt_limit_date[$model->testdiv];

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdataLabel");
        knjCreateHidden($objForm, "selectdata2");
        
        //DB切断
        Query::dbCheckIn($db);

        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl082bindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl082bForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
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
?>
