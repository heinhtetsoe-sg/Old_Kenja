<?php
class knjl306kForm1
{
    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl306kForm1", "POST", "knjl306kindex.php", "", "knjl306kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //試験区分を作成する
        $this->makeTestdivCmb($objForm, $arg, $db, $model);

        //中高判別フラグを作成する
        $jhflg = 0;
        $row = $db->getOne(knjl306kQuery::GetJorH());
        if ($row == 1) {
            $jhflg = 1;
        } else {
            $jhflg = 2;
        }

        //帳票種別ラジオ
        $this->makeRadio($objForm, $arg, $model, $jhflg);

        //ボタン作成
        $this->makeButton($objForm, $arg, $model);

        //hiddenを作成する
        $this->makeHidden($objForm, $model, $jhflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl306kForm1.html", $arg); 
    }

    //テスト種別コンボ作成
    function makeTestdivCmb(&$objForm, &$arg, $db, &$model)
    {
        $opt_testdiv = array();
        $testcnt = 0;

        $result = $db->query(knjl306kQuery::GetTestdiv($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_testdiv[] = array("label" => $row["NAME1"],
                                   "value" => $row["NAMECD2"]);
            $testcnt++;
        }
        if ($testcnt == 0){
            $opt_testdiv[$testcnt] = array("label" => "　　",
                                           "value" => "99");
        }

        $model->testdiv = (!$model->testdiv) ? $opt_testdiv[0]["value"] : $model->testdiv;

        $result->free();
        $arg["data"]["TESTDIV"] = $this->createCombo($objForm, "TESTDIV", $model->testdiv, $opt_testdiv, "", 1);
    }

    //帳票種別ラジオ
    function makeRadio(&$objForm, &$arg, &$model, $jhflg)
    {
        //中高で帳票種別が異なる
        if ($jhflg == 1) {
            $opt_sitei[] = array("label" => "(漢字氏名○、かな氏名×) リスト", "value" => "on");
            $opt_sitei[] = array("label" => "(漢字氏名×、かな氏名○) リスト", "value" => "no");
            $opt_sitei[] = array("label" => "(漢字氏名○、かな氏名○) リスト", "value" => "oo");
        } else {
            $opt_sitei[] = array("label" => "(漢字氏名○、かな氏名○、出身学校○) リスト", "value" => "ooo");
            $opt_sitei[] = array("label" => "(漢字氏名○、かな氏名○、出身学校×) リスト", "value" => "oon");
            $opt_sitei[] = array("label" => "(漢字氏名○、かな氏名×、出身学校○) リスト", "value" => "ono");
            $opt_sitei[] = array("label" => "(漢字氏名○、かな氏名×、出身学校×) リスト", "value" => "onn");
            $opt_sitei[] = array("label" => "(漢字氏名×、かな氏名○、出身学校○) リスト", "value" => "noo");
            $opt_sitei[] = array("label" => "(漢字氏名×、かな氏名○、出身学校×) リスト", "value" => "non");
            $opt_sitei[] = array("label" => "(漢字氏名×、かな氏名×、出身学校○) リスト", "value" => "nno");
            $arg["highschool"] = $jhflg;
        }
        $model->output = (!$model->output) ? $opt_sitei[0]["value"] : $model->output;

        $opt_label = array();   //ラジオ名称
        $opt_value = array();   //ラジオ値
        for ($i = 0; $i < count($opt_sitei); $i++) {
            $opt_label[] = $opt_sitei[$i]["label"];
            $opt_value[] = $opt_sitei[$i]["value"];
        }
        //ラジオ作成
        $this->createRadio($objForm, $arg, "OUTPUT", $model->output, "", $opt_value, count($opt_value));
        //ラジオ名称作成
        $this->setRadioName($arg, "NAME", $opt_label, count($opt_label));
    }

    //ラジオ名称作成
    function setRadioName(&$arg, $name, $value, $count)
    {
        for ($i = 1; $i <= $count; $i++) {
            $arg["data"][$name.$i] = $value[$i - 1];
        }
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
    function makeHidden(&$objForm, $model, $jhflg)
    {
        $objForm->ae($this->createHiddenAe("JHFLG", $jhflg));           //中高判定
        $objForm->ae($this->createHiddenAe("YEAR", $model->ObjYear));   //年度
        $objForm->ae($this->createHiddenAe("DBNAME", DB_DATABASE));     //DB名
        $objForm->ae($this->createHiddenAe("PRGID", "KNJL306K"));        //プログラムID
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
            $arg["data"][$name.$i] = $objForm->ge($name, $multi[$i -1]);
        }
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
