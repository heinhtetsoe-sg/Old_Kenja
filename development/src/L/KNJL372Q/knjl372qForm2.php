<?php

require_once('for_php7.php');

class knjl372qForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl372qindex.php", "", "edit");

        $db = Query::dbCheckOut();
        
        //データ取得
        $getDataQuery = knjl372qQuery::getGroupCnt();
        $getResult = $db->query($getDataQuery);
        while($getRow = $getResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["GROUPNAME"] = $getRow["GROUPNAME"];
            $data["APPCNT"] = $getRow["APPCNT"];
            $data["TAKECNT"] = $getRow["TAKECNT"];
            $data["ABSCNT"] = $getRow["ABSCNT"];
            
            $arg["data"][] = $data;
        }
        
        Query::dbCheckIn($db);
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd
                            ) );
        
        //hidden作成
        makeHidden($objForm, $model);
        
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl372qForm2.html", $arg);
    }
} 
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //CSV出力
    $extra = " onclick=\"btn_submit('group_csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "type", "radar");
    knjCreateHidden($objForm, "maxTicksLimit", 6);
}
?>
