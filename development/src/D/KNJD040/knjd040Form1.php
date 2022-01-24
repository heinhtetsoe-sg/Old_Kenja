<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：テスト項目の設定をDBからでなく、直接設定 山城 2004/10/26 */
/* ･NO002：テスト項目の設定を３学期は、期末のみ設定 山城 2004/11/29 */
/* ･NO003：出力設定ラジオボタンを追加               山城 2004/12/02 */
/********************************************************************/

class knjd040Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成////////////////////////////////////////////////////////////////////////
        $arg["start"]   = $objForm->get_start("knjd040Form1", "POST", "knjd040index.php", "", "knjd040Form1");


        //年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["YEAR"] = $model->control["年度"];


        //学期コンボの設定/////////////////////////////////////////////////////////////////////////////////////////////
        if (is_numeric($model->control["学期数"])) {
            for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ) {
                $opt[]= array("label" => $model->control["学期名"][$i+1],
                              "value" => sprintf("%d", $i+1)
                             );
            }
        /*
            //学年末を追加
                $opt[]= array("label" => $model->control["学期名"][9], 
                              "value" => sprintf("%d", 9)
                              );
        */
        }

        if (!isset($model->field["GAKKI"])) {
            $model->field["GAKKI"]=$model->control["学期"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
                            "extrahtml"  => "onchange=\"return btn_submit('gakki');\"",
                            "options"    => isset($opt)?$opt:array() ) );

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


        //テスト名コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
        /* NO001 ↓ */
        if ($model->field["GAKKI"] == 3) {    /* NO002 */
            $row2[]= array('label' => '0201　期末テスト',
                           'value' => '0201');
        } else {
            $row2[]= array('label' => '0101　中間テスト',
                           'value' => '0101');
            $row2[]= array('label' => '0201　期末テスト',
                           'value' => '0201');
        }

        /* NO001 ↑ */
        $objForm->ae( array("type"       => "select",
                            "name"       => "TEST",
                            "size"       => "1",
                            "value"      => $model->field["TEST"],
        //                  "extrahtml"  => ($model->field["OUTPUT"]=="1")?"disabled":"",
                            "options"    => isset($row2)?$row2:array()));

        $arg["data"]["TEST"] = $objForm->ge("TEST");


        //クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
        $db = Query::dbCheckOut();
        $row1 = array();
        $query = knjd040Query::getClassData($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["VALUE"]." ".$row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:170px\" width:\"170px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:170px\" width:\"170px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
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

        //NO003 帳票出力指定ラジオボタン/////////////////////////////////////////////////////////////////////////////////////
        $opt_sitei    = array();
        $opt_sitei[0] = 1;
        $opt_sitei[1] = 2;

        $objForm->ae( array("type"      => "radio",
                            "name"      => "OUTPUT",
                            "value"     => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:1,
                            "multiple"  => $opt_sitei));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD040"
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "useCurriculumcd",
                            "value"     => $model->Properties["useCurriculumcd"]
                            ) );

        //年度データ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->control["年度"]
                            ) );

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd040Form1.html", $arg); 
    }
}
?>
