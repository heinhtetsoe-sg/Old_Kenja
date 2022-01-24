<?php

require_once('for_php7.php');

class knjd642Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjd642index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjd642Query::selectYearQuery());
        $opt       = array();
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) { 
            $opt[] = array("label" => CTRL_YEAR, "value" => CTRL_YEAR);
            unset($model->year);

        }else{
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["YEAR"],
                               "value" => $row["YEAR"]);

                if ($model->year == $row["YEAR"]){
                    $flg = true;
                }
            }
        }
        $result->free();
        //初期表示の年度設定
        if(!$flg) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR;
            } else if ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];

            } else if ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];

            } else {
                $model->year = $db->getOne(knjd642Query::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjd642index.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //年度コンボボックスを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "year",
                            "size"      => "1",
                            "extrahtml" => "onchange=\"return btn_submit('list');\"",
                            "value"     => $model->year,
                            "options"   => $opt));

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_year_add",
                            "value"     => "次年度作成",
                            "extrahtml" => "onclick=\"return btn_submit('copy');\"" ));

        $arg["year"] = array("VAL"      => $objForm->ge("year")."&nbsp;&nbsp;".
                                           $objForm->ge("btn_year_add"));

        //リスト表示
        $result = $db->query(knjd642Query::selectQuery($model->year));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
             array_walk($row, "htmlspecialchars_array");

             $hash = array("cmd"        => "edit",
                           "year"       => $row["YEAR"],
                           "BU_CD"      => $row["BU_CD"],
                           "KA_CD"      => $row["KA_CD"]);

             if($row["DIV"] == '1'){
                $row["DIV_NAME"]='文系' ;
             } else {
                $row["DIV_NAME"]='理系' ;
             }

             $row["BU_NAME"] = $row["BU_CD"] .":". $row["BU_NAME"] ;
             $row["KA_NAME"] = View::alink("knjd642index.php", $row["KA_CD"] .":". $row["KA_NAME"], "target=\"right_frame\"", $hash);
             $arg["data"][] = $row;
        }
        $result->free();

        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd"
                            ));

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjd642index.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd642Form1.html", $arg);
    }
}
?>
