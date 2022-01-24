<?php

require_once('for_php7.php');

class knja132Form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja132Form1", "POST", "knja132index.php", "", "knja132Form1");

        //年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

        $arg["data"]["YEAR"] = $model->control["年度"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => $model->control["年度"],
                            ) );

        //学期テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

        $arg["data"]["GAKKI"] = $model->control["学期名"][$model->control["学期"]];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"      => $model->control["学期"],
                            ) );

        //ポップアップカレンダーを作成する/////////////////////////////////////////////////////////////////////////////////

        //出力順ラジオボタンを作成
        $opt[0]=1;
        $opt[1]=2;
        $disable = 0;
        if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;
        if ($model->field["OUTPUT"] == 1) $disable = 1;
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");

        for ($i = 1; $i <= get_count($opt); $i++) {
            $objForm->ae( array("type"       => "radio",
                                "name"       => "OUTPUT",
                                "value"      => isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1",
                                "extrahtml"	 => $extra[$i-1]." onclick =\" return btn_submit('clickchange');\"",
                                "multiple"   => $opt));

            $arg["data"]["OUTPUT".$i] = $objForm->ge("OUTPUT",$i);
        }

        if ($disable == 1){
            $arg["student"] = 1;
        }else {
            $arg["hr_class"] = 2;
        }
        //クラス選択コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
        $db = Query::dbCheckOut();
        if ($disable == 1){
            $query = common::getHrClassAuth(CTRL_YEAR,CTRL_SEMESTER,AUTHORITY,STAFFCD);
        }else {
            $query = knja132Query::getAuth2($model);
        }
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        //宮城県の場合チェックボックスをカット                
        $z010name1 = "";
        $query = knja132Query::getNameMst("Z010", "00");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $z010name1 = $row["NAME1"];
        }
        $result->free();
        if ($z010name1 == 'miyagiken') {
            $arg["not_miyagiken"] = "";
        } else {
            $arg["not_miyagiken"] = "1";
        }

        Query::dbCheckIn($db);

        if(!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        if($model->cmd == 'clickchange' ) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            $model->cmd = 'knja132';
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"	 => "onchange=\"return btn_submit('knja132'),AllClearList();\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
        $db = Query::dbCheckOut();
        if ($disable == 1){
            $query = "SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO,SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ".
                     "FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ".
                     "WHERE (((SCHREG_REGD_DAT.YEAR)='" .$model->control["年度"] ."') AND ".
                     "((SCHREG_REGD_DAT.SEMESTER)='" .$model->control["学期"] ."') AND ".
                     "((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."'))".
                     "ORDER BY ATTENDNO";
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt1[]= array('label' =>  $row["NAME"],
                               'value' => $row["SCHREGNO"]);
            }
        }else {
            $query = common::getHrClassAuth(CTRL_YEAR,CTRL_SEMESTER,AUTHORITY,STAFFCD);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if (substr($row["VALUE"],0,2) != $model->field["GRADE_HR_CLASS"]) continue;
                $opt1[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left',$disable)\"",
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\"",
                            "size"       => "20",
                            "options"    => array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left',$disable);\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //csvボタンを作成する/////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_csv",
                            "value"       => "ＣＳＶ出力",
                            "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

        $arg["button"]["btn_csv"] = $objForm->ge("btn_csv");


        //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //帳票種別チェックボックスを作成する////////////////////////////////////////////////////////////////////////////////

        /********************/
        /* チェックボックス */
        /********************/
        //生徒指導要録
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "seito",
                            "checked"    => true,
                            "extrahtml"  => "onclick=\"kubun();\" id=\"seito\"",
                            "value"      => isset($model->field["seito"]) ? $model->field["seito"] : "1"));
        $arg["data"]["SEITO"] = $objForm->ge("seito");

        //氏名
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "simei",
                            "checked"    => true,
                            "extrahtml"  => "onclick=\"kubun();\" id=\"simei\"",
                            "value"      => isset($model->field["simei"]) ? $model->field["simei"] : "1"));
        $arg["data"]["SIMEI"] = $objForm->ge("simei");

        //学習の記録（前期課程）
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "kanten",
                            "checked"    => true,
                            "extrahtml"  => "onclick=\"kubun();\" id=\"kanten\"",
                            "value"      => isset($model->field["kanten"]) ? $model->field["kanten"] : "1"));
        $arg["data"]["KANTEN"] = $objForm->ge("kanten");

        //修得単位の記録（後期課程）
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "tani",
                            "checked"    => true,
                            "extrahtml"  => "onclick=\"kubun();\" id=\"tani\"",
                            "value"      => isset($model->field["tani"]) ? $model->field["tani"] : "1"));
        $arg["data"]["TANI"] = $objForm->ge("tani");

        //学習の記録（後期課程）
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "gakushu",
                            "checked"    => true,
                            "extrahtml"  => "onclick=\"kubun();\" id=\"gakushu\"",
                            "value"      => isset($model->field["gakushu"]) ? $model->field["gakushu"] : "1"));
        $arg["data"]["GAKUSHU"] = $objForm->ge("gakushu");

        //総合的な学習の時間の記録
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "sogo",
                            "checked"    => true,
                            "extrahtml"  => "onclick=\"kubun();\" id=\"sogo\"",
                            "value"      => isset($model->field["sogo"]) ? $model->field["sogo"] : "1"));
        $arg["data"]["SOGO"] = $objForm->ge("sogo");

        //特別活動の記録
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "katsudo",
                            "checked"    => true,
                            "extrahtml"  => "onclick=\"kubun();\" id=\"katsudo\"",
                            "value"      => isset($model->field["katsudo"]) ? $model->field["katsudo"] : "1"));
        $arg["data"]["KATSUDO"] = $objForm->ge("katsudo");

        //検査・検定の名称・結果等
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "kentei",
                            "checked"    => true,
                            "extrahtml"  => "onclick=\"kubun();\" id=\"kentei\"",
                            "value"      => isset($model->field["kentei"]) ? $model->field["kentei"] : "1"));
        $arg["data"]["KENTEI"] = $objForm->ge("kentei");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA132");
        knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DATE", CTRL_DATE);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja132Form1.html", $arg); 
    }
}
?>
