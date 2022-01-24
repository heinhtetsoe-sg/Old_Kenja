<?php

require_once('for_php7.php');


class knjh302form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh302Form1", "POST", "knjh302index.php", "", "knjh302Form1");

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $query = knjh302Query::getSchregno_name($model->schregno);
        $Row                  = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"]     = $Row["NAME_SHOW"];
        
        //学籍賞罰データよりデータを取得
        if($model->schregno)
        {        
                $result = $db->query(knjh302Query::getAward($model->schregno));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                        $row["DETAIL_SDATE"]  = str_replace("-","/",$row["DETAIL_SDATE"]);
                        $row["DETAIL_EDATE"]  = str_replace("-","/",$row["DETAIL_EDATE"]);
                        $arg["data"][] = $row;
                }
        }
        Query::dbCheckIn($db);


        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "戻る",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));


        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh302Form1.html", $arg);
    }
}
?>
