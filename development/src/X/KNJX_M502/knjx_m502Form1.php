<?php

require_once('for_php7.php');

class knjx_m502Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";
        //DB接続
        $db = Query::dbCheckOut();

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $arg["data"]["SHORI_MEI"] = $this->createCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $arg["data"]["HEADER"] = $this->createCheckBox($objForm, "HEADER", "on", $check_header, "");

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt_shubetsu = array(1, 2, 3, 4);
        if ($model->field["OUTPUT"]=="") {
            $model->field["OUTPUT"] = "1";
        }
        $this->createRadio($objForm, $arg, "OUTPUT", $model->field["OUTPUT"], "", $opt_shubetsu, get_count($opt_shubetsu));

        //ファイルからの取り込み
        $arg["FILE"] = $this->createFile($objForm, "FILE", 1024000);

        //年度一覧コンボボックス
        $result     = $db->query(knjx_m502query::getSelectFieldSQL());
        $opt_year   = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"]."年度 ".$row["SEMESTERNAME"],
                                "value" => $row["YEAR"].$row["SEMESTER"]);
        }
        $result->free();
        if ($model->field["YEAR"]=="") {
            $model->field["YEAR"] = CTRL_YEAR.CTRL_SEMESTER;
        }
        $arg["data"]["YEAR"] = $this->createCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, "onchange=\"btn_submit('');\"", 1);

        //年組一覧コンボボックス
        $result      = $db->query(knjx_m502query::getSelectFieldSQL2($model));
        $opt_gr_hr   = array();
        $opt_gr_hr[] = array("label" => "(全て出力)","value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_gr_hr[] = array("label" => $row["HR_NAME"],
                                 "value" => $row["GRADE"].$row["HR_CLASS"]);
        }
        $result->free();
        $arg["data"]["GRADE_HR_CLASS"] = $this->createCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $opt_gr_hr, "", 1);

        //学期コンボ
        $opt = array();
        $query = knjx_m502query::getSemester();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //ボタン作成
        $this->makeButton($objForm, $arg, $model);

        //hiddenを作成する
        $objForm->ae($this->createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx_m502index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_m502Form1.html", $arg);
    }

    //ボタン作成
    public function makeButton(&$objForm, &$arg, $model)
    {
        //実行ボタン
        $arg["btn_exec"] = $this->createBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
        //終了ボタン
        $arg["btn_end"]  = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
    }

    //コンボ作成
    public function createCombo(&$objForm, $name, $value, $options, $extra, $size)
    {
        $objForm->ae(array("type"      => "select",
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }

    //チェックボックス作成
    public function createCheckBox(&$objForm, $name, $value, $extra, $multi)
    {
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));

        return $objForm->ge($name);
    }

    //ラジオ作成
    public function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
    {
        $objForm->ae(array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));
        for ($i = 1; $i <= $count; $i++) {
            $arg["data"][$name.$i] = $objForm->ge($name, $i);
        }
    }

    //File作成
    public function createFile(&$objForm, $name, $size, $extra = "")
    {
        $objForm->add_element(array("type"      => "file",
                                    "name"      => $name,
                                    "size"      => $size,
                                    "extrahtml" => $extra ));

        return $objForm->ge($name);
    }

    //ボタン作成
    public function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae(array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ));
        return $objForm->ge($name);
    }

    //Hidden作成ae
    public function createHiddenAe($name, $value = "")
    {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
    }
}
