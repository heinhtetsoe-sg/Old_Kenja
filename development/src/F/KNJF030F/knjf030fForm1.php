<?php

require_once('for_php7.php');


class knjf030fForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjf030fForm1", "POST", "knjf030findex.php", "", "knjf030fForm1");

        //リスト表示選択
        $opt = array(1, 2); //1:クラス選択 2:個人選択
        if (!$model->field["KUBUN"]) $model->field["KUBUN"] = 1;
        $onClick = " onclick =\" return btn_submit('knjf030f');\"";
        $extra = array("id=\"KUBUN1\"".$onClick, "id=\"KUBUN2\"".$onClick);
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["KUBUN"] == 1) $arg["clsno"] = $model->field["KUBUN"];
        if ($model->field["KUBUN"] == 2) $arg["schno"] = $model->field["KUBUN"];

        //学校名
        $db = Query::dbCheckOut();
        $query = knjf030fQuery::getZ010();
        $result = $db->query($query);
        while($rowf = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $model->schoolName = $rowf["NAME1"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //現在の学期コードをhiddenで送る
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //クラス一覧リスト
        $db = Query::dbCheckOut();
        $row1 = array();
        $query = knjf030fQuery::getHrClassList($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        //2:個人表示指定用
        $opt_left = array();
        if ($model->field["KUBUN"] == 2) {
            if ($model->field["GRADE_HR_CLASS"]=="") $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

            $objForm->ae( array("type"       => "select",
                                "name"       => "GRADE_HR_CLASS",
                                "size"       => "1",
                                "value"      => $model->field["GRADE_HR_CLASS"],
                                "extrahtml"  => "onChange=\"return btn_submit('change_class');\"",
                                "options"    => $row1));

            $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

            $row1 = array();
            //生徒単位
            $selectleft = explode(",", $model->selectleft);
            $query = knjf030fQuery::getSchno($model);//生徒一覧取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"], 
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

                if($model->cmd == 'change_class' ) {
                    if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                        $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                        'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                    }
                } else {
                    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            }
            //左リストで選択されたものを再セット
            if($model->cmd == 'change_class' ) {
                foreach ($model->select_opt as $key => $val){
                    if (in_array($key, $selectleft)) {
                        $opt_left[] = $val;
                    }
                }
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        $chdt = $model->field["KUBUN"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:300px; height:300px;\" ondblclick=\"move1('left',$chdt)\"",
                            "size"       => "18",
                            "options"    => $row1));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:300px; height:300px;\" ondblclick=\"move1('right',$chdt)\"",
                            "size"       => "18",
                            "options"    => $opt_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right',$chdt);\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //出力順ラジオボタンを作成
        $opt = array(1, 2);     // 1:結果印刷 2:フォーム印刷
        $model->field["OUTPUTA"] = ($model->field["OUTPUTA"] == "") ? "1" : $model->field["OUTPUTA"];
        $disable = "";
        $extra = array("id=\"OUTPUTA1\"".$disable, "id=\"OUTPUTA2\"".$disable);
        $radioArray = knjCreateRadio($objForm, "OUTPUTA", $model->field["OUTPUTA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
        knjCreateHidden($objForm, "useKnjf030AHeartBiko", $model->Properties["useKnjf030AHeartBiko"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "useParasite_J", $model->Properties["useParasite_J"]);
        knjCreateHidden($objForm, "useParasite_H", $model->Properties["useParasite_H"]);
        knjCreateHidden($objForm, "useParasite_P", $model->Properties["useParasite_P"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "useForm5_H_Ha", $model->Properties["useForm5_H_Ha"]);
        knjCreateHidden($objForm, "useForm5_H_Ippan", $model->Properties["useForm5_H_Ippan"]);
        knjCreateHidden($objForm, "kenkouSindanIppanNotPrintNameMstComboNamespare2Is1", $model->Properties["kenkouSindanIppanNotPrintNameMstComboNamespare2Is1"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf030fForm1.html", $arg); 

    }

}
?>
