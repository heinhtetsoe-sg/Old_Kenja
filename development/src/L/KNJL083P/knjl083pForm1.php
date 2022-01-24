<?php

require_once('for_php7.php');

class knjl083pForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("sel", "POST", "knjl083pindex.php", "", "sel");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl083pQuery::getNameMst($model->ObjYear, "L003");
        $extra = "Onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分
        $namecd1 = ($model->applicantdiv == "2") ? "L004" : "L024";
        $query = knjl083pQuery::getNameMst($model->ObjYear, $namecd1);
        $extra = "Onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //リストタイトル用
        $arg["LEFT_TITLE"]  = "手続き済者一覧";
        $arg["RIGHT_TITLE"] = "合格者一覧";

        //対象者・合格者一覧
        $opt_left = $opt_right = array();
        $query = knjl083pQuery::GetLeftList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["PRO_DATE"] = str_replace("-","/",$row["PRO_DATE"]);
            if ($row["LEFT_FLG"] == "1") {
                //対象者一覧
                $opt_left[]  = array("label" => $row["EXAMNO"]."：".$row["NAME"]."：".$row["PRO_DATE"],
                                     "value" => $row["EXAMNO"]);
            } else {
                //合格者一覧
                $opt_right[] = array("label" => $row["EXAMNO"]."：".$row["NAME"],
                                     "value" => $row["EXAMNO"]);
            }
        }
        $result->free();

        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_PART", "left", $opt_left, $extra, 30);

        //合格者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_PART", "right", $opt_right, $extra, 30);

        //追加ボタン（全て）
        $extra = "onclick=\"return move3('sel_add_all','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //追加ボタン（一部）
        $extra = "onclick=\"return move3('left','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //削除ボタン（一部）
        $extra = "onclick=\"return move3('right','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //削除ボタン（全て）
        $extra = "onclick=\"return move3('sel_del_all','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //手続日
        $value = ($model->pro_date != "") ? $model->pro_date : str_replace("-","/",CTRL_DATE);
        $arg["PRO_DATE"] = View::popUpCalendar2($objForm, "PRO_DATE", $value, "", "", "");

        //更新ボタン
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

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjl083pForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
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

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
