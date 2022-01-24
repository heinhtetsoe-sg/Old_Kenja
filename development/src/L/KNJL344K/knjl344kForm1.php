<?php

require_once('for_php7.php');


class knjl344kForm1
{
    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl344kForm1", "POST", "knjl344kindex.php", "", "knjl344kForm1");
        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = $model->ObjYear;

        //試験区分コンボ作成
        $this->makeTestdivCmb($objForm, $arg, $model, $db);

        /**
         * 各種帳票ラジオ
         * 1:合格者名簿１, 2:追加合格者名簿, 3:繰上合格者名簿, 4:手続者名簿, 5:入学辞退者名簿, 6:手続辞退者名簿
         * 7:入学者名簿, 8:合格者名簿, 9:スカラシップ認定者名簿, 10:不合格者名簿, 11:欠席者名簿
         */
        $opt = array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        $model->output = (!$model->output) ? 1 : $model->output;
        $extra = "onclick=\" sort_val(this.value);return btn_submit('knjl344k');\"";
        $this->createRadio($objForm, $arg, "OUTPUT", $model->output, $extra, $opt, get_count($opt));

        //追加繰上合格グループNoコンボボックスを作成する
        $query = knjl344kQuery::GetPassdiv($model);
        $disabled = ($model->output == 2 || $model->output == 3) ? "" : "disabled" ;
        $this->makeCmb($objForm, $arg, $db, $query, "GROUP_NO", "GROUP_NO", $model->passdiv, $disabled, "PASSDIV");

        //スカラシップコンボボックスを作成する---NO005
        $query = knjl344kQuery::GetScalashipdiv($model);
        $disabled = ($model->output == 9) ? "" : "disabled" ;
        $this->makeCmb($objForm, $arg, $db, $query, "NAME1", "SCALASHIPDIV", $model->scalashipdiv, $disabled, "SCALASHIPDIV");

        //中高判別フラグを作成する
        $jhflg = 0;
        $row = $db->getOne(knjl344kQuery::GetJorH());
        if ($row == 1){
            $jhflg = 1;
        }else {
            $jhflg = 2;
        }
        $objForm->ae($this->createHiddenAe("JHFLG", $jhflg));

        //NO001-->
        if (!$model->output) $model->output = 1;
        /**
         * 備考欄出力「する」「しない」チェックボックス
         */
        //合格者名簿
        $this->makeCheckbox($objForm, $arg, $model, "checked", 1, "CHECK1", "on", "");
        //手続者名簿
        $this->makeCheckbox($objForm, $arg, $model, "checked", 4, "CHECK4", "on", "");
        //入学者名簿---NO003
        $this->makeCheckbox($objForm, $arg, $model, "checked", 7, "CHECK7", "on", "");
        //不合格者名簿
        $this->makeCheckbox($objForm, $arg, $model, "checked", 10, "CHECK10", "on", "");
        //<--NO001

        //高校のみ表示項目用のdef
        if ($jhflg == 2) {
            $arg["jhflg"] = $jhflg;
        }

        //NO004-->
        //ソート用ラジオ（1:受験番号順,2:かな氏名順）
        $opt_srt = array(1, 2);
        $model->srt = (!$model->srt) ? 1 : $model->srt;
        $extra = ($model->output == 1 || $model->output == 4 || $model->output == 7 || $model->output == 9) ? "" : " disabled";
        $this->createRadio($objForm, $arg, "SORT", $model->srt, $extra, $opt_srt, get_count($opt_srt));

        //ソート指定用リストToリスト作成
        $this->makeSortList($objForm, $arg, $model);
        //<--NO004

        //ボタン作成
        $this->makeButton($objForm, $arg, $model);

        //hiddenを作成する
        $this->makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl344kForm1.html", $arg); 
    }

    //試験区分コンボ作成
    function makeTestdivCmb(&$objForm, &$arg, &$model, $db)
    {
        $opt_testdiv = array();
        $testcnt = 0;

        $result = $db->query(knjl344kQuery::GetTestdiv($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_testdiv[] = array("label" => $row["NAME1"],
                                   "value" => $row["NAMECD2"]);
            $testcnt++;
        }
        if ($testcnt == 0) {
            $opt_testdiv[$testcnt] = array("label" => "　　",
                                           "value" => "99");
        }
        $result->free();

        $model->testdiv = (!$model->testdiv) ? $opt_testdiv[0]["value"] : $model->testdiv;
        $arg["data"]["TESTDIV"] = $this->createCombo($objForm, "TESTDIV", $model->testdiv, $opt_testdiv, "onchange=\" return btn_submit('knjl344k');\"", 1);

        //特別理由区分
        $opt = array();
        $value_flg = false;
        $query = knjl344kQuery::getSpecialReasonDiv($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->special_reason_div == $row["VALUE"]) $value_flg = true;

            if ($row["NAMESPARE1"] == '1') {
                $special_reason_div = $row["VALUE"];
            }
        }
        $model->special_reason_div = (strlen($model->special_reason_div) && $value_flg) ? $model->special_reason_div : $special_reason_div;
        $extra = "";
        $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCombo($objForm, "SPECIAL_REASON_DIV", $model->special_reason_div, $opt, $extra, 1);
    }

    //各チェックボックス作成
    function makeCheckbox(&$objForm, &$arg, $model, $checked, $output, $name, $val, $multi)
    {
        $dis_check = ($model->output == $output) ? "" : " disabled";
        $arg["data"][$name] = $this->createCheckBox($objForm, $name, $val, $checked .$dis_check, $multi);
    }

    //各コンボ作成
    function makeCmb(&$objForm, &$arg, $db, $query, $label, $valname, &$modelVal, $disabled, $name)
    {
        $opt_div   = array();
        $opt_div[] = array("label" => "　　",
                           "value" => "99");

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_div[] = array("label" => $row[$label],
                               "value" => $row[$valname]);
        }

        $modelVal = (!$modelVal) ? $opt_div[0]["value"] : $modelVal;
        $result->free();

        $arg["data"][$name] = $this->createCombo($objForm, $name, $modelVal, $opt_div, $disabled, 1);
    }

    //ソート指定用リストToリスト画面作成
    function makeSortList(&$objForm, &$arg, $model)
    {
        $dis_sortb = ($model->output == 1 || $model->output == 4 || $model->output == 7) ? "" : " disabled";    //NO006
        //選択ソート一覧
        $opt_sort = $opt_left = $opt_right = array();
        $opt_sort[] = array("label" => "男女別",   "value" => "1");
        $opt_sort[] = array("label" => "専／併別", "value" => "2");
        $opt_sort[] = array("label" => "コース別", "value" => "3");

        $selectdata = ($model->selectdata != "") ? explode(",",$model->selectdata) : array();

        //選択ソート一覧
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[]  = array("label" => $opt_sort[$selectdata[$i] - 1]["label"],
                                 "value" => $opt_sort[$selectdata[$i] - 1]["value"]);
        }

        //ソート一覧
        for ($i = 0; $i < get_count($opt_sort); $i++) {
            if (in_array($opt_sort[$i]["value"],$selectdata)) {
                continue;
            }
            $opt_right[] = array("label" => $opt_sort[$i]["label"],
                                 "value" => $opt_sort[$i]["value"]);
        }

        //選択ソート一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"".$dis_sortb;
        $arg["main_part"]["LEFT_PART"] = $this->createCombo($objForm, "L_COURSE", "left", $opt_left, $extra, 7);

        //ソート一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"".$dis_sortb;
        $arg["main_part"]["RIGHT_PART"] = $this->createCombo($objForm, "R_COURSE", "left", $opt_right, $extra, 7);

        //≪ボタン
        $extra = "onclick=\"return move('sel_add_all');\"".$dis_sortb;
        $arg["main_part"]["SEL_ADD_ALL"] = $this->createBtn($objForm, "sel_add_all", "≪", $extra);

        //＜ボタン
        $extra = "onclick=\"return move('left');\"".$dis_sortb;
        $arg["main_part"]["SEL_ADD"] = $this->createBtn($objForm, "sel_add", "＜", $extra);

        //＞ボタン
        $extra = "onclick=\"return move('right');\"".$dis_sortb;
        $arg["main_part"]["SEL_DEL"] = $this->createBtn($objForm, "sel_del", "＞", $extra);

        //≫ボタン
        $extra = "onclick=\"return move('sel_del_all');\"".$dis_sortb;
        $arg["main_part"]["SEL_DEL_ALL"] = $this->createBtn($objForm, "sel_del_all", "≫", $extra);

        $objForm->ae($this->createHiddenAe("selectdata"));
    }

    //ボタン作成
    function makeButton(&$objForm, &$arg, $model)
    {
        //印刷ボタン
        $arg["button"]["btn_print"] = $this->createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
        //終了ボタン
        $arg["button"]["btn_end"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
    }

    //hidden作成
    function makeHidden(&$objForm, $model)
    {
        $objForm->ae($this->createHiddenAe("YEAR", $model->ObjYear));   //年度
        $objForm->ae($this->createHiddenAe("DBNAME", DB_DATABASE));     //DB名
        $objForm->ae($this->createHiddenAe("PRGID"));                   //プログラムID
        $objForm->ae($this->createHiddenAe("cmd"));                     //コマンド
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

    //ラジオ作成
    function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
    {
        $objForm->ae( array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));
        for ($i = 1; $i <= $count; $i++) {
            $arg["data"][$name.$i] = $objForm->ge($name, $i);
        }
    }

    //チェックボックス作成
    function createCheckBox(&$objForm, $name, $value, $extra, $multi)
    {

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));

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

}
?>
