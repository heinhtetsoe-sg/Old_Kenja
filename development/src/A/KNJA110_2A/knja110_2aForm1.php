<?php

require_once('for_php7.php');

class knja110_2aForm1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knja110_2aindex.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();

        $db = Query::dbCheckOut();

        //起動チェック
        if (!knja110_2aQuery::checkToStart($db)) {
            $link = REQUESTROOT."/A/KNJA110A/knja110aindex.php?cmd=edit&schregno=".$model->schregno;
            $arg["close"] = "closing_window('$link');";
        }

        if ($model->cmd == "list2") {
            $link = REQUESTROOT."/A/KNJA110_2A/knja110_2aindex.php?cmd=edit2&schregno=".$model->schregno."&INFO_DIV=".$model->infoDiv;
            $arg["close"] = "openEdit('$link');";
        }

        //学籍基礎マスタより学籍番号と名前を取得
        $Row         = $db->getRow(knja110_2aQuery::getSchregnoName($model), DB_FETCHMODE_ASSOC);
        $arg["NO"]   = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        //家族番号
        $query = knja110_2aQuery::getFamilyNo($model);
        $model->familyNo = $db->getOne($query);

        //編集対象選択ラジオ
        $opt = array(1, 2, 3, 4, 5, 6);   //1:生徒情報 2:保護者情報 3:保護者情報２ 4:保証人情報 5:その他情報 6:家族情報（生徒カードの親族情報）
        $model->infoDiv = ($model->infoDiv == "") ? "1" : $model->infoDiv;
        $click = " onClick=\"btn_submit('list2')\"";
        $extra = array("id=\"INFO_DIV1\"".$click, "id=\"INFO_DIV2\"".$click, "id=\"INFO_DIV3\"".$click, "id=\"INFO_DIV4\"".$click, "id=\"INFO_DIV5\"".$click, "id=\"INFO_DIV6\"".$click);
        $radioArray = knjCreateRadio($objForm, "INFO_DIV", $model->infoDiv, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["top"][$key] = $val;
            if ("INFO_DIV".$model->infoDiv == $key) {
                $arg["top"][$key."_COLOR"] = "style=\"color:yellow;\"";
            }
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
        } elseif ($model->infoDiv == "4") {
            $arg["infoDiv4"] = "ON";
        } elseif ($model->infoDiv == "5") {
            $arg["infoDiv5"] = "ON";
        } elseif ($model->infoDiv == "6") {
            $arg["infoDiv6"] = "ON";
        } else {
            $arg["infoDiv1"] = "ON";
        }

        //学籍住所データよりデータを取得
        if ($model->schregno) {
            //地区コード
            $areacd_array = array();
            $result = $db->query(knja110_2aQuery::listAreaCd());
            while ($rowArea = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $areacd_array[$rowArea["NAMECD2"]] = $rowArea["NAME1"];
            }

            if ($model->infoDiv == "6") {
                $relaName = array();
                $result = $db->query(knja110_2aQuery::getNNMst());
                while ($rowRela = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $relaName[$rowRela["NAMECD1"]][$rowRela["NAMECD2"]] = $rowRela["NAME1"];
                }

                //兄弟姉妹の卒業区分取得
                $GrdBro = array();
                $result = $db->query(knja110_2aQuery::getGrdBrother($model));
                while ($rowBro = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $GrdBro[$rowBro["SCHREGNO"]] = '('.$rowBro["NAME1"].')';
                }

                //在卒区分（表示用）
                $regd_grd_array = array("1" => "在学", "2" => "卒業");

                //学年表示取得
                $relaG = array();
                $result = $db->query(knja110_2aQuery::getSchregRegdGdat($model));
                while ($rowG = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $relaG[$rowG["VALUE"]] = $rowG["LABEL"];
                }
            }

            //緊急連絡先情報の取得(2005/10/20 ADD)
            $row2 = $db->getRow(knja110_2aQuery::getEmergencyInfo($model->schregno), DB_FETCHMODE_ASSOC);
            $result = $db->query(knja110_2aQuery::getAddressAll($model));

            //電話番号の優先順位を項目名欄に表示
            foreach ($row2 as $field => $val) {
                if (preg_match('/^PRIORITY/', $field)) {
                    if ($val != "") {
                        $arg[$field."_SHOW"] = "<br>【 ".$val." 】";
                    }
                }
            }

            //区分名称
            $send_div_array = array("1" => "その他", "2" => "その他２", "3" => "下宿保証人");

            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row  = array_merge((array)$row1, (array)$row2);

                $row["TELNO"]               = $row["TELNO"].(strlen($row["TELNO_MEMO"]) ? "(".$row["TELNO_MEMO"].")" : "");
                $row["TELNO2"]              = $row["TELNO2"].(strlen($row["TELNO2_MEMO"]) ? "(".$row["TELNO2_MEMO"].")" : "");
                $row["EMERGENCYTELNO"]      = $row["EMERGENCYTELNO"].(strlen($row["E_TELNO_MEMO"]) ? "(".$row["E_TELNO_MEMO"].")" : "");
                $row["EMERGENCYTELNO_2"]    = $row["EMERGENCYTELNO_2"].(strlen($row["E_TELNO_MEMO_2"]) ? "(".$row["E_TELNO_MEMO_2"].")" : "");
                $row["EMERGENCYTELNO2"]     = $row["EMERGENCYTELNO2"].(strlen($row["E_TELNO_MEMO2"]) ? "(".$row["E_TELNO_MEMO2"].")" : "");
                $row["EMERGENCYTELNO2_2"]   = $row["EMERGENCYTELNO2_2"].(strlen($row["E_TELNO_MEMO2_2"]) ? "(".$row["E_TELNO_MEMO2_2"].")" : "");

                $row["AREA_CD"] = $row["AREACD"].":".$areacd_array[$row["AREACD"]];
                $row["INFO_DIV"] = $model->infoDiv;
                $row["SEND_AREACD_SHOW"] = $row["SEND_AREACD"].":".$areacd_array[$row["SEND_AREACD"]];
                $row["SEND_DIV_SHOW"] = (array_key_exists($row["SEND_DIV"], $send_div_array)) ? $send_div_array[$row["SEND_DIV"]] : "その他".mb_convert_kana($row["SEND_DIV"], "N");
                if ($model->infoDiv == "6") {
                    $row["RELA_SEX"] = $relaName['Z002'][$row["RELA_SEX"]];
                    $row["RELA_RELATIONSHIP"] = $relaName['H201'][$row["RELA_RELATIONSHIP"]];
                    $row["RELA_REGIDENTIALCD"] = $relaName['H200'][$row["RELA_REGIDENTIALCD"]];
                    $row["REGD_GRD_FLG"] = $regd_grd_array[$row["REGD_GRD_FLG"]];
                    $row["RELA_GRADE"] = $relaG[$row["RELA_GRADE"]];
                    $row["RELA_BIRTHDAY"] = str_replace("-", "/", $row["RELA_BIRTHDAY"]);
                    $row["RELA_SCHREGNO"] = $row["RELA_SCHREGNO"].$GrdBro[$row["RELA_SCHREGNO"]];
                }

                foreach (array("ADDR_FLG", "ISSUEDATE", "EXPIREDATE") as $key) {
                    if ($model->infoDiv == "2" || $model->infoDiv == "3") {
                        $head = "GUARD_";
                    } elseif ($model->infoDiv == "4") {
                        $head = "GUARANTOR_";
                    } elseif ($model->infoDiv == "5") {
                        $head = "SEND_";
                    } elseif ($model->infoDiv == "6") {
                        $head = "RELA_";
                    } else {
                        $head = "";
                    }

                    if ($key == "ADDR_FLG") {
                        $row[$head.$key]   = ($row[$head.$key] == "1") ? "可" : "";
                    } else {
                        $row[$head.$key]   = str_replace("-", "/", $row[$head.$key]);
                    }
                }
                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"));

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja110_2aForm1.html", $arg);
    }
}
