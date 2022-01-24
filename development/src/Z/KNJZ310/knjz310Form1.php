<?php

require_once('for_php7.php');

class knjz310Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz310index.php", "", "edit");

        $db = Query::dbCheckOut();

        //コンボボックス内データ取得 NO001
        $query = knjz310Query::selectYearQuery($model);
        $result = $db->query($query);
        $cmb_opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cmb_opt[] = array("label" => $row["YEAR"], "value" => $row["YEAR"]);
        }

        $result->free();
        //年度コンボボックスを作成する NO001
        $objForm->ae( array("type"      => "select",
                            "name"      => "year",
                            "size"      => "1",
                            "value"     => $model->year,
                            "extrahtml" => "onChange=\"return btn_submit('list');\"",
                            "options"   => $cmb_opt));

        $arg["year"] = array( "VAL" => $objForm->ge("year"));

        $query = knjz310Query::getUsergroupMst($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
             array_walk($row, "htmlspecialchars_array");
//------uchima(10/1)
             $row["link"] = View::alink(REQUESTROOT."/Z/KNJZ310_2/knjz310_2index.php", "設定" , "target=\"_parent\" ",
                                        array("GROUPCD"     => $row["GROUPCD"],
                                              "GROUPNAME"   => $row["GROUPNAME"],
                                              "year"        => $model->year)); //NO001
//-----------------             
             $arg["data"][] = $row; 
        }
        //$arg["link"] = REQUESTROOT."/Z/KNJZ310_2/knjz310_2index.php";
        $result->free();
        Query::dbCheckIn($db);
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        knjCreateHidden($objForm, "SEND_selectSchoolKind", $model->selectSchoolKind);

        $link1 = REQUESTROOT."/X/KNJXUSRGRPLST/knjxusrgrplstindex.php";

        //職員別所属グループ確認ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "auth_check",
                            "value"     => "グループ別所属職員確認",
                            "extrahtml" => "style=\"width:200px\"onclick=\" Page_jumper('".$link1."','2');\""));

        $arg["auth_check"] = $objForm->ge("auth_check");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz310Form1.html", $arg);
    }
} 
?>
