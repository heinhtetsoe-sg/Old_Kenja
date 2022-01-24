<?php

require_once('for_php7.php');

class knjl082fForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入金日
        $value = ($model->pro_date != "") ? $model->pro_date : str_replace("-","/",CTRL_DATE);
        $arg["TOP"]["PRO_DATE"] = View::popUpCalendar2($objForm, "PRO_DATE", $value, "", "", "");

        //入試制度
        $query = knjl082fQuery::GetName("L003", $model->ObjYear);
        $extra = "Onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分
        $opt = array();
        $opt_limit_date = array();
        $namecd1 = ($model->applicantdiv == "2") ? "L004" : "L024";
        $result = $db->query(knjl082fQuery::GetName($namecd1, $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_limit_date[$row["NAMECD2"]] = $row["NAMESPARE3"];
            $opt[]  = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
        }
        $result->free();
        if (!strlen($model->testdiv)) $model->testdiv = $opt[0]["value"];
        $extra = "Onchange=\"btn_submit('main');\"";
        $arg["TOP"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->testdiv, $opt, $extra, 1);

        //入試回数コンボボックス
        if ($model->applicantdiv == "2") {
            $extra = "Onchange=\"btn_submit('main');\"";
            $query = knjl082fQuery::getTestdiv0($model->ObjYear, $model->testdiv);
            makeCmb($objForm, $arg, $db, $query, "TESTDIV0", $model->testdiv0, $extra, 1);
            $arg["TOP"]["TESTDIV0_LABEL"] = "※ 入試回数";
        }

        //リストタイトル用
        $arg["LEFT_TITLE"]  = "手続者一覧";
        $arg["RIGHT_TITLE"] = "合格者一覧（辞退者は除く）";

        //対象者・合格者一覧
        $opt_left = $opt_right = array();
        $query = knjl082fQuery::GetLeftList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["PRO_DATE"] = str_replace("-","/",$row["PRO_DATE"]);
            $entranceMark = $row["ENTRANCE_FLG"] == "1" ? "●" : "";
            $setPayMoney = $row["PAY_MONEY"];
            $setPayMoney = strlen($setPayMoney) ? number_format($setPayMoney) : "";
            $setPayMoney = strlen($setPayMoney) ? "(\\{$setPayMoney})" : "";

            if ($row["LEFT_FLG"] == "1") {
                //対象者一覧
                $opt_left[]  = array("label" => $row["RECEPTNO"]."：".$row["NAME"]."：".$row["PRO_DATE"].$setPayMoney.$entranceMark,
                                     "value" => $row["RECEPTNO"].":".$row["EXAMNO"]);
            } else {
                //合格者一覧
                $opt_right[] = array("label" => $row["RECEPTNO"]."：".$row["NAME"]."：".$setPayMoney.$entranceMark,
                                     "value" => $row["RECEPTNO"].":".$row["EXAMNO"]);
            }
        }
        $result->free();

        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "SPECIALS", "left", $opt_left, $extra, 30);

        //合格者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "APPROVED", "right", $opt_right, $extra, 30);

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
        knjCreateHidden($objForm, "selectdata2");

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl082findex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl082fForm1.html", $arg); 
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
