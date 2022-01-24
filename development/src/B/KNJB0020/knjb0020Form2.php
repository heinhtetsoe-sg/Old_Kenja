<?php

require_once('for_php7.php');

class knjb0020Form2
{
    function main(&$model) {
        $arg["jscript"] = "";
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjb0020index.php", "", "edit");

        $db     = Query::dbCheckOut();

        $arg["YEAR"] = CTRL_YEAR."年度";

        //所属コンボ
        $section = array();
        $result = $db->query(knjb0020Query::GetSection());

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $section[] = array("label" => $row["SECTIONCD"]."：".$row["SECTIONNAME"],
                               "value" => $row["SECTIONCD"]);
        }

        $section[] = array("label" => "すべて表示",
                           "value" => "all");

        if ($result->numRows() == 0) {
            $arg["jscript"] = "OnPreError('所属マスタ');";
        }

        if ($model->sectioncd == "") $model->sectioncd = $section[0]["value"];

        $objForm->ae( array("type"      => "select",
                            "name"      => "sectioncd",
                            "value"     => $model->sectioncd,
                            "options"   => $section,
                            "extrahtml" => "onchange=\"btn_submit('combo');\""));
                                            
        $arg["section"] = $objForm->ge("sectioncd");

        $objForm->ae( array("type"        => "button",
                            "name"        => "copy_btn",
                            "value"       => "前年度からコピー",
                            "extrahtml"   => "onclick=\"btn_submit('copy');\"" ));
        $arg["copy_btn"] = $objForm->ge("copy_btn");

        //事前チェック（職員マスタ）
        $chkStaff = $db->getOne(knjb0020Query::PreCheckStaffMst());
        if ($chkStaff == 0) {
            $arg["jscript"] = "OnPreError('職員マスタ');";
        }

        list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
        $result = $db->query(knjb0020Query::GetStaff($model->sectioncd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");

            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $row["FUSE_STAFFCD"] = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - (int)$simo), (int)$simo);
            } else {
                $row["FUSE_STAFFCD"] = $row["STAFFCD"];
            }
            $row["FUSE_STAFFCD"] = View::alink("knjb0020index.php", $row["FUSE_STAFFCD"],"target=right_frame",
                                               array("STAFFCD"      => $row["STAFFCD"],
                                                     "FUSE_STAFFCD" => $row["FUSE_STAFFCD"],
                                                     "cmd"          => "edit",
                                                     "NAME"         => $row["NAME"]." ".$row["NAME_KANA"]));

            $arg["data"][] = $row; 
        }

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        if ($model->cmd == "combo") {
            $arg["jscript"] = "window.open('knjb0020index.php?cmd=edit','right_frame')";
        }

        $arg["finish"]  = $objForm->get_finish();

        $result->free();
        Query::dbCheckIn($db);

        View::toHTML($model, "knjb0020Form2.html", $arg); 
    }
}
?>
