<?php

require_once('for_php7.php');

class knjl790hForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl790hindex.php", "", "main");
        
        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "change" || $model->cmd == "changeTestDiv") {
            $model->field["EXAMNO"] = "";
        }

        //生徒表示
        if ((!isset($model->warning))) {
            //データを取得
            $query = knjl790hQuery::getSelectQuery($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $query = knjl790hQuery::getSelectQuery($model);
                    $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                }
                $model->field["EXAMNO"] = $Row["EXAMNO"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference') {
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }

        //入試年度
        $arg["data"]["YEAR"] = $model->ObjYear . "年度";

        //入試制度
        $extra = "onChange=\"return btn_submit('change');\"";
        $query = knjl790hQuery::getNameMst($model, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分
        $extra = "onChange=\"return btn_submit('changeTestDiv');\"";
        $query = knjl790hQuery::getTestDiv($model, $model->field["APPLICANTDIV"]);
        if ($model->cmd == "change") {
            $model->field["TESTDIV"] = "";
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //受験番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $Row["EXAMNO"], "EXAMNO", 6, 4, $extra);

        //氏名
        $arg["data"]["NAME"] = $Row["NAME"];

        //氏名カナ
        $arg["data"]["NAME_KANA"] = $Row["NAME_KANA"];

        //入試種別
        $arg["data"]["KINDDIV"] = $Row["KINDDIV"];

        //性別
        $arg["data"]["SEX"] = $Row["SEX"];

        //合否
        $extra = "";
        $query = knjl790hQuery::getEntexamSettingMst($model, "L013");
        makeCmb($objForm, $arg, $db, $query, $Row["JUDGEMENT"], "JUDGEMENT", $extra, 1, "blank");

        //繰上順位
        $extra = "";
        $arg["data"]["SUB_ORDER"] = knjCreateTextBox($objForm, $Row["SUB_ORDER"], "SUB_ORDER", 6, 4, $extra);

        //面接不合格
        $extra = " id=\"UNPASS\" ";
        $checked = $Row["UNPASS"] == '1' ? " checked " : "";
        $arg["data"]["UNPASS"] = knjCreateCheckBox($objForm, "UNPASS", "1", $checked.$extra);

        //手続区分
        $extra = "";
        $query = knjl790hQuery::getEntexamSettingMst($model, "L011");
        makeCmb($objForm, $arg, $db, $query, $Row["PROCEDUREDIV"], "PROCEDUREDIV", $extra, 1, "blank");

        //手続日付
        $Row["PROCEDUREDATE"] = str_replace("-", "/", $Row["PROCEDUREDATE"]);
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", $Row["PROCEDUREDATE"], "", "", "");

        //入辞区分
        $extra = "";
        $query = knjl790hQuery::getEntexamSettingMst($model, "L012");
        makeCmb($objForm, $arg, $db, $query, $Row["ENTDIV"], "ENTDIV", $extra, 1, "blank");
        
        //学籍番号
        $extra = "";
        $arg["data"]["STUDENTNO"] = knjCreateTextBox($objForm, $Row["STUDENTNO"], "STUDENTNO", 6, 4, $extra);

        //高校推薦フラグ 学校種別 2:高校 入試区分 01:推薦
        $suisenFlg = false;
        //高校一般フラグ 学校種別 2:高校 入試区分 02:一般
        $ippanFlg = false;
        if ($model->field["APPLICANTDIV"] == "2") {
            if ($model->field["TESTDIV"] == "01") {
                $suisenFlg = true;
            } else {
                $ippanFlg = true;
            }
        }
        $arg["suisenFlg"] = $suisenFlg;
        $arg["ippanFlg"] = $ippanFlg;
        
        //一覧表示
        if ($model->field["APPLICANTDIV"] != "" && $model->field["TESTDIV"] != "" && $model->field["EXAMNO"] != "") {
            $list = array();
            $keyKyoka = 0;
            $headerKyoka = array(); //科目名
            $textboxKyoka = array(); //得点
            $total = ""; //合計
            $rank = ""; //順位
            $recomExamno = ""; //備考
            $katen = ""; //加点
            $sakubunAvg = ""; //作文平均
            $naishinTotal = ""; //内申合計
            $shikenTotal = ""; //試験合計点
            $tyofukuKaten = ""; //重複加点
            $result = $db->query(knjl790hQuery::getSUbclassList($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $total = $row["TOTAL"]; //合計

                //学校種別 2:高校 かつ 科目が面接の以外場合
                if (!($model->field["APPLICANTDIV"] == "2" && $row["SUB_TYPE"] == "1")) {
                    $headerKyoka[$keyKyoka]["TITLE"] = $row["TESTSUBCLASS_NAME"]; //科目名
                    $textboxKyoka[$keyKyoka]["FORM"] = $row["SCORE"]; //得点
                }

                //学校種別 2:高校
                if ($model->field["APPLICANTDIV"] == "2") {
                    $katen = $row["KATEN"]; //加点

                    //入試区分 推薦
                    if ($model->field["TESTDIV"] == "01") {
                        $sakubunAvg = $row["SAKUBUN_AVG"]; //作文平均
                        $naishinTotal = $row["NAISHIN_TOTAL"]; //内申合計
                    //入試区分 一般
                    } else {
                        $shikenTotal = $row["SHIKEN_TOTAL"]; //試験合計点
                        $tyofukuKaten = $row["TYOFUKU_KATEN"]; //重複加点
                    }
                }

                $total = $row["TOTAL"]; //合計
                $rank = $row["TOTAL_RANK4"]; //順位
                $recomExamno = $row["RECOM_EXAMNO"]; //備考
                $keyKyoka++;
            }
            if ($keyKyoka == 0) {
                // $model->setMessage("MSG303");
            } else {
                //科目名
                $arg["headerKyoka"] = $headerKyoka;

                //明細
                $list["textboxKyoka"] = $textboxKyoka; //得点
                $list["SAKUBUN_AVG"] = $sakubunAvg; //作文平均(推薦)
                $list["NAISHIN_TOTAL"] = $naishinTotal; //内申合計(推薦)
                $list["SHIKEN_TOTAL"] = $shikenTotal; //試験合計点(一般)
                $list["TYOFUKU_KATEN"] = $tyofukuKaten; //重複加点(一般)
                $list["KATEN"] = $katen; //加点
                $list["TOTAL"] = $total; //合計
                $list["RANK"] = $rank; //順位
                $list["RECOM_EXAMNO"] = $recomExamno; //備考
                $arg["data2"][] = $list;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $disabled);

        //hidden作成
        makeHidden($objForm);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjl790hForm1.html", $arg);
    }
}

//makeCmb
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
        if ($name == "APPLICANTDIV") {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
    }
    $value = ($value != "") ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $disabled)
{
    //検索周りの制御
    $disabled2 = ($model->field["APPLICANTDIV"] && $model->field["TESTDIV"]) ? "" : " disabled ";

    //検索ボタン
    $extra = "onclick=\"return btn_submit('reference');\"";
    $extra = $extra.$disabled2;
    $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

    //かな検索ボタン
    
    $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL790H/search_name.php?cmd=search&year={$model->ObjYear}&applicantdiv={$model->field["APPLICANTDIV"]}&testdiv={$model->field["TESTDIV"]}&examno='+document.forms[0]['EXAMNO'].value+'&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
    $extra = $extra.$disabled2;
    $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

    //前の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1');\"";
    $extra = $extra.$disabled2;
    $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

    //次の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1');\"";
    $extra = $extra.$disabled2;
    $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

    //更新ボタン
    $extra = $disabled." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

    //更新ボタン(更新後前の志願者)
    $extra = $disabled." style=\"width:150px\" onclick=\"return btn_submit('back');\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);

    //更新ボタン(更新後次の志願者)
    $extra = $disabled." style=\"width:150px\" onclick=\"return btn_submit('next');\"";
    $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectLeft");
    knjCreateHidden($objForm, "selectLeftText");
    knjCreateHidden($objForm, "selectRight");
    knjCreateHidden($objForm, "selectRightText");
}
