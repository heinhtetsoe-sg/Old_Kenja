<?php
class knjlz02iForm1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjlz02iindex.php", "", "sel");
        $db = Query::dbCheckOut();

        //年度コンボボックス
        $query = knjlz02iQuery::selectYearQuery($model);
        $result = $db->query($query);
        $opt = array();
        $j=0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["ENTEXAMYEAR"],
                           "value" => $row["ENTEXAMYEAR"]);
            if ($model->examyear==$row["ENTEXAMYEAR"]) {
                $j++;
            }
        }
        if ($j==0) {
            $model->examyear = $opt[0]["value"];
        }
        $extra = "onchange=\"return btn_submit('');\"";
        $yearAddCombo = knjCreateCombo($objForm, "examyear", $model->examyear, $opt, $extra, 1);
        //年度追加
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $yearAddText = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);
        //年度追加ボタン
        $extra = "onclick=\"return add('');\"";
        $yearAddBtn = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["year"] = array("COMBO"        => $yearAddCombo,
                             "VAL"          => $yearAddText,
                             "BTN_YEAR_ADD" => $yearAddBtn
                            );
        
        //入試制度コンボボックス
        $extra = "onChange=\"return btn_submit('chgAppDiv')\"";
        $query = knjlz02iQuery::getNameMst($model->examyear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボ
        $model->testdiv = ($model->cmd == "chgAppDiv") ? "" : $model->testdiv;
        $extra = " onchange=\"return btn_submit('sel');\" ";
        $query = knjlz02iQuery::getTestDiv($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, "1");

        //リスト作成
        makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm);

        $arg["info"] = array("TOP"        => "対象年度",
                             "LEFT_LIST"  => "年度科目一覧",
                             "RIGHT_LIST" => "科目一覧");

        $arg["finish"]  = $objForm->get_finish();

        $arg["jscript"] = " setFirstData(); ";

        Query::dbCheckIn($db);

        // テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "sel.html", $arg);
    }
}

//コンボ作成関数
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($name == 'APPLICANTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{

    //保存ボタン
    $extra = "onclick=\"return doSubmit();\"";
    $btnUpd = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $btnDel = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $btnEnd = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    $arg["button"] = array("BTN_YEAR_ADD" =>$btnYearAdd,
                            "BTN_UPD"      =>$btnUpd,
                            "BTN_CLEAR"    =>$btnDel,
                            "BTN_END"      =>$btnEnd);
}

function makeList(&$objForm, &$arg, $db, $model)
{
    //年度科目一覧取得
    $query = knjlz02iQuery::selectQuery($model);
    
    $result = $db->query($query);
    $opt_right = $opt_left = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($row["TESTSUBCLASSCD"] != "") {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        } else {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();
    
    //対象年度科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"";
    $subclassyear = knjCreateCombo($objForm, "subclassyear", "left", $opt_left, $extra, 20);

    //科目一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"";
    $subclassmaster = knjCreateCombo($objForm, "subclassmaster", "right", $opt_right, $extra, 20);

    //移動ボタン
    $extra = "onclick=\"moves('left');\"";
    $sel_add_all = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    $extra = "onclick=\"move1('left');\"";
    $sel_add = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    $extra = "onclick=\"move1('right');\"" ;
    $sel_del = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    $extra = "onclick=\"moves('right');\"";
    $sel_del_all = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);


    $arg["main_part"] = array( "LEFT_PART"   => $subclassyear,
                                "RIGHT_PART"  => $subclassmaster,
                                "SEL_ADD_ALL" => $sel_add_all,
                                "SEL_ADD"     => $sel_add,
                                "SEL_DEL"     => $sel_del,
                                "SEL_DEL_ALL" => $sel_del_all);
}

function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "firstData");
    knjCreateHidden($objForm, "rightMoveData");
}
