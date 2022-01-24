<?php

require_once('for_php7.php');

/********************************************************************/
/* 自転車登録                                       山城 2005/11/01 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 : テーブル変更AWARD→DITAIL                山城 2006/01/27 */
/* NO002 : 削除後の日付をブランクにする。           山城 2006/01/30 */
/********************************************************************/
class knjh155form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh155index.php", "", "edit");

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $query = knjh155Query::getSchregno_name($model->schregno);
        $Row   = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"]     = $Row["NAME_SHOW"];
        
        //学籍賞罰データよりデータを取得
        if($model->schregno)
        {       
                $result = $db->query(knjh155Query::getAward($model->schregno));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $row["DETAIL_SDATE"]    = str_replace("-","/",$row["DETAIL_SDATE"]);
                    $row["DETAIL_EDATE"]    = str_replace("-","/",$row["DETAIL_EDATE"]);
                    $arg["data"][] = $row;
                }
        }
        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type" => "hidden",
                            "name" => "cmd"));
        //hiddenを作成する
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "clear",
                            "value" => "0"));

        $arg["finish"]  = $objForm->get_finish();

        if (!$model->prg){
            $arg["FRAMENAME"] = "edit_frame";
        }else {
            $arg["FRAMENAME"] = "bottom_frame";
        }
        if (VARS::get("cmd") == "right_list"){ 
            $arg["reload"]  = "window.open('knjh155index.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh155Form1.html", $arg);
    }
}
?>
