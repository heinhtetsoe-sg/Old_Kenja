<?php

require_once('for_php7.php');

class knjf030hForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjf030hForm1", "POST", "knjf030hindex.php", "", "knjf030hForm1");

        //リスト表示選択
        $opt = array(1, 2); //1:クラス選択 2:個人選択
        if (!$model->field["KUBUN"]) $model->field["KUBUN"] = 1;
        $onClick = " onclick =\" return btn_submit('knjf030h');\"";
        $extra = array("id=\"KUBUN1\"".$onClick, "id=\"KUBUN2\"".$onClick);
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["KUBUN"] == 1) $arg["clsno"] = $model->field["KUBUN"];
        if ($model->field["KUBUN"] == 2) $arg["schno"] = $model->field["KUBUN"];

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //現在の学期コードをhiddenで送る
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //クラス一覧リスト
        $db = Query::dbCheckOut();
        $query = knjf030hQuery::getZ010($model);
        $model->isSaga = "sagaken" == $db->getOne($query) ? "1" : "0";
        
        $row1 = array();
        $query = knjf030hQuery::getHrClassList($model);
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
            $query = knjf030hQuery::getSchno($model);//生徒一覧取得
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
                            "extrahtml"  => "multiple style=\"width:100%\"; height:180px;\" ondblclick=\"move1('left',$chdt)\"",
                            "size"       => "18",
                            "options"    => $row1));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:100%\"; height:180px;\" ondblclick=\"move1('right',$chdt)\"",
                            "size"       => "18",
                            "options"    => $opt_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right',$chdt);\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //作成日カレンダーを作成
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar2($objForm,"DATE",$value);

        //帳票種類
        for ($i = 1; $i <= 20; $i++) {
            $name = "CHECK".$i;
            $extra  = ($model->field[$name] == "on") ? "checked" : "";
            $extra .= " id=\"".$name."\"";
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "on", $extra, "");
        }

        //未検診(内科 眼科 耳鼻科)
        $opt = array(1, 2); //1:生徒毎にまとめて出力 2:検査項目毎に出力
        if (!$model->field["N19SORT"]) $model->field["N19SORT"] = 1;
        $extra = array("id=\"N19SORT1\"", "id=\"N19SORT2\"");
        $radioArray = knjCreateRadio($objForm, "N19SORT", $model->field["N19SORT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        for ($i = 1; $i <= 3; $i++) {
            $name = "N19CHECK".$i;
            $extra  = ($model->field[$name] == "on") ? "checked" : "";
            $extra .= " id=\"".$name."\"";
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "on", $extra, "");
        }

        //尿検査結果種類
        for ($i = 1; $i <= 3; $i++) {
            $name = "NYOCHECK".$i;
            $extra  = ($model->field[$name] == "on") ? "checked" : "";
            $extra .= " id=\"".$name."\"";
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "on", $extra, "");
        }

        //表示項目
        if ($model->isSaga != "1") {
            $arg["dispchkbox"] = 1;
        }
        for ($i = 1; $i <= 4; $i++) {
            $name = "HYOJI".$i;
            $extra = " id=\"".$name."\" checked";
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "on", $extra);
        }

        //button
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", PROGRAMID);
        knjCreateHidden($objForm, "cmd");
        //左のリストを保持
        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
        knjCreateHidden($objForm, "useSpecial_Support_School", $model->Properties["useSpecial_Support_School"]);// 特別支援学校
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
        knjCreateHidden($objForm, "useEar4000Hz", $model->Properties["useEar4000Hz"]);
        //校種　２）健康診断（歯・口腔）で使用
        $db = Query::dbCheckOut();
        $result = $db->query(knjf030hQuery::getNameMstA023());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            knjCreateHidden($objForm, "KNJF030H_notPrint_SubTitle_{$row["VALUE"]}", $model->Properties["KNJF030H_notPrint_SubTitle_{$row["VALUE"]}"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf030hForm1.html", $arg); 

    }

}
?>
