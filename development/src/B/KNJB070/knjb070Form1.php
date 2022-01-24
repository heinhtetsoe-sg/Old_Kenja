<?php

require_once('for_php7.php');


class knjb070Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb070Form1", "POST", "knjb070index.php", "", "knjb070Form1");

        $opt=array();
        //ラジオボタンを作成//時間割種別（基本時間割/通常時間割）
        $opt[0]=1;
        $opt[1]=2;
        for ($i = 1; $i <= 2; $i++) {
            $name = "RADIO".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "RADIO",
                                "value"      => isset($model->field["RADIO"])?$model->field["RADIO"]:"1",
                                "extrahtml"  => "onclick=\"jikanwari(this);\" id=\"$name\"",
                                "multiple"   => $opt));

            $arg["data"][$name] = $objForm->ge("RADIO",$i);
        }

        if ( $model->field["RADIO"] == 2 ) {     //通常時間割選択時
            $dis_jikan = "disabled";                //時間割選択コンボ使用不可
            $dis_date  = "";                        //指定日付テキスト使用可
            $arg["Dis_Date"]  = " dis_date(false); " ;
        } else {                                //基本時間割選択時
            $dis_jikan = "";                        //時間割選択コンボ使用可
            $dis_date  = "disabled";                //指定日付テキスト使用不可
            $arg["Dis_Date"]  = " dis_date(true); " ;
        }


        //時間割選択コンボボックスを作成
        $row2 = knjb070Query::getBscHdQuery($model);

        $objForm->ae( array("type"       => "select",
                            "name"       => "TITLE",
                            "size"       => "1",
                            "value"      => $model->field["TITLE"],
                            "options"    => isset($row2)?$row2:array(),
                            "extrahtml"  => "$dis_jikan "));
//                          "extrahtml"  => "$dis_jikan onchange=\"conbo_select();\""));

        $arg["data"]["TITLE"] = $objForm->ge("TITLE");

        //指定日付テキストボックスを作成
/*      $arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$model->control["学籍処理日"]);

        $objForm->ae( array("type"       => "text",
                            "name"       => "DATE2",
                            "size"       => "12",
                            "value"      => $model->control["学籍処理日"],
                            "extrahtml"  => "disabled"));

        $arg["data"]["DATE2"] = $objForm->ge("DATE2");
*/
        //指定日付テキストボックスを作成
        if ($model->field["RADIO"] == 2) {
            if (!isset($model->field["DATE1"]))
                $model->field["DATE1"] = $model->control["学籍処理日"];
            //指定日を含む指定週の開始日(月曜日)と終了日(日曜日)を取得
            common::DateConv2($model->field["DATE1"],$OutDate1,$OutDate2,1);
            $model->field["DATE2"] = $OutDate2;
        } else {
            $model->field["DATE1"] = "";
            $model->field["DATE2"] = "";
        }
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$model->field["DATE1"],"reload=true");

        $objForm->ae( array("type"       => "text",
                            "name"       => "DATE2",
                            "size"       => "12",
                            "value"      => $model->field["DATE2"],
                            "extrahtml"  => "disabled"));

        $arg["data"]["DATE2"] = $objForm->ge("DATE2");

        //チェックボックスを作成（帳票区分）
        //学年リストボックスを作成する//////////////////////////////////////////
        $db = Query::dbCheckOut();
        $opt_grade=array();
        $query = knjb070Query::getSelectGrade($model);
        $result = $db->query($query);
        $i=0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_grade[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
            $i++;
        }
        if ($model->field["NEN"]=="") $model->field["NEN"] = $opt_grade[0]["value"];
        $result->free();
        Query::dbCheckIn($db);
        $extra = "multiple";
        $arg["data"]["NEN"] = knjCreateCombo($objForm, "NEN", $model->field["NEN"], $opt_grade, $extra, $i);

        //所属選択コンボボックスを作成
        $db = Query::dbCheckOut();
        $opt = array();
        $query = knjb070Query::getSection();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        $extra = "";
        $arg["data"]["SECTION_CD_NAME1"] = knjCreateCombo($objForm, "SECTION_CD_NAME1", $model->field["SECTION_CD_NAME1"], $opt, $extra, 1);
        $extra = "";
        $value = isset($model->field["SECTION_CD_NAME2"]) ? $model->field["SECTION_CD_NAME2"] : $opt[get_count($opt)-1]["value"];
        $arg["data"]["SECTION_CD_NAME2"] = knjCreateCombo($objForm, "SECTION_CD_NAME2", $value, $opt, $extra, 1);

        //施設選択コンボボックスを作成
        $db = Query::dbCheckOut();
        $opt = array();
        $query = knjb070Query::getFacility();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        $extra = "";
        $arg["data"]["FACCD_NAME1"] = knjCreateCombo($objForm, "FACCD_NAME1", $model->field["FACCD_NAME1"], $opt, $extra, 1);
        $extra = "";
        $value = isset($model->field["FACCD_NAME2"]) ? $model->field["FACCD_NAME2"] : $opt[get_count($opt)-1]["value"];
        $arg["data"]["FACCD_NAME2"] = knjCreateCombo($objForm, "FACCD_NAME2", $value, $opt, $extra, 1);

        /**********/
        /* ラジオ */
        /**********/
        //帳票区分
        $opt = array(1, 2, 3); //1:学年別 2:職員別 3:施設別 
        $model->field["KUBUN"] = ($model->field["KUBUN"] == "") ? "1" : $model->field["KUBUN"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"KUBUN{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /*学年別*/
        //出力項目(上段)
        $opt = array(1, 2); //1:科目名 2:講座名
        $model->field["SUBCLASS_CHAIR_DIV"] = ($model->field["SUBCLASS_CHAIR_DIV"] == "") ? "1" : $model->field["SUBCLASS_CHAIR_DIV"];
        $extra = array("id=\"SUBCLASS_CHAIR_DIV1\"", "id=\"SUBCLASS_CHAIR_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_CHAIR_DIV", $model->field["SUBCLASS_CHAIR_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
        //出力項目(下段)
        $opt = array(1, 2); //1:職員名 2:施設名
        $model->field["STAFF_SISETU_DIV"] = ($model->field["STAFF_SISETU_DIV"] == "") ? "1" : $model->field["STAFF_SISETU_DIV"];
        $extra = array("id=\"STAFF_SISETU_DIV1\"", "id=\"STAFF_SISETU_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "STAFF_SISETU_DIV", $model->field["STAFF_SISETU_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        /*職員別*/
        //出力項目(上段)
        $opt = array(1, 2); //1:科目名 2:講座名
        $model->field["K2SUBCLASS_CHAIR_DIV"] = ($model->field["K2SUBCLASS_CHAIR_DIV"] == "") ? "1" : $model->field["K2SUBCLASS_CHAIR_DIV"];
        $extra = array("id=\"K2SUBCLASS_CHAIR_DIV1\"", "id=\"K2SUBCLASS_CHAIR_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "K2SUBCLASS_CHAIR_DIV", $model->field["K2SUBCLASS_CHAIR_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
        //出力項目(下段)
        $opt = array(1, 2); //1:施設名 2:受講クラス
        $model->field["SISETU_CLASS_DIV"] = ($model->field["SISETU_CLASS_DIV"] == "") ? "1" : $model->field["SISETU_CLASS_DIV"];
        $extra = array("id=\"SISETU_CLASS_DIV1\"", "id=\"SISETU_CLASS_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SISETU_CLASS_DIV", $model->field["SISETU_CLASS_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /*施設別*/
        //出力項目(上段)
        $opt = array(1, 2); //1:科目名 2:講座名
        $model->field["K3SUBCLASS_CHAIR_DIV"] = ($model->field["K3SUBCLASS_CHAIR_DIV"] == "") ? "1" : $model->field["K3SUBCLASS_CHAIR_DIV"];
        $extra = array("id=\"K3SUBCLASS_CHAIR_DIV1\"", "id=\"K3SUBCLASS_CHAIR_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "K3SUBCLASS_CHAIR_DIV", $model->field["K3SUBCLASS_CHAIR_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
        //出力項目(下段)
        $opt = array(1, 2); //1:教員名 2:受講クラス
        $model->field["STAFF_CLASS_DIV"] = ($model->field["STAFF_CLASS_DIV"] == "") ? "1" : $model->field["STAFF_CLASS_DIV"];
        $extra = array("id=\"STAFF_CLASS_DIV1\"", "id=\"STAFF_CLASS_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "STAFF_CLASS_DIV", $model->field["STAFF_CLASS_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /**********/
        /* ボタン */
        /**********/
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //年度
        $arg["data"]["YEAR"] = $model->control["年度"];

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJB070");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        //JavaScriptで参照するため
        knjCreateHidden($objForm, "GAKUSEKI", $model->control["学籍処理日"]);
        knjCreateHidden($objForm, "T_YEAR");
        knjCreateHidden($objForm, "T_BSCSEQ");
        knjCreateHidden($objForm, "T_SEMESTER");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb070Form1.html", $arg); 
    }
}
?>
