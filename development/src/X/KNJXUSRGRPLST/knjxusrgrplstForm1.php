<?php
class knjxusrgrplstForm1
{
    function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjxusrgrplstindex.php", "", "main");
        $db             = Query::dbCheckOut();

            //ソート
            $mark = array("(▼)","(▲)");

            switch ($model->s_id) {
                    case "1":
                            $mark1 = $mark[$model->sort[$model->s_id]];break;
                    case "2":
                            $mark2 = $mark[$model->sort[$model->s_id]];break;
                    case "3":
                            $mark3 = $mark[$model->sort[$model->s_id]];break;
                    case "4":
                            $mark4 = $mark[$model->sort[$model->s_id]];break;
            }

            $arg["sort1"] = View::alink("knjxusrgrplstindex.php", "職員コード".$mark1."＋グループコード(△)", "target=_self tabindex=\"-1\"",
                            array("cmd"         => "main",
                                  "sort1"       => ($model->sort["1"] == "1")?"0":"1",
                                  "s_id"        => "1" ,
                      "NO"        =>$model->no) );

            $arg["sort2"] = View::alink("knjxusrgrplstindex.php", "職員氏名かな".$mark2."＋グループコード(△)", "target=_self tabindex=\"-1\"",
                            array("cmd"         => "main",
                                  "sort2"       => ($model->sort["2"] == "1")?"0":"1",
                                  "s_id"        => "2" ,
                      "NO"        =>$model->no) );

            $arg["sort3"] = View::alink("knjxusrgrplstindex.php", "グループコード".$mark3."＋職員コード(△)", "target=_self tabindex=\"-1\"",
                            array("cmd"         => "main",
                                  "sort3"       => ($model->sort["3"] == "1")?"0":"1",
                                  "s_id"        => "3" ,
                      "NO"        =>$model->no) );

            $arg["sort4"] = View::alink("knjxusrgrplstindex.php", "グループコード".$mark4."＋職員氏名かな(△)", "target=_self tabindex=\"-1\"",
                            array("cmd"         => "main",
                                  "sort4"       => ($model->sort["4"] == "1")?"0":"1",
                                  "s_id"        => "4" ,
                      "NO"        =>$model->no) );

        $result    = $db->query(knjxusrgrplstQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {            
            $h = $h+1;
            $arg["data"][] = $row; 
        }
            $arg["h"]=($h>20)? "450" : "*";;

        Query::dbCheckIn($db);

            if($model->no == 1){
                    $link1=REQUESTROOT."/Z/KNJZ300/knjz300index.php";
            }else{
                    $link1=REQUESTROOT."/Z/KNJZ310/knjz310index.php";
            }

                //戻るボタンを作成する
                $objForm->ae( array("type"      => "button",
                                    "name"      => "back",
                                    "value"     => "戻る",
                                    "extrahtml" => "onclick=\" Page_jumper('".$link1."');\""));

            $arg["back"] = $objForm->ge("back");

            //hiddenを作成する
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "cmd") );

        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjxusrgrplstForm1.html", $arg);
     unset($this->cmd);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
