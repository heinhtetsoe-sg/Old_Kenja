<?php

require_once('for_php7.php');

/********************************************************************/
/* 成績通知表                                       山城 2005/01/10 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：個人選択/クラス選択をラジオで制御        山城 2005/05/20 */
/********************************************************************/

class knjd171Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成/////////////////////////////////////////////////////////////////////////////////////////////
        $arg["start"]   = $objForm->get_start("knjd171Form1", "POST", "knjd171index.php", "", "knjd171Form1");


        //年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////


        $arg["data"]["YEAR"] = $model->control["年度"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => $model->control["年度"],
                            ) );


        //学期コンボの設定/////////////////////////////////////////////////////////////////////////////////////////////
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

        if ($ga > 2) {
            $opt_kind[]= array('label' => '学期末成績',
                               'value' => '99');
        } else {
            $opt_kind[]= array('label' => '中間成績',
                               'value' => '01');
            $opt_kind[]= array('label' => '学期末成績',
                               'value' => '99');
        }

        if ($model->field["TESTKINDCD"] == "") $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->field["TESTKINDCD"],
                            "extrahtml"  => "",
                            //"extrahtml"  => "STYLE=\"WIDTH:120\" ",
                            "options"    => $opt_kind));

        $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

        //出力順ラジオボタン 1:個人指定 2:クラス指定
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onclick =\" return btn_submit('clickcheng');\"", "id=\"OUTPUT2\" onclick =\" return btn_submit('clickcheng');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        $disable = ($model->field["OUTPUT"] == 1) ? 1 : 0;

        if ($disable == 1){
            $arg["kojin"] = 1;
        }else {
            $arg["kurasu"] = 2;
        }

        //クラス選択コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
        /* NO001 ↓ */
        $db = Query::dbCheckOut();
        if ($disable == 1){
            //---2005.06.01---↓---
            $query = common::getHrClassAuth(CTRL_YEAR,$ga,AUTHORITY,STAFFCD);
            //$query = knjd171Query::getAuth($model->control["年度"],$ga);
            //---2005.06.01---↑---
        }else {
            $query = knjd171Query::getAuth2($model->control["年度"],$ga);
        }
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

        if($model->cmd == 'clickcheng' ) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            $model->cmd = 'knjd171';
        }
        /* NO001 ↑ */

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd171'),AllClearList();\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");


        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        if ($disable == 1) {
            //異動対象日付（テキスト）
            $disabled = "";
            $extra = "onblur=\"isDate(this); tmp_list('knjd171', 'on')\"".$disabled;
            $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
            //異動対象日付（カレンダー）
            global $sess;
            $extra = "onclick=\"tmp_list('knjd171', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
            $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
            //異動対象日付
            $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;
        } else {
            $arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);
        }

        //作成日付 NO001
        if ($model->field["DATE2"] == "") $model->field["DATE2"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm    ,"DATE2"    ,$model->field["DATE2"]);

        /* NO001 ↓ */
            $db = Query::dbCheckOut();
            $opt_right = $opt_left = array();
            if ($disable == 1){
                //対象外の生徒取得
                $query = knjd171Query::getSchnoIdou($model,$ga);
                $result = $db->query($query);
                $opt_idou = array();
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $opt_idou[] = $row["SCHREGNO"];
                }
                $result->free();
                Query::dbCheckIn($db);

                //対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
                $db = Query::dbCheckOut();
                $query = knjd171Query::gettaisyo($model,$ga);

                $result = $db->query($query);
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
            } else {
                //---2005.06.01---↓---
                $query = common::getHrClassAuth(CTRL_YEAR,$ga,AUTHORITY,STAFFCD);
                //$query = knjd171Query::gettaisyo2($model,$ga);
                //---2005.06.01---↑---
                $result = $db->query($query);
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    if (substr($row["VALUE"],0,2) != $model->field["GRADE_HR_CLASS"]) continue;//---2005.06.01
                    $opt_right[]= array('label' => $row["LABEL"],
                                        'value' => $row["VALUE"]);
                }
            }
            $result->free();
            Query::dbCheckIn($db);

            $objForm->ae( array("type"       => "select",
                                "name"       => "category_name",
                                "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left',$disable)\"",
                                "size"       => "20",
                                "options"    => $opt_right));

            $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");
        /* NO001 ↑ */
        //生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\"",
                            "size"       => "20",
                            "options"    => $opt_left));

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
        $objForm->ae( array("type"         => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
        //                    "value"       => "直接印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //csvボタンを作成する/////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_csv",
                            "value"       => "ＣＳＶ出力",
                            "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

        $arg["button"]["btn_csv"] = $objForm->ge("btn_csv");
        //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        // 2004/02/12 nakamoto 証明日付カット
        //$arg["el"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
        //add  04/09/08 yamauchi
        /*
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRINTNAME",
                            "value"      => DIRECT_PRINTER
                            ) );
        */
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD171"
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );  

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd171Form1.html", $arg); 
    }
}
?>
