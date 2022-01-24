<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjx_syukketsukirokuForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_syukketsukirokuindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        // Add by HPA for title start 2020/02/03
          $arg["TITLE"] = $arg["NAME_SHOW"]."の出欠の記録参照画面";
          echo "<script>var title= '".$arg["TITLE"]."';
              </script>";
        // Add by HPA for title end 2020/02/20

        $semesFlg = ($model->div == "1") ? $model->div : "0";
        if($semesFlg == "1") $arg["useSemes"] = 1;

        $checkFlg = "0"; // 0:なし, 1:VIRUSのみ, 2:KOUDOMEのみ, 3:両項目あり
        $width = "11%";
        $colspan = "9";
        if ($model->Properties["useSuspendBunsanHyoji"] == '1') {
            if ($model->Properties["useVirus"] == "true" && $model->Properties["useKoudome"] == "true") {
                $checkFlg = "3";
                $width = "9%";
                $colspan = "11";
            } else if ($model->Properties["useVirus"] == "true") {
                $checkFlg = "1";
                $width = "10%";
                $colspan = "10";
            } else if ($model->Properties["useKoudome"] == "true") {
                $checkFlg = "2";
                $width = "10%";
                $colspan = "10";
            }
        }
        if($semesFlg == "1") $colspan =  $colspan + 1;

        //項目サイズ切替
        $arg["WIDTH"] = $width;
        $arg["COLSPAN"] = $colspan;

        //校種、学校コード
        $schoolcd = $school_kind = "";
        if ($db->getOne(knjx_syukketsukirokuQuery::checkSchoolMst()) > 0) {
            $schoolcd       = sprintf("%012d", SCHOOLCD);
            $school_kind    = $db->getOne(knjx_syukketsukirokuQuery::getSchoolKind($model));
        }

        //学校マスタ情報
        $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, $model->exp_year, $schoolcd, $school_kind);

        //出欠の記録
        $sick = $late = $early = 0;
        $semes = 0;
        $result = $db->query(knjx_syukketsukirokuQuery::getAttendSemesDat($model, $knjSchoolMst));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semes++;
            if ($row["SUSPEND"] != "") $suspend += (int)$row["SUSPEND"];
            if ($row["MOURNING"] != "") $mourning += (int)$row["MOURNING"];
            if ($row["SICK"] != "") $sick += (int)$row["SICK"];
            if ($row["LATE"] != "") $late += (int)$row["LATE"];
            if ($row["EARLY"] != "") $early += (int)$row["EARLY"];
            if ($model->Properties["useSuspendBunsanHyoji"] == '1') {
                if ($model->Properties["useVirus"] == "true") {
                    if ($row["VIRUS"] != "") $virus += $row["VIRUS"];
                    knjCreateHidden($objForm, "VIRUS".$semes , $row["VIRUS"]); //出停伝染病
                }
                if ($model->Properties["useKoudome"] == "true") {
                    if ($row["KOUDOME"] != "") $koudome += $row["KOUDOME"];
                    knjCreateHidden($objForm, "KOUDOME".$semes , $row["KOUDOME"]); //出停交止
                }
            }
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $semesterName = $row["SEMESTERNAME"];
            //学期チェックボックス
            $extra = " aria-label= \"".$semesterName."取込\" id=\"CHECK_SEMES".$semes."\" onclick=\"return OptionUse(this, $checkFlg, $semesFlg);\"";
            $row["CHECK_SEMES"] = knjCreateCheckBox($objForm, "CHECK_SEMES".$semes, $semes, $extra, "");
            /* Edit by HPA for PC-talker 読み end 2020/02/20 */

            knjCreateHidden($objForm, "SUSPEND".$semes , $row["SUSPEND"]);   //授業日数
            knjCreateHidden($objForm, "MOURNING".$semes , $row["MOURNING"]); //忌引
            knjCreateHidden($objForm, "SICK".$semes , $row["SICK"]);         //欠席日数
            knjCreateHidden($objForm, "LATE".$semes , $row["LATE"]);         //遅刻
            knjCreateHidden($objForm, "EARLY".$semes , $row["EARLY"]);       //早退

            $arg["data"][] = $row;
        }
        $result->free();

        //停止チェックボックス
        $extra  = (strlen($suspend) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_SUSPEND\" onclick=\"return OptionUse(this, $checkFlg, $semesFlg);\"";
        $arg["CHECK_SUSPEND"] = knjCreateCheckBox($objForm, "CHECK_SUSPEND", $suspend, $extra, "");
        
        if ($model->Properties["useSuspendBunsanHyoji"] == '1') {
            if ($model->Properties["useVirus"] == "true") {
                //出停伝染病チェックボックス
                $extra  = (strlen($virus) > 0) ? "" : "disabled";
                $extra .= " id=\"CHECK_VIRUS\" onclick=\"return OptionUse(this, $checkFlg, $semesFlg);\"";
                $arg["CHECK_VIRUS"] = knjCreateCheckBox($objForm, "CHECK_VIRUS", $virus, $extra, "");
                $virusName = $db->getOne(knjx_syukketsukirokuQuery::getNameMst("NAME1","C001","19"));
                $arg["VIRUS_NAME"] = $virusName;
                knjCreateHidden($objForm, "VIRUS_NAME" , $virusName);
                $arg["useVirus"] = 1;
            }

            if ($model->Properties["useKoudome"] == "true") {
                //出停交止チェックボックス
                $extra  = (strlen($koudome) > 0) ? "" : "disabled";
                $extra .= " id=\"CHECK_KOUDOME\" onclick=\"return OptionUse(this, $checkFlg, $semesFlg);\"";
                $arg["CHECK_KOUDOME"] = knjCreateCheckBox($objForm, "CHECK_KOUDOME", $koudome, $extra, "");
                $koudomeName = $db->getOne(knjx_syukketsukirokuQuery::getNameMst("NAME1","C001","25"));
                $arg["KOUDOME_NAME"] = $koudomeName;
                knjCreateHidden($objForm, "KOUDOME_NAME" , $koudomeName);
                $arg["useKoudome"] = 1;
            }
        }

        //忌引チェックボックス
        $extra  = (strlen($mourning) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_MOURNING\" onclick=\"return OptionUse(this, $checkFlg, $semesFlg);\" aria-label = \"忌引\"";
        $arg["CHECK_MOURNING"] = knjCreateCheckBox($objForm, "CHECK_MOURNING", $mourning, $extra, "");

        //欠席日数チェックボックス
        $extra  = (strlen($sick) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_SICK\" onclick=\"return OptionUse(this, $checkFlg, $semesFlg);\"";
        $arg["CHECK_SICK"] = knjCreateCheckBox($objForm, "CHECK_SICK", $sick, $extra, "");

        //遅刻チェックボックス
        $extra  = (strlen($late) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_LATE\" onclick=\"return OptionUse(this, $checkFlg, $semesFlg);\"";
        $arg["CHECK_LATE"] = knjCreateCheckBox($objForm, "CHECK_LATE", $late, $extra, "");

        //早退チェックボックス
        $extra  = (strlen($early) > 0) ? "" : "disabled";
        $extra .= " id=\"CHECK_EARLY\" onclick=\"return OptionUse(this, $checkFlg, $semesFlg);\"";
        $arg["CHECK_EARLY"] = knjCreateCheckBox($objForm, "CHECK_EARLY", $early, $extra, "");

        //取込ボタン
        $extra  = "disabled style=\"color:#1E90FF;font:bold\" ";
        if($semesFlg == "1"){
            $extra .= "onclick=\"return dataPositionSetSemes('{$model->target}', $checkFlg);\" aria-label = \"取込\"";
        } else {
            $extra .= "onclick=\"return dataPositionSet('{$model->target}', $checkFlg);\" aria-label = \"取込\"";
        }
        $arg["button"]["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //戻るボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "onclick=\"return parent.closeit();\" aria-label = \"戻る\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "MAX_SEMES", $semes);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_syukketsukirokuForm1.html", $arg);
    }
}
?>
