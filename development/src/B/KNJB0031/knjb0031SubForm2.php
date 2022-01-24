<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjb0031SubForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform2", "POST", "knjb0031index.php", "", "subform2");

        $arg["NAME_SHOW"] = substr($model->term, 0, 4)."年度";

        $db = Query::dbCheckOut();

        //SQL文発行
        //科目担任一覧取得
        $subclass = VARS::get("subclass");
        $check = $db->getOne(knjb0031Query::getStaffClass($model, $subclass));
        $staff = $db->getCol(knjb0031Query::getStaffClass($model, $subclass, "1"));

        $query = knjb0031Query::selectQuerySubForm2($model->term, $staff, $subclass);
        $result = $db->query($query);
        $i = 0;
        $param = VARS::get("param");
        $param2 = VARS::get("param2");
        $counter = VARS::get("counter");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            if (strstr($param, $row["STAFFCD"])) {
                $stf_charge = $row["STAFFCD"]."-0";
                if (strstr($param2, $stf_charge)) {
                    $check = "";
                    $check_fuku = "checked";
                    $val_fuku = "0";
                } else {
                    $check = "checked";
                    $check_fuku = "";
                    $val_fuku = "1";
                }
            } else {
                $check = "";
                $check_fuku = "";
            }
            //職員番号マスク処理
            list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $row["FUSE_STAFFCD"] = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - (int)$simo), (int)$simo);
            } else {
                $row["FUSE_STAFFCD"] = $row["STAFFCD"];
            }
            //選択（チェック）
            $objForm->ae(array("type"        => "checkbox",
                               "name"          => "CHECK",
                               "value"          => $row["STAFFCD"].",".$row["STAFFNAME_SHOW"],
                               "extrahtml"    => $check,
                               "multiple"    => "1" ));

            $row["CHECK"] = $objForm->ge("CHECK");

            //副（チェック）
            $objForm->ae(array("type"        => "checkbox",
                               "name"          => "CHECK_FUKU",
                               "value"          => $row["STAFFCD"].",".$row["STAFFNAME_SHOW"],//---2004.04.22
//                               "value"          => "on",
                               "extrahtml"    => $check_fuku,
                               "multiple"    => "1" ));

            $row["CHECK_FUKU"] = $objForm->ge("CHECK_FUKU");

            $row["backcolor"] = (strstr($param, $row["STAFFCD"])) ? "#ccffcc" : "#ffffff";  //#ccffff
//            $row["backcolor"] = "#ffffff";
            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);


        //選択ボタンを作成する
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_sentaku",
                            "value"     => "選 択",
                            "extrahtml" => "onclick=\"return btn_submit('{$i}', '{$counter}')\"" ));// 04//11/12Add $i

        $arg["btn_sentaku"] = $objForm->ge("btn_sentaku");

        //終了ボタンを作成する
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return parent.closeit()\"" ));

        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd" ));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb0031SubForm2.html", $arg);
    }
}
