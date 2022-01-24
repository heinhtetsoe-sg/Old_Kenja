<?php

require_once('for_php7.php');

class knjd302tForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd302tForm1", "POST", "knjd302tindex.php", "", "knjd302tForm1");

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期リスト
        $query = knjd302tQuery::getSemester($model);
        $result = $db->query($query);
        $opt_seme = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_seme[]= array('label' 	=> $row["SEMESTERNAME"],
                                'value' => $row["SEMESTER"]);
        }
        $result->free();

        if ($model->field["GAKKI"]=="") $model->field["GAKKI"] = CTRL_SEMESTER;

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
                            "extrahtml"  => "onChange=\"return btn_submit('knjd302t');\"",
                            "options"    => $opt_seme));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


        //テスト種別リスト
        /* NO003 ↓*/
        if($model->field["GAKKI"] == 9 )
        {
            $opt_kind[] = array("label" => "","value" => "0");
        /* NO003 ↑*/
        /* NO001 ↓ */
        }
        else if($model->field["GAKKI"] != 3 )
        {
            $opt_kind[] = array("label" => "","value" => "0");
            $opt_kind[]= array('label' => '01　中間テスト',
                               'value' => '01');
            $opt_kind[]= array('label' => '02　期末テスト',
                               'value' => '02');
        }
        else
        {
            $opt_kind[]= array('label' => '02　期末テスト',
                               'value' => '02');
        }
        /* NO001 ↑ */

        //if ($model->field["TESTKINDCD"]=="") $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->field["TESTKINDCD"],
                            "extrahtml"  => "STYLE=\"WIDTH:130\" ",		/* NO003 */
                            "options"    => $opt_kind));

        $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");


        //学年コンボボックスを作成する/* NO004 */
        $opt_schooldiv = "学年";

        $opt_grade=array();
        $query = knjd302tQuery::getSelectGrade($model);
        $result = $db->query($query);
        $grade_flg = true;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $grade_show= sprintf("%d",$row["GRADE"]);
            $opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
                                 'value' => $row["GRADE"]);
            if( $model->field["GRADE"]==$row["GRADE"] ) $grade_flg = false;
        }
        if( $grade_flg ) $model->field["GRADE"] = $opt_grade[0]["value"];
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE",
                            "size"       => "1",
                            "value"      => $model->field["GRADE"],
                            "extrahtml"  => "onChange=\"return btn_submit('knjd302t');\"",
                            "options"    => $opt_grade));

        $arg["data"]["GRADE"] = $objForm->ge("GRADE");

		//対象リストの設定
        $this->makeStudentCmb($objForm, $arg, $model, $inSentence);

        //異動日付作成
        if ($model->field["DATE"] == "") {
            $model->field["DATE"] = str_replace("-","/",CTRL_DATE);
        }
        $arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);

        //印刷ボタンを作成する
		$arg["button"]["btn_print"] = $this->createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");

        //終了ボタンを作成する
		$arg["button"]["btn_end"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

		//hiddenを作成
        $this->makeHidden($objForm, $arg);

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd302tForm1.html", $arg); 
    }

    //対象リスト作成
    function makeStudentCmb(&$objForm, &$arg, &$model, $inSentence)
    {
        //クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
      //---2005.06.02---↓---
        $semester = ($model->field["GAKKI"] == "9") ? CTRL_SEMESTER : $model->field["GAKKI"];
        $query = common::getHrClassAuth(CTRL_YEAR,$semester,AUTHORITY,STAFFCD);
        //$query = knjd302tQuery::getAuth($model);
        //---2005.06.01---↑---
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (substr($row["VALUE"],0,2) != $model->field["GRADE"]) continue;//---2005.06.02
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => array()));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");
    }

    //Hidden設定
    function makeHidden(&$objForm, &$arg)
    {
		$objForm->ae($this->createHiddenAe("YEAR", CTRL_YEAR));
		$objForm->ae($this->createHiddenAe("cmd"));
		$objForm->ae($this->createHiddenAe("DBNAME", DB_DATABASE));
		$objForm->ae($this->createHiddenAe("PRGID", "KNJD302T"));
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
