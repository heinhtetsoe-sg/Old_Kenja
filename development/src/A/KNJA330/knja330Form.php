<?php

require_once('for_php7.php');

class knja330form
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knja330index.php", "", "right_list");

        $db = Query::dbCheckOut();

        $arg["YEAR"]        = $model->year;
        $arg["YEAR_ADD"]    = $model->year_add;
        $arg["SEMENAME"]    = $model->control["学期名"][CTRL_SEMESTER];
        $arg["SEMENAME2"]   = $model->control["学期名"][1];
        //画面切り替えラジオ作成
        $this->makeOutput($objForm, $arg, $model);

        //コピー用画面作成
        $this->makeData($objForm, $arg, $db, $model, "ADD", $model->year, CTRL_SEMESTER);

        //削除用画面作成
        $this->makeData($objForm, $arg, $db, $model, "DEL", $model->year, "1");

        //実行ボタンを作成する
        $arg["btn_execute"] = $this->createBtn($objForm, "btn_execute", "実 行", "onclick=\"return btn_submit('execute');\"");

        //削除ボタンを作成する
        $arg["btn_del"] = $this->createBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");

        //終了ボタンを作成する
        $arg["btn_end"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hiddenを作成する
        $objForm->ae($this->createHiddenAe("cmd"));

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja330Form.html", $arg);
    }

    //画面切り替えラジオ作成
    function makeOutput(&$objForm, &$arg, &$model)
    {
        $opt[0]=1;
        $opt[1]=2;
        if (!$model->output) {
            $model->output = $model->checkdiv;
        }
        $this->createRadio($objForm, $arg, "OUTPUT", $model->output, "onclick=\"return btn_submit('edit');\"", $opt, get_count($opt));
        if ($model->output == "1") {
            $arg["ADDVIEW"] = "on";
        } else {
            $arg["DELVIEW"] = "on";
        }
    }

    //画面作成
    function makeData(&$objForm, &$arg, $db, $model, $makediv, $year, $semester)
    {
        $setval = array();  //出力データ配列
        $getData = array();
        $getData = $this->setData($objForm, $db, $model, $makediv, $year, $semester);
        for ($i = 0; $i < get_count($getData); $i++) {
            $setval[$makediv."_GRADENAME"] = $getData[$i][$makediv."_GRADENAME"];
            $setval[$makediv."_REMARK"]    = $getData[$i][$makediv."_REMARK"];
            $setval[$makediv."_CHECKED"]   = $getData[$i][$makediv."_CHECKED"];
            $arg[$makediv."data"][] = $setval;
        }
    }

    //表示用データ配列作成
    function setData(&$objForm, $db, $model, $makediv, $year, $semester)
    {
        $setD = array();
        $setcnt = 0;
        $maxGrade = $db->getOne(knja330Query::getMaxGrade($model, $year, $semester, str_replace("/","-",$model->control["学期終了日付"][9]), $model->checkdiv));
        $query = knja330Query::getGrade($model, $year, $semester, str_replace("/","-",$model->control["学期終了日付"][9]), $model->checkdiv);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $remark = $db->getOne(knja330Query::getRemark($model->year, $model->year_add, $row["GRADE"], $makediv, str_replace("/","-",$model->control["学期終了日付"][9]), $model->checkdiv));
            $disabled = "";
            $checked  = "checked";
            if ($remark != "") {
                $disabled = "disabled";
            }
            if ($makediv == "ADD" && $maxGrade == $row["GRADE"]) {
                $checked = "";
            }
            $setD[$setcnt] = array($makediv."_GRADENAME" => $row["GRADE_NAME1"],
                                   $makediv."_REMARK"    => $remark,
                                   $makediv."_CHECKED" => $this->createCheckBox($objForm, $makediv."_CHECKED", $row["GRADE"], $disabled." ".$checked, "1"));
            $setcnt++;
        }
        $result->free();
        return $setD;
    }

    //ラジオ作成
    function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
    {
        for ($i = 1; $i <= $count; $i++) {
            $id_name = $name.$i;
            $objForm->ae( array("type"      => "radio",
                                "name"      => $name,
                                "value"     => $value,
                                "extrahtml" => $extra." id=\"$id_name\"",
                                "multiple"  => $multi));
            $arg["data"][$name.$i] = $objForm->ge($name, $i);
        }
    }

    //チェックボックス作成
    function createCheckBox(&$objForm, $name, $value, $extra, $multi) {

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
