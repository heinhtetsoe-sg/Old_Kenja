<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje063bSubForm1
{

    public function main(&$model)
    {

        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje063bindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["shidouKeikakuSansyoTable"] == "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT") {
            $arg["HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT"] = 1;
        } elseif ($model->Properties["shidouKeikakuSansyoTable"] == "HREPORT_GUIDANCE_SCHREG_DAT") {
            $arg["HREPORT_GUIDANCE_SCHREG_DAT"] = 1;
        }

        //メイン画面からの取得データ
        list ($getYear, $getSubclass) = explode(',', $model->getData);

        //科目名取得
        $subclassname = $db->getOne(knje063bQuery::getSubclassName($getSubclass));

        //データ情報表示
        $arg["info"]["YEAR"]     = $getYear;
        $arg["info"]["SUBCLASS"] = $subclassname;

        /*******************************/
        /* HREPORT_GUIDANCE_SCHREG_DAT */
        /*******************************/
        //項目情報取得
        $size = array();
        $query = knje063bQuery::getD057();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $size[$row["NAMECD2"]]["label"] = $row["NAME1"];
            $size[$row["NAMECD2"]]["moji"]  = $row["NAMESPARE1"];
        }

        //項目名表示
        $arg["label"]["VALUE_TEXT_LABEL"] = $size["05"]["label"];

        //指導計画一覧
        $query = knje063bQuery::getSemesterMst($getYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row = $db->getRow(knje063bQuery::getHreportremarkGuidanceDat($model, $getYear, $getSubclass, $row["SEMESTER"], 52), DB_FETCHMODE_ASSOC);
            //文言評価
            $moji = $size["05"]["moji"];
            $gyo  = 10;
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $extra = "aria-label = \"{$size["05"]["label"]} {$row["SEMESTERNAME"]}\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
            /* Edit by HPA for PC-talker 読み end 2020/02/20 */
            $row["VALUE_TEXT"] = knjCreateTextArea($objForm, "VALUE_TEXT", $gyo, $moji * 2, "wrap", $extra, $Row["REMARK"]);
            $arg["data"][] = $row;
        }

        /****************************************/
        /* HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT */
        /****************************************/
        $colspan = 1;
        //グループ情報
        $query = knje063bQuery::getGuidancePattern($model, $getYear);
        $result = $db->query($query);
        $model->schregInfo = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schregInfo = $row;
        }

        //項目名称セット
        $model->itemNameArr = array();
        $koumokuCnt = 0;
        $query = knje063bQuery::getItemName($model, $getYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i=1; $i <= $model->maxRemarkCnt; $i++) {
                if ($row["ITEM_REMARK".$i] != '') {
                    $row["ITEM_REMARK"] = $row["ITEM_REMARK".$i];
                    $model->itemNameArr[$i] = $row["ITEM_REMARK"];
                    $koumokuCnt++;
                }
            }
        }

        //項目名 最後の項目が対象
        $arg["koumoku"]["ITEM_REMARK"] = $model->itemNameArr[$koumokuCnt];

        //パターン取得
        $query = knje063bQuery::getNameMstPattern($model, $getYear, $model->schregInfo["GUIDANCE_PATTERN"], 'NAMESPARE1');
        $tmp = $db->getOne($query);
        $model->printPattern = substr($tmp, 1, 1);

        //単元取得
        $query = knje063bQuery::getUnit($model, $getYear, $getSubclass, $row["SEMESTER"]);
        
        $result = $db->query($query);
        $unitArr = array();
        $uniFlg  = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $unitArr[$row["VALUE"]] = $row["LABEL"];
            $uniFlg  = true;
        }
        if (!$uniFlg) {
            $unitArr["00"] = '';
            $arg["CMB"][UNITCD] = "単元は設定されていません。";
            $model->unitcd = "00";
        } else {
            $extra = "onChange=\"return btn_submit('subform1');\"";
            makeCmb($objForm, $arg, $db, $query, $model->unitcd, "UNITCD", $extra, 1);
        }

        //データ取得
        $query = knje063bQuery::getGuidanceSchregSubclassDat($model, $getYear, $getSubclass, $row["SEMESTER"]);
        $result = $db->query($query);
        $dataGuArr = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setKey = $row["SEMESTER"].'-'.$row["UNITCD"].'-'.$row["SEQ"];
            $dataGuArr[$setKey] = $row["REMARK"];
        }

        //指導計画一覧
        $query = knje063bQuery::getSemesterMst($getYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($unitArr as $unitCd => $unitName) {
            if ($unitCd != $model->unitcd) {
                            continue;
                        }
                foreach ($model->itemNameArr as $nameCd2 => $remarkTitle) {
                    $miniData = array();

                    //最後の項目だけを参照する
                    if ($koumokuCnt == $nameCd2) {
                        //データセット
                        $moji = $model->paternInfo[$model->printPattern][$nameCd2]["MOJI"];
                        $moji = 15; // 固定
                        $setKey = $row["SEMESTER"].'-'.$unitCd.'-'.$nameCd2;
                        $setRemark = $dataGuArr[$setKey];
                        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
                        $setItemUniName = $remarkTitle.(($unitCd != '00') ? "(".$unitName.")": "");
                        $extra = "aria-label = \"{$setItemUniName} {$row["SEMESTERNAME"]}\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
                        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
                        $miniData["REMARK"] = knjCreateTextArea($objForm, "REMARK", "10", $moji * 2, "", $extra, $setRemark);
                        $row["koumoku"][]   = $miniData;
                    }
                }
            }

            if (!is_array($arg["koumoku"])) {
                $arg["koumoku"][] = array();
            }
            if (!is_array($row["koumoku"])) {
                $row["koumoku"][] = array();
            }

            $arg["data2"][] = $row;
        }

        $arg["colspan"] = $colspan;

        //戻るボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
        $extra = "aria-label = \"戻る\" onclick=\"parent.current_cursor_focus();return parent.closeit()\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje063bSubForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["CMB"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
