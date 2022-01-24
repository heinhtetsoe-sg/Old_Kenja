<?php

require_once('for_php7.php');


class knjh100Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成////////////////////////////////////////////////////////////////////////
        $arg["start"]   = $objForm->get_start("knjh100Form1", "POST", "knjh100index.php", "", "knjh100Form1");


        //年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

        $arg["data"]["YEAR"] = $model->control["年度"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => $model->control["年度"],
                            ) );


        //現在の学期コードをhiddenで送る//////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"      => $model->control["学期"],
                            ) );


        //クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
        $db = Query::dbCheckOut();
        $query = knjh100Query::getAuth($model, $model->control["年度"],$model->control["学期"]);
        /*
        $query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
                    "FROM SCHREG_REGD_HDAT ".
                    "WHERE YEAR='" .$model->control["年度"] ."'".
                    "AND SEMESTER='".$model->control["学期"] ."'";
        */
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:215px\" width=\"215px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:215px\" width=\"215px\" ondblclick=\"move1('right')\"",
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

        //名簿の選択
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //表彰実績区分チェックボックスを作成////////////////////////////////////////////////////////////////////
        if($model->field["HYOSHO"] == "on")
        {
            $check_hyosho = "checked";
        }
        else
        {
            $check_hyosho = "";
        }

        $objForm->ae( array("type" => "checkbox",
                            "name"        => "HYOSHO",
                            "value"     => "on",
                            "extrahtml" =>"onclick=\"HSUse('this');\" id=\"HYOSHO\" ".$check_hyosho ) );

        $arg["data"]["HYOSHO"] = $objForm->ge("HYOSHO");


        //表彰実績区分選択コンボボックスを作成する（FROM?TO）//////////////////////////////////////////////////////////////
        $db = Query::dbCheckOut();
        $query4 = "SELECT M.NAMECD2 AS VALUE,M.NAMECD2 || ':' || M.NAME1 AS LABEL,M.NAMECD1 AS CD1 ".
                    "FROM NAME_YDAT D INNER JOIN NAME_MST M ON D.NAMECD1=M.NAMECD1 AND D.NAMECD2=M.NAMECD2 ".
                    "WHERE (D.YEAR='" .$model->control["年度"] ."')".
                    "AND (M.NAMECD1='H303') ORDER BY M.NAMECD2";    // 2004/02/11 nakamoto ('H011'→'H303')

        $result4 = $db->query($query4);
        while($rowg = $result4->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row4[]= array('label' => $rowg["LABEL"],
                            'value' => $rowg["VALUE"]);
        }
        $result4->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "HS_KUBUN_FROM",
                            "size"       => "1",
                            "value"      => $model->field["HS_KUBUN_FROM"],
                            "options"    => isset($row4)?$row4:array(),
                            "extrahtml" =>"disabled" ) );

        $arg["data"]["HS_KUBUN_FROM"] = $objForm->ge("HS_KUBUN_FROM");


        $objForm->ae( array("type"       => "select",
                            "name"       => "HS_KUBUN_TO",
                            "size"       => "1",
                            "value"      => isset($model->field["HS_KUBUN_TO"])?$model->field["HS_KUBUN_TO"]:$row4[get_count($row4)-1]["value"],
                            "options"    => isset($row4)?$row4:array(),
                            "extrahtml" =>"disabled" ) );

        $arg["data"]["HS_KUBUN_TO"] = $objForm->ge("HS_KUBUN_TO");


        //罰則区分チェックボックスを作成////////////////////////////////////////////////////////////////////////////
        if($model->field["BATSU"] == "on")
        {
            $check_batsu = "checked";
        }
        else
        {
            $check_batsu = "";
        }

        $objForm->ae( array("type" => "checkbox",
                            "name"        => "BATSU",
                            "value"     => "on",
                            "extrahtml" =>"onclick=\"BSUse('this');\" id=\"BATSU\" ".$check_batsu ) );

        $arg["data"]["BATSU"] = $objForm->ge("BATSU");


        //罰則区分選択コンボボックスを作成する（FROM?TO）//////////////////////////////////////////////////////////////////
        $db = Query::dbCheckOut();
        $query2 = "SELECT M.NAMECD2 AS VALUE,M.NAMECD2 || ':' || M.NAME1 AS LABEL,M.NAMECD1 AS CD1 ".
                    "FROM NAME_YDAT D INNER JOIN NAME_MST M ON D.NAMECD1=M.NAMECD1 AND D.NAMECD2=M.NAMECD2 ".
                    "WHERE (D.YEAR='" .$model->control["年度"] ."')".
                    "AND (M.NAMECD1='H304') ORDER BY M.NAMECD2";    // 2004/02/11 nakamoto ('H012'→'H304')

        $result2 = $db->query($query2);
        while($rowf = $result2->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row2[]= array('label' => $rowf["LABEL"],
                            'value' => $rowf["VALUE"]);
        }
        $result2->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "BS_KUBUN_FROM",
                            "size"       => "1",
                            "value"      => $model->field["BS_KUBUN_FROM"],
                            "options"    => isset($row2)?$row2:array(),
                            "extrahtml" =>"disabled" ) );

        $arg["data"]["BS_KUBUN_FROM"] = $objForm->ge("BS_KUBUN_FROM");


        $objForm->ae( array("type"       => "select",
                            "name"       => "BS_KUBUN_TO",
                            "size"       => "1",
                            "value"      => isset($model->field["BS_KUBUN_TO"])?$model->field["BS_KUBUN_TO"]:$row2[get_count($row2)-1]["value"],
                            "options"    => isset($row2)?$row2:array(),
                            "extrahtml" =>"disabled" ) );

        $arg["data"]["BS_KUBUN_TO"] = $objForm->ge("BS_KUBUN_TO");


        //資格特技区分チェックボックスを作成////////////////////////////////////////////////////////////////////
        /*
        if($model->field["SIKAKU"] == "on")
        {
            $check_sikaku = "checked";
        }
        else
        {
            $check_sikaku = "";
        }

        $objForm->ae( array("type" => "checkbox",
                            "name"        => "SIKAKU",
                            "value"     => "on",
                            "extrahtml" =>"onclick=\"STUse('this');\"".$check_sikaku ) );

        $arg["data"]["SIKAKU"] = $objForm->ge("SIKAKU");
        */

        //資格特技区分選択コンボボックスを作成する（FROM?TO）///////////////////////////////////////////////////////////////
        /*
        $db = Query::dbCheckOut();
        $query3 = "SELECT M.NAMECD2 AS VALUE,M.NAMECD2 || ':' || M.NAME1 AS LABEL,M.NAMECD1 AS CD1 ".
                    "FROM NAME_YDAT D INNER JOIN NAME_MST M ON D.NAMECD1=M.NAMECD1 AND D.NAMECD2=M.NAMECD2 ".
                    "WHERE (D.YEAR='" .$model->control["年度"] ."')".
                    "AND (M.NAMECD1='H013') ORDER BY M.NAMECD2";

        $result3 = $db->query($query3);
        while($rowh = $result3->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row3[]= array('label' => $rowh["LABEL"],
                            'value' => $rowh["VALUE"]);
        }
        $result3->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "ST_KUBUN_FROM",
                            "size"       => "1",
                            "value"      => $model->field["ST_KUBUN_FROM"],
                            "options"    => isset($row3)?$row3:array(),
                            "extrahtml" =>"disabled" ) );

        $arg["data"]["ST_KUBUN_FROM"] = $objForm->ge("ST_KUBUN_FROM");


        $objForm->ae( array("type"       => "select",
                            "name"       => "ST_KUBUN_TO",
                            "size"       => "1",
                            "value"      => $model->field["ST_KUBUN_TO"],
                            "value"      => isset($model->field["ST_KUBUN_TO"])?$model->field["ST_KUBUN_TO"]:$row3[get_count($row3)-1]["value"],
                            "options"    => isset($row3)?$row3:array(),
                            "extrahtml" =>"disabled" ) );

        $arg["data"]["ST_KUBUN_TO"] = $objForm->ge("ST_KUBUN_TO");
        */


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
                            "value"     => "KNJH100"
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh100Form1.html", $arg); 
    }
}
?>
