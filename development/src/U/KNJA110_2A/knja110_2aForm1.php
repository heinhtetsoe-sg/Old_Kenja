<?php

require_once('for_php7.php');

class knja110_2aForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knja110_2aindex.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();

        $db = Query::dbCheckOut();

        //起動チェック
        if (!knja110_2aQuery::ChecktoStart($db)) {
            $link = REQUESTROOT."/A/KNJA110A/knja110aindex.php?cmd=edit&schregno=".$model->schregno;
            $arg["close"] = "closing_window('$link');";
        }

        if ($model->cmd == "list2") {
            $link = REQUESTROOT."/U/KNJA110_2A/knja110_2aindex.php?cmd=edit2&schregno=".$model->schregno."&INFO_DIV=".$model->infoDiv;
            $arg["close"] = "openEdit('$link');";
        }

        //学籍基礎マスタより学籍番号と名前を取得
        $Row         = $db->getRow(knja110_2aQuery::getSchregno_name($model),DB_FETCHMODE_ASSOC);
        $arg["NO"]   = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        //編集対象選択ラジオ
        $opt = array(1, 2, 3, 4, 5, 6);   //1:生徒情報 2:保護者情報 3:保護者情報２ 4:保証人情報 5:その他情報 6:家族情報（生徒カードの親族情報）
        $model->infoDiv = ($model->infoDiv == "") ? "1" : $model->infoDiv;
        $click = " onClick=\"btn_submit('list2')\"";
        $extra = array("id=\"INFO_DIV1\"".$click, "id=\"INFO_DIV2\"".$click, "id=\"INFO_DIV3\"".$click, "id=\"INFO_DIV4\"".$click, "id=\"INFO_DIV5\"".$click, "id=\"INFO_DIV6\"".$click);
        $radioArray = knjCreateRadio($objForm, "INFO_DIV", $model->infoDiv, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) {
            $arg["top"][$key] = $val;
            if ("INFO_DIV".$model->infoDiv == $key) $arg["top"][$key."_COLOR"] = "style=\"color:yellow;\"";
        }

        //保護者情報２ラジオボタン表示
        if ($model->Properties["useGuardian2"] == '1') {
            $arg["useGuardian2"] = "ON";
        } else {
            $arg["useGuardian2"] = "";
        }

        //レイアウト切替
        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $arg["infoDiv2"] = "ON";
        } else if ($model->infoDiv == "4") {
            $arg["infoDiv4"] = "ON";
        } else if ($model->infoDiv == "5") {
            $arg["infoDiv5"] = "ON";
        } else if ($model->infoDiv == "6") {
            $arg["infoDiv6"] = "ON";
        } else {
            $arg["infoDiv1"] = "ON";
        }

        //学籍住所データよりデータを取得
        if($model->schregno)
        {
            //地区コード
            $areacd_array = array();
            $result = $db->query(knja110_2aQuery::List_AreaCd());
            while ($rowArea = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $areacd_array[$rowArea["NAMECD2"]] = $rowArea["NAME1"];
            }

            if ($model->infoDiv == "6") {
                $relaName = array();
                $result = $db->query(knja110_2aQuery::get_name_mst());
                while( $rowRela = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $relaName[$rowRela["NAMECD1"]][$rowRela["NAMECD2"]] = $rowRela["NAME1"];
                }
            }

            //緊急連絡先情報の取得(2005/10/20 ADD)
            $row2 = $db->getRow(knja110_2aQuery::getEmergencyInfo($model->schregno),DB_FETCHMODE_ASSOC);
            $result = $db->query(knja110_2aQuery::getAddress_all($model));

            while($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row  = array_merge((array)$row1,(array)$row2);

                $row["AREA_CD"] = $row["AREACD"].":".$areacd_array[$row["AREACD"]];
                $row["INFO_DIV"] = $model->infoDiv;
                $row["SEND_AREACD_SHOW"] = $row["SEND_AREACD"].":".$areacd_array[$row["SEND_AREACD"]];
                $row["SEND_DIV_SHOW"] = ($row["SEND_DIV"] == "1") ? "その他" : "その他".mb_convert_kana($row["SEND_DIV"], "N");
                if ($model->infoDiv == "6") {
                    $row["RELA_SEX"] = $relaName['Z002'][$row["RELA_SEX"]];
                    $row["RELA_RELATIONSHIP"] = $relaName['H201'][$row["RELA_RELATIONSHIP"]];
                    $row["RELA_REGIDENTIALCD"] = $relaName['H200'][$row["RELA_REGIDENTIALCD"]];
                    $row["RELA_BIRTHDAY"] = str_replace("-","/",$row["RELA_BIRTHDAY"]);
                }

                foreach (array("ADDR_FLG", "ISSUEDATE", "EXPIREDATE") as $key) {
                    if ($model->infoDiv == "2" || $model->infoDiv == "3") {
                        $head = "GUARD_";
                    } else if ($model->infoDiv == "4") {
                        $head = "GUARANTOR_";
                    } else if ($model->infoDiv == "5") {
                        $head = "SEND_";
                    } else if ($model->infoDiv == "6") {
                        $head = "RELA_";
                    } else {
                        $head = "";
                    }

                    if ($key == "ADDR_FLG") {
                        $row[$head.$key]   = ($row[$head.$key] == "1") ? "可" : "";
                    } else {
                        $row[$head.$key]   = str_replace("-","/",$row[$head.$key]);
                    }
                }
                $arg["data"][] = $row;
            }

        }
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"));

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja110_2aForm1.html", $arg);
    }
}
?>
