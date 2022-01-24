<?php

require_once('for_php7.php');


class knjd102hForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd102hForm1", "POST", "knjd102hindex.php", "", "knjd102hForm1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //学期コンボの設定
        if (is_numeric($model->control["学期数"]))
        {
            for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ )
            {
                $opt[]= array("label" => $model->control["学期名"][$i+1],
                              "value" => sprintf("%d", $i+1)
                             );
            }
            $ga = isset($model->field["GAKKI"])?$model->field["GAKKI"]:$model->control["学期"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => isset($model->field["GAKKI"])?$model->field["GAKKI"]:$model->control["学期"],
                            "extrahtml"  => "onchange=\"return btn_submit('gakki'),AllClearList();\"",
                            "options"    => $opt ) );

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        //テストコンボ作成
        $db = Query::dbCheckOut();
        $opt_kind = array();
        $query = knjd102hQuery::get_testkind($model->control["年度"], $ga, $model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_kind[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
        }
        $result->free();

        if ($model->field["TESTKINDCD"] == "") $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->field["TESTKINDCD"],
                            "extrahtml"  => "",
                            "options"    => $opt_kind));

        $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

        //クラス選択コンボボックスを作成する
        $query = knjd102hQuery::getAuth($model, $model->control["年度"], $ga);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if(!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd102h'),AllClearList();\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd102h', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd102h', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //radio
        $opt = array(1, 2);
        $model->field["RANK_BASE"] = ($model->field["RANK_BASE"] == "") ? "1" : $model->field["RANK_BASE"];
        $extra = array("id=\"RANK_BASE1\"", "id=\"RANK_BASE2\"");
        $radioArray = knjCreateRadio($objForm, "RANK_BASE", $model->field["RANK_BASE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //radio
        $opt = array(1, 2);
        $model->field["RANK_DIV"] = ($model->field["RANK_DIV"] == "") ? "1" : $model->field["RANK_DIV"];
        $extra = array("id=\"RANK_DIV1\"", "id=\"RANK_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "RANK_DIV", $model->field["RANK_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //対象外の生徒取得
        $db = Query::dbCheckOut();
        $query = knjd102hQuery::getSchnoIdou($model, $ga);
        $result = $db->query($query);
        $opt_idou = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //対象者リストを作成する
        $db = Query::dbCheckOut();
        $query  = "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW ";
        $query .= "FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2 ";
        $query .= "WHERE T2.YEAR = '".$model->control["年度"]."' AND ";
        $query .= "      T2.SEMESTER = '".$ga."' AND ";
        $query .= "      T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "      T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "ORDER BY T2.ATTENDNO ";

        $result = $db->query($query);
        $opt_right = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
            if (in_array($row["SCHREGNO"], $model->select_data["selectdata"])) {
                $opt_left[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]);
            } else {
                $opt_right[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => $opt_right));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => $opt_left));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD102H"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SDATE",
                            "value"     => $model->control["学期開始日付"][$ga]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "EDATE",
                            "value"     => $model->control["学期終了日付"][$ga]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );  

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR
                            ) );

        //テスト種別
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "COUNTFLG",
                            "value"     => $model->testTable
                            ) );

        //教育課程コード
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "useCurriculumcd",
                            "value"     => $model->Properties["useCurriculumcd"]
                            ) );

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useJFreeChart", $model->Properties["useJFreeChart"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd102hForm1.html", $arg); 

    }
}
?>
