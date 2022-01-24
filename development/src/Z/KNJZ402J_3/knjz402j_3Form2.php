<?php

require_once('for_php7.php');

class knjz402j_3Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz402j_3index.php", "", "edit");
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //科目名取得
        $getSubclassName = $db->getOne(knjz402j_3Query::getSubclassName($model));
        $arg["TITLE"] = $getSubclassName;
        
        $counter = 0;
        $query = knjz402j_3Query::getRow($model);
        $result = $db->query($query);
        $model->data = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //VIEWCDを配列で取得
            $model->data["VIEWCD"][$counter] = $row["VIEWCD"];
        
            $setData["VIEWCD"] = $row["VIEWCD"].'：'.$row["VIEWNAME"];
            $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
            $setData["WEIGHT"] = knjCreateTextBox($objForm, $row["WEIGHT"], "WEIGHT"."_".$counter, 2, 3, $extra);
            
            $arg["data"][] = $setData;
            $counter++;
        }
        //取消
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //戻る
        $link = REQUESTROOT."/Z/KNJZ402J_2/knjz402j_2index.php?year_code=".$model->year_code;
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year_code",$model->year_code);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){ 
            $arg["reload"]  = "parent.left_frame.location.href='knjz402j_3index.php?cmd=list';";
        }
        View::toHTML($model, "knjz402j_3Form2.html", $arg); 
    }
}
?>
