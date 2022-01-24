<?php

require_once('for_php7.php');

class knjh070form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh070index.php", "", "edit");

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $query = knjh070Query::getSchregno_name($model->schregno);
        $Row                  = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"]     = $Row["NAME_SHOW"];
        
        //学籍賞罰データよりデータを取得
        if($model->schregno) {
            list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
            $result = $db->query(knjh070Query::selectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["TRAINDATE"] = View::alink('knjh070index.php', str_replace("-","/",$row["TRAINDATE"]), "target=bottom_frame",
                                                array("cmd"        => 'edit',
                                                      "SCHREGNO"   => $row["SCHREGNO"],
                                                      "TRAINDATE"  => $row["TRAINDATE"])                                                               );
                $ume = "" ;
                for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - (int)$simo; $umecnt++) {
                    $ume .= $fuseji;
                }
                if ($fuseji) {
                    $SET_VALUE = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - (int)$simo), (int)$simo);
                } else {
                    $SET_VALUE = $row["STAFFCD"];
                }
                $row["STAFFNAME_SHOW"] = str_replace($row["STAFFCD"], $SET_VALUE, $row["STAFFNAME_SHOW"]);

                 $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){ 
            $arg["reload"]  = "window.open('knjh070index.php?cmd=edit&SCHREGNO=$model->schregno','bottom_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh070Form1.html", $arg);
    }
}
?>
