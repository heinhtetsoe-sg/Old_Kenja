<?php

require_once('for_php7.php');

class knjl080rForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //CSV取込・書出しボタンチェック
        if ($model->csv_radio == "2") {
            $arg["reload"] = "clickRadio('2')";
        } else {
            $arg["reload"] = "clickRadio('1')";
        }

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl080rQuery::GetName("L003", $model->ObjYear, $model->fixApplicantDiv);
        $extra = "Onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分
        $opt = array();
        $opt_limit_date = array();
        $result = $db->query(knjl080rQuery::GetName("L004",$model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_limit_date[$row["NAMECD2"]][$model->appli_type] = ($model->appli_type == "1") ? $row["NAMESPARE1"] : $row["NAMESPARE3"];
            $opt[]  = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
            if ($model->testdiv == "" && $row["NAMESPARE2"] == '1') $model->testdiv = $row["NAMECD2"];
        }
        if (!strlen($model->testdiv)) $model->testdiv = $opt[0]["value"];
        $extra = "Onchange=\"btn_submit('main');\"";
        $arg["TOP"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->testdiv, $opt, $extra, 1);

        //対象者コンボ
        //2:一般入試
        if ($model->testdiv === '2') {
            $opt = array();
            $opt[] = array("label" => "1：一次手続者" , "value" => "1");
            $opt[] = array("label" => "2：二次手続者" , "value" => "2");
            if (!strlen($model->appli_type)) $model->appli_type = "1";
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        //1:推薦入試
        } else {
            $opt = array();
            $opt[] = array("label" => "2:推薦者手続" , "value" => "2");
            $model->appli_type = "2";
        }
        $extra = "Onchange=\"btn_submit('main');\"";
        $arg["TOP"]["APPLI_TYPE"] = knjCreateCombo($objForm, "APPLI_TYPE", $model->appli_type, $opt, $extra, 1);

        //手続日付
        $value = ($model->pro_date != "") ? $model->pro_date : str_replace("-","/",CTRL_DATE);
        $arg["TOP"]["PRO_DATE"] = View::popUpCalendar2($objForm, "PRO_DATE", $value, "", "", "");

        //リストタイトル用
        //2:一般入試
        if ($model->testdiv === '2') {
            $left_title  = array("1" => "一次手続者一覧" , "2" => "二次手続者一覧");
            $right_title = array("1" => "合格者一覧（辞退者は除く）" , "2" => "合格者一覧（辞退者は除く）");
        //1:推薦入試
        } else {
            $left_title  = array("2" => "手続者一覧");
            $right_title = array("2" => "推薦入試合格者一覧（辞退者は除く）");
        }
        $arg["LEFT_TITLE"]  = $left_title[$model->appli_type];
        $arg["RIGHT_TITLE"] = $right_title[$model->appli_type];

        //対象者・合格者一覧
        $opt_left = $opt_right = array();
        $result = $db->query(knjl080rQuery::GetLeftList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["PRODATE"] = str_replace("-","/",$row["PRODATE"]);
            if ($row["DIV"] == "1") {
                //対象者一覧
                $opt_left[]  = array("label" => $row["EXAMNO"]."：".$row["PRODATE1"].$row["NAME"]."：".$row["PRODATE"], "value" => $row["EXAMNO"]);
            } else {
                //合格者一覧
                $opt_right[] = array("label" => $row["EXAMNO"]."：".$row["PRODATE1"].$row["NAME"]."：".$row["PRODATE"], "value" => $row["EXAMNO"]);
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

        //CSV用
        $opt = array(1, 2);
        $model->csv_radio = ($model->csv_radio == "") ? "1" : $model->csv_radio;
        $extra = array("id=\"csv1\" onclick=\"btn_submit('main')\"", "id=\"csv2\" onclick=\"btn_submit('main')\"");
        $radioArray = knjCreateRadio($objForm, "csv", $model->csv_radio, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        
        //手続済み・全てチェックボックス
        if ($model->csv_radio !== '1') {
            //初期値
            if ($model->tetuduki_zumi_check == "" && $model->tetuduki_all_check == "") {
                $model->tetuduki_zumi_check = '1';
            }
            $arg["torikomi"] = "1";
            $extra = "id=\"TETUDUKI_ZUMI_CHECK\" onclick=\"check(this);\"";
            if ($model->tetuduki_zumi_check == "1") {
                $extra .= "checked='checked' ";
            } else {
                $extra .= "";
            }
            $arg["TETUDUKI_ZUMI_CHECK"] = knjCreateCheckBox($objForm, "TETUDUKI_ZUMI_CHECK", "1", $extra);
            
            $extra = "id=\"TETUDUKI_ALL_CHECK\" onclick=\"check(this);\"";
            if ($model->tetuduki_all_check == "1") {
                $extra .= "checked='checked' ";
            } else {
                $extra .= "";
            }
            $arg["TETUDUKI_ALL_CHECK"] = knjCreateCheckBox($objForm, "TETUDUKI_ALL_CHECK", "1", $extra);
        } else {
            $arg["kakidasi"] = "1";
        }
        
        $extra = "";
        $objFile = knjCreateFile($objForm, "csvfile", 409600, $extra);

        $extra = "onclick=\"return btn_submit('csv');\"";
        $objCsv = knjCreateBtn($objForm, "btn_csv", "実 行", $extra);

        $extra = "checked";
        $objHead = knjCreateCheckBox($objForm, "chk_header", "1", $extra);

        $arg["CSV_ITEM"] = $objFile.$objCsv.$objHead;

        //保存ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //一次・二次手続締切日
        //2:一般入試
        if ($model->testdiv === '2') {
            $one_two = ($model->appli_type == "1") ? "一次手続締切日：" : "二次手続締切日：";
        //1:推薦入試
        } else {
            $one_two = '推薦者手続締切日：';
        }
        $arg["LIMIT_DATE"] = $one_two .$opt_limit_date[$model->testdiv][$model->appli_type];

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl080rindex.php", "", "sel");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl080rForm1.html", $arg); 
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
