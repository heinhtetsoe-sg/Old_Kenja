<?php

class knjl325kForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl325kForm1", "POST", "knjl325kindex.php", "", "knjl325kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = $model->ObjYear;

        //分コンボ作成
        $this->makeCmb($objForm, $arg, $model, $db);

        //中高判別フラグを作成する
        $jhflg = 0;
        $row = $db->getOne(knjl325kQuery::GetJorH());
        if ($row == 1){
            $jhflg = 1;
        }else {
            $jhflg = 2;
        }
        $objForm->ae($this->createHiddenAe("JHFLG", $jhflg));

        //ボタン作成
        $this->makeButton($objForm, $arg, $model);

        //hiddenを作成
        $this->makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl325kForm1.html", $arg); 
    }

    //コンボ作成
    function makeCmb(&$objForm, &$arg, &$model, $db)
    {
        //試験区分
        $opt_testdiv = array();
        $optcnt = 0;
        $opt_testdiv[] = array("label" => "附属推薦者",
                               "value" => "3");
        $result = $db->query(knjl325kQuery::GetTestdiv($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_testdiv[] = array("label" => $row["NAME1"],
                                   "value" => $row["NAMECD2"]);
            $optcnt += ($optcnt == 0) ? 1 : 0;
        }
        if (!$model->testdiv) $model->testdiv = $opt_testdiv[$optcnt]["value"];

        $result->free();
        $arg["data"]["TESTDIV"] = $this->createCombo($objForm, "TESTDIV", $model->testdiv, $opt_testdiv, "", 1);

        //特別理由区分
        $opt = array();
        $value_flg = false;
        $query = knjl325kQuery::getSpecialReasonDiv($model);
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
        $objForm->ae($this->createHiddenAe("PRGID", "KNJL325K"));        //プログラムID
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
