<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjb3030SubFormStaff
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subformStaff", "POST", "knjb3030index.php", "", "subformStaff");

        $arg["NAME_SHOW"] = substr($model->term, 0, 4)."年度";

        $db = Query::dbCheckOut();

        if ($model->Properties["useChairStaffOrder"] == '1') {
            $arg["useChairStaffOrder"] = "1";
        }

        //SQL文発行
        //科目担任一覧取得
        $subclass = VARS::get("subclass");
        $check = $db->getOne(knjb3030Query::getStaffClass($model, $subclass));
        $staff = $db->getCol(knjb3030Query::getStaffClass($model, $subclass, "1"));

        $query = knjb3030Query::selectQuerySubFormStaff($model, $staff, $subclass);
        $result = $db->query($query);
        $i = 0;
        $param = VARS::get("param");
        $param2 = VARS::get("param2");
        $stfOrder = VARS::get("stfOrder");
        $stfOrderExplode = explode(",", $stfOrder);
        $stfOrderArray = array();
        foreach ($stfOrderExplode as $key => $val) {
            list($staffCd, $orderNo) = explode("-", $val);
            $stfOrderArray[$staffCd] = $orderNo;
        }

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $row["checkColor"] = "white";
            $row["checkFukuColor"] = "white";
            $stfOrderDisabled = "";
            if (strstr($param, $row["STAFFCD"])) {
                $stf_charge = $row["STAFFCD"]."-0";
                if (strstr($param2, $stf_charge)) {
                    $check = "";
                    $check_fuku = "checked";
                    $row["checkFukuColor"] = "#ccffcc";
                    $val_fuku = "0";
                } else {
                    $check = "checked";
                    $row["checkColor"] = "#ccffcc";
                    $check_fuku = "";
                    $val_fuku = "1";
                }
                if ($stfOrderArray[$row["STAFFCD"]]) {
                    $row["STAFF_ORDER"] = $stfOrderArray[$row["STAFFCD"]];
                }
            } else {
                $check = "";
                $check_fuku = "";
                $stfOrderDisabled = " disabled ";
            }
            //職員番号マスク処理
            list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
            $ume = "" ;
            if ($fuseji) {
                for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - $simo; $umecnt++) {
                    $ume .= $fuseji;
                }
            }
            if ($fuseji) {
                $row["FUSE_STAFFCD"] = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - $simo), $simo);
            } else {
                $row["FUSE_STAFFCD"] = $row["STAFFCD"];
            }

            $row["backcolor"] = (strstr($param, $row["STAFFCD"])) ? "#ccffcc" : "#ffffff";  //#ccffff
            //選択（チェック）
            $setId = " class=\"changeColor\" data-name=\"CHECK{$i}\" id=\"CHECK{$i}\" data-befColor=\"{$row["backcolor"]}\" data-num=\"{$i}\" ";
            $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["STAFFCD"].",".$row["STAFFNAME_SHOW"], $setId.$check, 1);

            $row["CHECK_NUM"] = $i;

            //副（チェック）
            $setId = " class=\"changeColor\" data-name=\"CHECK_FUKU{$i}\" id=\"CHECK_FUKU{$i}\" data-befColor=\"{$row["backcolor"]}\" data-num=\"{$i}\" ";
            $row["CHECK_FUKU"] = knjCreateCheckBox($objForm, "CHECK_FUKU", $row["STAFFCD"].",".$row["STAFFNAME_SHOW"], $setId.$check_fuku, 1);
            $row["CHECK_FUKU_NUM"] = $i;

            //表示順
            $extra = "style=\"text-align:right\" id=\"STAFF_ORDER{$i}\" onblur=\"this.value=toInteger(this.value);\"";
            $row["STAFF_ORDER"] = knjCreateTextBox($objForm, $row["STAFF_ORDER"], "STAFF_ORDER[]", 2, 2, $extra.$stfOrderDisabled);

            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);

        //選択ボタンを作成する
        $extra = "onclick=\"return btn_submit('".$i."')\"";
        $arg["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useChairStaffOrder", $model->Properties["useChairStaffOrder"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3030SubFormStaff.html", $arg);
    }
}
