<?php

require_once('for_php7.php');

class knja082aForm1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja082aForm1", "POST", "knja082aindex.php", "", "knja082aForm1");

        $opt=array();

        //年度
        $arg["data"]["YEAR"] = $model->nextyear;

        /*----------*/
        /* 帳票種類 */
        /*----------*/
        $output = array();
        $output[0] = 1;
        $output[1] = 2;

        for ($i = 1; $i <= 2; $i++) {
            $name = "OUTPUT".$i;
            $objForm->ae( array("type"      => "radio",
                                "name"      => "OUTPUT",
                                "value"     => 1,
                                "extrahtml" => "id=\"$name\" onClick=\"checkOutput(this)\"",
                                "options"   => $output));

            $arg["data"][$name] = $objForm->ge("OUTPUT",$i);
        }

        /*------------*/
        /* 学年コンボ */
        /*------------*/
        $db = Query::dbCheckOut();
        $grade = array();

        $result = $db->query(knja082aQuery::GetGrade($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $grade[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
        }
        $result->free();

        $grade[] = array('label' => "全学年",
                         'value' => "99");

        $objForm->ae( array("type"      => "select",
                            "name"      => "GRADE",
                            "size"      => "1",
                            "value"     => "$model->grade",
                            "extrahtml" => "onchange=\"return btn_submit('knja082a');\"",
                            "options"   => isset($grade)?$grade:array()));

        $arg["data"]["GRADE"] = $objForm->ge("GRADE");

        Query::dbCheckIn($db);

        //ふりがな出力チェックボックスを作成する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT3",
                            "value"     => "on",
                            "extrahtml" => "checked id=\"OUTPUT3\""));

        $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");

        //旧クラスを出力する
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT4",
                            "value"     => "on",
                            "extrahtml" => "id=\"OUTPUT4\" onClick=\"checkOutput2(this)\""));

        $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT4");
        
        
        $db = Query::dbCheckOut();
        $result = $db->getOne(knja082aQuery::GetHr($model));
        
        $check = "";
        $check2 = "";
        if((int)$result > 6){
            $check = "";
            $check2 = " checked";
        }else{
            $check = " checked";
            $check2 = "";
        }

        //frm指定 (1:6列 2:8列)
        $opt = array(1, 2);
        $extra = array("id=\"ROW1\"".$check, "id=\"ROW2\"".$check2);
        $radioArray = knjCreateRadio($objForm, "ROW", 1, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        Query::dbCheckIn($db);
        
        //中高判定フラグを作成する
        $db = Query::dbCheckOut();
        $row = $db->getOne(knja082aQuery::GetJorH());
        if ($row == 1){
            $jhflg = 1;
        }else {
            $jhflg = 2;
        }
        Query::dbCheckIn($db);
        $objForm->ae( array("type" => "hidden",
                            "name" => "JHFLG",
                            "value"=> $jhflg ) );

        //印刷ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_print",
                            "value"     => "プレビュー／印刷",
                            "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->nextyear);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA082A");
        knjCreateHidden($objForm, "STAFFCD", STAFFCD);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja082aForm1.html", $arg); 
    }
}
?>
