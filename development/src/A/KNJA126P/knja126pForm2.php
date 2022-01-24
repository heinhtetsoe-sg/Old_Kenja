<?php

require_once('for_php7.php');

class knja126pForm2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form2", "POST", "knja126pindex.php", "", "form2");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //学年の取得
        $query = knja126pQuery::getGradecd($model);
        $model->grade_cd = $db->getOne($query);

        //特別活動の記録出力項目取得
        $query = knja126pQuery::getNameMst($model, "D034");
        $result = $db->query($query);
        $model->itemArray = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArray[$setItem["NAMECD2"]] = $setItem;
        }

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knja126pQuery::getBehavior($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $scd = $row["DIV"] .$row["CODE"];
            $Row["RECORD"][$scd] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knja126pQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if(isset($model->warning)){
            $Row =& $model->record;
            $row =& $model->field;
        }

        //行動の記録チェックボックス
        for($i=1; $i<11; $i++)
        {
            $ival = "3" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録名前表示
        $setData = array();
        foreach ($model->itemArray as $key => $val) {            
            $setData["RECORD_NAME".$key] = $val["NAME1"];
        }
        $arg["data"][] = $setData;

        //特別活動の記録チェックボックス
        for($i=1; $i < get_count($model->itemArray) + 1; $i++)
        {
            $ival = "4" . sprintf("%02d", $i);
            $count = sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            if ($model->grade_cd == '01') {
                if ($model->itemArray[$count]["ABBV1"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } else if ($model->grade_cd == '02') {
                if ($model->itemArray[$count]["ABBV2"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } else if ($model->grade_cd == '03') {
                if ($model->itemArray[$count]["ABBV3"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } else if ($model->grade_cd == '04') {
                if ($model->itemArray[$count]["NAMESPARE1"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } else if ($model->grade_cd == '05') {
                if ($model->itemArray[$count]["NAMESPARE2"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } else if ($model->grade_cd == '06') {
                if ($model->itemArray[$count]["NAMESPARE3"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } else if ($model->grade_cd == '') {
                $extra = $check1." id=\"RECORD".$ival."\"";
            }
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録の観点
        $extra = "style=\"height:145px;\"";
        $arg["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 35, "soft", $extra, $row["SPECIALACTREMARK"]);


        /****************/
        /*  通知表参照  */
        /****************/
        //学期名表示
        for($i=1; $i<=$model->control["学期数"]; $i++) {
            $arg["SEM_NAME".$i] = $model->control["学期名"][$i];
        }

        if($model->control["学期数"] == "3") $arg["semester"] = 1;
        $arg["setCol"] = (int)$model->control["学期数"] + 1;

        //行動の記録項目名取得
        $result = $db->query(knja126pQuery::getNameMst($model, "D035"));
        $rfActItem = array();
        while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rfActItem[$rowItem["NAMECD2"]] = $rowItem["NAME1"];

        }

        //行動の記録取得
        $rfActRow = array();
        for($h=1; $h<11; $h++) {
            $rcd = sprintf("%02d", $h);
            for($i=1; $i<=$model->control["学期数"]; $i++) {
                $result = $db->query(knja126pQuery::getBehaviorSemesDat($model, $i, $rcd));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rfActRow["RECORD".$i][$rcd] = $row["RECORD"];
                }
                $result->free();
            }
        }

        //行動の記録
        $referData = array();
        for($h=1; $h<11; $h++) {
            $rcd = sprintf("%02d", $h);
            $referData["REFER_ITEMNAME"] = $rfActItem[$rcd];

            for($i=1; $i<=$model->control["学期数"]; $i++) {
                if ($model->Properties["knjdBehaviorsd_UseText"] == "1") {
                    $referData["RECORD_VAL".$i] = $rfActRow["RECORD".$i][$rcd];
                } else {
                    $check1 = ($rfActRow["RECORD".$i][$rcd] == "1") ? "checked" : "";
                    $extra = $check1." disabled";
                    $referData["RECORD_VAL".$i] = knjCreateCheckBox($objForm, "RECORD".$i.$rcd, "1", $extra, "");
                }
            }
            $arg["rdata"][] = $referData;
        }

        //通知表所見表示
        $query = knja126pQuery::getHreportRemarkDat($model);
        $result = $db->query($query);
        while($remarkRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //生活・特別活動のようす所見
            if ($model->specialactremark_gyou == "1") {
                $remarkRow["HREPORTSPECIALACTREMARK"] = knjCreateTextBox($objForm, $remarkRow["HREPORTSPECIALACTREMARK"], "HREPORTSPECIALACTREMARK", (int)$model->specialactremark_moji*2, (int)$model->specialactremark_moji*2, "");
            } else {
                $height = (int)$model->specialactremark_gyou * 13.5 + ((int)$model->specialactremark_gyou -1 ) * 3 + 5;
                $extra = "style=\"height:{$height}px;\" readonly";
                $remarkRow["HREPORTSPECIALACTREMARK"] = KnjCreateTextArea($objForm, "HREPORTSPECIALACTREMARK", $model->specialactremark_gyou, ((int)$model->specialactremark_moji * 2 + 1), "soft", $extra, $remarkRow["HREPORTSPECIALACTREMARK"]);
            }
            $arg["remark"][] = $remarkRow;
        }
        $result->free();

        //署名チェック
        $query = knja126pQuery::getOpinionsWk($model);
        $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opinion = ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) ? false : true;

        //学校種別
        $schoolkind = $db->getOne(knja126pQuery::getSchoolKind($model));

        //更新ボタン
        $extra = ((AUTHORITY < DEF_UPDATE_RESTRICT) || !$opinion || $schoolkind != 'P') ? "disabled" : "onclick=\"return btn_submit('update2')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear2')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja126pForm2.html", $arg);
    }
}
?>